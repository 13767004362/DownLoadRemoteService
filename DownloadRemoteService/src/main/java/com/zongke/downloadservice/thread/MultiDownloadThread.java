package com.zongke.downloadservice.thread;

import android.os.Process;
import android.util.Log;

import com.zongke.downloadservice.constants.CommonTaskConstants;
import com.zongke.downloadservice.okhttp.OkHttpProvider;
import com.zongke.downloadservice.task.MultiDownLoadTask;
import com.zongke.downloadservice.db.bean.DownloadItemBean;
import com.zongke.downloadservice.utils.BundleBuilder;
import com.zongke.downloadservice.utils.StringUtils;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by ${xinGen} on 2017/12/22.
 * <p>
 * <p>
 * 文件多线程断点下载
 */

public class MultiDownloadThread extends BaseThread {
    private static final String TAG = MultiDownloadThread.class.getSimpleName();
    private MultiDownLoadTask downLoadTask;
    private DownloadItemBean downloadItem;
    public MultiDownloadThread(MultiDownLoadTask downLoadTask, DownloadItemBean downloadItemBean) {
        this.downLoadTask = downLoadTask;
        this.downloadItem = downloadItemBean;
    }
    @Override
    public void runTask() {
        Log.i(TAG, " MultiDownloadThread 开始执行任务");
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        Response response = null;
        Call call = null;
        OutputStream outputStream = null;
        int state = CommonTaskConstants.task_download_finish;
        RandomAccessFile randomAccessFile = null;
        try {
            if (downLoadTask.isCancel()){
                return ;
            }
            OkHttpClient okHttpClient = downLoadTask.getOkHttpClient();
            Request baseRequest = OkHttpProvider.createOkHttpRequest(downLoadTask.getDownloadUrl());
            File filePath = new File(downLoadTask.getFilePath());
            randomAccessFile = new RandomAccessFile(filePath, "rwd");
            while (downloadItem.getCurrentIndex() < downloadItem.getEndIndex()) {
                long startIndex = downloadItem.getCurrentIndex();
                long endIndex = (startIndex + CommonTaskConstants.EVERY_REQUEST_MAX_LENGTH > downloadItem.getEndIndex()) ?
                        downloadItem.getEndIndex() : startIndex + CommonTaskConstants.EVERY_REQUEST_MAX_LENGTH;
                Request request = OkHttpProvider.addHeader(baseRequest, CommonTaskConstants.HEADER_NAME_RANGE, StringUtils.createRangeHeader(startIndex, endIndex));
                call = okHttpClient.newCall(request);
                if (downLoadTask.isCancel()){
                    return ;
                }
                response = call.execute();
                //206状态码是部分数据的标识
                if (response.code() != 206) {
                    Log.i(TAG, " MultiDownloadThread 网络执行，但失败响应");
                    state = CommonTaskConstants.task_download_failure;
                    downLoadTask.deliverResult(state, BundleBuilder.createBundle(downLoadTask));
                    response.body().close();
                } else {
                    //当前一个下载模块的长度，不是总长度
                    long fileLength = response.body().contentLength();
                    Log.i(TAG, " MultiDownloadThread 网络执行完，开始写入磁盘中" + " 网络流的长度是: " + fileLength);
                    InputStream inputStream = response.body().byteStream();
                    //移动到指定位置
                    randomAccessFile.seek(downloadItem.getCurrentIndex());
                    byte[] buffer = new byte[4096];
                    int count;
                    while ((count = inputStream.read(buffer)) != -1) {
                        if (Thread.interrupted()) {
                            return;
                        }
                        //断点写入下载文件中
                        randomAccessFile.write(buffer, 0, count);
                        long writeFileLength = downloadItem.getCurrentIndex() + count;
                        //写入数据库，记录下载进度
                        downloadItem.setCurrentIndex(writeFileLength);
                        //当起始位置与终点位置相同的时候，任务执行完成。
                        if (fileLength > 0) {
                            downLoadTask.handlerProgress();
                        }
                        this.downLoadTask.getDatabaseClient().updateDownloadItem(downloadItem, StringUtils.createTaskItemUpdateSQL(), new String[]{downloadItem.getThreadName()});
                      if (downLoadTask.isCancel()){
                          return ;
                      }
                    }
                    //执行完一部分数据后，清空该记录
                    response.body().close();
                   if (downLoadTask.isCancel()){
                       return ;
                   }
                }
            }
            if (downloadItem.getCurrentIndex()>=downloadItem.getEndIndex()){
                Log.i(TAG,"该模块已经下载完成了");
                downloadItem.setState(CommonTaskConstants.task_download_finish);
                this.downLoadTask.getDatabaseClient().updateDownloadItem(downloadItem, StringUtils.createTaskItemUpdateSQL(), new String[]{downloadItem.getThreadName()});
            }
            state = CommonTaskConstants.task_download_finish;
        } catch (Exception e) {
           // e.printStackTrace();
            state = CommonTaskConstants.task_download_error;
            if (e instanceof SecurityException) {
                state = CommonTaskConstants.task_stop_thread;
                if (call != null && !call.isCanceled()) {
                    call.cancel();
                }
            }
        } finally {
            try {
                if (response != null) {
                    response.body().close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
                if (randomAccessFile != null) {
                    randomAccessFile.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            this.downLoadTask.handleResult(state);
        }
    }

}

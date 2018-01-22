package com.zongke.downloadservice.thread;

import android.os.Process;
import android.util.Log;

import com.zongke.downloadservice.constants.CommonTaskConstants;
import com.zongke.downloadservice.okhttp.OkHttpProvider;
import com.zongke.downloadservice.task.DownLoadTask;
import com.zongke.downloadservice.db.bean.DownloadItemBean;
import com.zongke.downloadservice.utils.BundleBuilder;
import com.zongke.downloadservice.utils.StringUtils;

import java.io.File;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by ${xinGen} on 2017/12/22.
 * <p>
 * 一个进行下载任务的线程,多模块下载。
 */

public class MultiDownloadThread implements Runnable {
    private static final String TAG = MultiDownloadThread.class.getSimpleName();
    private DownLoadTask downLoadTask;

    public MultiDownloadThread(DownLoadTask downLoadTask) {
        this.downLoadTask = downLoadTask;
    }

    @Override
    public void run() {
        Log.i(TAG, " MultiDownloadThread 开始执行任务");
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        this.downLoadTask.setCurrentThread(Thread.currentThread());
        Response response = null;
        Call call = null;
        OutputStream outputStream = null;
        int state = CommonTaskConstants.task_download_finish;
        RandomAccessFile randomAccessFile = null;
        try {
            if (Thread.interrupted()) {
                return;
            }
            OkHttpClient okHttpClient = downLoadTask.getOkHttpClient();
            Request baseRequest = OkHttpProvider.createOkHttpRequest(downLoadTask.getDownloadUrl());
            int itemSize = downLoadTask.getItemSize();
            File filePath = new File(downLoadTask.getFilePath());
            randomAccessFile = new RandomAccessFile(filePath, "rwd");
            int finishSize = 0;
            for (int i = 0; i < itemSize; ++i) {
                DownloadItemBean downloadItem = downLoadTask.peekDownloadItem();
                long startIndex = downloadItem.getStartIndex();
                Request request = OkHttpProvider.addHeader(baseRequest, CommonTaskConstants.HEADER_NAME_RANGE,
                        StringUtils.createRangeHeader(startIndex, downloadItem.getEndIndex()));
                call = okHttpClient.newCall(request);
                if (Thread.interrupted()) {
                    return;
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
                    Log.i(TAG, " MultiDownloadThread 网络执行完，开始写入磁盘中"+" 网络流的长度是: "+fileLength);
                    InputStream inputStream = response.body().byteStream();
                    //移动到指定位置
                    randomAccessFile.seek(downloadItem.getStartIndex());
                    byte[] buffer = new byte[4096];
                    int count;
                    long total = 0;
                    while ((count = inputStream.read(buffer)) != -1) {
                        if (Thread.interrupted()) {
                            return;
                        }
                        total += count;
                        long writeFileLength = startIndex+ total;
                        if (fileLength > 0) {
                            //已经写入文件的数据长度=上次写入文件的长度（）+现在写入的长度
                            int progress = (int) ((float) writeFileLength * 100 / downLoadTask.getDownloadTaskLength());
                            Log.i(TAG, downLoadTask.getDownloadUrl() + " 写入进度 " + progress);
                            downLoadTask.deliverResult(CommonTaskConstants.task_download_progress, BundleBuilder.createBundle(downLoadTask, progress));
                        }
                        //断点写入下载文件中
                        randomAccessFile.write(buffer, 0, count);
                        //写入数据库，记录下载进度
                        downloadItem.setStartIndex(writeFileLength);
                        this.downLoadTask.getDatabaseClient().updateDownloadItem(downloadItem, StringUtils.createTaskItemUpdateSQL(), new String[]{downloadItem.getThreadName()});
                    }
                    //执行完一部分数据后，清空该记录
                    this.downLoadTask.getDatabaseClient().deleteDownloadItem(StringUtils.createTaskItemUpdateSQL(), new String[]{downloadItem.getThreadName()});
                    response.body().close();
                    if (Thread.interrupted()) {
                        return;
                    }
                    ++finishSize;
                }
            }
            Log.i(TAG," 计算完成的部分是 "+finishSize+" "+itemSize);
            if (finishSize == itemSize) {
                downLoadTask.getDownloadTaskBean().setState(CommonTaskConstants.task_download_finish);
                downLoadTask.getDatabaseClient().
                        updateDownloadTask(downLoadTask.getDownloadTaskBean(),
                                StringUtils.createTaskQuerySQL()
                                , new String[]{downLoadTask.getDownloadUrl()});
                state = CommonTaskConstants.task_download_finish;
                Log.i(TAG, " MultiDownloadThread 写入磁盘操作完成");
                downLoadTask.deliverResult(state, BundleBuilder.createBundle(downLoadTask));
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (e instanceof SecurityException) {
                state = CommonTaskConstants.task_stop_thread;
                if (call != null && !call.isCanceled()) {
                    call.cancel();
                }
            }
          //  downLoadTask.deliverResult(state, BundleBuilder.createBundle(downLoadTask));
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
            this.downLoadTask.setCurrentThread(null);
            this.downLoadTask.handleResult(state);
            Thread.interrupted();
        }
    }

}

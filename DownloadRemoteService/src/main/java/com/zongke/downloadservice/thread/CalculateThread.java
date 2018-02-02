package com.zongke.downloadservice.thread;

import android.os.Process;
import android.util.Log;

import com.zongke.downloadservice.constants.CommonTaskConstants;
import com.zongke.downloadservice.db.bean.DownloadTaskBean;
import com.zongke.downloadservice.db.sqlite.DownloadTaskConstants;
import com.zongke.downloadservice.okhttp.OkHttpProvider;
import com.zongke.downloadservice.task.MultiDownLoadTask;
import com.zongke.downloadservice.db.bean.DownloadItemBean;
import com.zongke.downloadservice.utils.BundleBuilder;
import com.zongke.downloadservice.utils.FileUtils;
import com.zongke.downloadservice.utils.StringUtils;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by ${xinGen} on 2018/1/5.
 *
 * 文件的计算线程，用于计算多线程断点下载的数据。
 */

public class CalculateThread extends BaseThread {
    private static final String TAG = CalculateThread.class.getSimpleName();
    private MultiDownLoadTask downLoadTask;
    public CalculateThread(MultiDownLoadTask downLoadTask) {
        this.downLoadTask = downLoadTask;
    }
    @Override
    public void runTask() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        this.downLoadTask.setCurrentThread(Thread.currentThread());
        Call call = null;
        Response response = null;
        RandomAccessFile randomAccessFile = null;
        int state = 0;
        try {
            if (Thread.interrupted()) {
                return;
            }
            String[] args= new String[]{downLoadTask.getDownloadUrl()};
            List<DownloadTaskBean> downloadTaskBeanList = downLoadTask.getDatabaseClient()
                    .queryDownloadTask(StringUtils.createTaskQuerySQL()
                            ,args);
            Log.i(TAG,"数据库中记录 的个数"+downloadTaskBeanList.size());
            if (downLoadTask.isAgainTask()){
                   deleteOldDownLoadTask(downloadTaskBeanList);
            }
            if (downloadTaskBeanList.size() > 0) {
                // 已经下载完成过了。
                if (downloadTaskBeanList.get(0).getState() == CommonTaskConstants.task_download_finish) {
                    Log.i(TAG,"数据库中查询到任务已经完成： : "+downLoadTask.getDownloadUrl());
                    state=CommonTaskConstants.task_already_download;
                    downLoadTask.getDownloadTaskBean().setFilePath(downloadTaskBeanList.get(0).getFilePath());
                    this.downLoadTask.deliverResult(state, BundleBuilder.createBundle(downLoadTask));
                } else {
                    List<DownloadItemBean> downloadItemBeanList = downLoadTask.getDatabaseClient().queryDownloadItem(StringUtils.createTaskItemQuerySQL(), args);
                    long fileLength=downloadTaskBeanList.get(0).getDownloadTaskLength();
                    downLoadTask.setDownloadTaskLength(downloadTaskBeanList.get(0).getDownloadTaskLength());
                    this.downLoadTask.getDownloadItemList().addAll(downloadItemBeanList);
                    state = CommonTaskConstants.task_calculate_finish;
                    Log.i(TAG,"数据库中查询到任务对应的多部分下载的数据 : "+downloadItemBeanList.size()+" 文件长度"+fileLength);
                }
            } else {
                if (Thread.interrupted()) {
                    return;
                }
                OkHttpClient okHttpClient = this.downLoadTask.getOkHttpClient();
                Request request = OkHttpProvider.createOkHttpRequest(this.downLoadTask.getDownloadUrl());
                call = okHttpClient.newCall(request);
                if (Thread.interrupted()) {
                    return;
                }
                response = call.execute();
                if (response.isSuccessful()) {
                    // rwd,实时写到底层设备
                    randomAccessFile = new RandomAccessFile(new File(this.downLoadTask.getFilePath()), "rwd");
                    long totalLength = response.body().contentLength();
                    // 设置文件的大小
                    randomAccessFile.setLength(totalLength);
                    int threadCount = this.downLoadTask.getThreadCount();
                    //计算每个线程需要下载的数据大小,下一个下载起点是上一个下载的终点
                    long averageCount =( totalLength %threadCount==0)?(totalLength/threadCount):(totalLength/threadCount+1);

                    Log.i(TAG,"数据库中记录当前的下载任务  : "+this.downLoadTask.getDownloadUrl());
                    for (int i = 0; i < threadCount; ++i) {
                        String threadName = UUID.randomUUID().toString();
                        long startIndex = i * averageCount;
                        long endIndex = (i + 1) * averageCount - 1;
                        if (i == threadCount - 1) {
                            //实际上，最后一个下载模块，结束尾部位置》=文件总长度
                            endIndex = totalLength - 1;
                        }
                        DownloadItemBean downloadItemBean = new DownloadItemBean.Builder()
                                .setThreadName(threadName)
                                .setStartIndex(startIndex)
                                .setCurrentIndex(startIndex)
                                .setEndIndex(endIndex)
                                .setBindTaskId(downLoadTask.getDownloadUrl())
                                .builder();
                        this.downLoadTask.getDownloadItemList().add(downloadItemBean);
                        Log.i(TAG,"计算线程中计算多模块的下载数据  : "+(i+1)+" "+startIndex+" "+endIndex+" 文件总长度 "+totalLength);
                    }
                    this.downLoadTask.setDownloadTaskLength(totalLength);
                    this.downLoadTask.getDatabaseClient().insertDownloadTask(downLoadTask.getDownloadTaskBean());
                    this.downLoadTask.getDatabaseClient().insertDownloadItem(downLoadTask.getDownloadItemList());
                    state = CommonTaskConstants.task_calculate_finish;
                } else {
                    Log.i(TAG,"计算线程中的网络请求失败  : "+this.downLoadTask.getDownloadUrl());
                    response.close();
                    state = CommonTaskConstants.task_calculate_failure;
                    this.downLoadTask.deliverResult(CommonTaskConstants.task_download_failure, BundleBuilder.createBundle(downLoadTask));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (e instanceof SecurityException) {
                if (call != null) {
                    call.cancel();
                }
            }
            state = CommonTaskConstants.task_stop_thread;
            this.downLoadTask.deliverResult(state, BundleBuilder.createBundle(downLoadTask));
        } finally {
            try {
                if (response != null) {
                    response.close();
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
    private void deleteOldDownLoadTask(List<DownloadTaskBean> downloadTaskBeanList){
        if (downloadTaskBeanList.size()>0){
            DownloadTaskBean downloadTaskBean=downloadTaskBeanList.get(0);
            File oldFile=new File(downloadTaskBean.getFilePath());
            FileUtils.deleteFile(oldFile);
            String[] args=new String[]{downloadTaskBean.getDownloadUrl()};
            //删除下载任务中记录
            downLoadTask.getDatabaseClient().deleteDownloadTask(DownloadTaskConstants.COLUMN_DOWNLOAD_URL+"=?",args);
            //删除下载模块中记录
            downLoadTask.getDatabaseClient().deleteDownloadItem(DownloadTaskConstants.COLUMN_BIND_TASK_ID+"=?",args);
            downloadTaskBeanList.clear();
        }
    }
}

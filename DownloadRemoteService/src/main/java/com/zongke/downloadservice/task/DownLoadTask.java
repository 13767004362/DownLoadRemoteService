package com.zongke.downloadservice.task;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.os.ResultReceiver;

import com.zongke.downloadservice.client.DatabaseClient;
import com.zongke.downloadservice.constants.CommonTaskConstants;
import com.zongke.downloadservice.db.bean.DownloadItemBean;
import com.zongke.downloadservice.db.bean.DownloadTaskBean;
import com.zongke.downloadservice.thread.CalculateThread;
import com.zongke.downloadservice.thread.MultiDownloadThread;
import com.zongke.downloadservice.thread.ThreadManager;

import java.util.concurrent.ArrayBlockingQueue;

import okhttp3.OkHttpClient;


/**
 * Created by ${xinGen} on 2017/12/22.
 */
public class DownLoadTask {
    private DownloadTaskBean downloadTaskBean;
    /**
     * 通讯对象
     */
    private ResultReceiver resultReceiver;
    /**
     * 当前线程对象
     */
    private Thread currentThread;
    /**
     * 下载的Runnable接口
     */
    private MultiDownloadThread downloadThread;
    /**
     * 计算的Runnable接口
     */
    private CalculateThread calculateThread;
    /**
     * 线程管理类
     */
    private ThreadManager threadManager;
    /**
     * 数据库操作类
     */
    private DatabaseClient databaseClient;
    /**
     * OkHttpClient网络配置
     */
    private OkHttpClient okHttpClient;
    /**
     * 线程个数
     */
    private int threadCount;
    /**
     * 下载线程中的下载任务列表
     */
    private ArrayBlockingQueue<DownloadItemBean> downloadItemList;
    /**
     * 是否重新下载的标志：先删除文件和数据库记录
     */
    private boolean isAgainTask;


    public DownLoadTask(ThreadManager threadManager, DatabaseClient databaseClient) {
        this.threadManager = threadManager;
        this.databaseClient = databaseClient;
        this.downloadItemList = new ArrayBlockingQueue<>(3);
        this.calculateThread = new CalculateThread(this);
        this.threadCount = CommonTaskConstants.DOWNLOAD_THREAD_ACCOUNT;
        this.downloadThread = new MultiDownloadThread(this);
    }

    /**
     * 初始化一些参数
     *
     * @param downloadUrl
     * @param filePath
     * @param resultReceiver
     */
    public void init(String downloadUrl, String filePath, ResultReceiver resultReceiver) {
        this.downloadTaskBean = new DownloadTaskBean.Builder()
                .setDownloadUrl(downloadUrl)
                .setFilePath(filePath)
                .builder();
        this.resultReceiver = resultReceiver;
    }

    public long getDownloadTaskLength() {
        return this.downloadTaskBean.getDownloadTaskLength();
    }

    public void setDownloadTaskLength(long totalLength) {
        this.downloadTaskBean.setDownloadTaskLength(totalLength);
    }

    public DownloadTaskBean getDownloadTaskBean() {
        return this.downloadTaskBean;
    }

    public Thread getCurrentThread() {
        synchronized (this) {
            return currentThread;
        }
    }

    public int getThreadCount() {
        return threadCount;
    }

    public void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }

    public String getDownloadUrl() {
        return this.downloadTaskBean.getDownloadUrl();
    }

    public ResultReceiver getResultReceiver() {
        return resultReceiver;
    }

    public OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }

    public void setOkHttpClient(OkHttpClient okHttpClient) {
        this.okHttpClient = okHttpClient;
    }

    public String getFilePath() {
        return this.downloadTaskBean.getFilePath();
    }


    public CalculateThread getCalculateThread() {
        return calculateThread;
    }

    /**
     * 传递结果
     */
    public MultiDownloadThread getDownloadThread() {
        return downloadThread;
    }

    @SuppressLint("RestrictedApi")
    public void deliverResult(int stateCode, Bundle bundle) {
        if (resultReceiver != null) {
            this.resultReceiver.send(stateCode, bundle);
        }
    }

    public void offerDownLoadItem(DownloadItemBean downloadItem) {
        synchronized (this) {
            this.downloadItemList.offer(downloadItem);
        }
    }

    public DownloadItemBean peekDownloadItem() {
        synchronized (this) {
            return this.downloadItemList.poll();
        }
    }

    public int getItemSize() {
        return this.downloadItemList.size();
    }

    public boolean isAgainTask() {
        return isAgainTask;
    }

    public void setAgainTask(boolean againTask) {
        isAgainTask = againTask;
    }

    /**
     * 记住当前执行线程的Id
     *
     * @param currentThread
     */
    public void setCurrentThread(Thread currentThread) {
        synchronized (this) {
            this.currentThread = currentThread;
        }
    }

    /**
     * 释放资源
     */
    public void releaseResource() {
        this.resultReceiver = null;
        this.downloadItemList.clear();
        this.downloadTaskBean = null;
        this.isAgainTask=false;
    }

    /**
     * 处理 结果
     */
    public void handleResult(int state) {
        switch (state) {
            case CommonTaskConstants.task_download_finish:
            case CommonTaskConstants.task_download_failure:
            case CommonTaskConstants.task_calculate_failure:
            case CommonTaskConstants.task_already_download:
                this.threadManager.recycleDownloadTask(this);
                break;
            case CommonTaskConstants.task_calculate_finish:
                this.threadManager.executeDownloadTask(this);
                break;
            default:
                break;
        }
    }


    public DatabaseClient getDatabaseClient() {
        return databaseClient;
    }
}

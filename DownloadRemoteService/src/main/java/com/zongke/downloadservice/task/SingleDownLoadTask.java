package com.zongke.downloadservice.task;

import android.os.Bundle;
import android.support.v4.os.ResultReceiver;

import com.zongke.downloadservice.client.DownLoadClient;
import com.zongke.downloadservice.constants.CommonTaskConstants;

import com.zongke.downloadservice.thread.SingleDownloadThread;
import com.zongke.downloadservice.thread.ThreadManager;

import okhttp3.OkHttpClient;


/**
 * Created by ${xinGen} on 2017/12/22.
 */
public class SingleDownLoadTask {
    /**
     * 下载地址
     */
    private String downloadUrl;
    /**
     * 文件存储路径
     */
    private String filePath;
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
    private SingleDownloadThread downloadThread;
    /**
     * 线程管理类
     */
    private ThreadManager threadManager;
    /**
     * OkHttpClient网络配置
     */
    private OkHttpClient okHttpClient;
    /**
     * 下载管理类
     */
    private DownLoadClient downLoadClient;

    public SingleDownLoadTask(ThreadManager threadManager, DownLoadClient downLoadClient) {
        this.threadManager = threadManager;
        this.downLoadClient=downLoadClient;
        this.downloadThread = new SingleDownloadThread(this);
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public void setResultReceiver(ResultReceiver resultReceiver) {
        this.resultReceiver = resultReceiver;
    }

    public Thread getCurrentThread() {
        synchronized (this) {
            return currentThread;
        }
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public ResultReceiver getResultReceiver() {
        return resultReceiver;
    }

    public SingleDownloadThread getDownloadThread() {
        return downloadThread;
    }

    public OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }

    public void setOkHttpClient(OkHttpClient okHttpClient) {
        this.okHttpClient = okHttpClient;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    /**
     * 传递结果
     */
    public void deliverResult(int stateCode, Bundle bundle) {
        if (resultReceiver != null) {
            this.resultReceiver.send(stateCode, bundle);
        }
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
        this.downloadUrl = null;
        this.resultReceiver = null;
        this.filePath = null;
    }

    /**
     * 处理 结果
     */
    public void handleResult(int state) {
        switch (state) {
            case CommonTaskConstants.task_download_finish:
            case CommonTaskConstants.task_download_failure:
                this.downLoadClient.removeDownloadTask(this);
                this.threadManager.recycleDownloadTask(this);
                break;
            default:
                break;
        }
    }

}

package com.zongke.downloadservicesdk.client;

import android.content.Context;
import android.os.Bundle;

import com.zongke.downloadservicesdk.listener.DownLoadResultListener;
import com.zongke.downloadservicesdk.task.DownLoadTask;

/**
 * Created by ${xinGen} on 2017/12/25.
 */

public interface ServiceClient {
    /**
     * 初始化
     * @param context
     */
    void init(Context context);
    /**
     * 开始下载任务
     * @param url
     * @param filePath
     * @param resultListener
     */
    void startSingleDownloadTask(String url, String filePath, DownLoadResultListener resultListener);

    /**
     *  断点下载
     * @param url
     * @param filePath
     * @param resultListener
     */
    void startMultiDownloadTask(String url, String filePath, DownLoadResultListener resultListener);
    /**
     * 开启下载任务
     * @param url
     */
    void stopDownloadTask(String url);

    /**
     * 删除以前的文件，重新下载
     * @param url
     * @param filePath
     * @param resultListener
     */
    void againStartDownloadTask(String url, String filePath, DownLoadResultListener resultListener);
    /**
     * 处理结果
     * @param resultCode
     * @param resultData
     */
    void handlerResult(int resultCode, Bundle resultData);
    /**
     * 移除下载任务
     * @param downLoadTask
     */
    void removeDownloadTask(DownLoadTask downLoadTask);


    /**
     * 反初始化
     */
    void unInit();
}

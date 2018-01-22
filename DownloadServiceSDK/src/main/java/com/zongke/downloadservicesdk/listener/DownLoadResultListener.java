package com.zongke.downloadservicesdk.listener;

/**
 * Created by ${xingen} on 2017/12/25.
 */

public interface DownLoadResultListener {
    /**
     * 任务下载中
     * @param url
     * @param filePath
     * @param progress
     */
    void taskProgress(String url,String filePath,int progress);
    /**
     * 任务完成
     * @param url
     * @param filePath
     */
    void taskFinish( String url,String filePath);

    /**
     * 任务失败
     */
    void taskFailure(String url);

    /**
     * 任务已经下载完成了
     * @param url
     */
    void taskAlreadyDownload(String url,String filePath);
}

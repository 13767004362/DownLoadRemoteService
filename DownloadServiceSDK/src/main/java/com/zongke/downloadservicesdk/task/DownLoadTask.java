package com.zongke.downloadservicesdk.task;

import com.zongke.downloadservicesdk.listener.DownLoadResultListener;

/**
 * Created by ${xingen} on 2017/12/25.
 */

public class DownLoadTask {
    private String url;
    private String filePath;
    private DownLoadResultListener resultListener;
    private int state;
    private int progress;
    public DownLoadTask(String url, String filePath, DownLoadResultListener resultListener) {
        this.url = url;
        this.filePath = filePath;
        this.resultListener = resultListener;
    }
    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public String getFilePath() {
        return filePath;
    }
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    public DownLoadResultListener getResultListener() {
        return resultListener;
    }
    public void setResultListener(DownLoadResultListener resultListener) {
        this.resultListener = resultListener;
    }
    public int getState() {
        return state;
    }
    public void setState(int state) {
        this.state = state;
    }
    public int getProgress() {
        return progress;
    }
    public void setProgress(int progress) {
        this.progress = progress;
    }

    /**
     * 释放资源
     */
    public void releaseResource(){
        this.url=null;
        this.filePath=null;
        this.resultListener=null;
    }
}

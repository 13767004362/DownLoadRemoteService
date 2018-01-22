package com.zongke.downloadservice.db.bean;

/**
 * Created by ${xinGen} on 2018/1/5.
 * <p>
 * 每个模块下载信息的实体
 */

public class DownloadItemBean {
    private long startIndex;
    private long endIndex;
    private String threadName;
    private String bindTaskId;

    public String getBindTaskId() {
        return bindTaskId;
    }
    public String getThreadName() {
        return threadName;
    }
    public long getStartIndex() {
        return startIndex;
    }
    public long getEndIndex() {
        return endIndex;
    }

    public void setStartIndex(long startIndex) {
        this.startIndex = startIndex;
    }

    public  static class Builder {
        private DownloadItemBean downloadItem;
        public Builder() {
            this.downloadItem = new DownloadItemBean();
        }
        public Builder setThreadName(String threadName) {
            this.downloadItem.threadName = threadName;
            return this;
        }
        public Builder setStartIndex(long startIndex) {
            downloadItem.startIndex = startIndex;
            return this;
        }
        public Builder setBindTaskId(String bindTaskId) {
            this.downloadItem.bindTaskId = bindTaskId;
            return this;
        }
        public Builder setEndIndex(long endIndex) {
            this.downloadItem.endIndex = endIndex;
            return this;
        }
        public DownloadItemBean builder() {
            return this.downloadItem;
        }
    }
}

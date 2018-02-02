// DownLoadRemoteService.aidl
package com.zongke.downloadservice;

import android.support.v4.os.ResultReceiver;
// Declare any non-default types here with import statements

interface DownLoadRemoteService {
    /**
     * 开启一个下载任务
     */
     void  startDownLoadTask(int mode,String downloadUrl,String filePath,in ResultReceiver resultReceiver);
     /**
      * 停止一个下载任务
      */
     void  stopDownloadTask(String downloadUrl);
    /**
     * 删除旧的文件，从新下载
     */
     void againStartDownloadTask(String downloadUrl,String filePath,in ResultReceiver resultReceiver);
}

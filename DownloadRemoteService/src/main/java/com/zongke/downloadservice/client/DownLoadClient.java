package com.zongke.downloadservice.client;

import android.content.IntentFilter;
import android.support.v4.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;

import com.zongke.downloadservice.okhttp.OkHttpProvider;
import com.zongke.downloadservice.task.SingleDownLoadTask;
import com.zongke.downloadservice.task.MultiDownLoadTask;
import com.zongke.downloadservice.thread.ThreadManager;

import java.lang.annotation.Target;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import okhttp3.OkHttpClient;

/**
 * Created by ${xinGen} on 2017/12/21.
 * 下载的客户端
 */

public class DownLoadClient {
    private static DownLoadClient instance;
    private final OkHttpClient okHttpClient;
    private final ThreadManager threadManager;
    private DatabaseClient databaseClient;
    /**
     * 一个记录当前正在下载的任务
     */
    private final List<SingleDownLoadTask> goingSingleTaskList;
    private final LinkedBlockingQueue<MultiDownLoadTask> goingMultiTasksList;
    private final String tag=DownLoadClient.class.getSimpleName();

    private DownLoadClient() {
        this.goingSingleTaskList = new CopyOnWriteArrayList<>();
        this.goingMultiTasksList=new LinkedBlockingQueue<>();
        this.okHttpClient = OkHttpProvider.createOkHttpClient();
        this.threadManager = ThreadManager.getInstance();
        this.databaseClient= DatabaseClient.getInstance();
    }
    public static synchronized DownLoadClient getInstance() {
        if (instance == null) {
            instance = new DownLoadClient();
        }
        return instance;
    }

    /**
     * 直接下载
     * @param downloadUrl
     * @param filePath
     * @param resultReceiver
     */
    public void startSingDownloadTask(String downloadUrl, String filePath, ResultReceiver resultReceiver){
             this.threadManager.executorTask(()->{
                 SingleDownLoadTask downLoadTask=this.threadManager.startSingleDownLoadThread(this,downloadUrl,filePath,resultReceiver);
                 if (!goingSingleTaskList.contains(downLoadTask)) {
                     goingSingleTaskList.add(downLoadTask);
                 }
             });
    }

    /**
     * 断点下载
     * @param downloadUrl
     * @param filePath
     * @param resultReceiver
     */
    public void startMultiDownloadTask(String downloadUrl, String filePath, ResultReceiver resultReceiver) {
        this.threadManager.executorTask(() -> {
            MultiDownLoadTask task = this.threadManager.startMultiDownLoadThread(this,this.okHttpClient, downloadUrl, filePath, resultReceiver);
            this.goingMultiTasksList.offer(task);
        });
    }
    public void stopDownloadTask(String downloadUrl) {
        removeMultiDownloadTask(downloadUrl);
        this.threadManager.stopSingleDownloadThread(getDownloadTask(downloadUrl));
    }
    public void deleteOldAndAgainDownloadTask(String downloadUrl, String filePath, ResultReceiver resultReceiver){
          this.threadManager.executorTask(()->{
              MultiDownLoadTask task = this.threadManager.startMultiDownLoadThread(this,this.okHttpClient, downloadUrl, filePath, resultReceiver,true);
              this.goingMultiTasksList.offer(task);
          });
    }
    public void releaseResource() {

        this.goingSingleTaskList.clear();
        this.goingMultiTasksList.clear();
        this.threadManager.stopAllDownloadTask();
    }
    /**
     * 根据指定下载地址获取对应的下载任务。
     *
     * @param downloadUrl
     * @return
     */
    private SingleDownLoadTask getDownloadTask(String downloadUrl) {
        SingleDownLoadTask downLoadTask = null;
        if (goingSingleTaskList.size() > 0) {
            for (SingleDownLoadTask task : this.goingSingleTaskList) {
                if (downloadUrl.equals(task.getDownloadUrl())) {
                    downLoadTask = task;
                    goingSingleTaskList.remove(task);
                    break;
                } else {
                    continue;
                }
            }
        }
        return downLoadTask;
    }

    /**
     * 移除
     * @param url
     */
    private  void removeMultiDownloadTask(String url){
        if (TextUtils.isEmpty(url)){
            return;
        }
        for (MultiDownLoadTask multiDownLoadTask:this.goingMultiTasksList){
             if (url.equalsIgnoreCase(multiDownLoadTask.getDownloadUrl())){
                 Log.i(tag,"移除的url "+url+" 匹配到的任务 "+multiDownLoadTask);
                 multiDownLoadTask.setCancel(true);
                 this.goingMultiTasksList.remove(multiDownLoadTask);
             }
        }
    }

    /**
     * 移除
     * @param multiDownLoadTask
     */
    public  void removeMultiDownloadTask(MultiDownLoadTask multiDownLoadTask){
        if (this.goingMultiTasksList.contains(multiDownLoadTask)){
            this.goingMultiTasksList.remove(multiDownLoadTask);
        }
    }
    /**
     * 移除结束了的下载任务
     * @param downLoadTask
     */
    public void removeDownloadTask(SingleDownLoadTask downLoadTask){
        if (downLoadTask!=null&& goingSingleTaskList.contains(downLoadTask)){
            goingSingleTaskList.remove(downLoadTask);
        }
    }
    public OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }

    public DatabaseClient getDatabaseClient() {
        return databaseClient;
    }
}

package com.zongke.downloadservice.client;

import android.provider.ContactsContract;
import android.support.v4.os.ResultReceiver;

import com.zongke.downloadservice.okhttp.OkHttpProvider;
import com.zongke.downloadservice.task.DownLoadTask;
import com.zongke.downloadservice.thread.ThreadManager;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

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
    private final List<DownLoadTask> goingTaskList;
    private DownLoadClient() {
        this.goingTaskList = new CopyOnWriteArrayList<>();
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
    public void startDownloadTask(String downloadUrl, String filePath, ResultReceiver resultReceiver) {
        this.threadManager.executorTask(() -> {
            DownLoadTask task = this.threadManager.startDownLoadThread(this.databaseClient,this.okHttpClient, downloadUrl, filePath, resultReceiver);
            if (!goingTaskList.contains(task)) {
                goingTaskList.add(task);
            }
        });
    }
    public void stopDownloadTask(String downloadUrl) {
        this.threadManager.stopDownloadThread(getDownloadTask(downloadUrl));
    }
    public void deleteOldAndAgainDownloadTask(String downloadUrl, String filePath, ResultReceiver resultReceiver){
          this.threadManager.executorTask(()->{
              DownLoadTask task = this.threadManager.startDownLoadThread(this.databaseClient,this.okHttpClient, downloadUrl, filePath, resultReceiver,true);
              if (!goingTaskList.contains(task)) {
                  goingTaskList.add(task);
              }
          });
    }
    public void releaseResource() {
        this.goingTaskList.clear();
        this.threadManager.stopAllDownloadTask();
    }
    /**
     * 根据指定下载地址获取对应的下载任务。
     *
     * @param downloadUrl
     * @return
     */
    private DownLoadTask getDownloadTask(String downloadUrl) {
        DownLoadTask downLoadTask = null;
        if (goingTaskList.size() > 0) {
            for (DownLoadTask task : this.goingTaskList) {
                if (downloadUrl.equals(task.getDownloadUrl())) {
                    downLoadTask = task;
                    goingTaskList.remove(task);
                    break;
                } else {
                    continue;
                }
            }
        }
        return downLoadTask;
    }
}

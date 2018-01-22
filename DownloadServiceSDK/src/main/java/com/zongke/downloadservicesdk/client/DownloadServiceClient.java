package com.zongke.downloadservicesdk.client;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.zongke.downloadservicesdk.connection.RemoteServiceConnection;
import com.zongke.downloadservicesdk.db.Constants;
import com.zongke.downloadservicesdk.exector.MainThreadExecutor;
import com.zongke.downloadservicesdk.listener.DownLoadResultListener;
import com.zongke.downloadservicesdk.receiver.DownloadResultReceiver;
import com.zongke.downloadservicesdk.task.DownLoadTask;
import com.zongke.downloadservicesdk.thread.ThreadHandlerBuilder;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by ${xinGen} on 2017/12/25.
 */
public class DownloadServiceClient implements ServiceClient {
    private static final String TAG = DownloadServiceClient.class.getSimpleName();
    private static DownloadServiceClient instance;
    private RemoteServiceConnection remoteServiceConnection;
    private final List<DownLoadTask> downLoadTaskList;
    private final DownloadResultReceiver resultReceiver;
    private Looper looper;
    private Context appContext;
    private Handler handler;
    private MainThreadExecutor mainThreadExecutor;

    private DownloadServiceClient() {
        this.mainThreadExecutor = new MainThreadExecutor();
        this.remoteServiceConnection = new RemoteServiceConnection();
        this.downLoadTaskList = new CopyOnWriteArrayList<>();
        this.handler = ThreadHandlerBuilder.createThreadHandler(TAG);
        this.looper = this.handler.getLooper();
        this.resultReceiver = new DownloadResultReceiver(handler, this);
    }

    /**
     * 单例模式
     *
     * @return
     */
    public static synchronized DownloadServiceClient getInstance() {
        if (instance == null) {
            instance = new DownloadServiceClient();
        }
        return instance;
    }

    @Override
    public void init(Context context) {
        this.appContext = context.getApplicationContext();
        if (remoteServiceConnection != null) {
            remoteServiceConnection.bindRemoteService(this.appContext);
        }
    }

    @Override
    public void startDownloadTask(String url, String filePath, DownLoadResultListener resultListener) {
        if (remoteServiceConnection != null) {
            DownLoadTask downLoadTask = new DownLoadTask(url, filePath, resultListener);
            this.remoteServiceConnection.startDownLoadTask(downLoadTask, this.resultReceiver);
            this.downLoadTaskList.add(downLoadTask);
        }
    }

    @Override
    public void againStartDownloadTask(String url, String filePath, DownLoadResultListener resultListener) {
        if (remoteServiceConnection != null) {
            DownLoadTask downLoadTask = new DownLoadTask(url, filePath, resultListener);
            this.remoteServiceConnection.againStartDownloadTask(downLoadTask, this.resultReceiver);
            this.downLoadTaskList.add(downLoadTask);
        }
    }

    @Override
    public void stopDownloadTask(String url) {
        if (remoteServiceConnection != null) {
            this.remoteServiceConnection.stopDownloadTask(url);
        }
        for (DownLoadTask downLoadTask : this.downLoadTaskList) {
            if (url.equals(downLoadTask.getUrl())) {
                this.removeDownloadTask(downLoadTask);
                break;
            }
        }
    }

    @Override
    public void unInit() {
        if (remoteServiceConnection != null) {
            this.remoteServiceConnection.unBindRemoteService(this.appContext);
        }
        this.looper.quit();
        this.downLoadTaskList.clear();
    }

    @Override
    public void removeDownloadTask(DownLoadTask downLoadTask) {
        if (downLoadTask != null) {
            this.downLoadTaskList.remove(downLoadTask);
            downLoadTask.releaseResource();
        }
    }

    @Override
    public void handlerResult(int resultCode, Bundle resultData) {
        String url = resultData.getString(Constants.KEY_DOWN_LOAD_URL);
        int progress = resultData.getInt(Constants.KEY_PROGRESS);
        // Log.i(TAG, " url " + url + " 进度是：" + progress);
        if (TextUtils.isEmpty(url)) {
            return;
        }
        for (DownLoadTask downLoadTask : downLoadTaskList) {
            if (url.equals(downLoadTask.getUrl())) {
                executionUpdateTask(resultCode, progress, downLoadTask);
                break;
            } else {
                continue;
            }
        }
    }

    /**
     * @param resultCode
     * @param progress
     * @param downLoadTask
     */
    private void executionUpdateTask(final int resultCode, final int progress, final DownLoadTask downLoadTask) {
        this.mainThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                DownLoadResultListener resultListener = downLoadTask.getResultListener();
                if (resultListener != null) {
                    switch (resultCode) {
                        case Constants.task_progress:
                            resultListener.taskProgress(downLoadTask.getUrl(), downLoadTask.getFilePath(), progress);
                            break;
                        case Constants.task_failure:
                            resultListener.taskFailure(downLoadTask.getUrl());
                            removeDownloadTask(downLoadTask);
                            break;
                        case Constants.task_finish:
                            resultListener.taskFinish(downLoadTask.getUrl(), downLoadTask.getFilePath());
                            removeDownloadTask(downLoadTask);
                            break;
                        case Constants.task_already_download:
                            resultListener.taskAlreadyDownload(downLoadTask.getUrl(), downLoadTask.getFilePath());
                            removeDownloadTask(downLoadTask);
                        default:
                            break;
                    }
                }
            }

        });
    }
}

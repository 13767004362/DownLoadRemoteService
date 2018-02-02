package com.zongke.downloadservice.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.os.ResultReceiver;

import com.zongke.downloadservice.DownLoadRemoteService;
import com.zongke.downloadservice.client.DatabaseClient;
import com.zongke.downloadservice.client.DownLoadClient;
import com.zongke.downloadservice.constants.CommonTaskConstants;

/**
 * Created by ${xingen} on 2017/12/22.
 */

public class DownLoadService extends Service {
    /**
     * 下载的客户端
     */
    private DownLoadClient client;
    @Override
    public void onCreate() {
        super.onCreate();
        this.client = DownLoadClient.getInstance();
        DatabaseClient.getInstance().init(getApplicationContext());
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        this.client.releaseResource();
    }
    /**
     * 进程通讯
     */
    private final DownLoadRemoteService.Stub mBinder = new DownLoadRemoteService.Stub() {
        @Override
        public void startDownLoadTask(int mode,String downloadUrl, String filePath, ResultReceiver resultReceiver) throws RemoteException {
            switch (mode){
                case CommonTaskConstants.mode_single:
                    client.startSingDownloadTask(downloadUrl, filePath, resultReceiver);
                    break;
                case CommonTaskConstants.mode_multi:
                    client.startMultiDownloadTask(downloadUrl, filePath, resultReceiver);
                    break;
            }
        }
        @Override
        public void stopDownloadTask(String downloadUrl) throws RemoteException {
            client.stopDownloadTask(downloadUrl);
        }
        @Override
        public  void againStartDownloadTask(String downloadUrl,String filePath, ResultReceiver resultReceiver)throws RemoteException {
               client.deleteOldAndAgainDownloadTask(downloadUrl,filePath,resultReceiver);
        }
    };

}

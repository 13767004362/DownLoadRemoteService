package com.zongke.downloadservicesdk.connection;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.zongke.downloadservice.DownLoadRemoteService;
import com.zongke.downloadservicesdk.receiver.DownloadResultReceiver;
import com.zongke.downloadservicesdk.task.DownLoadTask;
import com.zongke.downloadservicesdk.utils.IntentBuilder;

/**
 * Created by ${xingen} on 2017/12/25.
 * 远程服务连接类
 */

public class RemoteServiceConnection {
    private final String tag = RemoteServiceConnection.class.getSimpleName();
    private DownLoadRemoteService downLoadRemoteService;
    public RemoteServiceConnection() {
    }

    public void bindRemoteService(Context context) {
      //  Log.i(tag, " downLoadRemoteService bindRemoteService ");
        context.bindService(IntentBuilder.createServiceIntent(), serviceConnection, Context.BIND_AUTO_CREATE);
    }

    public void unBindRemoteService(Context context) {
       // Log.i(tag, " downLoadRemoteService unBindRemoteService ");
        context.unbindService(serviceConnection);
    }
    public boolean isConnection() {
        return downLoadRemoteService == null ? false : true;
    }

    public void startDownLoadTask(DownLoadTask downLoadTask, DownloadResultReceiver downloadResultReceiver) {
        if (isConnection()) {
            try {
              //  Log.i(tag, " downLoadRemoteService startDownLoadTask ");
                downLoadRemoteService.startDownLoadTask(downLoadTask.getUrl(), downLoadTask.getFilePath(),downloadResultReceiver );
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }
    public void againStartDownloadTask(DownLoadTask downLoadTask, DownloadResultReceiver downloadResultReceiver){
        if (isConnection()) {
            try {
                //  Log.i(tag, " downLoadRemoteService startDownLoadTask ");
                downLoadRemoteService.againStartDownloadTask(downLoadTask.getUrl(), downLoadTask.getFilePath(),downloadResultReceiver );
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }
    public void stopDownloadTask(String url) {
        if (isConnection()) {
            try {
              //  Log.i(tag, " downLoadRemoteService stopDownloadTask");
                downLoadRemoteService.stopDownloadTask(url);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            downLoadRemoteService = DownLoadRemoteService.Stub.asInterface(service);
          //  Log.i(tag, " downLoadRemoteService  onServiceConnected ");
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            downLoadRemoteService = null;
         //   Log.i(tag, " downLoadRemoteService  onServiceDisconnected ");
        }
    };
}

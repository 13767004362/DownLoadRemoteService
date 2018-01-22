package com.zongke.downloadservicesdk.exector;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import java.util.concurrent.Executor;

/**
 * Created by ${xinGen} on 2017/12/22.
 *
 * 主线程
 */

public class MainThreadExecutor implements Executor {
    private  final Handler handler;
    public MainThreadExecutor(){
        this.handler=new Handler(Looper.getMainLooper());
    }
    @Override
    public void execute(@NonNull Runnable command) {
        this.handler.post(command);
    }
}

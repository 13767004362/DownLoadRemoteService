package com.zongke.downloadservicesdk.thread;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;

/**
 * Created by ${xingen} on 2017/12/25.
 */

public class ThreadHandlerBuilder {
    public static Handler createThreadHandler(String threadName){
        HandlerThread handlerThread = new HandlerThread(threadName, Process.THREAD_PRIORITY_BACKGROUND);
        handlerThread.start();
        Handler  handler = new Handler( handlerThread.getLooper());
        return handler;
    }
}

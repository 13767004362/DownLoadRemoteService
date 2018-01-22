package com.zongke.downloadservice.thread;

import android.support.v4.os.ResultReceiver;
import android.util.Log;

import com.zongke.downloadservice.client.DatabaseClient;
import com.zongke.downloadservice.executor.MainThreadExecutor;
import com.zongke.downloadservice.task.DownLoadTask;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

/**
 * Created by ${xinGen} on 2017/12/22.
 * <p>
 * 线程池管理类
 */

public class ThreadManager {
    private static final String TAG = ThreadManager.class.getSimpleName();
    private static ThreadManager instance;
    private final int CODE_POOL_SIZE;
    private final int maxPoolSize;
    private final int KEEP_ALIVE_TIME;
    private final TimeUnit TIME_UNIT;
    /**
     * 配置线程池,下载
     */
    private ThreadPoolExecutor downLoadPoolExecutor;
    /**
     * 配置线程池，计算
     */
    private ThreadPoolExecutor calculatePoolExecutor;
    /**
     * 计算线程的线程池的队列
     */
    private final BlockingQueue<Runnable> calculateThreadQueue;
    /**
     * 下载线程的线程池的队列
     */
    private final BlockingQueue<Runnable> downLoadThreadQueue;
    /**
     * 复用性，DownLoadTask
     */
    private final BlockingQueue<DownLoadTask> downLoadTaskQueue;
    /**
     * 主线程
     */
    private final MainThreadExecutor mainThreadExecutor;
    /**
     * 根据android运行的
     */
    private final int NUMBER_OF_CORE;

    private ThreadManager() {
        this.CODE_POOL_SIZE =8;
        this.maxPoolSize =8;
        this.KEEP_ALIVE_TIME = 1;
        this.NUMBER_OF_CORE =Runtime.getRuntime().availableProcessors();
        this.TIME_UNIT = TimeUnit.SECONDS;
        this.calculateThreadQueue=new LinkedBlockingQueue<>();
        this.downLoadThreadQueue = new LinkedBlockingQueue<>();
        this.downLoadTaskQueue = new LinkedBlockingQueue<>();
        this.downLoadPoolExecutor = new ThreadPoolExecutor(this.CODE_POOL_SIZE, this.maxPoolSize, this.KEEP_ALIVE_TIME, this.TIME_UNIT, this.downLoadThreadQueue);
        this.calculatePoolExecutor=new ThreadPoolExecutor(this.NUMBER_OF_CORE,this.NUMBER_OF_CORE,this.KEEP_ALIVE_TIME,this.TIME_UNIT,this.calculateThreadQueue);
        this.mainThreadExecutor = new MainThreadExecutor();
    }
    public static synchronized ThreadManager getInstance() {
        if (instance == null) {
            instance = new ThreadManager();
        }
        return instance;
    }

    /**
     * 从线程池中开启一个线程
     *
     * @param okHttpClient
     * @param url
     * @param resultReceiver
     */
    public DownLoadTask startDownLoadThread(DatabaseClient databaseClient,OkHttpClient okHttpClient, String url, String filePath, ResultReceiver resultReceiver) {
       return startDownLoadThread(databaseClient,okHttpClient,url,filePath,resultReceiver,false);
    }
    public DownLoadTask startDownLoadThread(DatabaseClient databaseClient,OkHttpClient okHttpClient, String url, String filePath, ResultReceiver resultReceiver,boolean isAgain) {
        Log.i(TAG, "start download task 下载地址是：" + url);
        DownLoadTask downLoadTask = this.downLoadTaskQueue.poll();
        if (downLoadTask == null) {
            downLoadTask = new DownLoadTask(this,databaseClient);
            downLoadTask.setOkHttpClient(okHttpClient);
        }
        downLoadTask.setAgainTask(isAgain);
        downLoadTask.init(url,filePath,resultReceiver);
        this.executeCalculateTask(downLoadTask);
        return downLoadTask;
    }

    /**
     * 停止全部下载任务的线程
     */
    public void stopAllDownloadTask() {
        DownLoadTask[] downLoadTasks = new DownLoadTask[this.downLoadTaskQueue.size()];
        this.downLoadTaskQueue.toArray(downLoadTasks);
        int taskSize = downLoadTasks.length;
        synchronized (this) {
            for (int i = 0; i < taskSize; ++i) {
                Thread thread = downLoadTasks[i].getCurrentThread();
                if (null != thread) {
                    thread.interrupt();
                }
            }
        }
    }
    /**
     * 停止正在执行的线程，且从线程池队列中移除
     *
     * @param downLoadTask
     */
    public void stopDownloadThread(DownLoadTask downLoadTask) {
        this.executorTask(() -> {
            if (downLoadTask != null) {
                Log.i(TAG, "停止任务，地址是：" + downLoadTask.getDownloadUrl());
                Thread currentThread = downLoadTask.getCurrentThread();
                if (currentThread != null) {
                    currentThread.interrupt();
                }
                this.calculateThreadQueue.remove(downLoadTask.getCalculateThread());
                this.downLoadThreadQueue.remove(downLoadTask.getDownloadThread());
            }
        });
    }
    /**
     * 回收DownloadTask对象，从新放入队列中
     *
     * @param downLoadTask
     */
    public void recycleDownloadTask(DownLoadTask downLoadTask) {
        this.executorTask(() -> {
            Log.i(TAG, "回收任务，地址是：" + downLoadTask.getDownloadUrl());
            downLoadTask.releaseResource();
            this.downLoadTaskQueue.offer(downLoadTask);
        });
    }
    /**
     * 执行计算任务
     * @param downLoadTask
     */
    private void executeCalculateTask(DownLoadTask downLoadTask){
        this.calculatePoolExecutor.execute(downLoadTask.getCalculateThread());
    }
    /**
     * 执行完计算后，执行下载任务
     * @param downLoadTask
     */
    public void executeDownloadTask(DownLoadTask downLoadTask){
        this.downLoadPoolExecutor.execute(downLoadTask.getDownloadThread());
    }
    /**
     * 执行任务
     *
     * @param runnable
     */
    public void executorTask(Runnable runnable) {
        this.mainThreadExecutor.execute(runnable);
    }
}

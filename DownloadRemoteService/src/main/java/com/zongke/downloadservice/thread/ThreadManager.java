package com.zongke.downloadservice.thread;

import android.support.v4.os.ResultReceiver;
import android.util.Log;

import com.zongke.downloadservice.client.DatabaseClient;
import com.zongke.downloadservice.client.DownLoadClient;
import com.zongke.downloadservice.executor.MainThreadExecutor;
import com.zongke.downloadservice.task.SingleDownLoadTask;
import com.zongke.downloadservice.task.MultiDownLoadTask;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
     * 复用性，SingleDownLoadTask
     */
    private final BlockingQueue<SingleDownLoadTask> downLoadTaskQueue;
    /**
     * 主线程
     */
    private final MainThreadExecutor mainThreadExecutor;
    /**
     * 根据android运行的cpu运行个数
     */
    private final int NUMBER_OF_CORE;
    /**
     * 默认情况下，单文件直接下载的线程个数
     */
   public static final  int single_file_down_thread_size=3;
    
    private ThreadManager() {
        this.CODE_POOL_SIZE =single_file_down_thread_size;
        this.maxPoolSize =single_file_down_thread_size;
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
     * 执行文件直接下载
     *
     * @param downLoadClient
     * @param url
     * @param resultReceiver
     */
    public SingleDownLoadTask startSingleDownLoadThread(DownLoadClient downLoadClient, String url, String filePath, ResultReceiver resultReceiver) {
      SingleDownLoadTask downLoadTask  = this.downLoadTaskQueue.poll();
      if (downLoadTask==null){
          downLoadTask=new SingleDownLoadTask(this,downLoadClient);
          downLoadTask.setDownloadUrl(url);
          downLoadTask.setFilePath(filePath);
          downLoadTask.setOkHttpClient(downLoadClient.getOkHttpClient());
          downLoadTask.setResultReceiver(resultReceiver);
      }
      this.executeDownloadTask(downLoadTask.getDownloadThread());
      return  downLoadTask;
    }
    /**
     * 执行文件多线程断点下载
     *
     * @param okHttpClient
     * @param url
     * @param resultReceiver
     */
    public MultiDownLoadTask startMultiDownLoadThread(DownLoadClient downLoadClient, OkHttpClient okHttpClient, String url, String filePath, ResultReceiver resultReceiver) {
       return startMultiDownLoadThread(downLoadClient,okHttpClient,url,filePath,resultReceiver,false);
    }
    public MultiDownLoadTask startMultiDownLoadThread(DownLoadClient downLoadClient, OkHttpClient okHttpClient, String url, String filePath, ResultReceiver resultReceiver, boolean isAgain) {
        Log.i(TAG, "start download task 下载地址是：" + url);
        MultiDownLoadTask downLoadTask = new MultiDownLoadTask(this,downLoadClient);
        downLoadTask.setOkHttpClient(okHttpClient);
        downLoadTask.setAgainTask(isAgain);
        downLoadTask.init(url,filePath,resultReceiver);
        this.executeCalculateTask(downLoadTask.getCalculateThread());
        return downLoadTask;
    }
    /**
     * 停止全部下载任务的线程
     */
    public void stopAllDownloadTask() {
        SingleDownLoadTask[] downLoadTasks = new SingleDownLoadTask[this.downLoadTaskQueue.size()];
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
    public void stopSingleDownloadThread(SingleDownLoadTask downLoadTask) {
        this.executorTask(() -> {
            if (downLoadTask != null) {
                Log.i(TAG, "停止任务，地址是：" + downLoadTask.getDownloadUrl());
                Thread currentThread = downLoadTask.getCurrentThread();
                if (currentThread != null) {
                    currentThread.interrupt();
                }
                this.downLoadThreadQueue.remove(downLoadTask.getDownloadThread());
            }
        });
    }
    /**
     * 回收DownloadTask对象，从新放入队列中
     *
     * @param downLoadTask
     */
    public void recycleDownloadTask(SingleDownLoadTask downLoadTask) {
        this.executorTask(() -> {
            Log.i(TAG, "回收任务，地址是：" + downLoadTask.getDownloadUrl());
            downLoadTask.releaseResource();
            this.downLoadTaskQueue.offer(downLoadTask);
        });
    }
    /**
     * 创建指定线程个数的线程池
     * @param number
     * @return
     */
    public ExecutorService createThreadPool(int number){
      return Executors.newFixedThreadPool(number);
    }
    /**
     * 执行计算任务
     * @param runnable
     */
    private void executeCalculateTask(Runnable runnable){
        this.calculatePoolExecutor.execute(runnable);
    }
    /**
     * 执行完计算后，执行下载任务
     * @param runnable
     */
    public void executeDownloadTask(Runnable runnable){
        this.downLoadPoolExecutor.execute(runnable);
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

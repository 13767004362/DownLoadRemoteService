package com.zongke.downloadservice.task;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.os.ResultReceiver;
import android.util.Log;

import com.zongke.downloadservice.client.DatabaseClient;
import com.zongke.downloadservice.client.DownLoadClient;
import com.zongke.downloadservice.constants.CommonTaskConstants;
import com.zongke.downloadservice.db.bean.DownloadItemBean;
import com.zongke.downloadservice.db.bean.DownloadTaskBean;
import com.zongke.downloadservice.thread.CalculateThread;
import com.zongke.downloadservice.thread.MultiDownloadThread;
import com.zongke.downloadservice.thread.ThreadManager;
import com.zongke.downloadservice.utils.BundleBuilder;
import com.zongke.downloadservice.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;

import okhttp3.OkHttpClient;


/**
 * Created by ${xinGen} on 2017/12/22.
 */
public class MultiDownLoadTask {
    private final String TAG = MultiDownLoadTask.class.getSimpleName();
    private DownloadTaskBean downloadTaskBean;
    /**
     * 通讯对象
     */
    private ResultReceiver resultReceiver;
    /**
     * 当前线程对象
     */
    private Thread currentThread;

    /**
     * 计算的Runnable接口
     */
    private CalculateThread calculateThread;
    /**
     * 线程管理类
     */
    private ThreadManager threadManager;
    /**
     * 数据库操作类
     */
    private DatabaseClient databaseClient;
    /**
     * OkHttpClient网络配置
     */
    private OkHttpClient okHttpClient;
    /**
     * 线程个数
     */
    private int threadCount;
    /**
     * 下载线程中的下载任务列表
     */
    private List<DownloadItemBean> downloadItemList;
    /**
     * 是否重新下载的标志：先删除文件和数据库记录
     */
    private boolean isAgainTask;
    /**
     * 下载的多线程
     */
    private ExecutorService executorService;
    /**
     * 是否取消的标志
     */
    private volatile boolean isCancel;
    private DownLoadClient downLoadClient;

    public MultiDownLoadTask(ThreadManager threadManager, DownLoadClient downLoadClient) {
        this.threadManager = threadManager;
        this.databaseClient = downLoadClient.getDatabaseClient();
        this.downLoadClient=downLoadClient;
        this.downloadItemList = new ArrayList<>();
        this.calculateThread = new CalculateThread(this);
        this.threadCount = CommonTaskConstants.DOWNLOAD_THREAD_ACCOUNT;
    }

    /**
     * 初始化一些参数
     *
     * @param downloadUrl
     * @param filePath
     * @param resultReceiver
     */
    public void init(String downloadUrl, String filePath, ResultReceiver resultReceiver) {
        this.downloadTaskBean = new DownloadTaskBean.Builder()
                .setDownloadUrl(downloadUrl)
                .setFilePath(filePath)
                .builder();
        this.resultReceiver = resultReceiver;
    }

    public long getDownloadTaskLength() {
        return this.downloadTaskBean.getDownloadTaskLength();
    }

    public void setDownloadTaskLength(long totalLength) {
        this.downloadTaskBean.setDownloadTaskLength(totalLength);
    }

    public boolean isCancel() {
        return isCancel;
    }

    public void setCancel(boolean cancel) {
        isCancel = cancel;
        try {
            if (getCurrentThread()!=null){
                getCurrentThread().interrupt();
            }
            if (executorService!=null){
                executorService.shutdownNow();
                executorService=null;
            }
            releaseResource();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public DownloadTaskBean getDownloadTaskBean() {
        return this.downloadTaskBean;
    }

    public Thread getCurrentThread() {
        synchronized (this) {
            return currentThread;
        }
    }

    public int getThreadCount() {
        return threadCount;
    }

    public void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }

    public String getDownloadUrl() {
        return this.downloadTaskBean.getDownloadUrl();
    }

    public ResultReceiver getResultReceiver() {
        return resultReceiver;
    }

    public OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }

    public void setOkHttpClient(OkHttpClient okHttpClient) {
        this.okHttpClient = okHttpClient;
    }

    public String getFilePath() {
        return this.downloadTaskBean.getFilePath();
    }


    public CalculateThread getCalculateThread() {
        return calculateThread;
    }



    @SuppressLint("RestrictedApi")
    public void deliverResult(int stateCode, Bundle bundle) {
        if (resultReceiver != null) {
            this.resultReceiver.send(stateCode, bundle);
        }
    }

    public List<DownloadItemBean> getDownloadItemList() {
        return downloadItemList;
    }

    public void setDownloadItemList(List<DownloadItemBean> downloadItemList) {
        this.downloadItemList = downloadItemList;
    }

    public int getItemSize() {
        return this.downloadItemList.size();
    }

    public boolean isAgainTask() {
        return isAgainTask;
    }

    public void setAgainTask(boolean againTask) {
        isAgainTask = againTask;
    }

    /**
     * 记住当前执行线程的Id
     *
     * @param currentThread
     */
    public void setCurrentThread(Thread currentThread) {
        synchronized (this) {
            this.currentThread = currentThread;
        }
    }

    /**
     * 释放资源
     */
    public void releaseResource() {
        this.resultReceiver = null;
        this.downloadItemList.clear();
        this.downloadTaskBean = null;
        this.isAgainTask = false;
    }

    /**
     * 处理 结果
     */
    public void handleResult(int state) {
        if (isCancel()){
            return ;
        }
        switch (state) {
            case CommonTaskConstants.task_download_finish:
                handlerResult();
                break;
            case CommonTaskConstants.task_download_failure:
                downLoadClient.removeMultiDownloadTask(this);
                break;
            case CommonTaskConstants.task_calculate_failure:
                downLoadClient.removeMultiDownloadTask(this);
                break;
            case CommonTaskConstants.task_already_download:
                releaseResource();
                downLoadClient.removeMultiDownloadTask(this);
                break;
            case CommonTaskConstants.task_calculate_finish:
                startMultiDownloadThread();
                break;
            default:
                break;
        }
    }

    public void handlerProgress() {
        int total = 0;
        for (DownloadItemBean downloadItemBean : downloadItemList) {
            total += downloadItemBean.getUploadLength();
        }
        if (isCancel()){
            return;
        }
        //计算出剩余还剩下多少，然后100- 剩余进度=上传进度
        final int progress = (int) (((total) * 100) / downloadTaskBean.getDownloadTaskLength());
        Log.i(TAG, "当前线程 " + Thread.currentThread().getName() + " 上传了多少 " + progress);
        deliverResult(CommonTaskConstants.task_download_progress, BundleBuilder.createBundle(this, progress));
    }

    private void handlerResult() {
        int finish = 0;
        for (DownloadItemBean downloadItemBean : downloadItemList) {
            if (downloadItemBean.getState() == CommonTaskConstants.task_download_finish) {
                finish++;
            }
        }
        if (isCancel()){
            return;
        }
        Log.i(TAG, " 当前线程" + Thread.currentThread().getName() + "handlerResult " + finish + " 任务的个数 " + downloadItemList.size());
        if (finish == downloadItemList.size()) {
            deliverResult(CommonTaskConstants.task_download_finish, BundleBuilder.createBundle(this));
            //更新task的记录
            getDownloadTaskBean().setState(CommonTaskConstants.task_download_finish);
            getDatabaseClient().updateDownloadTask(getDownloadTaskBean(), StringUtils.createTaskQuerySQL(), new String[]{getDownloadUrl()});
            //删除对应的item记录
            getDatabaseClient().deleteDownloadItem(StringUtils.createTaskItemQuerySQL(), new String[]{getDownloadUrl()});
            Log.i(TAG, " MultiDownloadThread 写入磁盘操作完成");
            releaseResource();
            downLoadClient.removeMultiDownloadTask(this);
        }
    }
    public DatabaseClient getDatabaseClient() {
        return databaseClient;
    }
    /**
     * 开启下载任务
     */
    private void startMultiDownloadThread() {
        if (isCancel()){
            return;
        }
        executorService = threadManager.createThreadPool(ThreadManager.single_file_down_thread_size);
        for (DownloadItemBean downloadItemBean : downloadItemList) {
            if (downloadItemBean.getState() != CommonTaskConstants.task_download_finish) {
                executorService.execute(new MultiDownloadThread(this, downloadItemBean));
            }
        }
    }
}

package com.zongke.downloadservice.thread;

import android.support.v4.os.ResultReceiver;
import android.util.Log;

import com.zongke.downloadservice.constants.CommonTaskConstants;
import com.zongke.downloadservice.okhttp.OkHttpProvider;
import com.zongke.downloadservice.task.SingleDownLoadTask;
import com.zongke.downloadservice.utils.BundleBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by ${xinGen} on 2018/2/1.
 *
 * 单文件直接下载的线程
 */

public class SingleDownloadThread extends BaseThread {
    private static final String TAG=SingleDownloadThread.class.getSimpleName();
    private SingleDownLoadTask downLoadTask;
    public SingleDownloadThread(SingleDownLoadTask downLoadTask) {
        this.downLoadTask = downLoadTask;
    }
    @Override
    public void runTask() {
        Log.i(TAG," SingleDownloadThread 开始执行任务");

        this.downLoadTask.setCurrentThread(Thread.currentThread());
        Response response = null;
        Call call = null;
        OutputStream outputStream = null;
        int state = CommonTaskConstants.task_download_finish;
        ResultReceiver resultReceiver = downLoadTask.getResultReceiver();
        try {
            if (Thread.interrupted()) {
                return;
            }
            OkHttpClient okHttpClient = downLoadTask.getOkHttpClient();
            Request request = OkHttpProvider.createOkHttpRequest(downLoadTask.getDownloadUrl());
            call = okHttpClient.newCall(request);
            if (Thread.interrupted()) {
                return;
            }
            response = call.execute();
            if (!response.isSuccessful()) {
                Log.i(TAG," SingleDownloadThread 网络执行，但失败响应");
                state = CommonTaskConstants.task_download_failure;
                response.close();
                downLoadTask.deliverResult(state, BundleBuilder.createBundle(downLoadTask));
            } else {
                Log.i(TAG," SingleDownloadThread 网络执行完，开始写入磁盘中");
                long fileLength = response.body().contentLength();
                InputStream inputStream = response.body().byteStream();
                File filePath=new File(downLoadTask.getFilePath());
                if (filePath!=null&&filePath.exists()){
                    filePath.delete();
                }
                outputStream = new FileOutputStream(filePath);
                byte[] buffer = new byte[4096];
                int count;
                long total = 0;
                while ((count = inputStream.read(buffer)) != -1) {
                    if (Thread.interrupted()) {
                        return;
                    }
                    total += count;
                    if (fileLength > 0) {
                        if (resultReceiver != null) {
                            int progress = (int) ((float) total * 100 / fileLength);
                            Log.i(TAG,downLoadTask.getDownloadUrl()+" 写入进度 "+progress);
                            downLoadTask.deliverResult(CommonTaskConstants.task_download_progress, BundleBuilder.createBundle(downLoadTask,progress));
                        }
                    }
                    outputStream.write(buffer, 0, count);
                }
                outputStream.flush();
                if (Thread.interrupted()) {
                    return;
                }
                state = CommonTaskConstants.task_download_finish;
                Log.i(TAG," SingleDownloadThread 写入磁盘操作完成");
                downLoadTask.deliverResult(state, BundleBuilder.createBundle(downLoadTask));
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (e instanceof SecurityException) {
                state=CommonTaskConstants.task_stop_thread;
                if (call != null && !call.isCanceled()) {
                    call.cancel();
                }
            }
        } finally {
            try {
                if (response != null) {
                    response.body().close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            this.downLoadTask.setCurrentThread(null);
            this.downLoadTask.handleResult(state);
            Thread.interrupted();
        }
    }
}

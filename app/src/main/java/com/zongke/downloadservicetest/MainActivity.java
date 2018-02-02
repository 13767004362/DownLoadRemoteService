package com.zongke.downloadservicetest;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.zongke.downloadservicesdk.client.DownloadServiceClient;
import com.zongke.downloadservicesdk.client.ServiceClient;
import com.zongke.downloadservicesdk.listener.DownLoadResultListener;

import java.io.File;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, DownLoadResultListener {
    private Button btn1, btn2;
    private ProgressBar progressBar1, progressBar2;
    private ServiceClient serviceClient;
    private final String TAG = MainActivity.class.getSimpleName();
    private String url1 = "http://downpack.baidu.com/appsearch_AndroidPhone_1012271b.apk";
    private String url2 = "http://yun.aiwan.hk/1441972507.apk";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initDownloadService();
        initView();
    }

    /**
     *
     *  开启远程下载服务，先安装远程下载服务的apk.
     *
     */
    private void initDownloadService() {
        try {
            Intent intent = new Intent("com.zongke.downloadservice.service.DownLoadService");
            intent.setPackage("com.zongke.downloadservice");
            startService(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
        serviceClient = DownloadServiceClient.getInstance();
        serviceClient.init(getApplicationContext());
    }

    /**
     * 初始化控件
     */
    private void initView() {
        this.progressBar1 = (ProgressBar) findViewById(R.id.main_progressbar_1);
        this.progressBar2 = (ProgressBar) findViewById(R.id.main_progressbar_2);
        this.btn1 = (Button) this.findViewById(R.id.main_button_1);
        this.btn2 = (Button) this.findViewById(R.id.main_button_2);
        this.btn1.setOnClickListener(this);
        this.btn2.setOnClickListener(this);
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.main_button_1:
                if (btn1.getText().toString().equals("开始")) {
                    String filePath=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator + "baidu.apk";
                    serviceClient.startSingleDownloadTask(url1,filePath , this);
                    btn1.setText("暂停");
                } else if (btn1.getText().toString().equals("重新下载")) {
                    serviceClient.againStartDownloadTask(url1, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator + "baidu.apk", this);
                    btn1.setText("暂停");
                } else {
                    serviceClient.stopDownloadTask(url1);
                    btn1.setText("开始");
                }
                break;
            case R.id.main_button_2:
                if (btn2.getText().toString().equals("开始")) {
                    String filePath=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator + "baihewang.apk";
                    serviceClient.startMultiDownloadTask(url2,filePath , this);
                    btn2.setText("暂停");
                } else if (btn2.getText().toString().equals("重新下载")) {
                    String filePath=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator + "baihewang.apk";
                    serviceClient.againStartDownloadTask(url2,filePath , this);
                    btn2.setText("暂停");
                } else {
                    serviceClient.stopDownloadTask(url2);
                    btn2.setText("开始");
                }
                break;
            default:
                break;
        }
    }

    /**
     * 文件已经下载存在的回调监听
     * @param url
     * @param filePath
     */
    @Override
    public void taskAlreadyDownload(String url, String filePath) {
        Log.i(TAG, " 任务已经先前下载完成 " + url + " 线程是 "+Thread.currentThread().getName());
        if (url.equals(url1)) {
            progressBar1.setProgress(100);
            btn1.setText("重新下载");
        } else if (url.equals(url2)) {
            progressBar2.setProgress(100);
            btn2.setText("重新下载");
        }
        Toast.makeText(getApplicationContext(), "任务先前下载完成", Toast.LENGTH_SHORT).show();
    }
    @Override
    public void taskProgress(String url, String filePath, int progress) {
        Log.i(TAG, " 下载进度 " + url + " " + progress+" 线程是 "+Thread.currentThread().getName());
        if (url.equals(url1)) {
            progressBar1.setProgress(progress);
        } else if (url.equals(url2)) {
            progressBar2.setProgress(progress);
        }
    }
    @Override
    public void taskFinish(String url, String filePath) {
        Log.i(TAG, " 下载完成 " + url+" 线程是 "+Thread.currentThread().getName());
        if (url.equals(url1)) {
            btn1.setText("完成");
        } else if (url.equals(url2)) {
            btn2.setText("完成");
        }
        Toast.makeText(getApplicationContext(), "下载成功 " + url, Toast.LENGTH_SHORT).show();
    }
    @Override
    public void taskFailure(String url) {
        Log.i(TAG, " 下载失败 " + url+" 线程是 "+Thread.currentThread().getName());
        if (url.equals(url1)) {
            btn1.setText("失败");
        } else if (url.equals(url2)) {
            btn2.setText("失败");
        }
        Toast.makeText(getApplicationContext(), "下载失败 " + url, Toast.LENGTH_SHORT).show();
    }
}

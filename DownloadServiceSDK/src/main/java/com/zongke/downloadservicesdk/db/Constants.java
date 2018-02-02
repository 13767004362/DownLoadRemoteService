package com.zongke.downloadservicesdk.db;

/**
 * Created by ${xingen} on 2017/12/25.
 */

public class Constants {
    public static final String ACTION="com.zongke.downloadservice.service.DownLoadService";
    public static final String PACKAGE_NAME ="com.zongke.downloadservice";

    public static final String KEY_PROGRESS="progressKey";
    public static final String KEY_DOWN_LOAD_URL="downloadUrl";
    /**
     * 停止线程
     */
    public static final int task_stop_thread = 110;
    /**
     * 任务完成
     */
    public static final int task_finish = 111;
    /**
     * 任务进行中
     */
    public static final int task_progress=112;
    /**
     * 任务失败
     */
    public  static final int task_failure=113;
    /**
     * 任务已经下载完成
     */
    public static final  int task_already_download=116;
    /**
     * 直接下载的模式
     */
    public static final int mode_single=1;
    /**
     * 断点续传的模式
     */
    public static  final  int mode_multi=2;
}


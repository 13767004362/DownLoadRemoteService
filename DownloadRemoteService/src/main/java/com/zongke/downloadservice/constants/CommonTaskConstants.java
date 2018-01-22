package com.zongke.downloadservice.constants;

/**
 * Created by ${xinGen} on 2017/12/22.
 */

public final  class CommonTaskConstants {
    /**
     * 停止线程
     */
    public static final int task_stop_thread = 110;
    /**
     * 任务完成
     */
    public static final int task_download_finish = 111;
    /**
     * 任务进行中
     */
    public static final int task_download_progress =112;
    /**
     * 任务失败
     */
    public  static final int task_download_failure =113;
    /**
     * 计算任务完成
     */
    public static final int task_calculate_finish=114;
    /**
     * 计算任务失败
     */
    public static final int task_calculate_failure=115;

    /**
     * 任务已经下载完成
     */
    public static final  int task_already_download=116;

    /**
     * 下载线程的个数，这里默认是3个
     */
    public static final int DOWNLOAD_THREAD_ACCOUNT=3;

    /**
     *  key 名
     */
    public static final String KEY_PROGRESS="progressKey";
    public static final String KEY_DOWN_LOAD_URL="downloadUrl";
    public static final String KEY_FILE_PATH="filePath";

    public  static final String HEADER_NAME_RANGE="Range";
    /**
     * range对应values的前缀
     */
    public static final String RANGE_VALUES_REG_BYTE="bytes=";

}

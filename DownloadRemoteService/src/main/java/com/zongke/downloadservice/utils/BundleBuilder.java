package com.zongke.downloadservice.utils;

import android.os.Bundle;

import com.zongke.downloadservice.constants.CommonTaskConstants;
import com.zongke.downloadservice.task.DownLoadTask;

/**
 * Created by ${xingen} on 2017/12/22.
 */

public class BundleBuilder {

    public static Bundle createBundle(DownLoadTask downLoadTask, int progress){
        Bundle bundle=createBundle(downLoadTask);
        bundle.putInt(CommonTaskConstants.KEY_PROGRESS,progress);
        return bundle;
    }
    public static Bundle createBundle(DownLoadTask downLoadTask){
        Bundle bundle=new Bundle();
        bundle.putString(CommonTaskConstants.KEY_FILE_PATH,downLoadTask.getFilePath());
        bundle.putString(CommonTaskConstants.KEY_DOWN_LOAD_URL,downLoadTask.getDownloadUrl());
        return bundle;
    }
}

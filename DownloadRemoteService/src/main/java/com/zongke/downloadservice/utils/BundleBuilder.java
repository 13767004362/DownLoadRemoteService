package com.zongke.downloadservice.utils;

import android.os.Bundle;

import com.zongke.downloadservice.constants.CommonTaskConstants;
import com.zongke.downloadservice.task.SingleDownLoadTask;
import com.zongke.downloadservice.task.MultiDownLoadTask;

/**
 * Created by ${xingen} on 2017/12/22.
 */

public class BundleBuilder {

    public static Bundle createBundle(MultiDownLoadTask downLoadTask, int progress){
        Bundle bundle=createBundle(downLoadTask);
        bundle.putInt(CommonTaskConstants.KEY_PROGRESS,progress);
        return bundle;
    }
    public static Bundle createBundle(MultiDownLoadTask downLoadTask){
        Bundle bundle=new Bundle();
        bundle.putString(CommonTaskConstants.KEY_FILE_PATH,downLoadTask.getFilePath());
        bundle.putString(CommonTaskConstants.KEY_DOWN_LOAD_URL,downLoadTask.getDownloadUrl());
        return bundle;
    }

    public static Bundle createBundle(SingleDownLoadTask downLoadTask, int progress){
        Bundle bundle=createBundle(downLoadTask);
        bundle.putInt(CommonTaskConstants.KEY_PROGRESS,progress);
        return bundle;
    }
    public static Bundle createBundle(SingleDownLoadTask downLoadTask){
        Bundle bundle=new Bundle();
        bundle.putString(CommonTaskConstants.KEY_FILE_PATH,downLoadTask.getFilePath());
        bundle.putString(CommonTaskConstants.KEY_DOWN_LOAD_URL,downLoadTask.getDownloadUrl());
        return bundle;
    }
}

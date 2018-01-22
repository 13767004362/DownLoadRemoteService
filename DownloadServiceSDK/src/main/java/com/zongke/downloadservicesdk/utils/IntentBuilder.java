package com.zongke.downloadservicesdk.utils;

import android.content.Intent;

import com.zongke.downloadservicesdk.db.Constants;

/**
 * Created by ${xingen} on 2017/12/25.
 */

public class IntentBuilder {
    public static Intent createServiceIntent(){
        Intent intent=new Intent(Constants.ACTION);
        intent.setPackage(Constants.PACKAGE_NAME);
        return intent;
    }
}

package com.zongke.downloadservicesdk.receiver;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.os.ResultReceiver;

import com.zongke.downloadservicesdk.client.ServiceClient;

/**
 * Created by ${xingen} on 2017/12/25.
 */

public class DownloadResultReceiver  extends ResultReceiver{
    private ServiceClient serviceClient;
    public DownloadResultReceiver(Handler handler,ServiceClient serviceClient) {
        super(handler);
        this.serviceClient=serviceClient;
    }
    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
            this.serviceClient.handlerResult(resultCode,resultData);
    }
}

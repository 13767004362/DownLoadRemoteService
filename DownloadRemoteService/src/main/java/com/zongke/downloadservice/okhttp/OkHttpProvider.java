package com.zongke.downloadservice.okhttp;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by ${xingen} on 2017/12/21.
 */

public class OkHttpProvider {
    /**
     * 创建一个默认的OkHttpClient
     * @return
     */
    public static OkHttpClient createOkHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .cache(null);
        return builder.build();
    }

    /**
     * 创建一个下载的Request
     * @param downloadUrl
     * @return
     */
    public static Request createOkHttpRequest(String downloadUrl){
        Request.Builder builder=new Request.Builder();
        builder.url(downloadUrl);
        return builder.build();
    }

    /**
     * 添加特殊的header
     * @param request
     * @param key
     * @param value
     * @return
     */
    public static Request addHeader(Request request,String key,String value){
        Request.Builder builder=request.newBuilder();

        builder.addHeader(key,value);
        return builder.build();
    }
}

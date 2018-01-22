package com.zongke.downloadservice.utils;

import java.io.File;

/**
 * Created by ${xinGen} on 2018/1/16.
 */

public class FileUtils {
    public static boolean deleteFile(File file){
        boolean finish=false;
        try {
            if (file!=null&&file.exists()){
                if (file.isFile()){
                    file.delete();
                    finish=true;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return finish;
    }
}

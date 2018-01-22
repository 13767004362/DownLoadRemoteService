package com.zongke.downloadservice.db.dao;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

import com.zongke.downloadservice.db.bean.DownloadItemBean;
import com.zongke.downloadservice.db.bean.DownloadTaskBean;
import com.zongke.downloadservice.db.sqlite.DownloadTaskConstants;
import com.zongke.downloadservice.db.utils.DBUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ${xinGen} on 2018/1/6.
 */

public class DownloadTaskImp implements BaseDao<DownloadTaskBean>{

    private static DownloadTaskImp instance;
    private ContentResolver contentResolver;
    private DownloadTaskImp(){}
    public static synchronized  DownloadTaskImp getInstance(){
        if (instance==null){
            instance=new DownloadTaskImp();
        }
        return instance;
    }
    @Override
    public void init(ContentResolver contentResolver) {
        this.contentResolver=contentResolver;
    }
    @Override
    public List<DownloadTaskBean> queryAll() {
        return null;
    }
    @Override
    public List<DownloadTaskBean> queryAction(String select, String[] selectArg) {
        List<DownloadTaskBean> downloadItemList = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = contentResolver.query(DownloadTaskConstants.URI_DOWNLOAD_TASK, null, select, selectArg, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    downloadItemList.add(DBUtils.createDownloadTask(cursor));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return downloadItemList;
    }

    @Override
    public long insert(DownloadTaskBean downloadTaskBean) {
       Uri uri= contentResolver.insert(DownloadTaskConstants.URI_DOWNLOAD_TASK, DBUtils.createContentValues(downloadTaskBean));
       String[] s=uri.getEncodedPath().split("/");
       String lengthStr=s[s.length-1];
        return Long.valueOf(lengthStr) ;
    }

    @Override
    public int bulkInsert(List<DownloadTaskBean> list) {
        return 0;
    }

    @Override
    public int update(DownloadTaskBean downloadTaskBean, String select, String[] selectArg) {
        return this.contentResolver.update(DownloadTaskConstants.URI_DOWNLOAD_TASK,DBUtils.createContentValues(downloadTaskBean),select,selectArg);
    }
    @Override
    public int delete(String select, String[] selectArg) {
        return  this.contentResolver.delete(DownloadTaskConstants.URI_DOWNLOAD_TASK,select,selectArg);
    }
    @Override
    public void deleteAll() {

    }
}

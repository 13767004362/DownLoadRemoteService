package com.zongke.downloadservice.db.dao;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;

import com.zongke.downloadservice.db.sqlite.DownloadTaskConstants;
import com.zongke.downloadservice.db.utils.DBUtils;
import com.zongke.downloadservice.db.bean.DownloadItemBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ${xinGen} on 2018/1/6.
 */

public class DownloadItemImp implements BaseDao<DownloadItemBean> {
    private static DownloadItemImp instance;
    private ContentResolver contentResolver;
    private DownloadItemImp() {
    }
    public static synchronized DownloadItemImp getInstance() {
        if (instance == null) {
            instance = new DownloadItemImp();
        }
        return instance;
    }
    @Override
    public void init(ContentResolver contentResolver) {
        this.contentResolver = contentResolver;
    }
    @Override
    public List<DownloadItemBean> queryAll() {
        return null;
    }
    @Override
    public List<DownloadItemBean> queryAction(String select, String[] selectArg) {
        List<DownloadItemBean> downloadItemList = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = contentResolver.query(DownloadTaskConstants.URI_DOWNLOAD_ITEM, null, select, selectArg, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                     downloadItemList.add(DBUtils.createDownloadItem(cursor));
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
    public long insert(DownloadItemBean downloadItem) {
        return 0;
    }
    @Override
    public int bulkInsert(List<DownloadItemBean> list) {
        ContentValues[] contentValueArray=new ContentValues[list.size()];
        for (int i=0;i<list.size();++i){
            contentValueArray[i]=DBUtils.createContentValues(list.get(i));
        }
        return contentResolver.bulkInsert(DownloadTaskConstants.URI_DOWNLOAD_ITEM,contentValueArray);
    }
    @Override
    public int update(DownloadItemBean downloadItem, String select, String[] selectArg) {
        ContentValues contentValues=DBUtils.createContentValues(downloadItem);
        return contentResolver.update(DownloadTaskConstants.URI_DOWNLOAD_ITEM,contentValues,select,selectArg);
    }
    @Override
    public int delete(String select, String[] selectArg) {
        return contentResolver.delete(DownloadTaskConstants.URI_DOWNLOAD_ITEM,select,selectArg);
    }
    @Override
    public void deleteAll() {

    }
}

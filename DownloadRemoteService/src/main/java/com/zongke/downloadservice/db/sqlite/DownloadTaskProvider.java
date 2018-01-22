package com.zongke.downloadservice.db.sqlite;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by ${xinGen} on 2018/1/5.
 */

public class DownloadTaskProvider extends ContentProvider {
    private static final int TABLE_DIR_1 = 1;
    private static final int TABLE_DIR_2 = 2;
    private static UriMatcher uriMatcher;
    private DownloadTaskDatabase dataHelper;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(DownloadTaskConstants.AUTHORITY, DownloadTaskConstants.TABLE_NAME_DOWNLOAD_TASK, TABLE_DIR_1);
        uriMatcher.addURI(DownloadTaskConstants.AUTHORITY, DownloadTaskConstants.TABLE_NAME_DOWNLOAD_ITEM, TABLE_DIR_2);
    }

    @Override
    public boolean onCreate() {
        this.dataHelper = new DownloadTaskDatabase(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase db = dataHelper.getReadableDatabase();
        Cursor cursor = null;
        switch (uriMatcher.match(uri)) {
            case TABLE_DIR_1:
                cursor = db.query(DownloadTaskConstants.TABLE_NAME_DOWNLOAD_TASK, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case TABLE_DIR_2:
                cursor = db.query(DownloadTaskConstants.TABLE_NAME_DOWNLOAD_ITEM, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                break;
        }
        if (cursor != null) {
            //添加通知对象
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (uriMatcher.match(uri)) {
            case TABLE_DIR_1:
                return "vnd.android.cursor.dir/vnd." + DownloadTaskConstants.AUTHORITY
                        + DownloadTaskConstants.TABLE_NAME_DOWNLOAD_TASK;
            case TABLE_DIR_2:
                return "vnd.android.cursor.dir/vnd." + DownloadTaskConstants.AUTHORITY
                        + DownloadTaskConstants.TABLE_NAME_DOWNLOAD_ITEM;
            default:
                break;
        }
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        SQLiteDatabase sqLiteDatabase = dataHelper.getWritableDatabase();
        Uri returnUri = null;
        switch (uriMatcher.match(uri)) {
            case TABLE_DIR_1: {
                long rowId = sqLiteDatabase.insert(DownloadTaskConstants.TABLE_NAME_DOWNLOAD_TASK, null, values);
                returnUri = Uri.parse("content://" + DownloadTaskConstants.AUTHORITY + "/"
                        + DownloadTaskConstants.TABLE_NAME_DOWNLOAD_TASK + "/" + rowId);
            }
            break;
            case TABLE_DIR_2: {
                long rowId = sqLiteDatabase.insert(DownloadTaskConstants.TABLE_NAME_DOWNLOAD_ITEM, null, values);
                returnUri = Uri.parse("content://" + DownloadTaskConstants.AUTHORITY + "/"
                        + DownloadTaskConstants.TABLE_NAME_DOWNLOAD_ITEM + "/" + rowId);
            }
            break;
            default:
                break;
        }
        //通知，数据源发生改变
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase sqLiteDatabase = this.dataHelper.getWritableDatabase();
        int deliteRow = 0;
        switch (uriMatcher.match(uri)) {
            case TABLE_DIR_1:
                deliteRow = sqLiteDatabase.delete(DownloadTaskConstants.TABLE_NAME_DOWNLOAD_TASK, selection, selectionArgs);
                break;
            case TABLE_DIR_2:
                deliteRow = sqLiteDatabase.delete(DownloadTaskConstants.TABLE_NAME_DOWNLOAD_ITEM, selection, selectionArgs);
                break;
            default:
                break;
        }
        //通知，数据源发生改变
        getContext().getContentResolver().notifyChange(uri, null);
        return deliteRow;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase sqLiteDatabase = dataHelper.getWritableDatabase();
        int updateRow = 0;
        switch (uriMatcher.match(uri)) {
            case TABLE_DIR_1:
                updateRow = sqLiteDatabase.update(DownloadTaskConstants.TABLE_NAME_DOWNLOAD_TASK, values, selection, selectionArgs);
                break;
            case TABLE_DIR_2:
                updateRow = sqLiteDatabase.update(DownloadTaskConstants.TABLE_NAME_DOWNLOAD_ITEM, values, selection, selectionArgs);
                break;
            default:
                break;
        }
        if (updateRow > 0) {     //通知，数据源发生改变
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return 0;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        switch (uriMatcher.match(uri)) {
            case TABLE_DIR_1: {
                return bulkInsert(uri, DownloadTaskConstants.TABLE_NAME_DOWNLOAD_TASK, values);
            }
            case TABLE_DIR_2: {
                return bulkInsert(uri, DownloadTaskConstants.TABLE_NAME_DOWNLOAD_ITEM, values);
            }
            default:
                return super.bulkInsert(uri, values);
        }
    }
    /**
     * 循环批量插入的动作
     * @param uri
     * @param tableName
     * @param values
     * @return
     */
    private int bulkInsert(Uri uri, String tableName, ContentValues[] values) {
        SQLiteDatabase sqLiteDatabase = dataHelper.getWritableDatabase();
        int numValues = 0;
        try {
            sqLiteDatabase.beginTransaction();
            for (int i = 0; i < values.length; ++i) {
                sqLiteDatabase.insert(tableName, null, values[i]);
            }
            sqLiteDatabase.setTransactionSuccessful();
            numValues = values.length;
            getContext().getContentResolver().notifyChange(uri, null);
        } catch (Exception e) {
            e.printStackTrace();
            numValues = 0;
        } finally {
            sqLiteDatabase.endTransaction();
        }
        return numValues;
    }

}

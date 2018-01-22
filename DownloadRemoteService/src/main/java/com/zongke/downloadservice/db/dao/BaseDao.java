package com.zongke.downloadservice.db.dao;

import android.content.ContentResolver;

import java.util.List;

/**
 * Created by ${xinGen} on 2018/1/6.
 */

public interface BaseDao<T> {
        void init(ContentResolver contentResolver);
        /**
         * 获取全部
         * @return
         */
        List<T> queryAll();

        /**
         *  指定条件下的查询
         * @param select
         * @param selectArg
         * @return
         */
        List<T> queryAction(String select,String[] selectArg);

        /**
         * 新增
         * @param t
         * @return
         */
        long insert(T t);

        /**
         *  批量插入
         * @param list
         * @return
         */
        int bulkInsert( List<T> list);

        /**
         * 更新
         * @param t
         * @param select
         * @param selectArg
         * @return
         */
        int update(T t,String select,String[] selectArg);

        /**
         * 指定条件的删除
         * @param select
         * @param selectArg
         * @return
         */
        int delete(String select,String[] selectArg);

        /**
         * 删除全部
         */
        void deleteAll();
}

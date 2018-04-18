package cn.trinea.android.common.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import cn.trinea.android.common.constant.DbConstants;

/**
 * db helper
 * 
 * @author <a href="http://www.trinea.cn" target="_blank">Trinea</a> 2013-10-21
 */
public class DbHelper extends SQLiteOpenHelper {

    public DbHelper(Context context) {
        super(context, DbConstants.DB_NAME, null, DbConstants.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.beginTransaction();
        try {
            //db.execSQL  执行ssql
            db.execSQL(DbConstants.CREATE_IMAGE_SDCARD_CACHE_TABLE_SQL.toString());//创建图像SDCARD缓存表SQL
            db.execSQL(DbConstants.CREATE_IMAGE_SDCARD_CACHE_TABLE_INDEX_SQL.toString());//创建图像SDCARD缓存表索引SQL

            db.execSQL(DbConstants.CREATE_HTTP_CACHE_TABLE_SQL.toString());//创建HTTP缓存表SQL
            db.execSQL(DbConstants.CREATE_HTTP_CACHE_TABLE_INDEX_SQL.toString());//创建HTTP缓存表索引SQL
            db.execSQL(DbConstants.CREATE_HTTP_CACHE_TABLE_UNIQUE_INDEX.toString());//创建HTTP缓存表唯一索引
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}
}

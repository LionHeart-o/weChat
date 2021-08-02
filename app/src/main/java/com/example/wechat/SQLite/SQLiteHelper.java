package com.example.wechat.SQLite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.wechat.javaBean.ChatBean;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class SQLiteHelper extends SQLiteOpenHelper {

    private final static String DATABASE_NAME = "usersInfo";
    private final static int DATABASE_VERSION = 1;
    private final static String TABLE_NAME = "usersmessage";

    //构造函数，创建数据库
    public SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //建表
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table if not exists " + TABLE_NAME
                + "(id INTEGER PRIMARY KEY,"
                + " sendUser VARCHAR(255)  NOT NULL,"
                + " receiveUser VARCHAR(255) NOT NULL,"
                + " message VARCHAR(255) NOT NULL,"
                + " type int(8) NOT NULL,"
                + " createTime VARCHAR(255) NOT NULL"+
                ") ";
        db.execSQL(sql);

    }


    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String sql = "DROP TABLE IF EXISTS " + TABLE_NAME;
        db.execSQL(sql);
        onCreate(db);
    }

    //获取游标
    public Cursor select() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null);
        return cursor;
    }

    //插入一条记录
    public void insert(String sendUser,String receiveUser,String message,int type,String time) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("sendUser",sendUser);
        cv.put("receiveUser", receiveUser);
        cv.put("message", message);
        cv.put("type", type);
        //设置日期格式
        cv.put("createTime", time);
        db.insert(TABLE_NAME, null, cv);
    }

    //根据条件查询
    public Cursor query(String ownEmail, String contactEmail) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from usersmessage where (sendUser = '" + ownEmail + "' and receiveUser = '" + contactEmail + "') or (sendUser = '" + contactEmail + "' and receiveUser = '" + ownEmail + "') ", null);
        return cursor;
    }

    //根据条件查询
    public Cursor queryLastMessage(String ownEmail,String contactEmail) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from usersmessage where (sendUser = '" + ownEmail + "' and receiveUser = '" + contactEmail + "') or (sendUser = '" + contactEmail + "' and receiveUser = '" + ownEmail + "')order by id desc limit 1 ", null);
        return cursor;
    }

    //删除记录
    public void delete(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        String where ="id = ?";
        String[] whereValue = { Integer.toString(id) };
        db.delete(TABLE_NAME, where, whereValue);
    }

    //更新记录
    public void update(int id, String sendUser,String receiveUser,String message) {
        SQLiteDatabase db = this.getWritableDatabase();
        String where = "id = ?";
        String[] whereValue = { Integer.toString(id) };
        ContentValues cv = new ContentValues();
        cv.put("sendUser",sendUser);
        cv.put("receiveUser", receiveUser);
        cv.put("message", message);
        db.update(TABLE_NAME, cv, where, whereValue);
    }
}
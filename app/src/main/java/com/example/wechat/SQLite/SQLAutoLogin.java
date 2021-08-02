package com.example.wechat.SQLite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLAutoLogin extends SQLiteOpenHelper {

    private final static String DATABASE_NAME = "usersInfo";
    private final static int DATABASE_VERSION = 1;
    private final static String TABLE_NAME = "userAutoLogin";

    //构造函数，创建数据库
    public SQLAutoLogin(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //建表
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table if not exists " + TABLE_NAME
                + "(id INTEGER PRIMARY KEY,"
                + " email VARCHAR(255)  NOT NULL,"
                + " password VARCHAR(255) NOT NULL" +
                ") ";
        db.execSQL(sql);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String sql = "DROP TABLE IF EXISTS " + TABLE_NAME;
        db.execSQL(sql);
        onCreate(db);
    }


    //插入一条记录
    public void insert(String email,String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("email",email);
        cv.put("password", password);
        db.insert(TABLE_NAME, null, cv);
    }

    //根据条件查询
    public Cursor query() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from userAutoLogin", null);
        return cursor;
    }

    //删除记录
    public void delete() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME,"",null);
    }

}
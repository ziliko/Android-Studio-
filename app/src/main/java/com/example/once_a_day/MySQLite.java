package com.example.once_a_day;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MySQLite extends SQLiteOpenHelper {
    public static final int VERSION = 1;
    public static final String DB_NAME = "My.db";
    public static final String TABLE_NAME = "student";
    public static final String ITEM_NAME = "item";
    public static int check=1;


    public MySQLite(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (id INTEGER PRIMARY KEY, name VARCHAR, major VARCHAR)";
        db.execSQL(sql);
        sql = "CREATE TABLE IF NOT EXISTS " + ITEM_NAME + " (id INTEGER PRIMARY KEY, name VARCHAR, number INTEGER,description VARCHAR)";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //
    }
}

package com.example.luchaojun.reviewdefinecontentprovider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by luchaojun on 9/4/17.
 */

public class SalaryProvider extends ContentProvider {

    private MyOpenHelper openHelper;
    private static final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH); //定义一个路径的适配器
    private static final int QUERYSUCCESS = 0;

    static {
        matcher.addURI("com.wistrol.provider","query",QUERYSUCCESS);
    }
    @Override
    public boolean onCreate() {
        openHelper = new MyOpenHelper(getContext());
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] strings, @Nullable String s, @Nullable String[] strings1, @Nullable String s1) {
        int code = matcher.match(uri);
        if(code == QUERYSUCCESS){
            SQLiteDatabase writableDatabase = openHelper.getWritableDatabase();
            writableDatabase.query("info")
        }
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }
}

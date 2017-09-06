package com.wistron.generic.pqaa.utility;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;

public class DataContentProvider extends ContentProvider {
    public static String AUTHORITY = "com.wistron.provider.generic.pqaa.database";
    private final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private SQLiteDatabase mDatabase;

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // TODO Auto-generated method stub
        if (getType(uri).equals(RunInfo.CONTENT_TYPE)) {
            return mDatabase.delete(DatabaseLibrary.TABLE_SAVED, selection, selectionArgs);
        } else {
            return mDatabase.delete(DatabaseLibrary.TABLE_AUTOMATIC, selection, selectionArgs);
        }
    }

    @Override
    public String getType(Uri uri) {
        // TODO Auto-generated method stub
        switch (sUriMatcher.match(uri)) {
            case 0:
                return RunInfo.CONTENT_TYPE;
            case 1:
                return RunInfo.CONTENT_ITEM_TYPE;
            case 2:
                return RunInfo.CONTENT_TYPE_AUTOMATIC;
            case 3:
                return RunInfo.CONTENT_ITEM_TYPE_AUTOMATIC;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // TODO Auto-generated method stub
        long mResultRow = 0;
        if (getType(uri).equals(RunInfo.CONTENT_TYPE)) {
            mResultRow = mDatabase.insert(DatabaseLibrary.TABLE_SAVED, RunInfo.ITEM, values);
            if (mResultRow > 0) {
                Uri noteUri = ContentUris.withAppendedId(RunInfo.CONTENT_URI_TEST, mResultRow);
                getContext().getContentResolver().notifyChange(noteUri, null);
                return noteUri;
            }
        } else {
            mResultRow = mDatabase.insert(DatabaseLibrary.TABLE_AUTOMATIC, RunInfo.ITEM, values);
            if (mResultRow > 0) {
                Uri noteUri = ContentUris.withAppendedId(RunInfo.CONTENT_URI_TEST_AUTOMATIC, mResultRow);
                getContext().getContentResolver().notifyChange(noteUri, null);
                return noteUri;
            }
        }
        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public boolean onCreate() {
        // TODO Auto-generated method stub
        DatabaseLibrary mLibrary = new DatabaseLibrary(getContext(), DatabaseLibrary.DATABASE_NAME, null,
                DatabaseLibrary.DATABASE_DEFAULT_VERSION);
        sUriMatcher.addURI(AUTHORITY, "test", 0);
        sUriMatcher.addURI(AUTHORITY, "test/#", 1);
        sUriMatcher.addURI(AUTHORITY, "automatic", 2);
        sUriMatcher.addURI(AUTHORITY, "automatic/#", 3);
        mDatabase = mLibrary.getWritableDatabase();
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        // TODO Auto-generated method stub
        String mTable = DatabaseLibrary.TABLE_SAVED;
        if (getType(uri).equals(RunInfo.CONTENT_TYPE_AUTOMATIC)) {
            mTable = DatabaseLibrary.TABLE_AUTOMATIC;
        }
        Cursor c = mDatabase.query(mTable, projection, selection, selectionArgs, null, null, sortOrder);
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // TODO Auto-generated method stub
        String mTable = DatabaseLibrary.TABLE_SAVED;
        if (getType(uri).equals(RunInfo.CONTENT_TYPE_AUTOMATIC)) {
            mTable = DatabaseLibrary.TABLE_AUTOMATIC;
        }
        int count = mDatabase.update(mTable, values, selection, selectionArgs);
        return count;
    }

    public static class DatabaseLibrary extends SQLiteOpenHelper {
        public static final String DATABASE_NAME = "wistron.db";
        public static final int DATABASE_DEFAULT_VERSION = 2;
        public static final String TABLE_SAVED = "saved";
        public static final String TABLE_AUTOMATIC = "automatic";

        public DatabaseLibrary(Context context, String name, CursorFactory factory, int version) {
            super(context, name, factory, version);
            // TODO Auto-generated constructor stub
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            // TODO Auto-generated method stub
            db.execSQL("CREATE TABLE " + TABLE_SAVED + " (" + RunInfo._ID + " INTEGER PRIMARY KEY, " + RunInfo.ITEM
                    + " TEXT, " + RunInfo.ARG1 + " TEXT DEFAULT '', " + RunInfo.ARG2 + " TEXT DEFAULT '', "
                    + RunInfo.ARG3 + " TEXT DEFAULT '', " + RunInfo.ARG4 + " TEXT DEFAULT '', "
                    + RunInfo.ARG5 + " TEXT DEFAULT '', " + RunInfo.ARG6 + " TEXT DEFAULT '', "
                    + RunInfo.ARG7 + " TEXT DEFAULT '', " + RunInfo.ARG8 + " TEXT DEFAULT '', "
                    + RunInfo.ARG9 + " TEXT DEFAULT '', " + RunInfo.INDEX + " INTEGER, " + RunInfo.REMANENT
                    + " INTEGER DEFAULT 0" + " );");

            db.execSQL("CREATE TABLE " + TABLE_AUTOMATIC + " (" + RunInfo._ID + " INTEGER PRIMARY KEY, " + RunInfo.ITEM
                    + " TEXT, " + RunInfo.ARG1 + " TEXT DEFAULT '', " + RunInfo.ARG2 + " TEXT DEFAULT '', "
                    + RunInfo.ARG3 + " TEXT DEFAULT '', " + RunInfo.ARG4 + " TEXT DEFAULT '', "
                    + RunInfo.ARG5 + " TEXT DEFAULT '', " + RunInfo.ARG6 + " TEXT DEFAULT '', "
                    + RunInfo.ARG7 + " TEXT DEFAULT '', " + RunInfo.ARG8 + " TEXT DEFAULT '', "
                    + RunInfo.ARG9 + " TEXT DEFAULT '', " + RunInfo.INDEX + " INTEGER, " + RunInfo.REMANENT
                    + " INTEGER DEFAULT 0" + " );");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // TODO Auto-generated method stub
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_SAVED);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_AUTOMATIC);
            onCreate(db);
        }
    }

    public static class RunInfo implements BaseColumns {
        public static final Uri CONTENT_URI_TEST = Uri.parse("content://" + AUTHORITY + "/test");
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.wistron.test";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.wistron.test";

        public static final Uri CONTENT_URI_TEST_AUTOMATIC = Uri.parse("content://" + AUTHORITY + "/automatic");
        public static final String CONTENT_TYPE_AUTOMATIC = "vnd.android.cursor.dir/vnd.wistron.automatic";
        public static final String CONTENT_ITEM_TYPE_AUTOMATIC = "vnd.android.cursor.item/vnd.wistron.automatic";
        public static final String DEFAULT_SORT_ORDER = "modified DESC";
        public static final String ITEM = "item";
        public static final String ARG1 = "arg1";
        public static final String ARG2 = "arg2";
        public static final String ARG3 = "arg3";
        public static final String ARG4 = "arg4";
        public static final String ARG5 = "arg5";
        public static final String ARG6 = "arg6";
        public static final String ARG7 = "arg7";
        public static final String ARG8 = "arg8";
        public static final String ARG9 = "arg9";
        public static final String INDEX = "location";
        public static final String REMANENT = "remanent";
    }
}

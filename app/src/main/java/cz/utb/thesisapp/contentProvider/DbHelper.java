package cz.utb.thesisapp.contentProvider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import cz.utb.thesisapp.services.kryonet.Network;

import static android.content.ContentValues.TAG;
import static cz.utb.thesisapp.GlobalValues.*;

public class DbHelper extends SQLiteOpenHelper {
    private static final String TAG = "DbHelper";
    private static final int DATABASE_VERSION = 1;
    private static final String CREATE_TABLE = "create table " + DB_TABLE_NAME +
            "(" + DB_ID + " integer primary key autoincrement, " + DB_TOUCH_TYPE + " text," +
            "" + DB_X + " float, " +
            DB_Y + " float," +
            "" + DB_CLIENT_CREATED + " DATETIME DEFAULT(STRFTIME('%Y-%m-%d %H:%M:%f'))," +
            "" + DB_CLIENT_RECEIVED + " DATETIME DEFAULT(STRFTIME('%Y-%m-%d %H:%M:%f')) );";
    private static final String DROP_TABLE = "drop table if exists " + DB_TABLE_NAME;


    public DbHelper(Context context) {
        super(context, DB_DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d("DbHelper", "onUpgrade: ~~");
        db.execSQL(DROP_TABLE);
        onCreate(db);
    }



//    public void saveToLocalDatabase(String touchType, float x, float y, Date created, SQLiteDatabase db) {
//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
//        ContentValues contentValues = new ContentValues();
//        contentValues.put(DB_TOUCH_TYPE, touchType);
//        contentValues.put(DB_X, x);
//        contentValues.put(DB_Y, y);
//        contentValues.put(DB_SERVER_RECEIVED, dateFormat.format(created));
//        db.insert(DB_TABLE_NAME, null, contentValues);
//    }

//    public Cursor readFromLocalDatabase(SQLiteDatabase db) {
//        String[] projection = {DB_TOUCH_TYPE, DB_X, DB_Y, DB_CLIENT_CREATED};
//        return (db.query(DB_TABLE_NAME, projection, null, null, null, null, null));
//    }

//    public void updateLocalDatabase(Date created, Date serverReceived, Date clientReceived, SQLiteDatabase db) {
//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
//        ContentValues contentValues = new ContentValues();
//        contentValues.put(DB_SERVER_RECEIVED, dateFormat.format(serverReceived));
//        contentValues.put(DB_CLIENT_CREATED, dateFormat.format(serverReceived));
//        String selection = DB_CLIENT_CREATED + " LIKE ?";
//        String[] selectionArgs = {dateFormat.format(created)};
//        db.update(DB_TABLE_NAME, contentValues, selection, selectionArgs);
//    }
}

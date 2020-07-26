package cz.utb.thesisapp.contentProvider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import static cz.utb.thesisapp.GlobalValues.*;

public class DbHelper extends SQLiteOpenHelper {
    private static final String TAG = "DbHelper";
    private static final int DATABASE_VERSION = 2;
    private static final String CREATE_TABLE_REMOTE = "create table " + DB_TABLE_REMOTE +
            "(" + DB_ID + " integer primary key autoincrement, " + DB_TOUCH_TYPE + " text," +
            "" + DB_X + " float, " +
            DB_Y + " float," +
            "" + DB_CREATED + " DATETIME DEFAULT(STRFTIME('%Y-%m-%d %H:%M:%f'))," +
            "" + DB_RECEIVED + " DATETIME DEFAULT(STRFTIME('%Y-%m-%d %H:%M:%f')) );";
    private static final String CREATE_TABLE_LOCAL = "create table " + DB_TABLE_LOCAL +
            "(" + DB_ID + " integer primary key autoincrement, " + DB_TOUCH_TYPE + " text," +
            "" + DB_X + " float, " +
            DB_Y + " float," +
            "" + DB_CREATED + " DATETIME DEFAULT(STRFTIME('%Y-%m-%d %H:%M:%f')) );";
    private static final String DROP_TABLE_REMOTE = "drop table if exists " + DB_TABLE_REMOTE;
    private static final String DROP_TABLE_LOCAL = "drop table if exists " + DB_TABLE_LOCAL;


    public DbHelper(Context context) {
        super(context, DB_DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_LOCAL);
        db.execSQL(CREATE_TABLE_REMOTE);
        Log.i(TAG, "onCreate: ~~");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i("DbHelper", "onUpgrade: ~~");
        db.execSQL(DROP_TABLE_REMOTE);
        db.execSQL(DROP_TABLE_LOCAL);
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

package cz.utb.thesisapp.contentProvider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static cz.utb.thesisapp.GlobalValues.DB_CREATED;
import static cz.utb.thesisapp.GlobalValues.DB_TABLE_LOCAL;
import static cz.utb.thesisapp.GlobalValues.DB_TABLE_REMOTE;

public class MyContentProvider extends ContentProvider {
    private static final String TAG = "ContentProvider";
    private DbHelper dbHelper;
    private static final int ALL_TOUCHES_REMOTE = 1;
    private static final int SINGLE_TOUCH_REMOTE = 2;
    private static final int ALL_TOUCHES_LOCAL = 3;
    private static final int SINGLE_TOUCH_LOCAL = 4;

    private static final String AUTHORITY = "cz.utb.thesisapp.contentProvider";
    public static final Uri CONTENT_REMOTE_URI = Uri.parse("content://" + AUTHORITY + "/" + DB_TABLE_REMOTE);
    public static final Uri CONTENT_LOCAL_URI = Uri.parse("content://" + AUTHORITY + "/" + DB_TABLE_LOCAL);
    /*
    a content URI pattern matches content URIs using wildcard characters:
    *: matches a String of any valid characters of any length
    #: matches a String of numeric characters of any length
    */
    private static final UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH); //NO_MATCH is a constant with value -1
        uriMatcher.addURI(AUTHORITY, DB_TABLE_REMOTE, ALL_TOUCHES_REMOTE);
        uriMatcher.addURI(AUTHORITY, DB_TABLE_LOCAL, ALL_TOUCHES_LOCAL);
        uriMatcher.addURI(AUTHORITY, DB_TABLE_REMOTE + "/#", SINGLE_TOUCH_REMOTE);
        uriMatcher.addURI(AUTHORITY, DB_TABLE_LOCAL + "/#", SINGLE_TOUCH_LOCAL);
    }


    @Override
    public boolean onCreate() {
        //get access to dbz helper
        dbHelper = new DbHelper(getContext());
        return false;
    }

    /*
    The query() method must return a Cursor object, or if it fails,
    throw an exception. If you are using an SQLite database as your data storage,
    you can simply return the Cursor returned by one of the Query() methods of the
    SQLiteDatabase class. If the query does not match any rows, you should return a
    cursor instance whose getCount() method returns 0. You should return null only if an
    internal error occurred during the query process
    */
    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        switch (uriMatcher.match(uri)) {
            case ALL_TOUCHES_REMOTE:
                queryBuilder.setTables(DB_TABLE_REMOTE);
//                Log.i(TAG, "query: ~~  ALL_TOUCHES_REMOTE");
                break;
            case ALL_TOUCHES_LOCAL:
                queryBuilder.setTables(DB_TABLE_LOCAL);
//                Log.i(TAG, "query: ~~  ALL_TOUCHES_LOCAL");
                break;
            case SINGLE_TOUCH_REMOTE:
                queryBuilder.setTables(DB_TABLE_REMOTE);
//                Log.i(TAG, "query: ~~ SINGLE_TOUCH_REMOTE");
                String id = uri.getLastPathSegment(); //tu bude asi chyba
                queryBuilder.appendWhere(DB_CREATED + "=" + id);
                break;

            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        return queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        /*
        The MIME types returned by ContentProvider.getType have two distinct parts:
        type/subType

        type portion indicates the well known type that is returned for a given URI by the Content provider, as the query
        methods can only return Cursor the type should always be:

        -vnd.android.cursor.dir for when you expect the Cursor to contain 0 through infinity item
        or
        -vnd.android.cursor.item for when you expect the Cursor to contain 1 item

        the subType portion can be either a well known subtype or something to your application

        So when using a ContentProvider you can customize the second subType portion of the MIME type,
        but not the first portion, eg. a valid MIME type for your apps ContentProvider could be:
        vnd.android.cursor.dir/vnd.myexample.whatever

        The MIME type returned from a ContentProvider can be used by an Intent to determine which activity to launch to handle
        the data retrieved from a give URI
          */
        switch (uriMatcher.match(uri)) {
            case ALL_TOUCHES_REMOTE:
                return "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + DB_TABLE_REMOTE;
            case ALL_TOUCHES_LOCAL:
                return "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + DB_TABLE_LOCAL;
            case SINGLE_TOUCH_REMOTE:
                return "vnd.android.cursor.item/vnd." + AUTHORITY + "." + DB_TABLE_REMOTE;
            case SINGLE_TOUCH_LOCAL:
                return "vnd.android.cursor.item/vnd." + AUTHORITY + "." + DB_TABLE_LOCAL;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    /*
    The insert() method adds a new row to the appropriate table, using the values
    in the ContentValues argument
    */
    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String tableName;
        switch (uriMatcher.match(uri)) {
            case ALL_TOUCHES_REMOTE:
                tableName = DB_TABLE_REMOTE;
                //do nothing
                break;
            case ALL_TOUCHES_LOCAL:
                tableName = DB_TABLE_LOCAL;
                //do nothing
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        long id = db.insert(tableName, null, contentValues);
        try {
            getContext().getContentResolver().notifyChange(uri, null);
        } catch (NullPointerException e) {
//            Log.i(TAG, "insert: ~~" + e);
        }
        return Uri.parse(uri + "/" + id);
    }

    /*
        This method deletes rows based on the selection or if and id is
        provided then it deletes a single row. The method returns the number of records affected from db
        */
    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        switch (uriMatcher.match(uri)) {
            case ALL_TOUCHES_REMOTE:
                //do nothing
                break;
            case SINGLE_TOUCH_REMOTE:
                String id = uri.getLastPathSegment();
                selection = DB_CREATED + "=" + id
                        + (!TextUtils.isEmpty(selection) ?
                        "AND (" + selection + ')' : "");
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        int deleteCount = db.delete(DB_TABLE_REMOTE, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return deleteCount;
    }

    /*
    this method is same as delete() which updates multiple rows
    based on the selection or single row if the row id is provided. The
    update method returns the number of updated rows
    */
    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        switch (uriMatcher.match(uri)) {
            case ALL_TOUCHES_REMOTE:
                //do nothing
                break;
            case SINGLE_TOUCH_REMOTE:
                String id = uri.getLastPathSegment();
                selection = DB_CREATED + "=" + id
                        + (!TextUtils.isEmpty(selection) ?
                        "AND (" + selection + ')' : "");
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        int updateCount = db.update(DB_TABLE_REMOTE, contentValues, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return updateCount;
    }
}
/*
MainActivity using LoaderManager for Country List - MainActivity.java
The list of countries is displayed in a ListView using the CursorLoader
 that queries the ContentResolver and returns a Cursor. This class implements
the loader protocol in a standard way for querying cursor, building an AsyncTaskLoader to perform the
cursor query on a background thread so that it does not block the application's UI. After the loader
has finished its loading just swap the new cursor from the Content provider and return the old Cursor

*/


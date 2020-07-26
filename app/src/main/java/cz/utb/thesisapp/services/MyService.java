package cz.utb.thesisapp.services;

import android.app.Service;

import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;


import cz.utb.thesisapp.contentProvider.MyContentProvider;
import cz.utb.thesisapp.services.firebase.FirebaseClient;
import cz.utb.thesisapp.services.kryonet.KryoClient;
import cz.utb.thesisapp.services.kryonet.Network;
import cz.utb.thesisapp.services.webServices.RestApi;
import cz.utb.thesisapp.services.webServices.Sse;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import android.content.Loader.*;

import static cz.utb.thesisapp.GlobalValues.DATE_FORMAT;
import static cz.utb.thesisapp.GlobalValues.DB_CREATED;
import static cz.utb.thesisapp.GlobalValues.DB_RECEIVED;
import static cz.utb.thesisapp.GlobalValues.DB_TOUCH_TYPE;
import static cz.utb.thesisapp.GlobalValues.DB_X;
import static cz.utb.thesisapp.GlobalValues.DB_Y;

public class MyService extends Service implements OnLoadCompleteListener<Cursor> {

    private static final String TAG = "MyService";
    // Binder given to clients
    private final IBinder mBinder = new MyBinder();
    // Random number generator
    private final Random mGenerator = new Random();
    public Broadcast broadcast = new Broadcast(this);
    public KryoClient kryoClient = new KryoClient(broadcast, this);
    public Sse sse = new Sse(this, broadcast);
    public SpeedTest speedTest = new SpeedTest(broadcast);
    public RestApi restApi = new RestApi(this, broadcast);
    public FirebaseClient firebaseClient = new FirebaseClient(this, broadcast);
    public Cursor oldCursor;
    private CursorLoader mCursorLoader;
    public boolean kryo = false;
    public boolean webservices = false;
    public boolean firebase = false;

    SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT);

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class MyBinder extends Binder {

        public MyService getService() {

            // Return this instance of LocalService so clients can call public methods
            return MyService.this;
        }
    }


    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
//        Log.i(TAG, "onTaskRemoved: called.");
        stopSelf();
    }

    @Override
    public void onDestroy() {
        if (mCursorLoader != null) {
//            mCursorLoader.unregisterListener((android.content.Loader.OnLoadCompleteListener<Cursor>) this);
            mCursorLoader.cancelLoad();
            mCursorLoader.stopLoading();
        }
        super.onDestroy();
//        Log.i(TAG, "onDestroy: called.");
    }

    public void saveToRemoteDatabase(Date created, Date clientReceived, float x, float y, String touchType) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        ContentValues contentValues = new ContentValues();
        contentValues.put(DB_TOUCH_TYPE, touchType);
        contentValues.put(DB_X, x);
        contentValues.put(DB_Y, y);
        contentValues.put(DB_CREATED, dateFormat.format(created));
        contentValues.put(DB_RECEIVED, dateFormat.format(clientReceived));
        getContentResolver().insert(MyContentProvider.CONTENT_REMOTE_URI, contentValues);
    }

    @Override
    public void onCreate() {
//        Log.i(TAG, "onCreateLoader: ~~");
        String[] projection = {
                DB_TOUCH_TYPE,
                DB_X,
                DB_Y,
                DB_CREATED
        };
        mCursorLoader = new CursorLoader(this, MyContentProvider.CONTENT_LOCAL_URI, projection, null, null, null);

        mCursorLoader.registerListener(0, this);
        mCursorLoader.startLoading();
    }

    @Override
    public void onLoadComplete(Loader<Cursor> loader, Cursor cursor) {
        Log.i(TAG, "onLoadComplete: ~~-------------------------------------------------");
        Log.i(TAG, "onLoadComplete: ~~" + cursor.getCount());
//        cursor.close();
        cursor.setNotificationUri(getContentResolver(), MyContentProvider.CONTENT_LOCAL_URI);
        if (oldCursor != null) {
            setViewModelTouch(cursor, oldCursor.getCount());
            oldCursor = cursor;
        } else if (oldCursor == null) {
            setViewModelTouch(cursor, 0);
            oldCursor = cursor;
        }
    }


    public void setViewModelTouch(Cursor cursor, int position) {
        int i = position;
        ArrayList<Network.Touch> touches = new ArrayList<>();
        while (cursor.moveToPosition(i)) {
            Log.i(TAG, "setViewModelTouch~~" + cursor.getString(0) + " creat:" + cursor.getString(3));
            Date created;
            try {
                created = df.parse(cursor.getString(3));
            } catch (ParseException e) {
//                Log.i(TAG, "setViewModelTouch: ~~" + e);
                created = new Date(System.currentTimeMillis());
            }
            Network.Touch touch = new Network.Touch();
            touch.touchType = cursor.getString(0);
            touch.x = cursor.getFloat(1);
            touch.y = cursor.getFloat(2);
            touch.clientCreated = created;

            touches.add(touch);
            i++;
        }
        if (touches.size() > 0) {
//            Log.i(TAG, "setViewModelTouch: ~~sending touch");
            if (firebase) {
                firebaseClient.sendTouch(touches);
            } else if (webservices) {
                restApi.sendTouch(touches);
            } else if (kryo) {
                kryoClient.sendTouches(touches);
            }

        }
    }


}
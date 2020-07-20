package cz.utb.thesisapp.services;

import android.app.Service;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import cz.utb.thesisapp.contentProvider.MyContentProvider;
import cz.utb.thesisapp.services.firebase.FirebaseClient;
import cz.utb.thesisapp.services.kryonet.KryoClient;
import cz.utb.thesisapp.services.webServices.RestApi;
import cz.utb.thesisapp.services.webServices.Sse;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import static cz.utb.thesisapp.GlobalValues.DATE_FORMAT;
import static cz.utb.thesisapp.GlobalValues.DB_CLIENT_CREATED;
import static cz.utb.thesisapp.GlobalValues.DB_CLIENT_RECEIVED;
import static cz.utb.thesisapp.GlobalValues.DB_TOUCH_TYPE;
import static cz.utb.thesisapp.GlobalValues.DB_X;
import static cz.utb.thesisapp.GlobalValues.DB_Y;

public class MyService extends Service {

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
        Log.d(TAG, "onTaskRemoved: called.");
        stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: called.");
    }

    public void saveToLocalDatabase(Date created, Date clientReceived, float x, float y, String touchType) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        ContentValues contentValues = new ContentValues();
        contentValues.put(DB_TOUCH_TYPE, touchType);
        contentValues.put(DB_X, x);
        contentValues.put(DB_Y, y);
        contentValues.put(DB_CLIENT_CREATED, dateFormat.format(created));
        contentValues.put(DB_CLIENT_RECEIVED, dateFormat.format(clientReceived));
        getContentResolver().insert(MyContentProvider.CONTENT_URI, contentValues);
    }
}
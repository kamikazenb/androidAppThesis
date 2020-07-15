package cz.utb.thesisapp;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionMenu;
import com.github.mikephil.charting.data.Entry;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.view.GravityCompat;
import androidx.lifecycle.ViewModelProviders;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.TimeUnit;

import cz.utb.thesisapp.contentProvider.DbHelper;
import cz.utb.thesisapp.contentProvider.MyContentProvider;
import cz.utb.thesisapp.services.MyService;
import cz.utb.thesisapp.services.SNTPClient;
import cz.utb.thesisapp.ui.home.HomeFragment;
import cz.utb.thesisapp.ui.home.HomeViewModel;
import cz.utb.thesisapp.ui.info.InfoViewModel;
import cz.utb.thesisapp.ui.touch.TouchViewModel;
import io.github.eterverda.sntp.SNTP;

import android.app.LoaderManager;

import static cz.utb.thesisapp.GlobalValues.*;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "MainActivity";
    private AppBarConfiguration mAppBarConfiguration;
    private DrawerLayout drawer;
    public MyService mService;
    boolean mBound = false;
    Context context;
    String currentlyShownTag;
    public LinkedHashMap<Integer, Date> operation = new LinkedHashMap<>();
    private HomeViewModel homeViewModel;
    private InfoViewModel infoViewModel;
    private TouchViewModel touchViewModel;
    private SimpleCursorAdapter dataAdapter;
    private Cursor oldCursor;
    SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
    public long difference = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        infoViewModel =
                ViewModelProviders.of(this).get(InfoViewModel.class);
        touchViewModel =
                ViewModelProviders.of(this).get(TouchViewModel.class);
        setContentView(R.layout.activity_main);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        context = this;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });
        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        Menu topLevelMenu = navigationView.getMenu();
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                topLevelMenu)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        currentlyShownTag = HomeFragment.class.getName();

        ((com.github.clans.fab.FloatingActionButton) findViewById(R.id.fabRefresh)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                homeViewModel.setRequireRefresh(true);
                ((FloatingActionMenu) findViewById(R.id.floatingActionMenu)).close(true);
            }
        });

        ((com.github.clans.fab.FloatingActionButton) findViewById(R.id.fabEdit)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mService.broadcast.sendValue(FILTER_MAIN_ACTIVITY, EXTRA_EDIT, true);
                ((FloatingActionMenu) findViewById(R.id.floatingActionMenu)).close(true);
            }
        });

//        delays.add(new Entry(5000,2));
        Log.d(TAG, "onCreate: ~~");
//        readFromLocalStorage();
        init();
    }

    //    rework
    /*
    private void readFromLocalStorage() {
        DbHelper dbHelper = new DbHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = dbHelper.readFromLocalDatabase(db);

        while (cursor.moveToNext()) {
            String created = cursor.getString(cursor.getColumnIndex(DB_CLIENT_CREATED));
            Log.d(TAG, "readFromLocalStorage: ~~" + created);
        }
        cursor.close();
        dbHelper.close();
    }
*/
    private void saveToLocalDatabase(Date created, float x, float y, String touchType) {

        ContentValues contentValues = new ContentValues();
        contentValues.put(DB_TOUCH_TYPE, touchType);
        contentValues.put(DB_X, x);
        contentValues.put(DB_Y, y);
        contentValues.put(DB_SERVER_RECEIVED, dateFormat.format(created));
        getContentResolver().insert(MyContentProvider.CONTENT_URI, contentValues);
    }


    public boolean ismBound() {
        return mBound;
    }

    public void setmBound(boolean mBound) {
        this.mBound = mBound;
    }

    private void showAlertDialog(String showDialog, final String tokenSeeker,
                                 final String service) {
        AlertDialog.Builder aletBuilder = new AlertDialog.Builder(context);
        aletBuilder.setMessage(showDialog);
        aletBuilder.setCancelable(false);
        aletBuilder.setPositiveButton(
                "Accept",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (service.equals("kryo")) {
                            mService.kryoClient.sendPairAcceptationResult(tokenSeeker, true);
                        }
                        dialog.cancel();
                    }
                });
        aletBuilder.setNegativeButton(
                "Decline",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (service.equals("kryo")) {
                            mService.kryoClient.unPair();
                        }
                        dialog.cancel();
                    }
                });

        AlertDialog alert11 = aletBuilder.create();
        try {
            alert11.show();
        } catch (Exception e) {
            Log.d(TAG, "onClick: ~~" + e);
        }

    }

    private final BroadcastReceiver infoReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra(EXTRA_DOWNLOAD)) {
                Entry entry = new Entry(getTime(),
                        (intent.getFloatExtra(EXTRA_DOWNLOAD, 0)) * 100);
                infoViewModel.downloadAddEntry(entry);
                if (intent.getIntExtra(EXTRA_SPEED_PROGRESS, 0) < 101) {
                    infoViewModel.setProgress(intent.getIntExtra(EXTRA_SPEED_PROGRESS, 0));
                    infoViewModel.setDownloadSpeedText(String.valueOf(intent.getFloatExtra(EXTRA_DOWNLOAD, 0)) + "MB/s");
                }

            }
            if (intent.hasExtra(EXTRA_UPLOAD)) {
                Entry entry = new Entry(getTime(),
                        (intent.getFloatExtra(EXTRA_UPLOAD, 0)) * 100);
                infoViewModel.uploadAddEntry(entry);
                infoViewModel.setUploadSpeedText(String.valueOf(intent.getFloatExtra(EXTRA_UPLOAD, 0)) + "MB/s");
                if (intent.getIntExtra(EXTRA_SPEED_PROGRESS, 0) == 101) {
                    infoViewModel.setProgress(0);
                } else {
                    infoViewModel.setProgress(intent.getIntExtra(EXTRA_SPEED_PROGRESS, 0));
                }
            }
        }
    };

    private float getTime() {
        long millis = System.currentTimeMillis();
        long millisWithoutDays = millis - TimeUnit.DAYS.toMillis(TimeUnit.MILLISECONDS.toDays(millis));
        String sb1 = Long.toString(millisWithoutDays);
        sb1 = sb1.substring(1);
        return Float.valueOf(sb1) / 1000;
    }

/*    private final BroadcastReceiver touchReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra(EXTRA_TOUCH_MOVE)) {
                ArrayList<Float> a = new ArrayList<>();
                a.add(intent.getFloatExtra(EXTRA_X, 0));
                a.add(intent.getFloatExtra(EXTRA_Y, 0));
                touchViewModel.setTouchMove(a);

//                saveToLocalDatabase(new Date(System.currentTimeMillis()), intent.getFloatExtra(EXTRA_X, 0),
//                        intent.getFloatExtra(EXTRA_Y, 0), EXTRA_TOUCH_MOVE);
            }
            if (intent.hasExtra(EXTRA_TOUCH_START)) {
                ArrayList<Float> a = new ArrayList<>();
                a.add(intent.getFloatExtra(EXTRA_X, 0));
                a.add(intent.getFloatExtra(EXTRA_Y, 0));
                touchViewModel.setTouchStart(a);
//                saveToLocalDatabase(new Date(System.currentTimeMillis()), intent.getFloatExtra(EXTRA_X, 0),
//                        intent.getFloatExtra(EXTRA_Y, 0), EXTRA_TOUCH_START);
            }
            if (intent.hasExtra(EXTRA_TOUCH_UP)) {
                touchViewModel.setTouchUp(true);
//                saveToLocalDatabase(new Date(System.currentTimeMillis()), (float) 0.0,
//                        (float) 0.0, EXTRA_TOUCH_UP);
            }
        }
    };*/


    private final BroadcastReceiver kryoReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra(EXTRA_PAIRED)) {
                try {
                    homeViewModel.setPairedname(intent.getStringExtra(EXTRA_PAIRED));
                    homeViewModel.setPaired(true);
                } catch (Exception e) {

                }
            }
            if (intent.hasExtra(EXTRA_KROYSERVER_USE_DATABASE)) {
                try {
                    homeViewModel.setKryoUseDatabase(intent.getBooleanExtra(EXTRA_KROYSERVER_USE_DATABASE, true));
                } catch (Exception e) {

                }
            }
            if (intent.hasExtra(EXTRA_UNPAIRED)) {
                try {
                    homeViewModel.setPaired(false);
                    homeViewModel.setPairedname(intent.getStringExtra(""));
                } catch (Exception e) {

                }
            }
            if (intent.hasExtra(EXTRA_COMMAND)) {
                if (intent.getStringExtra(EXTRA_COMMAND).equals(EXTRA_COMMAND_SET_CHECKED)) {
                    homeViewModel.setKryoConnected(true);
                }
                if (intent.getStringExtra(EXTRA_COMMAND).equals(EXTRA_COMMAND_SET_UNCHECKED)) {
                    homeViewModel.setKryoConnected(false);
                }
            }
            if (intent.hasExtra(EXTRA_USERS)) {
                try {
                    HashMap<String, String> hashMap = (HashMap<String, String>) intent.getSerializableExtra("users");
                    Log.d(TAG, "onReceive: ~~1" + hashMap.toString());
                    homeViewModel.setUsers(hashMap);
                } catch (Exception e) {

                }
            }
            if (intent.hasExtra(EXTRA_ACCEPT_PAIR_REQUEST)) {
                HashMap<String, String> hashMap = (HashMap<String, String>) intent.getSerializableExtra("acceptPair");
                showAlertDialog("Do you accept sync with: \n \n" + hashMap.values().toArray()[0].toString(),
                        hashMap.keySet().toArray()[0].toString(), "kryo");
            }

            if (intent.hasExtra(EXTRA_CONNECTION_CLOSED)) {
                homeViewModel.setKryoConnected(false);
                Log.d(TAG, "onReceive: ~~in if" + intent.getExtras().getString(EXTRA_CONNECTION_CLOSED));
                Toast.makeText(getApplicationContext(), intent.getExtras().getString(EXTRA_CONNECTION_CLOSED), Toast.LENGTH_SHORT).show();
            }
        }
    };

    public void sendTouch(float x, float y, String touchType) {

        if (ismBound()) {
            if (mService.kryoClient.isClientsConnected()) {
                addTimeStamp(x, y);
                mService.kryoClient.sendTouch(x, y, touchType);
            } else if(mService.webServicesSelected){
                addTimeStamp(x, y);
                mService.restApi.sendTouch(x, y, touchType);
            }
        }
    }

    public void addTimeStamp(float x, float y) {
        Float z = x + y;
        operation.put(z.hashCode(), new Date(System.currentTimeMillis()));
    }


    public void startKryo(String ip, String userName) {
        if (ismBound()) {
            if (!mService.kryoClient.isClientsConnected()) {
                mService.kryoClient.newClients(ip, userName);
            }
        }
        Thread t = new Thread() {
            public void run() {
                try {
                    long global = SNTP.currentTimeMillisFromNetwork();
                    difference = global - System.currentTimeMillis();
                } catch (IOException e) {
                    Log.d(TAG, "run: ~~" + e);
                }

            }
        };
        t.start();
    }

    public void sendRequest(boolean speed, boolean registeredUsers) {
        if (ismBound()) {
            if (mService.kryoClient.isClientsConnected()) {
                mService.kryoClient.sendRequest(speed, registeredUsers);
            }
        }
    }

    public void stopKryo() {
        if (ismBound()) {
            if (mService.kryoClient.isClientsConnected()) {
                mService.kryoClient.stopClients();
            }
        }
    }

    public void startWebServices(String ip) {
        if (ismBound()) {
            mService.webServicesSelected = true;
            mService.sse.start(ip);
            mService.restApi.startRestApi(ip);
        }
    }

    public void stopWebServices() {
        if (ismBound()) {
            mService.webServicesSelected = false;
            mService.sse.stop();
            mService.restApi.stop();
        }
    }

    public void requestPartner(String token) {
        if (ismBound()) {
            if (mService.kryoClient.isClientsConnected()) {
                mService.kryoClient.requestPartner(token);
            }
        }
    }

    public void kryoUnpair() {
        if (ismBound()) {
            if (mService.kryoClient.isClientsConnected()) {
                mService.kryoClient.unPair();
            }
        }
    }

    public void kryoUseDatabase(Boolean bool) {
        if (ismBound()) {
            mService.kryoClient.sendUseDatabase(bool);
        }
    }

    public void kryoPairResponse(String seekerToken, boolean response) {
        mService.kryoClient.sendPairAcceptationResult(seekerToken, response);
    }

    private void startService() {
        Log.d(TAG, "run: ~~ " + Thread.currentThread().getId());
        Intent i = new Intent(this, MyService.class);
        startService(i);
        bindService();
    }

    private void bindService() {
        Intent i = new Intent(this, MyService.class);
        bindService(i, connection, Context.BIND_AUTO_CREATE);
    }

    public void startDownloadTest() {
        if (ismBound()) {
            mService.speedTest.startDownload();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        startService();
        Log.d(TAG, "onStart: ~~");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: ~~");
        try {
            unbindService(connection);
        } catch (Exception e) {
            Log.d(TAG, "~~" + e);
        }

        mBound = false;
        homeViewModel.setmBounded(mBound);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(kryoReceiver);
    }

    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MyService.MyBinder binder = (MyService.MyBinder) service;
            mService = binder.getService();
            mBound = true;
            homeViewModel.setmBounded(mBound);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
            homeViewModel.setmBounded(mBound);
        }
    };

    //old
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        LocalBroadcastManager.getInstance(this).registerReceiver(kryoReceiver, new IntentFilter(FILTER_KRYO));
        LocalBroadcastManager.getInstance(this).registerReceiver(infoReceiver, new IntentFilter(FILTER_INFO));
//        LocalBroadcastManager.getInstance(this).registerReceiver(touchReceiver, new IntentFilter(FILTER_TOUCH));
        startService();
        Log.d(TAG, "onResume: ~~");
        //Starts a new or restarts an existing Loader in this manager
        getLoaderManager().restartLoader(0, null, this);
        super.onResume();
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                DB_TOUCH_TYPE,
                DB_X,
                DB_Y,
                DB_CLIENT_CREATED,
                DB_SERVER_RECEIVED,
                DB_CLIENT_RECEIVED
        };
        return new CursorLoader(this,
                MyContentProvider.CONTENT_URI, projection, null, null, null);
    }

    public void init() {
        DbHelper dbHelper = new DbHelper(this);
        dbHelper.onUpgrade(dbHelper.getWritableDatabase(), 0, 1);
        getLoaderManager().initLoader(0, null, this);
        SNTPClient.getDate(Calendar.getInstance().getTimeZone(), new SNTPClient.Listener() {
            @Override
            public void onTimeReceived(String rawDate) {
                // rawDate -> 2019-11-05T17:51:01+0530
                Log.d(SNTPClient.TAG, "server~~" + rawDate);
                Log.d(SNTPClient.TAG, "local~~" + dateFormat.format(new Date(System.currentTimeMillis())));
            }

            @Override
            public void onError(Exception ex) {
                Log.e(SNTPClient.TAG, ex.getMessage());
            }
        });
        SNTP.init();
        Thread t = new Thread() {
            public void run() {
                long global = SNTP.safeCurrentTimeMillis();
                difference = global - System.currentTimeMillis();
            }
        };
        t.start();

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        Log.d(TAG, "onLoadFinished: ~~-------------------------------------------");
        cursor.setNotificationUri(getContentResolver(), MyContentProvider.CONTENT_URI);
        if (oldCursor != null) {
            int diff = cursor.getCount() - oldCursor.getCount();
            setViewModelTouch(cursor, oldCursor.getCount());
            oldCursor = cursor;
        } else {
            setViewModelTouch(cursor, 0);
            oldCursor = cursor;
        }
    }

    public void setViewModelTouch(Cursor cursor, int position) {
        int i = position;
        ArrayList<Touch> al = new ArrayList<>();
        while (cursor.moveToPosition(i)) {
          /*  Log.d(TAG, "~~" + cursor.getString(0) + " creat:" + cursor.getString(3)
                    + " serverRec:" + cursor.getString(4) + " clientRec:" + cursor.getString(5));*/
            Touch touch = new Touch();
            touch.touchType = cursor.getString(0);
            touch.x = cursor.getFloat(1);
            touch.y = cursor.getFloat(2);
            try {
                touch.clientCreated = dateFormat.parse(cursor.getString(3));
                touch.serverReceived = dateFormat.parse(cursor.getString(4));
                touch.clientReceived = dateFormat.parse(cursor.getString(5));
            } catch (ParseException e) {
                Log.d(TAG, "setViewModelTouch: ~~" + e);
                touch.clientCreated = new Date(System.currentTimeMillis());
                touch.serverReceived = new Date(System.currentTimeMillis());
                touch.clientReceived = new Date(System.currentTimeMillis());
            }
            al.add(touch);
            i++;
        }
        touchViewModel.setTouch(al);
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
//        dataAdapter.swapCursor(null);
    }

    public void dltDatabase(String where) {
        String selection = DB_TOUCH_TYPE + " LIKE ?";
        String[] selectionArgs = {where};
        getContentResolver().delete(MyContentProvider.CONTENT_URI, selection, selectionArgs);
    }


}

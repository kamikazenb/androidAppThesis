package cz.utb.thesisapp;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;
import com.github.mikephil.charting.data.Entry;
import com.google.android.material.navigation.NavigationView;
import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;
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

import java.io.File;
import java.io.FileWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import cz.utb.thesisapp.contentProvider.CSVWriter;
import cz.utb.thesisapp.contentProvider.DbHelper;
import cz.utb.thesisapp.contentProvider.MyContentProvider;
import cz.utb.thesisapp.services.MyService;
import cz.utb.thesisapp.services.TokenGenerator;
import cz.utb.thesisapp.ui.home.HomeFragment;
import cz.utb.thesisapp.ui.home.HomeViewModel;
import cz.utb.thesisapp.ui.info.InfoViewModel;
import cz.utb.thesisapp.ui.touch.TouchViewModel;
import android.app.LoaderManager;
import static cz.utb.thesisapp.GlobalValues.*;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "MainActivity";
    private AppBarConfiguration mAppBarConfiguration;
    private DrawerLayout drawer;
    public MyService mService;
    boolean mBound = false;
    Context context;
    private HomeViewModel homeViewModel;
    private InfoViewModel infoViewModel;
    private TouchViewModel touchViewModel;
    private Cursor oldCursor;
    private String token;


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
        context = this;
        SpeedDialView speedDialView = findViewById(R.id.speedDial);
        speedDialView.addActionItem(
                new SpeedDialActionItem.Builder(R.id.fab_start_test, R.drawable.ic_network_check_white_36dp).setLabel("test")
                        .create());
        speedDialView.setOnActionSelectedListener(new SpeedDialView.OnActionSelectedListener() {
            @Override
            public boolean onActionSelected(SpeedDialActionItem actionItem) {
                switch (actionItem.getId()) {
                    case R.id.fab_start_test:

                        Log.i(TAG, "onActionSelected: ~~" + actionItem.getId());
                        touchViewModel.setTest(TOUCH_FAB_TOUCHED);
                        break;
                    default:
                }
                return false;
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
        DbHelper dbHelper = new DbHelper(this);
        dbHelper.onUpgrade(dbHelper.getWritableDatabase(), 0, 1);
        getLoaderManager().initLoader(0, null, this);
    }

    public boolean ismBound() {
        return mBound;
    }

    public void setmBound(boolean mBound) {
        this.mBound = mBound;
    }

    private final BroadcastReceiver webReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra(EXTRA_CONNECTION_CLOSED)) {
                homeViewModel.setWebConnected(false);
                Toast.makeText(getApplicationContext(), intent.getExtras().getString(EXTRA_CONNECTION_CLOSED), Toast.LENGTH_SHORT).show();
                stopWebServices();
            }

        }
    };
    private final BroadcastReceiver infoReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra(EXTRA_USER_INFO)) {
                Toast.makeText(getApplicationContext(), intent.getExtras().getString(EXTRA_USER_INFO), Toast.LENGTH_SHORT).show();
            }
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


    private final BroadcastReceiver kryoReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.hasExtra(EXTRA_KROYSERVER_USE_DATABASE)) {
                try {
                    homeViewModel.setKryoUseDatabase(intent.getBooleanExtra(EXTRA_KROYSERVER_USE_DATABASE, true));
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
                    Log.i(TAG, "onReceive: ~~1" + hashMap.toString());
                    homeViewModel.setUsers(hashMap);
                } catch (Exception e) {

                }
            }

            if (intent.hasExtra(EXTRA_CONNECTION_CLOSED)) {
                homeViewModel.setKryoConnected(false);
                Log.i(TAG, "onReceive: ~~in if" + intent.getExtras().getString(EXTRA_CONNECTION_CLOSED));
                Toast.makeText(getApplicationContext(), intent.getExtras().getString(EXTRA_CONNECTION_CLOSED), Toast.LENGTH_SHORT).show();
            }
        }
    };

    public void sendTouch(float x, float y, String touchType) {
        saveToLocalDatabase(new Date(System.currentTimeMillis()), x, y, touchType);
    }


    public void startKryo(String ip, String userName) {
        if (ismBound()) {
            if (!mService.kryoClient.isClientConnected()) {
                mService.kryoClient.newClient(ip, userName);
                homeViewModel.setKryoConnected(true);
            }
        } else {
            homeViewModel.setKryoConnected(false);
        }

    }

    public void stopKryo() {
        homeViewModel.setKryoConnected(false);
        if (ismBound()) {
            if (mService.kryoClient.isClientConnected()) {
                mService.kryoClient.stopClient();
            }
        }
    }

    public void startWebServices(String ip, String name) {
        if (ismBound()) {
            homeViewModel.setWebConnected(true);
            TokenGenerator tg = new TokenGenerator();
            token = tg.generateRandom(20);
            mService.sse.start(ip, token);
            mService.restApi.startRestApi(ip, name, token);
            mService.webservices = true;
        } else {
            homeViewModel.setWebConnected(false);
        }
    }

    public void startFirebaseServices(String name) {
        if (ismBound()) {
            homeViewModel.setFirebaseConnected(true);
            TokenGenerator tg = new TokenGenerator();
            token = tg.generateRandom(20);
            mService.firebaseClient.start(homeViewModel.getFirebaseRemoteListener().getValue(), token, name);
            mService.firebase = true;
        } else {
            homeViewModel.setFirebaseConnected(false);
        }
    }

    public void setFirebaseRemoteListener() {
        if (ismBound()) {
            mService.firebaseClient.start(homeViewModel.getFirebaseRemoteListener().getValue());
        } else {
            homeViewModel.setFirebaseConnected(false);
        }
    }


    public void stopWebServices() {
        homeViewModel.setWebConnected(false);
        if (ismBound()) {
            mService.sse.stop();
            mService.restApi.stop();
            mService.webservices = false;
        }
    }

    public void stopFirebase() {
        homeViewModel.setFirebaseConnected(false);
        if (ismBound()) {
            mService.firebaseClient.stop();
            mService.firebase = false;
        }
    }


    public void kryoUnfollow() {
        if (ismBound()) {
            if (mService.kryoClient.isClientConnected()) {
                mService.kryoClient.unFollow();
            }
        }
    }

    public void kryoRequestFollow(String token) {
        if (ismBound()) {
            if (mService.kryoClient.isClientConnected()) {
                mService.kryoClient.requestFollow(token);
            }
        }
    }

    public void kryoUseDatabase(Boolean bool) {
        if (ismBound()) {
            mService.kryoClient.sendUseDatabase(bool);
        }
    }

    private void startService() {
        Log.i(TAG, "run: ~~ " + Thread.currentThread().getId());
        Intent i = new Intent(this, MyService.class);
        startService(i);
        bindService();
    }

    private void bindService() {
        Intent i = new Intent(this, MyService.class);
        bindService(i, connection, Context.BIND_AUTO_CREATE);
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

    public void startDownloadTest() {
        if (ismBound()) {
            mService.speedTest.startDownload();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        startService();
        Log.i(TAG, "onStart: ~~");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop: ~~");
        try {
            removeOldDB();
            mService.kryoClient.stopClient();
            mService.firebaseClient.stop();
            mService.sse.stop();
            unbindService(connection);
        } catch (Exception e) {
            Log.i(TAG, "~~" + e);
        }

        mBound = false;
        homeViewModel.setmBounded(mBound);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(kryoReceiver);
    }



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
        LocalBroadcastManager.getInstance(this).registerReceiver(webReceiver, new IntentFilter(FILTER_WEB));
        startService();
        Log.i(TAG, "onResume: ~~");
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

    public void exportDB(String databaseType, Date time) {
        boolean remoteDatabase = false;
        DbHelper dbHelper = new DbHelper(this);

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        File exportDir = new File(context.getExternalFilesDir(null), "");
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }

        File file = new File(exportDir, new SimpleDateFormat(DATE_FORMAT).format(time) + " " + databaseType + ".csv");
        try {
            file.createNewFile();
            CSVWriter csvWrite = new CSVWriter(new FileWriter(file));
            Cursor curCSV = db.rawQuery("SELECT * FROM " + databaseType, null);
            csvWrite.writeNext(curCSV.getColumnNames());
            while (curCSV.moveToNext()) {
                //Which column you want to exprort
                String[] arrStr;
                try {
                    arrStr = new String[]{curCSV.getString(0), curCSV.getString(1), curCSV.getString(2),
                            curCSV.getString(3), curCSV.getString(4), curCSV.getString(5),};
                    remoteDatabase = true;
                } catch (Exception e) {
                    arrStr = new String[]{curCSV.getString(0), curCSV.getString(1), curCSV.getString(2),
                            curCSV.getString(3), curCSV.getString(4),};
                }
                csvWrite.writeNext(arrStr);

            }
            csvWrite.close();
            curCSV.close();
            if (remoteDatabase) {
                removeOldDB();
                Toast.makeText(getApplicationContext(), "Database exported", Toast.LENGTH_SHORT).show();

            }
            Log.i(TAG, "exportDB: ~~Exported " + databaseType);
        } catch (Exception sqlEx) {
            Log.i(TAG, "exportDB: ~~" + sqlEx.getMessage(), sqlEx);
//            Log.i(TAG, sqlEx.getMessage(), sqlEx);
        }
    }

    private void removeOldDB() {
        DbHelper dbHelper = new DbHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        dbHelper.onUpgrade(db, 0, 1);
        if (ismBound()) {
            mService.oldCursor = null;
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        Log.i(TAG, "onCreateLoader: ~~");
        String[] projection = {
                DB_TOUCH_TYPE,
                DB_X,
                DB_Y,
                DB_CREATED,
                DB_RECEIVED
        };
        return new CursorLoader(this,
                MyContentProvider.CONTENT_REMOTE_URI, projection, null, null, null);
    }


    public void init() {
        Log.i(TAG, "init: ~~");


    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
//        Log.i(TAG, "onLoadFinished: ~~------------------------------------------");
        cursor.setNotificationUri(getContentResolver(), MyContentProvider.CONTENT_REMOTE_URI);
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
            Touch touch = new Touch();
            touch.touchType = cursor.getString(0);
            touch.x = cursor.getFloat(1);
            touch.y = cursor.getFloat(2);
            try {
                touch.clientCreated = new SimpleDateFormat(DATE_FORMAT).parse(cursor.getString(3));
                touch.clientReceived = new SimpleDateFormat(DATE_FORMAT).parse(cursor.getString(4));
            } catch (ParseException e) {
                Log.i(TAG, "setViewModelTouch: ~~" + e);
                touch.clientCreated = new Date(System.currentTimeMillis());
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

    public void saveToLocalDatabase(Date created, float x, float y, String touchType) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        ContentValues contentValues = new ContentValues();
        contentValues.put(DB_TOUCH_TYPE, touchType);
        contentValues.put(DB_X, x);
        contentValues.put(DB_Y, y);
        contentValues.put(DB_CREATED, dateFormat.format(created));
        getContentResolver().insert(MyContentProvider.CONTENT_LOCAL_URI, contentValues);
    }


}

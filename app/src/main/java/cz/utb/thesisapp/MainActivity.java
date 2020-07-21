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
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.github.clans.fab.FloatingActionMenu;
import com.github.mikephil.charting.data.Entry;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;

import androidx.annotation.LongDef;
import androidx.core.view.GravityCompat;
import androidx.lifecycle.Observer;
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
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import cz.utb.thesisapp.contentProvider.CSVWriter;
import cz.utb.thesisapp.contentProvider.DbHelper;
import cz.utb.thesisapp.contentProvider.MyContentProvider;
import cz.utb.thesisapp.services.MyService;
import cz.utb.thesisapp.services.TokenGenerator;
import cz.utb.thesisapp.ui.home.HomeFragment;
import cz.utb.thesisapp.ui.home.HomeViewModel;
import cz.utb.thesisapp.ui.info.InfoViewModel;
import cz.utb.thesisapp.ui.touch.TouchFragment;
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
    String currentlyShownTag;
    public LinkedHashMap<Integer, Date> operation = new LinkedHashMap<>();
    private HomeViewModel homeViewModel;
    private InfoViewModel infoViewModel;
    private TouchViewModel touchViewModel;
    private SimpleCursorAdapter dataAdapter;
    private Cursor oldCursor;
    SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
    private String token;
    private Firebase firebase;
    private final static Lock lock = new ReentrantLock();

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
        /*  FloatingActionButton fab = findViewById(R.id.fab);*/
        context = this;
      /*  fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });*/
        SpeedDialView speedDialView = findViewById(R.id.speedDial);
        speedDialView.addActionItem(
                new SpeedDialActionItem.Builder(R.id.fab_start_test, R.drawable.ic_network_check_white_36dp).setLabel("Start test")
                        .create());
        speedDialView.setOnActionSelectedListener(new SpeedDialView.OnActionSelectedListener() {
            @Override
            public boolean onActionSelected(SpeedDialActionItem actionItem) {
                switch (actionItem.getId()) {
                    case R.id.fab_start_test:
                        Log.d(TAG, "onActionSelected: ~~" + actionItem.getId());
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

        currentlyShownTag = HomeFragment.class.getName();


        Log.d(TAG, "onCreate: ~~");

        init();


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
                    Log.d(TAG, "onReceive: ~~1" + hashMap.toString());
                    homeViewModel.setUsers(hashMap);
                } catch (Exception e) {

                }
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
            if (mService.kryoClient.isClientConnected()) {
                addTimeStamp(x, y);
                mService.kryoClient.sendTouch(x, y, touchType);
            } else if (homeViewModel.getWebConnected().getValue()) {
                addTimeStamp(x, y);
                mService.restApi.sendTouch(x, y, touchType, token);
            } else if (homeViewModel.getFirebaseConnected().getValue()) {
                addTimeStamp(x, y);
                mService.firebaseClient.sendTouch(x, y, touchType);


            }
        }
    }

    public void addTimeStamp(float x, float y) {
    /*    Float z = x + y;
        operation.put(z.hashCode(), new Date(System.currentTimeMillis()));*/
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
        }
    }

    public void stopFirebase() {
        homeViewModel.setFirebaseConnected(false);
        if (ismBound()) {
            mService.firebaseClient.stop();
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
        LocalBroadcastManager.getInstance(this).registerReceiver(webReceiver, new IntentFilter(FILTER_WEB));
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
                DB_CLIENT_RECEIVED
        };
        return new CursorLoader(this,
                MyContentProvider.CONTENT_URI, projection, null, null, null);
    }

    public void exportDB() {
        Log.d(TAG, "exportDB: ~~starting");
        DbHelper dbHelper = new DbHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        File exportDir = new File(context.getExternalFilesDir(null), "");
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }

        File file = new File(exportDir, "test" + dateFormat.format(new Date()) + ".csv");
        try {
            file.createNewFile();
            CSVWriter csvWrite = new CSVWriter(new FileWriter(file));
            Cursor curCSV = db.rawQuery("SELECT * FROM " + DB_TABLE_NAME, null);
            csvWrite.writeNext(curCSV.getColumnNames());
            while (curCSV.moveToNext()) {
                //Which column you want to exprort
                String arrStr[] = {curCSV.getString(0), curCSV.getString(1), curCSV.getString(2),
                        curCSV.getString(3), curCSV.getString(4), curCSV.getString(5),};
                csvWrite.writeNext(arrStr);
            }
            csvWrite.close();
            curCSV.close();
            dbHelper.onUpgrade(db, 0, 1);
            Log.d(TAG, "exportDB: ~~Exported");
        } catch (Exception sqlEx) {
            Log.d(TAG, "exportDB: ~~" + sqlEx.getMessage(), sqlEx);
//            Log.d(TAG, sqlEx.getMessage(), sqlEx);
        }
    }

    public void init() {
        DbHelper dbHelper = new DbHelper(this);
        dbHelper.onUpgrade(dbHelper.getWritableDatabase(), 0, 1);
        getLoaderManager().initLoader(0, null, this);

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
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
            Touch touch = new Touch();
            touch.touchType = cursor.getString(0);
            touch.x = cursor.getFloat(1);
            touch.y = cursor.getFloat(2);
            try {
                touch.clientCreated = dateFormat.parse(cursor.getString(3));
                touch.clientReceived = dateFormat.parse(cursor.getString(4));
            } catch (ParseException e) {
                Log.d(TAG, "setViewModelTouch: ~~" + e);
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


}

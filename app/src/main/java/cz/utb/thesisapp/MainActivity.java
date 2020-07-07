package cz.utb.thesisapp;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.Menu;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.TimeUnit;

import cz.utb.thesisapp.services.MyService;
import cz.utb.thesisapp.ui.home.HomeFragment;
import cz.utb.thesisapp.ui.home.HomeViewModel;
import cz.utb.thesisapp.ui.info.InfoViewModel;
import cz.utb.thesisapp.ui.touch.TouchViewModel;

import static cz.utb.thesisapp.GlobalValues.*;

public class MainActivity extends AppCompatActivity {
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

    private final BroadcastReceiver touchReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra(EXTRA_TOUCH_MOVE)) {
                ArrayList<Float> a = new ArrayList<>();
                a.add(intent.getFloatExtra(EXTRA_X, 0));
                a.add(intent.getFloatExtra(EXTRA_Y, 0));
                touchViewModel.setTouchMove(a);
            }
            if (intent.hasExtra(EXTRA_TOUCH_START)) {
                ArrayList<Float> a = new ArrayList<>();
                a.add(intent.getFloatExtra(EXTRA_X, 0));
                a.add(intent.getFloatExtra(EXTRA_Y, 0));
                touchViewModel.setTouchStart(a);
            }
            if (intent.hasExtra(EXTRA_TOUCH_UP)) {

                touchViewModel.setTouchUp(true);
            }
        }
    };


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


    public void sendTouchStart(float x, float y) {

        if (ismBound()) {
            if (mService.kryoClient.isClientsConnected()) {
                addTimeStamp(x, y);
                mService.kryoClient.sendTouchStart(x, y);
            }
        }
    }

    public void addTimeStamp(float x, float y) {
        Float z = x + y;
        operation.put(z.hashCode(), new Date(System.currentTimeMillis()));
    }

    public void sendTouchMove(float x, float y) {

        if (ismBound()) {
            if (mService.kryoClient.isClientsConnected()) {
                addTimeStamp(x, y);
                mService.kryoClient.sendTouchMove(x, y);
            }
        }
    }

    public void sendTouchUp(boolean state) {
        if (ismBound()) {
            if (mService.kryoClient.isClientsConnected()) {
                mService.kryoClient.sendTouchUp(state);
            }
        }
    }

    public void startKryo(String ip, String userName) {
        if (ismBound()) {
            if (!mService.kryoClient.isClientsConnected()) {
                mService.kryoClient.newClients(ip, userName);
            }
        }
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
        LocalBroadcastManager.getInstance(this).registerReceiver(touchReceiver, new IntentFilter(FILTER_TOUCH));
        startService();
        Log.d(TAG, "onResume: ~~");
        super.onResume();
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}

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
import cz.utb.thesisapp.services.*;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.view.GravityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.HashMap;

import cz.utb.thesisapp.services.MyService;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private AppBarConfiguration mAppBarConfiguration;
    private DrawerLayout drawer;
    public MyService mService;
    boolean mBound = false;
    Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        context = this;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mBound) {
                    // Call a method from the LocalService.
                    // However, if this call were something that might hang, then this request should
                    // occur in a separate thread to avoid slowing down the activity performance.

                }
            }
        });

        Log.i(TAG, "~onCreate0");
        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        Log.d(TAG, "~~onCreate1");
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter("kryo"));
    }

    public boolean ismBound() {
        return mBound;
    }

    public void setmBound(boolean mBound) {
        this.mBound = mBound;
    }

    private void showAlertDialog(String showDialog, final String tokenSeeker, final String service){
        AlertDialog.Builder aletBuilder = new AlertDialog.Builder(context);
        aletBuilder.setMessage(showDialog);
        aletBuilder.setCancelable(false);
        aletBuilder.setPositiveButton(
                "Accept",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if(service.equals("kryo")){
                            mService.kryoClient.sendPairAcceptationResult(tokenSeeker, true);
                        }
                        dialog.cancel();
                    }
                });
        aletBuilder.setNegativeButton(
                "Decline",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if(service.equals("kryo")){
                            mService.kryoClient.unPair();
                        }
                        dialog.cancel();
                    }
                });

        AlertDialog alert11 = aletBuilder.create();
        try {
            alert11.show();
        }catch (Exception e){
            Log.d(TAG, "onClick: ~~"+e);
        }
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive: ~~start");
            if (intent.hasExtra("acceptPair")) {
                HashMap<String, String> hashMap = (HashMap<String, String>) intent.getSerializableExtra("acceptPair");
                showAlertDialog("Do you accept sync with: \n \n"+hashMap.values().toArray()[0].toString(),
                        hashMap.keySet().toArray()[0].toString(), "kryo");
            }
            if (intent.hasExtra("userInfo")) {
                Log.d(TAG, "onReceive: ~~in if"+intent.getExtras().getString("userInfo"));

                Toast.makeText(getApplicationContext(), intent.getExtras().getString("userInfo"), Toast.LENGTH_SHORT).show();
            }

        }
    };

    public void sendTouchStart(float x, float y){
        if(ismBound()){
            mService.kryoClient.sendTouchStart(x, y);
        }
    }
    public void sendTouchMove(float x, float y){
        if(ismBound()){
            mService.kryoClient.sendTouchMove(x, y);
        }
    }
    public void sendTouchUp(boolean state){
        if(ismBound()){
            mService.kryoClient.sendTouchUp(state);
        }
    }
    public void startKryo(String ip) {
        if(ismBound()){
            mService.kryoClient.newClients(ip);
        }
    }

    public void stopKryo() {
        if(ismBound()){
            mService.kryoClient.stopClients();
        }
    }

    public void requestPartner(String token) {
        if(ismBound()){
            mService.kryoClient.requestPartner(token);
        }
    }
    public void kryoUnpair(){
        mService.kryoClient.unPair();
    }

    public void kryoPairResponse(String seekerToken, boolean response){
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

    @Override
    protected void onStart() {
        super.onStart();
        startService();

    }

    public static class Receiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String data = intent.getStringExtra("data");
            Log.d(TAG, "onReceive: ~~" + data);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: ");
        try {
            unbindService(connection);
        } catch (Exception e) {
            Log.d(TAG, "~~" + e);
        }

        mBound = false;
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
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
        }
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
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
        super.onResume();
        startService();
        Log.d(TAG, "onResume: ~~");
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}

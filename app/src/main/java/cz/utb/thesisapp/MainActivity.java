package cz.utb.thesisapp;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.sip.SipSession;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.widget.Button;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.core.view.GravityCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private AppBarConfiguration mAppBarConfiguration;
    private DrawerLayout drawer;

    private MyService mService;
    private MainActivityViewModel mViewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleUpdates();
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

        mViewModel = ViewModelProviders.of(this).get(MainActivityViewModel.class);
        setObservers();

    }

    private void setObservers() {
        mViewModel.getBinder().observe(this, new Observer<MyService.MyBinder>() {
            @Override
            public void onChanged(MyService.MyBinder myBinder) {
                if (myBinder == null) {
                    Log.d(TAG, "onChanged: ~~unbound from service");
                } else {
                    Log.d(TAG, "onChanged: ~~bound to service.");
                    mService = myBinder.getService();
                }
            }
        });

        mViewModel.getIsProgressBarUpdating().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(final Boolean aBoolean) {
                final Handler handler = new Handler();
                final Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        if (aBoolean) {
                            if (mViewModel.getBinder().getValue() != null) { // meaning the service is bound
                                if (mService.getProgress() == mService.getMaxValue()) {
                                    mViewModel.setIsProgressBarUpdating(false);
                                }
//                                Log.d(TAG, "run: ~~mService.getProgress() " + mService.getProgress());
//                                Log.d(TAG, "run: ~~mService.getMaxValue() " + mService.getMaxValue());
                                Log.d(TAG, "run: ~~" + String.valueOf(100 * mService.getProgress() / mService.getMaxValue()) + "%");

                            }
                            handler.postDelayed(this, 100);
                        } else {
                            handler.removeCallbacks(this);
                        }
                    }
                };

                // control what the button shows
                if (aBoolean) {
                    Log.d(TAG, "onChanged: ~~pause");
                    handler.postDelayed(runnable, 100);

                } else {
                    if (mService.getProgress() == mService.getMaxValue()) {
                        Log.d(TAG, "onChanged: ~~restart");
                    } else {
                        Log.d(TAG, "onChanged: ~~start");
                    }
                }
            }
        });
    }

    private void toggleUpdates() {
        if (mService != null) {
            if (mService.getProgress() == mService.getMaxValue()) {
                mService.resetTask();
                Log.d(TAG, "toggleUpdates: ~~start");
            } else {
                if (mService.getIsPaused()) {
                    mService.unPausePretendLongRunningTask();
                    mViewModel.setIsProgressBarUpdating(true);
                } else {
                    mService.pausePretendLongRunningTask();
                    mViewModel.setIsProgressBarUpdating(false);
                }
            }

        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mViewModel.getBinder() != null) {
            unbindService(mViewModel.getServiceConnection());
        }
    }

    private void startService(){
        Intent serviceIntent = new Intent(this, MyService.class);
        startService(serviceIntent);

        bindService();
    }

    private void bindService(){
        Intent serviceBindIntent =  new Intent(this, MyService.class);
        bindService(serviceBindIntent, mViewModel.getServiceConnection(), Context.BIND_AUTO_CREATE);
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
        super.onResume();
        Log.d(TAG, "onResume: ~~");
        startService();
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}

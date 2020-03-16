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
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.ExpandableListAdapter;
import android.widget.Toast;

import cz.utb.thesisapp.services.*;

import com.github.clans.fab.FloatingActionMenu;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.HashMap;
import java.util.List;

import cz.utb.thesisapp.services.MyService;
import cz.utb.thesisapp.ui.home.HomeFragment;
import cz.utb.thesisapp.ui.slideshow.SlideshowFragment;
import cz.utb.thesisapp.ui.touch.TouchFragment;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "MainActivity";
    private AppBarConfiguration mAppBarConfiguration;
    private DrawerLayout drawer;
    public MyService mService;
    boolean mBound = false;
    Context context;
    String currentlyShownTag;

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
            }
        });
        //cely layout kde je menu
        drawer = findViewById(R.id.drawer_layout);
        //konkretny panel s menu
        NavigationView navigationView = findViewById(R.id.nav_view);

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setDrawerLayout(drawer)
                .build();
        //content main
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        //pripojenie menu s content main - zobrazeni burger menu
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        //pripojenie click na presun
        NavigationUI.setupWithNavController(navigationView, navController);
//        navigationView.setNavigationItemSelectedListener(this);
        currentlyShownTag = HomeFragment.class.getName();
        Fragment home = getVisibleFragment();

        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter("kryo"));

        ((com.github.clans.fab.FloatingActionButton) findViewById(R.id.fabRefresh)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mService.broadcast.sendMainActivity("refresh", true);
                ((FloatingActionMenu)findViewById(R.id.floatingActionMenu)).close(true);
            }
        });
        ((com.github.clans.fab.FloatingActionButton) findViewById(R.id.fabEdit)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mService.broadcast.sendMainActivity("edit", true);
                ((FloatingActionMenu)findViewById(R.id.floatingActionMenu)).close(true);
            }
        });
        Log.d(TAG, "onCreate: ~~");
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        Log.d(TAG, "onNavigationItemSelected: ~~" + menuItem);
     /*
        Fragment newFragment;
        android.app.FragmentTransaction transaction = getFragmentManager().beginTransaction();
        switch (position) {
            case 0:
                newFragment = new HomeFragment();
                transaction.replace(R.id.nav_host_fragment, newFragment);
                transaction.addToBackStack(null);
                transaction.commit();
                break;

            case 1:
                newFragment = new TouchFragment();
                transaction.replace(R.id.nav_host_fragment, newFragment);
                transaction.addToBackStack(null);
                transaction.commit();
                break;
        }
        //DrawerList.setItemChecked(position, true);
        setTitle(ListTitles[position]);
        DrawerLayout.closeDrawer(DrawerList);
      */

     /*
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        Fragment fragment = null;
        for (Fragment currentFragment : fragments) {
            switch (menuItem.getItemId()) {
                case R.id.nav_home:
                    Log.d(TAG, "onNavigationItemSelected: ~~  case R.id.nav_home:" + menuItem);
                    if (!(currentFragment instanceof HomeFragment)) {
                        Log.d(TAG, "onNavigationItemSelected: ~~  if (!(currentFragment instanceof HomeFragment))" + menuItem);
                        drawer.closeDrawer(GravityCompat.START);
                    }
                    break;
            }
        }

    */
        //        if (menuItem.isChecked()) {
        //            return false;
        //        }

        List<Fragment> fragments = getSupportFragmentManager().getFragments();
//        for(Fragment fragment:fragments){
//           String tag =  fragment.getTag();
//        }
        int id = menuItem.getItemId();
        menuItem.setChecked(true);
        setTitle(menuItem.getTitle());

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

//        Fragment currentlyShown =  fragmentManager.findFragmentByTag(currentlyShownTag);
        Fragment currentlyShown = fragmentManager.findFragmentById(menuItem.getItemId());
        Fragment visibleFragment = getVisibleFragment();
        String a = visibleFragment.getTag();
        Fragment f = getSupportFragmentManager().findFragmentById(R.id.nav_home);
        Fragment dest;
        switch (id) {
            case R.id.nav_home:
                dest = fragmentManager.findFragmentByTag(HomeFragment.class.getName());
                if (dest == null) {
                    dest = new HomeFragment();
                    currentlyShownTag = HomeFragment.class.getName();
                    transaction.add(R.id.nav_host_fragment, dest, HomeFragment.class.getName());
                }
                break;
            case R.id.nav_gallery:
                dest = fragmentManager.findFragmentByTag(TouchFragment.class.getName());
                if (dest == null) {
                    dest = new TouchFragment();
                    currentlyShownTag = TouchFragment.class.getName();
                    transaction.add(R.id.nav_host_fragment, dest, TouchFragment.class.getName());
                }
                break;
            default:
                dest = fragmentManager.findFragmentByTag(HomeFragment.class.getName());
                break;
        }

        if (currentlyShown != null) {
            transaction.hide(currentlyShown);
        }


//        transaction.addToBackStack(null);
        transaction.show(dest);
        transaction.commit();
        drawer.closeDrawer(GravityCompat.START);

        return true;

    }

    public Fragment getVisibleFragment() {
        FragmentManager fragmentManager = MainActivity.this.getSupportFragmentManager();
        List<Fragment> fragments = fragmentManager.getFragments();
        if (fragments != null) {
            for (Fragment fragment : fragments) {
                if (fragment != null && fragment.isVisible())
                    return fragment;
            }
        }
        return null;
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

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive: ~~start");
            if (intent.hasExtra("acceptPair")) {
                HashMap<String, String> hashMap = (HashMap<String, String>) intent.getSerializableExtra("acceptPair");
                showAlertDialog("Do you accept sync with: \n \n" + hashMap.values().toArray()[0].toString(),
                        hashMap.keySet().toArray()[0].toString(), "kryo");
            }
            if (intent.hasExtra("userInfo")) {
                Log.d(TAG, "onReceive: ~~in if" + intent.getExtras().getString("userInfo"));

                Toast.makeText(getApplicationContext(), intent.getExtras().getString("userInfo"), Toast.LENGTH_SHORT).show();
            }

        }
    };

    public void sendTouchStart(float x, float y) {
        if (ismBound()) {
            if (mService.kryoClient.isClientsConnected()) {
                mService.kryoClient.sendTouchStart(x, y);
            }
        }
    }

    public void sendTouchMove(float x, float y) {
        if (ismBound()) {
            if (mService.kryoClient.isClientsConnected()) {
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

    @Override
    protected void onStart() {
        super.onStart();
        startService();
        Log.d(TAG, "onStart: ~~");
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
        Log.d(TAG, "onStop: ~~");
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

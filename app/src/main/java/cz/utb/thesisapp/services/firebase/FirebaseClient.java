package cz.utb.thesisapp.services.firebase;

import android.util.Log;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.MutableData;
import com.firebase.client.Transaction;
import com.firebase.client.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import cz.utb.thesisapp.GlobalValues;
import cz.utb.thesisapp.services.Broadcast;
import cz.utb.thesisapp.services.MyService;

public class FirebaseClient {
    SimpleDateFormat df = new SimpleDateFormat(GlobalValues.DATE_FORMAT);
    private static final String TAG = "FirebaseClient";
    MyService myService;
    Broadcast broadcast;
    Firebase fbRemoteListener;
    Firebase fbSenderThisListener;
    boolean remoteListener = false;
    String token;
    String name;

    ValueEventListener listener;


    public FirebaseClient(MyService myService, Broadcast broadcast) {
        this.myService = myService;
        this.broadcast = broadcast;
    }

    public void startRemoteListener() {
        createListener();
        fbRemoteListener = new Firebase("https://thesis-app-efcd9.firebaseio.com/clients/touch");
        fbRemoteListener.addValueEventListener(listener);
    }

    public void createListener() {
        listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: ~~" + dataSnapshot.getKey());
                Log.d(TAG, "onDataChange: ~~" + dataSnapshot.getValue());
                if(remoteListener){
                    try {
                        Map<String, String> map = dataSnapshot.getValue(Map.class);
                        myService.saveToLocalDatabase(df.parse(map.get("clientCreated")),
                                new Date(System.currentTimeMillis()),
                                new Date(System.currentTimeMillis()),
                                Float.parseFloat(map.get("x")),
                                Float.parseFloat(map.get("y")),
                                map.get("touchType"));
                    } catch (Exception e) {
                        Log.d(TAG, "onDataChange: ~~" + e);
                    }
                }


            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        };
    }

    public void stop() {
        fbRemoteListener = null;
        fbSenderThisListener = null;
    }

    public void start(boolean remoteListener, String token, String name) {
        this.name = name;
        start(remoteListener, token);
    }

    public void start(boolean remoteListener, String token) {
        this.token = token;
        start(remoteListener);
    }

    public void start(boolean remoteListener) {
        Log.d(TAG, "start: ~~"+remoteListener);
        this.remoteListener = remoteListener;
        if (!remoteListener) {
            try {
                fbRemoteListener.removeEventListener(listener);
            } catch (Exception e) {
                Log.d(TAG, "start: ~~" + e);
            }
        } else {
            startRemoteListener();
        }
    }

    public void sendTouch(float x, float y, String touchType, Date created) {

        fbSenderThisListener = new Firebase("https://thesis-app-efcd9.firebaseio.com/clients/touch");
        Map<String, Object> values = new HashMap<>();
        values.put("x", String.valueOf(x));
        values.put("y", String.valueOf(y));
        values.put("touchType", touchType);
        values.put("clientCreated", df.format(created));
        fbSenderThisListener.updateChildren(values);
        if (!remoteListener) {
            fbSenderThisListener.runTransaction(new Transaction.Handler() {
                @Override
                public Transaction.Result doTransaction(MutableData mutableData) {
                    Log.d(TAG, "doTransaction: !~~" + mutableData);
                    // Return passed in data
                    return Transaction.success(mutableData);
                }

                @Override
                public void onComplete(FirebaseError firebaseError, boolean success, DataSnapshot dataSnapshot) {
                    Log.d(TAG, "onComplete: !~~" + dataSnapshot);
                    if (firebaseError != null || !success || dataSnapshot == null) {
                        System.out.println("Failed to get DataSnapshot");
                    } else {
                        try {
                            Map<String, String> map = dataSnapshot.getValue(Map.class);
                            myService.saveToLocalDatabase(df.parse(map.get("clientCreated")),
                                    new Date(System.currentTimeMillis()),
                                    new Date(System.currentTimeMillis()),
                                    Float.parseFloat(map.get("x")),
                                    Float.parseFloat(map.get("y")),
                                    map.get("touchType"));
                        } catch (Exception e) {
                            Log.d(TAG, "onDataChange: ~~" + e);
                        }
                        System.out.println("Successfully get DataSnapshot");
                        //handle data here
                    }
                }
            });
        }

    }


}

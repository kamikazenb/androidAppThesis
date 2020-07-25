package cz.utb.thesisapp.services.firebase;

//import android.util.Log;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.MutableData;
import com.firebase.client.Transaction;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import cz.utb.thesisapp.GlobalValues;
import cz.utb.thesisapp.services.Broadcast;
import cz.utb.thesisapp.services.MyService;

public class FirebaseClient {
    private static final String TAG = "FirebaseClient";
    MyService myService;
    Broadcast broadcast;
    Firebase fbRemoteListener;
    Firebase fbSenderThisListener;
    boolean remoteListener = false;
    String token;
    String name;
    int id = 0;



    ChildEventListener listener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
////            Log.i(TAG, "onChildAdded: ~~" + dataSnapshot);
            SimpleDateFormat df = new SimpleDateFormat(GlobalValues.DATE_FORMAT);
           useNewData(dataSnapshot, df);
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
//            Log.i(TAG, "onChildChanged: ~~" + dataSnapshot);
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
//            Log.i(TAG, "onChildRemoved: ~~" + dataSnapshot);
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
//            Log.i(TAG, "onChildMoved: ~~" + dataSnapshot);
        }

        @Override
        public void onCancelled(FirebaseError firebaseError) {
//            Log.i(TAG, "onCancelled: ~~" + firebaseError);
        }
    };


    public FirebaseClient(MyService myService, Broadcast broadcast) {
        this.myService = myService;
        this.broadcast = broadcast;
    }


    public void stop() {
        fbRemoteListener = null;
        fbSenderThisListener = null;
    }


    public void start(boolean remoteListener, String token, String name) {
//        Log.i(TAG, "start: removed~~");
        this.name = name;
        fbRemoteListener = new Firebase("https://thesis-app-efcd9.firebaseio.com/clients/" + "token" + "/touch");
        fbRemoteListener.child("name").setValue(name);
        start(remoteListener, token);
    }

    public void start(boolean remoteListener, String token) {
        this.token = token;
        start(remoteListener);
    }

    public void start(boolean remoteListener) {
//        Log.i(TAG, "start: ~~" + remoteListener);
        this.remoteListener = remoteListener;
        if (!remoteListener) {
            try {
                fbRemoteListener.removeEventListener(listener);
            } catch (Exception e) {
//                Log.i(TAG, "start: ~~" + e);
            }
        } else {
            fbRemoteListener.addChildEventListener(listener);
        }

    }

    public void useNewData(DataSnapshot dataSnapshot, SimpleDateFormat dateFormat) {
        try {

            Map<String, String> map = dataSnapshot.getValue(Map.class);
////            Log.i(TAG, "onComplete: ~~                                " + dataSnapshot);
////                                    Log.i()(TAG, "onComplete: ~~                                " + df.format(new Date(System.currentTimeMillis())));
            myService.saveToLocalDatabase(dateFormat.parse(map.get("clientCreated")),
                    new Date(System.currentTimeMillis()),
                    Float.parseFloat(map.get("x")),
                    Float.parseFloat(map.get("y")),
                    map.get("touchType"));
        } catch (Exception e) {
//            Log.i(TAG, "onDataChange: ~~" + e);
        }
    }

    public void sendTouch(float x, float y, String touchType) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                SimpleDateFormat df = new SimpleDateFormat(GlobalValues.DATE_FORMAT);
                Map<String, Object> values = new HashMap<>();
                values.put("x", String.valueOf(x));
                values.put("y", String.valueOf(y));
                values.put("touchType", touchType);
                values.put("clientCreated", df.format(new Date(System.currentTimeMillis())));
                fbSenderThisListener = new Firebase("https://thesis-app-efcd9.firebaseio.com/clients/" + "token" + "/touch");
                Firebase fbChildRef = fbSenderThisListener.child(String.valueOf(++id));
//                Log.i(TAG, "sendTouch: ~~" + id + " " + values.get("clientCreated"));
                fbChildRef.updateChildren(values);
                if (!remoteListener) {
                    fbChildRef.runTransaction(new Transaction.Handler() {
                        @Override
                        public Transaction.Result doTransaction(MutableData mutableData) {
//                            Log.i(TAG, "doTransaction: !~~" + mutableData);
                            // Return passed in data
                            return Transaction.success(mutableData);
                        }

                        @Override
                        public void onComplete(FirebaseError firebaseError, boolean success, DataSnapshot dataSnapshot) {
////                            Log.i()(TAG, "onComplete: !~~                                         " + dataSnapshot);
                            if (firebaseError != null || !success || dataSnapshot == null) {
                                System.out.println("Failed to get DataSnapshot");
                            } else {
                                useNewData(dataSnapshot, df);
//                                System.out.println("Successfully get DataSnapshot");
                                //handle data here
                            }
                        }
                    });
                }
            }
        });
        thread.start();


    }


}

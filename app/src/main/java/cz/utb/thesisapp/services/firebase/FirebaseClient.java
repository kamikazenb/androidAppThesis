package cz.utb.thesisapp.services.firebase;

//import android.util.Log;

import android.util.Log;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.MutableData;
import com.firebase.client.Transaction;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import cz.utb.thesisapp.GlobalValues;
import cz.utb.thesisapp.services.Broadcast;
import cz.utb.thesisapp.services.MyService;
import cz.utb.thesisapp.services.kryonet.Network;

public class FirebaseClient {
    private static final String TAG = "FirebaseClient";
    MyService myService;
    Broadcast broadcast;
    Firebase fbRemoteListener;
    boolean remoteListener = false;
    String token;
    String name;
    int id = 0;


    ChildEventListener listener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//////            Log.i(TAG, "onChildAdded: ~~" + dataSnapshot);

            SimpleDateFormat df = new SimpleDateFormat(GlobalValues.DATE_FORMAT);
            useNewData(dataSnapshot, df);


        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
////            Log.i(TAG, "onChildChanged: ~~" + dataSnapshot);
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
////            Log.i(TAG, "onChildRemoved: ~~" + dataSnapshot);
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
////            Log.i(TAG, "onChildMoved: ~~" + dataSnapshot);
        }

        @Override
        public void onCancelled(FirebaseError firebaseError) {
////            Log.i(TAG, "onCancelled: ~~" + firebaseError);
        }
    };


    public FirebaseClient(MyService myService, Broadcast broadcast) {
        this.myService = myService;
        this.broadcast = broadcast;
    }


    public void stop() {
        fbRemoteListener.removeEventListener(listener);
        fbRemoteListener = null;
        myService.firebase = false;
    }


    public void start(boolean remoteListener, String token, String name) {
////        Log.i(TAG, "start: removed~~");
        this.name = name;
        Firebase bs = new Firebase("https://thesis-app-efcd9.firebaseio.com/clients/" + "token");
        bs.removeValue();
        fbRemoteListener = new Firebase("https://thesis-app-efcd9.firebaseio.com/clients/" + "token" + "/touch");

        bs.child("name").setValue(name);
        start(remoteListener, token);
    }

    public void start(boolean remoteListener, String token) {
        this.token = token;
        start(remoteListener);
    }

    public void start(boolean remoteListener) {
////        Log.i(TAG, "start: ~~" + remoteListener);
        id = 0;
        this.remoteListener = remoteListener;
        if (!remoteListener) {
            try {
                fbRemoteListener.removeEventListener(listener);
            } catch (Exception e) {
////                Log.i(TAG, "start: ~~" + e);
            }
        } else {
            fbRemoteListener.addChildEventListener(listener);
        }

    }

    public void useNewData(DataSnapshot dataSnapshot, SimpleDateFormat dateFormat) {
        try {
            for (DataSnapshot itemSnapshot : dataSnapshot.getChildren()) {
                Map<String, String> overall = itemSnapshot.getValue(Map.class);
                myService.saveToRemoteDatabase(dateFormat.parse(overall.get("clientCreated")),
                        new Date(System.currentTimeMillis()),
                        Float.parseFloat(overall.get("x")),
                        Float.parseFloat(overall.get("y")),
                        overall.get("touchType"));

            }


        } catch (Exception e) {
//            Log.i(TAG, "useNewData: ~~" + e.getCause());
        }
    }

    public void sendTouch(ArrayList<Network.Touch> touches) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                int row = 0;
                Map<String, Object> overall = new HashMap<>();
                for (Network.Touch touch : touches) {
                    Map<String, Object> values = new HashMap<>();
                    values.put("x", String.valueOf(touch.x));
                    values.put("y", String.valueOf(touch.y));
                    values.put("touchType", touch.touchType);
                    values.put("clientCreated",  new SimpleDateFormat(GlobalValues.DATE_FORMAT).format(touch.clientCreated));
                    overall.put(String.valueOf(row++), values);
                }
                Firebase fb = new Firebase("https://thesis-app-efcd9.firebaseio.com/clients/" + "token" + "/touch/" + id++);
                fb.updateChildren(overall);
                if (!remoteListener) {
                    fb.runTransaction(new Transaction.Handler() {
                        @Override
                        public Transaction.Result doTransaction(MutableData mutableData) {
//                            Log.i(TAG, "doTransaction: !~~\n" + mutableData);
                            // Return passed in data
                            return Transaction.success(mutableData);
                        }

                        @Override
                        public void onComplete(FirebaseError firebaseError, boolean success, DataSnapshot dataSnapshot) {
//                            Log.i(TAG, "onComplete: !~~ \n" + dataSnapshot);
                            if (firebaseError != null || !success || dataSnapshot == null) {
                                Log.i(TAG, "onComplete: ~~Failed to get DataSnapshot");
                            } else {
                                useNewData(dataSnapshot,  new SimpleDateFormat(GlobalValues.DATE_FORMAT));
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

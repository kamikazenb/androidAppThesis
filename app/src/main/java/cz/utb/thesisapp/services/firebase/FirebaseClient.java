package cz.utb.thesisapp.services.firebase;

import android.util.Log;

import androidx.annotation.NonNull;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.MutableData;
import com.firebase.client.Transaction;
import com.firebase.client.ValueEventListener;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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
    boolean connected;
    Firebase fbListener;
    Firebase fbSender;
    int counter = 0;
    String token;

    public FirebaseClient(MyService myService, Broadcast broadcast) {
        this.myService = myService;
        this.broadcast = broadcast;
        connected = false;
    }

    public void start(String token, String name) {
        this.token = token;
//        fbSender = new Firebase("https://thesis-app-efcd9.firebaseio.com/clients");
//        Firebase fbChildRef = fbSender.child(token);
//        fbChildRef.child("name").setValue(name);
//        fbChildRef.child("online").setValue("1");
/*
        fbListener = new Firebase("https://thesis-app-efcd9.firebaseio.com/clients/" + token);
        fbChildRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "onChildAdded: ~~" + dataSnapshot);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "onChildAdded: ~~" + dataSnapshot);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onChildAdded: ~~" + dataSnapshot);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "onChildAdded: ~~" + dataSnapshot);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.d(TAG, "onChildAdded: ~~" + firebaseError);
            }
        });*/

//        fbListener = new Firebase("https://thesis-app-efcd9.firebaseio.com/clients/" + token+"/touch");


//        fbListener = new Firebase("https://thesis-app-efcd9.firebaseio.com/clients/touch");


        fbListener = new Firebase("https://thesis-app-efcd9.firebaseio.com/clients/touch");
//        fbListener.runTransaction(new Transaction.Handler() {
//            @Override
//            public Transaction.Result doTransaction(MutableData mutableData) {
//                Log.d(TAG, "doTransaction: !~~");
//                // Return passed in data
//                return Transaction.success(mutableData);
//            }
//
//            @Override
//            public void onComplete(FirebaseError firebaseError, boolean success, DataSnapshot dataSnapshot) {
//                Log.d(TAG, "onComplete: !~~");
//                if (firebaseError != null || !success || dataSnapshot == null) {
//                    System.out.println("Failed to get DataSnapshot");
//                } else {
//                    System.out.println("Successfully get DataSnapshot");
//                    //handle data here
//                }
//            }
//        });
      /* fbListener.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: ~~" + dataSnapshot.getKey());
                Log.d(TAG, "onDataChange: ~~" + dataSnapshot.getValue());
                fbListener.keepSynced(true);
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

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });*/
    }

    public void stop() {
        fbListener = null;
        fbSender = null;
    }

    public void sendTouch(float x, float y, String touchType, Date created) {
//        fbSender = new Firebase("https://thesis-app-efcd9.firebaseio.com/clients/" + token+"/touch");
        fbSender = new Firebase("https://thesis-app-efcd9.firebaseio.com/clients/touch");
        //    Firebase fbChildRef = fbSender.child(String.valueOf(counter++));
        Map<String, Object> values = new HashMap<>();
        values.put("x", String.valueOf(x));
        values.put("y", String.valueOf(y));
        values.put("touchType", touchType);
        values.put("clientCreated", df.format(created));
        fbSender.updateChildren(values);
        /*fbSender.updateChildren(values, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                if (firebaseError != null) {
                    Log.d(TAG, "onComplete: ~~Data could not be saved.");
                } else {
                    Log.d(TAG, "onComplete: ~~Data saved successfull");
                }
            }
        });*/
        fbSender.runTransaction(new Transaction.Handler() {
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
        fbSender.updateChildren(values);
        // Use runTransaction to bypass cached DataSnapshot




    /*    fbChildRef.child("x").setValue(String.valueOf(x));
        fbChildRef.child("y").setValue(String.valueOf(y));
        fbChildRef.child("touchType").setValue(touchType);
        fbChildRef.child("clientCreated").setValue(df.format(created));*/
    }


}

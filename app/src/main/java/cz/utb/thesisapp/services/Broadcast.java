package cz.utb.thesisapp.services;

import android.content.Intent;
import android.os.Bundle;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class Broadcast {

    MyService myService;


    public Broadcast(MyService myService){
       this.myService = myService;
    }

    public void sendBroadcastHashMap(String filter, String name, HashMap<String, String> values) {
        Intent i = new Intent(filter);
        i.putExtra(name, values);
        LocalBroadcastManager.getInstance(myService).sendBroadcast(i);
    }

    public void sendBroadcastArrayList(String filter, String name, ArrayList<String> values) {
        Intent i = new Intent(filter);
        Bundle args = new Bundle();
        args.putSerializable("ARRAYLIST", (Serializable) values);
        i.putExtra(name, args);
        LocalBroadcastManager.getInstance(myService).sendBroadcast(i);
    }

    public void sendBroadcastString(String filter, String name, String value) {
        Intent i = new Intent(filter);
        i.putExtra(name, value);
        LocalBroadcastManager.getInstance(myService).sendBroadcast(i);
    }

}

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

    public void sendServiceHashMap(String filter, String name, HashMap<String, String> values) {
        Intent i = new Intent(filter);
        i.putExtra(name, values);
        LocalBroadcastManager.getInstance(myService).sendBroadcast(i);
    }
    public void sendTouchFloats(String valueName, float x, float y){
        Intent i = new Intent("touch");
        i.putExtra(valueName, true);
        i.putExtra("x", x);
        i.putExtra("y", y);
        LocalBroadcastManager.getInstance(myService).sendBroadcast(i);
    }
    public void sendTouchBoolean(String valueName, boolean data){
        Intent i = new Intent("touch");
        i.putExtra(valueName, true);
        i.putExtra("data", data);
        LocalBroadcastManager.getInstance(myService).sendBroadcast(i);
    }
    public void sendTouchFloat(String valueName, float data){
        Intent i = new Intent("touch");
        i.putExtra(valueName, true);
        i.putExtra("data", data);
        LocalBroadcastManager.getInstance(myService).sendBroadcast(i);
    }

    public void sendServiceArrayList(String filter, String name, ArrayList<String> values) {
        Intent i = new Intent(filter);
        Bundle args = new Bundle();
        args.putSerializable("ARRAYLIST", (Serializable) values);
        i.putExtra(name, args);
        LocalBroadcastManager.getInstance(myService).sendBroadcast(i);
    }

    public void sendServiceString(String filter, String name, String value) {
        Intent i = new Intent(filter);
        i.putExtra(name, value);
        LocalBroadcastManager.getInstance(myService).sendBroadcast(i);
    }
    public void sendMainActivity(String command, boolean value){
        Intent i = new Intent("MainActivity");
        i.putExtra(command, value);
        LocalBroadcastManager.getInstance(myService).sendBroadcast(i);
    }
    public void sendInfoFragmentSpeed(String uploadOrDownload, String speed, int progress){
        Intent i = new Intent("info");
        i.putExtra(uploadOrDownload, speed);
        i.putExtra("progress", progress);
        LocalBroadcastManager.getInstance(myService).sendBroadcast(i);
    }

}

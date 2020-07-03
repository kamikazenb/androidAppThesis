package cz.utb.thesisapp.services;

import android.content.Intent;
import android.os.Bundle;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import static cz.utb.thesisapp.GlobalValues.*;

public class Broadcast {

    MyService myService;


    public Broadcast(MyService myService) {
        this.myService = myService;
    }

    public void sendHashMap(String filter, String extraName, HashMap<String, String> hashMap) {
        Intent i = new Intent(filter);
        i.putExtra(extraName, hashMap);
        LocalBroadcastManager.getInstance(myService).sendBroadcast(i);
    }

    public void sendTouchFloats(String extraTouchType, float extraX, float extraY) {
        Intent i = new Intent(FILTER_TOUCH);
        i.putExtra(extraTouchType, true);
        i.putExtra(EXTRA_X, extraX);
        i.putExtra(EXTRA_Y, extraY);
        LocalBroadcastManager.getInstance(myService).sendBroadcast(i);
    }

    public void sendTouchBoolean(String valueName, boolean data) {
        Intent i = new Intent(FILTER_TOUCH);
        i.putExtra(valueName, true);
        i.putExtra("data", data);
        LocalBroadcastManager.getInstance(myService).sendBroadcast(i);
    }

    public void sendTouchFloat(String valueName, float data) {
        Intent i = new Intent(FILTER_TOUCH);
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

    public void sendString(String filter, String extraName, String string) {
        Intent i = new Intent(filter);
        i.putExtra(extraName, string);
        LocalBroadcastManager.getInstance(myService).sendBroadcast(i);
    }

    public void sendMainActivity(String command, boolean value) {
        Intent i = new Intent(FILTER_MAIN_ACTIVITY);
        i.putExtra(command, value);
        LocalBroadcastManager.getInstance(myService).sendBroadcast(i);
        int a = 5;
    }

    /**
     * send to broadcast object of types <br>
     * String <br>Integer <br> Boolean <br> Float
     * @param filter    filter name
     * @param extraName extra name
     * @param value     CANNOT USE PRIMITIVE TYPES. Need values cast to objects
     */
    public <T> void sendValue(String filter, String extraName, T value) {
        Intent i = new Intent(filter);
        if (value instanceof String) {
            i.putExtra(extraName, (String) value);
        }
        if (value instanceof Integer) {
            i.putExtra(extraName, (Integer) value);
        }
        if (value instanceof Float) {
            i.putExtra(extraName, (Float) value);
        }
        if (value instanceof Boolean) {
            i.putExtra(extraName, (Boolean) value);
        }
        LocalBroadcastManager.getInstance(myService).sendBroadcast(i);
    }

    public void sendBoolean(String filter, String extraName, boolean bool) {
        Intent i = new Intent(filter);
        i.putExtra(extraName, bool);
        LocalBroadcastManager.getInstance(myService).sendBroadcast(i);
    }


    public void sendInfoFragmentSpeed(String uploadOrDownload, float speed, int progress) {
        Intent i = new Intent(FILTER_INFO);
        i.putExtra(uploadOrDownload, speed);
        i.putExtra(EXTRA_SPEED_PROGRESS, progress);
        LocalBroadcastManager.getInstance(myService).sendBroadcast(i);
    }

}

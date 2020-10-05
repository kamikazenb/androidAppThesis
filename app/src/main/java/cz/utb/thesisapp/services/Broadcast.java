package cz.utb.thesisapp.services;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import static cz.utb.thesisapp.GlobalValues.*;

public class Broadcast {
    private static final String TAG = "Broadcast";
    MyService myService;

    public Broadcast(MyService myService) {
        this.myService = myService;
    }

    public void sendFloats(String filter, String extraTouchType, float extraX, float extraY) {
        Intent i = new Intent(filter);
        i.putExtra(extraTouchType, true);
        i.putExtra(EXTRA_X, extraX);
        i.putExtra(EXTRA_Y, extraY);
        LocalBroadcastManager.getInstance(myService).sendBroadcast(i);
    }

    public void sendServiceArrayList(String filter, String name, ArrayList<String> values) {
        Intent i = new Intent(filter);
        Bundle args = new Bundle();
        args.putSerializable("ARRAYLIST", (Serializable) values);
        i.putExtra(name, args);
        LocalBroadcastManager.getInstance(myService).sendBroadcast(i);
    }

    /**
     * send to broadcast object of type: <br>
     * String <br>Integer <br> Boolean <br> Float <br> HashMap K-String V-String
     *
     * @param value throws Log.i ~~ error if incomparable type
     */
    public <T> void sendValue(String filter, String extraName, T value) {
        Intent i = new Intent(filter);
        try {
            if (value instanceof String) {
                i.putExtra(extraName, (String) value);
            } else if (value instanceof Integer) {
                i.putExtra(extraName, (Integer) value);
            } else if (value instanceof Float) {
                i.putExtra(extraName, (Float) value);
            } else if (value instanceof Boolean) {
                i.putExtra(extraName, (Boolean) value);
            } else if (value instanceof HashMap) {
                i.putExtra(extraName, (HashMap<String, String>) value);
            }
            LocalBroadcastManager.getInstance(myService).sendBroadcast(i);
        } catch (ClassCastException e) {
            Log.i(TAG, "sendValue: ~~" + e);
        }

    }

    public void sendInfoFragmentSpeed(String uploadOrDownload, float speed, int progress) {
        Intent i = new Intent(FILTER_INFO);
        i.putExtra(uploadOrDownload, speed);
        i.putExtra(EXTRA_SPEED_PROGRESS, progress);
        LocalBroadcastManager.getInstance(myService).sendBroadcast(i);
    }

}

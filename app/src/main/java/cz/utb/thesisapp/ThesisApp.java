package cz.utb.thesisapp;

import android.app.Application;

import com.firebase.client.Firebase;

public class ThesisApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
    }
}

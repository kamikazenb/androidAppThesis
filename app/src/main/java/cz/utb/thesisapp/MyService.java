package cz.utb.thesisapp;

import android.app.Service;

import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import cz.utb.thesisapp.serialization.Network;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import java.io.IOException;
import java.util.Random;

public class MyService extends Service {

    private static final String TAG = "MyService";
    // Binder given to clients
    private final IBinder mBinder = new MyBinder();
    // Random number generator
    private final Random mGenerator = new Random();

    Client client;

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class MyBinder extends Binder {

        MyService getService() {
            // Return this instance of LocalService so clients can call public methods
            return MyService.this;
        }
    }

    public void newClient(final String ip) {
        Log.d(TAG, "newClient: ~~creating");
        client = new Client();
        client.start();
        Network.register(client);
        client.addListener(new Listener.ThreadedListener(new Listener() {
            public void connected(Connection connection) {
                Network.Register register = new Network.Register();
                register.name = "testHost1";
                client.sendTCP(register);
            }

            public void received(Connection connection, Object object) {
                if (object instanceof Network.Info) {
                    Network.Info info = (Network.Info) object;
                    Log.d(TAG, "received: ~~Network.info" + info.message);
                }
            }
        }));
        Thread t = new Thread() {
            public void run() {
                try {
                    //195.178.94.66
                    client.connect(5000, ip, Network.port);
                    sendBroadcast("kryo", "userInfo", "connection to kryonet successful");
                    sendBroadcast("kryo", "command", "setChecked");
                } catch (IOException ex) {
                    ex.printStackTrace();
                    sendBroadcast("kryo", "userInfo", "There is a connection error");
                    sendBroadcast("kryo", "command", "setUnchecked");
                }
            }
        };
        t.start();
    }

    public void clientStop(){
        client.stop();
    }

    private void sendBroadcast(String filter, String name, String value){
        Intent i = new Intent(filter);
        i.putExtra(name, value);
        LocalBroadcastManager.getInstance(MyService.this).sendBroadcast(i);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Log.d(TAG, "onTaskRemoved: called.");
        stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: called.");
    }
}
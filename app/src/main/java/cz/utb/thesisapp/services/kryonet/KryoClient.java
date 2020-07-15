package cz.utb.thesisapp.services.kryonet;

import android.util.Log;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

import cz.utb.thesisapp.services.Broadcast;
import cz.utb.thesisapp.services.MyService;
import cz.utb.thesisapp.services.TokenGenerator;

import static cz.utb.thesisapp.GlobalValues.*;

public class KryoClient {
    private MyService service;
    private boolean clientsConnected = false;
    private static final String TAG = "KryoClient";
    private TokenGenerator tokenGenerator = new TokenGenerator();
    private Broadcast broadcast;
    //    sender
    ClientClassExtension client;

    HashMap<String, Network.Register> usersHashmap = new HashMap<>();

    public KryoClient(Broadcast broadcast, MyService service) {
        this.broadcast = broadcast;
        this.service = service;
    }

    private void addClients(final String ip, final ClientClassExtension client, final String userName) {
        client.start();
        Network.register(client);
        client.klientName = "AndoidClient" + String.valueOf(android.os.Process.myPid());
        client.systemName = client.klientName;
        client.token = tokenGenerator.generateRandom(20);
        client.addListener(new Listener.ThreadedListener(new Listener() {
            public void connected(Connection connection) {
                Network.Register register = new Network.Register();
                register.userName = userName;
                register.token = client.token;
                client.sendTCP(register);
            }

            public void disconnected(Connection connection) {
                Log.d(TAG, "disconnected: ~~mainClient ");
                setClientsConnected(false);
            }

            public void received(Connection connection, Object object) {

                if (object instanceof Network.RegisteredUsers) {
                    Log.d(TAG, "received: ~~if (object instanceof Network.RegisteredUsers) {");
                    NetworkRegisteredUsers(connection, object, client);
                }
                if (object instanceof Network.Touch) {
                    broadcast.sendFloats(FILTER_TOUCH, ((Network.Touch) object).touchType,
                            ((Network.Touch) object).x, ((Network.Touch) object).y);
                    service.saveToLocalDatabase(((Network.Touch) object).clientCreated,
                            ((Network.Touch) object).serverReceived,
                            new Date(System.currentTimeMillis()),
                            ((Network.Touch) object).x,
                            ((Network.Touch) object).y,
                            ((Network.Touch) object).touchType);
                }
                if (object instanceof Network.UseDatabase) {
                    Log.d(TAG, "received: ~~useDatabase");
                    broadcast.sendValue(FILTER_KRYO, EXTRA_KROYSERVER_USE_DATABASE, ((Network.UseDatabase) object).useDatabase);
                }

            }
        }));
        Thread t = new Thread() {
            public void run() {
                try {
                    //195.178.94.66
                    client.connect(10000, ip, Network.port);
                    broadcast.sendValue(FILTER_KRYO, EXTRA_USER_INFO, "connections to kryonet successful");
                    broadcast.sendValue(FILTER_KRYO, EXTRA_COMMAND, EXTRA_COMMAND_SET_CHECKED);
                    clientsConnected = true;
                } catch (IOException ex) {
                    ex.printStackTrace();
                    setClientsConnected(false);
                }
            }
        };
        t.start();
    }

    private void NetworkRegisteredUsers(Connection connection, Object object, ClientClassExtension client) {
        Network.RegisteredUsers registeredUsers = (Network.RegisteredUsers) object;
        usersHashmap.clear();
        for (int i = 0; i < registeredUsers.users.size(); i++) {
            Network.Register reg = registeredUsers.users.get(i);
            usersHashmap.put(reg.token, reg);
        }
        //      userName   token
        HashMap<String, String> usersMap = new HashMap<>();

        for (Object value : usersHashmap.values()) {
            Network.Register a = (Network.Register) value;
            usersMap.put(a.token, a.userName);
        }
        broadcast.sendValue(FILTER_KRYO, EXTRA_USERS, usersMap);
//                broadcast.sendHashMap(FILTER_KRYO, EXTRA_USERS, usersMap);
    }

    public void sendUseDatabase(boolean bool) {
        Network.UseDatabase useDatabase = new Network.UseDatabase();
        useDatabase.useDatabase = bool;
        Thread t = new Thread() {
            public void run() {
                client.sendTCP(useDatabase);
            }
        };
        t.start();
    }

    public void sendTouch(float x, float y, String touchType) {
        Network.Touch touch = new Network.Touch();
        touch.x = x;
        touch.y = y;
        touch.clientCreated = new Date(System.currentTimeMillis());
        touch.touchType = touchType;

        final Network.Touch sendTouch = touch;
        Thread t = new Thread() {
            public void run() {
                client.sendTCP(sendTouch);
            }
        };
        t.start();
    }

    public void unFollow() {
        client.followedToken = "";
        Network.FollowClient followClient = new Network.FollowClient();
        followClient.follow = false;
        followClient.token = "";
        final Network.FollowClient sendFollow = followClient;
        Thread t = new Thread() {
            public void run() {
                client.sendTCP(sendFollow);
            }
        };
        t.start();
    }

    public void requestFollow(String followedToken) {
        client.followedToken = followedToken;
        Network.FollowClient followClient = new Network.FollowClient();
        followClient.follow = true;
        followClient.token = followedToken;
        final Network.FollowClient sendFollow = followClient;
        Thread t = new Thread() {
            public void run() {
                client.sendTCP(sendFollow);
            }
        };
        t.start();
    }

    public void newClient(String ip, String userName) {
        client = new ClientClassExtension();
        addClients(ip, client, userName);
    }

    public void stopClient() {
        try {
            client.stop();
        } catch (Exception e) {
        }
        broadcast.sendValue(FILTER_KRYO, EXTRA_CONNECTION_CLOSED, "Kryonet: connection NA");
    }

    public boolean isClientConnected() {
        return clientsConnected;
    }

    public void setClientsConnected(boolean clientsConnected) {
        this.clientsConnected = clientsConnected;
        if (!clientsConnected) {
            broadcast.sendValue(FILTER_KRYO, EXTRA_CONNECTION_CLOSED, "Kryonet: connection NA");
            broadcast.sendValue(FILTER_KRYO, EXTRA_COMMAND, EXTRA_COMMAND_SET_UNCHECKED);
            stopClient();
        }
    }
}

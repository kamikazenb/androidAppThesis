package cz.utb.thesisapp.services.kryonet;

import android.app.Service;
import android.util.Log;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

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
    ClientClassExtension clientSenderReceiver;
    //    receiver
    ClientClassExtension clientReceiver;
    HashMap<String, Network.Register> usersHashmap = new HashMap<>();

    public KryoClient(Broadcast broadcast, MyService service) {
        this.broadcast = broadcast;
        this.service = service;
    }

    private void addClients(final String ip, final ClientClassExtension client, final String userName, final boolean mainClient) {
        client.start();
        Network.register(client);
        client.klientName = "AndoidClient" + String.valueOf(android.os.Process.myPid());
        client.systemName = client.klientName;
        client.token = tokenGenerator.generateRandom(20);
        client.addListener(new Listener.ThreadedListener(new Listener() {
            public void connected(Connection connection) {
                Network.Register register = new Network.Register();
                client.mainClient = mainClient;
                register.mainClient = client.mainClient;
                register.userName = userName;
                register.systemName = userName.trim() + String.valueOf(android.os.Process.myPid());
                register.token = client.token;
                client.sendTCP(register);
            }

            public void disconnected(Connection connection) {
                Log.d(TAG, "disconnected: ~~mainClient " + client.mainClient);
                setClientsConnected(false);
            }

            public void received(Connection connection, Object object) {

                if (object instanceof Network.Info) {
                    Log.d(TAG, "received: ~~  if (object instanceof Network.Pair) {");
                    NetworkInfo(connection, object, client);
                }
                if (object instanceof Network.Pair) {
                    Log.d(TAG, "received: ~~  if (object instanceof Network.Pair) {");
                    NetworkPair(connection, object, client);
                }
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
                if (object instanceof Network.ScreenSize) {
                    broadcast.sendFloats(FILTER_TOUCH, "ScreenSize",
                            ((Network.ScreenSize) object).x, ((Network.ScreenSize) object).y);
                }
                if (object instanceof Network.CleanCanvas) {
                    broadcast.sendValue(FILTER_TOUCH, "CleanCanvas", ((Network.CleanCanvas) object).cleanCanvas);
                }

                if (object instanceof Network.TouchTolerance) {
                    broadcast.sendValue(FILTER_TOUCH, "TouchTolerance", ((Network.TouchTolerance) object).TOUCH_TOLERANCE);
                }
                if (object instanceof Network.UseDatabase) {
                    Log.d(TAG, "received: ~~useDatabase");
                    broadcast.sendValue(FILTER_KRYO, EXTRA_KROYSERVER_USE_DATABASE, ((Network.UseDatabase) object).useDatabase);
                }
                if (object instanceof Network.Speed) {
                    Log.d(TAG, "received: ~~   if (object instanceof Network.Speed) {");
                    if (client.mainClient) {
                        float download = ((Network.Speed) object).download;
                        float upload = ((Network.Speed) object).upload;
                    }
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

        Log.d(TAG, "received: ~~RegisteredUsers " + client.token);
        if (client.mainClient) {
            Network.RegisteredUsers registeredUsers = (Network.RegisteredUsers) object;
            usersHashmap.clear();
            for (int i = 0; i < registeredUsers.users.size(); i++) {
                if (registeredUsers.users.get(i).mainClient) {
                    Network.Register reg = registeredUsers.users.get(i);
                    usersHashmap.put(reg.token, reg);
                }
                if (((Network.RegisteredUsers) object).users.get(i).token.equals(clientReceiver.token)) {
                    Network.Register reg = registeredUsers.users.get(i);
                    usersHashmap.put(reg.token, reg);
                }
            }
            //      userName   token
            HashMap<String, String> usersMap = new HashMap<>();

            for (Object value : usersHashmap.values()) {
                Network.Register a = (Network.Register) value;
                if (a.token.equals(clientSenderReceiver.token)) {
                    continue;
                }
                usersMap.put(a.token, a.userName);
            }
            if (registeredUsers.users.size() > 0) {
                Log.d(TAG, "NetworkRegisteredUsers: ~~1" + usersMap.toString());
                broadcast.sendValue(FILTER_KRYO, EXTRA_USERS, usersMap);
//                broadcast.sendHashMap(FILTER_KRYO, EXTRA_USERS, usersMap);
            }
        }

    }

    private void NetworkInfo(Connection connection, Object object, ClientClassExtension client) {
        Network.Info info = (Network.Info) object;
        Log.d(TAG, "received: ~~inf " + client.token);
    }

    private void NetworkPair(Connection connection, Object object, ClientClassExtension client) {
        Log.d(TAG, "received: ~~Pair " + client.token);
        Network.Pair pair = (Network.Pair) object;
        if (pair.connectionAlive) {
            if (pair.seekerAccepted && !pair.respondentAccepted) {
                String userName = Objects.requireNonNull(usersHashmap.get(pair.tokenPairSeeker)).userName;
                HashMap<String, String> send = new HashMap<>();
                if (pair.tokenPairSeeker.equals(clientSenderReceiver.token)) {
                    userName = "This app";
                }
                send.put(pair.tokenPairSeeker, userName);
//            broadcast.sendHashMap(FILTER_KRYO, EXTRA_ACCEPT_PAIR_REQUEST, send);
                broadcast.sendValue(FILTER_KRYO, EXTRA_ACCEPT_PAIR_REQUEST, send);
            }
            if (pair.seekerAccepted && pair.respondentAccepted) {
                if (pair.tokenPairRespondent.equals(client.token)) {
                    client.pairedToken = pair.tokenPairSeeker;
                } else {
                    client.pairedToken = pair.tokenPairRespondent;
                }
                try {
                    broadcast.sendValue(FILTER_KRYO, EXTRA_PAIRED, (String) usersHashmap.get(client.pairedToken).userName);
                } catch (Exception e) {
                }
            }
        } else {
            client.pairedToken = null;
            broadcast.sendValue(FILTER_KRYO, EXTRA_UNPAIRED, "");
        }
    }

    public void sendUseDatabase(boolean bool) {
        Network.UseDatabase useDatabase = new Network.UseDatabase();
        useDatabase.useDatabase = bool;
        Thread t = new Thread() {
            public void run() {
                clientSenderReceiver.sendTCP(useDatabase);
            }
        };
        t.start();
    }

    public void sendPairAcceptationResult(String seekerToken, boolean result) {
        Network.Pair pair = new Network.Pair();
        if (seekerToken.equals(clientSenderReceiver.token)) {
            pair.tokenPairRespondent = clientReceiver.token;
        } else {
            pair.tokenPairRespondent = clientSenderReceiver.token;
        }
        pair.tokenPairSeeker = seekerToken;
        pair.seekerAccepted = true;
        pair.respondentAccepted = result;
        pair.connectionAlive = result;
        final Network.Pair sendPair = pair;
        Thread t = new Thread() {
            public void run() {
                clientSenderReceiver.sendTCP(sendPair);
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
                clientSenderReceiver.sendTCP(sendTouch);
            }
        };
        t.start();
    }

    public void sendRequest(boolean speed, boolean registeredUsers) {
        Network.Request request = new Network.Request();
        request.internetSpeed = speed;
        request.registredUsers = registeredUsers;
        final Network.Request sendRequest = request;
        Thread t = new Thread() {
            public void run() {
                clientSenderReceiver.sendTCP(sendRequest);
            }
        };
        t.start();
    }

    public void unPair() {
        Network.Pair pair = new Network.Pair();
        pair.tokenPairRespondent = clientSenderReceiver.token;
        pair.tokenPairSeeker = clientSenderReceiver.pairedToken;
        pair.seekerAccepted = false;
        pair.connectionAlive = false;
        final Network.Pair sendPair = pair;
        Thread t = new Thread() {
            public void run() {
                clientSenderReceiver.sendTCP(sendPair);
            }
        };
        t.start();
    }

    public void requestPartner(String _partnerToken) {
        try {
            String partnerToken = _partnerToken;
            if (partnerToken.equals(clientSenderReceiver.token)) {
                partnerToken = clientReceiver.token;
            }
            Network.Pair pair = new Network.Pair();
            pair.seekerAccepted = true;
            pair.respondentAccepted = false;
            pair.connectionAlive = true;
            pair.tokenPairRespondent = partnerToken;
            pair.tokenPairSeeker = clientSenderReceiver.token;
            final Network.Pair sendPair = pair;
            Thread t = new Thread() {
                public void run() {
                    clientSenderReceiver.sendTCP(sendPair);
                }
            };
            t.start();
            Log.d(TAG, "requestPartner: ~~end");
        } catch (Exception e) {
            Log.d(TAG, "requestPartner: ~~error: " + e);
        }
    }

    public void newClients(String ip, String userName) {
        clientSenderReceiver = new ClientClassExtension();
        addClients(ip, clientSenderReceiver, userName, true);
        clientReceiver = new ClientClassExtension();
        addClients(ip, clientReceiver, userName, false);
    }

    public void stopClients() {
        try {
            clientSenderReceiver.stop();
        } catch (Exception e) {
        }
        try {
            clientReceiver.stop();
        } catch (Exception e) {
        }
        broadcast.sendValue(FILTER_KRYO, EXTRA_CONNECTION_CLOSED, "Kryonet: connection NA");
    }

    public boolean isClientsConnected() {
        return clientsConnected;
    }

    public void setClientsConnected(boolean clientsConnected) {
        this.clientsConnected = clientsConnected;
        if (!clientsConnected) {
            broadcast.sendValue(FILTER_KRYO, EXTRA_USER_INFO, "There is a connection error");
            broadcast.sendValue(FILTER_KRYO, EXTRA_COMMAND, EXTRA_COMMAND_SET_UNCHECKED);
            stopClients();
        }
    }
}

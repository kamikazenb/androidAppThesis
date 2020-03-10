package cz.utb.thesisapp.services.kryonet;

import android.net.NetworkInfo;
import android.util.Log;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

import cz.utb.thesisapp.services.Broadcast;
import cz.utb.thesisapp.services.TokenGenerator;

public class KryoClient {
    private static final String TAG = "KryoClient";
    private TokenGenerator tokenGenerator = new TokenGenerator();
    private Broadcast broadcast;
    //    sender
    MyClient clientSenderReceiver;
    //    receiver
    MyClient clientReceiver;
    HashMap<String, Network.Register> usersHashmap = new HashMap<>();
    final private String filter = "kryo";

    public KryoClient(Broadcast broadcast) {
        this.broadcast = broadcast;
    }

    private void addClients(final String ip, final MyClient client) {
        client.start();
        Network.register(client);
        client.klientName = "AndoidClient" + String.valueOf(android.os.Process.myPid());
        client.systemName = client.klientName;
        client.token = tokenGenerator.generateRandom(20);
        client.addListener(new Listener.ThreadedListener(new Listener() {
            public void connected(Connection connection) {
                Network.Register register = new Network.Register();
                register.systemName = "AndoidClient" + String.valueOf(android.os.Process.myPid());
                register.userName = register.systemName;
                register.token = client.token;
                client.sendTCP(register);
            }

            public void received(Connection connection, Object object) {

                if (object instanceof Network.Info) {
                    NetworkInfo(connection, object, client);
                }
                if (object instanceof Network.Pair) {
                    NetworkPair(connection, object, client);
                }
                if (object instanceof Network.RegisteredUsers) {
                    NetworkRegisteredUsers(connection, object, client);
                }
                if (object instanceof Network.TouchStart) {
                    broadcast.sendTouchFloats("TouchStart",
                            ((Network.TouchStart) object).x, ((Network.TouchStart) object).y);
                }
                if (object instanceof Network.TouchMove) {
                    broadcast.sendTouchFloats("TouchMove",
                            ((Network.TouchMove) object).x, ((Network.TouchMove) object).y);
                }
                if (object instanceof Network.ScreenSize) {
                    broadcast.sendTouchFloats("ScreenSize",
                            ((Network.ScreenSize) object).x, ((Network.ScreenSize) object).y);
                }
                if (object instanceof Network.CleanCanvas) {
                    broadcast.sendTouchBoolean("CleanCanvas",
                            ((Network.CleanCanvas) object).cleanCanvas);
                }
                if (object instanceof Network.TouchUp) {
                    broadcast.sendTouchBoolean("TouchUp",
                            ((Network.TouchUp) object).touchUp);
                }
                if (object instanceof Network.TouchTolerance) {
                    broadcast.sendTouchFloat("TouchTolerance",
                            ((Network.TouchTolerance) object).TOUCH_TOLERANCE);
                }
            }
        }));
        Thread t = new Thread() {
            public void run() {
                try {
                    //195.178.94.66
                    client.connect(5000, ip, Network.port);
                    broadcast.sendServiceString("kryo", "userInfo", "connections to kryonet successful");
                    broadcast.sendServiceString("kryo", "command", "setChecked");
                } catch (IOException ex) {
                    ex.printStackTrace();
                    broadcast.sendServiceString("kryo", "userInfo", "There is a connection error");
                    broadcast.sendServiceString("kryo", "command", "setUnchecked");
                    stopClients();
                }
            }
        };
        t.start();
    }

    private void NetworkRegisteredUsers(Connection connection, Object object, MyClient client) {
        Log.d(TAG, "received: ~~RegisteredUsers " + client.token);
        Network.RegisteredUsers registeredUsers = (Network.RegisteredUsers) object;
        for (int i = 0; i < ((Network.RegisteredUsers) object).users.size(); i++) {
            Network.Register reg = ((Network.RegisteredUsers) object).users.get(i);
            usersHashmap.put(reg.token, reg);
        }
        //      userName   token
        HashMap<String, String> usersMap = new HashMap<>();
        for (int i = 0; i < registeredUsers.users.size(); i++) {
            if (!registeredUsers.users.get(i).token.equals(client.token)) {
                usersMap.put(registeredUsers.users.get(i).token,
                        registeredUsers.users.get(i).userName);
            }
        }
        if (registeredUsers.users.size() > 0) {
            broadcast.sendServiceHashMap("kryo", "users", usersMap);
        }
    }

    private void NetworkInfo(Connection connection, Object object, MyClient client) {
        Network.Info info = (Network.Info) object;
        Log.d(TAG, "received: ~~inf " + client.token);
    }

    private void NetworkPair(Connection connection, Object object, MyClient client) {
        Log.d(TAG, "received: ~~Pair " + client.token);
        Network.Pair pair = (Network.Pair) object;
        if (pair.seekerAccepted && !pair.respondentAccepted) {
            String tokenPairSeeker = pair.tokenPairSeeker;
            String userName = Objects.requireNonNull(usersHashmap.get(tokenPairSeeker)).userName;
            HashMap<String, String> send = new HashMap<>();
            if (pair.tokenPairSeeker.equals(clientSenderReceiver.token)) {
                userName = "This app";
            }
            send.put(tokenPairSeeker, userName);
            broadcast.sendServiceHashMap(filter, "acceptPair", send);
        }
        if (pair.seekerAccepted && pair.respondentAccepted) {
            if (pair.tokenPairRespondent.equals(client.token)) {
                client.pairedToken = pair.tokenPairSeeker;
            } else {
                client.pairedToken = pair.tokenPairRespondent;
            }
            try {
                broadcast.sendServiceString(filter, "paired", usersHashmap.get(client.pairedToken).userName);
            } catch (Exception e) {
            }
        }
        if (!pair.seekerAccepted) {
            client.pairedToken = null;
            broadcast.sendServiceString(filter, "unpaired", "");
        }
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
        final Network.Pair sendPair = pair;
        Thread t = new Thread() {
            public void run() {
                clientSenderReceiver.sendTCP(sendPair);
            }
        };
        t.start();
    }

    public void sendTouchStart(float x, float y) {
        Network.TouchStart touch = new Network.TouchStart();
        touch.x = x;
        touch.y = y;
        final Network.TouchStart sendTouch = touch;
        Thread t = new Thread() {
            public void run() {
                clientSenderReceiver.sendTCP(sendTouch);
            }
        };
        t.start();
    }
    public void sendTouchMove(float x, float y) {
        Network.TouchMove touch = new Network.TouchMove();
        touch.x = x;
        touch.y = y;
        final Network.TouchMove sendTouch = touch;
        Thread t = new Thread() {
            public void run() {
                clientSenderReceiver.sendTCP(sendTouch);
            }
        };
        t.start();
    }
    public void sendTouchUp(boolean state) {
        Network.TouchUp touch = new Network.TouchUp();
        touch.touchUp = state;
        final Network.TouchUp sendTouch = touch;
        Thread t = new Thread() {
            public void run() {
                clientSenderReceiver.sendTCP(sendTouch);
            }
        };
        t.start();
    }


    public void unPair() {
        Network.Pair pair = new Network.Pair();
        pair.tokenPairRespondent = clientSenderReceiver.token;
        pair.tokenPairSeeker = clientSenderReceiver.pairedToken;
        pair.seekerAccepted = false;
        pair.seekerAccepted = false;
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

    public void newClients(String ip) {
        clientSenderReceiver = new MyClient();
        addClients(ip, clientSenderReceiver);
        clientReceiver = new MyClient();
        addClients(ip, clientReceiver);
    }

    public void stopClients() {
        try {
            clientSenderReceiver.stop();
            clientReceiver.stop();
        } catch (Exception e) {

        }
        broadcast.sendServiceString("kryo", "userInfo", "Connection closed");
    }
}

package cz.utb.thesisapp.kryonet;

import android.util.Log;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import cz.utb.thesisapp.services.Broadcast;
import cz.utb.thesisapp.services.TokenGenerator;

public class KryoClient {
    private static final String TAG = "KryoClient";
    private TokenGenerator tokenGenerator = new TokenGenerator();
    private Broadcast broadcast;
    //    sender
    MyClient client0;
    //    receiver
    MyClient client1;
    ArrayList<Network.Register> users;

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
                    Network.Info info = (Network.Info) object;
                    Log.d(TAG, "received: ~~Network.info " + info.message);
                }
                if (object instanceof Network.Pair) {
                    Network.Pair pair = (Network.Pair) object;
                    if (pair.seekerAccepted) {

                    }

                }
                if (object instanceof Network.RegisteredUsers) {
                    Network.RegisteredUsers registeredUsers = (Network.RegisteredUsers) object;
                    users = ((Network.RegisteredUsers) object).users;
                    //      userName   token
                    HashMap<String, String> usersMap = new HashMap<>();
                    for (int i = 0; i < registeredUsers.users.size(); i++) {
                        if (!registeredUsers.users.get(i).token.equals(client.token)) {
                            usersMap.put(registeredUsers.users.get(i).userName,
                                    registeredUsers.users.get(i).token);
                        }
                    }
                    if (users.size() > 0) {
                        broadcast.sendBroadcastHashMap("kryo", "users", usersMap);
                    }

                }
            }
        }));
        Thread t = new Thread() {
            public void run() {
                try {
                    //195.178.94.66
                    client.connect(5000, ip, Network.port);
                    broadcast.sendBroadcastString("kryo", "userInfo", "connections to kryonet successful");
                    broadcast.sendBroadcastString("kryo", "command", "setChecked");
                } catch (IOException ex) {
                    ex.printStackTrace();
                    broadcast.sendBroadcastString("kryo", "userInfo", "There is a connection error");
                    broadcast.sendBroadcastString("kryo", "command", "setUnchecked");
                    stopClients();
                }
            }
        };
        t.start();
    }

    public void requestPartner(String token) {
        try {
            String partnerToken = token;
            if (partnerToken.equals(client0.token)) {
                partnerToken = client1.token;
            }
            Network.Pair pair = new Network.Pair();
            pair.seekerAccepted = true;
            pair.tokenPairRespondent = partnerToken;
            pair.tokenPairSeeker = client0.token;
            client0.sendTCP(pair);
        } catch (Exception e) {

        }
    }

    public void newClients(String ip) {
        client0 = new MyClient();
        addClients(ip, client0);
        client1 = new MyClient();
        addClients(ip, client1);
    }

    public void stopClients() {
        try {
            client0.stop();
            client1.stop();
        } catch (Exception e) {

        }
        broadcast.sendBroadcastString("kryo", "userInfo", "Connection closed");
    }
}

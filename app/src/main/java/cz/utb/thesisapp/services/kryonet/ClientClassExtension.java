package cz.utb.thesisapp.services.kryonet;

import com.esotericsoftware.kryonet.Client;

public class ClientClassExtension extends Client {
    public boolean mainClient;
    public String systemName;
    public String klientName;
    public String token;
    public String pairedToken;
}

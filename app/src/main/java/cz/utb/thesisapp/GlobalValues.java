package cz.utb.thesisapp;

import static cz.utb.thesisapp.services.kryonet.Network.TOUCH_MOVE;
import static cz.utb.thesisapp.services.kryonet.Network.TOUCH_START;
import static cz.utb.thesisapp.services.kryonet.Network.TOUCH_UP;

public class GlobalValues {
    //broadcast filters
    public static final String FILTER_KRYO = "kryo";
    public static final String FILTER_INFO = "info";
    public static final String FILTER_TOUCH = "touch";
    public static final String FILTER_MAIN_ACTIVITY = "MainActivity";

    //broadcast commands a.k.a. intent's extras
    public static final String EXTRA_TOUCH_START = TOUCH_START;
    public static final String EXTRA_TOUCH_MOVE = TOUCH_MOVE;
    public static final String EXTRA_TOUCH_UP = TOUCH_UP;
    public static final String EXTRA_X = "x";
    public static final String EXTRA_Y = "y";

    public static final String EXTRA_USERS = "users";
    public static final String EXTRA_USER_INFO = "userInfo";
    public static final String EXTRA_CONNECTION_CLOSED = "connectionClosed";
    public static final String EXTRA_PAIRED = "paired";
    public static final String EXTRA_UNPAIRED = "unpaired";
    public static final String EXTRA_ACCEPT_PAIR_REQUEST = "acceptPair";
    public static final String EXTRA_EDIT = "edit";
    public static final String EXTRA_COMMAND = "command";
    public static final String EXTRA_COMMAND_SET_CHECKED = "setChecked";
    public static final String EXTRA_COMMAND_SET_UNCHECKED = "setUnchecked";
    public static final String EXTRA_KROYSERVER_USE_DATABASE = "kryoserverUseDatabase";

    public static final String EXTRA_UPLOAD = "UPLOAD";
    public static final String EXTRA_DOWNLOAD = "DOWNLOAD";
    public static final String EXTRA_SPEED_PROGRESS = "progress";

    //db

    public static final String DB_DATABASE_NAME = "appDB";
    public static final String DB_ID = "id";
    public static final String DB_TABLE_NAME = "touch";
    public static final String DB_X = "x";
    public static final String DB_Y = "y";
    public static final String DB_TOUCH_TYPE = "touchType";
    public static final String DB_CLIENT_CREATED = "clientCreated";
    public static final String DB_SERVER_RECEIVED = "serverReceived";
    public static final String DB_CLIENT_RECEIVED = "clientReceived";
}

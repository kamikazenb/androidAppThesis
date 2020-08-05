package cz.utb.thesisapp;

import java.util.Date;

import static cz.utb.thesisapp.services.kryonet.Network.TOUCH_MOVE;
import static cz.utb.thesisapp.services.kryonet.Network.TOUCH_START;
import static cz.utb.thesisapp.services.kryonet.Network.TOUCH_UP;

public class GlobalValues {
//    variables for testing function
    public static final int TOUCH_SLEEP_MIN = 50;
    public static final int TOUCH_SLEEP_BASE = 250;
    public static final int TOUCH_ITERATIONS = 500;

    public static final int SLEEP_BEFORE_EXPORT_DB = 1500;

    public static final int TOUCH_NO_TEST = 0;
    public static final int TOUCH_START_TEST = 1;
    public static final int TOUCH_BREAK_TEST = 2;
    public static final int TOUCH_TEST_FINISHED = 4;
    public static final int TOUCH_FAB_TOUCHED = 3;
    //broadcast filters
    public static final String FILTER_KRYO = "kryo";
    public static final String FILTER_INFO = "info";
    public static final String FILTER_TOUCH = "touch";
    public static final String FILTER_WEB = "web";
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
    public static final String DB_TABLE_REMOTE = "touchRemote";
    public static final String DB_TABLE_LOCAL = "touchLocal";
    public static final String DB_X = "x";
    public static final String DB_Y = "y";
    public static final String DB_TOUCH_TYPE = "touchType";
    public static final String DB_CREATED = "clientCreated";
    public static final String DB_RECEIVED = "clientReceived";

    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";


    public static final String API_PORT = ":50202";
    public static final String API_REST = "/rest";
    public static final String API_CLIENT = "/client";
    public static final String API_TOUCH = "/touch";
    public static final String API_SSE = "/sse";

    static public class Touch {
        public Touch(float x, float y, String touchType, Date clientCreated, Date clientReceived) {
            this.x = x;
            this.y = y;
            this.touchType = touchType;
            this.clientCreated = clientCreated;
            this.clientReceived = clientReceived;
        }

        public Touch(String touchType, float x, float y, Date clientCreated) {
            this.x = x;
            this.y = y;
            this.touchType = touchType;
            this.clientCreated = clientCreated;
        }

        public Touch() {

        }

        public float x;
        public float y;
        public String touchType;
        public Date clientCreated;
        public Date clientReceived;
    }
}

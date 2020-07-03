package cz.utb.thesisapp;

public class GlobalValues {
    //broadcast filters
    public static final String FILTER_KRYO = "kryo";
    public static final String FILTER_INFO = "info";
    public static final String FILTER_TOUCH = "touch";
    public static final String FILTER_MAIN_ACTIVITY = "MainActivity";

    //broadcast commands a.k.a. intent's extras
    public static final String EXTRA_TOUCH_START = "TouchStart";
    public static final String EXTRA_TOUCH_MOVE = "TouchMove";
    public static final String EXTRA_TOUCH_UP = "TouchUp";
    public static final String EXTRA_X = "x";
    public static final String EXTRA_Y = "y";

    public static final String EXTRA_USERS = "users";
    public static final String EXTRA_COMMAND = "command";
    public static final String EXTRA_USER_INFO = "userInfo";
    public static final String EXTRA_CONNECTION_CLOSED = "connectionClosed";
    public static final String EXTRA_PAIRED = "paired";
    public static final String EXTRA_UNPAIRED = "unpaired";
    public static final String EXTRA_ACCEPT_PAIR_REQUEST = "acceptPair";
    public static final String EXTRA_EDIT = "edit";

    public static final String EXTRA_UPLOAD = "UPLOAD";
    public static final String EXTRA_DOWNLOAD = "DOWNLOAD";
    public static final String EXTRA_SPEED_PROGRESS = "progress";
}

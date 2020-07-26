package cz.utb.thesisapp.services.webServices;

import android.util.Log;

import com.here.oksse.OkSse;
import com.here.oksse.ServerSentEvent;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

import cz.utb.thesisapp.GlobalValues;
import cz.utb.thesisapp.services.Broadcast;
import cz.utb.thesisapp.services.MyService;
import okhttp3.Request;
import okhttp3.Response;

import static cz.utb.thesisapp.GlobalValues.API_SSE;
import static cz.utb.thesisapp.GlobalValues.API_PORT;
import static cz.utb.thesisapp.GlobalValues.EXTRA_USER_INFO;
import static cz.utb.thesisapp.GlobalValues.FILTER_INFO;

public class Sse {
    private static final String TAG = "SSE";
    private Request request;
    private OkSse okSse;
    private ServerSentEvent sse;
    private MyService myService;
    private Broadcast broadcast;
    SimpleDateFormat df = new SimpleDateFormat(GlobalValues.DATE_FORMAT);


    ServerSentEvent.Listener listener = new ServerSentEvent.Listener() {
        @Override
        public void onOpen(ServerSentEvent sse, Response response) {
            Log.i(TAG, "onOpen: ~~");
            broadcast.sendValue(FILTER_INFO, EXTRA_USER_INFO, "onOpen");
        }

        @Override
        public void onMessage(ServerSentEvent sse, String id, String event, String message) {
            try {
                JSONObject jo = new JSONObject(message);
                myService.saveToRemoteDatabase(
                        df.parse(jo.getString("clientCreated")),
                        new Date(System.currentTimeMillis()),
                        (float) jo.getDouble("x"),
                        (float) jo.getDouble("y"),
                        jo.getString("touchType")
                );

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onComment(ServerSentEvent sse, String comment) {
            Log.i(TAG, "onComment: ~~");
        }

        @Override
        public boolean onRetryTime(ServerSentEvent sse, long milliseconds) {
            Log.i(TAG, "onRetryTime: ~~");
            return false;
        }

        @Override
        public boolean onRetryError(ServerSentEvent sse, Throwable throwable, Response response) {
            Log.i(TAG, "onRetryError: ~~" + throwable);
            return false;
        }

        @Override
        public void onClosed(ServerSentEvent sse) {
            Log.i(TAG, "onClosed: ~~");
            broadcast.sendValue(FILTER_INFO, EXTRA_USER_INFO, "Web Services: connection NA");
        }

        @Override
        public Request onPreRetry(ServerSentEvent sse, Request originalRequest) {
            Log.i(TAG, "onPreRetry: ~~");
            return null;
        }
    };

    public Sse(MyService myService, Broadcast broadcast) {
        this.broadcast = broadcast;
        this.myService = myService;
    }


    public void start(String ipAddress, String token) {
        request = new Request.Builder().url("http://" + ipAddress + API_PORT + API_SSE + "/" + token).build();
        okSse = new OkSse();
        Log.i(TAG, "start: ~~startSSE");
        Thread t = new Thread() {
            public void run() {
                sse = okSse.newServerSentEvent(request, listener);
            }
        };
        t.start();
    }

    public void stop() {
        if(sse != null){
            sse.close();
        }
        myService.webservices = false;
    }
}

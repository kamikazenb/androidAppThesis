package cz.utb.thesisapp.services.webServices;

import android.util.Log;

import com.here.oksse.OkSse;
import com.here.oksse.ServerSentEvent;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Request;
import okhttp3.Response;

import static cz.utb.thesisapp.GlobalValues.API_SSE;
import static cz.utb.thesisapp.GlobalValues.API_URL;

public class Sse {
    private static final String TAG = "SSE";
    private Request request;
    private OkSse okSse;
    private ServerSentEvent sse;
    ServerSentEvent.Listener listener = new ServerSentEvent.Listener() {
        @Override
        public void onOpen(ServerSentEvent sse, Response response) {
            Log.d(TAG, "onOpen: ~~");
        }

        @Override
        public void onMessage(ServerSentEvent sse, String id, String event, String message) {
//            Log.d(TAG, "onMessage: ~~%" + message);
            try {
                JSONObject jsonObject = new JSONObject("message");
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onComment(ServerSentEvent sse, String comment) {
            Log.d(TAG, "onComment: ~~");
        }

        @Override
        public boolean onRetryTime(ServerSentEvent sse, long milliseconds) {
            Log.d(TAG, "onRetryTime: ~~");
            return false;
        }

        @Override
        public boolean onRetryError(ServerSentEvent sse, Throwable throwable, Response response) {
            Log.d(TAG, "onRetryError: ~~" + throwable);
            return false;
        }

        @Override
        public void onClosed(ServerSentEvent sse) {
            Log.d(TAG, "onClosed: ~~");
        }

        @Override
        public Request onPreRetry(ServerSentEvent sse, Request originalRequest) {
            Log.d(TAG, "onPreRetry: ~~");
            return null;
        }
    };

    public Sse() {

    }

    public void start(String ipAddress) {
        request = new Request.Builder().url("http://"+ipAddress+":"+ API_URL + ""+API_SSE).build();
        okSse = new OkSse();
        Thread t = new Thread() {
            public void run() {
                  sse = okSse.newServerSentEvent(request, listener);
            }
        };
        t.start();
    }

    public void stop() {
        sse.close();
    }
}

package cz.utb.thesisapp.services.webServices;

import android.app.Service;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import cz.utb.thesisapp.services.Broadcast;

import static cz.utb.thesisapp.GlobalValues.API_CLIENT;
import static cz.utb.thesisapp.GlobalValues.API_REST;
import static cz.utb.thesisapp.GlobalValues.API_TOUCH;
import static cz.utb.thesisapp.GlobalValues.API_PORT;
import static cz.utb.thesisapp.GlobalValues.DATE_FORMAT;
import static cz.utb.thesisapp.GlobalValues.EXTRA_CONNECTION_CLOSED;
import static cz.utb.thesisapp.GlobalValues.FILTER_WEB;

public class RestApi {
    private static final String TAG = "RestApi";
    private RequestQueue mQueue;
    private String url = "";
    private Service service;
    private Broadcast broadcast;
    SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT);

    public RestApi(Service service, Broadcast broadcast) {
        this.service = service;
        this.broadcast = broadcast;
    }

    public void startRestApi(String ipAddress, String name, String token) {
        url = "http://" + ipAddress + API_PORT+API_REST;
        mQueue = Volley.newRequestQueue(service);
        mQueue.start();
        sendClient(name, token);
    }

    public void stop() {
        mQueue.stop();
    }

    public void sendClient(String name, String token) {
        JSONObject joSend = new JSONObject();
        try {
            joSend.put("name", name);
            joSend.put("token", token);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.i(TAG, "sendTouch: ~~" + joSend.toString());
        Log.i(TAG, "sendTouch: ~~" + url+API_CLIENT);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url+API_CLIENT,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        broadcast.sendValue(FILTER_WEB, EXTRA_CONNECTION_CLOSED, "WebServices connection NA");
                    }

                }) {
            @Override
            public byte[] getBody() throws AuthFailureError {
                return joSend.toString().getBytes();
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };
        // Add the request to the RequestQueue.
        mQueue.add(stringRequest);

    }

    public void sendTouch(float x, float y, String touchType, String token) {
        JSONObject joTouch = new JSONObject();
        JSONObject joClient = new JSONObject();
        JSONObject joSend = new JSONObject();
        try {
            joTouch.put("x", x);
            joTouch.put("y", y);
            joTouch.put("touchType", touchType);
            joTouch.put("clientCreated", df.format(new Date(System.currentTimeMillis())));
            joClient.put("token", token);
            joClient.put("name", "");
            joSend.put("touch", joTouch);
            joSend.put("client", joClient);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.i(TAG, "sendTouch: ~~" + joSend.toString());

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url+API_TOUCH,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }

                }) {
            @Override
            public byte[] getBody() throws AuthFailureError {
                return joSend.toString().getBytes();
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };
        // Add the request to the RequestQueue.
        mQueue.add(stringRequest);

    }
}

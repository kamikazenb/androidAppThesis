package cz.utb.thesisapp.services.webServices;

import android.app.Service;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static cz.utb.thesisapp.GlobalValues.API_URL;
import static cz.utb.thesisapp.GlobalValues.DATE_FORMAT;

public class RestApi {
    private static final String TAG = "RestApi";
    private RequestQueue mQueue;
    private String url = "";
    private Service service;
    SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT);
    public RestApi(Service service) {
        this.service = service;
    }

    public void startRestApi(String ipAddress) {
        url = "http://" + ipAddress + ":" + API_URL;
        mQueue = Volley.newRequestQueue(service);
        mQueue.start();
    }
    public void stop(){
        mQueue.stop();
    }

    public void sendTouch(float x, float y, String touchType) {
        JSONObject jo = new JSONObject();
        try {
            jo.put("x", x);
            jo.put("y", y);
            jo.put("touchType", touchType);
            jo.put("clientCreated", df.format(new Date(System.currentTimeMillis())));
            jo.put("serverReceived", df.format(new Date(System.currentTimeMillis())));
            jo.put("client_idclient", 103);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "sendTouch: ~~"+jo.toString());

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
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
                return jo.toString().getBytes();
            }
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError
            {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };
        // Add the request to the RequestQueue.
        mQueue.add(stringRequest);

    }
}

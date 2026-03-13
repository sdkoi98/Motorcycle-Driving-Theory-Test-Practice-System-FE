package com.example.motorcycletheory.network;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.example.motorcycletheory.R;

public class ApiClient {
    private static ApiClient instance;
    private final RequestQueue requestQueue;
    private final String baseUrl;

    private ApiClient(Context context) {
        requestQueue = Volley.newRequestQueue(context.getApplicationContext());
        baseUrl = context.getString(R.string.api_base_url);
    }

    public static synchronized ApiClient getInstance(Context context) {
        if (instance == null) {
            instance = new ApiClient(context);
        }
        return instance;
    }

    public RequestQueue getRequestQueue() {
        return requestQueue;
    }

    public String getBaseUrl() {
        return baseUrl;
    }
}

package com.tranthuong.fetchapp.services;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by TRANTHUONG.
 */
@EBean(scope = EBean.Scope.Singleton)
public class DataService
{
    public static final String DATA_FRETCHING_FINISHED = "com.tranthuong.fetchapp.services.DATA_FETCHING_FINISHED";
    public static final String NEW_DATA_FETCHING_AVAILABLE_ATTR = "com.tranthuong.fetchapp.services.NEW_FETCHING_DATA_AVAILABLE";
    public static final String NEW_DATA_FETCHING_AVAILABLE_CODE_ATTR = "com.tranthuong.fetchapp.services.NEW_FETCHING_DATA_AVAILABLE_CODE";


    private static final String TAG = "DataService";
    private static final String FETCH_DATA_SERVICE_URL = "http://dev-api2.goplayme.com/fetch_data";
    @RootContext
    Context context;

    private boolean isFetchingDataProgress;

    private RequestQueue mRequestQueue;

    @AfterInject
    protected void afterInject()
    {
        Log.d(TAG, ">> afterInject ");
        mRequestQueue = getRequestQueue();
        isFetchingDataProgress = false;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null)
        {
                mRequestQueue = Volley.newRequestQueue(context.getApplicationContext());
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req)
    {
        getRequestQueue().add(req);
    }

    private void senBroadcast(int code , String newData)
    {
        Intent broadcastIntent = new Intent(DATA_FRETCHING_FINISHED);
        if(newData!=null)
        {
            broadcastIntent.putExtra(NEW_DATA_FETCHING_AVAILABLE_ATTR, newData);
            broadcastIntent.putExtra(NEW_DATA_FETCHING_AVAILABLE_CODE_ATTR, code);
        }
        context.sendBroadcast(broadcastIntent);
    }

    public void fetchDataFromServer()
    {

        if(mRequestQueue!=null)
        {
            isFetchingDataProgress = true;
            StringRequest stringRequest = new StringRequest(Request.Method.GET, FETCH_DATA_SERVICE_URL,
                    new Response.Listener<String>()
                    {
                        @Override
                        public void onResponse(String response)
                        {
                            //Log.d(TAG, "fetchDataFromServer = \n" + response);
                            isFetchingDataProgress = false;
                            int responseCode = 0;
                            try {
                                new JSONObject(response);
                            } catch (JSONException e) {
                                e.printStackTrace();
                                responseCode = 2;//JSON data error
                                response = e.toString();
                            }
                            senBroadcast(responseCode, response);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error)
                        {
                            Log.e(TAG,"fetchDataFromServer, that didn't work!  \n"+error.toString());
                            isFetchingDataProgress = false;
                            senBroadcast(1, error.toString());
                        }
                    });

            mRequestQueue.add(stringRequest);
        }

    }

    public boolean isFetchingDataProgress()
    {
        return isFetchingDataProgress;
    }
}

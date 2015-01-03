package com.tranthuong.fetchapp;

/**
 * Created by TRANTHUONG.
 */

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.tranthuong.fetchapp.services.DataService;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_main)
public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";

    @Bean
    protected DataService dataService;

    @ViewById(R.id.fetchFromServerBt_id)
    protected Button fetchFromServerBt;

    @ViewById(R.id.progressPanel_id)
    protected View progressPanel;

    private DataLoadedBroadcastReceiver dataLoadingFinishedReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Log.d(TAG, ">> onCreate ()");
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @AfterViews
    protected void AfterViews()
    {
        if(dataService.isFetchingDataProgress())
        {
            fetchFromServerBt.setVisibility(View.GONE);
            progressPanel.setVisibility(View.VISIBLE);
        }
        else
        {
            fetchFromServerBt.setVisibility(View.VISIBLE);
            progressPanel.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onStart() {
        Log.d(TAG, ">> onStart ()");
        super.onStart();
        if (dataLoadingFinishedReceiver == null)
        {
            dataLoadingFinishedReceiver = new DataLoadedBroadcastReceiver();
            registerReceiver(dataLoadingFinishedReceiver, new IntentFilter(DataService.DATA_FRETCHING_FINISHED));
        }
    }

    @Override
    protected void onStop()
    {
        Log.d(TAG, ">> onStop ()");
        if (dataLoadingFinishedReceiver != null)
        {
            unregisterReceiver(dataLoadingFinishedReceiver);
            dataLoadingFinishedReceiver=null;
        }

        super.onStop();
    }


    @Click(R.id.fetchFromServerBt_id)
    protected void fetchFromServerBtClicked()
    {
        fetchFromServerBt.setVisibility(View.GONE);
        progressPanel.setVisibility(View.VISIBLE);

        dataService.fetchDataFromServer();
    }

    private class DataLoadedBroadcastReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            int errCode = intent.getExtras().getInt(DataService.NEW_DATA_FETCHING_AVAILABLE_CODE_ATTR);
            String dataStr = intent.getExtras().getString(DataService.NEW_DATA_FETCHING_AVAILABLE_ATTR);

            //Log.d(TAG,"onReceive dataStr=\n"+dataStr);

            if (errCode == 0)
            {
                //OK
                Intent startIntent = getPackageManager().getLaunchIntentForPackage("com.example.tranthuong.hostapp");
                if (startIntent != null)
                {
                    startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startIntent.putExtra("fetched.data", dataStr);
                    //Log.d(TAG,"onReceive startIntent=\n"+startIntent);
                    startActivity(startIntent);
                }
                else
                {
                    Toast.makeText(context, "HostApp n'est pas installée!", Toast.LENGTH_LONG).show();
                }
            }
            else
            {
                //ERROR
                Toast.makeText(context, "Fetch data error ! \n"+dataStr, Toast.LENGTH_LONG).show();
            }

            fetchFromServerBt.setVisibility(View.VISIBLE);
            progressPanel.setVisibility(View.GONE);

        }
    }
}

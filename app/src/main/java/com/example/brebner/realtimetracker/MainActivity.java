package com.example.brebner.realtimetracker;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognition;

import android.util.Log;
import android.widget.Toast;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.tasks.Task;

import java.security.KeyException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;



import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    public GoogleApiClient googleApiClient;

    private static final String TAG="MainActivity";

    public static final String[] xData = {"Car", "Bicycle", "Foot", "Running", "Still", "Tilting", "Walking", "Unknown"};
    private int[] yData = {1, 1, 1, 1, 1, 1, 1, 1};

    PieChart pieChart;

    // Broadcast receiver for receiving status updates from the IntentService
    private class GetActivityDataReceiver extends BroadcastReceiver {

        // Prevents instantiation
        private GetActivityDataReceiver() {
        }

        // Called when the BroadcastReceiver gets an Intent it's registered to receive
        @Override
        public void onReceive(Context context, Intent intent) {
            /*
             * Handle Intents here.
             */
            Log.d(TAG, "onReceive: Called");
            Bundle extras = intent.getExtras();
            Bundle details = (Bundle)extras.get(Constants.EXTENDED_DATA_STATUS);
            for (String key : xData) {
                int value = details.getInt(key);
                try {
                    int pos = getPos(key);
                    yData[pos] = value;
                }
                catch (KeyException ke) {
                    Log.e(TAG, "onReceive: Bad Activity string received", ke);
                }
            }
            addDataSet(); // updated yData - now process and show
        }

    }

    private void validate_xdata() {
        assert xData[0] == getString(R.string.car_str);
        assert xData[1] == getString(R.string.bicycle_str);
        assert xData[2] == getString(R.string.foot_str);
        assert xData[3] == getString(R.string.running_str);
        assert xData[4] == getString(R.string.still_str);
        assert xData[5] == getString(R.string.tilting_str);
        assert xData[6] == getString(R.string.walking_str);
        assert xData[7] == getString(R.string.unknown_str);
    }

    private int getPos(String key) throws KeyException {
        // because Java cannot initialise Maps sensibly :(
        for (int i=0; i < xData.length; i++) {
            if (key == xData[i]) {
                return i;
            }
        }
        throw new KeyException("Incorrect key found");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        validate_xdata();
        // set up the intent filter for broadcasts
        // The filter's action is BROADCAST_ACTION
        IntentFilter statusIntentFilter = new IntentFilter(
                Constants.BROADCAST_ACTION);
        // Instantiates a new DownloadStateReceiver
        GetActivityDataReceiver getActivityDataReceiver = new GetActivityDataReceiver();
        // Registers the DownloadStateReceiver and its intent filters
        LocalBroadcastManager.getInstance(this).registerReceiver(
                getActivityDataReceiver,
                statusIntentFilter);
        // now set up the API client to Google
        googleApiClient = new GoogleApiClient.Builder(MainActivity.this)
                .addApi(ActivityRecognition.API)
                .addConnectionCallbacks(MainActivity.this)
                .addOnConnectionFailedListener(MainActivity.this)
                .build();
        googleApiClient.connect();
        pieChart = (PieChart) findViewById(R.id.chart);
        pieChart.setRotationEnabled(true);
        pieChart.setUsePercentValues(true);
        pieChart.setHoleColor(Color.BLUE);
        pieChart.setCenterTextColor(Color.WHITE);
        pieChart.setHoleRadius(25f);
        pieChart.setTransparentCircleAlpha(0);
        pieChart.setCenterText("Activity Chart");
        pieChart.setCenterTextSize(10);
        addDataSet();
        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                Log.d(TAG, "onValueSelected: Value select from chart.");
                Log.d(TAG, "onValueSelected: " + e.toString());
                Log.d(TAG, "onValueSelected: " + h.toString());

                int xpos = h.toString().indexOf("x: ");
                String value = h.toString().substring(xpos + "x: ".length());
                int dotzeropos = value.indexOf(".0");
                if (dotzeropos > 0) {
                    value = value.substring(0, dotzeropos);
                }
                Log.d(TAG, "onValueSelected: X = " + value + " xpos -> " + xpos);

                int which = Integer.parseInt(value);
                String activity = xData[which];
                Toast.makeText(MainActivity.this,  activity, Toast.LENGTH_LONG).show();
            }
            @Override
            public void onNothingSelected() {

            }
        });

    }

    private void addDataSet() {
        Log.d(TAG, "addDataSet started");
        ArrayList<PieEntry> yEntrys = new ArrayList<>();
        ArrayList<String> xEntrys = new ArrayList<>();
        assert xData.length == yData.length;  // otherwise bad stuff happens
        for(int i = 0; i < yData.length; i++){
            yEntrys.add(new PieEntry(yData[i] , i));
        }

        for(int i = 0; i < xData.length; i++){
            xEntrys.add(xData[i]);
        }
        //create the data set
        PieDataSet pieDataSet = new PieDataSet(yEntrys, "Activity");
        pieDataSet.setSliceSpace(2);
        pieDataSet.setValueTextSize(12);
        //add colors to dataset
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.GRAY);
        colors.add(Color.BLUE);
        colors.add(Color.RED);
        colors.add(Color.GREEN);
        colors.add(Color.CYAN);
        colors.add(Color.YELLOW);
        colors.add(Color.MAGENTA);
        colors.add(Color.BLACK);
        pieDataSet.setColors(colors);
        //add legend to chart
        Legend legend = pieChart.getLegend();
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setPosition(Legend.LegendPosition.LEFT_OF_CHART);
        //create pie data object
        PieData pieData = new PieData(pieDataSet);
        pieChart.setData(pieData);
        pieChart.invalidate();
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Intent intent = new Intent(MainActivity.this, ActivityRecognizedService.class);
        PendingIntent pendingIntent = PendingIntent.getService(MainActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        //ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(googleApiClient, 3000, pendingIntent);
        ActivityRecognitionClient activityRecognitionClient = ActivityRecognition.getClient(this);
        Task task = activityRecognitionClient.requestActivityUpdates(3000, pendingIntent);

    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast toast = Toast.makeText(this, R.string.connection_paused, Toast.LENGTH_LONG);
        toast.show();

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast toast = Toast.makeText(this, R.string.connection_failed, Toast.LENGTH_LONG);
        toast.show();
    }
}

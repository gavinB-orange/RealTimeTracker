package com.example.brebner.realtimetracker;

import android.app.IntentService;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.HashMap;
import java.util.List;

/**
 * Created by brebner on 12-Mar-18.
 */


public class ActivityRecognizedService extends IntentService {

    private static final String TAG = "ARS";
    private boolean forceTestData = false;

    public ActivityRecognizedService() {
        this("ActivityRecogizedService");
    }

    public ActivityRecognizedService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (ActivityRecognitionResult.hasResult(intent)) {
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            Bundle results = handleDetectActivity(result.getProbableActivities(), forceTestData);
            /*
             * Creates a new Intent containing a Uri object
             * BROADCAST_ACTION is a custom Intent action
             */
            Intent localIntent =
                    new Intent(Constants.BROADCAST_ACTION)
                            // Puts the status into the Intent
                            .putExtra(Constants.EXTENDED_DATA_STATUS, results);
            // Broadcasts the Intent to receivers in this app.
            Log.d(TAG, "onHandleIntent: Sending broadcast");
            LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
            Log.d(TAG, "onHandleIntent:     sent");
        }
    }

    private Bundle handleDetectActivity(List<DetectedActivity> probableActivities, boolean forceTestData) {
        Bundle bundle = new Bundle();
        if (forceTestData) {
            for (int i = 0; i < MainActivity.xData.length; i++) {
                bundle.putInt(MainActivity.xData[i], 2);
            }
            for (int i=0; i< MainActivity.xData.length; i++) {
                Log.d(TAG, "handleDetectActivity: " + MainActivity.xData[i] + " :" + bundle.getInt(MainActivity.xData[i]));
            }
            return bundle;
        }
        for (int i = 0; i < MainActivity.xData.length; i++) {
            bundle.putInt(MainActivity.xData[i], 0);
        }
        bundle.putInt("Still", 1);   // by default, Still
        for (DetectedActivity activity:probableActivities) {
            switch(activity.getType()) {
                case DetectedActivity.IN_VEHICLE:{
                    Log.d(TAG, "handleDetectActivity: in vehicle " + activity.getConfidence() + "%");
                    bundle.putInt(getString(R.string.car_str), activity.getConfidence());
                    break;
                }
                case DetectedActivity.ON_BICYCLE:{
                    Log.d(TAG, "handleDetectActivity: on bycycle " + activity.getConfidence() + "%");
                    bundle.putInt(getString(R.string.bicycle_str), activity.getConfidence());
                    break;
                }
                case DetectedActivity.ON_FOOT:{
                    Log.d(TAG, "handleDetectActivity: on foot " + activity.getConfidence() + "%");
                    bundle.putInt(getString(R.string.foot_str), activity.getConfidence());
                    break;
                }
                case DetectedActivity.RUNNING:{
                    Log.d(TAG, "handleDetectActivity: running " + activity.getConfidence() + "%");
                    bundle.putInt(getString(R.string.running_str), activity.getConfidence());
                    break;
                }
                case DetectedActivity.STILL:{
                    Log.d(TAG, "handleDetectActivity: still " + activity.getConfidence() + "%");
                    bundle.putInt(getString(R.string.still_str), activity.getConfidence());
                    break;
                }
                case DetectedActivity.TILTING:{
                    Log.d(TAG, "handleDetectActivity: tilting " + activity.getConfidence() + "%");
                    bundle.putInt(getString(R.string.tilting_str), activity.getConfidence());
                    break;
                }
                case DetectedActivity.WALKING:{
                    Log.d(TAG, "handleDetectActivity: walking " + activity.getConfidence() + "%");
                    bundle.putInt(getString(R.string.walking_str), activity.getConfidence());
                    break;
                }
                case DetectedActivity.UNKNOWN:{
                    Log.d(TAG, "handleDetectActivity: unknown " + activity.getConfidence() + "%");
                    bundle.putInt(getString(R.string.unknown_str), activity.getConfidence());
                    break;
                }
                default: {
                    Log.d(TAG, "handleDetectActivity: activity not recognized");
                }
            }
        }
        return bundle;
    }


}

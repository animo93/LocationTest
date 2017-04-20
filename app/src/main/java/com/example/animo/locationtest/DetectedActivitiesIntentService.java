package com.example.animo.locationtest;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.ArrayList;

/**
 * Created by animo on 19/4/17.
 */

public class DetectedActivitiesIntentService extends IntentService{

    protected static final String LOG_TAG=DetectedActivitiesIntentService.class.getSimpleName();
    public DetectedActivitiesIntentService() {
        super(LOG_TAG);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
        Intent localIntent = new Intent(Constants.BROADCAST_ACTION);

        ArrayList<DetectedActivity> detectedActivities = (ArrayList<DetectedActivity>) result.getProbableActivities();

        Log.i(LOG_TAG,"activities detected");

        localIntent.putExtra(Constants.ACTIVITY_EXTRA,detectedActivities);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);

    }
}

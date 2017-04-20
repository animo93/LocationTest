package com.example.animo.locationtest;

import android.*;
import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.vision.text.Text;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener ,
        ResultCallback<Status>{

    protected ActivityDetectionBroadcastReceiver mBroadcastReceiver;
    private static final int REQUEST_FINE_LOCATION_ACCESS = 1;
    private static final String[] PERMISSION_LOCATION = {
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    private final String LOG_TAG = MainActivity.class.getSimpleName();
    private TextView textView;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Button requestUpdatesButton;
    private Button removeUpdateButton;

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver,
                new IntentFilter(Constants.BROADCAST_ACTION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestUpdatesButton= (Button) findViewById(R.id.request_activity_updates_button);
        removeUpdateButton= (Button) findViewById(R.id.remove_activity_updates_button);
        mBroadcastReceiver= new ActivityDetectionBroadcastReceiver();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(ActivityRecognition.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        textView = (TextView) findViewById(R.id.detectedActivities);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
       /* mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(1000);
        //mLocationRequest.setFastestInterval(500);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.e(LOG_TAG,"Permission Not Granted Asking for permission");
            int permission= ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

            if(permission != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this,
                        PERMISSION_LOCATION,
                        REQUEST_FINE_LOCATION_ACCESS);
            }
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this
        );*/
       Log.i(LOG_TAG,"inside onConnected");

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(LOG_TAG,"Connection Suspended");

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(LOG_TAG,"Connection Failed");

    }

    public void requestActivityUpdatesButtonHandler(View view){
        Log.i(LOG_TAG,"insiderequestActivityUpdatesButtonHandler");
        if(!mGoogleApiClient.isConnected()){
            Toast.makeText(this,getString(R.string.not_connected),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(
                mGoogleApiClient,
                Constants.DETECTION_INTERVAL_IN_MILLISECONDS,
                getActivityDetectionPendingIntent()
        ).setResultCallback(this);
        requestUpdatesButton.setEnabled(false);
        removeUpdateButton.setEnabled(true);
    }

    public void removeActivityUpdatesButtonHandler(View view){
        Log.i(LOG_TAG,"inside removeActivityUpdatesButtonHandler");
        {
            if(!mGoogleApiClient.isConnected()){
                Toast.makeText(this,getString(R.string.not_connected),
                        Toast.LENGTH_SHORT).show();
                return;
            }
            ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(
                    mGoogleApiClient,
                    getActivityDetectionPendingIntent()
            ).setResultCallback(this);
            requestUpdatesButton.setEnabled(true);
            removeUpdateButton.setEnabled(false);
        }
    }

    private PendingIntent getActivityDetectionPendingIntent() {
        Intent intent = new Intent(this,DetectedActivitiesIntentService.class);
        return PendingIntent.getService(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(LOG_TAG,"inside onlocationChanged");
        textView.setText(Double.toString(location.getLatitude()));

    }

    public String getActivityString(int detectedActivityType){
        Resources resources = this.getResources();
        switch (detectedActivityType){
            case DetectedActivity.IN_VEHICLE:
                return resources.getString(R.string.in_vehicle);
            case DetectedActivity.ON_BICYCLE:
                return resources.getString(R.string.on_bicycle);
            case DetectedActivity.ON_FOOT:
                return resources.getString(R.string.on_foot);
            case DetectedActivity.RUNNING:
                return resources.getString(R.string.running);
            case DetectedActivity.STILL:
                return resources.getString(R.string.still);
            case DetectedActivity.TILTING:
                return resources.getString(R.string.tilting);
            case DetectedActivity.UNKNOWN:
                return resources.getString(R.string.unknown);
            case DetectedActivity.WALKING:
                return resources.getString(R.string.walking);
            default:
                return resources.getString(R.string.unidentifiable_activity);
        }
    }

    @Override
    public void onResult(@NonNull Status status) {
        if(status.isSuccess()){
            Log.i(LOG_TAG,"Successfully added Activities");
        }else {
            Log.e(LOG_TAG,"Error adding or removing activity "+ status.getStatusMessage());
        }
    }

    public class ActivityDetectionBroadcastReceiver extends BroadcastReceiver {
        protected  final String TAG=ActivityDetectionBroadcastReceiver.class.getSimpleName();

        @Override
        public void onReceive(Context context, Intent intent) {
            ArrayList<DetectedActivity> updatedActivities =
                    intent.getParcelableArrayListExtra(Constants.ACTIVITY_EXTRA);

            String status ="";
            for(DetectedActivity thisActivity:updatedActivities){
                status += getActivityString(thisActivity.getType()) + thisActivity.getConfidence() + "%\n";
            }
            textView.setText(status);

        }

    }
}

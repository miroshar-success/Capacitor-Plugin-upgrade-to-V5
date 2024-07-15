package com.capacitor.plugin.geofencing;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.getcapacitor.JSObject;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeofenceService extends Service implements GeofenceServiceInterface {

    private GeofencingClient geofencingClient;
    private PendingIntent geofencePendingIntent;

    String url;
    boolean notifyOnEntry;
    boolean notifyOnExit;
    JSObject payload;

    private final Map<String, Geofence> geofenceMap = new HashMap<>();

    @Override
    public void onCreate() {
        super.onCreate();

        geofencingClient = LocationServices.getGeofencingClient(this);
        addGeofence();

        // Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter("GEOFENCE_TRANSITION_ACTION");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.registerReceiver(this, mGeofenceReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(mGeofenceReceiver, filter);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            url = intent.getStringExtra("url");
            notifyOnEntry = intent.getBooleanExtra("notifyOnEntry", true);
            notifyOnExit = intent.getBooleanExtra("notifyOnExit", true);
            try {
                payload = new JSObject(intent.getStringExtra("payload"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return START_STICKY;
    }

    private void addGeofence() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        
        // Unregister the BroadcastReceiver
        unregisterReceiver(mGeofenceReceiver);
    }

    public class MyBinder extends Binder {
        GeofenceServiceInterface getService() {
            return GeofenceService.this;
        }
    }

    @Override
    public boolean addRegion(double latitude, double longitude, double radius, String identifier) {
        Geofence geofence = new Geofence.Builder()
                .setRequestId(identifier)
                .setCircularRegion(latitude, longitude, (float) radius)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();

        GeofencingRequest geofencingRequest = new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build();

        // Create an Intent and add extra data (string parameter)
        Intent intent = new Intent(this, GeofenceBroadcastReceiver.class);
        intent.putExtra("identifier", identifier);

        geofencePendingIntent = PendingIntent.getBroadcast(this, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        geofenceMap.put(identifier, geofence);
        geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Geofence added successfully
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to add geofence
                    }
                });

        return true;
    }

    @Override
    public boolean stopMonitoring(String identifier) {
        // Remove the geofence from the geofencing client
        List<String> removeRequestIds = new ArrayList<>();
        removeRequestIds.add(identifier);

        geofencingClient.removeGeofences(removeRequestIds)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Geofence removed successfully
                        geofenceMap.remove(identifier); // Remove from the list
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to remove geofence
                    }
                });

        return true;
    }

    @Override
    public String monitoredRegions() {
        return geofenceMap.values().toString();
    }

    private final BroadcastReceiver mGeofenceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("GEOFENCE_TRANSITION_ACTION".equals(intent.getAction())) {
                String identifier = intent.getStringExtra("identifier");
                boolean enter = intent.getBooleanExtra("enter", false);
                handleEvent(identifier, enter);
            }
        }
    };

    private void handleEvent(String identifier, boolean enter) {
        try {
            payload.put("identifier", identifier);
            payload.put("enter", enter);

            URL backendUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) backendUrl.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);

            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(payload.toString().getBytes());
            outputStream.flush();
            outputStream.close();

            int responseCode = connection.getResponseCode();
            System.out.println("Post request finished with response code: " + responseCode);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}


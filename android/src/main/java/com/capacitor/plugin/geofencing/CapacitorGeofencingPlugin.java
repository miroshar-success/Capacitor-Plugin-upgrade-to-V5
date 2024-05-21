package com.capacitor.plugin.geofencing;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import com.getcapacitor.annotation.Permission;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

@CapacitorPlugin(
        name = "CapacitorGeofencing",
        permissions = {
                @Permission(strings = {Manifest.permission.ACCESS_COARSE_LOCATION}, alias = "coarseLocation"),
                @Permission(strings = {Manifest.permission.ACCESS_FINE_LOCATION}, alias = "fineLocation")
        }
)
public class CapacitorGeofencingPlugin extends Plugin {

    public static CapacitorGeofencingPlugin instance;

    public static final String TAG = "CapGeofencingPlugin";

    private PluginCall currentCall;

    String url;
    boolean notifyOnEntry;
    boolean notifyOnExit;
    JSObject payload;

    GeofenceServiceInterface mBoundService;
    boolean mServiceBound = false;

    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // Service connection established, get the service instance
            GeofenceService.MyBinder binder = (GeofenceService.MyBinder) service;
            mBoundService = binder.getService();
            mServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mServiceBound = false;
        }
    };
    @Override
    public void load() {
        super.load();
        instance = this;
        Log.i(TAG, "LOADed");
    }

    private void startService() {
        Log.i(TAG, "STARTING SERVICE");

        Intent serviceIntent = new Intent(getContext(), GeofenceService.class);
        serviceIntent.putExtra("url", url);
        serviceIntent.putExtra("notifyOnEntry", notifyOnEntry);
        serviceIntent.putExtra("notifyOnExit", notifyOnExit);
        serviceIntent.putExtra("payload", payload.toString());

        if (Build.VERSION.SDK_INT >= 26) {
            Log.i(TAG, "Hit foreground start");
            getContext().startForegroundService(serviceIntent);
        } else {
            Log.i(TAG, "Hit regular start");
            getContext().startService(serviceIntent);
        }
        final boolean success = getContext().bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);

        if (this.currentCall != null) {
            if (success)
                this.currentCall.resolve();
            else
                this.currentCall.reject("Failed to start service");
            this.currentCall = null;
        }
    }

    @Override
    protected void handleRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.handleRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.e(TAG, "got permission result");
        if (this.currentCall == null) {
            return;
        }

        for (int result : grantResults) {
            if (result == PackageManager.PERMISSION_DENIED) {
                this.currentCall.error("User denied location permission");
                this.currentCall = null;
                Log.e(TAG, "User denied permission to location");
                return;
            }
        }

        this.startService();
    }

    @PluginMethod
    public void setup(PluginCall call) {
        this.currentCall = call;

        url = call.getString("url");
        notifyOnEntry = Boolean.TRUE.equals(call.getBoolean("notifyOnEntry", Boolean.TRUE));
        notifyOnExit = Boolean.TRUE.equals(call.getBoolean("notifyOnExit", true));
        payload = call.getObject("payload");

        if (url == null || payload == null) {
            call.reject("Must provide url and payload.");
            return;
        }

        if (super.hasRequiredPermissions()) {
            this.startService();
        } else {
            Log.i(TAG, "requesting necessary permissions");
            super.pluginRequestAllPermissions();
        }

        call.resolve();
    }

    @PluginMethod
    public void addRegion(PluginCall call) {
        Double latitude = call.getDouble("latitude");
        Double longitude = call.getDouble("longitude");
        Double radius = call.getDouble("radius", 50.0);
        String identifier = call.getString("identifier");

        if (latitude == null || longitude == null || identifier == null || radius == null) {
            call.reject("Must provide latitude, longitude, and identifier.");
            return;
        }

        boolean success = mBoundService.addRegion(latitude, longitude, radius, identifier);
        if (success) {
            call.resolve();
        } else {
            call.reject("Could not start monitoring the region.");
        }
    }

    @PluginMethod
    public void stopMonitoring(PluginCall call) {
        Log.i(TAG, "stopping service");

        String identifier = call.getString("identifier");
        if (identifier == null) {
            call.reject("Must provide identifier.");
        }
        boolean success = mBoundService.stopMonitoring(identifier);
        if (success) {
            call.resolve();
        } else {
            call.reject("Could not find a region with that identifer.");
        }
    }

    @PluginMethod
    public void monitoredRegions(PluginCall call) {
        JSObject result = new JSObject();
        result.put("regions", mBoundService.monitoredRegions());
        call.resolve(result);
    }
}

package com.capacitor.plugin.geofencing;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        String identifier = intent.getStringExtra("identifier");

        if (geofencingEvent.hasError()) {
            return;
        }

        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            Intent broadcastIntent = new Intent("GEOFENCE_TRANSITION_ACTION");
            broadcastIntent.putExtra("identifier", identifier);
            broadcastIntent.putExtra("enter", true);
            context.sendBroadcast(broadcastIntent);
        } else if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            Intent broadcastIntent = new Intent("GEOFENCE_TRANSITION_ACTION");
            broadcastIntent.putExtra("identifier", identifier);
            broadcastIntent.putExtra("enter", false);
            context.sendBroadcast(broadcastIntent);
        }
    }

}

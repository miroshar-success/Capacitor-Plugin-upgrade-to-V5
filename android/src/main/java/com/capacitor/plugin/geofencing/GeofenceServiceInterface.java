package com.capacitor.plugin.geofencing;

public interface GeofenceServiceInterface {
    boolean addRegion(double latitude, double longitude, double radius, String identifier);
    String monitoredRegions();
    boolean stopMonitoring(String identifier);
}

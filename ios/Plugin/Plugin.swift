import Foundation
import Capacitor

/**
 * Please read the Capacitor iOS Plugin Development Guide
 * here: https://capacitorjs.com/docs/plugins/ios
 */
@objc(CapacitorGeofencingPlugin)
public class CapacitorGeofencingPlugin: CAPPlugin {
    @objc func setup(_ call: CAPPluginCall) {
        // Check if all properties are present
        guard let backendUrl = call.getString("url") else {
            call.reject("Must provide url.")
            return
        }
        guard let notifyOnEntry = call.getBool("notifyOnEntry") else {
            call.reject("Must provide notifyOnEntry.")
            return
        }
        guard let notifyOnExit = call.getBool("notifyOnExit") else {
            call.reject("Must provide notifyOnExit.")
            return
        }
        guard let payload = call.getObject("payload") else {
            call.reject("Must provide payload.")
            return
        }
        
        guard let url = URL(string: backendUrl) else {
            call.reject("Given url isn't valid.")
            return
        }
        
        GeofenceManager.shared.backendUrl = url
        GeofenceManager.shared.notifyOnEntry = notifyOnEntry
        GeofenceManager.shared.notifyOnExit = notifyOnExit
        GeofenceManager.shared.payload = payload
        GeofenceManager.shared.requestAlwaysAuthorization { (success) in
            if success {
                call.resolve()
            } else {
                call.reject("User did not give 'alwaysAuthorization' permission.")
            }
        }
    }
    
    @objc func addRegion(_ call: CAPPluginCall) {
        // Check if all properties are present
        guard let lat = call.getDouble("latitude") else {
            call.reject("Must provide latitude.")
            return
        }
        guard let lng = call.getDouble("longitude") else {
            call.reject("Must provide longitude.")
            return
        }
        guard let identifer = call.getString("identifier") else {
            call.reject("Must provide identifier.")
            return
        }
        let radius = call.getDouble("radius") ?? 50
        
        let region = GeofenceManager.shared.geofenceRegion(lat: lat, lng: lng, radius: radius, identifier: identifer)
        GeofenceManager.shared.startMonitoring(region: region)
            ? call.resolve()
            : call.reject("Could not start monitoring the region.")
    }
    
    @objc func stopMonitoring(_ call: CAPPluginCall) {
        guard let identifier = call.getString("identifier") else {
            call.reject("Must provide identifier.")
            return
        }
        GeofenceManager.shared.stopMonitoring(identifier: identifier)
            ? call.resolve()
            : call.reject("Could not find a region with that identifer.")
    }
    
    @objc func monitoredRegions(_ call: CAPPluginCall) {
        call.resolve([
            "regions": GeofenceManager.shared.monitoredRegions()
        ])
    }

}

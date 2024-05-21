#import <Foundation/Foundation.h>
#import <Capacitor/Capacitor.h>

// Define the plugin using the CAP_PLUGIN Macro, and
// each method the plugin supports using the CAP_PLUGIN_METHOD macro.
CAP_PLUGIN(CapacitorGeofencingPlugin, "CapacitorGeofencing",
           CAP_PLUGIN_METHOD(setup, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(addRegion, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(stopMonitoring, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(monitoredRegions, CAPPluginReturnPromise);
)

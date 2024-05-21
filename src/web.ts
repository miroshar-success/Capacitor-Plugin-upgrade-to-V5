import { WebPlugin } from '@capacitor/core';

import type { CapacitorGeofencingPlugin } from './definitions';

export class CapacitorGeofencingWeb extends WebPlugin implements CapacitorGeofencingPlugin {
  async setup(options: { url: string, notifyOnEntry: boolean, notifyOnExit: boolean, payload: Record<string, unknown> }): Promise<{value: string}> {
    console.log('setup', options);
    return {value: "success"};
  }

  async addRegion(options: { latitude: number, longitude: number, radius?: number, identifier: string }): Promise<{value: string}> {
    console.log('addRegion', options);
    return {value: "success"};
  }

  async stopMonitoring(options: { identifier: string }): Promise<{value: string}> {
    console.log('stopMonitoring', options);
    return {value: "success"};
  }

  async monitoredRegions(): Promise<{value: string}> {
    console.log('monitoredRegions');
    return {value: "success"};
  }
}

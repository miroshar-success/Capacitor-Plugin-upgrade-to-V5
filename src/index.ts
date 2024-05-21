import { registerPlugin } from '@capacitor/core';

import type { CapacitorGeofencingPlugin } from './definitions';

const CapacitorGeofencing = registerPlugin<CapacitorGeofencingPlugin>('CapacitorGeofencing', {
  web: () => import('./web').then(m => new m.CapacitorGeofencingWeb()),
});

export * from './definitions';
export { CapacitorGeofencing };

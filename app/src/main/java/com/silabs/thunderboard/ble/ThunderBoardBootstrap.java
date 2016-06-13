package com.silabs.thunderboard.ble;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.RemoteException;

import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.startup.BootstrapNotifier;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class ThunderBoardBootstrap {

    private BeaconManager beaconManager;
    private Context context;
    private RangeNotifier rangeNotifier;
    private List<Region> regions;
    private boolean disabled = false;
    private BeaconConsumer beaconConsumer;

    public ThunderBoardBootstrap(Context context, RangeNotifier rangeNotifier, Region region) {
        this.context = context;
        this.rangeNotifier = rangeNotifier;
        beaconManager = BeaconManager.getInstanceForApplication(context);
        regions = new ArrayList<>();
        regions.add(region);
        beaconConsumer = new InternalBeaconConsumer();
        beaconManager.bind(beaconConsumer);
        Timber.d("Waiting for BeaconService connection");
    }

    public ThunderBoardBootstrap(Context context, RangeNotifier rangeNotifier, List<Region> regions) {
        this.context = context;
        beaconManager = BeaconManager.getInstanceForApplication(context);
        this.regions = regions;
        beaconConsumer = new InternalBeaconConsumer();
        beaconManager.bind(beaconConsumer);
        Timber.d("Waiting for BeaconService connection");
    }

    /**
     * Used to disable additional bootstrap callbacks after the first is received.  Unless this is called,
     * your application will be get additional calls as the supplied regions are entered or exited.
     */
    public void disable() {
        Timber.d("Disabling region bootstrap");
        if (disabled) {
            return;
        }
        disabled = true;
        try {
            for (Region region : regions) {
                beaconManager.stopMonitoringBeaconsInRegion(region);
            }
        } catch (RemoteException e) {
            Timber.d("Can't stop bootstrap regions");
        }
        beaconManager.unbind(beaconConsumer);
    }

    private class InternalBeaconConsumer implements BeaconConsumer {

        /**
         * Method reserved for system use
         */
        @Override
        public void onBeaconServiceConnect() {

            Timber.d("Activating background region monitoring");
            beaconManager.setRangeNotifier(rangeNotifier);
            try {
                Identifier id1 = Identifier.parse("cef797da-2e91-4ea4-a424-f45082ac0682");
                beaconManager.startRangingBeaconsInRegion(new Region("ThunderBoardUniqueId", id1, null, null));
            } catch (RemoteException e) {
                Timber.d("Can't set up bootstrap regions, exception: %s", e.getMessage());
            }
        }

        /**
         * Method reserved for system use
         */
        @Override
        public boolean bindService(Intent intent, ServiceConnection conn, int arg2) {
            Timber.d("intent: %s", intent.toString());
            return context.bindService(intent, conn, arg2);
        }

        /**
         * Method reserved for system use
         */
        @Override
        public Context getApplicationContext() {
            return context;
        }

        /**
         * Method reserved for system use
         */
        @Override
        public void unbindService(ServiceConnection conn) {
            context.unbindService(conn);
        }
    }

}

package com.silabs.thunderboard.settings.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.silabs.thunderboard.R;
import com.silabs.thunderboard.common.data.PreferenceManager;
import com.silabs.thunderboard.common.data.model.ThunderBoardPreferences;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import timber.log.Timber;

/**
 * Adapter for displaying beacons in the BeaconNotificationsActivity RecyclerView.
 * There are two kinds of items for this adapter, header and beacon, each of
 * which have their own ViewHolders.
 */
public class BeaconNotificationsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final static int ALLOWED_HEADER = 0;
    private final static int ALLOWED_BEACON = 1;
    private final static int OTHER_HEADER = 2;
    private final static int OTHER_BEACON = 3;
    private final static int NO_ITEM = 4;

    private Context context;
    private PreferenceManager preferenceManager;
    private List<ThunderBoardPreferences.Beacon> allowedDevices;
    private List<ThunderBoardPreferences.Beacon> otherDevices;
    private String connectedDeviceAddress;

    public BeaconNotificationsAdapter(Context context,
                                      PreferenceManager preferenceManager,
                                      List<ThunderBoardPreferences.Beacon> allowedDevices,
                                      List<ThunderBoardPreferences.Beacon> otherDevices) {
        this.context = context;
        this.preferenceManager = preferenceManager;
        this.allowedDevices = allowedDevices;
        this.otherDevices = otherDevices;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        LayoutInflater inflater = LayoutInflater.from(context);

        if (viewType == ALLOWED_HEADER) {
            view = inflater.inflate(R.layout.listitem_beacon_header_allowed, parent, false);
            return new BeaconHeaderViewHolder(view);
        } else if (viewType == ALLOWED_BEACON || viewType == OTHER_BEACON) {
            view = inflater.inflate(R.layout.listitem_beacon, parent, false);
            return new BeaconViewHolder(view);
        } else if (viewType == OTHER_HEADER) {
            view = inflater.inflate(R.layout.listitem_beacon_header_other, parent, false);
            return new BeaconHeaderViewHolder(view);
        } else {
            return null;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        final ThunderBoardPreferences.Beacon beacon;

        if (getItemViewType(position) == ALLOWED_BEACON) {
            beacon = allowedDevices.get(position - 1);
            BeaconViewHolder viewHolder = (BeaconViewHolder) holder;
            viewHolder.beaconNameText.setText(beacon.deviceName);
            viewHolder.beaconAction.setText(R.string.settings_remove);
            viewHolder.showConnectedState((connectedDeviceAddress != null && connectedDeviceAddress.equals(beacon.deviceAddress)));
            viewHolder.beaconAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    beacon.allowNotifications = false;
                    allowedDevices.remove(beacon);
                    ThunderBoardPreferences prefs = preferenceManager.getPreferences();
                    prefs.beacons.remove(beacon.deviceAddress);
                    preferenceManager.setPreferences(prefs);
                    notifyDataSetChanged();
                }
            });
        } else if (getItemViewType(position) == OTHER_BEACON) {
            beacon = otherDevices.get((allowedDevices.size() == 0) ? position - 2 : position - allowedDevices.size() - 2);
            BeaconViewHolder viewHolder = (BeaconViewHolder) holder;
            viewHolder.hideConnectedState();
            viewHolder.beaconNameText.setText((beacon.deviceName == null) ? "n/a" : beacon.deviceName);
            viewHolder.beaconAction.setText(R.string.settings_allow);
            viewHolder.showConnectedState((connectedDeviceAddress != null && connectedDeviceAddress.equals(beacon.deviceAddress)));
            viewHolder.beaconAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    beacon.allowNotifications = true;
                    allowedDevices.add(beacon);
                    otherDevices.remove(beacon);
                    ThunderBoardPreferences prefs = preferenceManager.getPreferences();
                    prefs.beacons.put(beacon.deviceAddress, beacon);
                    preferenceManager.setPreferences(prefs);
                    notifyDataSetChanged();
                }
            });
            Timber.d("beacon address: %s, name: %s", beacon.deviceAddress, beacon.deviceName);
        }
    }

    /**
     * getItemViewType
     *
     * Determines the type of item, based on the position in the RecyclerView.
     *
     * There are two lists of devices. The first list is allowedDevices, and this
     * method checks if there are any of these. If so, then position 0 is the allowed
     * header and the subsequent devices (positions 1, 2, 3, etc) are of allowed beacon type.
     *
     * For the other devices list, we first check to see if were any allowed devices. If not,
     * then position 0 is for the other header and positions 1, 2, 3, etc are for other beacon
     * types. Otherwise, position allowedSize + 1 is the other header type and the remaining
     * items are other beacon types.
     *
     * @param position
     * @return type (ALLOWED_HEADER, ALLOWED_BEACON, OTHER_HEADER, or OTHER_BEACON)
     */
    @Override
    public int getItemViewType(int position) {
        int allowedSize = allowedDevices.size();
        int otherSize = otherDevices.size();

        if (position == 0) {
            return ALLOWED_HEADER;
        } else if (allowedSize > 0 && position <= allowedSize) {
            return ALLOWED_BEACON;
        }

        if (otherSize > 0) {
            if (allowedSize > 0) {
                if (position == allowedSize + 1) {
                    return OTHER_HEADER;
                } else {
                    return OTHER_BEACON;
                }

            } else {
                if (position == 1) {
                    return OTHER_HEADER;
                } else {
                    return OTHER_BEACON;
                }
            }
        } else {
            return OTHER_HEADER;
        }

        // requirement to always show both headers even on empty lists
        // return NO_ITEM;
    }

    /**
     * getItemCount
     *
     * Since there are two lists of devices, we need to add up the number of devices in both
     * lists, and count also their headers.
     *
     * @return the total number of items to be inflated in this adapter.
     *
     */
    @Override
    public int getItemCount() {
        int numItems = 0;

        if (allowedDevices.size() > 0) {
            numItems += (allowedDevices.size() + 1);
        } else {
            // always display header
            numItems = 1;
        }
        if (otherDevices.size() > 0) {
            numItems += (otherDevices.size() + 1);
        } else {
            // always display header
            numItems++;
        }

        return numItems;
    }

    public void setConnectedDeviceAddress(String connectedDeviceAddress) {
        this.connectedDeviceAddress = connectedDeviceAddress;
    }

    public static class BeaconHeaderViewHolder extends RecyclerView.ViewHolder {

        public BeaconHeaderViewHolder(View itemView) {
            super(itemView);
        }
    }

    public static class BeaconViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.beacon_state)
        public TextView beaconStateText;

        @Bind(R.id.beacon_name)
        public TextView beaconNameText;

        @Bind(R.id.beacon_action)
        public TextView beaconAction;

        public BeaconViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        private void showConnectedState(boolean connected) {
            beaconStateText.setVisibility(View.VISIBLE);
            beaconStateText.setText(connected ? R.string.settings_connected : R.string.settings_not_connected);
        }

        private void hideConnectedState() {
            beaconStateText.setVisibility(View.INVISIBLE);
        }

    }
}

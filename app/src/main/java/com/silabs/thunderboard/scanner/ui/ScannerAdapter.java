package com.silabs.thunderboard.scanner.ui;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.silabs.thunderboard.R;
import com.silabs.thunderboard.ble.model.ThunderBoardDevice;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Adapter class for the recycler view in ScannerActivity, to display list of available devices
 */
public class ScannerAdapter extends RecyclerView.Adapter<ScannerAdapter.DeviceHolder> {

    private List<ThunderBoardDevice> dataSet;
    private OnDeviceItemClickListener listener;

    public ScannerAdapter(List<ThunderBoardDevice> dataSet) {
        this.dataSet = dataSet;
    }

    // RecyclerView.Adapter

    @Override
    public int getItemCount() {
        if (dataSet == null) {
            return 0;
        } else {
            return dataSet.size();
        }
    }

    @Override
    public DeviceHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.listitem_device, parent, false);
        return new DeviceHolder(view);
    }

    @Override
    public void onBindViewHolder(DeviceHolder holder, int position) {

        final ThunderBoardDevice device = dataSet.get(position);
        holder.deviceName.setText(device.getName());
        holder.deviceAddress.setText(device.getAddress());
        holder.signalStrengthIndicator.setSignalStrength(device.getRssi());

        holder.rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onDeviceItemClick(v, device);
                }
            }
        });
    }

    public void updateDataSet(List<ThunderBoardDevice> devices) {
        this.dataSet = devices;
        notifyDataSetChanged();
    }

    public void setListener(OnDeviceItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnDeviceItemClickListener {
        void onDeviceItemClick(View view, ThunderBoardDevice device);
    }

    public static class DeviceHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.device_name)
        TextView deviceName;

        @Bind(R.id.device_address)
        TextView deviceAddress;

        @Bind(R.id.signal_strength)
        SignalStrengthIndicator signalStrengthIndicator;

        View rootView;

        public DeviceHolder(View view) {
            super(view);
            rootView = view;
            ButterKnife.bind(this, view);
        }
    }
}

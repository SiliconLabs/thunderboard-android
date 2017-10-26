package com.silabs.thunderboard.demos.ui;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.silabs.thunderboard.R;
import com.silabs.thunderboard.common.app.ThunderBoardConstants;
import com.silabs.thunderboard.demos.model.Demo;

import java.util.List;

/**
 * Adapter for the RecyclerView in DemosSelectionActivity
 */
public class DemosAdapter extends RecyclerView.Adapter<DemoViewHolder> {

    private List<Demo> demosList;
    private Activity activity;

    private final String deviceAddress;

    public DemosAdapter(Activity activity, List<Demo> list, String deviceAddress) {
        this.activity = activity;
        this.demosList = list;
        this.deviceAddress = deviceAddress;
    }

    @Override
    public DemoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(activity);
        View view = inflater.inflate(R.layout.listitem_demo, parent, false);

        return new DemoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DemoViewHolder holder, int position) {
        final Demo demo = demosList.get(position);

        holder.demoLabel.setText(demo.demoName);
        holder.demoIcon.setImageResource(demo.demoImageResource);

        holder.rootView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activity, demo.demoClass);
                intent.putExtra(ThunderBoardConstants.EXTRA_DEVICE_ADDRESS, deviceAddress);
                activity.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return demosList.size();
    }
}

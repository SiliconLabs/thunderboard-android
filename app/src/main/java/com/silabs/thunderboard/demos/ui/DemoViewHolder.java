package com.silabs.thunderboard.demos.ui;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.silabs.thunderboard.R;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * View holder for DemosAdapter
 */
public class DemoViewHolder extends RecyclerView.ViewHolder {

    @Bind(R.id.demo_label)
    TextView demoLabel;

    @Bind(R.id.demo_icon)
    ImageView demoIcon;

    View rootView;

    public DemoViewHolder(View itemView) {
        super(itemView);
        rootView = itemView;
        ButterKnife.bind(this, itemView);
    }
}

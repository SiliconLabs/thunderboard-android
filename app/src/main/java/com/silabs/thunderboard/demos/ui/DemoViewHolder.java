package com.silabs.thunderboard.demos.ui;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.silabs.thunderboard.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * View holder for DemosAdapter
 */
public class DemoViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.demo_label)
    TextView demoLabel;

    @BindView(R.id.demo_icon)
    ImageView demoIcon;

    View rootView;

    public DemoViewHolder(View itemView) {
        super(itemView);
        rootView = itemView;
        ButterKnife.bind(this, itemView);
    }
}

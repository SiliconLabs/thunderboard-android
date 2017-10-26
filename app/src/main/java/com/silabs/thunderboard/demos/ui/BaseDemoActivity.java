package com.silabs.thunderboard.demos.ui;

import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.silabs.thunderboard.BuildConfig;
import com.silabs.thunderboard.R;
import com.silabs.thunderboard.common.app.ThunderBoardConstants;
import com.silabs.thunderboard.common.data.PreferenceManager;
import com.silabs.thunderboard.common.data.model.ThunderBoardPreferences;
import com.silabs.thunderboard.common.ui.ThunderBoardActivity;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import timber.log.Timber;

public abstract class BaseDemoActivity extends ThunderBoardActivity implements BaseDemoViewListener {

    @Inject
    PreferenceManager prefsManager;

    /**
     * The toolbar at the top of the activity
     */
    protected Toolbar toolbar;

    /**
     * Container for motion, environment, and i/o layouts
     */
    protected FrameLayout mainSection;

    protected Switch streamingSwitch;
    protected TextView streamingIndicator;

    protected String deviceAddress;

    private Menu menu;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo_base);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        mainSection = (FrameLayout) findViewById(R.id.main_section);

        setupToolbar();
        deviceAddress = getIntent().getStringExtra(ThunderBoardConstants.EXTRA_DEVICE_ADDRESS);

        streamingSwitch = (Switch) findViewById(R.id.streaming_switch);
        streamingIndicator = (TextView) findViewById(R.id.streaming_indicator);

        streamingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean buttonState) {
                setMenuItemsEnabled(buttonState);
                if (buttonState) {
                    getDemoPresenter().startStreaming();
                    streamingIndicator.setText(R.string.demo_streaming_to_cloud);
                } else {
                    getDemoPresenter().stopStreaming();
                    streamingIndicator.setText(R.string.demo_stream_to_cloud);
                }
            }
        });
        streamingIndicator.setText(streamingSwitch.isChecked() ? R.string.demo_streaming_to_cloud : R.string.demo_stream_to_cloud);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_demo, menu);
        this.menu = menu;
        setMenuItemsEnabled(streamingSwitch.isChecked());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_share) {
            shareURL();
            return true;
        } else if (id == R.id.action_open_in_browser) {
            launchBrowser();
            return true;
        } else if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        getDemoPresenter().clearViewListener();
        super.onDestroy();
    }

    // ThunderBoardActivity

    @Override
    public void onBluetoothDisabled() {
        finish();
    }

    protected String getSharedUrl() {
        return getDemoPresenter().getSharedUrl();
    }

    // BaseDemoViewListener

    @Override
    public void onWifi(boolean isConnected) {
        if (!isConnected) {
            Toast.makeText(this, "No Wi-Fi", Toast.LENGTH_SHORT);
        }
    }

    public void onDisconnected() {
        getDemoPresenter().clearViewListener();
    }

    protected void setupToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setBackgroundColor(getToolbarColor());
        toolbar.setTitle(getToolbarString());

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) toolbar.getLayoutParams();
        params.height += getStatusBarHeight();
        toolbar.setLayoutParams(params);

        toolbar.setPadding(0, getStatusBarHeight(), 0, 0);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    protected abstract int getToolbarColor();

    protected abstract String getToolbarString();

    protected abstract BaseDemoPresenter getDemoPresenter();

    protected abstract void initControls();

    /**
     * setMenuItemsStreaming
     *
     * Makes the share and open in browser icons appear or
     * disappear, based on whether the streaming is true of false.
     *
     * @param isStreaming
     *
     */
    private void setMenuItemsEnabled(boolean isStreaming) {
        menu.findItem(R.id.action_share).setVisible(isStreaming);
        menu.findItem(R.id.action_open_in_browser).setVisible(isStreaming);
    }

    /**
     * isBadString
     *
     * Checks to see if a string is valid
     *
     * @param string
     * @return true if the input string is not valid
     */
    private boolean isBadString(String string) {
        return TextUtils.isEmpty(string) || TextUtils.equals(string, "null");
    }

    /**
     * shareURL
     *
     * Generates a message that is sent out via email, chat, social media, etc.
     * This message contains the URL to the website that holds the streaming
     * data, as well as a signature block.
     *
     */
    private void shareURL() {
        ThunderBoardPreferences prefs = prefsManager.getPreferences();

        String signature = "\n\n"
                + (!isBadString(prefs.userName) ? (prefs.userName + "\n") : "")
                + (!isBadString(prefs.userTitle) ? (prefs.userTitle + "\n") : "")
                + (!isBadString(prefs.userEmail) ? (prefs.userEmail + "\n") : "")
                + (!isBadString(prefs.userPhone) ? prefs.userPhone : "");
        String url = getSharedUrl();
        String message = String.format(getString(R.string.share_message), url, BuildConfig.MICROSITE_URL) + signature;

        List<Intent> targetedSharedIntents = new ArrayList<Intent>();
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");

        List<ResolveInfo> resInfo = getPackageManager().queryIntentActivities(shareIntent, 0);
        if (!resInfo.isEmpty()) {
            for (ResolveInfo resolveInfo: resInfo) {
                String packageName = resolveInfo.activityInfo.packageName;
                Timber.d("package: " + packageName);

                Intent targetedSharedIntent = new Intent(Intent.ACTION_SEND);
                targetedSharedIntent.setType("text/plain");

                if (prefs.userCCSelf) {
                    targetedSharedIntent.putExtra(Intent.EXTRA_CC, prefs.userEmail);
                }

                if (TextUtils.equals(packageName, "com.google.android.apps.docs")) {
                    // copy the url to the clipboard
                    targetedSharedIntent.putExtra(Intent.EXTRA_TEXT, url);
                } else {
                    // everything else...
                    targetedSharedIntent.putExtra(Intent.EXTRA_TEXT, message);
                }

                targetedSharedIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_subject));

                targetedSharedIntent.setPackage(packageName);
                targetedSharedIntents.add(targetedSharedIntent);
            }

            Intent chooserIntent = Intent.createChooser(targetedSharedIntents.remove(0), getString(R.string.share_link));
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetedSharedIntents.toArray(new Parcelable[]{}));
            startActivity(chooserIntent);
        }
    }

    /**
     * launchBrowser
     *
     * Launches a browser intent with the URL to the website holding the streaming
     * data. The browser that gets launched is the default browser for the device.
     *
     */
    private void launchBrowser() {
        Uri uri = Uri.parse(getSharedUrl());
        if (uri != null) {
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }
    }
}

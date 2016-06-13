package com.silabs.thunderboard.settings.ui;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ToggleButton;

import com.silabs.thunderboard.R;
import com.silabs.thunderboard.common.app.ThunderBoardApplication;
import com.silabs.thunderboard.common.data.PreferenceManager;
import com.silabs.thunderboard.common.data.model.ThunderBoardPreferences;
import com.silabs.thunderboard.common.injection.component.ActivityComponent;
import com.silabs.thunderboard.common.injection.component.DaggerActivityComponent;
import com.silabs.thunderboard.common.ui.ThunderBoardActivity;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import timber.log.Timber;

public class SettingsEditActivity extends ThunderBoardActivity {

    @Inject
    PreferenceManager prefsManager;

    @Bind(R.id.settings_edit_toolbar)
    Toolbar toolbar;

    @Bind(R.id.settings_edit_name)
    EditText editName;

    @Bind(R.id.settings_edit_title)
    EditText editTitle;

    @Bind(R.id.settings_edit_email)
    EditText editEmail;

    @Bind(R.id.settings_edit_phone)
    EditText editPhone;

    @Bind(R.id.settings_edit_self_cc)
    ToggleButton editCCSelf;

    private ActivityComponent component;
    private ThunderBoardPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_edit);
        ButterKnife.bind(this);
        component().inject(this);

        setSupportActionBar(toolbar);
        toolbar.setBackgroundColor(getResourceColor(R.color.primary_color));
        toolbar.setTitle(R.string.settings_personal_info);

        changeStatusBarColor(getResourceColor(R.color.primary_color));
    }

    @Override
    public void onResume() {
        super.onResume();
        loadPersonalInfo();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.cancel) {
            finish();
            return true;
        } else if (id == R.id.ok) {
            saveSettings();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void saveSettings() {

        preferences.userName = editName.getText().toString();
        preferences.userTitle = editTitle.getText().toString();
        preferences.userEmail = editEmail.getText().toString();
        preferences.userPhone = editPhone.getText().toString();

        preferences.userCCSelf = editCCSelf.isChecked();

        prefsManager.setPreferences(preferences);
        finish();
    }

    private void loadPersonalInfo() {
        preferences = prefsManager.getPreferences();
        Timber.d("prefs: %s", preferences.toString());

        editName.setText(preferences.userName);
        editTitle.setText(preferences.userTitle);
        editEmail.setText(preferences.userEmail);
        editPhone.setText(preferences.userPhone);

        editCCSelf.setChecked(preferences.userCCSelf);
    }

    @Override
    protected ActivityComponent component() {
        if (component == null) {
            component = DaggerActivityComponent.builder()
                    .thunderBoardComponent(((ThunderBoardApplication) getApplication()).component())
                    .build();
        }
        return component;
    }
}

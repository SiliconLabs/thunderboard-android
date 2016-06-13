package com.silabs.thunderboard.common.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.silabs.thunderboard.common.data.model.ThunderBoardPreferences;
import com.silabs.thunderboard.common.injection.qualifier.ForApplication;

import java.util.HashMap;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Singleton;

import timber.log.Timber;

@Singleton
public class PreferenceManager {

    private static final String PREFERENCES_KEY = "ThunderBoard";
    private static final String PREFERENCES_CONTENT = PreferenceManager.class.getSimpleName() + "preferences";

    private final SharedPreferences sharedPreferences;
    private final Locale locale;

    private ThunderBoardPreferences preferences;

    @Inject
    public PreferenceManager(@ForApplication Context context) {
        this.locale = context.getResources().getConfiguration().locale;
        this.sharedPreferences = context.getSharedPreferences(PREFERENCES_KEY, Context.MODE_PRIVATE);
        this.preferences = getPreferences();
        Timber.d(preferences.toString());
    }

    public ThunderBoardPreferences getPreferences() {
        if (preferences != null) {
            return preferences;
        } else {
            String jsonString = sharedPreferences.getString(PREFERENCES_CONTENT, null);
            if (jsonString == null) {
                return new ThunderBoardPreferences(locale);
            } else {
                return new Gson().fromJson(jsonString, ThunderBoardPreferences.class);
            }
        }
    }

    public void setPreferences(ThunderBoardPreferences preferences) {
        Timber.d(preferences.toString());
        this.preferences = preferences;
        sharedPreferences.edit().putString(PREFERENCES_CONTENT, new Gson().toJson(preferences)).commit();
    }

    public void clear() {
        if (sharedPreferences != null) {
            sharedPreferences.edit().clear().commit();
        }
    }

    public void addConnected(String deviceAddress, String deviceName) {
        if (preferences.beacons == null) {
            preferences.beacons = new HashMap<>();
        }
        if (!preferences.beacons.containsKey(deviceAddress)) {
            preferences.beacons.put(deviceAddress, new ThunderBoardPreferences.Beacon(deviceAddress, deviceName, false));
            String jsonString = new Gson().toJson(preferences);
            Timber.d(jsonString);
            sharedPreferences.edit().putString(PREFERENCES_CONTENT, jsonString).apply();
        }
    }
}

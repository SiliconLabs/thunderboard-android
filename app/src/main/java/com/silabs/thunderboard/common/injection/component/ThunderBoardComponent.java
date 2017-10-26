package com.silabs.thunderboard.common.injection.component;

import com.silabs.thunderboard.common.app.ThunderBoardApplication;
import com.silabs.thunderboard.web.CloudManager;
import com.silabs.thunderboard.ble.BleManager;
import com.silabs.thunderboard.common.data.PreferenceManager;
import com.silabs.thunderboard.common.injection.module.ThunderBoardModule;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {ThunderBoardModule.class})
public interface ThunderBoardComponent {
    BleManager provideBleManager();
    PreferenceManager providePreferenceManager();
    CloudManager provideCloudManager();

    void inject(ThunderBoardApplication o);
}

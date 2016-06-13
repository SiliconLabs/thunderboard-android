package com.silabs.thunderboard.common.injection.component;

import com.silabs.thunderboard.demos.ui.DemoEnvironmentActivity;
import com.silabs.thunderboard.common.injection.scope.ActivityScope;
import com.silabs.thunderboard.common.ui.ThunderBoardStatusFragment;
import com.silabs.thunderboard.demos.ui.DemoIOActivity;
import com.silabs.thunderboard.demos.ui.DemoMotionActivity;
import com.silabs.thunderboard.demos.ui.DemosSelectionActivity;
import com.silabs.thunderboard.scanner.ui.ScannerActivity;
import com.silabs.thunderboard.settings.ui.BeaconNotificationsActivity;
import com.silabs.thunderboard.settings.ui.SettingsActivity;
import com.silabs.thunderboard.settings.ui.SettingsEditActivity;

import dagger.Component;

@ActivityScope
@Component(dependencies = ThunderBoardComponent.class)
public interface ActivityComponent {
    void inject(ScannerActivity object);
    void inject(SettingsActivity object);
    void inject(SettingsEditActivity object);
    void inject(DemosSelectionActivity object);
    void inject(ThunderBoardStatusFragment object);
    void inject(DemoIOActivity object);
    void inject(DemoMotionActivity object);
    void inject(DemoEnvironmentActivity object);
    void inject(BeaconNotificationsActivity object);
}

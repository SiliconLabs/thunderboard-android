package com.silabs.thunderboard.common.injection.module;

import android.content.Context;

import com.silabs.thunderboard.common.injection.qualifier.ForCloudData;
import com.silabs.thunderboard.common.injection.qualifier.ForApplication;
import com.silabs.thunderboard.common.injection.qualifier.ForCloudDemo;
import com.silabs.thunderboard.common.injection.qualifier.ForCloudKeyFirebase;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class ThunderBoardModule {

    private final Context context;
    private final String cloudDataUrl;
    private final String cloudDemoUrl;
    private final String keyFirebase;

    public ThunderBoardModule(Context context, String cloudDataUrl, String cloudDemoUrl, String keyFirebase) {
        this.context = context;
        this.cloudDataUrl = cloudDataUrl;
        this.cloudDemoUrl = cloudDemoUrl;
        this.keyFirebase = keyFirebase;
    }

    @Provides
    @Singleton
    @ForApplication
    Context provideContext() {
        return context;
    }

    @Provides
    @Singleton
    @ForCloudData
    String provideCloudDataUrl() {
        return cloudDataUrl;
    }

    @Provides
    @Singleton
    @ForCloudDemo
    String provideCloudDemoUrl() {
        return cloudDemoUrl;
    }

    @Provides
    @Singleton
    @ForCloudKeyFirebase
    String provideKeyFirebase() {
        return keyFirebase;
    }

}

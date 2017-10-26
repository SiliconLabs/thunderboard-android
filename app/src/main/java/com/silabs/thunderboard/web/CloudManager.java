package com.silabs.thunderboard.web;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.CountDownTimer;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.FirebaseException;
import com.silabs.thunderboard.ble.ThunderBoardSensor;
import com.silabs.thunderboard.common.data.PreferenceManager;
import com.silabs.thunderboard.common.data.model.ThunderBoardPreferences;
import com.silabs.thunderboard.common.injection.qualifier.ForApplication;
import com.silabs.thunderboard.common.injection.qualifier.ForCloudData;
import com.silabs.thunderboard.common.injection.qualifier.ForCloudDemo;
import com.silabs.thunderboard.common.injection.qualifier.ForCloudKeyFirebase;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Observable;
import rx.Subscriber;
import rx.subjects.BehaviorSubject;
import timber.log.Timber;

/**
 * Provides Firebase functionality.
 * <p/>
 * One of the features of this app is to send live demo data to a cloud service. The implementation
 * is done through Firebase as the backend service.
 */
@Singleton
public class CloudManager implements Firebase.AuthResultHandler {
    private static final String START_TIME = "startTime";
    private static final String TEMPERATURE_TYPE = "temperatureUnits";
    private static final String MEASUREMENTS_TYPE = "measurementUnits";
    private static final String SHORT_URL = "shortURL";
    private static final String DATA = "data";

    private final Context context;
    private final String baseDataUrl;
    private final String baseDataThunderBoardUrl; // data for thunderboard
    private final String baseDataSessionsUrl; // data for sessions
    private final String baseDemoUrl;
    private final String baseDemoSessionsUrl;
    private final String keyFirebase;
    private final PreferenceManager prefsManager;

    private Firebase rootDataThundeBoardSessionsReference;
    private Firebase rootDataSessionsReference;
    private Firebase rootDataSessionsDemoReference;
    private Firebase shortenUrlReference;
    private String shortUrl; // sent as web link

    Map<Long, Object> data;

    public final BehaviorSubject<Boolean> wifiMonitor = BehaviorSubject.create();

    @Inject
    public CloudManager(
            @ForApplication Context context,
            @ForCloudData String baseDataUrl,
            @ForCloudDemo String baseDemoUrl,
            @ForCloudKeyFirebase String keyFirebase,
            PreferenceManager prefsManager) {
        this.context = context;
        this.baseDataUrl = baseDataUrl;
        this.baseDataThunderBoardUrl = baseDataUrl + "thunderboard/";
        this.baseDataSessionsUrl = baseDataUrl + "sessions/";
        this.baseDemoUrl = baseDemoUrl;
        this.baseDemoSessionsUrl = baseDemoUrl;
        this.keyFirebase = keyFirebase;
        this.prefsManager = prefsManager;

        // The Firebase library must be initialized once with an Android context.
        // This must happen before any Firebase app reference is created or used.
        Firebase.setAndroidContext(context);

        Timber.d("data url: %s", baseDataUrl);
        Timber.d("demo url: %s", baseDemoUrl);

        registerWifiUpdateReceiver();
    }

    // AuthResultHandler implementation

    @Override
    public void onAuthenticated(AuthData authData) {
        Timber.d("Firebase authentication success");
    }

    @Override
    public void onAuthenticationError(FirebaseError firebaseError) {
        Timber.w("Firebase authentication failure: %s, %s", firebaseError.getMessage(), firebaseError.getDetails());
    }

    /**
     * Creates all firebase references for the current session.
     *
     * @param model
     * @param deviceName
     * @param uniqueID
     * @param demo
     * @param sensor
     * @return
     */
    public String createFirebaseReference(String model, String deviceName, String uniqueID, String demo, ThunderBoardSensor sensor) {

        try {

            long currentTime = System.currentTimeMillis();

            String rootDataThundeBoardSessionsUrl = String.format("%s%s/sessions", baseDataThunderBoardUrl, model);
            Timber.d("rootDataThundeBoardSessionsUrl: %s", rootDataThundeBoardSessionsUrl);
            rootDataThundeBoardSessionsReference = new Firebase(rootDataThundeBoardSessionsUrl);
            rootDataThundeBoardSessionsReference.authWithCustomToken(keyFirebase, this);

            String rootDataSessionsUrl = String.format("%s%s", baseDataSessionsUrl, uniqueID);
            Timber.d("rootDataSessionsUrl: %s", rootDataSessionsUrl);
            rootDataSessionsReference = new Firebase(rootDataSessionsUrl);
            rootDataSessionsReference.authWithCustomToken(keyFirebase, this);
            rootDataSessionsDemoReference = rootDataSessionsReference.child(demo);

            // push the start time
            rootDataSessionsReference.child(START_TIME).setValue(currentTime);
            rootDataSessionsReference.child(TEMPERATURE_TYPE).setValue(prefsManager.getPreferences().temperatureType);
            rootDataSessionsReference.child(MEASUREMENTS_TYPE).setValue(prefsManager.getPreferences().measureUnitType);
            Timber.d("root sessions ref: %s", rootDataSessionsReference.getPath().toString());

            // push contactInfo
            ContactInfo ci = new ContactInfo();
            ThunderBoardPreferences prefs = prefsManager.getPreferences();
            ci.emailAddress = prefs.userEmail;
            ci.fullName = prefs.userName;
            ci.title = prefs.userTitle;
            ci.phoneNumber = prefs.userPhone;
            ci.deviceName = deviceName;
            rootDataSessionsReference.child("contactInfo").setValue(ci);

            String demoUrl = String.format("%s%s/%s/%s", baseDemoSessionsUrl, model, uniqueID, demo);
            Timber.d("short demo url: %s", demoUrl);
            rootDataSessionsReference.child(SHORT_URL).setValue(demoUrl);
            // will be overriden later, we do not want to keep it null....
            // if the requirement is to use a short url, then a refactor is needed to wait until it's available
            shortUrl = demoUrl;

            data = new HashMap<>();

            push(sensor);

            pushTimer.start();

            // request in the background
            shortenUrlReference = rootDataSessionsReference;
            shortenUrl(demoUrl);

            rootDataThundeBoardSessionsReference.child(String.valueOf(currentTime)).setValue(uniqueID);

            return demoUrl;

        } catch (FirebaseException e) {
            e.printStackTrace();
            Timber.d(e.getMessage());
            return null;
        }
    }

    public void push(ThunderBoardSensor sensor) {

        try {
            long time = System.currentTimeMillis();
            Timber.d("%d: %s", time, sensor.getSensorData().toString());
            data.put(time, sensor.getSensorData().clone());
        } catch (FirebaseException e) {
            e.printStackTrace();
            Timber.d(e.getMessage());
        }
    }

    private void pushDataMap(Map<Long, Object> data) {
        Iterator it = data.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            Timber.d("%s", pair.toString());
            rootDataSessionsDemoReference.child(DATA).child(String.valueOf(pair.getKey())).setValue(pair.getValue());
            it.remove();
        }
    }

    public void clearFirebaseReference(String uniqueID) {
        data = null;
        pushTimer.cancel();
    }

    public String getShortUrl() {
        return shortUrl;
    }

    private void shortenUrl(String demoUrl) {
        // Fetch and print a list of the contributors to this library.
        Observable<ShortenUrl.ShortUrl> url = ShortenUrl.getInstance().shorten.convert(demoUrl, "json", new Object());
        url
                // .observeOn()
                .subscribe(new Subscriber<ShortenUrl.ShortUrl>() {
                    @Override
                    public void onCompleted() {
                        if (isUnsubscribed()) unsubscribe();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e.getMessage());
                        if (isUnsubscribed()) unsubscribe();
                    }

                    @Override
                    public void onNext(ShortenUrl.ShortUrl url) {
                        shortUrl = url.shorturl;
                        shortUrl = shortUrl.replace("http:", "https:");
                        Timber.d(shortUrl);

                        shortenUrlReference.child(SHORT_URL).setValue(shortUrl);
                        if (isUnsubscribed()) unsubscribe();
                    }
                });

    }

    private final CountDownTimer pushTimer = new CountDownTimer(1000, 20000) {

        @Override
        public void onTick(long millisUntilFinished) {
            // n/a
        }

        @Override
        public void onFinish() {
            pushDataMap(data);
            start();
        }
    };

    private void registerWifiUpdateReceiver() {
        IntentFilter connectionFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        context.registerReceiver(wifiUpdateReceiver, connectionFilter);
    }

    private BroadcastReceiver wifiUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!isOnline()) {
                clearFirebaseReference("clear all references");
                wifiMonitor.onNext(false);
            } else {
                wifiMonitor.onNext(true);
            }
        }
    };

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    private static class ContactInfo {
        public String emailAddress;
        public String fullName;
        public String phoneNumber;
        public String title;
        public String deviceName;
    }
}
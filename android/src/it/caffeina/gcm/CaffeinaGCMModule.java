package it.caffeina.gcm;

import android.app.Activity;
import android.content.Intent;

import org.appcelerator.kroll.KrollModule;
import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.kroll.common.TiConfig;
import org.appcelerator.titanium.TiApplication;

import com.google.android.gcm.GCMRegistrar;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

import java.util.HashMap;
import java.util.Map;

@Kroll.module(name = "CaffeinaGCM", id = "it.caffeina.gcm")
public class CaffeinaGCMModule extends KrollModule {

    private static final String LCAT = "it.caffeina.gcm.CaffeinaGCMModule";

    private static CaffeinaGCMModule instance = null;

    private KrollFunction successCallback = null;
    private KrollFunction errorCallback = null;
    private KrollFunction messageCallback = null;

    public CaffeinaGCMModule() {
        super();
        instance = this;
    }

    public static CaffeinaGCMModule getInstance() {
        return instance;
    }

    @Kroll.method
    @SuppressWarnings("unchecked")
    public void registerForPushNotifications(HashMap options) {
        String senderId = (String)options.get("senderId");

        successCallback = (KrollFunction)options.get("success");
        errorCallback = (KrollFunction)options.get("error");
        messageCallback = (KrollFunction)options.get("callback");

        if (senderId != null) {
            GCMRegistrar.register(TiApplication.getInstance(), senderId);
        } else {
            sendError("No GCM senderId specified; get it from the Google Play Developer Console");
        }
    }

    @Kroll.method
    public void unregisterForPushNotifications() {
        GCMRegistrar.unregister(TiApplication.getInstance());
    }

    @Kroll.method
    @Kroll.getProperty
    public Boolean isRemoteNotificationsEnabled() {
        String registrationId = this.getRegistrationId();
        return (registrationId != null && registrationId.length() > 0);
    }

    @Kroll.method
    @Kroll.getProperty
    public String getRemoteDeviceUUID() {
        return this.getRegistrationId();
    }

    @Kroll.method
    @Kroll.getProperty
    public String getRegistrationId() {
        return GCMRegistrar.getRegistrationId(TiApplication.getInstance());
    }

    @Kroll.method
    public void setAppBadge(int count) {
        BadgeUtils.setBadge(TiApplication.getInstance().getApplicationContext(), count);
    }

    @Kroll.method
    public int getAppBadge() {
        return 0;
    }

    public void sendSuccess(String registrationId) {
        if (successCallback == null) {
            Log.e(LCAT, "sendSuccess invoked but no successCallback defined");
            return;
        }

        if (registrationId == null || registrationId.length() == 0) {
            sendError("RegistrationId from GCM is empty");
            return;
        }

        HashMap<String, Object> e = new HashMap<String, Object>();
        e.put("registrationId", registrationId);
        e.put("deviceToken", registrationId);

        successCallback.callAsync(getKrollObject(), e);

        // Send old notification if present

        Intent intent = TiApplication.getInstance().getRootOrCurrentActivity().getIntent();
        if (intent.hasExtra("notification")) {
            Log.d(LCAT, "Intent has notification in its extra");
            sendMessage(intent.getStringExtra("notification"), true);
        } else {
            Log.d(LCAT, "No notification in Intent");
        }
    }

    public void sendError(String error) {
        if (errorCallback == null) return;

        HashMap<String, Object> e = new HashMap<String, Object>();
        e.put("error", error);

        errorCallback.callAsync(getKrollObject(), e);
    }

    public void sendMessage(String dataAsString, Boolean inBackground) {
        if (messageCallback == null) {
            Log.e(LCAT, "sendMessage invoked but no messageCallback defined");
            return;
        }

        try {
            HashMap<String, Object> e = new HashMap<String, Object>();
            e.put("data", dataAsString);
            e.put("inBackground", inBackground);

            messageCallback.call(getKrollObject(), e);

        } catch (Exception ex) {
            Log.e(LCAT, ex.getMessage());
        }
    }

    @Kroll.onAppCreate
    public static void onAppCreate(TiApplication app) {
        Log.d(LCAT, "onAppCreate " + app + " (" + (instance != null) + ")");
    }

}
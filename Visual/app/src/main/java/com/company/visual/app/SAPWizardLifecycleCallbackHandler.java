package com.company.visual.app;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.company.visual.logon.ClientPolicyUtilities;
import com.company.visual.logon.LogonActivity;
import com.sap.cloud.mobile.foundation.authentication.AppLifecycleCallbackHandler;
import com.sap.cloud.mobile.foundation.securestore.SecureKeyValueStore;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Singleton class for handling application lifecycle callbacks. It is actually a wrapper around
 * the class {@link AppLifecycleCallbackHandler} by adding some additional logic which handles
 * application passcode timeout.
 */
public class SAPWizardLifecycleCallbackHandler implements Application.ActivityLifecycleCallbacks {

    private AppLifecycleCallbackHandler fndHandler;
    private Timer timer;
    private Activity currentActivity;
    private Object lock = new Object();

    private static class SingletonHolder {

        private static final SAPWizardLifecycleCallbackHandler INSTANCE = new SAPWizardLifecycleCallbackHandler();
    }

    public static SAPWizardLifecycleCallbackHandler getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private SAPWizardLifecycleCallbackHandler() {
        fndHandler = AppLifecycleCallbackHandler.getInstance();
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        fndHandler.onActivityCreated(activity, savedInstanceState);
    }

    @Override
    public void onActivityStarted(Activity activity) {
        fndHandler.onActivityStarted(activity);
    }

    @Override
    public void onActivityResumed(Activity activity) {
        fndHandler.onActivityResumed(activity);

        synchronized (lock) {
            // set current activity
            currentActivity = activity;

            if (!isAppInBackground(activity)) {
                if (timer != null) {
                    timer.cancel();

                    SecureKeyValueStore store = SAPWizardApplication.getApplicatiton().getStore();
                    if (!store.isOpen()) {
                        Intent startIntent = new Intent(activity, LogonActivity.class);
                        activity.startActivity(startIntent);
                    }
                    timer = null;
                }
            }
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {
        fndHandler.onActivityPaused(activity);


    }

    @Override
    public void onActivityStopped(Activity activity) {
        fndHandler.onActivityStopped(activity);

        synchronized (lock) {
            if (isAppInBackground(activity)) {
                // if application is in background start 'timer'
                if (timer == null) {
                    int timeOut = SAPWizardApplication.getApplicatiton().getPasscodeLockTimeout();
                    if (timeOut >= 0) {
                        timer = new Timer();
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                SecureKeyValueStore store = SAPWizardApplication.getApplicatiton().getStore();
                                boolean isUserPc = SAPWizardApplication.getApplicatiton().isUserPasscode();
                                if (store != null && store.isOpen() && isUserPc) {
                                    store.close();
                                }
                            }
                        }, timeOut * 1000);
                    }
                }
            }
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        fndHandler.onActivitySaveInstanceState(activity, outState);
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        fndHandler.onActivityResumed(activity);
    }

    /**
     * Method checks if the app is in background or not
     */
    public static boolean isAppInBackground(Context context) {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
            if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                for (String activeProcess : processInfo.pkgList) {
                    if (activeProcess.equals(context.getPackageName())) {
                        isInBackground = false;
                        break;
                    }
                }
            }
        }
        return isInBackground;
    }

    public Activity getCurrentActivity() {
        return currentActivity;
    }
}

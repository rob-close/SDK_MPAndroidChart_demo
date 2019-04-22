package com.company.visual.mdui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.annotation.NonNull;

import com.company.visual.R;

import com.company.visual.app.SAPWizardApplication;
import com.company.visual.service.SAPServiceManager;
import com.sap.cloud.mobile.foundation.common.ClientProvider;
import com.sap.cloud.mobile.foundation.logging.Logging;
import com.sap.cloud.mobile.onboarding.passcode.ChangePasscodeActivity;
import com.sap.cloud.mobile.onboarding.passcode.SetPasscodeActivity;
import com.sap.cloud.mobile.onboarding.passcode.SetPasscodeSettings;

import android.provider.Settings;
import android.widget.Toast;
import com.company.visual.service.SAPServiceManager;
import com.sap.cloud.mobile.foundation.networking.AppHeadersInterceptor;
import com.sap.cloud.mobile.foundation.networking.WebkitCookieJar;
import okhttp3.OkHttpClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;


import com.company.visual.logon.BasicAuthPersistentCredentialStore;
import java.util.concurrent.TimeUnit;
import com.sap.cloud.mobile.foundation.authentication.BasicAuthDialogAuthenticator;

public class SettingsFragment extends PreferenceFragment {

    private static Logger logger = LoggerFactory.getLogger(EntitySetListActivity.class);
    private Logging.UploadListener listener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        final Preference logLevelPreference = findPreference(getActivity().getApplicationContext().getString(R.string.log_level));
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        logLevelPreference.setSummary(defaultSharedPreferences.getString(getActivity().getApplicationContext().getString(R.string.log_level), "Debug"));
        logLevelPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                logger.debug("setting summary of log level preference to " + newValue);
                preference.setSummary((String) newValue);
                return true;
            }
        });
        Preference changePasscodePreference = findPreference(getActivity().getApplicationContext().getString(R.string.manage_passcode));
        changePasscodePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (SAPWizardApplication.getApplicatiton().isUserPasscode()) {
                    Intent intent = new Intent(SettingsFragment.this.getActivity(), ChangePasscodeActivity.class);
                    SetPasscodeSettings setPasscodeSettings = new SetPasscodeSettings();
                    setPasscodeSettings.saveToIntent(intent);
                    SettingsFragment.this.getActivity().startActivity(intent);
                } else {
                    Intent intent = new Intent(SettingsFragment.this.getActivity(), SetPasscodeActivity.class);
                    SetPasscodeSettings setPasscodeSettings = new SetPasscodeSettings();
                    setPasscodeSettings.saveToIntent(intent);
                    SettingsFragment.this.getActivity().startActivity(intent);
                }
                return false;
            }
        });
        // Uploading the logs
        final Preference logUploadPreference = findPreference(getActivity().getApplicationContext().getString(R.string.upload_log));
        logUploadPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
           @Override
           public boolean onPreferenceClick(Preference preference) {
                setClientProvider();
                Logging.uploadLog(ClientProvider.get(), SAPWizardApplication.getApplicatiton().getSettingsParameters());
                return false;
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Initialize logging
        Logging.initialize(context, new Logging.ConfigurationBuilder().initialLevel(Level.WARN).logToConsole(true).build());
    }

    @Override
    public void onResume() {
        super.onResume();
        Logging.addLogUploadListener(getListener());
    }

    @Override
    public void onPause() {
        super.onPause();
        Logging.removeLogUploadListener(getListener());
    }

    private Logging.UploadListener getListener() {
        if (listener == null) {
            listener = new Logging.UploadListener() {
                @Override
                public void onSuccess() {
                    Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.log_upload_ok), Toast.LENGTH_LONG).show();
                    logger.info("Log is uploaded to the server.");
                }

                @Override
                public void onError(@NonNull Throwable throwable) {
                    Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.log_upload_failed), Toast.LENGTH_LONG).show();
                    logger.error("Log upload failed!");
                }

                @Override
                public void onProgress(int i) {
                    // You could add a progress indicator and update it from here
                }
            };
        }
        return listener;
    }

    private void setClientProvider() {
         String devID = Settings.Secure.getString(SAPWizardApplication.getApplicatiton().getContentResolver(), Settings.Secure.ANDROID_ID);

         OkHttpClient okHttpClient = new OkHttpClient.Builder()
                 .addInterceptor(new AppHeadersInterceptor(SAPServiceManager.APPLICATION_ID, devID, SAPServiceManager.APPLICATION_VERSION))
                 .authenticator(new BasicAuthDialogAuthenticator(BasicAuthPersistentCredentialStore.getInstance()))
                 .cookieJar(new WebkitCookieJar())
                 .connectTimeout(30, TimeUnit.SECONDS)
                 .build();

        // Set the provider
        ClientProvider.set(okHttpClient);
    }
}

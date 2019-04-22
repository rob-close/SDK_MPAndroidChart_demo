package com.company.visual.logon;

import android.support.v4.app.Fragment;
import android.provider.Settings;
import android.app.Activity;
import java.util.concurrent.TimeUnit;
import android.widget.Toast;
import com.company.visual.app.SAPWizardApplication;
import ch.qos.logback.classic.Level;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sap.cloud.mobile.foundation.logging.Logging;

import com.company.visual.R;
import com.company.visual.app.ErrorMessage;

import com.company.visual.mdui.EntitySetListActivity;
import com.company.visual.service.SAPServiceManager;

import com.sap.cloud.mobile.foundation.authentication.BasicAuthDialogAuthenticator;
import com.sap.cloud.mobile.foundation.networking.AppHeadersInterceptor;
import com.sap.cloud.mobile.onboarding.launchscreen.WelcomeScreenActionHandler;
import com.sap.cloud.mobile.foundation.networking.WebkitCookieJar;
import com.sap.cloud.mobile.foundation.common.ClientProvider;

import android.webkit.CookieManager;
import android.webkit.ValueCallback;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LaunchScreenActionHandlerImpl implements WelcomeScreenActionHandler {

   private static final String DEFAULT_SECURESTORE_PASSCODE = "defaultpasscode";

   private Activity activity;

	private Logger logger = LoggerFactory.getLogger(LaunchScreenActionHandlerImpl.class);
	 
    private static OkHttpClient basicAuthOkHttpClient;

    @Override
    public void startStandardOnboarding(final Fragment fragment) {

		activity = fragment.getActivity();

		
		// delete the cookies
		removeAllCookies(fragment);
		
		String devID = Settings.Secure.getString(activity.getContentResolver(), Settings.Secure.ANDROID_ID);

		basicAuthOkHttpClient = new OkHttpClient.Builder()
				.addInterceptor(new AppHeadersInterceptor(SAPServiceManager.APPLICATION_ID, devID, SAPServiceManager.APPLICATION_VERSION))
				.authenticator(new BasicAuthDialogAuthenticator(BasicAuthPersistentCredentialStore.getInstance()))
				.cookieJar(new WebkitCookieJar())
				.connectTimeout(30, TimeUnit.SECONDS)
				.build();

		Request request = new Request.Builder()
				.get()
				.url(SAPServiceManager.SERVICE_URL + SAPServiceManager.CONNECTION_ID)
				.build();

		try (Response response = basicAuthOkHttpClient.newCall(request).execute()) {
			if (response.isSuccessful()) {
				ClientProvider.set(basicAuthOkHttpClient);
				new SAPServiceManager().initSAPService(basicAuthOkHttpClient);
				// Initialize logging
				Logging.initialize(activity.getApplicationContext(), new Logging.ConfigurationBuilder().initialLevel(Level.WARN).logToConsole(true).build());
				activity.setResult(Activity.RESULT_OK);
				activity.finish();
			} else {
				fragment.getActivity().runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(fragment.getActivity().getApplicationContext(), "Error: " + response.message(), Toast.LENGTH_LONG).show();
					}
				});
			}
		} catch (IOException e) {
			((SAPWizardApplication) activity.getApplication()).setIsOnBoarded(false);
		}
    }
	private void removeAllCookies(Fragment fragment) {
		// delete the cookies
        CookieManager webkitCookieManager = CookieManager.getInstance();

        fragment.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                webkitCookieManager.removeAllCookies(new ValueCallback<Boolean>() {
                    @Override
                    public void onReceiveValue(Boolean success) {
                        if (success) {
                            logger.info("Cookies are deleted.");
                        } else {
                            logger.error("Cookies couldn't be removed!");
                        }
                    }
                });
            }
        });
	}

    @Override
    public void startDemoMode(final Fragment fragment) {
    }

    @Override
    public void startOnboardingWithDiscoveryServiceEmail(Fragment fragment, String s) {
    }
}

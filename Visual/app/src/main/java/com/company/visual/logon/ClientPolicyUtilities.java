package com.company.visual.logon;

import android.support.annotation.NonNull;

import com.company.visual.R;
import com.company.visual.app.ErrorMessage;
import com.company.visual.app.SAPWizardApplication;
import com.company.visual.service.SAPServiceManager;

import com.sap.cloud.mobile.foundation.authentication.BasicAuthDialogAuthenticator;
import com.sap.cloud.mobile.foundation.networking.AppHeadersInterceptor;
import com.sap.cloud.mobile.foundation.common.SettingsParameters;
import com.sap.cloud.mobile.foundation.networking.WebkitCookieJar;
import com.sap.cloud.mobile.foundation.securestore.SecureKeyValueStore;
import com.sap.cloud.mobile.foundation.settings.Settings;
import com.sap.cloud.mobile.onboarding.passcode.PasscodePolicy;


import org.json.JSONObject;

import java.util.Locale;
import java.util.concurrent.CountDownLatch;

import ch.qos.logback.classic.Level;
import okhttp3.OkHttpClient;

// TODO extend this class with the needed methods
public class ClientPolicyUtilities {

	public static final String KEY_RETRY_LIMIT = "retryLimit";
    public static final String KEY_RETRY_COUNT = "retryCount";
	public static final String KEY_PC_WAS_SET_AT = "when_was_the_pc_set";

    private static final String PASSCODE_POLICY_FINGERPRINT_ENABLED = "passwordPolicyFingerprintEnabled";
    private static final String PASSCODE_POLICY_DIGIT_REQUIRED = "passwordPolicyDigitRequired";
    private static final String PASSCODE_POLICY_LOWER_REQUIRED = "passwordPolicyLowerRequired";
    private static final String PASSCODE_POLICY_SPECIAL_REQUIRED = "passwordPolicySpecialRequired";
    private static final String PASSCODE_POLICY_UPPER_REQUIRED = "passwordPolicyUpperRequired";
    private static final String PASSCODE_POLICY_MIN_LENGTH = "passwordPolicyMinLength";
    private static final String PASSCODE_POLICY_MIN_UNIQUE_CHARS = "passwordPolicyMinUniqueChars";
    private static final String PASSCODE_POLICY_RETRY_LIMIT = "passwordPolicyRetryLimit";
    private static final String PASSCODE_POLICY_IS_DIGITS_ONLY = "passwordPolicyIsDigitsOnly";
    private static final String PASSCODE_POLICY_ENABLED = "passwordPolicyEnabled";
    private static final String PASSCODE_POLICY_LOCK_TIMEOUT = "passwordPolicyLockTimeout";
    private static final String PASSCODE_POLICY_EXPIRES_IN_N_DAYS = "passwordPolicyExpiresInNDays";
    private static final String PASSCODE_POLICY_DEFAULT_ENABLED = "passwordPolicyDefaultPasswordAllowed";


    private static final String LOG_POLICY_LOG_LEVEL = "logLevel";
    private static final String LOG_POLICY_LOG_ENABLED = "logEnabled";

    private static final String SETTINGS_PASSCODE = "passwordPolicy";
    private static final String SETTINGS_LOG = "logSettings";

    private static final String KEY_CLIENT_POLICY = "passcodePolicy";

    private static ClientPolicy lastPolicy;
    private static ClientPolicy policyFromServer;

    public static ClientPolicy getClientPolicy(boolean forceRefresh) {

        ClientPolicy clientPolicy = null;
        if (!forceRefresh && lastPolicy != null) {
            clientPolicy = lastPolicy;
        } else {
            // first try to read it from the server, if not successful then take the local
            // persisted one, finally, fallback to the default one
            getClientPolicyFromServer();
            clientPolicy = policyFromServer;
            if (clientPolicy == null) {
                clientPolicy = getClientPolicyFromStore();
                if (clientPolicy == null) {
                    clientPolicy = getDefaultPolicy();
                }
            } else {
                // store in the local secure store
                SecureKeyValueStore store = SAPWizardApplication.getApplicatiton().getStore();
                if (store != null && store.isOpen()) {
                    store.put(KEY_CLIENT_POLICY, clientPolicy);
                }
				// store retry limit in the RLM store
                SecureKeyValueStore rlmStore = SAPWizardApplication.getApplicatiton().getRlmStore();
                if (rlmStore != null && rlmStore.isOpen()) {
                    int rtLimit = clientPolicy.getPcPolicy().getRetryLimit();
                    rlmStore.put(KEY_RETRY_LIMIT, rtLimit);
                    rlmStore.put(KEY_RETRY_COUNT, 0);
					rlmStore.close();
                }
            }
            lastPolicy = clientPolicy;
        }
        return clientPolicy;
    }

    private static ClientPolicy getDefaultPolicy() {
        ClientPolicy defClientPolicy = new ClientPolicy();

        PasscodePolicy defPcPolicy = new PasscodePolicy();
        defPcPolicy.setHasDigit(true);
        defPcPolicy.setHasLower(false);
        defPcPolicy.setHasSpecial(false);
        defPcPolicy.setHasUpper(false);
        defPcPolicy.setIsDigitsOnly(false);
        defPcPolicy.setMinLength(8);
        defPcPolicy.setMinUniqueChars(0);
        defPcPolicy.setRetryLimit(3);
        defPcPolicy.setSkipEnabled(false);
        defClientPolicy.setPcPolicy(defPcPolicy);

        defClientPolicy.setLogEnabled(true);
        defClientPolicy.setLogLevel(Level.INFO);
        return defClientPolicy;
    }

    private static void getClientPolicyFromServer() {

         policyFromServer = null;

        String devID = android.provider.Settings.Secure.getString(SAPWizardApplication.getApplicatiton().getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new AppHeadersInterceptor(SAPServiceManager.APPLICATION_ID, devID, SAPServiceManager.APPLICATION_VERSION))
                .authenticator(new BasicAuthDialogAuthenticator(BasicAuthPersistentCredentialStore.getInstance()))
                .cookieJar(new WebkitCookieJar())
                .build();

        SettingsParameters settingsParameters = SAPWizardApplication.getApplicatiton().getSettingsParameters();
        if (settingsParameters != null) {
            CountDownLatch downloadLatch = new CountDownLatch(1);
            Thread downloadThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Settings settings = new Settings(okHttpClient, settingsParameters);
                    settings.load(Settings.SettingTarget.APPLICATION, "mobileservices/settingsExchange", new PolicyCallbackListener(downloadLatch));

                }
            });
            downloadThread.start();
            try {
                downloadLatch.await();
            } catch (InterruptedException e) {
            }
        }
    }

    private static ClientPolicy getClientPolicyFromStore() {
      ClientPolicy clientPolicy = null;
      SecureKeyValueStore store = SAPWizardApplication.getApplicatiton().getStore();
      if (store != null && store.isOpen()) {
        clientPolicy = store.getSerializable(KEY_CLIENT_POLICY);
      }
      return clientPolicy;
    }

    private static Level logLevelFromServerString(String logLevel) {
      String lowerCaseLogLevel = logLevel.toLowerCase(Locale.getDefault());
      if (lowerCaseLogLevel.equals("none")) {
        return Level.OFF;
      } else if (lowerCaseLogLevel.equals("fatal")) {
        return Level.ERROR;
      } else if (lowerCaseLogLevel.equals("error")) {
        return Level.ERROR;
      } else if (lowerCaseLogLevel.startsWith("warn")) {
      // use startsWith so this matches both the server-provided string WARN, and the user-friendly string Warning.
        return  Level.WARN;
      } else if (lowerCaseLogLevel.equals("info")) {
        return Level.INFO;
      } else if (lowerCaseLogLevel.equals("debug")) {
        return Level.DEBUG;
      } else if (lowerCaseLogLevel.equals("path")) {
         return Level.ALL;
      }
    return Level.DEBUG;
    }

    private static class PolicyCallbackListener implements Settings.CallbackListener {

          private CountDownLatch downloadLatch;

          public PolicyCallbackListener(CountDownLatch downloadLatch) {
              this.downloadLatch = downloadLatch;
          }

          public ClientPolicy getPolicyFromServer() {
              return policyFromServer;
          }

          @Override
          public void onSuccess(@NonNull JSONObject result) {
              if (result != null) {
                  JSONObject passcodePolicyJson = result.optJSONObject(SETTINGS_PASSCODE);
                  if (passcodePolicyJson != null) {
                      policyFromServer = new ClientPolicy();
                      boolean isPcPolicyEnabled = passcodePolicyJson.optBoolean(PASSCODE_POLICY_ENABLED, true);
                      policyFromServer.setPasscodePolicyEnabled(isPcPolicyEnabled);

                      PasscodePolicy passcodePolicy = new PasscodePolicy();
                      passcodePolicy.setAllowsFingerprint(passcodePolicyJson.optBoolean(PASSCODE_POLICY_FINGERPRINT_ENABLED, true));
                      passcodePolicy.setHasDigit(passcodePolicyJson.optBoolean(PASSCODE_POLICY_DIGIT_REQUIRED, false));
                      passcodePolicy.setHasLower(passcodePolicyJson.optBoolean(PASSCODE_POLICY_LOWER_REQUIRED, false));
                      passcodePolicy.setHasSpecial(passcodePolicyJson.optBoolean(PASSCODE_POLICY_SPECIAL_REQUIRED, false));
                      passcodePolicy.setHasUpper(passcodePolicyJson.optBoolean(PASSCODE_POLICY_UPPER_REQUIRED, false));
                      passcodePolicy.setIsDigitsOnly(passcodePolicyJson.optBoolean(PASSCODE_POLICY_IS_DIGITS_ONLY, false)); // Is this actually set on the server??
                      passcodePolicy.setMinLength(passcodePolicyJson.optInt(PASSCODE_POLICY_MIN_LENGTH, 8));
                      passcodePolicy.setMinUniqueChars(passcodePolicyJson.optInt(PASSCODE_POLICY_MIN_UNIQUE_CHARS, 0));
                      passcodePolicy.setRetryLimit(passcodePolicyJson.optInt(PASSCODE_POLICY_RETRY_LIMIT, 20));
                      passcodePolicy.setSkipEnabled(passcodePolicyJson.optBoolean(PASSCODE_POLICY_DEFAULT_ENABLED, false));
                      policyFromServer.setPcPolicy(passcodePolicy);
                      SAPWizardApplication.getApplicatiton().setPasscodeLockTimeout(passcodePolicyJson.optInt(PASSCODE_POLICY_LOCK_TIMEOUT, 300));
                      SAPWizardApplication.getApplicatiton().setPasscodeExpiresInNDays(passcodePolicyJson.optInt(PASSCODE_POLICY_EXPIRES_IN_N_DAYS, 0));
                  }

                  JSONObject logSettingsJson = result.optJSONObject(SETTINGS_LOG);
                  if (logSettingsJson != null) {
                      boolean isLogEnabled = logSettingsJson.optBoolean(LOG_POLICY_LOG_ENABLED, false);
                      policyFromServer.setLogEnabled(isLogEnabled);
                      if (isLogEnabled) {
                          String logLevelStr = logSettingsJson.optString(LOG_POLICY_LOG_LEVEL, "DEBUG");
                          Level logLevel = logLevelFromServerString(logLevelStr);
                          policyFromServer.setLogLevel(logLevel);
                      }
                  }
              }
              downloadLatch.countDown();
          }

          @Override
          public void onError(@NonNull Throwable throwable) {
              policyFromServer = null;
              String eTitle = SAPWizardApplication.getApplicatiton().getResources().getString(R.string.policy_download_failed);
              String eDetail = SAPWizardApplication.getApplicatiton().getResources().getString(R.string.policy_download_failed_details);
              ErrorMessage eMsg = new ErrorMessage(eTitle, eDetail);
              SAPWizardApplication.getErrorHandler().sendErrorMessage(eMsg);
              downloadLatch.countDown();
          }
      }
}

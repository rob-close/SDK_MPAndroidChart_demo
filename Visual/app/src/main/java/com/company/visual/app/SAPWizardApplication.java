package com.company.visual.app;

import android.app.Application;
import android.provider.Settings;
import com.company.visual.R;
import com.company.visual.service.SAPServiceManager;
import com.company.visual.logon.ClientPolicyUtilities;


import com.sap.cloud.mobile.foundation.authentication.AppLifecycleCallbackHandler;
import com.sap.cloud.mobile.foundation.common.SettingsParameters;
import com.sap.cloud.mobile.foundation.securestore.SecureKeyValueStore;
import com.sap.cloud.mobile.foundation.common.EncryptionError;
import com.sap.cloud.mobile.foundation.common.EncryptionUtil;
import com.sap.cloud.mobile.foundation.securestore.OpenFailureException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.util.Date;
import java.util.Calendar;

/**
 * This class extends the {@link Application} class. It maintains an
 * {@link ActivityLifecycleCallbacks} instance, as well. By extending the callback's default
 * implementation the application will be able to react on lifecycle events of the contained activities.
 */
public class SAPWizardApplication extends Application {

    // secure store for application data
    public static final String APP_SECURE_STORE_NAME = "APP_SECURE_STORE";
    public static final String APP_SECURE_STORE_PCODE_ALIAS = "app_pc_alias";
    // secure store for handling the retry limit
    public static final String RLM_SECURE_STORE_NAME = "RLM_SECURE_STORE";
    public static final String RLM_SECURE_STORE_PCODE_ALIAS = "rlm_pc_alias";


	 public static final String IS_PASSCODE_POLICY_ENABLED = "isPasscodePolicyEnabled";
	 public static final String IS_DEFAULT_PASSCODE_ENABLED = "isDefaultPasscodeEnabled";
	 public static final String IS_ONBOARDED = "isOnboarded";
	 public static final String IS_USER_PASSCODE = "isUserPasscode";
	 public static final String PASSCODE_POLICY_EXPIRES_AT = "passwordPolicyExpiresAt";
	 public static final String PASSCODE_POLICY_LOCK_TIMEOUT = "passwordPolicyLockTimeout";
	 public static final String PASSCODE_POLICY_EXPIRES_IN_N_DAYS = "passwordPolicyExpiresInNDays";


    private static SAPWizardApplication app;
	  private static ErrorHandler eHandler;

    private SecureKeyValueStore appStore;

	  private static Logger logger = LoggerFactory.getLogger(SAPWizardApplication.class);

    public static SAPWizardApplication getApplicatiton() {
        return app;
    }

    public void setAppStore(SecureKeyValueStore keyValueStore) {
      appStore = keyValueStore;
    }

    public SecureKeyValueStore getStore() {
		return appStore;
    }
	
	public SecureKeyValueStore getRlmStore() {
        SecureKeyValueStore rlmStore = null;
        try {
            // create the store for the retry variables (with default passcode)
            EncryptionUtil.initialize(this.getApplicationContext());
            rlmStore = new SecureKeyValueStore(this, RLM_SECURE_STORE_NAME);
            if (rlmStore != null && !rlmStore.isOpen()) {
                byte[] rlmEncKey = EncryptionUtil.getEncryptionKey(RLM_SECURE_STORE_PCODE_ALIAS);
                rlmStore.open(rlmEncKey);
            }
        } catch (EncryptionError | OpenFailureException e) {
            rlmStore = null;
            logger.error("RLM secure store couldn't be created at startup.");
        }
        return rlmStore;
    }

    public boolean isUserPasscode() {
        SecureKeyValueStore rlmStore = getRlmStore();
        Boolean isUserPc = rlmStore.getBoolean(IS_USER_PASSCODE);
        rlmStore.close();
        if (isUserPc != null) {
            return isUserPc;
        } else {
            return true;
        }
    }

    public void setIsUserPasscode(boolean isUserPc) {
       SecureKeyValueStore rlmStore = getRlmStore();
       rlmStore.put(IS_USER_PASSCODE, isUserPc);
       rlmStore.close();
  }

    public boolean isOnBoarded() {
        SecureKeyValueStore rlmStore = getRlmStore();
        Boolean isOnboarded = rlmStore.getBoolean(IS_ONBOARDED);
        rlmStore.close();
        if (isOnboarded != null) {
            return isOnboarded;
        } else {
            return false;
        }
    }

    public void setIsOnBoarded(boolean isOnBoarded) {
       SecureKeyValueStore rlmStore = getRlmStore();
       rlmStore.put(IS_ONBOARDED, isOnBoarded);
       rlmStore.close();
    }
    public int getPasscodeExpiresInNDays() {
        SecureKeyValueStore rlmStore = getRlmStore();
        Integer expiresIn = rlmStore.getInt(PASSCODE_POLICY_EXPIRES_IN_N_DAYS);
        rlmStore.close();
        if (expiresIn != null) {
            return expiresIn;
        } else {
            return -1;
        }
    }

    public void setPasscodeExpiresInNDays(int expireInterval) {
        SecureKeyValueStore rlmStore = getRlmStore();
        rlmStore.put(PASSCODE_POLICY_EXPIRES_IN_N_DAYS, expireInterval);
        rlmStore.close();
    }

    public long getPasscodeExpiresAt() {
        long retVal = 0;
        if (getPasscodeExpiresInNDays() > 0) {
            SecureKeyValueStore rlmStore = getRlmStore();
            Calendar expiresAt = (Calendar) rlmStore.getSerializable(ClientPolicyUtilities.KEY_PC_WAS_SET_AT);
            rlmStore.close();
            if (expiresAt != null) {
                expiresAt.add(Calendar.DAY_OF_YEAR, getPasscodeExpiresInNDays());
                Date expiration = expiresAt.getTime();
                logger.info("Passcode expires at: " + expiration.toString());
                retVal = expiresAt.getTimeInMillis();
            }
        }
        return retVal;
    }

    public boolean isPasscodeExpired() {
         if (isPasscodePolicyEnabled() && getPasscodeExpiresInNDays() != 0) {
             if (System.currentTimeMillis() - getPasscodeExpiresAt() > 0) {
                 return true;
             } else {
                 return false;
             }
        } else {
            return false;
        }
    }

    public int getPasscodeLockTimeout() {
        SecureKeyValueStore rlmStore = getRlmStore();
        Integer lockTimeout = rlmStore.getInt(PASSCODE_POLICY_LOCK_TIMEOUT);
        rlmStore.close();
        if (lockTimeout != null) {
            return lockTimeout;
        } else {
            return -1;
        }
    }

    public void setPasscodeLockTimeout(int lockTimeout) {
        SecureKeyValueStore rlmStore = getRlmStore();
        rlmStore.put(PASSCODE_POLICY_LOCK_TIMEOUT, lockTimeout);
        rlmStore.close();
    }

    public boolean isPasscodePolicyEnabled() {
        SecureKeyValueStore rlmStore = getRlmStore();
        Boolean isPolicyEnabled = rlmStore.getBoolean(IS_PASSCODE_POLICY_ENABLED);
        rlmStore.close();
        if (isPolicyEnabled != null) {
            return isPolicyEnabled;
        } else {
            return true;
        }
    }

    public void setIsPasscodePolicyEnabled(boolean isPcPolicyEnabled) {
        SecureKeyValueStore rlmStore = getRlmStore();
        rlmStore.put(IS_PASSCODE_POLICY_ENABLED, isPcPolicyEnabled);
        rlmStore.close();
    }

 
    /**
    * Returns a static reference to the error handler (see {@link ErrorHandler}, which can be used
    * to present error notifications to the application's user.
    */
    public static ErrorHandler getErrorHandler() {
        return eHandler;
    }

    public boolean isDefaultPasscodeEnabled() {
        SecureKeyValueStore rlmStore = getRlmStore();
        Boolean isDefaultEnabled = rlmStore.getBoolean(IS_DEFAULT_PASSCODE_ENABLED);
        rlmStore.close();
        if (isDefaultEnabled != null) {
            return isDefaultEnabled;
        } else {
            return true;
        }
    }

    public void setIsDefaultPasscodeEnabled(boolean isDefPcEnabled) {
        SecureKeyValueStore rlmStore = getRlmStore();
        rlmStore.put(IS_DEFAULT_PASSCODE_ENABLED, isDefPcEnabled);
        rlmStore.close();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        SAPWizardApplication.app = this;

        // starts an error handler thread
        eHandler = new ErrorHandler("SAPWizardErrorHandler", this);
        ErrorPresenter presenter = new ErrorPresenterByNotification(this);
        eHandler.setPresenter(presenter);
        eHandler.start();

        // registers lifecycle callback
        registerActivityLifecycleCallbacks(SAPWizardLifecycleCallbackHandler.getInstance());
    }

    public SettingsParameters getSettingsParameters() {
        try {
            String devID = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
            return new SettingsParameters(SAPServiceManager.SERVICE_URL, SAPServiceManager.APPLICATION_ID, devID, SAPServiceManager.APPLICATION_VERSION);
        } catch (MalformedURLException e) {
            String eTitle = getResources().getString(R.string.settings_error);
            String eDetail = getResources().getString(R.string.settings_download_failed_detail);
            ErrorMessage eMsg = new ErrorMessage(eTitle, eDetail, e, false);
        }
        return null;
    }
}

package com.company.visual.logon;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;

import com.company.visual.R;
import com.company.visual.app.ErrorMessage;
import com.company.visual.app.SAPWizardApplication;
import com.sap.cloud.mobile.foundation.common.EncryptionError;
import com.sap.cloud.mobile.foundation.common.EncryptionUtil;
import com.sap.cloud.mobile.foundation.securestore.OpenFailureException;
import com.sap.cloud.mobile.foundation.securestore.SecureKeyValueStore;
import com.sap.cloud.mobile.onboarding.passcode.PasscodeActionHandler;
import com.sap.cloud.mobile.onboarding.passcode.PasscodeInputMode;
import com.sap.cloud.mobile.onboarding.passcode.PasscodePolicy;
import com.sap.cloud.mobile.onboarding.passcode.PasscodeValidationException;
import com.sap.cloud.mobile.onboarding.passcode.SetPasscodeActivity;
import com.sap.cloud.mobile.onboarding.passcode.SetPasscodeSettings;
import com.sap.cloud.mobile.onboarding.passcode.PasscodeValidationFailedToMatchException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.List;
import java.util.Arrays;
import java.util.Calendar;

public class PasscodeActionHandlerImpl implements PasscodeActionHandler {

    private static char[] old_passcode;

    private Logger logger = LoggerFactory.getLogger(PasscodeActionHandlerImpl.class);
    private SecureKeyValueStore store = SAPWizardApplication.getApplicatiton().getStore();
    private CountDownLatch changePasscodeLatch;

    @Override
    public void shouldTryPasscode(char[] passcode, PasscodeInputMode mode, final Fragment fragment) throws PasscodeValidationException {

        Context applicationContext = fragment.getActivity().getApplicationContext();

        if (store == null) {
			store = new SecureKeyValueStore(fragment.getActivity(), SAPWizardApplication.APP_SECURE_STORE_NAME);
            SAPWizardApplication.getApplicatiton().setAppStore(store);
        }

        switch (mode) {
            case CREATE:
                if (!SAPWizardApplication.getApplicatiton().isUserPasscode()) {
                    if (passcode != null) {
                        // change from default to user pc
                        try {
							              EncryptionUtil.enablePasscode(SAPWizardApplication.APP_SECURE_STORE_PCODE_ALIAS, passcode);
                            byte[] encKey = EncryptionUtil.getEncryptionKey(SAPWizardApplication.APP_SECURE_STORE_PCODE_ALIAS, passcode);
                            store.open(encKey);
							
                            // save the timestamp
                            SecureKeyValueStore rlmStore = SAPWizardApplication.getApplicatiton().getRlmStore();
                            rlmStore.put(ClientPolicyUtilities.KEY_PC_WAS_SET_AT, Calendar.getInstance());
                            rlmStore.close();							
                        } catch (OpenFailureException | EncryptionError e) {
                            Resources res = applicationContext.getResources();
                            String eTitle = res.getString(R.string.invalid_passcode);
                            String eDetail = res.getString(R.string.invalid_passcode_detail);
                            logger.debug(eDetail, e);
                            ErrorMessage eMsg = new ErrorMessage(eTitle, eDetail);
                            SAPWizardApplication.getErrorHandler().sendErrorMessage(eMsg);
                        }
                    } else {
                        // create with default
                        try {
							byte[] encKey = EncryptionUtil.getEncryptionKey(SAPWizardApplication.APP_SECURE_STORE_PCODE_ALIAS);
                            store.open(encKey);
                        } catch (OpenFailureException | EncryptionError e) {
                            Resources res = applicationContext.getResources();
                            String eTitle = res.getString(R.string.passcode_error);
                            String eDetail = res.getString(R.string.disabled_passcode);
                            logger.debug(eDetail, e);
                            ErrorMessage eMsg = new ErrorMessage(eTitle, eDetail);
                            SAPWizardApplication.getErrorHandler().sendErrorMessage(eMsg);
                        }
                    }
                } else {
                    try {
						EncryptionUtil.enablePasscode(SAPWizardApplication.APP_SECURE_STORE_PCODE_ALIAS, passcode);
                        byte[] encKey = EncryptionUtil.getEncryptionKey(SAPWizardApplication.APP_SECURE_STORE_PCODE_ALIAS, passcode);
                        store.open(encKey);
						
						// save the timestamp
						SecureKeyValueStore rlmStore = SAPWizardApplication.getApplicatiton().getRlmStore();
						rlmStore.put(ClientPolicyUtilities.KEY_PC_WAS_SET_AT, Calendar.getInstance());
						rlmStore.close();
                    } catch (OpenFailureException | EncryptionError e) {
                        Resources res = applicationContext.getResources();
                        ErrorMessage eMsg = new ErrorMessage(res.getString(R.string.passcode_error), res.getString(R.string.invalid_passcode));
                        SAPWizardApplication.getErrorHandler().sendErrorMessage(eMsg);
                        throw new PasscodeValidationException("Invalid Passcode", e);
                    }
                }

                SAPWizardApplication.getApplicatiton().setIsOnBoarded(true);
                SAPWizardApplication.getApplicatiton().setIsUserPasscode(true);
                break;
            case CHANGE:
                try {
                     if (old_passcode != null) {
                        // from user passcode
                        EncryptionUtil.changePasscode(SAPWizardApplication.APP_SECURE_STORE_PCODE_ALIAS, old_passcode, passcode);
                        Arrays.fill(old_passcode, ' ');
                        old_passcode = null;
                    } else {
                        // from default passcode
                        EncryptionUtil.enablePasscode(SAPWizardApplication.APP_SECURE_STORE_PCODE_ALIAS, passcode);
                        byte[] encKey = EncryptionUtil.getEncryptionKey(SAPWizardApplication.APP_SECURE_STORE_PCODE_ALIAS, passcode);
                        store.open(encKey);
                        SAPWizardApplication.getApplicatiton().setIsUserPasscode(true);
                    }
					
					// save the timestamp
					SecureKeyValueStore rlmStore = SAPWizardApplication.getApplicatiton().getRlmStore();
					rlmStore.put(ClientPolicyUtilities.KEY_PC_WAS_SET_AT, Calendar.getInstance());
					rlmStore.close();
                } catch (EncryptionError | OpenFailureException e) {
                    Resources res = applicationContext.getResources();
                    ErrorMessage eMsg = new ErrorMessage(res.getString(R.string.passcode_change_error), res.getString(R.string.passcode_change_error_detail));
                    SAPWizardApplication.getErrorHandler().sendErrorMessage(eMsg);
                    throw new PasscodeValidationException("Invalid Passcode", e);
                }
                break;
            case MATCH:
                matchPasscode(applicationContext, passcode);
                // client policy refresh is forced from the server
                if (!ClientPolicyUtilities.getClientPolicy(true).getPcPolicy().validate(passcode) ||
                        SAPWizardApplication.getApplicatiton().isPasscodeExpired()) {
                    changePasscodeLatch = new CountDownLatch(1);
                    fragment.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(fragment.getActivity());
                            Resources res = applicationContext.getResources();
                            alertBuilder.setTitle(res.getString(R.string.new_policy_required));
                            alertBuilder.setMessage(res.getString(R.string.passcode_policy_changed));
                            alertBuilder.setPositiveButton(res.getString(R.string.ok), null);
                            alertBuilder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    Intent intent = new Intent(fragment.getActivity(), SetPasscodeActivity.class);
                                    SetPasscodeSettings setPasscodeSettings = new SetPasscodeSettings();
                                    setPasscodeSettings.setChangePasscode(true);
                                    setPasscodeSettings.saveToIntent(intent);
                                    fragment.getActivity().startActivity(intent);
                                    changePasscodeLatch.countDown();
                                }
                            });
                            alertBuilder.create().show();
                        }
                    });
                    try {
                        changePasscodeLatch.await();
                    } catch (InterruptedException e) {
                        logger.error("InterruptedException while changing passcode: " + e.getMessage(), e);
                    }
                }
                break;
            case MATCHFORCHANGE:
				 matchPasscode(applicationContext, passcode);
                 break;
            default:
                throw new Error("Unknown input mode");
        }
    }

    private void matchPasscode(Context ctx, char[] pCode) throws PasscodeValidationFailedToMatchException {
        SecureKeyValueStore rlmStore = SAPWizardApplication.getApplicatiton().getRlmStore();
        int rtLimit = rlmStore.getInt(ClientPolicyUtilities.KEY_RETRY_LIMIT);
        int rtCount = rlmStore.getInt(ClientPolicyUtilities.KEY_RETRY_COUNT);
        store.close();
        try {
            byte[] encKey = EncryptionUtil.getEncryptionKey(SAPWizardApplication.APP_SECURE_STORE_PCODE_ALIAS, pCode);
            store.open(encKey);
            old_passcode = pCode;
        } catch (OpenFailureException | EncryptionError e) {
            // invalid passcode
            rtCount++;
            int remaining = rtLimit - rtCount;
            rlmStore.put(ClientPolicyUtilities.KEY_RETRY_COUNT, rtCount);
            Resources res = ctx.getResources();
            throw new PasscodeValidationFailedToMatchException(res.getString(R.string.invalid_passcode), remaining, e);
		} finally {
            rlmStore.close();
		}
    }

    @Override
    public void shouldResetPasscode(Fragment fragment) {
		 // reset the application: delete the secure stores
        store.deleteStore(fragment.getContext());
        SecureKeyValueStore rlmStore = SAPWizardApplication.getApplicatiton().getRlmStore();
        rlmStore.deleteStore(fragment.getContext());
		
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
		
        // OAuth tokens and Basic credentials are stored in the applications
        // secure store, so they are cleaned automatically
		
        try {
            EncryptionUtil.delete(SAPWizardApplication.RLM_SECURE_STORE_PCODE_ALIAS);
            EncryptionUtil.delete(SAPWizardApplication.APP_SECURE_STORE_PCODE_ALIAS);
        } catch (EncryptionError encryptionError) {
            logger.error("Encryption keys couln't be cleaned!");
        }

        // set onboarding state
        SAPWizardApplication.getApplicatiton().setIsOnBoarded(false);

        // kill the application
        ActivityManager activityManager = (ActivityManager) fragment.getContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.AppTask> tasks = activityManager.getAppTasks();
        for (ActivityManager.AppTask task : tasks) {
            task.finishAndRemoveTask();
        }
    }

    @Override
    public void didSkipPasscodeSetup(Fragment fragment) {
        logger.info("didSkipPasscodeSetup");

        SecureKeyValueStore store = SAPWizardApplication.getApplicatiton().getStore();

        if (store != null) {
            try {
                  if (old_passcode != null) {
                    EncryptionUtil.disablePasscode(SAPWizardApplication.APP_SECURE_STORE_PCODE_ALIAS, old_passcode);
                    byte[] encKey = EncryptionUtil.getEncryptionKey(SAPWizardApplication.APP_SECURE_STORE_PCODE_ALIAS);
                    store.changeEncryptionKey(encKey);
                    Arrays.fill(old_passcode, ' ');
                    old_passcode = null;
                }
            } catch (EncryptionError e) {
                Resources res = fragment.getActivity().getResources();
                ErrorMessage eMsg = new ErrorMessage(res.getString(R.string.passcode_change_error), res.getString(R.string.passcode_change_error_detail_default));
                SAPWizardApplication.getErrorHandler().sendErrorMessage(eMsg);
            }
        } else {
            store = new SecureKeyValueStore(fragment.getActivity(), SAPWizardApplication.APP_SECURE_STORE_NAME);
            try {
                byte[] encKey = EncryptionUtil.getEncryptionKey(SAPWizardApplication.APP_SECURE_STORE_PCODE_ALIAS);
                store.open(encKey);
            } catch (OpenFailureException | EncryptionError e) {
                logger.debug("Store already existed with non-default key when trying to skip passcode!", e);
            }
        }
        SAPWizardApplication.getApplicatiton().setAppStore(store);
        SAPWizardApplication.getApplicatiton().setIsUserPasscode(false);
        SAPWizardApplication.getApplicatiton().setIsOnBoarded(true);

        Intent intent = new Intent();
        fragment.getActivity().setResult(Activity.RESULT_OK, intent);
        fragment.getActivity().finish();
    }

    /**
     * SAPWizardApplication.getApplicaiton()
     * Starts retrieving the passcode policy.
     *
     * @param fragment the enclosing fragment invoking this handler, must be non-null
     * @return the passcode policy
     */
    @Override
    public PasscodePolicy getPasscodePolicy(Fragment fragment) {
        logger.debug("Get PasscodePolicy");
        return ClientPolicyUtilities.getClientPolicy(true).getPcPolicy();
    }
}

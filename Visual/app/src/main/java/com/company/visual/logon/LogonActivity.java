package com.company.visual.logon;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;

import com.company.visual.R;
import com.company.visual.app.ErrorMessage;
import com.company.visual.app.SAPWizardApplication;
import com.company.visual.mdui.EntitySetListActivity;

import com.sap.cloud.mobile.foundation.common.EncryptionError;
import com.sap.cloud.mobile.foundation.common.EncryptionState;
import com.sap.cloud.mobile.foundation.common.EncryptionUtil;
import com.sap.cloud.mobile.foundation.securestore.OpenFailureException;
import com.sap.cloud.mobile.foundation.securestore.SecureKeyValueStore;

import com.sap.cloud.mobile.onboarding.launchscreen.LaunchScreenSettings;
import com.sap.cloud.mobile.onboarding.passcode.SetPasscodeSettings;
import com.sap.cloud.mobile.onboarding.utility.OnboardingType;
import com.sap.cloud.mobile.onboarding.passcode.SetPasscodeActivity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import static  com.company.visual.app.SAPWizardApplication.APP_SECURE_STORE_NAME;
import static  com.company.visual.app.SAPWizardApplication.APP_SECURE_STORE_PCODE_ALIAS;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class LogonActivity extends AppCompatActivity {

    private static final int LAUNCH_SCREEN = 100;
    private static final int SET_PASSCODE = 200;
    private static final int ENTER_PASSCODE = 300;
    private static final int ENTITYSET_LIST = 400;
    private static Logger logger = LoggerFactory.getLogger(LogonActivity.class);

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case LAUNCH_SCREEN:
                switch (resultCode) {
                    case RESULT_OK:
                        setPasscode();
                        break;
                    case CONTEXT_IGNORE_SECURITY:
                         startEntitySetListActivity();
                        break;
                    case RESULT_CANCELED:
                        moveTaskToBack(true);
                        break;
                    default:
                        startLaunchScreen();
                        break;
                }
                break;
            case SET_PASSCODE:
                switch (resultCode) {
                    case RESULT_OK:
                          startEntitySetListActivity();
                        break;
                    case RESULT_CANCELED:
                        startLaunchScreen();
                        break;
                }
                break;
            case ENTER_PASSCODE:
                   switch (resultCode) {
                   case RESULT_OK:
                       startEntitySetListActivity();
                       break;
                   case RESULT_CANCELED:
                       ErrorMessage eMsg = new ErrorMessage(null, getResources().getString(R.string.error_back_navigation), null, true);
                       SAPWizardApplication.getErrorHandler().sendErrorMessage(eMsg);
                       break;
               }
               break;
             case ENTITYSET_LIST:
                // TODO check if something should be done here?
                break;

            default:
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logon);

        SecureKeyValueStore store = SAPWizardApplication.getApplicatiton().getStore();
        if (store == null) {
            store = new SecureKeyValueStore(this, APP_SECURE_STORE_NAME);
            SAPWizardApplication.getApplicatiton().setAppStore(store);
        }

        boolean isOnBoarded = ((SAPWizardApplication) getApplication()).isOnBoarded();
        if (!isOnBoarded) {
            // create a secure store with default passcode
            EncryptionUtil.initialize(this);
            try {
				        // create the store for application data (with default passcode)
                byte[] appEncKey = EncryptionUtil.getEncryptionKey(APP_SECURE_STORE_PCODE_ALIAS);
                SecureKeyValueStore appStore = new SecureKeyValueStore(this, APP_SECURE_STORE_NAME);
                SAPWizardApplication.getApplicatiton().setAppStore(appStore);
                appStore.open(appEncKey);
            } catch (EncryptionError | OpenFailureException e) {
				        logger.error("Application secure store couldn't be created at startup.");
                SAPWizardApplication.getApplicatiton().setAppStore(null);
            }
            startLaunchScreen();
        } else {
            boolean isUserPasscode = SAPWizardApplication.getApplicatiton().isUserPasscode();
            if (isUserPasscode) {
                // user passcode
                if (store.isOpen()) {
                    startEntitySetListActivity();
                } else {
                    enterPasscode();
                }
            } else {
                  // default passcode
                  openStore(store);
                  Thread pThread = new Thread(new Runnable() {
                      @Override
                      public void run() {
                          boolean isPolicyEnabled = ClientPolicyUtilities.getClientPolicy(true).isPasscodePolicyEnabled();
                          SAPWizardApplication.getApplicatiton().setIsPasscodePolicyEnabled(isPolicyEnabled);
                          boolean isDefaultEnabled = ClientPolicyUtilities.getClientPolicy(false).getPcPolicy().isSkipEnabled();
                          if (isPolicyEnabled && !isDefaultEnabled) {
                              LogonActivity.this.runOnUiThread(new Runnable() {
                                  @Override
                                  public void run() {
                                      AlertDialog.Builder alertBuilder = new AlertDialog.Builder(LogonActivity.this);
                                      Resources res = LogonActivity.this.getResources();
                                      alertBuilder.setTitle(res.getString(R.string.new_policy_required));
                                      alertBuilder.setMessage(res.getString(R.string.passcode_policy_changed));
                                      alertBuilder.setPositiveButton(res.getString(R.string.ok), null);
                                      alertBuilder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                          @Override
                                          public void onDismiss(DialogInterface dialog) {
                                              Intent intent = new Intent(LogonActivity.this, SetPasscodeActivity.class);
                                              SetPasscodeSettings setPasscodeSettings = new SetPasscodeSettings();
                                              setPasscodeSettings.setChangePasscode(true);
                                              setPasscodeSettings.saveToIntent(intent);
                                              LogonActivity.this.startActivityForResult(intent, SET_PASSCODE);
                                          }
                                      });
                                      alertBuilder.create().show();
                                  }
                              });
                          } else {
                                startEntitySetListActivity();
                          }
                      }
                  });
                  pThread.start();
            }
        }
    }

    private void startLaunchScreen() {
        Intent welcome = new Intent(this,
                com.sap.cloud.mobile.onboarding.launchscreen.LaunchScreenActivity.class);
        LaunchScreenSettings launchScreenSettings = new LaunchScreenSettings();
        launchScreenSettings.setDemoAvailable(false);
        launchScreenSettings.setLaunchScreenHeadline(getString(R.string.welcome_screen_headline_label));
        launchScreenSettings.setWelcomeScreenType(OnboardingType.STANDARD_ONBOARDING);
        launchScreenSettings.setLaunchScreenTitles(new String[]{getString(R.string.application_name)});
        launchScreenSettings.setLaunchScreenImages(new int[]{R.drawable.ic_android_white});
        launchScreenSettings.setLaunchScreenDescriptions(new String[]{getString(R.string.welcome_screen_detail_label)});
        launchScreenSettings.saveToIntent(welcome);
        startActivityForResult(welcome, LAUNCH_SCREEN);
    }

    private void setPasscode() {
         Thread pThread = new Thread(new Runnable() {
             @Override
             public void run() {
                 boolean isPolicyEnabled = ClientPolicyUtilities.getClientPolicy(true).isPasscodePolicyEnabled();
                 SAPWizardApplication.getApplicatiton().setIsPasscodePolicyEnabled(isPolicyEnabled);

                 if (SAPWizardApplication.getApplicatiton().isPasscodePolicyEnabled()) {
                     Intent i = new Intent(LogonActivity.this,
                             com.sap.cloud.mobile.onboarding.passcode.SetPasscodeActivity.class);
                     startActivityForResult(i, SET_PASSCODE);
                 } else {
                     SecureKeyValueStore store = SAPWizardApplication.getApplicatiton().getStore();
                     if (store != null) {
                         openStore(store);
                     } else {
                         store = new SecureKeyValueStore(LogonActivity.this, APP_SECURE_STORE_NAME);
                         SAPWizardApplication.getApplicatiton().setAppStore(store);
                         openStore(store);
                     }
                     SAPWizardApplication.getApplicatiton().setIsDefaultPasscodeEnabled(true);
                     SAPWizardApplication.getApplicatiton().setIsOnBoarded(true);
                     SAPWizardApplication.getApplicatiton().setIsUserPasscode(false);
                     startEntitySetListActivity();
                 }
             }
         });
         pThread.start();
     }

    private void enterPasscode() {
        // client policy is refreshed now in the UI callback

        Intent i = new Intent(this, com.sap.cloud.mobile.onboarding.passcode.EnterPasscodeActivity.class);
        startActivityForResult(i, ENTER_PASSCODE);
    }

    private void startEntitySetListActivity() {
        Intent intent = new Intent(this, EntitySetListActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivityForResult(intent, ENTITYSET_LIST);
    }

    private void openStore(SecureKeyValueStore store) {
        if (store != null && !store.isOpen()
			  && EncryptionUtil.getState(APP_SECURE_STORE_PCODE_ALIAS) == EncryptionState.NO_PASSCODE) {
            try {
				byte[] encKey = EncryptionUtil.getEncryptionKey(APP_SECURE_STORE_PCODE_ALIAS);
                store.open(encKey);
            } catch (EncryptionError  | OpenFailureException e) {
                String eTitle = getResources().getString(R.string.secure_store_error);
                String eDetail = getResources().getString(R.string.secure_store_open_default_error_detail);
                ErrorMessage eMsg = new ErrorMessage(eTitle, eDetail, e, false);
                SAPWizardApplication.getApplicatiton().getErrorHandler().sendErrorMessage(eMsg);
            }
        }
    }
}

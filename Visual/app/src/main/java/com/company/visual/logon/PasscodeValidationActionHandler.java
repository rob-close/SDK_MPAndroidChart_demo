package com.company.visual.logon;

import com.sap.cloud.mobile.onboarding.passcode.PasscodeValidationException;
import com.sap.cloud.mobile.onboarding.passcode.PasscodeValidationFailedToMeetPolicy;

public class PasscodeValidationActionHandler implements com.sap.cloud.mobile.onboarding.passcode.PasscodeValidationActionHandler {

    @Override
    public void validate (char[] passcode) throws PasscodeValidationException{
        //TODO
        // You can extend the validator with your own policy.

    }
}

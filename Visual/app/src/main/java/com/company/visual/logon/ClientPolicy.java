package com.company.visual.logon;

import com.sap.cloud.mobile.onboarding.passcode.PasscodePolicy;

import java.io.Serializable;

import ch.qos.logback.classic.Level;

/**
 * Wrapper class which contains the client policies, which could arrive from the server. It contains
 * the {@link com.sap.cloud.mobile.onboarding.passcode.PasscodePolicy}, the log settings and some
 * boolean flags, e.g. whether passode policy is enabled.
 */
public class ClientPolicy implements Serializable{

    private boolean isPasscodePolicyEnabled;
    private PasscodePolicy pcPolicy;
    private Level logLevel;
    private boolean isLogEnabled;

    public boolean isPasscodePolicyEnabled() {
        return isPasscodePolicyEnabled;
    }

    public void setPasscodePolicyEnabled(boolean passcodePolicyEnabled) {
        isPasscodePolicyEnabled = passcodePolicyEnabled;
    }

    public PasscodePolicy getPcPolicy() {
        return pcPolicy;
    }

    public void setPcPolicy(PasscodePolicy pcPolicy) {
        this.pcPolicy = pcPolicy;
    }

    public Level getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(Level logLevel) {
        this.logLevel = logLevel;
    }

    public boolean isLogEnabled() {
        return isLogEnabled;
    }

    public void setLogEnabled(boolean logEnabled) {
        isLogEnabled = logEnabled;
    }
}

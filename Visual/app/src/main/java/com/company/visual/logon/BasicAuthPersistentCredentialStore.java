package com.company.visual.logon;

import  com.company.visual.app.SAPWizardApplication;

import com.sap.cloud.mobile.foundation.authentication.BasicAuthCredentialStore;
import com.sap.cloud.mobile.foundation.securestore.SecureKeyValueStore;

import java.util.HashMap;
import java.util.Map;

/**
 * This class implements a {@link BasicAuthCredentialStore}, where the credentials are
 * persisted in the applications secure store. The implementation of the logon flow guaranties,
 * that the secure store is already present, when the credential store needs it.
 */

@SuppressWarnings("unchecked")
public class BasicAuthPersistentCredentialStore implements BasicAuthCredentialStore {

    private static final String CRED_KEY = "basicauth_credentials";

    private static class SingletonHolder {
        private static final BasicAuthPersistentCredentialStore INSTANCE = new BasicAuthPersistentCredentialStore();
    }

    public static BasicAuthPersistentCredentialStore getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public synchronized void storeCredential(String rootUrl, String realm, String[] credentials) {
        SecureKeyValueStore store = SAPWizardApplication.getApplicatiton().getStore();
        if (store != null && store.isOpen()) {
            Map<String, String[]> credMap = (Map<String, String[]>) store.getSerializable(CRED_KEY);
            if (credMap == null) {
                credMap = new HashMap<String, String[]>();
            }
            credMap.put(this.makeKey(rootUrl, realm), credentials);
            store.put(CRED_KEY, credMap);
        }
    }

    public synchronized String[] getCredential(String rootUrl, String realm) {
        SecureKeyValueStore store = SAPWizardApplication.getApplicatiton().getStore();
        String[] retVal = null;
        if (store != null && store.isOpen()) {
            Map<String, String[]> credMap = (Map<String, String[]>) store.getSerializable(CRED_KEY);
            if (credMap != null) {
                retVal = credMap.get(makeKey(rootUrl, realm));
            }
        }
        return retVal;
    }

    public synchronized void deleteCredential(String rootUrl, String realm) {
        SecureKeyValueStore store = SAPWizardApplication.getApplicatiton().getStore();
        if (store != null && store.isOpen()) {
            Map<String, String[]> credMap = (Map<String, String[]>) store.getSerializable(CRED_KEY);
            if (credMap != null) {
                credMap.remove(makeKey(rootUrl, realm));
            }
            store.put(CRED_KEY, credMap);
        }
    }

    public synchronized void deleteAllCredentials() {
        SecureKeyValueStore store = SAPWizardApplication.getApplicatiton().getStore();
        if (store != null && store.isOpen()) {
            store.remove(CRED_KEY);
        }
    }

    private String makeKey(String rootUrl, String realm) {
        return rootUrl + "::" + realm;
    }
}


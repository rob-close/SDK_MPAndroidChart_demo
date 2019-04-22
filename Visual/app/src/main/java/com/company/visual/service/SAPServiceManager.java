package com.company.visual.service;

import android.app.Activity;

import  com.sap.cloud.android.odata.espmcontainer.ESPMContainer;
import com.sap.cloud.mobile.odata.OnlineODataProvider;
import com.sap.cloud.mobile.odata.http.OKHttpHandler;

import okhttp3.OkHttpClient;

public class SAPServiceManager {

    private static OnlineODataProvider provider;
    private static ESPMContainer mESPMContainer;
    public static final String SERVICE_URL = "https://hcpms-i826478trial.hanatrial.ondemand.com/";
    public static final String APPLICATION_ID = "com.sap.android.wizard.visual";
    public static final String CONNECTION_ID = "com.sap.android.wizard.visual";
    public static final String APPLICATION_VERSION = "1.0";

    private static final SAPServiceManager ourInstance = new SAPServiceManager();

    public static SAPServiceManager getInstance() {
        return ourInstance;
    }

    public SAPServiceManager() {
        provider = new OnlineODataProvider("SAPService", SERVICE_URL + CONNECTION_ID);
        mESPMContainer = new ESPMContainer(provider);

    }
        public ESPMContainer getSAPService() {
            return mESPMContainer;
        }

    public void initSAPService(OkHttpClient okHttpClient) {
       provider = new OnlineODataProvider("SAPService", SERVICE_URL + CONNECTION_ID);
       provider.getNetworkOptions().setHttpHandler(new OKHttpHandler(okHttpClient));
       provider.getServiceOptions().setCheckVersion(false);
       provider.getServiceOptions().setRequiresType(true);
       provider.getServiceOptions().setCacheMetadata(false);
       mESPMContainer = new ESPMContainer(provider);
    }
}
package com.company.visual.data.connector;

import com.sap.cloud.android.odata.espmcontainer.SalesOrderHeader;
import com.sap.cloud.mobile.odata.EntityValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SalesOrderHeadersUIConnector extends EntityValueUIConnector {

    private SalesOrderHeader mSalesOrderHeader;

     private final String[] kNamesArray = {
     "SalesOrderId"
      };
     private final String masterProp = "CreatedAt";
     // ordered, key fields are first
     private final String[] pNamesArray = {
         "SalesOrderId",
         "CreatedAt",
         "CurrencyCode",
         "CustomerId",
         "GrossAmount",
         "LifeCycleStatus",
         "LifeCycleStatusName",
         "NetAmount",
         "TaxAmount"
        };

    private Map<String, String> pValues = new HashMap<>();

    public SalesOrderHeadersUIConnector() {
        this.mSalesOrderHeader = new SalesOrderHeader(true);
    }

    public SalesOrderHeadersUIConnector(SalesOrderHeader entity) {
        this.mSalesOrderHeader = entity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMasterPropertyName() {
        return masterProp;
    }

     /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getKeyPropertyNames() {
        List<String> kpNames = new ArrayList<>();
        kpNames.addAll(Arrays.asList(kNamesArray));
        return kpNames;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> getPropertiesWithValues() {
            Map<String, String> pValues = new HashMap<>();
            pValues.put("CurrencyCode", mSalesOrderHeader.getCurrencyCode());
            pValues.put("LifeCycleStatusName", mSalesOrderHeader.getLifeCycleStatusName());
            pValues.put("TaxAmount", String.valueOf(mSalesOrderHeader.getTaxAmount()));
            pValues.put("SalesOrderId", mSalesOrderHeader.getSalesOrderID());
            pValues.put("CreatedAt", String.valueOf(mSalesOrderHeader.getCreatedAt()));
            pValues.put("NetAmount", String.valueOf(mSalesOrderHeader.getNetAmount()));
            pValues.put("LifeCycleStatus", mSalesOrderHeader.getLifeCycleStatus());
            pValues.put("CustomerId", mSalesOrderHeader.getCustomerID());
            pValues.put("GrossAmount", String.valueOf(mSalesOrderHeader.getGrossAmount()));
      return pValues;
    }

   /**
    * {@inheritDoc}
    */
    @Override
    public List<String> getPropertyNames() {
         List<String> pNames = new ArrayList<>();
         pNames.addAll(Arrays.asList(pNamesArray));
         return pNames;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EntityValue getConnectedObject() {
        return this.mSalesOrderHeader;
    }
}

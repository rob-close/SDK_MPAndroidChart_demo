package com.company.visual.data.connector;

import com.sap.cloud.android.odata.espmcontainer.PurchaseOrderHeader;
import com.sap.cloud.mobile.odata.EntityValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PurchaseOrderHeadersUIConnector extends EntityValueUIConnector {

    private PurchaseOrderHeader mPurchaseOrderHeader;

     private final String[] kNamesArray = {
     "PurchaseOrderId"
      };
     private final String masterProp = "CurrencyCode";
     // ordered, key fields are first
     private final String[] pNamesArray = {
         "PurchaseOrderId",
         "CurrencyCode",
         "GrossAmount",
         "NetAmount",
         "SupplierId",
         "TaxAmount"
        };

    private Map<String, String> pValues = new HashMap<>();

    public PurchaseOrderHeadersUIConnector() {
        this.mPurchaseOrderHeader = new PurchaseOrderHeader(true);
    }

    public PurchaseOrderHeadersUIConnector(PurchaseOrderHeader entity) {
        this.mPurchaseOrderHeader = entity;
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
            pValues.put("CurrencyCode", mPurchaseOrderHeader.getCurrencyCode());
            pValues.put("SupplierId", mPurchaseOrderHeader.getSupplierID());
            pValues.put("TaxAmount", String.valueOf(mPurchaseOrderHeader.getTaxAmount()));
            pValues.put("NetAmount", String.valueOf(mPurchaseOrderHeader.getNetAmount()));
            pValues.put("PurchaseOrderId", mPurchaseOrderHeader.getPurchaseOrderID());
            pValues.put("GrossAmount", String.valueOf(mPurchaseOrderHeader.getGrossAmount()));
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
        return this.mPurchaseOrderHeader;
    }
}

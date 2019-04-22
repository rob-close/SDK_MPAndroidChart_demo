package com.company.visual.data.connector;

import com.sap.cloud.android.odata.espmcontainer.PurchaseOrderItem;
import com.sap.cloud.mobile.odata.EntityValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PurchaseOrderItemsUIConnector extends EntityValueUIConnector {

    private PurchaseOrderItem mPurchaseOrderItem;

     private final String[] kNamesArray = {
     "ItemNumber",
     "PurchaseOrderId"
      };
     private final String masterProp = "CurrencyCode";
     // ordered, key fields are first
     private final String[] pNamesArray = {
         "ItemNumber",
         "PurchaseOrderId",
         "CurrencyCode",
         "GrossAmount",
         "NetAmount",
         "ProductId",
         "Quantity",
         "QuantityUnit",
         "TaxAmount"
        };

    private Map<String, String> pValues = new HashMap<>();

    public PurchaseOrderItemsUIConnector() {
        this.mPurchaseOrderItem = new PurchaseOrderItem(true);
    }

    public PurchaseOrderItemsUIConnector(PurchaseOrderItem entity) {
        this.mPurchaseOrderItem = entity;
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
            pValues.put("CurrencyCode", mPurchaseOrderItem.getCurrencyCode());
            pValues.put("TaxAmount", String.valueOf(mPurchaseOrderItem.getTaxAmount()));
            pValues.put("ItemNumber", String.valueOf(mPurchaseOrderItem.getItemNumber()));
            pValues.put("NetAmount", String.valueOf(mPurchaseOrderItem.getNetAmount()));
            pValues.put("PurchaseOrderId", mPurchaseOrderItem.getPurchaseOrderID());
            pValues.put("Quantity", String.valueOf(mPurchaseOrderItem.getQuantity()));
            pValues.put("ProductId", mPurchaseOrderItem.getProductID());
            pValues.put("GrossAmount", String.valueOf(mPurchaseOrderItem.getGrossAmount()));
            pValues.put("QuantityUnit", mPurchaseOrderItem.getQuantityUnit());
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
        return this.mPurchaseOrderItem;
    }
}

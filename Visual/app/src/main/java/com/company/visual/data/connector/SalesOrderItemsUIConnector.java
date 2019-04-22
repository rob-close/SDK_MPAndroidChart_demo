package com.company.visual.data.connector;

import com.sap.cloud.android.odata.espmcontainer.SalesOrderItem;
import com.sap.cloud.mobile.odata.EntityValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SalesOrderItemsUIConnector extends EntityValueUIConnector {

    private SalesOrderItem mSalesOrderItem;

     private final String[] kNamesArray = {
     "ItemNumber",
     "SalesOrderId"
      };
     private final String masterProp = "CurrencyCode";
     // ordered, key fields are first
     private final String[] pNamesArray = {
         "ItemNumber",
         "SalesOrderId",
         "CurrencyCode",
         "DeliveryDate",
         "GrossAmount",
         "NetAmount",
         "ProductId",
         "Quantity",
         "QuantityUnit",
         "TaxAmount"
        };

    private Map<String, String> pValues = new HashMap<>();

    public SalesOrderItemsUIConnector() {
        this.mSalesOrderItem = new SalesOrderItem(true);
    }

    public SalesOrderItemsUIConnector(SalesOrderItem entity) {
        this.mSalesOrderItem = entity;
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
            pValues.put("CurrencyCode", mSalesOrderItem.getCurrencyCode());
            pValues.put("TaxAmount", String.valueOf(mSalesOrderItem.getTaxAmount()));
            pValues.put("ItemNumber", String.valueOf(mSalesOrderItem.getItemNumber()));
            pValues.put("SalesOrderId", mSalesOrderItem.getSalesOrderID());
            pValues.put("NetAmount", String.valueOf(mSalesOrderItem.getNetAmount()));
            pValues.put("Quantity", String.valueOf(mSalesOrderItem.getQuantity()));
            pValues.put("ProductId", mSalesOrderItem.getProductID());
            pValues.put("DeliveryDate", String.valueOf(mSalesOrderItem.getDeliveryDate()));
            pValues.put("GrossAmount", String.valueOf(mSalesOrderItem.getGrossAmount()));
            pValues.put("QuantityUnit", mSalesOrderItem.getQuantityUnit());
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
        return this.mSalesOrderItem;
    }
}

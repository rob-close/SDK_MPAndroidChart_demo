package com.company.visual.data.connector;

import com.sap.cloud.android.odata.espmcontainer.Stock;
import com.sap.cloud.mobile.odata.EntityValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StockUIConnector extends EntityValueUIConnector {

    private Stock mStock;

     private final String[] kNamesArray = {
     "ProductId"
      };
     private final String masterProp = "LotSize";
     // ordered, key fields are first
     private final String[] pNamesArray = {
         "ProductId",
         "LotSize",
         "MinStock",
         "Quantity",
         "QuantityLessMin",
         "UpdatedTimestamp"
        };

    private Map<String, String> pValues = new HashMap<>();

    public StockUIConnector() {
        this.mStock = new Stock(true);
    }

    public StockUIConnector(Stock entity) {
        this.mStock = entity;
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
            pValues.put("MinStock", String.valueOf(mStock.getMinStock()));
            pValues.put("UpdatedTimestamp", String.valueOf(mStock.getUpdatedTimestamp()));
            pValues.put("Quantity", String.valueOf(mStock.getQuantity()));
            pValues.put("ProductId", mStock.getProductID());
            pValues.put("QuantityLessMin", String.valueOf(mStock.getQuantityLessMin()));
            pValues.put("LotSize", String.valueOf(mStock.getLotSize()));
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
        return this.mStock;
    }
}

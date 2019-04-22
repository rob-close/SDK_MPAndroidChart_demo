package com.company.visual.data.connector;

import com.sap.cloud.android.odata.espmcontainer.ProductText;
import com.sap.cloud.mobile.odata.EntityValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductTextsUIConnector extends EntityValueUIConnector {

    private ProductText mProductText;

     private final String[] kNamesArray = {
     "Id"
      };
     private final String masterProp = "Language";
     // ordered, key fields are first
     private final String[] pNamesArray = {
         "Id",
         "Language",
         "LongDescription",
         "Name",
         "ProductId",
         "ShortDescription"
        };

    private Map<String, String> pValues = new HashMap<>();

    public ProductTextsUIConnector() {
        this.mProductText = new ProductText(true);
    }

    public ProductTextsUIConnector(ProductText entity) {
        this.mProductText = entity;
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
            pValues.put("LongDescription", mProductText.getLongDescription());
            pValues.put("Language", mProductText.getLanguage());
            pValues.put("ProductId", mProductText.getProductID());
            pValues.put("Id", String.valueOf(mProductText.getId()));
            pValues.put("Name", mProductText.getName());
            pValues.put("ShortDescription", mProductText.getShortDescription());
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
        return this.mProductText;
    }
}

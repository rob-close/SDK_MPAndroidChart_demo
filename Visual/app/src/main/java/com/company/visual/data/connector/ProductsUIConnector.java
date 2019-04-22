package com.company.visual.data.connector;

import com.sap.cloud.android.odata.espmcontainer.Product;
import com.sap.cloud.mobile.odata.EntityValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductsUIConnector extends EntityValueUIConnector {

    private Product mProduct;

     private final String[] kNamesArray = {
     "ProductId"
      };
     private final String masterProp = "Category";
     // ordered, key fields are first
     private final String[] pNamesArray = {
         "ProductId",
         "Category",
         "CategoryName",
         "CurrencyCode",
         "DimensionDepth",
         "DimensionHeight",
         "DimensionUnit",
         "DimensionWidth",
         "LongDescription",
         "Name",
         "PictureUrl",
         "Price",
         "QuantityUnit",
         "ShortDescription",
         "SupplierId",
         "UpdatedTimestamp",
         "Weight",
         "WeightUnit"
        };

    private Map<String, String> pValues = new HashMap<>();

    public ProductsUIConnector() {
        this.mProduct = new Product(true);
    }

    public ProductsUIConnector(Product entity) {
        this.mProduct = entity;
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
            pValues.put("UpdatedTimestamp", String.valueOf(mProduct.getUpdatedTimestamp()));
            pValues.put("Category", mProduct.getCategory());
            pValues.put("WeightUnit", mProduct.getWeightUnit());
            pValues.put("ProductId", mProduct.getProductID());
            pValues.put("DimensionUnit", mProduct.getDimensionUnit());
            pValues.put("DimensionHeight", String.valueOf(mProduct.getDimensionHeight()));
            pValues.put("Weight", String.valueOf(mProduct.getWeight()));
            pValues.put("Name", mProduct.getName());
            pValues.put("ShortDescription", mProduct.getShortDescription());
            pValues.put("CurrencyCode", mProduct.getCurrencyCode());
            pValues.put("DimensionWidth", String.valueOf(mProduct.getDimensionWidth()));
            pValues.put("SupplierId", mProduct.getSupplierID());
            pValues.put("LongDescription", mProduct.getLongDescription());
            pValues.put("Price", String.valueOf(mProduct.getPrice()));
            pValues.put("CategoryName", mProduct.getCategoryName());
            pValues.put("PictureUrl", mProduct.getPictureUrl());
            pValues.put("DimensionDepth", String.valueOf(mProduct.getDimensionDepth()));
            pValues.put("QuantityUnit", mProduct.getQuantityUnit());
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
        return this.mProduct;
    }
}

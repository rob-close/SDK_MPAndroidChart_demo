package com.company.visual.data.connector;

import com.sap.cloud.android.odata.espmcontainer.ProductCategory;
import com.sap.cloud.mobile.odata.EntityValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductCategoriesUIConnector extends EntityValueUIConnector {

    private ProductCategory mProductCategory;

     private final String[] kNamesArray = {
     "Category"
      };
     private final String masterProp = "CategoryName";
     // ordered, key fields are first
     private final String[] pNamesArray = {
         "Category",
         "CategoryName",
         "MainCategory",
         "MainCategoryName",
         "NumberOfProducts",
         "UpdatedTimestamp"
        };

    private Map<String, String> pValues = new HashMap<>();

    public ProductCategoriesUIConnector() {
        this.mProductCategory = new ProductCategory(true);
    }

    public ProductCategoriesUIConnector(ProductCategory entity) {
        this.mProductCategory = entity;
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
            pValues.put("UpdatedTimestamp", String.valueOf(mProductCategory.getUpdatedTimestamp()));
            pValues.put("Category", mProductCategory.getCategory());
            pValues.put("MainCategoryName", mProductCategory.getMainCategoryName());
            pValues.put("NumberOfProducts", String.valueOf(mProductCategory.getNumberOfProducts()));
            pValues.put("CategoryName", mProductCategory.getCategoryName());
            pValues.put("MainCategory", mProductCategory.getMainCategory());
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
        return this.mProductCategory;
    }
}

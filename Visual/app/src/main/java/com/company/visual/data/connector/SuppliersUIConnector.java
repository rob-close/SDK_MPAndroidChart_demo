package com.company.visual.data.connector;

import com.sap.cloud.android.odata.espmcontainer.Supplier;
import com.sap.cloud.mobile.odata.EntityValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SuppliersUIConnector extends EntityValueUIConnector {

    private Supplier mSupplier;

     private final String[] kNamesArray = {
     "SupplierId"
      };
     private final String masterProp = "City";
     // ordered, key fields are first
     private final String[] pNamesArray = {
         "SupplierId",
         "City",
         "Country",
         "EmailAddress",
         "HouseNumber",
         "PhoneNumber",
         "PostalCode",
         "Street",
         "SupplierName",
         "UpdatedTimestamp"
        };

    private Map<String, String> pValues = new HashMap<>();

    public SuppliersUIConnector() {
        this.mSupplier = new Supplier(true);
    }

    public SuppliersUIConnector(Supplier entity) {
        this.mSupplier = entity;
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
            pValues.put("SupplierId", mSupplier.getSupplierID());
            pValues.put("UpdatedTimestamp", String.valueOf(mSupplier.getUpdatedTimestamp()));
            pValues.put("HouseNumber", mSupplier.getHouseNumber());
            pValues.put("SupplierName", mSupplier.getSupplierName());
            pValues.put("Country", mSupplier.getCountry());
            pValues.put("PhoneNumber", mSupplier.getPhoneNumber());
            pValues.put("PostalCode", mSupplier.getPostalCode());
            pValues.put("Street", mSupplier.getStreet());
            pValues.put("City", mSupplier.getCity());
            pValues.put("EmailAddress", mSupplier.getEmailAddress());
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
        return this.mSupplier;
    }
}

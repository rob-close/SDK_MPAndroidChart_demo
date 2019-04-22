package com.company.visual.data.connector;

import com.sap.cloud.android.odata.espmcontainer.Customer;
import com.sap.cloud.mobile.odata.EntityValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomersUIConnector extends EntityValueUIConnector {

    private Customer mCustomer;

     private final String[] kNamesArray = {
     "CustomerId"
      };
     private final String masterProp = "City";
     // ordered, key fields are first
     private final String[] pNamesArray = {
         "CustomerId",
         "City",
         "Country",
         "DateOfBirth",
         "EmailAddress",
         "FirstName",
         "HouseNumber",
         "LastName",
         "PhoneNumber",
         "PostalCode",
         "Street",
         "UpdatedTimestamp"
        };

    private Map<String, String> pValues = new HashMap<>();

    public CustomersUIConnector() {
        this.mCustomer = new Customer(true);
    }

    public CustomersUIConnector(Customer entity) {
        this.mCustomer = entity;
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
            pValues.put("DateOfBirth", String.valueOf(mCustomer.getDateOfBirth()));
            pValues.put("UpdatedTimestamp", String.valueOf(mCustomer.getUpdatedTimestamp()));
            pValues.put("HouseNumber", mCustomer.getHouseNumber());
            pValues.put("FirstName", mCustomer.getFirstName());
            pValues.put("PostalCode", mCustomer.getPostalCode());
            pValues.put("CustomerId", mCustomer.getCustomerID());
            pValues.put("City", mCustomer.getCity());
            pValues.put("EmailAddress", mCustomer.getEmailAddress());
            pValues.put("Country", mCustomer.getCountry());
            pValues.put("PhoneNumber", mCustomer.getPhoneNumber());
            pValues.put("Street", mCustomer.getStreet());
            pValues.put("LastName", mCustomer.getLastName());
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
        return this.mCustomer;
    }
}

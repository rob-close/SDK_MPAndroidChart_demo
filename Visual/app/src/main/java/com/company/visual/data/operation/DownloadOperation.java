package com.company.visual.data.operation;

import android.provider.Settings;

import okhttp3.OkHttpClient;
import java.util.ArrayList;
import java.util.List;

import com.company.visual.mdui.EntitySetListActivity;
import com.company.visual.data.connector.EntityValueUIConnector;
import com.company.visual.service.SAPServiceManager;
import com.company.visual.app.SAPWizardApplication;
import com.sap.cloud.mobile.foundation.networking.AppHeadersInterceptor;
import com.sap.cloud.mobile.foundation.networking.WebkitCookieJar;

import com.company.visual.data.connector.SalesOrderHeadersUIConnector;
import com.company.visual.data.connector.StockUIConnector;
import com.company.visual.data.connector.CustomersUIConnector;
import com.company.visual.data.connector.PurchaseOrderItemsUIConnector;
import com.company.visual.data.connector.SalesOrderItemsUIConnector;
import com.company.visual.data.connector.ProductTextsUIConnector;
import com.company.visual.data.connector.SuppliersUIConnector;
import com.company.visual.data.connector.ProductCategoriesUIConnector;
import com.company.visual.data.connector.PurchaseOrderHeadersUIConnector;
import com.company.visual.data.connector.ProductsUIConnector;
import com.sap.cloud.android.odata.espmcontainer.ESPMContainer;

import com.sap.cloud.mobile.foundation.authentication.BasicAuthDialogAuthenticator;
import com.company.visual.logon.BasicAuthPersistentCredentialStore;


public class DownloadOperation extends CRUDOperation{

    private EntitySetListActivity.EntitySetName eSetName;

    public DownloadOperation(OnODataOperation oCallback, EntitySetListActivity.EntitySetName eSetName) {
        super(oCallback, eSetName);
        this.eSetName = eSetName;
    }

    @Override
    protected OperationResult doInBackground(Object... params) {

        OperationResult downloadOperationResult = null;
        List<EntityValueUIConnector> iList = new ArrayList<>();
        try {

         String devID = Settings.Secure.getString(SAPWizardApplication.getApplicatiton().getContentResolver(), Settings.Secure.ANDROID_ID);

         OkHttpClient basicAuthOkHttpClient = new OkHttpClient.Builder()
                 .addInterceptor(new AppHeadersInterceptor(SAPServiceManager.APPLICATION_ID, devID, SAPServiceManager.APPLICATION_VERSION))
                 .authenticator(new BasicAuthDialogAuthenticator(BasicAuthPersistentCredentialStore.getInstance()))
                 .cookieJar(new WebkitCookieJar())
                 .build();

         SAPServiceManager.getInstance().initSAPService(basicAuthOkHttpClient);

         ESPMContainer mESPMContainer = SAPServiceManager.getInstance().getSAPService();
            switch (eSetName) {
                  case SalesOrderHeaders:
                  List<com.sap.cloud.android.odata.espmcontainer.SalesOrderHeader> salesorderheaders = mESPMContainer.getSalesOrderHeaders();
                  for (com.sap.cloud.android.odata.espmcontainer.SalesOrderHeader i : salesorderheaders) {
                    iList.add(new SalesOrderHeadersUIConnector(i));
                  }
                  break;
                  case Stock:
                  List<com.sap.cloud.android.odata.espmcontainer.Stock> stock = mESPMContainer.getStock();
                  for (com.sap.cloud.android.odata.espmcontainer.Stock i : stock) {
                    iList.add(new StockUIConnector(i));
                  }
                  break;
                  case Customers:
                  List<com.sap.cloud.android.odata.espmcontainer.Customer> customers = mESPMContainer.getCustomers();
                  for (com.sap.cloud.android.odata.espmcontainer.Customer i : customers) {
                    iList.add(new CustomersUIConnector(i));
                  }
                  break;
                  case PurchaseOrderItems:
                  List<com.sap.cloud.android.odata.espmcontainer.PurchaseOrderItem> purchaseorderitems = mESPMContainer.getPurchaseOrderItems();
                  for (com.sap.cloud.android.odata.espmcontainer.PurchaseOrderItem i : purchaseorderitems) {
                    iList.add(new PurchaseOrderItemsUIConnector(i));
                  }
                  break;
                  case SalesOrderItems:
                  List<com.sap.cloud.android.odata.espmcontainer.SalesOrderItem> salesorderitems = mESPMContainer.getSalesOrderItems();
                  for (com.sap.cloud.android.odata.espmcontainer.SalesOrderItem i : salesorderitems) {
                    iList.add(new SalesOrderItemsUIConnector(i));
                  }
                  break;
                  case ProductTexts:
                  List<com.sap.cloud.android.odata.espmcontainer.ProductText> producttexts = mESPMContainer.getProductTexts();
                  for (com.sap.cloud.android.odata.espmcontainer.ProductText i : producttexts) {
                    iList.add(new ProductTextsUIConnector(i));
                  }
                  break;
                  case Suppliers:
                  List<com.sap.cloud.android.odata.espmcontainer.Supplier> suppliers = mESPMContainer.getSuppliers();
                  for (com.sap.cloud.android.odata.espmcontainer.Supplier i : suppliers) {
                    iList.add(new SuppliersUIConnector(i));
                  }
                  break;
                  case ProductCategories:
                  List<com.sap.cloud.android.odata.espmcontainer.ProductCategory> productcategories = mESPMContainer.getProductCategories();
                  for (com.sap.cloud.android.odata.espmcontainer.ProductCategory i : productcategories) {
                    iList.add(new ProductCategoriesUIConnector(i));
                  }
                  break;
                  case PurchaseOrderHeaders:
                  List<com.sap.cloud.android.odata.espmcontainer.PurchaseOrderHeader> purchaseorderheaders = mESPMContainer.getPurchaseOrderHeaders();
                  for (com.sap.cloud.android.odata.espmcontainer.PurchaseOrderHeader i : purchaseorderheaders) {
                    iList.add(new PurchaseOrderHeadersUIConnector(i));
                  }
                  break;
                  case Products:
                  List<com.sap.cloud.android.odata.espmcontainer.Product> products = mESPMContainer.getProducts();
                  for (com.sap.cloud.android.odata.espmcontainer.Product i : products) {
                    iList.add(new ProductsUIConnector(i));
                  }
                  break;
            }
            downloadOperationResult = new OperationResult(iList, OperationResult.Operation.READ);
        } catch (Exception ex) {
            downloadOperationResult = new OperationResult(ex, OperationResult.Operation.READ);
        }
        return downloadOperationResult;
    }
  }

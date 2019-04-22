package com.company.visual.mdui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.company.visual.mdui.ItemListActivity;
import com.sap.cloud.mobile.fiori.object.ObjectCell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.company.visual.R;


public class EntitySetListActivity extends AppCompatActivity {

    private static final int SETTINGS_SCREEN_ITEM = 200;
    private static Logger logger = LoggerFactory.getLogger(EntitySetListActivity.class);
    private static final int BLUE_ANDROID_ICON = R.drawable.ic_android_blue;
    private static final int WHITE_ANDROID_ICON = R.drawable.ic_android_white;

    public enum EntitySetName {
           SalesOrderHeaders("SalesOrderHeaders", R.string.eset_salesorderheaders,BLUE_ANDROID_ICON),
           Stock("Stock", R.string.eset_stock,WHITE_ANDROID_ICON),
           Customers("Customers", R.string.eset_customers,BLUE_ANDROID_ICON),
           PurchaseOrderItems("PurchaseOrderItems", R.string.eset_purchaseorderitems,WHITE_ANDROID_ICON),
           SalesOrderItems("SalesOrderItems", R.string.eset_salesorderitems,BLUE_ANDROID_ICON),
           ProductTexts("ProductTexts", R.string.eset_producttexts,WHITE_ANDROID_ICON),
           Suppliers("Suppliers", R.string.eset_suppliers,BLUE_ANDROID_ICON),
           ProductCategories("ProductCategories", R.string.eset_productcategories,WHITE_ANDROID_ICON),
           PurchaseOrderHeaders("PurchaseOrderHeaders", R.string.eset_purchaseorderheaders,BLUE_ANDROID_ICON),
           Products("Products", R.string.eset_products,WHITE_ANDROID_ICON);

        private int titleId;
        private int iconId;
        private String esName;

        EntitySetName(String name, int titleId, int iconId) {
            this.esName = name;
            this.titleId = titleId;
            this.iconId = iconId;
        }

        public int titleId() {
            return this.titleId;
        }

        public String esName() {
            return this.esName;
        }
    }

    private final List<String> eSetTitles = new ArrayList<>();
    private final Map<String, EntitySetName> eTitleMap = new HashMap<>();

      @Override
      protected void onCreate(Bundle savedInstanceState) {
          super.onCreate(savedInstanceState);

          setContentView(R.layout.activity_entity_list);
          Toolbar toolbar = findViewById(R.id.toolbar);
          setSupportActionBar(toolbar);

          eSetTitles.clear();
          eTitleMap.clear();
          for (EntitySetName eSet : EntitySetName.values()) {
              String eSetTitle = getResources().getString(eSet.titleId());
              eSetTitles.add(eSetTitle);
              eTitleMap.put(eSetTitle, eSet);
          }

          final ListView listview = findViewById(R.id.entity_list);
          final EntitySetListAdapter adapter = new EntitySetListAdapter(this, R.layout.entity_list_element, eSetTitles);

          listview.setAdapter(adapter);

          listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
              @Override
              public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {

                  EntitySetName eSetName = eTitleMap.get(adapter.getItem(position));
                  Context context = EntitySetListActivity.this;
                  Intent intent = new Intent(context, ItemListActivity.class);
                  intent.putExtra(ItemListActivity.ARG_ITEM_SET, eSetName);
                  context.startActivity(intent);
              }
          });
      }

     public class EntitySetListAdapter extends ArrayAdapter<String> {

             EntitySetListAdapter(@NonNull Context context, int resource, List<String> eSetTitles) {
                 super(context, resource, eSetTitles);
             }

             @NonNull
             @Override
             public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                 EntitySetName entitySetName = eTitleMap.get(getItem(position));
                 if (convertView == null) {
                     convertView = LayoutInflater.from(getContext()).inflate(R.layout.entity_list_element, parent, false);
                 }
                 ObjectCell entitySetCell = convertView.findViewById(R.id.entity_set_name);
                 entitySetCell.setHeadline(entitySetName.esName);
                 entitySetCell.setDetailImage(entitySetName.iconId);
                 return convertView;
             }
         }
      @Override
      public void onBackPressed() {
          moveTaskToBack(true);
      }

      @Override
          public void onNewIntent(Intent intent) {
              super.onResume();

          }

    private static final int BAR_CHART_SCREEN_ITEM = 201;
    private static final int LINE_CHART_SCREEN_ITEM = 202;
    private static final int VBAR_CHART_SCREEN_ITEM = 203;
    private static final int COMBO_CHART_SCREEN_ITEM = 204;

      @Override
      public boolean onCreateOptionsMenu(Menu menu) {
          menu.add(0, SETTINGS_SCREEN_ITEM, 0, R.string.menu_item_settings);
          menu.add(0, BAR_CHART_SCREEN_ITEM, 1, "Bar chart");
          menu.add(0, LINE_CHART_SCREEN_ITEM, 1, "Line chart");
          menu.add(0, VBAR_CHART_SCREEN_ITEM, 1, "Vertical Bar chart");
          menu.add(0, COMBO_CHART_SCREEN_ITEM, 1, "Combo chart");
        return true;
      }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        logger.debug("onOptionsItemSelected: " + item.getTitle());
        if (item.getItemId() == SETTINGS_SCREEN_ITEM) {
            logger.debug("settings screen menu item selected.");
            Intent intent = new Intent(this, SettingsActivity.class);
            this.startActivityForResult(intent, SETTINGS_SCREEN_ITEM);
            return true;
        } else if (item.getItemId() == BAR_CHART_SCREEN_ITEM) {
            Intent intent = new Intent(this, BarcharActivity.class);
            this.startActivityForResult(intent, BAR_CHART_SCREEN_ITEM);
            return true;
        } else if (item.getItemId() == VBAR_CHART_SCREEN_ITEM) {
            Intent intent = new Intent(this, VBarchartActivity.class);
            this.startActivityForResult(intent, VBAR_CHART_SCREEN_ITEM);
            return true;
        } else if (item.getItemId() == COMBO_CHART_SCREEN_ITEM) {
            Intent intent = new Intent(this, CombinedchartActivity.class);
            this.startActivityForResult(intent, COMBO_CHART_SCREEN_ITEM);
            return true;
        } else if (item.getItemId() == LINE_CHART_SCREEN_ITEM) {
            Intent intent = new Intent(this, LinechartActivity.class);
            this.startActivityForResult(intent, LINE_CHART_SCREEN_ITEM);
            return true;
        }

        return false;
    }

      @Override
      protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
         logger.debug("EntitySetListActivity::onActivityResult, request code: " + requestCode + " result code: " + resultCode);
         if (requestCode == SETTINGS_SCREEN_ITEM) {
                  logger.debug("Calling AppState to retrieve settings after settings screen is closed.");
         //      SAPWizardApplication.getApplicaiton().getState().retrievePersistedSettings(this);
          }
      }

      protected void onPause() {
         super.onPause();
      }
}

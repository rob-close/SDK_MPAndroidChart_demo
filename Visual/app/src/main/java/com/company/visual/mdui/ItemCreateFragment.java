package com.company.visual.mdui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.company.visual.mdui.EntitySetListActivity;
import com.company.visual.R;
import com.company.visual.data.DataContentUtilities;
import com.company.visual.app.ErrorMessage;
import com.company.visual.app.SAPWizardApplication;

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
import com.company.visual.data.connector.EntityValueUIConnector;
import com.company.visual.data.operation.OnODataOperation;
import com.company.visual.data.operation.OperationResult;
import com.sap.cloud.mobile.fiori.object.ObjectHeader;
import com.sap.cloud.mobile.odata.BooleanValue;
import com.sap.cloud.mobile.odata.DataType;
import com.sap.cloud.mobile.odata.DataValue;
import com.sap.cloud.mobile.odata.DecimalValue;
import com.sap.cloud.mobile.odata.DoubleValue;
import com.sap.cloud.mobile.odata.FloatValue;
import com.sap.cloud.mobile.odata.GeographyValue;
import com.sap.cloud.mobile.odata.GlobalDateTime;
import com.sap.cloud.mobile.odata.GuidValue;
import com.sap.cloud.mobile.odata.IntValue;
import com.sap.cloud.mobile.odata.LocalDateTime;
import com.sap.cloud.mobile.odata.LongValue;
import com.sap.cloud.mobile.odata.Property;
import com.sap.cloud.mobile.odata.ShortValue;
import com.sap.cloud.mobile.odata.StringValue;
import com.sap.cloud.mobile.odata.UnsignedByte;
import com.sap.cloud.mobile.odata.core.GUID;

import java.math.BigDecimal;
import java.util.List;

public class ItemCreateFragment extends Fragment implements OnODataOperation {

    /**
     * Name of the connector-set.
     */
    private EntitySetListActivity.EntitySetName eSetName;

    /**
     * Flag to differentiate update and create scenarios
     */
    private boolean isUpdate;

    /**
     * EntityValueUIConnector data object behind the UI.
     */
    private EntityValueUIConnector connector;

    /**
     * Entity id of the updated item.
     */
    private int eId;
    /**
     * Logger for logging the events
     */
    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(ItemCreateFragment.class);
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ItemCreateFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // hide the object header
        ObjectHeader oHeader = this.getActivity().findViewById(R.id.objectHeader);
        if (oHeader != null) {
            oHeader.setVisibility(View.GONE);
        }

        FloatingActionButton fab = this.getActivity().findViewById(R.id.fab);
        if (fab != null) {
            fab.setVisibility(View.VISIBLE);
            if (isUpdate) {
                fab.setImageResource(android.R.drawable.ic_menu_save);
            } else {
                fab.setImageResource(android.R.drawable.ic_menu_add);
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        if (bundle != null) {
            eSetName = (EntitySetListActivity.EntitySetName) getArguments().getSerializable(ItemDetailActivity.ARG_ITEM_TYPE);
            isUpdate = getArguments().getBoolean(ItemDetailActivity.ARG_UPDATE);
            eId = getArguments().getInt(ItemDetailActivity.ARG_ITEM_ID);
            if (eId != -1) {
                connector = DataContentUtilities.getInstance().getItems().get(eId);
            }
            if (eSetName != null) {
                Activity activity = this.getActivity();
                if (activity != null) {
                  Toolbar toolbar = activity.findViewById(R.id.detail_toolbar);
                  if (toolbar != null) {
                      String title = null;
                      if (isUpdate) {
                          title = getResources().getString(R.string.title_update_fragment);
                          String mName = connector.getMasterPropertyName();
                          String mValue = connector.getPropertiesWithValues().get(mName);
                          toolbar.setTitle(title + " " + mValue);
                      } else {
                          title = getResources().getString(R.string.title_create_fragment);
                          toolbar.setTitle(title + " " + eSetName.esName());
                      }
                  }
              }
            }
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_item_create, container, false);

        if (!isUpdate) {
            connector = createEntityValueUIConnector(eSetName);
        }
        // List for all of the property names, keys first
        final List<String> pNames = connector.getPropertyNames();
        final ItemCreateFragment.ItemAdapter mItemAdapter = new ItemCreateFragment.ItemAdapter(this.getActivity(), R.layout.fragment_item_list, pNames);
        final ListView listview = rootView.findViewById(R.id.add_item_detail_list);
        listview.setAdapter(mItemAdapter);

        Activity activity = this.getActivity();
        FloatingActionButton fab = activity.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // update the data from the screen
                for (int i = 0; i < listview.getChildCount(); i++) {
                    View lItem = listview.getChildAt(i);
                    TextView pView = lItem.findViewById(R.id.property_label);
                    EditText vView = lItem.findViewById(R.id.property_value);

                    String pName = pView.getText().toString();
                    Property p = connector.getConnectedObject().getEntityType().getProperty(pName);
                    String strValue = vView.getText().toString();
                    DataValue pDataValue = convertPropertyForCreate(strValue, p.getDataType());
                    if (pDataValue != null) {
                        connector.getConnectedObject().setDataValue(p, pDataValue);
                    }
                }

                // validate the items
                boolean isValid = true;
                for (int i = 0; i < pNames.size(); i++) {
                    View pView = mItemAdapter.getView(i, null, listview);
                    EditText editText = (EditText) pView.findViewById(R.id.property_value);
                    String pStrValue = editText.getText().toString();
                    Property p = connector.getConnectedObject().getEntityType().getProperty(pNames.get(i));
                    if (!isValidProperty(p, pStrValue)) {
                        String eMsg = getResources().getString(R.string.mandatory_warning);
                        editText.setError(eMsg);
                        isValid = false;
                    }
                }

                if (isValid) {
                    if (isUpdate) {
                        // update the existing item
                        DataContentUtilities.getInstance().update(ItemCreateFragment.this, connector);
                    } else {
                        // create a new item
                        DataContentUtilities.getInstance().create(ItemCreateFragment.this, connector);
                    }
                }
            }
        });
        return rootView;
    }

    private EntityValueUIConnector createEntityValueUIConnector(EntitySetListActivity.EntitySetName eSetName) {

        EntityValueUIConnector connector = null;
        switch (eSetName) {
            case SalesOrderHeaders:
              connector = new SalesOrderHeadersUIConnector();
              break;
            case Stock:
              connector = new StockUIConnector();
              break;
            case Customers:
              connector = new CustomersUIConnector();
              break;
            case PurchaseOrderItems:
              connector = new PurchaseOrderItemsUIConnector();
              break;
            case SalesOrderItems:
              connector = new SalesOrderItemsUIConnector();
              break;
            case ProductTexts:
              connector = new ProductTextsUIConnector();
              break;
            case Suppliers:
              connector = new SuppliersUIConnector();
              break;
            case ProductCategories:
              connector = new ProductCategoriesUIConnector();
              break;
            case PurchaseOrderHeaders:
              connector = new PurchaseOrderHeadersUIConnector();
              break;
            case Products:
              connector = new ProductsUIConnector();
              break;
        }
        return connector;
    }

    /**
     * Simple validataion: checks the presence of mandatory fields.
     * @param p property
     * @param value string value from the UI
     * @return isValid
     */
    private boolean isValidProperty(Property p, String value) {
        boolean isValid = true;
        if (!p.isNullable()) {
            if (value == null || value.length() == 0) {
                isValid = false;
            }
        }
        return isValid;
    }

    @Override
    public void onOperation(OperationResult result) {
        if (result.getError() != null) {
            OperationResult.Operation op = result.getOperation();
            Exception ex = result.getError();
            ErrorMessage eMsg = null;
            switch (op) {
                case UPDATE:
                    eMsg = new ErrorMessage(getResources().getString(R.string.update_failed), getResources().getString(R.string.update_failed_detail), ex, false);
                    break;
                case DELETE:
                    eMsg = new ErrorMessage(getResources().getString(R.string.delete_failed), getResources().getString(R.string.delete_failed_detail), ex, false);
                    break;
                case CREATE:
                    eMsg = new ErrorMessage(getResources().getString(R.string.create_failed), getResources().getString(R.string.create_failed_detail), ex, false);
                    break;
                case READ:
                    eMsg = new ErrorMessage(getResources().getString(R.string.read_failed), getResources().getString(R.string.read_failed_detail), ex, true);
                    break;
            }
            SAPWizardApplication.getErrorHandler().sendErrorMessage(eMsg);
    } else {
            if (result.getResult() != null) {
                switch (result.getOperation()) {
                    case UPDATE:
                        if (getActivity() instanceof ItemListActivity) {
                            // two-pane
                            View view = getActivity().getCurrentFocus();
                            if (view != null) {
                                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                            }

                            Bundle arguments = new Bundle();
                            arguments.putInt(ItemDetailActivity.ARG_ITEM_ID, eId);
                            arguments.putSerializable(ItemDetailActivity.ARG_ITEM_TYPE, eSetName);
                            ItemDetailFragment fragment = new ItemDetailFragment();
                            fragment.setArguments(arguments);
                            getActivity().getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.fragment_item_detail, fragment)
                                    .commit();
                            ((ItemListActivity) getActivity()).refreshItemList();
                        } else {
                            this.getActivity().finish();
                        }
                        break;
                    case CREATE:
                        if (getActivity() instanceof ItemListActivity) {
                            View view = getActivity().getCurrentFocus();
                            if (view != null) {
                                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                            }

                            // restart the activity
                            Intent intent = getActivity().getIntent();
                            getActivity().finish();
                            startActivity(intent);
                        } else {
                            this.getActivity().finish();
                        }
                        break;
                }
            }
        }
    }

    public class ItemAdapter extends ArrayAdapter<String> {

        private final List<String> pNames;

        public ItemAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<String> properties) {
            super(context, resource, properties);

            this.pNames = properties;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.create_list_element, parent, false);
            }

            TextView mProperty = convertView.findViewById(R.id.property_label);
            EditText mValue = convertView.findViewById(R.id.property_value);

            String pName = pNames.get(position);
            Property p = connector.getConnectedObject().getEntityType().getProperty(pName);

            // update the data model, when a view lost focus
            mValue.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (!hasFocus) {
                        String pStrValue = mValue.getText().toString();
                        DataValue pDataValue = convertPropertyForCreate(pStrValue, p.getDataType());
                        if (pDataValue != null) {
                            connector.getConnectedObject().setDataValue(p, pDataValue);
                        }
                    }
                }
            });

            int typeCode = p.getDataType().getCode();
            switch (typeCode) {
                case DataType.INT:
                case DataType.INTEGER:
                case DataType.UNSIGNED_INT:
                case DataType.DECIMAL:
                    mValue.setInputType(InputType.TYPE_CLASS_NUMBER);
                    break;
                case DataType.STRING:
                    mValue.setInputType(InputType.TYPE_CLASS_TEXT);
                    break;
                case DataType.LOCAL_DATE:
                    mValue.setInputType(InputType.TYPE_CLASS_DATETIME);
                    break;
                default:
                    mValue.setInputType(InputType.TYPE_CLASS_TEXT);
            }

                Property pUp = connector.getConnectedObject().getEntityType().getProperty(pName);
                DataValue pDv = connector.getConnectedObject().getDataValue(pUp);
                if ( pDv != null) {
                    mValue.setText(pDv.toString());
                }

                if (isUpdate) {
                    if (connector.getKeyPropertyNames().contains(pName)) {
                        // key fields aren't allowed to be updated
                        mValue.setEnabled(false);
                    } else {
                        mValue.setEnabled(true);
                    }
                }

                mValue.setHint(pName);
                mProperty.setText(pName);
                return convertView;
        }
    }

    /**
     * Converts the string property value to the proper DataValue type.
     * @param strValue read from the UI
     * @param mDataType type of the Property
     * @return the proper DataValue fitting to the type information
     */
    public static DataValue convertPropertyForCreate(String strValue, DataType mDataType) {

        DataValue result = null;
        try {
            switch (mDataType.getCode()) {
                case DataType.STRING:
                    result = StringValue.of(strValue);
                    break;
                case DataType.INT:
                case DataType.INTEGER:
                    result = IntValue.of(Integer.valueOf(strValue));
                    break;
                case DataType.GLOBAL_DATE_TIME:
                    result = GlobalDateTime.parse(strValue);
                    break;
                case DataType.DECIMAL:
                    result = DecimalValue.of(BigDecimal.valueOf(Double.valueOf(strValue)));
                    break;
                case DataType.FLOAT:
                    result = FloatValue.of(Float.valueOf(strValue));
                    break;
                case DataType.GUID_VALUE:
                    result = GuidValue.of(GUID.fromString(strValue));
                    break;
                case DataType.LONG:
                    result = LongValue.of(Long.valueOf(strValue));
                    break;
                case DataType.LOCAL_DATE_TIME:
                    if ("now".equals(strValue)) {
                        result = LocalDateTime.now();
                    } else {
                        result = LocalDateTime.parse(strValue);
                    }
                    break;
                case DataType.UNSIGNED_BYTE:
                    result = UnsignedByte.of(Integer.valueOf(strValue));
                    break;
                case DataType.BOOLEAN:
                    result = BooleanValue.of(Boolean.valueOf(strValue));
                    break;
                case DataType.SHORT:
                    result = ShortValue.of(Short.valueOf(strValue));
                    break;
                case DataType.DOUBLE:
                    result = DoubleValue.of(Double.valueOf(strValue));
                    break;
                case DataType.GEOGRAPHY_POINT:
                    result = GeographyValue.parseAnyWKT(strValue);
                    break;
                default:
                    // Unknown data type handle as string
                    result = StringValue.of(strValue);
                    break;
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
        return result;
    }
}

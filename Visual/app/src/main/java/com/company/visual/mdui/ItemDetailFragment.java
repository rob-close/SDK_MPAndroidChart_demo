package com.company.visual.mdui;

import android.app.Activity;
import android.app.ActionBar;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.company.visual.R;
import com.company.visual.data.DataContentUtilities;
import com.company.visual.data.connector.EntityValueUIConnector;
import com.company.visual.data.operation.OnODataOperation;
import com.company.visual.data.operation.OperationResult;
import com.sap.cloud.mobile.fiori.object.ObjectHeader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A fragment representing a single Item detail screen.
 * This fragment is either contained in a {@link ItemListActivity}
 * in two-pane mode (on tablets) or a {@link ItemDetailActivity}
 * on handsets.
 */
public class ItemDetailFragment extends Fragment implements OnODataOperation {
    /**
    * Logger for logging the events
    */
    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(ItemDetailFragment.class);
    /**
     * The item and item type for the presented entity here.
     */
    private EntityValueUIConnector eValueCon;
    private String eType;
    private int eId;
    private Toolbar toolbar;
    private ObjectHeader objHeader;

    private ActionMode aMode;
    private Context ctx;
    private ItemAdapter iAdapter;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        this.ctx = context;

        if (this.getActivity() instanceof ItemDetailActivity) {
            aMode = this.getActivity().startActionMode(new ActionMode.Callback() {
			
				boolean isActionClicked = false;

                // Called when the action mode is created; startActionMode() was called
                @Override
                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    MenuInflater inflater = mode.getMenuInflater();
                    // TODO rename menu
                    inflater.inflate(R.menu.itemlist_options, menu);
                    return true;
                }

                // Called each time the action mode is shown. Always called after onCreateActionMode, but
                // may be called multiple times if the mode is invalidated.
                @Override
                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    return false; // Return false if nothing is done
                }

                // Called when the user selects a contextual menu item
                @Override
                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                    boolean retVal = false;
					isActionClicked = true;
                    switch (item.getItemId()) {
                        case R.id.update_item:
                            Bundle arguments = new Bundle();
                            arguments.putString(ItemDetailActivity.ARG_ITEM_TYPE, eType);
                            arguments.putBoolean(ItemDetailActivity.ARG_UPDATE, true);
                            arguments.putInt(ItemDetailActivity.ARG_ITEM_ID, eId);
                            ItemCreateFragment fragment = new ItemCreateFragment();
                            fragment.setArguments(arguments);
                            ItemDetailFragment.this.getActivity().getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.item_detail_container, fragment)
                                    .commit();
                            retVal = true;
                            break;
                        case R.id.delete_item:
                            List<EntityValueUIConnector> selectedValues = new ArrayList<>();
                            selectedValues.add(eValueCon);
                            ItemDeleteDialog dDialog = new ItemDeleteDialog(selectedValues, ctx, ItemDetailFragment.this);
                            dDialog.confirmDelete();
                            retVal = true;
                            break;
                    }
                    return retVal;
                }

                // Called when the user exits the action mode
                @Override
                public void onDestroyActionMode(ActionMode mode) {
					if (isActionClicked) {
                        String mpName = eValueCon.getMasterPropertyName();
                        if (toolbar != null) {
                            toolbar.setTitle(eValueCon.getPropertiesWithValues().get(mpName));
                        }
                    } else {
                        ItemDetailFragment.this.getActivity().finish();

                   }
                }
            });
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // finish action mode
        if (aMode != null) {
            aMode.finish();
        }
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ItemDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            if (bundle.containsKey(ItemDetailActivity.ARG_ITEM_ID) && bundle.containsKey(ItemDetailActivity.ARG_ITEM_TYPE)) {
                // Load the data content specified by the fragment
                // arguments. In a real-world scenario, use a Loader
                // to load content from a content provider.

                eId = getArguments().getInt(ItemDetailActivity.ARG_ITEM_ID);

                if (0 <= eId && eId < DataContentUtilities.getInstance().getItems().size()) {
                    eValueCon = DataContentUtilities.getInstance().getItems().get(eId);

                    Activity activity = this.getActivity();
                    if (activity != null) {
                         // initialize the toolbar
                         toolbar = activity.findViewById(R.id.detail_toolbar);
                         String mpName = eValueCon.getMasterPropertyName();
                         if (toolbar != null) {
                             toolbar.setTitle(eValueCon.getPropertiesWithValues().get(mpName));
                         }

                         // initialize the object-header
                         objHeader = this.getActivity().findViewById(R.id.objectHeader);
                         if (objHeader != null) {
                             objHeader.setHeadline(eValueCon.getConnectedObject().getEntityType().getLocalName());
                             List<String> keys = eValueCon.getKeyPropertyNames();
                             StringBuilder subHeadLineBuilder = new StringBuilder();
                             for (String key: keys) {
                                 String keyValue = eValueCon.getPropertiesWithValues().get(key);
                                 subHeadLineBuilder.append(key).append(": ").append(keyValue).append("\n");
                             }
                             objHeader.setSubheadline(subHeadLineBuilder.toString());
                             String mProp = eValueCon.getPropertiesWithValues().get(mpName);
                             objHeader.setTag(mProp, 0);
                             objHeader.setTag(null, 2);
                             objHeader.setTag(null, 1);

                             objHeader.setBody("You can set the header body text here.");
                             objHeader.setFootnote("You can set the header footnote here.");
                             objHeader.setDescription("You can add a detailed item description here.");
                         }
                    }
                }
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_item_detail, container, false);

        // Show the data content as text in a TextView.
        if (eValueCon != null) {
            // collection of all of the properties
            List<String> allProps = eValueCon.getPropertyNames();
            iAdapter = new ItemAdapter(this.getActivity(), R.layout.fragment_item_list, allProps);
            final ListView listview = rootView.findViewById(R.id.item_detail_list);
            listview.setAdapter(iAdapter);
        }
        return rootView;
    }

    @Override
    public void onOperation(OperationResult result) {
        OperationResult.Operation op = result.getOperation();
        switch (op) {
            case UPDATE:
                if (aMode != null) {
                    aMode.finish();
                }
                List<EntityValueUIConnector> iList = result.getResult();
                if (iList != null && iList.size() == 1) {
                    eValueCon = iList.get(0);
                    iAdapter.notifyDataSetChanged();
                } else {
                    // TODO error handling
                }
                break;
            case DELETE:
                this.getActivity().finish();
                break;
                default:
                    LOGGER.error("Current operation is " + op + ". Only UPDATE and DELETE is valid.");
        }

    }

    public class ItemAdapter extends ArrayAdapter<String> {

        private final List<String> pNames;
        private final Map<String, String> valueMap = eValueCon.getPropertiesWithValues();

        public ItemAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<String> properties) {
            super(context, resource, properties);
            this.pNames = properties;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return eValueCon.getPropertyListItem(position, convertView, parent, getContext());
        }
    }
}

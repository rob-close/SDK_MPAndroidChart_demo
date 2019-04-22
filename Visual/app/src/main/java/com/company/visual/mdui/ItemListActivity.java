package com.company.visual.mdui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.company.visual.mdui.EntitySetListActivity;
import com.company.visual.R;
import com.company.visual.app.ErrorMessage;
import com.company.visual.app.SAPWizardApplication;
import com.company.visual.data.DataContentUtilities;
import com.company.visual.data.connector.EntityValueUIConnector;
import com.company.visual.data.operation.OnODataOperation;
import com.company.visual.data.operation.OperationResult;
import com.sap.cloud.mobile.fiori.object.ObjectCell;
import java.util.ArrayList;
import java.util.List;

import static com.company.visual.mdui.ItemDetailActivity.ARG_ITEM_ID;

/**
 * An activity representing a list of Items. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link ItemDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class ItemListActivity extends AppCompatActivity implements OnODataOperation {

    public static final String ARG_ITEM_SET = "entityset";
    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(ItemListActivity.class);
    private SimpleItemRecyclerViewAdapter adapter;
    private SwipeRefreshLayout refreshLayout;

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    /**
     * Entity set which is shown on the list.
     */
    private EntitySetListActivity.EntitySetName eSetName;

    @Override
    protected void onResume() {
        super.onResume();

        if (adapter.aMode != null) {
            adapter.aMode.finish();
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.itemlist_menu, menu);
        return true;
    }

    /*
    * Listen for option item selections so that we receive a notification
    * when the user requests a refresh by selecting the refresh action bar item.
    */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Check if user triggered a refresh:
            case R.id.menu_refresh:
                LOGGER.info(eSetName + " list was refreshed.");
                // Signal SwipeRefreshLayout to start the progress indicator
                refreshLayout.setRefreshing(true);

                // Start the refresh background task.
                // This method calls setRefreshing(false) when it's finished.
                updateItemList();

                return true;
        }
        // User didn't trigger a refresh, let the superclass handle this action
        return super.onOptionsItemSelected(item);
    }

    private void updateItemList() {
        DataContentUtilities.getInstance().download(this);
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_item_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Intent startIntent = getIntent();
        eSetName = (EntitySetListActivity.EntitySetName) startIntent.getSerializableExtra(ARG_ITEM_SET);
        if (eSetName != null) {
            DataContentUtilities.getInstance().init(eSetName);
            String title = getResources().getString(eSetName.titleId());
            setTitle(title);
        }

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mTwoPane) {
                    Bundle arguments = new Bundle();
                    arguments.putSerializable(ItemDetailActivity.ARG_ITEM_TYPE, eSetName);
                    ItemCreateFragment fragment = new ItemCreateFragment();
                    fragment.setArguments(arguments);
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_item_detail, fragment)
                            .commit();
                } else {
                    Context context = view.getContext();
                    Intent intent = new Intent(context, ItemDetailActivity.class);
                    intent.putExtra(ItemDetailActivity.ARG_ITEM_TYPE, eSetName);
                    context.startActivity(intent);
                }
            }
        });

        View recyclerView = findViewById(R.id.item_list);
        if (recyclerView == null) throw new AssertionError();
        setupRecyclerView((RecyclerView) recyclerView, this);

        if (findViewById(R.id.fragment_item_detail) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }

        // setup swipe-to-refresh
        refreshLayout = findViewById(R.id.swiperefresh);
        /*
         * Sets up a SwipeRefreshLayout.OnRefreshListener that is invoked when the user
         * performs a swipe-to-refresh gesture.
         */
        refreshLayout.setOnRefreshListener(
            new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    // This method performs the actual data-refresh operation.
                    // The method calls setRefreshing(false) when it's finished.
                    updateItemList();
                }
            }
        );
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView, Context ctx) {

        this.adapter = new SimpleItemRecyclerViewAdapter(ctx);
        recyclerView.setAdapter(adapter);

        DataContentUtilities dUtil = DataContentUtilities.getInstance();
        dUtil.init(eSetName);

        dUtil.download(this);
    }

    @Override
    public void onOperation(OperationResult result) {

        // if it were a swipe-to-refresh, stop it
        if (refreshLayout.isRefreshing()) {
            refreshLayout.setRefreshing(false);
        }

        if (result.getError() != null) {
            // handle error
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
                    eMsg = new ErrorMessage(getResources().getString(R.string.read_failed), getResources().getString(R.string.read_failed_detail), ex, false);
                    break;
            }
            SAPWizardApplication.getErrorHandler().sendErrorMessage(eMsg);
        } else {
            // ok
            adapter.notifyDataSetChanged();
        }
        if (adapter.aMode != null) {
            adapter.aMode.finish();
        }
    }

    public class SimpleItemRecyclerViewAdapter extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private Context ctx;
        private ActionMode aMode;
        private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

            // Called when the action mode is created; startActionMode() was called
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                // Inflate a menu resource providing context menu items
                MenuInflater inflater = mode.getMenuInflater();
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
                switch (item.getItemId()) {
                    case R.id.update_item:
                        Intent intent = new Intent(ctx, ItemDetailActivity.class);
                        if (mSelectedValues.size() == 1) {
                            int position = mValues.indexOf(mSelectedValues.get(0));
                            if (mTwoPane) {
                                Bundle arguments = new Bundle();
                                arguments.putInt(ItemDetailActivity.ARG_ITEM_ID, position);
                                arguments.putSerializable(ItemDetailActivity.ARG_ITEM_TYPE, eSetName);
                                arguments.putBoolean(ItemDetailActivity.ARG_UPDATE, true);
                                ItemCreateFragment fragment = new ItemCreateFragment();
                                fragment.setArguments(arguments);
                                getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.fragment_item_detail, fragment)
                                        .commit();
                            } else {
                                intent.putExtra(ARG_ITEM_ID, position);
                                intent.putExtra(ItemDetailActivity.ARG_ITEM_TYPE, eSetName);
                                intent.putExtra(ItemDetailActivity.ARG_UPDATE, true);
                                ctx.startActivity(intent);
                            }
                        }
                        retVal = true;
                        break;
                    case R.id.delete_item:
                        ItemDeleteDialog dDialog = new ItemDeleteDialog(mSelectedValues, ctx, ItemListActivity.this);
                        dDialog.confirmDelete();
                        retVal = true;
                        break;
                }
                return retVal;
            }

            // Called when the user exits the action mode
            @Override
            public void onDestroyActionMode(ActionMode mode) {
                aMode = null;
                mSelectedValues.clear();
                notifyDataSetChanged();
            }
        };

        /**
         * Entity values are stored in a list, the selected ones in a separate one.
         */
        private List<EntityValueUIConnector> mValues = DataContentUtilities.getInstance().getItems();
        private List<EntityValueUIConnector> mSelectedValues = new ArrayList<>();

        /**
         * In two-pane mode the active item has to be maintained.
         */
        private int activeItemPos = -1;
        private View activeItemView;
        private EntityValueUIConnector activeEntity;

        public SimpleItemRecyclerViewAdapter(Context ctx) {
            this.ctx = ctx;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.collection_list_element, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {

            final EntityValueUIConnector eValue = mValues.get(holder.getAdapterPosition());
            String mProp = eValue.getMasterPropertyName();
            String mPropValue = eValue.getPropertiesWithValues().get(mProp);

            holder.mContentView.setHeadline(mPropValue);
            holder.p = holder.getAdapterPosition();

            if (mSelectedValues.contains(eValue)) {
                if (holder.p == activeItemPos) {
                    holder.mView.setBackground(ContextCompat.getDrawable(this.ctx, R.drawable.list_item_active_selected));
                } else {
                    holder.mView.setBackground(ContextCompat.getDrawable(this.ctx, R.drawable.list_item_selected));
                }
                holder.mCbx.setChecked(true);
                holder.mCbx.setVisibility(View.VISIBLE);
            } else {
                if (holder.p == activeItemPos) {
                    holder.mView.setBackground(ContextCompat.getDrawable(this.ctx, R.drawable.list_item_active));
                } else {
                    holder.mView.setBackground(ContextCompat.getDrawable(this.ctx, R.drawable.list_item_default));
                }
                holder.mCbx.setChecked(false);
                holder.mCbx.setVisibility(View.INVISIBLE);
            }

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mTwoPane) {
                        Bundle arguments = new Bundle();
                        arguments.putInt(ARG_ITEM_ID, holder.getAdapterPosition());
                        arguments.putSerializable(ItemDetailActivity.ARG_ITEM_TYPE, eSetName);
                        ItemDetailFragment fragment = new ItemDetailFragment();
                        fragment.setArguments(arguments);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_item_detail, fragment)
                                .commit();
                        if (activeItemView != null) {
                            setItemBackground(activeItemView, activeEntity, false);
                        }
                        setItemBackground(holder.mView, eValue, true);
                        activeItemView = holder.mView;
                        activeEntity = eValue;
                        activeItemPos = holder.p;
                    } else {
                        Context context = v.getContext();
                        Intent intent = new Intent(context, ItemDetailActivity.class);
                        intent.putExtra(ARG_ITEM_ID, holder.getAdapterPosition());
                        intent.putExtra(ItemDetailActivity.ARG_ITEM_TYPE, eSetName);
                        context.startActivity(intent);
                    }
                }
            });

            holder.mView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(final View view) {

                    if (aMode == null) {
                        aMode = ItemListActivity.this.startActionMode(mActionModeCallback);
                    }

                    CheckBox box = holder.mCbx;
                    if (box.isChecked()) {
                        box.setChecked(false);
                        box.setVisibility(View.INVISIBLE);
                    } else {
                        box.setChecked(true);
                        box.setVisibility(View.VISIBLE);
                    }
                    return true;
                }
            });

            holder.mCbx.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (b) {
                        holder.mCbx.setVisibility(View.VISIBLE);
                        mSelectedValues.add(eValue);
                        if (mSelectedValues.size() > 1) {
                            if (aMode != null) {
                                aMode.getMenu().findItem(R.id.update_item).setVisible(false);
                            }
                        }
                    } else {
                        holder.mCbx.setVisibility(View.INVISIBLE);
                        mSelectedValues.remove(eValue);
                        if (aMode != null) {
                            switch (mSelectedValues.size()) {
                                case 1:
                                    aMode.getMenu().findItem(R.id.update_item).setVisible(true);
                                    break;
                                case 0:
                                    aMode.finish();
                                    break;
                                default:
                            }
                        }
                    }
                    if (aMode != null) {
                        aMode.setTitle(String.valueOf(mSelectedValues.size()));
                    }
                    boolean isActive = holder.p == activeItemPos;
                    setItemBackground(holder.mView, eValue, isActive);
                }
            });
        }

        private void setItemBackground(View v, EntityValueUIConnector eValue, boolean isActive) {
            if (mSelectedValues.contains(eValue)) {
                if (isActive) {
                    v.setBackground(ContextCompat.getDrawable(this.ctx, R.drawable.list_item_active_selected));
                } else {
                    v.setBackground(ContextCompat.getDrawable(this.ctx, R.drawable.list_item_selected));
                }
            } else {
                if (isActive) {
                    v.setBackground(ContextCompat.getDrawable(this.ctx, R.drawable.list_item_active));
                } else {
                    v.setBackground(ContextCompat.getDrawable(this.ctx, R.drawable.list_item_default));
                }
            }
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            public final View mView;
            public final ObjectCell mContentView;
            public final CheckBox mCbx;
            public int p;



            public ViewHolder(View view) {
                super(view);

                mView = view;
                mContentView = view.findViewById(R.id.content);
                mCbx = view.findViewById(R.id.cbx);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mContentView.getDescription() + "'";
            }
        }
    }

    public void refreshItemList() {
        adapter.notifyDataSetChanged();
    }
}

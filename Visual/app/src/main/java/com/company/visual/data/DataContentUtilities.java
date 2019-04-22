package com.company.visual.data;

import com.company.visual.mdui.EntitySetListActivity;
import com.company.visual.data.connector.EntityValueUIConnector;
import com.company.visual.data.operation.CreateOperation;
import com.company.visual.data.operation.DeleteOperation;
import com.company.visual.data.operation.DownloadOperation;
import com.company.visual.data.operation.OnODataOperation;
import com.company.visual.data.operation.OperationResult;
import com.company.visual.data.operation.UpdateOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * Singleton class to handle ODATA operations on the entity sets. If the entity set were
 * changed the instance should be re-initiated.
 */
public class DataContentUtilities implements OnODataOperation {

    private EntitySetListActivity.EntitySetName eSetName;
    private OnODataOperation callback;
    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(DataContentUtilities.class);

      /**
      * Storage for all the entity set data, where the key is the name of the entity
      * set.
      */
     private Map<EntitySetListActivity.EntitySetName, List<EntityValueUIConnector>> iListMap = new HashMap<>();

     /**
      * An array for storing the data items of the actual entity set.
      */
     private List<EntityValueUIConnector> iList = null;


    private EntityValueUIConnector iForUpdate;
    private List<EntityValueUIConnector> iListForDelete = new ArrayList<>();

    @Override
    public void onOperation(OperationResult result) {
        if (result.getError() != null) {
            // real error handling is done in the UI layer
            LOGGER.error(result.getError().getMessage());
        } else {
            // success
            OperationResult.Operation op = result.getOperation();
            List<EntityValueUIConnector> results = result.getResult();
            switch (op) {
                case READ:
                    if (results != null && results.size() > 0) {
                        this.iList.clear();
                        iList.addAll(results);
                        break;
                    }
                    break;
                case UPDATE:
                    if (results != null && results.size() == 1) {
                        EntityValueUIConnector uValue = results.get(0);
                        iList.set(iList.indexOf(iForUpdate), uValue);
                    } else {
                        // this couldn't happen if the operation was successful
                        LOGGER.error("Inconsistency in the results.");
                    }
                    break;
                case DELETE:
                    if (results != null && results.size() > 0) {
                        iList.removeAll(results);
                    }
                    break;
                case CREATE:
                    if (results != null && results.size() == 1) {
                        iList.add(results.get(0));
                    } else {
                        // this couldn't happen if the operation was successful
                        LOGGER.error("Inconsistency in the results.");
                    }
            }
        }
        callback.onOperation(result);
    }

    private static final class SingletonHolder {

        private static final DataContentUtilities INSTANCE = new DataContentUtilities();
    }

    public static DataContentUtilities getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private DataContentUtilities() {
    }

    public void init(EntitySetListActivity.EntitySetName eSetName) {

        this.eSetName = eSetName;
        this.iList = iListMap.get(eSetName);
        if (iList == null) {
            iList = new ArrayList<EntityValueUIConnector>();
            iListMap.put(eSetName, iList);
        }
    }

    public List<EntityValueUIConnector> getItems() {
        return iList;
    }

    /**
     * Triggers a download operation. Notification about the outcome will be
     * sent on the {@link OnODataOperation} callback.
     */
    public void download(OnODataOperation callback) {
        this.callback = callback;

        DownloadOperation download = new DownloadOperation(this, eSetName);
        download.execute();
    }

    public void create(OnODataOperation callback, EntityValueUIConnector eValue) {
        if (callback != null && eValue != null) {
            this.callback = callback;

            CreateOperation create = new CreateOperation(this, eValue);
            create.execute();
        } else {
            LOGGER.error("One of the parameters is null."); 
        }
    }

    public void update(OnODataOperation callback, EntityValueUIConnector eValue) {
        if (callback != null && eValue != null) {
            this.callback = callback;
            this.iForUpdate = eValue;

            UpdateOperation update = new UpdateOperation(this, eValue);
            update.execute();
        } else {
            LOGGER.error("One of the parameters is null.");
        }
    }

    public void delete(OnODataOperation callback, List<EntityValueUIConnector> eValues) {
        if (callback != null && eValues != null && eValues.size() > 0) {
            this.callback = callback;
            this.iListForDelete.addAll(eValues);

            DeleteOperation delete = new DeleteOperation(this, eValues);
            delete.execute();
        } else {
            LOGGER.error("One of the parameters is null.");
        }
    }
}

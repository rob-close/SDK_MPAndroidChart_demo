package com.company.visual.data.operation;

import com.company.visual.data.connector.EntityValueUIConnector;
import com.company.visual.service.SAPServiceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;

public class DeleteOperation extends CRUDOperation {

    private List<EntityValueUIConnector> selValues;
    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(DeleteOperation.class);

    public DeleteOperation(OnODataOperation callback, List<EntityValueUIConnector> selValues) {
       super(callback, selValues);
       this.selValues = selValues;
    }

    @Override
    protected OperationResult doInBackground(Object... params) {
        OperationResult deleteOperationResult = null;
        try {
            //TODO transaction?
            for (EntityValueUIConnector eValue: selValues) {
                SAPServiceManager.getInstance().getSAPService().deleteEntity(eValue.getConnectedObject());
            }
            List<EntityValueUIConnector> iList = new ArrayList<>();
            iList.addAll(selValues);
            deleteOperationResult = new OperationResult(iList, OperationResult.Operation.DELETE);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            deleteOperationResult = new OperationResult(e, OperationResult.Operation.DELETE);
        }
        return deleteOperationResult;
    }
}








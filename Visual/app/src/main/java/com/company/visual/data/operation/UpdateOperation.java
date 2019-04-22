package com.company.visual.data.operation;

import com.company.visual.data.connector.EntityValueUIConnector;
import com.company.visual.service.SAPServiceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;

public class UpdateOperation extends CRUDOperation {

    private OnODataOperation oCallback;
    private EntityValueUIConnector eValue;
    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(UpdateOperation.class);

    public UpdateOperation(OnODataOperation callback, EntityValueUIConnector eValue) {
        super(callback, eValue);
        this.eValue = eValue;
    }

    @Override
    protected OperationResult doInBackground(Object... params) {
        OperationResult updateOperationResult = null;
        try {
            SAPServiceManager.getInstance().getSAPService().updateEntity(eValue.getConnectedObject());

            List<EntityValueUIConnector> iList = new ArrayList<>();
            iList.add(eValue);
            updateOperationResult = new OperationResult(iList, OperationResult.Operation.UPDATE);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            updateOperationResult = new OperationResult(e, OperationResult.Operation.UPDATE);
        }
        return updateOperationResult;
    }
}

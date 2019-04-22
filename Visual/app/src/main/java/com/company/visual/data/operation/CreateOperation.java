package com.company.visual.data.operation;

import com.company.visual.data.connector.EntityValueUIConnector;
import com.company.visual.service.SAPServiceManager;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateOperation extends CRUDOperation {

    private EntityValueUIConnector eValue;
    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(CreateOperation.class);
    public CreateOperation(OnODataOperation callback, EntityValueUIConnector eValue) {
        super(callback, eValue);
        this.eValue = eValue;
    }

    @Override
    protected OperationResult doInBackground(Object... params) {
        OperationResult createOperationResult = null;
        try {
            SAPServiceManager.getInstance().getSAPService().createEntity(eValue.getConnectedObject());
            List<EntityValueUIConnector> iList = new ArrayList<>();
            iList.add(eValue);
            createOperationResult = new OperationResult(iList, OperationResult.Operation.CREATE);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            createOperationResult = new OperationResult(e, OperationResult.Operation.CREATE);
        }
        return createOperationResult;
    }
}
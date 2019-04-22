package com.company.visual.data.operation;

import com.company.visual.data.connector.EntityValueUIConnector;

import java.util.List;

/**
 * We can wrap the AsyncTask result in this class
 * As results, we got the list of the Entities or if something error occurs
 * We can store the exception
 */
public class OperationResult {

    public static enum Operation {
        CREATE,
        READ,
        UPDATE,
        DELETE
    }

    private List<EntityValueUIConnector> result;
    private Exception error;
    private Operation operation;

    public OperationResult(List<EntityValueUIConnector> result, Operation op) {
        super();
        this.result = result;
        this.operation = op;
    }

    public OperationResult(Exception error, Operation op) {
        super();
        this.error = error;
        this.operation = op;
    }

    public Operation getOperation() {
        return this.operation;
    }

    public List<EntityValueUIConnector> getResult() {
        return result;
    }

    public Exception getError() {
        return error;
    }
}
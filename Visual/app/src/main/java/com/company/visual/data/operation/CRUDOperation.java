package com.company.visual.data.operation;

import android.os.AsyncTask;

public abstract class CRUDOperation extends AsyncTask<Object, Void, OperationResult> {

    private OnODataOperation oCallback;

    public CRUDOperation(OnODataOperation callback, Object data) {
        this.oCallback = callback;
    }

    @Override
    abstract protected OperationResult doInBackground(Object... params);

    @Override
    protected void onPostExecute(OperationResult result) {
            this.oCallback.onOperation(result);
    }
}
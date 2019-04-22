package com.company.visual.mdui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.company.visual.R;
import com.company.visual.data.DataContentUtilities;
import com.company.visual.data.connector.EntityValueUIConnector;
import com.company.visual.data.operation.OnODataOperation;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by i043971 on 2017. 12. 05..
 */

public class ItemDeleteDialog {

    /**
     * Entities which should be deleted.
     */
    private List<EntityValueUIConnector> selectedValues = new ArrayList<>();
    private Context ctx;

    /**
     * Callback which should handle the result of the ODATA operations.
     */
    private OnODataOperation oCallback;

    public ItemDeleteDialog(List<EntityValueUIConnector> eValue, Context ctx, OnODataOperation oCallback) {
        if (eValue != null) {
            selectedValues.addAll(eValue);
        }
        this.ctx = ctx;
        this.oCallback = oCallback;
    }

    public void confirmDelete() {

        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setTitle(R.string.delete_dialog_title).setMessage(R.string.delete_dialog_message);

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                DataContentUtilities.getInstance().delete(oCallback, selectedValues);
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}

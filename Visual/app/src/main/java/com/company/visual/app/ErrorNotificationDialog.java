package com.company.visual.app;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.company.visual.R;

import java.util.List;

/** This is an activity which is presented as a dialog for presenting
 * error notifications to the user. The notifications can have a short title, a detailed
 * message describing the error and its consequences and if it had an attached
 * stack trace, it could also be shown. Finally, notifications have a so-called fatal
 * flag. It it were true, then the application is killed, after the user pressed the
 * OK button.
 */
public class ErrorNotificationDialog extends Activity {

    public static final String TITLE = "error_title";
    public static final String MSG = "error_msg";
    public static final String EXP = "error_exception";
    public static final String FATAL = "isFatal";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error_notification);

        Intent startIntent = getIntent();
        String title = startIntent.getStringExtra(TITLE);
        String msg = startIntent.getStringExtra(MSG);
        Exception exp = (Exception) startIntent.getSerializableExtra(EXP);
        String expDetail = null;
        if (exp != null) {
            expDetail = getStackTrace(exp);
        }
        boolean isFatal = startIntent.getBooleanExtra(FATAL, false);

        TextView msgView = findViewById(R.id.error_notification_msg);
        TextView expView = findViewById(R.id.error_notification_exp);
        Button okButton = findViewById(R.id.error_notification_button);

        if (title !=null && !title.isEmpty()) {
            setTitle(title);
        } else {
            setTitle(SAPWizardApplication.getApplicatiton().getResources().getString(R.string.error));
        }
        msgView.setText(expDetail);

        if (expDetail != null && !expDetail.isEmpty()) {
            expView.setText(expDetail);
        }

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFatal) {
                    ActivityManager activityManager = (ActivityManager) ErrorNotificationDialog.this.getSystemService(Context.ACTIVITY_SERVICE);
                    List<ActivityManager.AppTask> tasks = activityManager.getAppTasks();
                    for (ActivityManager.AppTask task : tasks) {
                        task.finishAndRemoveTask();
                    }
                } else {
                    ErrorNotificationDialog.this.finish();
                }
            }
        });
        msgView.setText(msg);
    }

    private String getStackTrace(Exception ex) {
        String exMsg = null;
        if (ex != null) {
            StackTraceElement[] stackTraceElements = ex.getStackTrace();
            StringBuilder sBuilder = new StringBuilder();
            for (StackTraceElement sTraceElement : stackTraceElements) {
                sBuilder.append(sTraceElement.toString()).append("<br/>");
            }
            exMsg = sBuilder.toString();
        }
        return exMsg;
    }

    @Override
    public void onBackPressed() {
        // hardware back button is disabled here
    }
}

package com.company.visual.app;

import android.content.Context;
import android.content.Intent;

import com.sap.cloud.mobile.odata.DataServiceException;

/**
 * {@link ErrorPresenter} implementation which is based on notification dialogs. The dialogs are
 * able to show the stack trace of the catched exceptions, as well. If the error were marked as
 * fatal, then the application is shut down after pressing the 'OK' button.
 */
public class ErrorPresenterByNotification implements ErrorPresenter {

      private Context ctx;

      public ErrorPresenterByNotification(Context ctx) {
          this.ctx = ctx;
      }

      @Override
      public void presentError(String eTitle, String eDetail, Exception exp, boolean isFatal) {

          Intent startNotification = new Intent(ctx, ErrorNotificationDialog.class);
          startNotification.putExtra(ErrorNotificationDialog.TITLE, eTitle);
          startNotification.putExtra(ErrorNotificationDialog.MSG, eDetail);
          if (!DataServiceException.class.isInstance(exp)) {
              //DataServiceException is not serializable and leads to runtime exception
              startNotification.putExtra(ErrorNotificationDialog.EXP, exp);
          }
          startNotification.putExtra(ErrorNotificationDialog.FATAL, isFatal);
          ctx.startActivity(startNotification);
      }
}

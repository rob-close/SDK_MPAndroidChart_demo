package com.company.visual.app;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Central handler class, which processes {@link ErrorMessage} notifications received
 * from the application. The messages land in an error queue and processed one-by-one.
 * When the application is in foreground then each messages will be presented on
 * a dialog with a single 'OK' button. If the message contained an exception object,
 * then its stack trace will also be shown. If the message's fatal flag were 'true'
 * then after pressing the OK button, the application is shut down. If the application were
 * in background then the dialogs appear after it returns to foreground.
 */
public class ErrorHandler extends HandlerThread {

    private Handler handler;
    private Context ctx;
    private ErrorPresenter presenter;
    private Logger logger = LoggerFactory.getLogger(ErrorHandler.class);

    /**
     * Creates an error handler.
     *
     * @param name name for the handler
     * @param ctx reference to the application context where it was started from
     */
    public ErrorHandler(String name, Context ctx) {
        super(name);

        this.ctx = ctx;
    }

    /**
     * Returns the {@link ErrorPresenter} which was set for this handler.
     * @return {@link ErrorPresenter}
     */
    public ErrorPresenter getPresenter() {
        return presenter;
    }

    /**
     * This method is used to set the {@link ErrorPresenter}.
     * @param presenter {@link ErrorPresenter}
     */
    public void setPresenter(ErrorPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    protected void onLooperPrepared() {

        handler = new Handler(getLooper()) {

            @Override
            public void handleMessage(Message msg) {

                String eTitle = msg.getData().getString(ErrorMessage.EKEY_TITLE);
                String eDesc = msg.getData().getString(ErrorMessage.EKEY_DESC);
                Exception ex = (Exception) msg.getData().get(ErrorMessage.EKEY_EX);
                boolean isFatal = msg.getData().getBoolean(ErrorMessage.EKEY_ISFATAL);

                presenter.presentError(eTitle, eDesc, ex, isFatal);
            }
        };
    }

    /**
     * This method is used to send {@link ErrorMessage} objects to the handler.
     *
     * @param error {@link ErrorMessage} object containing the error information
     */
    public synchronized void sendErrorMessage(ErrorMessage error) {
        Bundle eBundle = error.getErrorBundle();
        Message eMsg = Message.obtain(handler);
        eMsg.setData(eBundle);
        handler.sendMessage(eMsg);
    }
}

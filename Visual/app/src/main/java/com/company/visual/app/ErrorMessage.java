package com.company.visual.app;

import android.os.Bundle;

/**
 * Simple wrapper class for error messages, which are used to send notifications
 * to the {@link ErrorHandler}.
 *
 * Error messages have a (short) title and a longer description. If an exception is also
 * attached, then its stack trace will be processed and presented to the user. The final argument
 * is can indicate whether the application can still work (isFatal = false) with somewhat
 * limited functionality or it should be shut down be the error handler (isFatal = true).
 */
public class ErrorMessage {

    public static final String EKEY_TITLE = "TITLE";
    public static final String EKEY_DESC = "DESC";
    public static final String EKEY_EX = "EX";
    public static final String EKEY_ISFATAL = "ISFATAL";

    // default title and description
    private String title = "Error!";
    private String description = "";
    private Exception ex = null;
    private boolean isFatal = false;
    private Bundle eBundle;

    /**
     * ErrorMessage constructor for non-fatal events with no exception.
     *
     * @param title short description
     * @param description longer description, explaining also the consequences of the
     *                    error
     */
    public ErrorMessage(String title, String description) {
        this(title, description, null, false);
    }

    /**
     * Error message constructor with complete customization possibilities.
     *
     * @param title short description
     * @param description longer description, explaining also the consequences of the
     *                    error
     * @param ex exception object, its stack trace will also be presented to the user
     * @param isFatal true indicates that the error is fatal,
     *                the application couldn't be continued
     */
    public ErrorMessage(String title, String description, Exception ex, boolean isFatal) {
        if (title != null && title.isEmpty()) {
            this.title = title;
        }

        if (description != null && description.isEmpty()) {
            this.description = description;
        }

        this.ex = ex;
        this.isFatal = isFatal;

        eBundle = new Bundle();
        eBundle.putString(EKEY_TITLE, title);
        eBundle.putString(EKEY_DESC, description);
        if (ex != null) {
            eBundle.putSerializable(EKEY_EX, ex);
        }
        eBundle.putBoolean(EKEY_ISFATAL, isFatal);
    }

    /**
     * Returns a {@link Bundle} containing the error parameters with the self-explaining
     * keys EKEY_TITLE, EKEY_DESC, EKEY_EX, EKEY_ISFATAL, for the title, description,
     * exception object and fatality.
     *
     * @return
     */
    public Bundle getErrorBundle() {
        return  eBundle;
    }
}


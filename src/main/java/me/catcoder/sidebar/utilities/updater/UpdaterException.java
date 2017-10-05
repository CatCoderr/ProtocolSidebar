package me.catcoder.sidebar.utilities.updater;

public class UpdaterException extends RuntimeException {
    public UpdaterException() {
        super();
    }

    public UpdaterException(String message) {
        super(message);
    }

    public UpdaterException(String message, Throwable cause) {
        super(message, cause);
    }

    public UpdaterException(Throwable cause) {
        super(cause);
    }

    protected UpdaterException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

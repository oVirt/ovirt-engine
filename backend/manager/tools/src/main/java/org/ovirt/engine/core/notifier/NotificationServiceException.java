package org.ovirt.engine.core.notifier;

/**
 * An exception of the notification service
 */
public class NotificationServiceException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public NotificationServiceException(String message) {
        super(message);
    }

    public NotificationServiceException(Throwable cause) {
        super(cause);
    }

    public NotificationServiceException(String message, Throwable cause) {
        super(message, cause);
    }

}

package org.ovirt.engine.ui.frontend;

/**
 * Generic notification handler that can be used across all modules.
 */
public interface NotificationHandler {
    void showToast(String text, NotificationStatus status);
}

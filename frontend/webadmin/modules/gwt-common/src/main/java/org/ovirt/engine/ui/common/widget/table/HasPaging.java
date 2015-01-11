package org.ovirt.engine.ui.common.widget.table;

/**
 * Classes that implement this interface support forward/back paging functionality.
 *
 */
public interface HasPaging {

    /**
     * Returns {@code true} when there is a "next" page available.
     */
    boolean canGoForward();

    /**
     * Returns {@code true} when there is a "previous" page available.
     */
    boolean canGoBack();

    /**
     * Goes forward to the next page (if available).
     */
    void goForward();

    /**
     * Goes back to the previous page (if available).
     */
    void goBack();

    /**
     * Refresh the current page.
     */
    void refresh();
}

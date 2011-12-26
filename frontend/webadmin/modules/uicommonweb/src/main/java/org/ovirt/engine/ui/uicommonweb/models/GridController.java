package org.ovirt.engine.ui.uicommonweb.models;

/**
 * Functionality required from a Grid Model that has dynamic refresh rates
 */
public interface GridController {

    /**
     * Get the Grid refresh rate
     */
    int getRefreshRate();

    /**
     * Set the Grid refresh rate
     */
    void setRefreshRate(int currentRefreshRate);

    /**
     * Notified the Grid that it is out of window focus
     */
    void toBackground();

    /**
     * Notified the Grid that it has received window focus
     */
    void toForground();

    /**
     * Get the timer used by the Grid
     */
    GridTimer getTimer();

    /**
     * Get the controller ID
     */
    String getId();

}

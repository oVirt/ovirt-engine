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
     * Get the timer used by the Grid
     */
    GridTimer getTimer();

    /**
     * Get the controller ID
     */
    String getId();

    /**
     * Refreshes the model immediately without waiting for the timer.
     */
    void refresh();

}

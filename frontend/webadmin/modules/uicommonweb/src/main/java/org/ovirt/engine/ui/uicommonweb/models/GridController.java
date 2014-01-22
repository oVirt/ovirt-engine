package org.ovirt.engine.ui.uicommonweb.models;

/**
 * Functionality required from a Grid Model that has dynamic refresh rates.
 */
public interface GridController {

    /**
     * Returns the refresh timer used by the Grid.
     * @return The Grid timer.
     */
    GridTimer getTimer();

    /**
     * Returns the controller ID.
     * @return The controller ID.
     */
    String getId();

    /**
     * Refreshes the model immediately without waiting for the timer.
     */
    void refresh();

}

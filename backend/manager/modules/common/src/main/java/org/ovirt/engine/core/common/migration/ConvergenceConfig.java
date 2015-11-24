package org.ovirt.engine.core.common.migration;

import java.io.Serializable;
import java.util.List;

public class ConvergenceConfig implements Serializable {

    /**
     * This steps will be executed as a reaction to stalling.
     */
    private List<ConvergenceItemWithStallingLimit> convergenceItems;

    /**
     * This will be executed at the beginning of the migration (but after the migration already started)
     */
    private List<ConvergenceItem> initialItems;

    /**
     * This is the step which will be set after all the steps from the convergence have been used and the app
     * migration is still not converging
     */
    private List<ConvergenceItem> lastItems;

    public ConvergenceConfig() {}

    public List<ConvergenceItemWithStallingLimit> getConvergenceItems() {
        return convergenceItems;
    }

    public void setConvergenceItems(List<ConvergenceItemWithStallingLimit> convergenceItems) {
        this.convergenceItems = convergenceItems;
    }

    public List<ConvergenceItem> getInitialItems() {
        return initialItems;
    }

    public void setInitialItems(List<ConvergenceItem> initialItems) {
        this.initialItems = initialItems;
    }

    public List<ConvergenceItem> getLastItems() {
        return lastItems;
    }

    public void setLastItems(List<ConvergenceItem> lastItems) {
        this.lastItems = lastItems;
    }
}

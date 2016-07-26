package org.ovirt.engine.ui.frontend.server.dashboard;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents Resource Utilization on a Global level for a particular resource.
 * It will contain aggregated data over this resource in the system.
 */
public class GlobalUtilizationResourceSummary {

    protected double used;
    protected double physicalTotal = 0;
    protected double virtualTotal;
    protected double virtualUsed;
    protected List<HistoryNode> history;
    protected Utilization utilization;

    /**
     * Constructor.
     */
    public GlobalUtilizationResourceSummary() {
        this(new ResourceUtilization());
    }

    /**
     * Constructor that takes a {@code Utilization} object.
     * @param utilization The utilization object.
     */
    public GlobalUtilizationResourceSummary(Utilization utilization) {
        history = new ArrayList<>();
        this.utilization = utilization;
        used = 0;
    }

    public List<HistoryNode> getHistory() {
        return history;
    }

    public void setHistory(List<HistoryNode> history) {
        this.history = history;
    }

    /**
     * Set the physical total resources. If the number is <= 0, it is set to 1.
     * @param physicalTotal The total number of physical resources.
     */
    public void setPhysicalTotal(double physicalTotal) {
        this.physicalTotal = physicalTotal;
    }

    /**
     * Set the virtual allocated resources
     * @param virtualTotal Total number of resources
     */
    public void setVirtualTotal(double virtualTotal) {
        this.virtualTotal = virtualTotal;
    }

    /**
     * Set the virtual used resources
     * @param virtualUsed Virtual used resources.
     */
    public void setVirtualUsed(double virtualUsed) {
        this.virtualUsed = virtualUsed;
    }

    /**
     * Return the current used virtual allocated resources in relation to the actual resources. The calculation is
     * Running virtual resources / Actual resources * 100. This returns the ratio of running virtual resources
     * in relation to the actual resources.
     * @return A percentage indicating the running virtual resource compared to actual resources.
     */
    public double getOvercommit() {
        return virtualUsed / (physicalTotal == 0 ? 1 : physicalTotal) * 100;
    }

    /**
     * Return the virtual allocated resources in relation to the actual resources. The calculation is
     * Allocated virtual resources / Actual resources * 100. This returns the ratio of allocated virtual resources
     * in relation to the actual resources.
     * @return A percentage indicating the allocated virtual resource compared to actual resources.
     */
    public double getAllocated() {
        return virtualTotal / (physicalTotal == 0 ? 1 : physicalTotal) * 100;
    }

    /**
     * Set used resources.
     * @param used Total used resources.
     */
    public void setUsed(double used) {
        this.used = used;
    }

    /**
     * Get actual used resource value. For CPU this will be a percentage.
     * @return used as a {@code double}
     */
    public double getUsed() {
        double result = used;
        if (used > getTotal()) {
            result = getTotal();
        }
        return result;
    }

    /**
     * Get total number of resources.
     * @return The total as a {@code double}
     */
    public double getTotal() {
        return physicalTotal;
    }

    /**
     * Get the current top 10 utilization of hosts/vms over an average of the last 5 minutes.
     * @return {@code Utilization} object.
     */
    public Utilization getUtilization() {
        return this.utilization;
    }
}

package org.ovirt.engine.ui.frontend.server.dashboard;

/**
 * Dashboard object, contains all the needed data to display the dashboard.
 */
public class Dashboard {
    /**
     * The global utilization object. Contains utilization of CPU/Memory/Storage
     */
    private GlobalUtilization globalUtilization;

    /**
     * Heat Map data. Contains cluster and storage domain heat map data.
     */
    private HeatMapData heatMapData;

    /**
     * Inventory card data. Data Centers/Clusters/Hosts/Storage Domains/VM/Events.
     */
    private Inventory inventory;

    /**
     * @return {@code HeatMapData} object containing heatmap data for clusters and storage domains.
     */
    public HeatMapData getHeatMapData() {
        return heatMapData;
    }

    /**
     * Set the heatMapData.
     * @param heatMapData The new heatmap data.
     */
    public void setHeatMapData(HeatMapData heatMapData) {
        this.heatMapData = heatMapData;
    }

    /**
     * @return {@code GlobalUtilization} object containing the global utilization data.
     */
    public GlobalUtilization getGlobalUtilization() {
        return globalUtilization;
    }

    /**
     * Set the global utilization data.
     * @param utilization GlobalUtilization data.
     */
    public void setGlobalUtilization(GlobalUtilization utilization) {
        this.globalUtilization = utilization;
    }

    /**
     * Get the inventory object containing inventory data for Data Centers/Clusters/Hosts/Storage Domains/VM/Events
     * @return {@code Inventory} object.
     */
    public Inventory getInventory() {
        return inventory;
    }

    /**
     * Set the inventory data.
     * @param inventory inventory data.
     */
    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }
}

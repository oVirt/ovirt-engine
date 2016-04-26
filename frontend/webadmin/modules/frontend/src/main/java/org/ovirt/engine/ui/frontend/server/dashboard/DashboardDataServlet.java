package org.ovirt.engine.ui.frontend.server.dashboard;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.codehaus.jackson.map.ObjectMapper;
import org.infinispan.Cache;
import org.infinispan.manager.CacheContainer;
import org.ovirt.engine.ui.frontend.server.dashboard.fake.FakeDataGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DashboardDataServlet extends HttpServlet {
    /**
     * SerialVersionUID
     */
    private static final long serialVersionUID = 6678850917843141114L;

    private static final Logger log = LoggerFactory.getLogger(DashboardDataServlet.class);

    private static final String CONTENT_TYPE = "application/json"; //$NON-NLS-1$
    private static final String ENCODING = "UTF-8"; //$NON-NLS-1$
    private static final String DASHBOARD = "dashboard"; //$NON-NLS-1$
    private static final String INVENTORY = "inventory"; //$NON-NLS-1$
    private static final String UTILIZATION_KEY = "utilization_key"; //$NON-NLS-1$
    private static final String INVENTORY_KEY = "inventory_key"; //$NON-NLS-1$
    private static final Object UTILIZATION_LOCK = new Object();

    @Resource(mappedName = "java:/DWHDataSource")
    private DataSource dwhDataSource;

    @Resource(mappedName = "java:/ENGINEDataSource")
    private DataSource engineDataSource;

    @Resource(lookup = "java:jboss/infinispan/ovirt-engine")
    private CacheContainer cacheContainer;

    private static Cache<String, Dashboard> dashboardCache;
    private static Cache<String, Inventory> inventoryCache;

    @PostConstruct
    private void initCache() {
        dashboardCache = cacheContainer.getCache(DASHBOARD);
        inventoryCache = cacheContainer.getCache(INVENTORY);
    }

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws IOException,
        ServletException {

        Dashboard dashboard;
        Inventory inventory;
        response.setContentType(CONTENT_TYPE);
        response.setCharacterEncoding(ENCODING);
        try {
            // Check if the browser wants fake data, if so generate fake data instead of querying the database.
            if (FakeDataGenerator.headerWantsFakeData(request)) {
                dashboard = getFakeDashboard(request);
            } else {
                synchronized (UTILIZATION_LOCK) {
                    // Get the dashboard from the cache if we can, if not query the database.
                    dashboard = dashboardCache.get(UTILIZATION_KEY);
                    if (dashboard == null) {
                        dashboard = getDashboard(request);
                        // Put the data in the cache for 5 minutes, after 5 minutes it is evicted and the next
                        // request will populate it again.
                        dashboardCache.put(UTILIZATION_KEY, dashboard, 5, TimeUnit.MINUTES);
                    }
                    // Inventory is in a different cache, get the data from the cache if possible otherwise get it
                    // from the database. Since this is potentially modifying the dashboard object, we need to have
                    // this inside the synchronized block of the dashboard.
                    inventory = inventoryCache.get(INVENTORY_KEY);
                    if (inventory == null) {
                        inventory = lookupInventory();
                        // Put the inventory in the cache for 15 seconds, after 15 seconds it is evicted and the
                        // next request will populate it again.
                        inventoryCache.put(INVENTORY_KEY, inventory, 15, TimeUnit.SECONDS);
                    }
                    dashboard.setInventory(inventory);
                }
            }
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(response.getOutputStream(), dashboard);
        } catch (final DashboardDataException se) {
            log.error("Unable to retrieve dashboard data", se); //$NON-NLS-1$
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(response.getOutputStream(),
                    new DashboardError("Unable to retrieve dashboard data")); //$NON-NLS-1$
        }
    }

    private Dashboard getFakeDashboard(final HttpServletRequest request) {
        Random random = new Random();
        Dashboard dashboard = new Dashboard();
        dashboard.setGlobalUtilization(FakeDataGenerator.fakeGlobalUtilization(random));
        dashboard.setHeatMapData(FakeDataGenerator.fakeHeatMapData(random));
        dashboard.setInventory(FakeDataGenerator.fakeInventory(random));
        return dashboard;
    }

    private Dashboard getDashboard(final HttpServletRequest request) throws DashboardDataException {
        Dashboard dashboard = new Dashboard();
        dashboard.setGlobalUtilization(lookupGlobalUtilization());
        dashboard.setHeatMapData(lookupClusterUtilization());
        return dashboard;
    }

    private Inventory lookupInventory() throws DashboardDataException {
        Inventory inventory = new Inventory();
        inventory.setDc(InventoryHelper.getDcInventoryStatus(engineDataSource));
        inventory.setCluster(InventoryHelper.getClusterInventoryStatus(engineDataSource));
        inventory.setHost(InventoryHelper.getHostInventoryStatus(engineDataSource));
        inventory.setStorage(InventoryHelper.getStorageInventoryStatus(engineDataSource));
        inventory.setVm(InventoryHelper.getVmInventorySummary(engineDataSource));
        inventory.setEvent(EventHelper.getEventStatus(engineDataSource));
        return inventory;
    }

    private HeatMapData lookupClusterUtilization() throws DashboardDataException {
        HeatMapData utilization = new HeatMapData();
        HeatMapHelper.getCpuAndMemory(utilization, dwhDataSource);
        utilization.setStorage(HeatMapHelper.getStorage(dwhDataSource));
        return utilization;
    }

    private GlobalUtilization lookupGlobalUtilization() throws DashboardDataException {
        GlobalUtilization utilization = new GlobalUtilization();
        HourlySummaryHelper.getCpuMemSummary(utilization, dwhDataSource);
        utilization.setStorage(HourlySummaryHelper.getStorageSummary(dwhDataSource));
        return utilization;
    }

    private static class DashboardError {
        private String message;

        DashboardError(String message) {
            setMessage(message);
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}

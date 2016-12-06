package org.ovirt.engine.ui.frontend.server.dashboard;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.codehaus.jackson.map.ObjectMapper;
import org.infinispan.Cache;
import org.infinispan.manager.CacheContainer;
import org.ovirt.engine.core.utils.EngineLocalConfig;
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

    private static final String ENABLE_CACHE_UPDATE_KEY = "DASHBOARD_CACHE_UPDATE"; //$NON-NLS-1$
    private static final String UTILIZATION_CACHE_UPDATE_INTERVAL_KEY = "DASHBOARD_UTILIZATION_CACHE_UPDATE_INTERVAL"; //$NON-NLS-1$
    private static final String INVENTORY_CACHE_UPDATE_INTERVAL_KEY = "DASHBOARD_INVENTORY_CACHE_UPDATE_INTERVAL"; //$NON-NLS-1$
    private static long UTILIZATION_CACHE_UPDATE_INTERVAL;
    private static long INVENTORY_CACHE_UPDATE_INTERVAL;

    private static final String PREFER_HEADER = "Prefer"; //$NON-NLS-1$
    private static final String PREFER_FAKE_DATA = "fake_data"; //$NON-NLS-1$
    private static final String PREFER_ERROR = "error"; //$NON-NLS-1$
    private static final String PREFER_NO_CACHE = "nocache"; //$NON-NLS-1$

    @Resource(mappedName = "java:/DWHDataSource")
    private DataSource dwhDataSource;

    @Resource(mappedName = "java:/ENGINEDataSource")
    private DataSource engineDataSource;

    @Resource(lookup = "java:jboss/infinispan/ovirt-engine")
    private CacheContainer cacheContainer;

    private static Cache<String, Dashboard> dashboardCache;
    private static Cache<String, Inventory> inventoryCache;

    @Resource
    private ManagedScheduledExecutorService scheduledExecutor;

    private ScheduledFuture<?> utilizationCacheUpdate = null;
    private ScheduledFuture<?> inventoryCacheUpdate = null;

    @PostConstruct
    private void initCache() {
        dashboardCache = cacheContainer.getCache(DASHBOARD);
        inventoryCache = cacheContainer.getCache(INVENTORY);

        EngineLocalConfig config = EngineLocalConfig.getInstance();
        boolean enableBackground;
        try {
            enableBackground = config.getBoolean(ENABLE_CACHE_UPDATE_KEY, Boolean.FALSE);
        } catch (IllegalArgumentException e) {
            log.error("Missing/Invalid key \"{}\", using default value of 'false'", ENABLE_CACHE_UPDATE_KEY, e); //$NON-NLS-1$
            enableBackground = false;
        }
        if (!enableBackground) {
            log.info("Dashboard DB query cache has been disabled."); //$NON-NLS-1$
            return;
        }

        /*
         * Update the utilization cache now and every 5 minutes (by default) thereafter, but never run 2 updates simultaneously.
         */
        try {
            UTILIZATION_CACHE_UPDATE_INTERVAL = config.getLong(UTILIZATION_CACHE_UPDATE_INTERVAL_KEY);
        } catch (IllegalArgumentException e) {
            log.error("Missing/Invalid key \"{}\", using default value of 300", UTILIZATION_CACHE_UPDATE_INTERVAL_KEY, e); //$NON-NLS-1$
            UTILIZATION_CACHE_UPDATE_INTERVAL = 300;
        }
        utilizationCacheUpdate = scheduledExecutor.scheduleAtFixedRate(new Runnable() {
            Logger log = LoggerFactory.getLogger(DashboardDataServlet.class.getName() + ".CacheUpdate.Utilization"); //$NON-NLS-1$

            @Override
            public void run() {
                log.trace("Attempting to update the Utilization cache"); //$NON-NLS-1$
                try {
                    populateUtilizationCache();
                } catch (DashboardDataException e) {
                    log.error("Could not update the Utilization Cache: {}", e.getMessage(), e); //$NON-NLS-1$
                }
            }
        }, 0, UTILIZATION_CACHE_UPDATE_INTERVAL, TimeUnit.SECONDS);

        /*
         * Update the inventory cache now and every 60 seconds (by default) thereafter, but never run 2 updates simultaneously.
         */
        try {
            INVENTORY_CACHE_UPDATE_INTERVAL = config.getLong(INVENTORY_CACHE_UPDATE_INTERVAL_KEY);
        } catch (IllegalArgumentException e) {
            log.error("Missing/Invalid key \"{}\", using default value of 60", INVENTORY_CACHE_UPDATE_INTERVAL_KEY, e); //$NON-NLS-1$
            INVENTORY_CACHE_UPDATE_INTERVAL = 60;
        }
        inventoryCacheUpdate = scheduledExecutor.scheduleAtFixedRate(new Runnable() {
            Logger log = LoggerFactory.getLogger(DashboardDataServlet.class.getName() + ".CacheUpdate.Inventory"); //$NON-NLS-1$

            @Override
            public void run() {
                log.trace("Attempting to update the Inventory cache"); //$NON-NLS-1$
                try {
                    populateInventoryCache();
                } catch (DashboardDataException e) {
                    log.error("Could not update the Inventory Cache: {}", e.getMessage(), e); //$NON-NLS-1$
                }

            }
        }, 0, INVENTORY_CACHE_UPDATE_INTERVAL, TimeUnit.SECONDS);
    }

    @PreDestroy
    private void stopScheduledTasks() {
        if (utilizationCacheUpdate != null) {
            utilizationCacheUpdate.cancel(true);
        }
        if (inventoryCacheUpdate != null) {
            inventoryCacheUpdate.cancel(true);
        }
    }

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws IOException,
        ServletException {

        Dashboard dashboard;
        response.setContentType(CONTENT_TYPE);
        response.setCharacterEncoding(ENCODING);
        try {
            // Check if the browser wants a forced error, fake data, non-cached data or standard cached data
            boolean preferFake = false;
            boolean preferError = false;
            boolean preferNoCache = false;

            String preferHeader = request.getHeader(PREFER_HEADER);
            String[] preferOptions = preferHeader == null ? new String[0] : preferHeader.trim().split("\\s*,\\s*"); //$NON-NLS-1$
            for (String option : preferOptions) {
                switch (option) {
                case PREFER_FAKE_DATA:
                    preferFake = true;
                    break;
                case PREFER_ERROR:
                    preferError = true;
                    break;
                case PREFER_NO_CACHE:
                    preferNoCache = true;
                    break;
                }
            }

            // Respond to the client based on the preferred method
            if (preferError) {
                log.debug("client requested an error condition"); //$NON-NLS-1$
                throw new ServletException("An error condition was requested."); //$NON-NLS-1$
            } else if (preferFake) {
                log.debug("client requested fake data"); //$NON-NLS-1$
                dashboard = getFakeDashboard();
            } else if (preferNoCache) {
                log.debug("client requested non-cache direct query data"); //$NON-NLS-1$
                dashboard = getDashboard();
                dashboard.setInventory(lookupInventory());
            } else {
                dashboard = getDashboardFromCache();
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

    private Dashboard getDashboardFromCache() throws DashboardDataException {
        Dashboard dashboard;
        Inventory inventory;

        synchronized (UTILIZATION_LOCK) {
            // Get the dashboard from the cache if we can. If not, query the database.
            dashboard = dashboardCache.get(UTILIZATION_KEY);
            if (dashboard == null) {
                dashboard = populateUtilizationCache();
            }

            // Inventory is in a different cache. Get the data from the cache if we can. If not, query
            // the database. Since this is potentially modifying the dashboard object, we need to have
            // this inside the synchronized block of the dashboard.
            inventory = inventoryCache.get(INVENTORY_KEY);
            if (inventory == null) {
                inventory = populateInventoryCache();
            }
        }

        dashboard.setInventory(inventory);
        return dashboard;
    }

    private Dashboard populateUtilizationCache() throws DashboardDataException {
        long startTime = System.currentTimeMillis();
        Dashboard dashboard = getDashboard();
        long endTime = System.currentTimeMillis();

        // Put the data in the cache for a default of 5 minutes, after 5 minutes it is evicted and the next
        // request will populate it again.
        dashboardCache.put(UTILIZATION_KEY, dashboard, UTILIZATION_CACHE_UPDATE_INTERVAL, TimeUnit.SECONDS);
        log.debug("Dashboard utilization cache updated in {}ms", endTime-startTime); //$NON-NLS-1$
        return dashboard;
    }

    private Inventory populateInventoryCache() throws DashboardDataException {
        long startTime = System.currentTimeMillis();
        Inventory inventory = lookupInventory();
        long endTime = System.currentTimeMillis();

        // Put the inventory in the cache for a default of 60 seconds, after 60 seconds it is evicted and the
        // next request will populate it again.
        inventoryCache.put(INVENTORY_KEY, inventory, INVENTORY_CACHE_UPDATE_INTERVAL, TimeUnit.SECONDS);
        log.debug("Dashboard inventoy cache updated in {}ms", endTime-startTime); //$NON-NLS-1$
        return inventory;
    }

    private Dashboard getFakeDashboard() {
        Random random = new Random();
        Dashboard dashboard = new Dashboard();
        dashboard.setGlobalUtilization(FakeDataGenerator.fakeGlobalUtilization(random));
        dashboard.setHeatMapData(FakeDataGenerator.fakeHeatMapData(random));
        dashboard.setInventory(FakeDataGenerator.fakeInventory(random));
        return dashboard;
    }

    private Dashboard getDashboard() throws DashboardDataException {
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
        inventory.setVolume(InventoryHelper.getGlusterVolumeInventorySummary(engineDataSource));
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

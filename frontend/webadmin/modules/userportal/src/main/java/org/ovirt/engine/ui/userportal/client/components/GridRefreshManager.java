package org.ovirt.engine.ui.userportal.client.components;

import java.util.Date;
import java.util.HashSet;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Window;

public class GridRefreshManager {

    // Refresh the grid every 60 seconds by default
    public static final int DEFAULT_REFRESH_RATE = 60;

    // Refresh rates (in seconds)
    public static final int[] REFRESH_RATES = new int[] { 10, 20, 30, 60 };

    // For saving refresh rate on client
    private static final String REFRESH_RATE_COOKIE_NAME = "RefreshRate";

    private static GridRefreshManager instance = null;

    private final HashSet<GridController<?>> gridControllerList = new HashSet<GridController<?>>();

    private int globalRefreshRate = DEFAULT_REFRESH_RATE;

    private GridRefreshManager() {

        // Add window's focus\blur events
        AttachWindowFocusEvents();

        // Read refresh rate cookie
        setGlobalRefreshRate(readRefreshRateCookie());

        // Save cookie on window close event and window refresh event
        Window.addCloseHandler(new CloseHandler<Window>() {
            @Override
            public void onClose(CloseEvent<Window> event) {
                saveRefreshRateCookie();
            }
        });
    }

    public static GridRefreshManager getInstance() {
        if (instance == null) {
            instance = new GridRefreshManager();
        }
        return instance;
    }

    // Subscribe the specified grid controller to the refresh service
    public void subscribe(GridController<?> controller) {
        if (!gridControllerList.contains(controller)) {
            gridControllerList.add(controller);
            controller.gridChangePerformed();

            GWT.log("Subscribe grid: " + controller.getClass().getName() + ", refresh rate: "
                    + controller.getRefreshRate());
        }
    }

    // Unsubscribe the specified grid controller to the refresh service
    public void unsubscribe(GridController<?> controller) {
        if (gridControllerList.contains(controller)) {
            gridControllerList.remove(controller);
            controller.stopRepeatedSearch();

            GWT.log("Unsubscribe grid: " + controller.getClass().getName());
        }
    }

    // Return true if the specified controller is subscribed to the service;
    // Otherwise, false
    public boolean isSubscribed(GridController<?> controller) {
        return gridControllerList.contains(controller);
    }

    // Suspend refresh
    public void suspendRefresh() {
        for (GridController<?> controller : gridControllerList) {
            controller.stopRepeatedSearch();
        }
    }

    // Refresh grids less frequently while not in focus
    public void backgroundRefresh() {
        for (GridController<?> controller : gridControllerList) {
            if (controller.isRapidTimerRunning()) {
                continue;
            }

            controller.stopRepeatedSearch();
            controller.repeatSearch(DEFAULT_REFRESH_RATE * 1000);

            GWT.log("Background refresh, grid: " + controller.getClass().getName() + ", refresh rate: "
                    + DEFAULT_REFRESH_RATE);
        }
    }

    // Start/Resume refresh
    public void refreshGrids() {
        for (GridController<?> controller : gridControllerList) {
            if (controller.isRapidTimerRunning()) {
                continue;
            }

            controller.search();
            controller.repeatSearch(controller.getRefreshRate() * 1000);

            GWT.log("Start/Resume refresh, grid: " + controller.getClass().getName() + ", refresh rate: "
                    + controller.getRefreshRate());
        }
    }

    // Set global refresh rate for all grids
    public void setGlobalRefreshRate(int refreshRate) {

        globalRefreshRate = refreshRate;

        for (GridController<?> controller : gridControllerList) {
            controller.setRefreshRate(refreshRate);
            controller.stopRepeatedSearch();
            refreshGrids();
        }
    }

    // Get global refresh rate
    // (global value - currently, all grids have the refresh same rate)
    public int getGlobalRefreshRate() {
        return globalRefreshRate;
    }

    // Save refresh rate value to a cookie and set expire date to fifty years from now
    // (only a single value is saved - currently, all grids have the refresh same rate)
    private void saveRefreshRateCookie()
    {
        long expire = new Date().getTime() + (1000 * 60 * 60 * 24 * 365 * 50); // fifty years

        if (gridControllerList.iterator().hasNext()) {
            int refreshRate = gridControllerList.iterator().next().getRefreshRate();
            Cookies.setCookie(REFRESH_RATE_COOKIE_NAME, String.valueOf(refreshRate), new Date(expire));
        }
    }

    // Return refresh rate value from the cookie - if exists; Otherwise, return default refresh rate
    // (connect automatically is true by default).
    private int readRefreshRateCookie()
    {
        String refreshRate = Cookies.getCookie(REFRESH_RATE_COOKIE_NAME);

        try {
            return new Integer(refreshRate).intValue();
        } catch (NumberFormatException e) {
            return DEFAULT_REFRESH_RATE;
        }
    }

    private JavaScriptObject activeElement;
    private final boolean lastEventWasBlur = false;

    public void onWindowFocus() {
        refreshGrids();
    }

    public void onWindowBlur() {
        backgroundRefresh();
    }

    public native void AttachWindowFocusEvents() /*-{
        var clientAgentType = @org.ovirt.engine.ui.userportal.client.util.ClientAgentType::new()();
        var browser = clientAgentType.@org.ovirt.engine.ui.userportal.client.util.ClientAgentType::browser;
        var isIE = browser.toLowerCase() == "explorer";

        if (isIE) {
            $doc.attachEvent("onfocusin", onFocus);
            $doc.attachEvent("onfocusout", onBlur);
        } else {
            $wnd.addEventListener("focus", onFocus, false);
            $wnd.addEventListener("blur", onBlur, false);
        }

        var context = this;
        function onFocus() {
            // only focus if previous event was a blur or we get lots of focus events (On IE)
            if (context.@org.ovirt.engine.ui.userportal.client.components.GridRefreshManager::lastEventWasBlur) {
                context.@org.ovirt.engine.ui.userportal.client.components.GridRefreshManager::lastEventWasBlur = false;
                context.@org.ovirt.engine.ui.userportal.client.components.GridRefreshManager::onWindowFocus()();
            }
        }
        function onBlur() {
            if (context.@org.ovirt.engine.ui.userportal.client.components.GridRefreshManager::activeElement != $doc.activeElement) {
                context.@org.ovirt.engine.ui.userportal.client.components.GridRefreshManager::activeElement = $doc.activeElement;
            } else {
                context.@org.ovirt.engine.ui.userportal.client.components.GridRefreshManager::lastEventWasBlur = true;
                context.@org.ovirt.engine.ui.userportal.client.components.GridRefreshManager::onWindowBlur()();
            }
        }
    }-*/;
}
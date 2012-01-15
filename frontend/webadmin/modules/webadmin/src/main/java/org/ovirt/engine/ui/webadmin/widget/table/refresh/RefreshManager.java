package org.ovirt.engine.ui.webadmin.widget.table.refresh;

import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

import org.ovirt.engine.ui.uicommonweb.models.GridController;
import org.ovirt.engine.ui.uicommonweb.models.GridTimer;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Cookies;

/**
 * Manages the Refresh Rate for a {@link GridController}
 */
public class RefreshManager {

    private static final Integer DEFAULT_REFRESH_RATE = GridTimer.DEFAULT_NORMAL_RATE;

    private static final String REFRESH_RATE_COOKIE_NAME = "GridRefreshRate";

    private static final Set<Integer> REFRESH_RATES = new LinkedHashSet<Integer>();

    static {
        REFRESH_RATES.add(DEFAULT_REFRESH_RATE);
        REFRESH_RATES.add(10000);
        REFRESH_RATES.add(20000);
        REFRESH_RATES.add(30000);
    }

    /**
     * the acceptable Refresh Rates
     *
     */
    public static Set<Integer> getRefreshRates() {
        return Collections.unmodifiableSet(REFRESH_RATES);
    }

    private final GridController controller;
    private final RefreshPanel refreshPanel;

    /**
     * Create a Manager for the specified {@link GridController}
     *
     */
    public RefreshManager(GridController controller) {
        this.controller = controller;
        controller.setRefreshRate(readRefreshRateCookie());
        controller.getTimer().addValueChangeHandler(new ValueChangeHandler<String>() {

            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                RefreshManager.this.refreshPanel.showStatus(event.getValue());
            }
        });

        refreshPanel = new RefreshPanel(this);
    }

    public int getCurrentRefreshRate() {
        return controller.getRefreshRate();
    }

    public RefreshPanel getRefreshPanel() {
        return refreshPanel;
    }

    /**
     * Called when the Grid becomes hidden
     */
    public void onBlur() {
        controller.toBackground();
    }

    /**
     * Called when the Grid becomes visible
     */
    public void onFocus() {
        controller.toForground();
    }

    public void setCurrentRefreshRate(int newRefreshRate) {
        controller.setRefreshRate(newRefreshRate);
        saveRefreshRateCookie();
    }

    // Return refresh rate value from the cookie - if exists; Otherwise, return default refresh rate
    private int readRefreshRateCookie()
    {
        String refreshRate = Cookies.getCookie(REFRESH_RATE_COOKIE_NAME + "_" + controller.getId());

        try {
            return new Integer(refreshRate).intValue();
        } catch (NumberFormatException e) {
            return DEFAULT_REFRESH_RATE;
        }
    }

    // Save refresh rate value to a cookie and set expire date to fifty years from now
    private void saveRefreshRateCookie()
    {
        long expire = new Date().getTime() + 1000 * 60 * 60 * 24 * 365 * 50; // fifty years

        Cookies.setCookie(REFRESH_RATE_COOKIE_NAME + "_" + controller.getId(),
                String.valueOf(getCurrentRefreshRate()),
                new Date(expire));
    }

}

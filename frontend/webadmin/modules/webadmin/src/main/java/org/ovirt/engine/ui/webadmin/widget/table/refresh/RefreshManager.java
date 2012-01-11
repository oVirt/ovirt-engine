package org.ovirt.engine.ui.webadmin.widget.table.refresh;

import java.util.Date;

import org.ovirt.engine.ui.common.widget.table.refresh.AbstractRefreshManager;
import org.ovirt.engine.ui.uicommonweb.models.GridController;

import com.google.gwt.user.client.Cookies;

public class RefreshManager extends AbstractRefreshManager {

    private final RefreshPanel refreshPanel;

    /**
     * Create a Manager for the specified {@link GridController}
     */
    public RefreshManager(GridController controller) {
        super(controller);
        this.refreshPanel = new RefreshPanel(this);
    }

    @Override
    protected void onRefresh(String status) {
        refreshPanel.showStatus(status);
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
            if (refreshRate == null) {
                return DEFAULT_REFRESH_RATE;
            }
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

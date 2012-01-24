package org.ovirt.engine.ui.common.widget.table.refresh;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.uicommonweb.models.GridController;
import org.ovirt.engine.ui.uicommonweb.models.GridTimer;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;

/**
 * Manages the Refresh Rate for a {@link GridController}.
 */
public abstract class AbstractRefreshManager {

    protected static final Integer DEFAULT_REFRESH_RATE = GridTimer.DEFAULT_NORMAL_RATE;
    protected static final String REFRESH_RATE_ITEM_NAME = "GridRefreshRate";
    protected static final Set<Integer> REFRESH_RATES = new LinkedHashSet<Integer>();

    static {
        REFRESH_RATES.add(DEFAULT_REFRESH_RATE);
        REFRESH_RATES.add(10000);
        REFRESH_RATES.add(20000);
        REFRESH_RATES.add(30000);
    }

    /**
     * The acceptable Refresh Rates
     */
    public static Set<Integer> getRefreshRates() {
        return Collections.unmodifiableSet(REFRESH_RATES);
    }

    protected final GridController controller;
    private final ClientStorage clientStorage;

    /**
     * Create a Manager for the specified {@link GridController}
     */
    public AbstractRefreshManager(GridController controller, ClientStorage clientStorage) {
        this.controller = controller;
        this.clientStorage = clientStorage;

        controller.setRefreshRate(readRefreshRate());
        controller.getTimer().addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                onRefresh(event.getValue());
            }
        });
    }

    protected abstract void onRefresh(String status);

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
        saveRefreshRate(newRefreshRate);
    }

    public int getCurrentRefreshRate() {
        return controller.getRefreshRate();
    }

    String getRefreshRateItemKey() {
        return REFRESH_RATE_ITEM_NAME + "_" + controller.getId();
    }

    /**
     * Saves the Grid refresh rate into local storage.
     */
    void saveRefreshRate(int newRefreshRate) {
        clientStorage.setLocalItem(getRefreshRateItemKey(), String.valueOf(newRefreshRate));
    }

    /**
     * Returns the Grid refresh rate if it was saved. Otherwise, returns the default refresh rate.
     */
    int readRefreshRate() {
        String refreshRate = clientStorage.getLocalItem(getRefreshRateItemKey());

        try {
            return new Integer(refreshRate).intValue();
        } catch (NumberFormatException e) {
            return DEFAULT_REFRESH_RATE;
        }
    }

}

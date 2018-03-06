package org.ovirt.engine.ui.common.widget.tab;

import org.ovirt.engine.ui.common.uicommon.model.GroupedTabData;

/**
 * Implementation of {@link com.gwtplatform.mvp.client.TabData TabData} interface for use with dynamic tab presenters.
 */
public class DynamicTabData extends GroupedTabData {

    private final String historyToken;

    public DynamicTabData(String label, int priority, String historyToken) {
        super(label, null, priority, -1, null);
        this.historyToken = historyToken;
    }

    public String getHistoryToken() {
        return historyToken;
    }

}

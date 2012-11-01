package org.ovirt.engine.ui.common.widget.tab;

import com.gwtplatform.mvp.client.TabDataBasic;

/**
 * Implementation of {@link com.gwtplatform.mvp.client.TabData TabData} interface for use with dynamic tab presenters.
 */
public class DynamicTabData extends TabDataBasic {

    private final String historyToken;

    public DynamicTabData(String label, float priority, String historyToken) {
        super(label, priority);
        this.historyToken = historyToken;
    }

    public String getHistoryToken() {
        return historyToken;
    }

}

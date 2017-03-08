package org.ovirt.engine.ui.common.widget.tab;

import org.ovirt.engine.ui.common.uicommon.model.GroupedTabData;
import org.ovirt.engine.ui.common.widget.Align;

/**
 * Implementation of {@link com.gwtplatform.mvp.client.TabData TabData} interface for use with dynamic tab presenters.
 */
public class DynamicTabData extends GroupedTabData {

    private final String historyToken;
    private final Align align;

    public DynamicTabData(String label, int priority, String historyToken) {
        this(label, priority, historyToken, Align.LEFT);
    }

    public DynamicTabData(String label, int priority, String historyToken, Align align) {
        super(label, null, align == Align.RIGHT ? Integer.MAX_VALUE : priority, -1, null);
        this.historyToken = historyToken;
        this.align = align;
    }

    public String getHistoryToken() {
        return historyToken;
    }

    public Align getAlign() {
        return align;
    }

}

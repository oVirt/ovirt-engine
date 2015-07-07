package org.ovirt.engine.ui.common.widget.tab;

import org.ovirt.engine.ui.common.widget.Align;
import com.gwtplatform.mvp.client.TabDataBasic;

/**
 * Implementation of {@link com.gwtplatform.mvp.client.TabData TabData} interface for use with dynamic tab presenters.
 */
public class DynamicTabData extends TabDataBasic {

    private final String historyToken;
    private final Align align;

    public DynamicTabData(String label, float priority, String historyToken) {
        this(label, priority, historyToken, Align.LEFT);
    }

    public DynamicTabData(String label, float priority, String historyToken, Align align) {
        super(label, priority);
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

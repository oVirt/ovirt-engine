package org.ovirt.engine.ui.common.view.popup.numa;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class NumaPanel extends Composite {
    interface WidgetUiBinder extends UiBinder<Widget, NumaPanel> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    @UiField(provided=true)
    CpuSummaryPanel cpuSummaryPanel;

    @UiField(provided=true)
    MemorySummaryPanel memorySummaryPanel;

    @Inject
    public NumaPanel(CpuSummaryPanel cpuSummary, MemorySummaryPanel memorySummary) {
        this.cpuSummaryPanel = cpuSummary;
        this.memorySummaryPanel = memorySummary;
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
    }

    public CpuSummaryPanel getCpuSummaryPanel() {
        return this.cpuSummaryPanel;
    }

    public MemorySummaryPanel getMemorySummaryPanel() {
        return this.memorySummaryPanel;
    }
}

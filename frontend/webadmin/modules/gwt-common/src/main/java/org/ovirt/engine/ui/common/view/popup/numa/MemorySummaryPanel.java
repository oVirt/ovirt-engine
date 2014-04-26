package org.ovirt.engine.ui.common.view.popup.numa;

import org.ovirt.engine.ui.common.CommonApplicationMessages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class MemorySummaryPanel extends Composite {
    interface WidgetUiBinder extends UiBinder<Widget, MemorySummaryPanel> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    private final CommonApplicationMessages messages;

    @UiField
    Label totalLabel;

    @UiField
    Label usedLabel;

    @Inject
    public MemorySummaryPanel(CommonApplicationMessages messages) {
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
        this.messages = messages;
    }

    public void setMemoryStats(long totalMemory, long usedMemory) {
        String totalMemoryString = messages.numaMemory(totalMemory);
        totalLabel.setTitle(totalMemoryString);
        totalLabel.setText(totalMemoryString);

        String usedMemoryString = messages.numaMemoryUsed(usedMemory);
        usedLabel.setTitle(usedMemoryString);
        usedLabel.setText(usedMemoryString);
    }
}

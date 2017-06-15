package org.ovirt.engine.ui.common.view.popup.numa;

import org.ovirt.engine.ui.common.CommonApplicationMessages;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.widget.label.LabelWithTextTruncation;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class MemorySummaryPanel extends Composite {
    interface WidgetUiBinder extends UiBinder<Widget, MemorySummaryPanel> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    private static final CommonApplicationMessages messages = AssetProvider.getMessages();

    @UiField
    LabelWithTextTruncation totalLabel;

    @UiField
    LabelWithTextTruncation usedLabel;

    @Inject
    public MemorySummaryPanel() {
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
    }

    public void setMemoryStats(long totalMemory, long usedMemory) {
        String totalMemoryString = messages.numaMemory(totalMemory);
        totalLabel.setText(totalMemoryString);

        String usedMemoryString = messages.numaMemoryUsed(usedMemory);
        usedLabel.setText(usedMemoryString);
    }
}

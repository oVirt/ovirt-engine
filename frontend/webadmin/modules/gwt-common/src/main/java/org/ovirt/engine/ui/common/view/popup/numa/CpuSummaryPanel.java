package org.ovirt.engine.ui.common.view.popup.numa;

import org.ovirt.engine.ui.common.CommonApplicationMessages;
import org.ovirt.engine.ui.common.gin.AssetProvider;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class CpuSummaryPanel extends Composite {

    interface WidgetUiBinder extends UiBinder<Widget, CpuSummaryPanel> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    @UiField
    Label nameLabel;

    @UiField
    Label totalLabel;

    @UiField
    Label percentageLabel;

    private final static CommonApplicationMessages messages = AssetProvider.getMessages();

    @Inject
    public CpuSummaryPanel() {
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
    }

    public void setName(String name) {
        nameLabel.setTitle(name);
        nameLabel.setText(name);
    }

    public void setCpus(int totalCpus, int usedPercentage) {
        String totalCpusString = messages.numaTotalCpus(totalCpus);

        totalLabel.setTitle(totalCpusString);
        totalLabel.setText(totalCpusString);

        String percentageUsed = messages.numaPercentUsed(usedPercentage);
        percentageLabel.setTitle(percentageUsed);
        percentageLabel.setText(percentageUsed);
    }
}

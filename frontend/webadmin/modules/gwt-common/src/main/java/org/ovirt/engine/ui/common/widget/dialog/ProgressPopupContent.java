package org.ovirt.engine.ui.common.widget.dialog;

import org.ovirt.engine.ui.common.widget.IsProgressContentWidget;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;

public class ProgressPopupContent extends Composite implements IsProgressContentWidget {

    interface WidgetUiBinder extends UiBinder<FlowPanel, ProgressPopupContent> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    public ProgressPopupContent() {
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
    }

    @Override
    public void setProgressMessage(String text) {
    }

}

package org.ovirt.engine.ui.common.widget.form;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Widget;

public class GeneralFormPanel extends AbstractFormPanel {

    interface WidgetUiBinder extends UiBinder<Widget, AbstractFormPanel> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    public GeneralFormPanel() {
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
    }

}

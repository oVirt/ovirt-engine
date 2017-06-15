package org.ovirt.engine.ui.common.widget.label;

import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.gin.AssetProvider;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class NoItemsLabel extends Composite {
    interface WidgetUiBinder extends UiBinder<Widget, NoItemsLabel> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    private static final CommonApplicationConstants constants = AssetProvider.getConstants();

    @UiField
    Label label;

    public NoItemsLabel() {
        super();
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
        label.setText(constants.noItemsToDisplay());
    }
}

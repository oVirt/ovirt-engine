package org.ovirt.engine.ui.webadmin.section.main.view.popup.macpool;

import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.widget.AddRemoveRowWidget;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.macpool.MacRangeModel;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Widget;

public class MacRangeWidget extends AddRemoveRowWidget<ListModel<MacRangeModel>, MacRangeModel, MacRangeEditor> {

    interface WidgetUiBinder extends UiBinder<Widget, MacRangeWidget> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    interface WidgetIdHandler extends ElementIdHandler<MacRangeWidget> {
        WidgetIdHandler idHandler = GWT.create(WidgetIdHandler.class);
    }

    public MacRangeWidget() {
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
        WidgetIdHandler.idHandler.generateAndSetIds(this);
    }

    @Override
    protected MacRangeEditor createWidget(MacRangeModel value) {
        MacRangeEditor widget = new MacRangeEditor();
        widget.edit(value);
        return widget;
    }

    @Override
    protected MacRangeModel createGhostValue() {
        return new MacRangeModel();
    }

    @Override
    protected boolean isGhost(MacRangeModel value) {
        return false;
    }

}

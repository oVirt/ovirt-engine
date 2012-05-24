package org.ovirt.engine.ui.common.widget.uicommon.popup.vm;

import org.ovirt.engine.ui.common.widget.uicommon.popup.AbstractModelBoundPopupWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.VncInfoModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;

public class VncInfoPopupWidget extends AbstractModelBoundPopupWidget<VncInfoModel> {

    interface Driver extends SimpleBeanEditorDriver<VncInfoModel, VncInfoPopupWidget> {
        Driver driver = GWT.create(Driver.class);
    }

    interface ViewUiBinder extends UiBinder<VerticalPanel, VncInfoPopupWidget> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    @Ignore
    HTML message;

    public VncInfoPopupWidget() {
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        addStyles();
        Driver.driver.initialize(this);
    }

    void addStyles() {
    }

    @Override
    public void edit(final VncInfoModel object) {
        Driver.driver.edit(object);
        message.setHTML((String) object.getVncMessage().getEntity());
    }

    @Override
    public VncInfoModel flush() {
        return Driver.driver.flush();
    }

}

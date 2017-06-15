package org.ovirt.engine.ui.common.widget.uicommon.popup.vm;

import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.widget.uicommon.popup.AbstractModelBoundPopupWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.VncInfoModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;

public class VncInfoPopupWidget extends AbstractModelBoundPopupWidget<VncInfoModel> {

    interface Driver extends UiCommonEditorDriver<VncInfoModel, VncInfoPopupWidget> {
    }

    interface ViewUiBinder extends UiBinder<VerticalPanel, VncInfoPopupWidget> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    @Ignore
    HTML message;

    private final Driver driver = GWT.create(Driver.class);

    public VncInfoPopupWidget() {
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        addStyles();
        driver.initialize(this);
    }

    void addStyles() {
    }

    @Override
    public void edit(final VncInfoModel object) {
        driver.edit(object);
        message.setHTML((String) object.getVncMessage().getEntity());
    }

    @Override
    public VncInfoModel flush() {
        return driver.flush();
    }

    @Override
    public void cleanup() {
        driver.cleanup();
    }
}

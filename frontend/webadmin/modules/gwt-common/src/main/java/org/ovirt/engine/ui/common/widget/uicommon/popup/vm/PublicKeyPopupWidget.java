package org.ovirt.engine.ui.common.widget.uicommon.popup.vm;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextAreaEditor;
import org.ovirt.engine.ui.common.widget.uicommon.popup.AbstractModelBoundPopupWidget;
import org.ovirt.engine.ui.uicommonweb.models.PublicKeyModel;

public class PublicKeyPopupWidget extends AbstractModelBoundPopupWidget<PublicKeyModel> {
    interface Driver extends SimpleBeanEditorDriver<PublicKeyModel, PublicKeyPopupWidget> {
    }

    interface ViewUiBinder extends UiBinder<FlowPanel, PublicKeyPopupWidget> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<PublicKeyPopupWidget> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    interface Style extends CssResource {
        String publicKeyEditorLabel();

        String publicKeyEditorBox();

        String publicKeyEditor();
    }

    @UiField
    @Path(value = "textInput.entity")
    @WithElementId("publicKey")
    StringEntityModelTextAreaEditor publicKeyEditor;

    private final Driver driver = GWT.create(Driver.class);

    private static final CommonApplicationConstants constants = GWT.create(CommonApplicationConstants.class);

    public PublicKeyPopupWidget() {
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));

        localize();
        ViewIdHandler.idHandler.generateAndSetIds(this);
        driver.initialize(this);
    }

    void localize() {
        publicKeyEditor.setLabel(constants.consolePublicKey());
    }

    @Override
    public void edit(PublicKeyModel object) {
        driver.edit(object);
    }

    @Override
    public PublicKeyModel flush() {
        return driver.flush();
    }

    @Override
    public void focusInput() {
        publicKeyEditor.setFocus(true);
    }
}

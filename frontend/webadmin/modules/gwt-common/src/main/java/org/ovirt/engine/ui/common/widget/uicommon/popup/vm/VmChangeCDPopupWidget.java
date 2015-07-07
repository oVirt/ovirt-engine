package org.ovirt.engine.ui.common.widget.uicommon.popup.vm;

import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.uicommon.popup.AbstractModelBoundPopupWidget;
import org.ovirt.engine.ui.uicommonweb.models.userportal.AttachCdModel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.VerticalPanel;

public class VmChangeCDPopupWidget extends AbstractModelBoundPopupWidget<AttachCdModel> {

    interface Driver extends SimpleBeanEditorDriver<AttachCdModel, VmChangeCDPopupWidget> {
    }

    interface ViewUiBinder extends UiBinder<VerticalPanel, VmChangeCDPopupWidget> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<VmChangeCDPopupWidget> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    interface Style extends CssResource {
        String isoImageEditorLabel();

        String isoImageEditorBox();
    }

    @UiField
    Style style;

    @UiField
    @Path(value = "isoImage.selectedItem")
    @WithElementId("isoImage")
    ListModelListBoxEditor<String> isoImageEditor;

    private final Driver driver = GWT.create(Driver.class);

    public VmChangeCDPopupWidget() {
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        addStyles();
        ViewIdHandler.idHandler.generateAndSetIds(this);
        driver.initialize(this);
    }

    void addStyles() {
        isoImageEditor.addLabelStyleName(style.isoImageEditorLabel());
        isoImageEditor.addContentWidgetContainerStyleName(style.isoImageEditorBox());
    }

    @Override
    public void edit(AttachCdModel object) {
        driver.edit(object);
    }

    @Override
    public AttachCdModel flush() {
        return driver.flush();
    }

    @Override
    public void focusInput() {
        isoImageEditor.setFocus(true);
    }

}

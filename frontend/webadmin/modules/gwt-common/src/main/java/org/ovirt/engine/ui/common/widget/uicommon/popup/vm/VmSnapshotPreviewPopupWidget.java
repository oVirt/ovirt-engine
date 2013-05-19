package org.ovirt.engine.ui.common.widget.uicommon.popup.vm;

import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.uicommon.popup.AbstractModelBoundPopupWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.SnapshotModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

public class VmSnapshotPreviewPopupWidget extends AbstractModelBoundPopupWidget<SnapshotModel> {

    interface Driver extends SimpleBeanEditorDriver<SnapshotModel, VmSnapshotPreviewPopupWidget> {
    }

    interface ViewUiBinder extends UiBinder<FlowPanel, VmSnapshotPreviewPopupWidget> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<VmSnapshotPreviewPopupWidget> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @UiField
    @Ignore
    Label messageLabel;

    @UiField(provided = true)
    @Path(value = "memory.entity")
    @WithElementId("memory")
    public EntityModelCheckBoxEditor memoryEditor;

    private final Driver driver = GWT.create(Driver.class);

    public VmSnapshotPreviewPopupWidget(CommonApplicationConstants constants) {
        initEditors();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        localize(constants);
        ViewIdHandler.idHandler.generateAndSetIds(this);
        driver.initialize(this);
    }

    private void initEditors() {
        memoryEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
    }

    void localize(CommonApplicationConstants constants) {
        memoryEditor.setLabel(constants.virtualMachineSnapshotPreviewPopupMemoryLabel());
        messageLabel.setText(constants.snapshotContainsMemory());
    }

    @Override
    public void edit(final SnapshotModel model) {
        driver.edit(model);
    }

    @Override
    public SnapshotModel flush() {
        return driver.flush();
    }

    @Override
    public void focusInput() {
        memoryEditor.setFocus(true);
    }
}

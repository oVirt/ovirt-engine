package org.ovirt.engine.ui.common.widget.uicommon.popup.vm;

import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.widget.editor.EntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.uicommon.popup.AbstractModelBoundPopupWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.SnapshotModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

public class VmSnapshotCreatePopupWidget extends AbstractModelBoundPopupWidget<SnapshotModel> {

    interface Driver extends SimpleBeanEditorDriver<SnapshotModel, VmSnapshotCreatePopupWidget> {
        Driver driver = GWT.create(Driver.class);
    }

    interface ViewUiBinder extends UiBinder<VerticalPanel, VmSnapshotCreatePopupWidget> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    @Path(value = "description.entity")
    EntityModelTextBoxEditor descriptionEditor;

    @UiField
    Label message;

    public VmSnapshotCreatePopupWidget(CommonApplicationConstants constants) {
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        localize(constants);
        Driver.driver.initialize(this);
    }

    void localize(CommonApplicationConstants constants) {
        descriptionEditor.setLabel(constants.virtualMachineSnapshotCreatePopupDescriptionLabel());
    }

    @Override
    public void edit(SnapshotModel object) {
        Driver.driver.edit(object);
    }

    @Override
    public SnapshotModel flush() {
        return Driver.driver.flush();
    }

    @Override
    public void focusInput() {
        descriptionEditor.setFocus(true);
    }

}

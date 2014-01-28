package org.ovirt.engine.ui.common.widget.uicommon.popup.vm;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.uicommon.popup.AbstractModelBoundPopupWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.CloneVmModel;

public class CloneVmWidget extends AbstractModelBoundPopupWidget<CloneVmModel> {

    private final CommonApplicationConstants constants;

    interface Driver extends SimpleBeanEditorDriver<CloneVmModel, CloneVmWidget> {
    }

    interface ViewUiBinder extends UiBinder<FlowPanel, CloneVmWidget> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<CloneVmWidget> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private final Driver driver = GWT.create(Driver.class);

    @UiField
    @Path(value = "cloneName.entity")
    @WithElementId("cloneName")
    StringEntityModelTextBoxEditor cloneNameEditor;

    public CloneVmWidget(CommonApplicationConstants constants) {
        this.constants = constants;

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        driver.initialize(this);

        localize();
    }

    private void localize() {
        cloneNameEditor.setLabel(constants.clonedVmName());
    }

    @Override
    public void edit(final CloneVmModel object) {
        driver.edit(object);
    }

    @Override
    public CloneVmModel flush() {
        return driver.flush();
    }

    @Override
    public void focusInput() {
        cloneNameEditor.setFocus(true);
    }
}

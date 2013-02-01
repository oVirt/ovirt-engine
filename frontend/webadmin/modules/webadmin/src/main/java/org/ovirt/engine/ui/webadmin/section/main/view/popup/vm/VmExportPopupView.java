package org.ovirt.engine.ui.webadmin.section.main.view.popup.vm;

import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.uicommonweb.models.vms.ExportVmModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmExportPopupPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;

public class VmExportPopupView extends AbstractModelBoundPopupView<ExportVmModel>
        implements VmExportPopupPresenterWidget.ViewDef {

    interface Driver extends SimpleBeanEditorDriver<ExportVmModel, VmExportPopupView> {
        Driver driver = GWT.create(Driver.class);
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, VmExportPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField(provided = true)
    @Path(value = "forceOverride.entity")
    EntityModelCheckBoxEditor forceOverride;

    @UiField(provided = true)
    @Path(value = "collapseSnapshots.entity")
    EntityModelCheckBoxEditor collapseSnapshots;

    @UiField
    @Ignore
    FlowPanel messagePanel;

    @Inject
    public VmExportPopupView(EventBus eventBus, ApplicationResources resources, ApplicationConstants constants) {
        super(eventBus, resources);
        initCheckBoxes();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        localize(constants);
        Driver.driver.initialize(this);
    }

    void initCheckBoxes() {
        forceOverride = new EntityModelCheckBoxEditor(Align.RIGHT);
        collapseSnapshots = new EntityModelCheckBoxEditor(Align.RIGHT);
    }

    void localize(ApplicationConstants constants) {
        forceOverride.setLabel(constants.vmExportPopupForceOverrideLabel());
        collapseSnapshots.setLabel(constants.vmExportPopupCollapseSnapshotsLabel());
    }

    @Override
    public void setMessage(String message) {
        if (message == null) {
            return;
        }

        messagePanel.add(new Label(message));
    }

    @Override
    public void edit(ExportVmModel object) {
        Driver.driver.edit(object);
    }

    @Override
    public ExportVmModel flush() {
        return Driver.driver.flush();
    }

}

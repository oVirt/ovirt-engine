package org.ovirt.engine.ui.webadmin.section.main.view.popup.vm;

import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.AlertWithIcon;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.uicommonweb.models.vms.ExportVmModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationMessages;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmExportPopupPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.inject.Inject;

public class VmExportPopupView extends AbstractModelBoundPopupView<ExportVmModel>
        implements VmExportPopupPresenterWidget.ViewDef {

    interface Driver extends UiCommonEditorDriver<ExportVmModel, VmExportPopupView> {
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
    AlertWithIcon messagePanel;

    private final Driver driver = GWT.create(Driver.class);

    private static final ApplicationConstants constants = AssetProvider.getConstants();
    private static final ApplicationMessages messages = AssetProvider.getMessages();

    @Inject
    public VmExportPopupView(EventBus eventBus) {
        super(eventBus);
        initCheckBoxes();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        localize();
        driver.initialize(this);
    }

    void initCheckBoxes() {
        forceOverride = new EntityModelCheckBoxEditor(Align.RIGHT);
        collapseSnapshots = new EntityModelCheckBoxEditor(Align.RIGHT);
    }

    void localize() {
        setMessage(messages.exportDomainDeprecationWarning());
        forceOverride.setLabel(constants.vmExportPopupForceOverrideLabel());
        collapseSnapshots.setLabel(constants.vmExportPopupCollapseSnapshotsLabel());
    }

    @Override
    public void setMessage(String message) {
        if (message == null) {
            return;
        }

        message = message.replace("\n", "<br>"); //$NON-NLS-1$ //$NON-NLS-2$
        messagePanel.setHtmlText(SafeHtmlUtils.fromTrustedString(message));
    }

    @Override
    public void edit(ExportVmModel object) {
        driver.edit(object);
    }

    @Override
    public ExportVmModel flush() {
        return driver.flush();
    }

    @Override
    public void cleanup() {
        driver.cleanup();
    }
}

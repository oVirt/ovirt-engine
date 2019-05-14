package org.ovirt.engine.ui.common.widget.uicommon.popup.vm;

import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationMessages;
import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.editor.ListModelRadioGroupEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.uicommon.popup.AbstractModelBoundPopupWidget;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.models.vms.SnapshotModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;

public class VmSnapshotPreviewPopupWidget extends AbstractModelBoundPopupWidget<SnapshotModel> {

    interface Driver extends UiCommonEditorDriver<SnapshotModel, VmSnapshotPreviewPopupWidget> {
    }

    interface ViewUiBinder extends UiBinder<FlowPanel, VmSnapshotPreviewPopupWidget> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<VmSnapshotPreviewPopupWidget> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @UiField
    FlowPanel partialSnapshotWarningPanel;

    @UiField
    @Ignore
    Label vmDisksLabel;

    @UiField
    @Ignore
    Label snapshotDisksLabel;

    @UiField
    @Path(value = "partialPreviewSnapshotOptions.selectedItem")
    public ListModelRadioGroupEditor<SnapshotModel.PreivewPartialSnapshotOption> partialPreviewSnapshotOptionEditor;


    @UiField
    FlowPanel memoryWarningPanel;

    @UiField
    SimplePanel horizontalSeparator;

    @UiField
    @Ignore
    Label messageLabel;

    @UiField(provided = true)
    @Path(value = "memory.entity")
    @WithElementId("memory")
    public EntityModelCheckBoxEditor memoryEditor;

    private final Driver driver = GWT.create(Driver.class);

    private static final CommonApplicationConstants constants = AssetProvider.getConstants();
    private static final CommonApplicationMessages messages = AssetProvider.getMessages();

    public VmSnapshotPreviewPopupWidget() {
        initEditors();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        localize();
        ViewIdHandler.idHandler.generateAndSetIds(this);
        driver.initialize(this);
    }

    private void initEditors() {
        memoryEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
    }

    void localize() {
        memoryEditor.setLabel(constants.virtualMachineSnapshotPreviewPopupMemoryLabel());
        messageLabel.setText(constants.previewSnapshotContainsMemory());
    }

    @Override
    public void edit(final SnapshotModel model) {
        driver.edit(model);

        if (model.isShowMemorySnapshotWarning() && !model.isShowPartialSnapshotWarning()) {
            Style dialogStyle = getParent().getParent().getParent().getElement().getStyle();
            dialogStyle.setWidth(450, Style.Unit.PX);
            dialogStyle.setHeight(240, Style.Unit.PX);
        }

        partialSnapshotWarningPanel.setVisible(model.isShowPartialSnapshotWarning());
        memoryWarningPanel.setVisible(model.isShowMemorySnapshotWarning());
        if (model.getOldClusterVersionOfSnapshotWithMemory() != null) {
            messageLabel.setText(messages.snapshotContainsMemoryIncompatibleCluster(
                    model.getOldClusterVersionOfSnapshotWithMemory().toString()));
            model.getMemory().setEntity(false);
        }

        horizontalSeparator.setVisible(model.isShowPartialSnapshotWarning() && model.isShowMemorySnapshotWarning());

        vmDisksLabel.setText(messages.vmDisksLabel(model.getVmDisks().size(),
                String.join(", ", Linq.getDiskAliases(model.getVmDisks())))); //$NON-NLS-1$
        snapshotDisksLabel.setText(messages.snapshotDisksLabel(model.getDisks().size(),
                String.join(", ", Linq.getDiskAliases(model.getDisks())))); //$NON-NLS-1$
    }

    @Override
    public SnapshotModel flush() {
        return driver.flush();
    }

    @Override
    public void cleanup() {
        driver.cleanup();
    }

    @Override
    public void focusInput() {
        memoryEditor.setFocus(true);
    }
}

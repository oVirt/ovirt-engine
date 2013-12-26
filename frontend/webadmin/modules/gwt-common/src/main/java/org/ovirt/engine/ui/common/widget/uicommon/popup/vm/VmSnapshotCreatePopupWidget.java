package org.ovirt.engine.ui.common.widget.uicommon.popup.vm;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.EntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.ListModelObjectCellTable;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.common.widget.uicommon.popup.AbstractModelBoundPopupWidget;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.SnapshotModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

public class VmSnapshotCreatePopupWidget extends AbstractModelBoundPopupWidget<SnapshotModel> {

    interface Driver extends SimpleBeanEditorDriver<SnapshotModel, VmSnapshotCreatePopupWidget> {
    }

    interface ViewUiBinder extends UiBinder<FlowPanel, VmSnapshotCreatePopupWidget> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<VmSnapshotCreatePopupWidget> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @UiField
    @Path(value = "description.entity")
    @WithElementId("description")
    EntityModelTextBoxEditor descriptionEditor;

    @UiField(provided = true)
    @Path(value = "memory.entity")
    @WithElementId("memory")
    EntityModelCheckBoxEditor memoryEditor;

    @UiField
    @Ignore
    Label disksTableLabel;

    @UiField
    @Ignore
    ScrollPanel disksPanel;

    @UiField(provided = true)
    @Ignore
    ListModelObjectCellTable<DiskImage, ListModel> disksTable;

    @UiField
    @Ignore
    FlowPanel messagePanel;

    private final Driver driver = GWT.create(Driver.class);

    public VmSnapshotCreatePopupWidget(CommonApplicationConstants constants) {
        initEditors();
        initTables(constants);
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        localize(constants);
        ViewIdHandler.idHandler.generateAndSetIds(this);
        driver.initialize(this);
    }

    private void initEditors() {
        memoryEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
    }

    private void initTables(CommonApplicationConstants constants) {
        disksTable = new ListModelObjectCellTable<DiskImage, ListModel>(true, true);
        disksTable.enableColumnResizing();

        disksTable.addColumn(new TextColumnWithTooltip<DiskImage>() {
            @Override
            public String getValue(DiskImage diskImage) {
                return diskImage.getDiskAlias();
            }
        }, constants.aliasDisk(), "150px"); //$NON-NLS-1$

        disksTable.addColumn(new TextColumnWithTooltip<DiskImage>() {
            @Override
            public String getValue(DiskImage diskImage) {
                return diskImage.getDiskDescription();
            }
        }, constants.descriptionDisk(), "150px"); //$NON-NLS-1$
    }

    void localize(CommonApplicationConstants constants) {
        descriptionEditor.setLabel(constants.virtualMachineSnapshotCreatePopupDescriptionLabel());
        memoryEditor.setLabel(constants.virtualMachineSnapshotCreatePopupMemoryLabel());
        disksTableLabel.setText(constants.snapshotDisks());
    }

    @Override
    public void edit(final SnapshotModel model) {
        driver.edit(model);

        editDisksTable(model);

        model.getPropertyChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                String propName = ((PropertyChangedEventArgs) args).propertyName;
                if ("Message".equals(propName)) { //$NON-NLS-1$
                    appendMessage(model.getMessage());
                } else if ("VM".equals(propName)) { //$NON-NLS-1$
                    updateMemoryBoxVisibility();
                }
            }

            private void updateMemoryBoxVisibility() {
                VM vm = model.getVm();
                if (vm == null) {
                    return;
                }

                boolean memorySnapshotSupported =
                        (Boolean) AsyncDataProvider.getConfigValuePreConverted(
                                ConfigurationValues.MemorySnapshotSupported,
                                vm.getVdsGroupCompatibilityVersion().toString());
                memoryEditor.setVisible(memorySnapshotSupported && vm.isRunning());
                // The memory option is enabled by default, so in case its checkbox
                // is not visible, we should disable it explicitly
                if (!memoryEditor.isVisible()) {
                    model.getMemory().setEntity(false);
                }
            }
        });
    }

    private void editDisksTable(SnapshotModel model) {
        disksTable.asEditor().edit(model.getSnapshotDisks());
        model.getSnapshotDisks().getItemsChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                disksTable.selectAll();
            }
        });
    }

    @Override
    public SnapshotModel flush() {
        return driver.flush();
    }

    @Override
    public void focusInput() {
        descriptionEditor.setFocus(true);
    }

    public void appendMessage(String message) {
        if (message == null) {
            return;
        }

        messagePanel.add(new Label(message));
    }

}

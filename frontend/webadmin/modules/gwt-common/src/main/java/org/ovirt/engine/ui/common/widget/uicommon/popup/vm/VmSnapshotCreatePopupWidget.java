package org.ovirt.engine.ui.common.widget.uicommon.popup.vm;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.ListModelObjectCellTable;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumnWithTooltip;
import org.ovirt.engine.ui.common.widget.uicommon.popup.AbstractModelBoundPopupWidget;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.SnapshotModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

import java.util.ArrayList;

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
    StringEntityModelTextBoxEditor descriptionEditor;

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

    @UiField
    SimplePanel warningPanel;

    private final Driver driver = GWT.create(Driver.class);
    private CommonApplicationTemplates templates;
    private CommonApplicationResources resources;
    private CommonApplicationConstants constants;

    public VmSnapshotCreatePopupWidget(CommonApplicationConstants constants, CommonApplicationTemplates templates,
                                       CommonApplicationResources resources) {
        this.constants = constants;
        this.templates = templates;
        this.resources = resources;

        initEditors();
        initTables();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        localize(constants);
        ViewIdHandler.idHandler.generateAndSetIds(this);
        driver.initialize(this);
    }

    private void initEditors() {
        memoryEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
    }

    private void initTables() {
        disksTable = new ListModelObjectCellTable<DiskImage, ListModel>(true, true);
        disksTable.enableColumnResizing();

        disksTable.addColumn(new AbstractTextColumnWithTooltip<DiskImage>() {
            @Override
            public String getValue(DiskImage diskImage) {
                return diskImage.getDiskAlias();
            }
        }, constants.aliasDisk(), "150px"); //$NON-NLS-1$

        disksTable.addColumn(new AbstractTextColumnWithTooltip<DiskImage>() {
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

        model.getPropertyChangedEvent().addListener(new IEventListener<PropertyChangedEventArgs>() {
            @Override
            public void eventRaised(Event<? extends PropertyChangedEventArgs> ev, Object sender, PropertyChangedEventArgs args) {
                String propName = args.propertyName;
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
                        AsyncDataProvider.getInstance().isMemorySnapshotSupported(vm);
                memoryEditor.setVisible(memorySnapshotSupported && vm.isRunning());
                // The memory option is enabled by default, so in case its checkbox
                // is not visible, we should disable it explicitly
                if (!memoryEditor.isVisible()) {
                    model.getMemory().setEntity(false);
                }
            }
        });

        model.getMemory().getEntityChangedEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                updateMemoryWarning(model);
            }
        });
    }

    private void editDisksTable(final SnapshotModel model) {
        disksTable.asEditor().edit(model.getSnapshotDisks());
        model.getSnapshotDisks().getItemsChangedEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                disksTable.selectAll();
            }
        });

        model.getSnapshotDisks().getSelectedItemsChangedEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                updateMemoryWarning(model);
            }
        });
    }

    private void updateMemoryWarning(SnapshotModel model) {
        ArrayList<DiskImage> diskImages = (ArrayList<DiskImage>) model.getSnapshotDisks().getItems();
        ArrayList<DiskImage> selectedDiskImages = (ArrayList<DiskImage>) model.getSnapshotDisks().getSelectedItems();

        boolean partialDisksSelection = selectedDiskImages != null && diskImages.size() != selectedDiskImages.size();
        boolean isIncludeMemory = model.getMemory().getEntity();

        SafeHtml warningImage = SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(
                resources.logWarningImage()).getHTML());
        HTML warningWidget = new HTML(templates.iconWithText(
                warningImage, constants.snapshotCreationWithMemoryAndPartialDisksWarning()));

        // Show warning in case of saving memory to snapshot and excluding some disks.
        warningPanel.setWidget(isIncludeMemory && partialDisksSelection ? warningWidget : null);
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

package org.ovirt.engine.ui.common.widget.uicommon.popup.vm;

import java.util.Collection;
import java.util.List;

import org.gwtbootstrap3.client.ui.Container;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.editor.ListModelObjectCellTable;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.common.widget.uicommon.popup.AbstractModelBoundPopupWidget;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.SnapshotModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;

public class VmSnapshotCreatePopupWidget extends AbstractModelBoundPopupWidget<SnapshotModel> {

    interface Driver extends UiCommonEditorDriver<SnapshotModel, VmSnapshotCreatePopupWidget> {
    }

    interface ViewUiBinder extends UiBinder<Container, VmSnapshotCreatePopupWidget> {
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

    @UiField(provided = true)
    @Ignore
    ListModelObjectCellTable<DiskImage, ListModel> disksTable;

    @UiField
    @Ignore
    FlowPanel messagePanel;

    @UiField
    FlowPanel warningPanel;

    private final Driver driver = GWT.create(Driver.class);

    private static final CommonApplicationTemplates templates = AssetProvider.getTemplates();
    private static final CommonApplicationResources resources = AssetProvider.getResources();
    private static final CommonApplicationConstants constants = AssetProvider.getConstants();

    public VmSnapshotCreatePopupWidget() {
        initEditors();
        initTables();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        driver.initialize(this);
    }

    private void initEditors() {
        memoryEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
    }

    private void initTables() {
        disksTable = new ListModelObjectCellTable<>(true, true);
        disksTable.enableColumnResizing();

        disksTable.addColumn(new AbstractTextColumn<DiskImage>() {
            @Override
            public String getValue(DiskImage diskImage) {
                return diskImage.getDiskAlias();
            }
        }, constants.aliasDisk(), "150px"); //$NON-NLS-1$

        disksTable.addColumn(new AbstractTextColumn<DiskImage>() {
            @Override
            public String getValue(DiskImage diskImage) {
                return diskImage.getDiskDescription();
            }
        }, constants.descriptionDisk(), "150px"); //$NON-NLS-1$
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

        model.getMemory().getEntityChangedEvent().addListener((ev, sender, args) -> updateMemoryWarning(model));
    }

    private void editDisksTable(final SnapshotModel model) {
        disksTable.asEditor().edit(model.getSnapshotDisks());
        model.getSnapshotDisks().getItemsChangedEvent().addListener((ev, sender, args) -> disksTable.selectAll());

        model.getSnapshotDisks().getSelectedItemsChangedEvent().addListener((ev, sender, args) -> updateMemoryWarning(model));
    }

    private void updateMemoryWarning(SnapshotModel model) {
        Collection<DiskImage> diskImages = model.getSnapshotDisks().getItems();
        List<DiskImage> selectedDiskImages = model.getSnapshotDisks().getSelectedItems();

        boolean partialDisksSelection = selectedDiskImages != null && diskImages.size() != selectedDiskImages.size();
        boolean includeMemory = model.getMemory().getEntity();

        SafeHtml warningImage = SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(
                resources.logWarningImage()).getHTML());

        HTML partialSnapshotWithMemoryWarningWidget = new HTML(templates.iconWithText(
                warningImage, constants.snapshotCreationWithMemoryAndPartialDisksWarning()));
        HTML memoryWarningWidget = new HTML(templates.iconWithText(
                warningImage, constants.snapshotCreationWithMemoryNotLiveWarning()));

        warningPanel.clear();

        // Show warning in case of saving memory to snapshot and excluding some disks.
        if (includeMemory && partialDisksSelection) {
            warningPanel.add(partialSnapshotWithMemoryWarningWidget);
        }

        // Show warning in case of saving memory since it is not really 'live'
        if (includeMemory) {
            warningPanel.add(memoryWarningWidget);
        }
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
        descriptionEditor.setFocus(true);
    }

    public void appendMessage(String message) {
        if (message == null) {
            return;
        }

        messagePanel.add(new Label(message));
    }

}

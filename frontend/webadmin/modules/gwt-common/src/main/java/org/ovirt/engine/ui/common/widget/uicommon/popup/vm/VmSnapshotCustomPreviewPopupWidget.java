package org.ovirt.engine.ui.common.widget.uicommon.popup.vm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.comparators.DiskByDiskAliasComparator;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationMessages;
import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.common.widget.table.cell.RadioboxCell;
import org.ovirt.engine.ui.common.widget.table.column.AbstractCheckboxColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractFullDateTimeColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.common.widget.table.header.ImageResourceHeader;
import org.ovirt.engine.ui.common.widget.table.header.SafeHtmlHeader;
import org.ovirt.engine.ui.common.widget.uicommon.popup.AbstractModelBoundPopupWidget;
import org.ovirt.engine.ui.common.widget.uicommon.vm.VmSnapshotInfoPanel;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.PreviewSnapshotModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.SnapshotModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.NoSelectionModel;

public class VmSnapshotCustomPreviewPopupWidget extends AbstractModelBoundPopupWidget<PreviewSnapshotModel> {

    interface Driver extends SimpleBeanEditorDriver<PreviewSnapshotModel, VmSnapshotCustomPreviewPopupWidget> {
    }

    interface ViewUiBinder extends UiBinder<SplitLayoutPanel, VmSnapshotCustomPreviewPopupWidget> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<VmSnapshotCustomPreviewPopupWidget> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @UiField
    @Ignore
    Label previewTableLabel;

    @UiField(provided = true)
    @Ignore
    EntityModelCellTable<ListModel> previewTable;

    @UiField(provided = true)
    SplitLayoutPanel splitLayoutPanel;

    @UiField
    SimplePanel snapshotInfoContainer;

    @UiField
    FlowPanel warningPanel;

    private PreviewSnapshotModel previewSnapshotModel;
    private VmSnapshotInfoPanel vmSnapshotInfoPanel;

    private static final CommonApplicationTemplates templates = AssetProvider.getTemplates();
    private static final CommonApplicationResources resources = AssetProvider.getResources();
    private static final CommonApplicationConstants constants = AssetProvider.getConstants();
    private static final CommonApplicationMessages messages = AssetProvider.getMessages();

    private final Driver driver = GWT.create(Driver.class);

    public VmSnapshotCustomPreviewPopupWidget() {
        initTables();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        localize();
        ViewIdHandler.idHandler.generateAndSetIds(this);
        driver.initialize(this);
    }

    private void initTables() {
        // Create custom preview table
        previewTable = new EntityModelCellTable<>(false, true);
        previewTable.enableColumnResizing();

        // Create Snapshot information tab panel
        vmSnapshotInfoPanel = new VmSnapshotInfoPanel();

        // Create split layout panel
        splitLayoutPanel = new SplitLayoutPanel(4);
    }

    private void createPreviewTable() {
        previewTable.addColumn(new AbstractFullDateTimeColumn<SnapshotModel>() {
            @Override
            protected Date getRawValue(SnapshotModel snapshotModel) {
                return snapshotModel.getEntity().getCreationDate();
            }
        }, constants.dateSnapshot(), "140px"); //$NON-NLS-1$

        previewTable.addColumn(new AbstractTextColumn<SnapshotModel>() {
            @Override
            public String getValue(SnapshotModel snapshotModel) {
                return snapshotModel.getEntity().getDescription();
            }
        }, constants.descriptionSnapshot(), "100px"); //$NON-NLS-1$
        previewTable.setSelectionModel(new NoSelectionModel());

        Column<SnapshotModel, Boolean> vmConfColumn = new Column<SnapshotModel, Boolean>(new RadioboxCell()) {
            @Override
            public Boolean getValue(SnapshotModel model) {
                Snapshot snapshotVmConf = model.getEntity();
                Snapshot toPreviewVmConf = previewSnapshotModel.getSnapshotModel().getEntity();
                if (snapshotVmConf == null && toPreviewVmConf == null) {
                    return true;
                }

                return snapshotVmConf != null && snapshotVmConf.equals(toPreviewVmConf);
            }
        };

        vmConfColumn.setFieldUpdater(new FieldUpdater<SnapshotModel, Boolean>() {
            @Override
            public void update(int index, SnapshotModel snapshotModel, Boolean value) {
                previewSnapshotModel.setSnapshotModel(snapshotModel);
                previewSnapshotModel.clearMemorySelection();
                updateWarnings();
                refreshTable(previewTable);

                if (snapshotModel.getVm() == null) {
                    snapshotModel.updateVmConfiguration(new INewAsyncCallback() {
                        @Override
                        public void onSuccess(Object model, Object returnValue) {
                            updateInfoPanel();
                        }
                    });
                }
                else {
                    updateInfoPanel();
                }
            }
        });

        previewTable.addColumn(vmConfColumn,
                new ImageResourceHeader(resources.vmConfIcon(), SafeHtmlUtils.fromTrustedString(constants.vmConfiguration())),
                "30px"); //$NON-NLS-1$

        AbstractCheckboxColumn<SnapshotModel> memoryColumn = new AbstractCheckboxColumn<SnapshotModel>(
                new FieldUpdater<SnapshotModel, Boolean>() {
                    @Override
                    public void update(int index, SnapshotModel snapshotModel, Boolean value) {
                        previewSnapshotModel.getSnapshotModel().getMemory().setEntity(value);
                        refreshTable(previewTable);
                        updateWarnings();
                    }
                }) {

            @Override
            public Boolean getValue(SnapshotModel snapshotModel) {
                return snapshotModel.getMemory().getEntity();
            }

            @Override
            protected boolean canEdit(SnapshotModel snapshotModel) {
                boolean containsMemory = !snapshotModel.getEntity().getMemoryVolume().isEmpty();
                SnapshotModel selectedSnapshotModel = previewSnapshotModel.getSnapshotModel();
                return containsMemory && snapshotModel == selectedSnapshotModel;
            }

            @Override
            public void render(Context context, SnapshotModel snapshotModel, SafeHtmlBuilder sb) {
                if (!snapshotModel.getEntity().getMemoryVolume().isEmpty()) {
                    super.render(context, snapshotModel, sb);
                }
                else {
                    sb.appendEscaped(constants.notAvailableLabel());
                }
            }
        };

        previewTable.addColumn(
                memoryColumn,
                templates.iconWithText(imageResourceToSafeHtml(resources.memorySmallIcon()), constants.memorySnapshot()),
                "100px"); //$NON-NLS-1$

        List<DiskImage> disks = previewSnapshotModel.getAllDisks();
        Collections.sort(disks, new DiskByDiskAliasComparator());

        for (final DiskImage disk : disks) {
            previewTable.addColumn(new AbstractCheckboxColumn<SnapshotModel>(new FieldUpdater<SnapshotModel, Boolean>() {
                @Override
                public void update(int index, SnapshotModel snapshotModel, Boolean value) {
                    ListModel diskListModel = previewSnapshotModel.getDiskSnapshotsMap().get(disk.getId());
                    DiskImage image = snapshotModel.getImageByDiskId(disk.getId());

                    diskListModel.setSelectedItem(Boolean.TRUE.equals(value) ? image : null);
                    refreshTable(previewTable);
                    updateWarnings();
                    updateInfoPanel();
                }
            }) {
                @Override
                public Boolean getValue(SnapshotModel snapshotModel) {
                    ListModel diskListModel = previewSnapshotModel.getDiskSnapshotsMap().get(disk.getId());
                    DiskImage image = snapshotModel.getImageByDiskId(disk.getId());

                    return image != null ? image.equals(diskListModel.getSelectedItem()) : false;
                }

                @Override
                protected boolean canEdit(SnapshotModel model) {
                    return true;
                }

                @Override
                public void render(Context context, SnapshotModel snapshotModel, SafeHtmlBuilder sb) {
                    DiskImage image = snapshotModel.getImageByDiskId(disk.getId());
                    if (image == null) {
                        sb.appendEscaped(constants.notAvailableLabel());
                    }
                    else if (image.getImageStatus() == ImageStatus.ILLEGAL) {
                        sb.append(templates.text(constants.notAvailableLabel()));
                    }
                    else {
                        super.render(context, snapshotModel, sb);
                    }
                }

                @Override
                public SafeHtml getTooltip(SnapshotModel model) {
                    if (disk != null && disk.getId() != null) {
                        DiskImage image = model.getImageByDiskId(disk.getId());
                        if (image != null && image.getImageStatus() == ImageStatus.ILLEGAL) {
                            return SafeHtmlUtils.fromSafeConstant(constants.illegalStatus());
                        }
                    }
                    return null;
                }
            },

            new SafeHtmlHeader(templates.iconWithText(imageResourceToSafeHtml(resources.diskIcon()), disk.getDiskAlias()),
                    SafeHtmlUtils.fromString(disk.getId().toString())),
                    "120px"); //$NON-NLS-1$

            // Edit preview table
            previewTable.asEditor().edit(previewSnapshotModel.getSnapshots());
        }

        previewTable.addCellPreviewHandler(new CellPreviewEvent.Handler<EntityModel>() {
            long lastClick = -1000;

            @Override
            public void onCellPreview(CellPreviewEvent<EntityModel> event) {
                NativeEvent nativeEvent = event.getNativeEvent();
                long clickAt = System.currentTimeMillis();

                if (BrowserEvents.CLICK.equals(nativeEvent.getType())) {
                    if (clickAt - lastClick < 300) { // double click: 2 clicks detected within 300 ms
                        SnapshotModel selectedSnapshotModel = (SnapshotModel) event.getValue();
                        previewSnapshotModel.clearSelection();
                        previewSnapshotModel.selectSnapshot(selectedSnapshotModel.getEntity().getId());
                        updateWarnings();
                        refreshTable(previewTable);
                    }
                    lastClick = System.currentTimeMillis();
                }
            }
        });
    }

    private void refreshTable(EntityModelCellTable table) {
        table.asEditor().edit(table.asEditor().flush());
        table.redraw();
    }

    private void updateWarnings() {
        List<DiskImage> selectedDisks = previewSnapshotModel.getSelectedDisks();
        List<DiskImage> disksOfSelectedSnapshot = previewSnapshotModel.getSnapshotModel().getEntity().getDiskImages();
        List<DiskImage> disksOfActiveSnapshot;
        if (previewSnapshotModel.getActiveSnapshotModel() != null) {
            disksOfActiveSnapshot = previewSnapshotModel.getActiveSnapshotModel().getEntity().getDiskImages();
        } else {
            disksOfActiveSnapshot = Collections.emptyList();
        }

        SnapshotModel selectedModel = previewSnapshotModel.getSnapshotModel();
        boolean includeAllDisksOfSnapshot = selectedDisks.containsAll(disksOfSelectedSnapshot);
        boolean includeMemory = selectedModel.getMemory().getEntity();
        boolean includeMemoryDifferentClusterVersion = includeMemory && !selectedModel.isVMWithMemoryCompatible();

        SafeHtml warningImage = SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(
                resources.logWarningImage()).getHTML());

        String warningText = ""; //$NON-NLS-1$
        if (includeMemoryDifferentClusterVersion) {
            // Show warning when snapshot with memory originates in different cluster version
            warningText += constants.snapshotPreviewWithMemoryFromDifferentClusterVersion();
        }

        if (!includeAllDisksOfSnapshot && includeMemory) {
            // Show warning in case of previewing a memory snapshot and excluding disks of the selected snapshot.
            warningText += constants.snapshotPreviewWithMemoryAndPartialDisksWarning();
        } else if (isDisksExcluded(disksOfActiveSnapshot, selectedDisks)) {
            // Show warning when excluding disks.
            warningText += constants.snapshotPreviewWithExcludedDisksWarning();
        }

        warningPanel.clear();
        if (!warningText.isEmpty()) {
            warningPanel.add(new HTML(templates.iconWithText(warningImage, warningText)));
            warningPanel.setVisible(true);
        } else {
            warningPanel.setVisible(false);
        }
    }

    // Search disks by ID (i.e. for each image, determines whether any image from the image-group is selected)
    private boolean isDisksExcluded(List<DiskImage> disks, List<DiskImage> selectedDisks) {
        for (DiskImage disk : disks) {
            if (!containsDisk(disk, selectedDisks)) {
                return true;
            }
        }
        return false;
    }

    // Check whether the specified disk list contains a disk by its ID (image-group)
    private boolean containsDisk(DiskImage snapshotDisk, List<DiskImage> disks) {
        for (DiskImage disk : disks) {
            if (disk.getId().equals(snapshotDisk.getId())) {
                return true;
            }
        }
        return false;
    }

    private void updateInfoPanel() {
        ArrayList<DiskImage> selectedImages = (ArrayList<DiskImage>) previewSnapshotModel.getSelectedDisks();
        Collections.sort(selectedImages, new DiskByDiskAliasComparator());
        SnapshotModel snapshotModel = previewSnapshotModel.getSnapshotModel();
        snapshotModel.setDisks(selectedImages);
        vmSnapshotInfoPanel.updateTabsData(snapshotModel);
    }

    void localize() {
        previewTableLabel.setText(constants.customPreviewSnapshotTableTitle());
    }

    @Override
    public void edit(PreviewSnapshotModel model) {
        driver.edit(model);
        previewSnapshotModel = model;
        snapshotInfoContainer.add(vmSnapshotInfoPanel);
        previewTable.asEditor().edit(previewSnapshotModel.getSnapshots());

        // Add selection listener
        model.getSnapshots().getSelectedItemChangedEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                ListModel snapshots = (ListModel) sender;
                SnapshotModel snapshotModel = (SnapshotModel) snapshots.getSelectedItem();
                if (snapshotModel != null) {
                    vmSnapshotInfoPanel.updatePanel(snapshotModel);
                }
            }
        });
        model.getSnapshots().getItemsChangedEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                createPreviewTable();
            }
        });
    }

    @Override
    public PreviewSnapshotModel flush() {
        previewTable.flush();
        return driver.flush();
    }

    private SafeHtml imageResourceToSafeHtml(ImageResource resource) {
        return SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(resource).getHTML());
    }
}

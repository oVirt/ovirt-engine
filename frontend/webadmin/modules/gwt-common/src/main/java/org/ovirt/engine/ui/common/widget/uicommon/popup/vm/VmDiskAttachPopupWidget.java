package org.ovirt.engine.ui.common.widget.uicommon.popup.vm;

import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskInterface;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.storage.LunDisk;
import org.ovirt.engine.core.common.utils.SizeConverter;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.RadioButtonPanel;
import org.ovirt.engine.ui.common.widget.ValidatedPanelWidget;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.label.NoItemsLabel;
import org.ovirt.engine.ui.common.widget.renderer.EnumRenderer;
import org.ovirt.engine.ui.common.widget.table.column.AbstractCheckboxColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractDiskSizeColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractImageResourceColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractListModelListBoxColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.common.widget.table.header.ImageResourceHeader;
import org.ovirt.engine.ui.common.widget.uicommon.popup.AbstractModelBoundPopupWidget;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.AttachDiskModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.DiskModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;

public class VmDiskAttachPopupWidget extends AbstractModelBoundPopupWidget<AttachDiskModel> {

    interface Driver extends UiCommonEditorDriver<AttachDiskModel, VmDiskAttachPopupWidget> {
    }

    interface ViewUiBinder extends UiBinder<FlowPanel, VmDiskAttachPopupWidget> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<VmDiskAttachPopupWidget> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private final Driver driver = GWT.create(Driver.class);

    @UiField(provided = true)
    @Path("isPlugged.entity")
    @WithElementId("isPlugged")
    EntityModelCheckBoxEditor isPluggedEditor;

    @UiField
    FlowPanel attachDiskContainer;

    @UiField
    ValidatedPanelWidget attachDiskPanel;

    @UiField
    RadioButtonPanel diskTypePanel;

    @Ignore
    @WithElementId
    EntityModelCellTable<ListModel> imageDiskTable;

    @Ignore
    @WithElementId
    EntityModelCellTable<ListModel> lunDiskTable;

    @Ignore
    @WithElementId
    EntityModelCellTable<ListModel> managedBlockDiskTable;

    @Ignore
    @UiField(provided = true)
    HTML messageLabel;

    @Ignore
    @UiField(provided = true)
    HTML warningLabel;

    private static final CommonApplicationTemplates templates = AssetProvider.getTemplates();

    private static final CommonApplicationResources resources = AssetProvider.getResources();

    private static final CommonApplicationConstants constants = AssetProvider.getConstants();

    boolean isNewLunDiskEnabled;

    public VmDiskAttachPopupWidget(boolean isLunDiskEnabled,
                                   boolean allowMultipleSelection) {
        this.isNewLunDiskEnabled = isLunDiskEnabled;
        initManualWidgets(allowMultipleSelection);
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initAttachPanelWidget();
        initDiskImagesTable();
        initLunDisksTable();
        initManagedBlockDisksTable();
        driver.initialize(this);
    }

    private void initManualWidgets(boolean allowMultipleSelection) {
        isPluggedEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        imageDiskTable = new EntityModelCellTable<>(allowMultipleSelection);
        lunDiskTable = new EntityModelCellTable<>(allowMultipleSelection);
        managedBlockDiskTable = new EntityModelCellTable<>(allowMultipleSelection);
        messageLabel = new HTML();
        warningLabel = new HTML();
    }

    private void initAttachPanelWidget() {
        // Create tables container
        FlowPanel panel = new FlowPanel();
        panel.add(imageDiskTable);
        panel.add(lunDiskTable);
        panel.add(managedBlockDiskTable);

        // Create ValidatedPanelWidget and add tables container
        attachDiskPanel.setWidget(panel);
    }

    private void commonInitDiskImageTable(EntityModelCellTable<ListModel> diskTable, boolean showActualSize) {
        diskTable.enableColumnResizing();

        AbstractTextColumn<EntityModel> aliasColumn = new AbstractTextColumn<EntityModel>() {
            @Override
            public String getValue(EntityModel object) {
                DiskImage diskImage = (DiskImage) ((DiskModel) object.getEntity()).getDisk();
                return diskImage.getDiskAlias();
            }
        };
        diskTable.addColumn(aliasColumn, constants.aliasVmDiskTable(), "85px"); //$NON-NLS-1$

        AbstractTextColumn<EntityModel> descriptionColumn = new AbstractTextColumn<EntityModel>() {
            @Override
            public String getValue(EntityModel object) {
                DiskImage diskImage = (DiskImage) ((DiskModel) object.getEntity()).getDisk();
                return diskImage.getDiskDescription();
            }
        };
        diskTable.addColumn(descriptionColumn, constants.descriptionVmDiskTable(), "85px"); //$NON-NLS-1$

        AbstractTextColumn<EntityModel> idColumn = new AbstractTextColumn<EntityModel>() {
            @Override
            public String getValue(EntityModel object) {
                DiskImage diskImage = (DiskImage) ((DiskModel) object.getEntity()).getDisk();
                return diskImage.getId().toString();
            }
        };
        diskTable.addColumn(idColumn, constants.idVmDiskTable(), "85px"); //$NON-NLS-1$

        AbstractDiskSizeColumn<EntityModel> sizeColumn = new AbstractDiskSizeColumn<EntityModel>() {
            @Override
            protected Long getRawValue(EntityModel object) {
                DiskImage diskImage = (DiskImage) ((DiskModel) object.getEntity()).getDisk();
                return diskImage.getSize();
            }
        };
        diskTable.addColumn(sizeColumn, constants.provisionedSizeVmDiskTable(), "100px"); //$NON-NLS-1$

        if (showActualSize) {
            AbstractDiskSizeColumn<EntityModel> actualSizeColumn = new AbstractDiskSizeColumn<EntityModel>() {
                @Override
                protected Long getRawValue(EntityModel object) {
                    DiskImage diskImage = (DiskImage) ((DiskModel) object.getEntity()).getDisk();
                    return diskImage.getActualSizeInBytes();
                }
            };
            diskTable.addColumn(actualSizeColumn, constants.sizeVmDiskTable(), "100px"); //$NON-NLS-1$
        }

        AbstractTextColumn<EntityModel> storageDomainColumn = new AbstractTextColumn<EntityModel>() {
            @Override
            public String getValue(EntityModel object) {
                DiskImage diskImage = (DiskImage) ((DiskModel) object.getEntity()).getDisk();
                return diskImage.getStoragesNames().get(0);
            }
        };
        diskTable.addColumn(storageDomainColumn, constants.storageDomainVmDiskTable(), "100px"); //$NON-NLS-1$

        diskTable.addColumn(getDiskInterfaceSelectionColumn(), constants.interfaceVmDiskPopup(), "115px"); //$NON-NLS-1$

        diskTable.addColumn(getReadOnlyCheckBoxColumn(),
                new ImageResourceHeader(resources.readOnlyDiskIcon(), SafeHtmlUtils.fromTrustedString(constants.readOnly())),
                "30px"); //$NON-NLS-1$

        diskTable.addColumn(getBootCheckBoxColumn(),
                new ImageResourceHeader(resources.bootableDiskIcon(), SafeHtmlUtils.fromTrustedString(constants.bootableDisk())),
                "30px"); //$NON-NLS-1$

        diskTable.addColumn(new AbstractImageResourceColumn<EntityModel>() {
            @Override
            public ImageResource getValue(EntityModel object) {
                Disk disk = ((DiskModel) object.getEntity()).getDisk();
                return disk.isShareable() ? resources.shareableDiskIcon() : null;
            }

            @Override
            public SafeHtml getTooltip(EntityModel object) {
                Disk disk = ((DiskModel) object.getEntity()).getDisk();
                if (disk.isShareable()) {
                    return SafeHtmlUtils.fromSafeConstant(constants.shareable());
                }
                return null;
            }
        }, new ImageResourceHeader(resources.shareableDiskIcon(), SafeHtmlUtils.fromTrustedString(constants.shareable())),
                "30px"); //$NON-NLS-1$

        diskTable.setWidth("100%"); // $NON-NLS-1$
        diskTable.setEmptyTableWidget(new NoItemsLabel());
    }

    private void initDiskImagesTable() {
        commonInitDiskImageTable(imageDiskTable, true);
    }

    private void initLunDisksTable() {
        lunDiskTable.enableColumnResizing();

        AbstractTextColumn<EntityModel> aliasColumn = new AbstractTextColumn<EntityModel>() {
            @Override
            public String getValue(EntityModel object) {
                LunDisk disk = (LunDisk) ((DiskModel) object.getEntity()).getDisk();
                return disk.getDiskAlias();
            }
        };
        lunDiskTable.addColumn(aliasColumn, constants.aliasVmDiskTable(), "60px"); //$NON-NLS-1$

        AbstractTextColumn<EntityModel> descriptionColumn = new AbstractTextColumn<EntityModel>() {
            @Override
            public String getValue(EntityModel object) {
                LunDisk disk = (LunDisk) ((DiskModel) object.getEntity()).getDisk();
                return disk.getDiskDescription();
            }
        };
        lunDiskTable.addColumn(descriptionColumn, constants.descriptionVmDiskTable(), "85px"); //$NON-NLS-1$

        AbstractTextColumn<EntityModel> lunIdColumn = new AbstractTextColumn<EntityModel>() {
            @Override
            public String getValue(EntityModel object) {
                LunDisk disk = (LunDisk) ((DiskModel) object.getEntity()).getDisk();
                return disk.getLun().getLUNId();
            }
        };
        lunDiskTable.addColumn(lunIdColumn, constants.lunIdSanStorage(), "60px"); //$NON-NLS-1$

        AbstractTextColumn<EntityModel> idColumn = new AbstractTextColumn<EntityModel>() {
            @Override
            public String getValue(EntityModel object) {
                LunDisk disk = (LunDisk) ((DiskModel) object.getEntity()).getDisk();
                return disk.getId().toString();
            }
        };
        lunDiskTable.addColumn(idColumn, constants.idVmDiskTable(), "60px"); //$NON-NLS-1$

        AbstractDiskSizeColumn<EntityModel> sizeColumn = new AbstractDiskSizeColumn<EntityModel>(SizeConverter.SizeUnit.GiB) {
            @Override
            protected Long getRawValue(EntityModel object) {
                LunDisk disk = (LunDisk) ((DiskModel) object.getEntity()).getDisk();
                return (long) disk.getLun().getDeviceSize();
            }
        };
        lunDiskTable.addColumn(sizeColumn, constants.devSizeSanStorage(), "70px"); //$NON-NLS-1$

        AbstractTextColumn<EntityModel> pathColumn = new AbstractTextColumn<EntityModel>() {
            @Override
            public String getValue(EntityModel object) {
                LunDisk disk = (LunDisk) ((DiskModel) object.getEntity()).getDisk();
                return String.valueOf(disk.getLun().getPathCount());
            }
        };
        lunDiskTable.addColumn(pathColumn, constants.pathSanStorage(), "40px"); //$NON-NLS-1$

        AbstractTextColumn<EntityModel> vendorIdColumn = new AbstractTextColumn<EntityModel>() {
            @Override
            public String getValue(EntityModel object) {
                LunDisk disk = (LunDisk) ((DiskModel) object.getEntity()).getDisk();
                return disk.getLun().getVendorId();
            }
        };
        lunDiskTable.addColumn(vendorIdColumn, constants.vendorIdSanStorage(), "70px"); //$NON-NLS-1$

        AbstractTextColumn<EntityModel> productIdColumn = new AbstractTextColumn<EntityModel>() {
            @Override
            public String getValue(EntityModel object) {
                LunDisk disk = (LunDisk) ((DiskModel) object.getEntity()).getDisk();
                return disk.getLun().getProductId();
            }
        };
        lunDiskTable.addColumn(productIdColumn, constants.productIdSanStorage(), "70px"); //$NON-NLS-1$

        AbstractTextColumn<EntityModel> serialColumn = new AbstractTextColumn<EntityModel>() {
            @Override
            public String getValue(EntityModel object) {
                LunDisk disk = (LunDisk) ((DiskModel) object.getEntity()).getDisk();
                return disk.getLun().getSerial();
            }
        };
        lunDiskTable.addColumn(serialColumn, constants.serialSanStorage(), "70px"); //$NON-NLS-1$

        lunDiskTable.addColumn(getDiskInterfaceSelectionColumn(), constants.interfaceVmDiskPopup(), "115px"); //$NON-NLS-1$

        lunDiskTable.addColumn(getReadOnlyCheckBoxColumn(),
                new ImageResourceHeader(resources.readOnlyDiskIcon(), SafeHtmlUtils.fromTrustedString(constants.readOnly())),
                "30px"); //$NON-NLS-1$

        lunDiskTable.addColumn(getBootCheckBoxColumn(),
                new ImageResourceHeader(resources.bootableDiskIcon(), SafeHtmlUtils.fromTrustedString(constants.bootableDisk())),
                "30px"); //$NON-NLS-1$


        lunDiskTable.addColumn(new AbstractImageResourceColumn<EntityModel>() {
            @Override
            public ImageResource getValue(EntityModel object) {
                Disk disk = ((DiskModel) object.getEntity()).getDisk();
                return disk.isShareable() ? resources.shareableDiskIcon() : null;
            }

            @Override
            public SafeHtml getTooltip(EntityModel object) {
                Disk disk = ((DiskModel) object.getEntity()).getDisk();
                if (disk.isShareable()) {
                    return SafeHtmlUtils.fromSafeConstant(constants.shareable());
                }
                return null;
            }
        }, new ImageResourceHeader(resources.shareableDiskIcon(), SafeHtmlUtils.fromTrustedString(constants.shareable())),
                "30px"); //$NON-NLS-1$

        lunDiskTable.setWidth("100%"); // $NON-NLS-1$
        lunDiskTable.setEmptyTableWidget(new NoItemsLabel());
    }

    private void initManagedBlockDisksTable() {
        commonInitDiskImageTable(managedBlockDiskTable, false);
    }

    @Override
    public void edit(final AttachDiskModel disk) {
        driver.edit(disk);

        if (!isNewLunDiskEnabled) {
            disk.getDiskStorageType().setEntity(DiskStorageType.IMAGE);
        }
        revealDiskPanel(disk);

        diskTypePanel.addRadioButton(
                constants.imageDisk(),
                disk.getIsNew() || disk.getDisk().getDiskStorageType() == DiskStorageType.IMAGE,
                disk.getIsNew(),
                event -> {
                    if (disk.getIsNew()) {
                        disk.getDiskStorageType().setEntity(DiskStorageType.IMAGE);
                        revealDiskPanel(disk);
                    }
                });

        diskTypePanel.addRadioButton(
                constants.directLunDisk(),
                !disk.getIsNew() && disk.getDisk().getDiskStorageType() == DiskStorageType.LUN,
                disk.getIsNew(),
                event -> {
                    if (disk.getIsNew()) {
                        disk.getDiskStorageType().setEntity(DiskStorageType.LUN);
                        revealDiskPanel(disk);
                    }
                });

        diskTypePanel.addRadioButton(
                constants.managedBlockDisk(),
                !disk.getIsNew() && disk.getDisk().getDiskStorageType() == DiskStorageType.MANAGED_BLOCK_STORAGE,
                disk.getIsNew(),
                event -> {
                    if (disk.getIsNew()) {
                        disk.getDiskStorageType().setEntity(DiskStorageType.MANAGED_BLOCK_STORAGE);
                        revealDiskPanel(disk);
                    }
                });

        // Add event handlers
        disk.getPropertyChangedEvent().addListener((ev, sender, args) -> {
            String propName = args.propertyName;
            if (propName.equals("IsValid")) { //$NON-NLS-1$
                if (disk.getIsValid()) {
                    attachDiskPanel.markAsValid();
                } else {
                    attachDiskPanel.markAsInvalid(disk.getInvalidityReasons());
                }
            } else if ("Message".equals(propName)) { //$NON-NLS-1$
                if (StringHelper.isNotNullOrEmpty(disk.getMessage())) {
                    messageLabel.setHTML(wrapInUnorderedList(disk.getMessage()));
                } else {
                    messageLabel.setHTML(""); //$NON-NLS-1$
                }
            }
        });

        disk.getWarningLabel().getPropertyChangedEvent().addListener((ev, sender, args) -> {
            EntityModel ownerModel = (EntityModel) sender;
            String propName = args.propertyName;

            if ("IsAvailable".equals(propName)) { //$NON-NLS-1$
                warningLabel.setVisible(ownerModel.getIsAvailable());
            }
        });

        disk.getWarningLabel().getEntityChangedEvent().addListener((ev, sender, args) -> warningLabel.setHTML(wrapInUnorderedList(disk.getWarningLabel().getEntity())));
        revealDiskPanel(disk);
    }

    private SafeHtml wrapInUnorderedList(String message) {
        SafeHtml listItem = templates.listItem(message);
        return templates.unorderedList(listItem);
    }

    private void revealDiskPanel(final AttachDiskModel disk) {
        diskTypePanel.setVisible(isNewLunDiskEnabled);
        imageDiskTable.setVisible(false);
        lunDiskTable.setVisible(false);
        managedBlockDiskTable.setVisible(false);

        EntityModelCellTable<ListModel> diskTable;
        switch (disk.getDiskStorageType().getEntity()) {
            case LUN:
                diskTable = lunDiskTable;
                break;
            case MANAGED_BLOCK_STORAGE:
                diskTable = managedBlockDiskTable;
                break;
            case IMAGE:
            default:
                diskTable = imageDiskTable;
                break;
        }

        diskTable.setVisible(true);
        diskTable.asEditor().edit(disk.getAttachableDisksMap().get(disk.getDiskStorageType().getEntity()));
    }

    @Override
    public AttachDiskModel flush() {
        return driver.flush();
    }

    @Override
    public void cleanup() {
        driver.cleanup();
    }

    @Override
    public int setTabIndexes(int nextTabIndex) {
        isPluggedEditor.setTabIndex(nextTabIndex++);
        return nextTabIndex;
    }

    private AbstractCheckboxColumn<EntityModel> getReadOnlyCheckBoxColumn() {
        AbstractCheckboxColumn<EntityModel> readOnlyCheckboxColumn = new AbstractCheckboxColumn<EntityModel>(
                (idx, object, value) -> {
                    DiskModel diskModel = (DiskModel) object.getEntity();
                    diskModel.setReadOnly(value);
                }) {
                @Override
                protected boolean canEdit(EntityModel object) {
                    DiskModel diskModel = (DiskModel) object.getEntity();
                    Disk disk = diskModel.getDisk();
                    boolean isScsiPassthrough = disk.isScsiPassthrough();
                    boolean ideLimitation = diskModel.getDiskInterface().getSelectedItem() == DiskInterface.IDE;
                    boolean sataLimitation = diskModel.getDiskInterface().getSelectedItem() == DiskInterface.SATA;
                    return !isScsiPassthrough && !ideLimitation && !sataLimitation;
                }

                @Override
                public Boolean getValue(EntityModel object) {
                    DiskModel diskModel = (DiskModel) object.getEntity();
                    return diskModel.isReadOnly();
                }
            };
        return readOnlyCheckboxColumn;
    }

    private AbstractCheckboxColumn<EntityModel> getBootCheckBoxColumn() {
        AbstractCheckboxColumn<EntityModel> bootCheckboxColumn = new AbstractCheckboxColumn<EntityModel>(
                (idx, object, value) -> {
                    DiskModel diskModel = (DiskModel) object.getEntity();
                    diskModel.getIsBootable().setEntity(value);
                }) {
                @Override
                public SafeHtml getTooltip(EntityModel object) {
                    EntityModel<Boolean> bootModel = ((DiskModel) object.getEntity()).getIsBootable();
                    return bootModel.getChangeProhibitionReason() == null ? null :
                            SafeHtmlUtils.fromString(bootModel.getChangeProhibitionReason());
                }

                @Override
                protected boolean canEdit(EntityModel object) {
                    EntityModel<Boolean> bootModel = ((DiskModel) object.getEntity()).getIsBootable();
                    return  bootModel.getIsChangable() || bootModel.getEntity();
                }

                @Override
                public Boolean getValue(EntityModel object) {
                    DiskModel diskModel = (DiskModel) object.getEntity();
                    return diskModel.getIsBootable().getEntity();
                }
            };
        return bootCheckboxColumn;
    }


    private Column getDiskInterfaceSelectionColumn() {
        AbstractListModelListBoxColumn diskInterfaceStringColumn =
                new AbstractListModelListBoxColumn<EntityModel, DiskInterface>(new EnumRenderer<DiskInterface>()) {
            @Override
            public ListModel getValue(EntityModel object) {
                return ((DiskModel) object.getEntity()).getDiskInterface();
            }
        };
        return diskInterfaceStringColumn;
    }
}

package org.ovirt.engine.ui.common.widget.uicommon.popup.vm;

import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.Disk.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskInterface;
import org.ovirt.engine.core.common.businessentities.LunDisk;
import org.ovirt.engine.core.common.utils.SizeConverter;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.RadioButtonsHorizontalPanel;
import org.ovirt.engine.ui.common.widget.ValidatedPanelWidget;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.table.column.DiskSizeColumn;
import org.ovirt.engine.ui.common.widget.table.column.EnumColumn;
import org.ovirt.engine.ui.common.widget.table.column.ImageResourceColumn;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.common.widget.uicommon.disks.DisksViewColumns;
import org.ovirt.engine.ui.common.widget.uicommon.popup.AbstractModelBoundPopupWidget;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.AttachDiskModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.DiskModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

public class VmDiskAttachPopupWidget extends AbstractModelBoundPopupWidget<AttachDiskModel> {

    interface Driver extends SimpleBeanEditorDriver<AttachDiskModel, VmDiskAttachPopupWidget> {
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
    VerticalPanel attachDiskContainer;

    @UiField
    ValidatedPanelWidget attachDiskPanel;

    @UiField
    RadioButtonsHorizontalPanel diskTypePanel;

    @Ignore
    @WithElementId
    EntityModelCellTable<ListModel> imageDiskTable;

    @Ignore
    @WithElementId
    EntityModelCellTable<ListModel> lunDiskTable;

    @UiField
    Label message;

    @UiField
    CommonApplicationConstants constants;

    boolean isNewLunDiskEnabled;

    public VmDiskAttachPopupWidget(CommonApplicationConstants constants,
                                   CommonApplicationResources resources,
                                   CommonApplicationTemplates templates,
                                   boolean isLunDiskEnabled) {
        this.isNewLunDiskEnabled = isLunDiskEnabled;
        initManualWidgets();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        localize(constants);
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initAttachPanelWidget();
        initInternalDiskTable(constants, resources, templates);
        initExternalDiskTable(constants, resources, templates);
        driver.initialize(this);
    }

    private void localize(CommonApplicationConstants constants) {
        isPluggedEditor.setLabel(constants.activateVmDiskPopup());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void initManualWidgets() {
        isPluggedEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        imageDiskTable = new EntityModelCellTable<ListModel>(true);
        lunDiskTable = new EntityModelCellTable<ListModel>(true);
    }

    private void initAttachPanelWidget() {
        // Create tables container
        VerticalPanel verticalPanel = new VerticalPanel();
        verticalPanel.add(imageDiskTable);
        verticalPanel.add(lunDiskTable);

        // Create ValidatedPanelWidget and add tables container
        attachDiskPanel.setWidget(verticalPanel);
    }

    private void initInternalDiskTable(final CommonApplicationConstants constants,
            final CommonApplicationResources resources,
            final CommonApplicationTemplates templates) {
        imageDiskTable.enableColumnResizing();

        TextColumnWithTooltip<EntityModel> aliasColumn = new TextColumnWithTooltip<EntityModel>() {
            @Override
            public String getValue(EntityModel object) {
                DiskImage diskImage = (DiskImage) (((DiskModel) (object.getEntity())).getDisk());
                return diskImage.getDiskAlias();
            }
        };
        imageDiskTable.addColumn(aliasColumn, constants.aliasVmDiskTable(), "85px"); //$NON-NLS-1$

        TextColumnWithTooltip<EntityModel> descriptionColumn = new TextColumnWithTooltip<EntityModel>() {
            @Override
            public String getValue(EntityModel object) {
                DiskImage diskImage = (DiskImage) (((DiskModel) (object.getEntity())).getDisk());
                return diskImage.getDiskDescription();
            }
        };
        imageDiskTable.addColumn(descriptionColumn, constants.descriptionVmDiskTable(), "85px"); //$NON-NLS-1$

        TextColumnWithTooltip<EntityModel> idColumn = new TextColumnWithTooltip<EntityModel>() {
            @Override
            public String getValue(EntityModel object) {
                DiskImage diskImage = (DiskImage) (((DiskModel) (object.getEntity())).getDisk());
                return diskImage.getId().toString();
            }
        };
        imageDiskTable.addColumn(idColumn, constants.idVmDiskTable(), "85px"); //$NON-NLS-1$

        DiskSizeColumn<EntityModel> sizeColumn = new DiskSizeColumn<EntityModel>() {
            @Override
            protected Long getRawValue(EntityModel object) {
                DiskImage diskImage = (DiskImage) (((DiskModel) (object.getEntity())).getDisk());
                return diskImage.getSize();
            }
        };
        imageDiskTable.addColumn(sizeColumn, constants.provisionedSizeVmDiskTable(), "105px"); //$NON-NLS-1$

        DiskSizeColumn<EntityModel> actualSizeColumn = new DiskSizeColumn<EntityModel>() {
            @Override
            protected Long getRawValue(EntityModel object) {
                DiskImage diskImage = (DiskImage) (((DiskModel) (object.getEntity())).getDisk());
                return diskImage.getActualSizeInBytes();
            }
        };
        imageDiskTable.addColumn(actualSizeColumn, constants.sizeVmDiskTable(), "105px"); //$NON-NLS-1$

        TextColumnWithTooltip<EntityModel> storageDomainColumn = new TextColumnWithTooltip<EntityModel>() {
            @Override
            public String getValue(EntityModel object) {
                DiskImage diskImage = (DiskImage) (((DiskModel) (object.getEntity())).getDisk());
                return diskImage.getStoragesNames().get(0);
            }
        };
        imageDiskTable.addColumn(storageDomainColumn, constants.storageDomainVmDiskTable(), "115px"); //$NON-NLS-1$

        TextColumnWithTooltip<EntityModel> interfaceColumn = new EnumColumn<EntityModel, DiskInterface>() {
            @Override
            protected DiskInterface getRawValue(EntityModel object) {
                Disk disk = (((DiskModel) (object.getEntity())).getDisk());
                return disk.getDiskInterface();
            }
        };
        imageDiskTable.addColumn(interfaceColumn, constants.interfaceVmDiskPopup(), "95px"); //$NON-NLS-1$

        SafeHtml readOnlyColumnHeader = templates.imageWithTitle(SafeHtmlUtils.fromTrustedString(
                AbstractImagePrototype.create(resources.readOnlyDiskIcon()).getHTML()), constants.readOnly()
        );
        imageDiskTable.addColumn(DisksViewColumns.readOnlyCheckboxColumn, readOnlyColumnHeader, "30px"); //$NON-NLS-1$

        SafeHtml bootableColumnHeader = templates.imageWithTitle(SafeHtmlUtils.fromTrustedString(
                AbstractImagePrototype.create(resources.bootableDiskIcon()).getHTML()), constants.bootable()
        );
        imageDiskTable.addColumn(new ImageResourceColumn<EntityModel>() {
            @Override
            public ImageResource getValue(EntityModel object) {
                Disk disk = (((DiskModel) (object.getEntity())).getDisk());
                setTitle(disk.isBoot() ? constants.bootableDisk() : null);
                return disk.isBoot() ? resources.bootableDiskIcon() : null;
            }
        }, bootableColumnHeader, "30px"); //$NON-NLS-1$

        SafeHtml shareableColumnHeader = templates.imageWithTitle(SafeHtmlUtils.fromTrustedString(
                AbstractImagePrototype.create(resources.shareableDiskIcon()).getHTML()), constants.shareable()
        );
        imageDiskTable.addColumn(new ImageResourceColumn<EntityModel>() {
            @Override
            public ImageResource getValue(EntityModel object) {
                Disk disk = (((DiskModel) (object.getEntity())).getDisk());
                setTitle(disk.isShareable() ? constants.shareable() : null);
                return disk.isShareable() ? resources.shareableDiskIcon() : null;
            }
        }, shareableColumnHeader, "30px"); //$NON-NLS-1$

        imageDiskTable.setWidth("100%", true); //$NON-NLS-1$
        imageDiskTable.setHeight("100%"); //$NON-NLS-1$
    }

    private void initExternalDiskTable(final CommonApplicationConstants constants,
            final CommonApplicationResources resources,
            final CommonApplicationTemplates templates) {
        lunDiskTable.enableColumnResizing();

        TextColumnWithTooltip<EntityModel> aliasColumn = new TextColumnWithTooltip<EntityModel>() {
            @Override
            public String getValue(EntityModel object) {
                LunDisk disk = (LunDisk) (((DiskModel) (object.getEntity())).getDisk());
                return disk.getDiskAlias();
            }
        };
        lunDiskTable.addColumn(aliasColumn, constants.aliasVmDiskTable(), "60px"); //$NON-NLS-1$

        TextColumnWithTooltip<EntityModel> descriptionColumn = new TextColumnWithTooltip<EntityModel>() {
            @Override
            public String getValue(EntityModel object) {
                LunDisk disk = (LunDisk) (((DiskModel) (object.getEntity())).getDisk());
                return disk.getDiskDescription();
            }
        };
        lunDiskTable.addColumn(descriptionColumn, constants.descriptionVmDiskTable(), "85px"); //$NON-NLS-1$

        TextColumnWithTooltip<EntityModel> lunIdColumn = new TextColumnWithTooltip<EntityModel>() {
            @Override
            public String getValue(EntityModel object) {
                LunDisk disk = (LunDisk) (((DiskModel) (object.getEntity())).getDisk());
                return disk.getLun().getLUN_id();
            }
        };
        lunDiskTable.addColumn(lunIdColumn, constants.lunIdSanStorage(), "60px"); //$NON-NLS-1$

        TextColumnWithTooltip<EntityModel> idColumn = new TextColumnWithTooltip<EntityModel>() {
            @Override
            public String getValue(EntityModel object) {
                LunDisk disk = (LunDisk) (((DiskModel) (object.getEntity())).getDisk());
                return disk.getId().toString();
            }
        };
        lunDiskTable.addColumn(idColumn, constants.idVmDiskTable(), "60px"); //$NON-NLS-1$

        DiskSizeColumn<EntityModel> sizeColumn = new DiskSizeColumn<EntityModel>(SizeConverter.SizeUnit.GB) {
            @Override
            protected Long getRawValue(EntityModel object) {
                LunDisk disk = (LunDisk) (((DiskModel) (object.getEntity())).getDisk());
                return (long) disk.getLun().getDeviceSize();
            }
        };
        lunDiskTable.addColumn(sizeColumn, constants.devSizeSanStorage(), "70px"); //$NON-NLS-1$

        TextColumnWithTooltip<EntityModel> pathColumn = new TextColumnWithTooltip<EntityModel>() {
            @Override
            public String getValue(EntityModel object) {
                LunDisk disk = (LunDisk) (((DiskModel) (object.getEntity())).getDisk());
                return String.valueOf(disk.getLun().getPathCount());
            }
        };
        lunDiskTable.addColumn(pathColumn, constants.pathSanStorage(), "40px"); //$NON-NLS-1$

        TextColumnWithTooltip<EntityModel> vendorIdColumn = new TextColumnWithTooltip<EntityModel>() {
            @Override
            public String getValue(EntityModel object) {
                LunDisk disk = (LunDisk) (((DiskModel) (object.getEntity())).getDisk());
                return disk.getLun().getVendorId();
            }
        };
        lunDiskTable.addColumn(vendorIdColumn, constants.vendorIdSanStorage(), "70px"); //$NON-NLS-1$

        TextColumnWithTooltip<EntityModel> productIdColumn = new TextColumnWithTooltip<EntityModel>() {
            @Override
            public String getValue(EntityModel object) {
                LunDisk disk = (LunDisk) (((DiskModel) (object.getEntity())).getDisk());
                return disk.getLun().getProductId();
            }
        };
        lunDiskTable.addColumn(productIdColumn, constants.productIdSanStorage(), "70px"); //$NON-NLS-1$

        TextColumnWithTooltip<EntityModel> serialColumn = new TextColumnWithTooltip<EntityModel>() {
            @Override
            public String getValue(EntityModel object) {
                LunDisk disk = (LunDisk) (((DiskModel) (object.getEntity())).getDisk());
                return disk.getLun().getSerial();
            }
        };
        lunDiskTable.addColumn(serialColumn, constants.serialSanStorage(), "70px"); //$NON-NLS-1$

        TextColumnWithTooltip<EntityModel> interfaceColumn = new EnumColumn<EntityModel, DiskInterface>() {
            @Override
            protected DiskInterface getRawValue(EntityModel object) {
                Disk disk = (((DiskModel) (object.getEntity())).getDisk());
                return disk.getDiskInterface();
            }
        };
        lunDiskTable.addColumn(interfaceColumn, constants.interfaceVmDiskPopup(), "90px"); //$NON-NLS-1$

        SafeHtml readOnlyColumnHeader = templates.imageWithTitle(SafeHtmlUtils.fromTrustedString(
                AbstractImagePrototype.create(resources.readOnlyDiskIcon()).getHTML()), constants.readOnly()
        );
        lunDiskTable.addColumn(DisksViewColumns.readOnlyCheckboxColumn, readOnlyColumnHeader, "30px"); //$NON-NLS-1$

        SafeHtml bootableColumnHeader = templates.imageWithTitle(SafeHtmlUtils.fromTrustedString(
                AbstractImagePrototype.create(resources.bootableDiskIcon()).getHTML()), constants.bootable()
        );
        lunDiskTable.addColumn(new ImageResourceColumn<EntityModel>() {
            @Override
            public ImageResource getValue(EntityModel object) {
                Disk disk = (((DiskModel) (object.getEntity())).getDisk());
                setTitle(disk.isBoot() ? constants.bootableDisk() : null);
                return disk.isBoot() ? resources.bootableDiskIcon() : null;
            }
        }, bootableColumnHeader, "30px"); //$NON-NLS-1$

        SafeHtml shareableColumnHeader = templates.imageWithTitle(SafeHtmlUtils.fromTrustedString(
                AbstractImagePrototype.create(resources.shareableDiskIcon()).getHTML()), constants.shareable()
        );
        lunDiskTable.addColumn(new ImageResourceColumn<EntityModel>() {
            @Override
            public ImageResource getValue(EntityModel object) {
                Disk disk = (((DiskModel) (object.getEntity())).getDisk());
                setTitle(disk.isShareable() ? constants.shareable() : null);
                return disk.isShareable() ? resources.shareableDiskIcon() : null;
            }
        }, shareableColumnHeader, "30px"); //$NON-NLS-1$

        lunDiskTable.setWidth("100%", true); //$NON-NLS-1$
        lunDiskTable.setHeight("100%"); //$NON-NLS-1$
    }

    @Override
    public void edit(final AttachDiskModel disk) {
        driver.edit(disk);

        if (!isNewLunDiskEnabled) {
            disk.getDiskStorageType().setEntity(DiskStorageType.IMAGE);
        }
        revealDiskPanel(disk);

        diskTypePanel.addRadioButton(
                constants.internalDisk(),
                disk.getIsNew() || disk.getDisk().getDiskStorageType() == DiskStorageType.IMAGE,
                disk.getIsNew(),
                new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        disk.getDiskStorageType().setEntity(DiskStorageType.IMAGE);
                        revealDiskPanel(disk);
                    }
                });

        diskTypePanel.addRadioButton(
                constants.externalDisk(),
                !disk.getIsNew() && disk.getDisk().getDiskStorageType() == DiskStorageType.LUN,
                disk.getIsNew(),
                new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        disk.getDiskStorageType().setEntity(DiskStorageType.LUN);
                        revealDiskPanel(disk);
                    }
                });

        // Add event handlers
        disk.getPropertyChangedEvent().addListener(new IEventListener<PropertyChangedEventArgs>() {
            @Override
            public void eventRaised(Event<PropertyChangedEventArgs> ev, Object sender, PropertyChangedEventArgs args) {
                String propName = args.propertyName;
                if (propName.equals("IsValid")) { //$NON-NLS-1$
                    if (disk.getIsValid()) {
                        attachDiskPanel.markAsValid();
                    } else {
                        attachDiskPanel.markAsInvalid(disk.getInvalidityReasons());
                    }
                }
            }
        });

        revealDiskPanel(disk);
    }

    private void revealDiskPanel(final AttachDiskModel disk) {
        diskTypePanel.setVisible(isNewLunDiskEnabled);
        imageDiskTable.setVisible(false);
        lunDiskTable.setVisible(false);

        EntityModelCellTable<ListModel> diskTable;
        switch (disk.getDiskStorageType().getEntity()) {
            case LUN:
                diskTable = lunDiskTable;
                break;
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
    public int setTabIndexes(int nextTabIndex) {
        isPluggedEditor.setTabIndex(nextTabIndex++);
        return nextTabIndex;
    }
}

package org.ovirt.engine.ui.common.widget.uicommon.popup.vm;

import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskInterface;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.storage.LunDisk;
import org.ovirt.engine.core.common.utils.SizeConverter;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.RadioButtonsHorizontalPanel;
import org.ovirt.engine.ui.common.widget.ValidatedPanelWidget;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelCheckBoxEditor;
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
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicompat.external.StringUtils;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
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
    FlowPanel attachDiskContainer;

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

    @Ignore
    @WithElementId
    EntityModelCellTable<ListModel> cinderDiskTable;

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
        initCinderDisksTable();
        driver.initialize(this);
    }

    private void initManualWidgets(boolean allowMultipleSelection) {
        isPluggedEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        imageDiskTable = new EntityModelCellTable<>(allowMultipleSelection);
        lunDiskTable = new EntityModelCellTable<>(allowMultipleSelection);
        cinderDiskTable = new EntityModelCellTable<>(allowMultipleSelection);
        messageLabel = new HTML();
        warningLabel = new HTML();
    }

    private void initAttachPanelWidget() {
        // Create tables container
        VerticalPanel verticalPanel = new VerticalPanel();
        verticalPanel.add(imageDiskTable);
        verticalPanel.add(lunDiskTable);
        verticalPanel.add(cinderDiskTable);

        // Create ValidatedPanelWidget and add tables container
        attachDiskPanel.setWidget(verticalPanel);
    }

    private void initDiskImagesTable() {
        imageDiskTable.enableColumnResizing();

        AbstractTextColumn<EntityModel> aliasColumn = new AbstractTextColumn<EntityModel>() {
            @Override
            public String getValue(EntityModel object) {
                DiskImage diskImage = (DiskImage) ((DiskModel) object.getEntity()).getDisk();
                return diskImage.getDiskAlias();
            }
        };
        imageDiskTable.addColumn(aliasColumn, constants.aliasVmDiskTable(), "85px"); //$NON-NLS-1$

        AbstractTextColumn<EntityModel> descriptionColumn = new AbstractTextColumn<EntityModel>() {
            @Override
            public String getValue(EntityModel object) {
                DiskImage diskImage = (DiskImage) ((DiskModel) object.getEntity()).getDisk();
                return diskImage.getDiskDescription();
            }
        };
        imageDiskTable.addColumn(descriptionColumn, constants.descriptionVmDiskTable(), "85px"); //$NON-NLS-1$

        AbstractTextColumn<EntityModel> idColumn = new AbstractTextColumn<EntityModel>() {
            @Override
            public String getValue(EntityModel object) {
                DiskImage diskImage = (DiskImage) ((DiskModel) object.getEntity()).getDisk();
                return diskImage.getId().toString();
            }
        };
        imageDiskTable.addColumn(idColumn, constants.idVmDiskTable(), "85px"); //$NON-NLS-1$

        AbstractDiskSizeColumn<EntityModel> sizeColumn = new AbstractDiskSizeColumn<EntityModel>() {
            @Override
            protected Long getRawValue(EntityModel object) {
                DiskImage diskImage = (DiskImage) ((DiskModel) object.getEntity()).getDisk();
                return diskImage.getSize();
            }
        };
        imageDiskTable.addColumn(sizeColumn, constants.provisionedSizeVmDiskTable(), "100px"); //$NON-NLS-1$

        AbstractDiskSizeColumn<EntityModel> actualSizeColumn = new AbstractDiskSizeColumn<EntityModel>() {
            @Override
            protected Long getRawValue(EntityModel object) {
                DiskImage diskImage = (DiskImage) ((DiskModel) object.getEntity()).getDisk();
                return diskImage.getActualSizeInBytes();
            }
        };
        imageDiskTable.addColumn(actualSizeColumn, constants.sizeVmDiskTable(), "100px"); //$NON-NLS-1$

        AbstractTextColumn<EntityModel> storageDomainColumn = new AbstractTextColumn<EntityModel>() {
            @Override
            public String getValue(EntityModel object) {
                DiskImage diskImage = (DiskImage) ((DiskModel) object.getEntity()).getDisk();
                return diskImage.getStoragesNames().get(0);
            }
        };
        imageDiskTable.addColumn(storageDomainColumn, constants.storageDomainVmDiskTable(), "100px"); //$NON-NLS-1$

        imageDiskTable.addColumn(getDiskInterfaceSelectionColumn(), constants.interfaceVmDiskPopup(), "110px"); //$NON-NLS-1$

        imageDiskTable.addColumn(getReadOnlyCheckBoxColumn(),
                new ImageResourceHeader(resources.readOnlyDiskIcon(), SafeHtmlUtils.fromTrustedString(constants.readOnly())),
                "30px"); //$NON-NLS-1$

        imageDiskTable.addColumn(getBootCheckBoxColumn(),
                new ImageResourceHeader(resources.bootableDiskIcon(), SafeHtmlUtils.fromTrustedString(constants.bootableDisk())),
                "30px"); //$NON-NLS-1$

        imageDiskTable.addColumn(new AbstractImageResourceColumn<EntityModel>() {
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

        imageDiskTable.setWidth("100%", true); //$NON-NLS-1$
        imageDiskTable.setHeight("100%"); //$NON-NLS-1$
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

        lunDiskTable.addColumn(getDiskInterfaceSelectionColumn(), constants.interfaceVmDiskPopup(), "110px"); //$NON-NLS-1$

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

        lunDiskTable.setWidth("100%", true); //$NON-NLS-1$
        lunDiskTable.setHeight("100%"); //$NON-NLS-1$
    }

    private void initCinderDisksTable() {
        cinderDiskTable.enableColumnResizing();

        AbstractTextColumn<EntityModel> aliasColumn = new AbstractTextColumn<EntityModel>() {
            @Override
            public String getValue(EntityModel object) {
                CinderDisk disk = (CinderDisk) ((DiskModel) object.getEntity()).getDisk();
                return disk.getDiskAlias();
            }
        };
        cinderDiskTable.addColumn(aliasColumn, constants.aliasVmDiskTable(), "100px"); //$NON-NLS-1$

        AbstractTextColumn<EntityModel> descriptionColumn = new AbstractTextColumn<EntityModel>() {
            @Override
            public String getValue(EntityModel object) {
                CinderDisk disk = (CinderDisk) ((DiskModel) object.getEntity()).getDisk();
                return disk.getDiskDescription();
            }
        };
        cinderDiskTable.addColumn(descriptionColumn, constants.descriptionVmDiskTable(), "100px"); //$NON-NLS-1$

        AbstractDiskSizeColumn<EntityModel> sizeColumn = new AbstractDiskSizeColumn<EntityModel>(SizeConverter.SizeUnit.GiB) {
            @Override
            protected Long getRawValue(EntityModel object) {
                CinderDisk disk = (CinderDisk) ((DiskModel) object.getEntity()).getDisk();
                return disk.getSizeInGigabytes();
            }
        };
        cinderDiskTable.addColumn(sizeColumn, constants.provisionedSizeVmDiskTable(), "100px"); //$NON-NLS-1$

        cinderDiskTable.addColumn(getDiskInterfaceSelectionColumn(), constants.interfaceVmDiskPopup(), "110px"); //$NON-NLS-1$

        AbstractTextColumn<EntityModel> cinderVolumeTypeColumn = new AbstractTextColumn<EntityModel>() {
            @Override
            public String getValue(EntityModel object) {
                Disk disk = ((DiskModel) object.getEntity()).getDisk();
                return disk.getCinderVolumeType();
            }
        };
        cinderDiskTable.addColumn(cinderVolumeTypeColumn, constants.cinderVolumeTypeDisk(), "90px"); //$NON-NLS-1$

        cinderDiskTable.addColumn(getReadOnlyCheckBoxColumn(),
                new ImageResourceHeader(resources.readOnlyDiskIcon(), SafeHtmlUtils.fromTrustedString(constants.readOnly())),
                "30px"); //$NON-NLS-1$

        cinderDiskTable.addColumn(getBootCheckBoxColumn(),
                new ImageResourceHeader(resources.bootableDiskIcon(), SafeHtmlUtils.fromTrustedString(constants.bootableDisk())),
                "30px"); //$NON-NLS-1$

        cinderDiskTable.addColumn(new AbstractImageResourceColumn<EntityModel>() {
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

        cinderDiskTable.setWidth("100%", true); //$NON-NLS-1$
        cinderDiskTable.setHeight("100%"); //$NON-NLS-1$
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
                new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        disk.getDiskStorageType().setEntity(DiskStorageType.IMAGE);
                        revealDiskPanel(disk);
                    }
                });

        diskTypePanel.addRadioButton(
                constants.directLunDisk(),
                !disk.getIsNew() && disk.getDisk().getDiskStorageType() == DiskStorageType.LUN,
                disk.getIsNew(),
                new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        disk.getDiskStorageType().setEntity(DiskStorageType.LUN);
                        revealDiskPanel(disk);
                    }
                });

        diskTypePanel.addRadioButton(
                constants.cinderDisk(),
                !disk.getIsNew() && disk.getDisk().getDiskStorageType() == DiskStorageType.CINDER,
                disk.getIsNew(),
                new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        disk.getDiskStorageType().setEntity(DiskStorageType.CINDER);
                        revealDiskPanel(disk);
                    }
                });

        // Add event handlers
        disk.getPropertyChangedEvent().addListener(new IEventListener<PropertyChangedEventArgs>() {
            @Override
            public void eventRaised(Event<? extends PropertyChangedEventArgs> ev, Object sender, PropertyChangedEventArgs args) {
                String propName = args.propertyName;
                if (propName.equals("IsValid")) { //$NON-NLS-1$
                    if (disk.getIsValid()) {
                        attachDiskPanel.markAsValid();
                    } else {
                        attachDiskPanel.markAsInvalid(disk.getInvalidityReasons());
                    }
                } else if ("Message".equals(propName)) { //$NON-NLS-1$
                    if (StringUtils.isNotEmpty(disk.getMessage())) {
                        messageLabel.setHTML(wrapInUnorderedList(disk.getMessage()));
                    } else {
                        messageLabel.setHTML(""); //$NON-NLS-1$
                    }
                }
            }
        });

        disk.getWarningLabel().getPropertyChangedEvent().addListener(new IEventListener<PropertyChangedEventArgs>() {
            @Override
            public void eventRaised(Event<? extends PropertyChangedEventArgs> ev,
                    Object sender,
                    PropertyChangedEventArgs args) {
                EntityModel ownerModel = (EntityModel) sender;
                String propName = args.propertyName;

                if ("IsAvailable".equals(propName)) { //$NON-NLS-1$
                    warningLabel.setVisible(ownerModel.getIsAvailable());
                }
            }
        });

        disk.getWarningLabel().getEntityChangedEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                warningLabel.setHTML(wrapInUnorderedList(disk.getWarningLabel().getEntity()));
            }
        });
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
        cinderDiskTable.setVisible(false);

        EntityModelCellTable<ListModel> diskTable;
        switch (disk.getDiskStorageType().getEntity()) {
            case LUN:
                diskTable = lunDiskTable;
                break;
            case CINDER:
                diskTable = cinderDiskTable;
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
    public int setTabIndexes(int nextTabIndex) {
        isPluggedEditor.setTabIndex(nextTabIndex++);
        return nextTabIndex;
    }

    private AbstractCheckboxColumn<EntityModel> getReadOnlyCheckBoxColumn() {
        AbstractCheckboxColumn<EntityModel> readOnlyCheckboxColumn = new AbstractCheckboxColumn<EntityModel>(
            new FieldUpdater<EntityModel, Boolean>() {
                @Override
                public void update(int idx, EntityModel object, Boolean value) {
                    DiskModel diskModel = (DiskModel) object.getEntity();
                    diskModel.getDisk().setReadOnly(value);
                }
            })
            {
                @Override
                protected boolean canEdit(EntityModel object) {
                    DiskModel diskModel = (DiskModel) object.getEntity();
                    Disk disk = diskModel.getDisk();
                    boolean isScsiPassthrough = disk.isScsiPassthrough();
                    boolean ideLimitation = diskModel.getDiskInterface().getSelectedItem() == DiskInterface.IDE;
                    return !isScsiPassthrough && !ideLimitation;
                }

                @Override
                public Boolean getValue(EntityModel object) {
                    DiskModel diskModel = (DiskModel) object.getEntity();
                    return diskModel.getDisk().getReadOnly();
                }
            };
        return readOnlyCheckboxColumn;
    }

    private AbstractCheckboxColumn<EntityModel> getBootCheckBoxColumn() {
        AbstractCheckboxColumn<EntityModel> bootCheckboxColumn = new AbstractCheckboxColumn<EntityModel>(
            new FieldUpdater<EntityModel, Boolean>() {
                @Override
                public void update(int idx, EntityModel object, Boolean value) {
                    DiskModel diskModel = (DiskModel) object.getEntity();
                    diskModel.getIsBootable().setEntity(value);
                }
            })
            {
                @Override
                public SafeHtml getTooltip(EntityModel object) {
                    EntityModel<Boolean> bootModel = ((DiskModel) object.getEntity()).getIsBootable();
                    return bootModel.getChangeProhibitionReason() == null ? null :
                            SafeHtmlUtils.fromString(bootModel.getChangeProhibitionReason());
                }

                @Override
                protected boolean canEdit(EntityModel object) {
                    EntityModel<Boolean> bootModel = ((DiskModel) object.getEntity()).getIsBootable();
                    return  bootModel.getIsChangable() || bootModel.getEntity() == true;
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

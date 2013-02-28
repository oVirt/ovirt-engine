package org.ovirt.engine.ui.common.widget.uicommon.popup.vm;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.Disk.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskInterface;
import org.ovirt.engine.core.common.businessentities.LunDisk;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.ValidatedPanelWidget;
import org.ovirt.engine.ui.common.widget.dialog.ProgressPopupContent;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.EntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.renderer.DiskSizeRenderer.DiskSizeUnit;
import org.ovirt.engine.ui.common.widget.renderer.EnumRenderer;
import org.ovirt.engine.ui.common.widget.renderer.NullSafeRenderer;
import org.ovirt.engine.ui.common.widget.renderer.StorageDomainFreeSpaceRenderer;
import org.ovirt.engine.ui.common.widget.table.column.DiskSizeColumn;
import org.ovirt.engine.ui.common.widget.table.column.EnumColumn;
import org.ovirt.engine.ui.common.widget.table.column.ImageResourceColumn;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.common.widget.uicommon.popup.AbstractModelBoundPopupWidget;
import org.ovirt.engine.ui.common.widget.uicommon.storage.AbstractStorageView;
import org.ovirt.engine.ui.common.widget.uicommon.storage.FcpStorageView;
import org.ovirt.engine.ui.common.widget.uicommon.storage.IscsiStorageView;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.FcpStorageModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.IStorageModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.IscsiStorageModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.NewEditStorageModelBehavior;
import org.ovirt.engine.ui.uicommonweb.models.storage.SanStorageModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.DiskModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class VmDiskPopupWidget extends AbstractModelBoundPopupWidget<DiskModel> {

    interface Driver extends SimpleBeanEditorDriver<DiskModel, VmDiskPopupWidget> {
        Driver driver = GWT.create(Driver.class);
    }

    interface ViewUiBinder extends UiBinder<FlowPanel, VmDiskPopupWidget> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<VmDiskPopupWidget> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @UiField
    @Path("size.entity")
    @WithElementId("size")
    EntityModelTextBoxEditor sizeEditor;

    @UiField
    @Path("alias.entity")
    @WithElementId("alias")
    EntityModelTextBoxEditor aliasEditor;

    @UiField
    @Path("description.entity")
    @WithElementId("description")
    EntityModelTextBoxEditor descriptionEditor;

    @UiField(provided = true)
    @Path("diskInterface.selectedItem")
    @WithElementId("interface")
    ListModelListBoxEditor<Object> interfaceEditor;

    @UiField(provided = true)
    @Path("volumeType.selectedItem")
    @WithElementId("volumeType")
    ListModelListBoxEditor<Object> volumeTypeEditor;

    @UiField(provided = true)
    @Path("dataCenter.selectedItem")
    @WithElementId("dataCenter")
    ListModelListBoxEditor<Object> datacenterEditor;

    @UiField(provided = true)
    @Path("storageDomain.selectedItem")
    @WithElementId("storageDomain")
    ListModelListBoxEditor<Object> storageDomainEditor;

    @UiField(provided = true)
    @Path("quota.selectedItem")
    @WithElementId("quota")
    ListModelListBoxEditor<Object> quotaEditor;

    @UiField(provided = true)
    @Path(value = "host.selectedItem")
    @WithElementId("host")
    ListModelListBoxEditor<Object> hostListEditor;

    @UiField(provided = true)
    @Path(value = "storageType.selectedItem")
    @WithElementId("storageType")
    ListModelListBoxEditor<Object> storageTypeEditor;

    @UiField(provided = true)
    @Path("isWipeAfterDelete.entity")
    @WithElementId("wipeAfterDelete")
    EntityModelCheckBoxEditor wipeAfterDeleteEditor;

    @UiField(provided = true)
    @Path("isBootable.entity")
    @WithElementId("isBootable")
    EntityModelCheckBoxEditor isBootableEditor;

    @UiField(provided = true)
    @Path("isShareable.entity")
    @WithElementId("isShareable")
    EntityModelCheckBoxEditor isShareableEditor;

    @UiField(provided = true)
    @Path("isPlugged.entity")
    @WithElementId("isPlugged")
    EntityModelCheckBoxEditor isPluggedEditor;

    @UiField(provided = true)
    @Path("isAttachDisk.entity")
    @WithElementId("attachDisk")
    EntityModelCheckBoxEditor attachEditor;

    @UiField
    @Ignore
    @WithElementId
    RadioButton internalDiskRadioButton;

    @UiField
    @Ignore
    @WithElementId
    RadioButton externalDiskRadioButton;

    @UiField
    VerticalPanel createDiskPanel;

    @UiField
    VerticalPanel attachDiskPanel;

    @UiField
    SimplePanel innerAttachDiskPanel;

    @UiField
    FlowPanel externalDiskPanel;

    @UiField
    HorizontalPanel topPanel;

    @UiField
    HorizontalPanel diskTypePanel;

    @Ignore
    @WithElementId
    EntityModelCellTable<ListModel> internalDiskTable;

    @Ignore
    @WithElementId
    EntityModelCellTable<ListModel> externalDiskTable;

    @UiField
    Label message;

    @Ignore
    ProgressPopupContent progressContent;

    @Ignore
    IscsiStorageView iscsiStorageView;

    @Ignore
    FcpStorageView fcpStorageView;

    @Ignore
    AbstractStorageView storageView;

    @Ignore
    ValidatedPanelWidget attachPanelWidget;

    boolean isNewLunDiskEnabled;
    StorageModel storageModel;
    IscsiStorageModel iscsiStorageModel;
    FcpStorageModel fcpStorageModel;
    SanStorageModel sanStorageModel;

    public VmDiskPopupWidget(CommonApplicationConstants constants, CommonApplicationResources resources,
            boolean isLunDiskEnabled) {
        this.isNewLunDiskEnabled = isLunDiskEnabled;
        this.progressContent = createProgressContentWidget();
        initManualWidgets();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        localize(constants);
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initAttachPanelWidget();
        initInternalDiskTable(constants, resources);
        initExternalDiskTable(constants, resources);
        Driver.driver.initialize(this);
    }

    private void localize(CommonApplicationConstants constants) {
        aliasEditor.setLabel(constants.aliasVmDiskPopup());
        sizeEditor.setLabel(constants.sizeVmDiskPopup());
        descriptionEditor.setLabel(constants.descriptionVmDiskPopup());
        datacenterEditor.setLabel(constants.dcVmDiskPopup());
        storageDomainEditor.setLabel(constants.storageDomainVmDiskPopup());
        hostListEditor.setLabel(constants.hostVmDiskPopup());
        quotaEditor.setLabel(constants.quotaVmDiskPopup());
        interfaceEditor.setLabel(constants.interfaceVmDiskPopup());
        volumeTypeEditor.setLabel(constants.allocationDisk());
        storageTypeEditor.setLabel(constants.storageTypeVmDiskPopup());
        wipeAfterDeleteEditor.setLabel(constants.wipeAfterDeleteVmDiskPopup());
        isBootableEditor.setLabel(constants.isBootableVmDiskPopup());
        isShareableEditor.setLabel(constants.isShareableVmDiskPopup());
        attachEditor.setLabel(constants.attachDiskVmDiskPopup());
        isPluggedEditor.setLabel(constants.activateVmDiskPopup());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void initManualWidgets() {
        storageDomainEditor = new ListModelListBoxEditor<Object>(new StorageDomainFreeSpaceRenderer());

        hostListEditor = new ListModelListBoxEditor<Object>(new AbstractRenderer<Object>() {
            @Override
            public String render(Object object) {
                return object == null ? "" : ((VDS) object).getName(); //$NON-NLS-1$
            }
        });

        quotaEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return ((Quota) object).getQuotaName();
            }
        });

        interfaceEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return object.toString();
            }
        });

        datacenterEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return ((storage_pool) object).getname();
            }
        });

        volumeTypeEditor = new ListModelListBoxEditor<Object>(new EnumRenderer());
        storageTypeEditor = new ListModelListBoxEditor<Object>(new EnumRenderer());

        wipeAfterDeleteEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        isBootableEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        isShareableEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        isPluggedEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        attachEditor = new EntityModelCheckBoxEditor(Align.RIGHT);

        internalDiskTable = new EntityModelCellTable<ListModel>(true);
        externalDiskTable = new EntityModelCellTable<ListModel>(true);
    }

    private void initAttachPanelWidget() {
        // Create tables container
        VerticalPanel verticalPanel = new VerticalPanel();
        verticalPanel.add(internalDiskTable);
        verticalPanel.add(externalDiskTable);

        // Create ValidatedPanelWidget and add tables container
        attachPanelWidget = new ValidatedPanelWidget();
        attachPanelWidget.setPanelWidget(verticalPanel);
        innerAttachDiskPanel.add(attachPanelWidget);
    }

    private void initInternalDiskTable(final CommonApplicationConstants constants,
            final CommonApplicationResources resources) {
        TextColumnWithTooltip<EntityModel> aliasColumn = new TextColumnWithTooltip<EntityModel>() {
            @Override
            public String getValue(EntityModel object) {
                DiskImage diskImage = (DiskImage) (((DiskModel) (object.getEntity())).getDisk());
                return diskImage.getDiskAlias();
            }
        };
        internalDiskTable.addColumn(aliasColumn, constants.aliasVmDiskTable());

        TextColumnWithTooltip<EntityModel> descriptionColumn = new TextColumnWithTooltip<EntityModel>() {
            @Override
            public String getValue(EntityModel object) {
                DiskImage diskImage = (DiskImage) (((DiskModel) (object.getEntity())).getDisk());
                return diskImage.getDiskDescription();
            }
        };
        internalDiskTable.addColumn(descriptionColumn, constants.descriptionVmDiskTable());

        TextColumnWithTooltip<EntityModel> idColumn = new TextColumnWithTooltip<EntityModel>() {
            @Override
            public String getValue(EntityModel object) {
                DiskImage diskImage = (DiskImage) (((DiskModel) (object.getEntity())).getDisk());
                return diskImage.getId().toString();
            }
        };
        internalDiskTable.addColumn(idColumn, constants.idVmDiskTable());

        DiskSizeColumn<EntityModel> sizeColumn = new DiskSizeColumn<EntityModel>() {
            @Override
            protected Long getRawValue(EntityModel object) {
                DiskImage diskImage = (DiskImage) (((DiskModel) (object.getEntity())).getDisk());
                return diskImage.getSize();
            }
        };
        internalDiskTable.addColumn(sizeColumn, constants.provisionedSizeVmDiskTable(), "105px"); //$NON-NLS-1$

        DiskSizeColumn<EntityModel> actualSizeColumn = new DiskSizeColumn<EntityModel>() {
            @Override
            protected Long getRawValue(EntityModel object) {
                DiskImage diskImage = (DiskImage) (((DiskModel) (object.getEntity())).getDisk());
                return diskImage.getActualSizeFromDiskImage();
            }
        };
        internalDiskTable.addColumn(actualSizeColumn, constants.sizeVmDiskTable(), "105px"); //$NON-NLS-1$

        TextColumnWithTooltip<EntityModel> storageDomainColumn = new TextColumnWithTooltip<EntityModel>() {
            @Override
            public String getValue(EntityModel object) {
                DiskImage diskImage = (DiskImage) (((DiskModel) (object.getEntity())).getDisk());
                return diskImage.getStoragesNames().get(0);
            }
        };
        internalDiskTable.addColumn(storageDomainColumn, constants.storageDomainVmDiskTable());

        TextColumnWithTooltip<EntityModel> interfaceColumn = new EnumColumn<EntityModel, DiskInterface>() {
            @Override
            protected DiskInterface getRawValue(EntityModel object) {
                Disk disk = (Disk) (((DiskModel) (object.getEntity())).getDisk());
                return disk.getDiskInterface();
            }
        };
        internalDiskTable.addColumn(interfaceColumn, constants.interfaceVmDiskPopup(), "55px"); //$NON-NLS-1$

        internalDiskTable.addColumn(new ImageResourceColumn<EntityModel>() {
            @Override
            public ImageResource getValue(EntityModel object) {
                Disk disk = (Disk) (((DiskModel) (object.getEntity())).getDisk());
                setTitle(disk.isBoot() ? constants.bootableDisk() : null);
                return disk.isBoot() ? resources.bootableDiskIcon() : null;
            }
        }, "", "30px"); //$NON-NLS-1$ //$NON-NLS-2$

        internalDiskTable.addColumn(new ImageResourceColumn<EntityModel>() {
            @Override
            public ImageResource getValue(EntityModel object) {
                Disk disk = (Disk) (((DiskModel) (object.getEntity())).getDisk());
                setTitle(disk.isShareable() ? constants.shareable() : null);
                return disk.isShareable() ? resources.shareableDiskIcon() : null;
            }
        }, "", "30px"); //$NON-NLS-1$ //$NON-NLS-2$

        internalDiskTable.setWidth("100%", true); //$NON-NLS-1$
        internalDiskTable.setHeight("100%"); //$NON-NLS-1$
    }

    private void initExternalDiskTable(final CommonApplicationConstants constants,
            final CommonApplicationResources resources) {
        TextColumnWithTooltip<EntityModel> aliasColumn = new TextColumnWithTooltip<EntityModel>() {
            @Override
            public String getValue(EntityModel object) {
                LunDisk disk = (LunDisk) (((DiskModel) (object.getEntity())).getDisk());
                return disk.getDiskAlias();
            }
        };
        externalDiskTable.addColumn(aliasColumn, constants.aliasVmDiskTable());

        TextColumnWithTooltip<EntityModel> descriptionColumn = new TextColumnWithTooltip<EntityModel>() {
            @Override
            public String getValue(EntityModel object) {
                LunDisk disk = (LunDisk) (((DiskModel) (object.getEntity())).getDisk());
                return disk.getDiskDescription();
            }
        };
        externalDiskTable.addColumn(descriptionColumn, constants.descriptionVmDiskTable());

        TextColumnWithTooltip<EntityModel> lunIdColumn = new TextColumnWithTooltip<EntityModel>() {
            @Override
            public String getValue(EntityModel object) {
                LunDisk disk = (LunDisk) (((DiskModel) (object.getEntity())).getDisk());
                return disk.getLun().getLUN_id();
            }
        };
        externalDiskTable.addColumn(lunIdColumn, constants.lunIdSanStorage());

        TextColumnWithTooltip<EntityModel> idColumn = new TextColumnWithTooltip<EntityModel>() {
            @Override
            public String getValue(EntityModel object) {
                LunDisk disk = (LunDisk) (((DiskModel) (object.getEntity())).getDisk());
                return disk.getId().toString();
            }
        };
        externalDiskTable.addColumn(idColumn, constants.idVmDiskTable());

        DiskSizeColumn<EntityModel> sizeColumn = new DiskSizeColumn<EntityModel>(DiskSizeUnit.GIGABYTE) {
            @Override
            protected Long getRawValue(EntityModel object) {
                LunDisk disk = (LunDisk) (((DiskModel) (object.getEntity())).getDisk());
                return (long) disk.getLun().getDeviceSize();
            }
        };
        externalDiskTable.addColumn(sizeColumn, constants.devSizeSanStorage(), "60px"); //$NON-NLS-1$

        TextColumnWithTooltip<EntityModel> pathColumn = new TextColumnWithTooltip<EntityModel>() {
            @Override
            public String getValue(EntityModel object) {
                LunDisk disk = (LunDisk) (((DiskModel) (object.getEntity())).getDisk());
                return String.valueOf(disk.getLun().getPathCount());
            }
        };
        externalDiskTable.addColumn(pathColumn, constants.pathSanStorage(), "40px"); //$NON-NLS-1$

        TextColumnWithTooltip<EntityModel> vendorIdColumn = new TextColumnWithTooltip<EntityModel>() {
            @Override
            public String getValue(EntityModel object) {
                LunDisk disk = (LunDisk) (((DiskModel) (object.getEntity())).getDisk());
                return disk.getLun().getVendorId();
            }
        };
        externalDiskTable.addColumn(vendorIdColumn, constants.vendorIdSanStorage());

        TextColumnWithTooltip<EntityModel> productIdColumn = new TextColumnWithTooltip<EntityModel>() {
            @Override
            public String getValue(EntityModel object) {
                LunDisk disk = (LunDisk) (((DiskModel) (object.getEntity())).getDisk());
                return disk.getLun().getProductId();
            }
        };
        externalDiskTable.addColumn(productIdColumn, constants.productIdSanStorage());

        TextColumnWithTooltip<EntityModel> serialColumn = new TextColumnWithTooltip<EntityModel>() {
            @Override
            public String getValue(EntityModel object) {
                LunDisk disk = (LunDisk) (((DiskModel) (object.getEntity())).getDisk());
                return disk.getLun().getSerial();
            }
        };
        externalDiskTable.addColumn(serialColumn, constants.serialSanStorage());

        TextColumnWithTooltip<EntityModel> interfaceColumn = new EnumColumn<EntityModel, DiskInterface>() {
            @Override
            protected DiskInterface getRawValue(EntityModel object) {
                Disk disk = (Disk) (((DiskModel) (object.getEntity())).getDisk());
                return disk.getDiskInterface();
            }
        };
        externalDiskTable.addColumn(interfaceColumn, constants.interfaceVmDiskPopup(), "55px"); //$NON-NLS-1$

        externalDiskTable.addColumn(new ImageResourceColumn<EntityModel>() {
            @Override
            public ImageResource getValue(EntityModel object) {
                Disk disk = (Disk) (((DiskModel) (object.getEntity())).getDisk());
                setTitle(disk.isBoot() ? constants.bootableDisk() : null);
                return disk.isBoot() ? resources.bootableDiskIcon() : null;
            }
        }, "", "30px"); //$NON-NLS-1$ //$NON-NLS-2$

        externalDiskTable.addColumn(new ImageResourceColumn<EntityModel>() {
            @Override
            public ImageResource getValue(EntityModel object) {
                Disk disk = (Disk) (((DiskModel) (object.getEntity())).getDisk());
                setTitle(disk.isShareable() ? constants.shareable() : null);
                return disk.isShareable() ? resources.shareableDiskIcon() : null;
            }
        }, "", "30px"); //$NON-NLS-1$ //$NON-NLS-2$

        externalDiskTable.setWidth("100%", true); //$NON-NLS-1$
        externalDiskTable.setHeight("100%"); //$NON-NLS-1$
    }

    private ProgressPopupContent createProgressContentWidget() {
        ProgressPopupContent progressPopupContent = new ProgressPopupContent();
        progressPopupContent.setHeight("100%"); //$NON-NLS-1$
        return progressPopupContent;
    }

    @Override
    public void focusInput() {
        sizeEditor.setFocus(true);
    }

    @Override
    public void edit(final DiskModel disk) {
        Driver.driver.edit(disk);

        disk.getIsAttachDisk().getEntityChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                boolean isAttach = (Boolean) ((EntityModel) sender).getEntity();
                createDiskPanel.setVisible(!isAttach);
                attachDiskPanel.setVisible(isAttach);

                if (!isAttach && !isNewLunDiskEnabled) {
                    disk.getIsInternal().setEntity(true);
                }
                revealDiskPanel(disk);
            }
        });

        disk.getIsDirectLunDiskAvaialable().getEntityChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                boolean isDirectLunDiskAvaialable = (Boolean) ((EntityModel) sender).getEntity();
                externalDiskPanel.setVisible(isDirectLunDiskAvaialable);
            }
        });

        internalDiskRadioButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                disk.getIsInternal().setEntity(true);
                revealDiskPanel(disk);
            }
        });
        externalDiskRadioButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                disk.getIsInternal().setEntity(false);
                revealStorageView(disk);
                revealDiskPanel(disk);
            }
        });
        internalDiskRadioButton.setValue(disk.getIsNew() ? true
                : disk.getDisk().getDiskStorageType() == DiskStorageType.IMAGE);
        externalDiskRadioButton.setValue(disk.getIsNew() ? false
                : disk.getDisk().getDiskStorageType() == DiskStorageType.LUN);

        internalDiskRadioButton.setEnabled(disk.getIsNew());
        externalDiskRadioButton.setEnabled(disk.getIsNew());

        storageModel = new StorageModel(new NewEditStorageModelBehavior());

        // Create IscsiStorageModel
        iscsiStorageModel = new IscsiStorageModel();
        iscsiStorageModel.setContainer(storageModel);
        iscsiStorageModel.getPropertyChangedEvent().addListener(progressEventHandler);
        iscsiStorageModel.setIsGrouppedByTarget(true);
        iscsiStorageModel.setIgnoreGrayedOut(true);
        iscsiStorageView = new IscsiStorageView(false, 108, 207, 246, 268, 275, 125, 55, -59);
        iscsiStorageView.edit(iscsiStorageModel);

        // Create FcpStorageModel
        fcpStorageModel = new FcpStorageModel();
        fcpStorageModel.setContainer(storageModel);
        fcpStorageModel.getPropertyChangedEvent().addListener(progressEventHandler);
        fcpStorageModel.setIsGrouppedByTarget(false);
        fcpStorageModel.setIgnoreGrayedOut(true);
        fcpStorageView = new FcpStorageView(false, 266, 240);
        fcpStorageView.edit(fcpStorageModel);

        // Set 'StorageModel' items
        ArrayList<IStorageModel> items = new ArrayList<IStorageModel>();
        items.add(iscsiStorageModel);
        items.add(fcpStorageModel);
        storageModel.setItems(items);
        storageModel.setHost(disk.getHost());

        // SelectedItemChangedEvent handlers
        disk.getStorageType().getSelectedItemChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                revealStorageView(disk);
            }
        });

        disk.getHost().getSelectedItemChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                revealStorageView(disk);
            }
        });

        // Add event handlers
        disk.getPropertyChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                String propName = ((PropertyChangedEventArgs) args).PropertyName;
                if (propName.equals("IsValid")) { //$NON-NLS-1$
                    if (disk.getIsValid()) {
                        attachPanelWidget.markAsValid();
                    } else {
                        attachPanelWidget.markAsInvalid(disk.getInvalidityReasons());
                    }
                }
            }
        });

        revealDiskPanel(disk);
    }

    private void revealDiskPanel(final DiskModel disk) {
        boolean isAttachDisk = (Boolean) disk.getIsAttachDisk().getEntity();
        boolean isInternal = internalDiskRadioButton.getValue();
        boolean isInVm = disk.getVm() != null;

        // Hide tables
        internalDiskTable.setVisible(false);
        externalDiskTable.setVisible(false);

        // Disk type (internal/external) selection panel is visible only when
        // 'Attach disk' mode is enabled or new LunDisk creation is enabled
        diskTypePanel.setVisible(isAttachDisk || isNewLunDiskEnabled);

        if (isAttachDisk) {
            if (isInternal) {
                // Show and edit internal disk table
                internalDiskTable.setVisible(true);
                internalDiskTable.edit(disk.getInternalAttachableDisks());
            }
            else {
                // Show and edit external disk table
                externalDiskTable.setVisible(true);
                externalDiskTable.edit(disk.getExternalAttachableDisks());
            }
        }
        else {
            externalDiskPanel.setVisible(isNewLunDiskEnabled && !isInternal);
        }

        topPanel.setVisible(isInVm && disk.getIsNew());
        aliasEditor.setFocus(!isInVm);
    }

    private void revealStorageView(final DiskModel diskModel) {
        if (!diskModel.getIsNew()) {
            return;
        }

        StorageType storageType = (StorageType) diskModel.getStorageType().getSelectedItem();

        // Set view and model by storage type
        if (storageType == StorageType.ISCSI) {
            storageView = iscsiStorageView;
            sanStorageModel = iscsiStorageModel;
        }
        else if (storageType == StorageType.FCP) {
            storageView = fcpStorageView;
            sanStorageModel = fcpStorageModel;
        }

        storageModel.setSelectedItem(sanStorageModel);
        diskModel.setSanStorageModel(sanStorageModel);

        // Execute 'UpdateCommand' to call 'GetDeviceList'
        sanStorageModel.getUpdateCommand().Execute();
    }

    public boolean handleEnterKeyDisabled() {
        return storageView != null && storageView.isSubViewFocused();
    }

    final IEventListener progressEventHandler = new IEventListener() {
        @Override
        public void eventRaised(Event ev, Object sender, EventArgs args) {
            PropertyChangedEventArgs pcArgs = (PropertyChangedEventArgs) args;
            if ("Progress".equals(pcArgs.PropertyName)) { //$NON-NLS-1$
                externalDiskPanel.clear();
                if (sanStorageModel.getProgress() != null) {
                    externalDiskPanel.add(progressContent);
                } else {
                    externalDiskPanel.add(storageView);
                }
            }
        }
    };

    @Override
    public DiskModel flush() {
        return Driver.driver.flush();
    }

    @Override
    public int setTabIndexes(int nextTabIndex) {
        attachEditor.setTabIndex(nextTabIndex++);

        internalDiskRadioButton.setTabIndex(nextTabIndex++);
        externalDiskRadioButton.setTabIndex(nextTabIndex++);

        sizeEditor.setTabIndex(nextTabIndex++);
        aliasEditor.setTabIndex(nextTabIndex++);
        descriptionEditor.setTabIndex(nextTabIndex++);
        interfaceEditor.setTabIndex(nextTabIndex++);
        volumeTypeEditor.setTabIndex(nextTabIndex++);
        datacenterEditor.setTabIndex(nextTabIndex++);
        storageDomainEditor.setTabIndex(nextTabIndex++);
        quotaEditor.setTabIndex(nextTabIndex++);
        hostListEditor.setTabIndex(nextTabIndex++);
        storageTypeEditor.setTabIndex(nextTabIndex++);
        wipeAfterDeleteEditor.setTabIndex(nextTabIndex++);
        isBootableEditor.setTabIndex(nextTabIndex++);
        isShareableEditor.setTabIndex(nextTabIndex++);

        isPluggedEditor.setTabIndex(nextTabIndex++);

        return nextTabIndex;
    }
}

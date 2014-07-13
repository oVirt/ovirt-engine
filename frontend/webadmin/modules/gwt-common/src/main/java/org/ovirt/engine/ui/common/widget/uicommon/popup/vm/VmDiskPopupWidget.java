package org.ovirt.engine.ui.common.widget.uicommon.popup.vm;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.Disk.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskInterface;
import org.ovirt.engine.core.common.businessentities.LunDisk;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.common.utils.SizeConverter;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.ValidatedPanelWidget;
import org.ovirt.engine.ui.common.widget.dialog.InfoIcon;
import org.ovirt.engine.ui.common.widget.dialog.ProgressPopupContent;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.LongEntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.renderer.EnumRenderer;
import org.ovirt.engine.ui.common.widget.renderer.NullSafeRenderer;
import org.ovirt.engine.ui.common.widget.renderer.StorageDomainFreeSpaceRenderer;
import org.ovirt.engine.ui.common.widget.table.column.DiskSizeColumn;
import org.ovirt.engine.ui.common.widget.table.column.EnumColumn;
import org.ovirt.engine.ui.common.widget.table.column.ImageResourceColumn;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.common.widget.uicommon.disks.DisksViewColumns;
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
import org.ovirt.engine.ui.uicommonweb.models.vms.AbstractDiskModel;
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
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.VerticalPanel;

public class VmDiskPopupWidget extends AbstractModelBoundPopupWidget<AbstractDiskModel> {

    interface Driver extends SimpleBeanEditorDriver<AbstractDiskModel, VmDiskPopupWidget> {
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
    LongEntityModelTextBoxEditor sizeEditor;

    @UiField
    @Path("sizeExtend.entity")
    StringEntityModelTextBoxEditor sizeExtendEditor;

    @UiField
    @Path("alias.entity")
    @WithElementId("alias")
    StringEntityModelTextBoxEditor aliasEditor;

    @UiField
    @Path("description.entity")
    @WithElementId("description")
    StringEntityModelTextBoxEditor descriptionEditor;

    @UiField(provided = true)
    @Path("diskInterface.selectedItem")
    @WithElementId("interface")
    ListModelListBoxEditor<DiskInterface> interfaceEditor;

    @UiField(provided = true)
    @Path("volumeType.selectedItem")
    @WithElementId("volumeType")
    ListModelListBoxEditor<VolumeType> volumeTypeEditor;

    @UiField(provided = true)
    @Path("dataCenter.selectedItem")
    @WithElementId("dataCenter")
    ListModelListBoxEditor<StoragePool> datacenterEditor;

    @UiField(provided = true)
    @Path("storageDomain.selectedItem")
    @WithElementId("storageDomain")
    ListModelListBoxEditor<StorageDomain> storageDomainEditor;

    @UiField(provided = true)
    @Path("quota.selectedItem")
    @WithElementId("quota")
    ListModelListBoxEditor<Quota> quotaEditor;

    @UiField(provided = true)
    @Path(value = "host.selectedItem")
    @WithElementId("host")
    ListModelListBoxEditor<VDS> hostListEditor;

    @UiField(provided = true)
    @Path(value = "storageType.selectedItem")
    @WithElementId("storageType")
    ListModelListBoxEditor<StorageType> storageTypeEditor;

    @UiField(provided = true)
    @Path(value = "isPlugged.entity")
    @WithElementId("plugDiskToVm")
    EntityModelCheckBoxEditor plugDiskToVmEditor;

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
    @Path("isReadOnly.entity")
    @WithElementId("isReadOnly")
    EntityModelCheckBoxEditor isReadOnlyEditor;

    @UiField(provided = true)
    @Path("isScsiPassthrough.entity")
    @WithElementId("isScsiPassthrough")
    EntityModelCheckBoxEditor isScsiPassthroughEditor;

    @UiField(provided = true)
    @Path("isSgIoUnfiltered.entity")
    @WithElementId("isSgIoUnfiltered")
    EntityModelCheckBoxEditor isSgIoUnfilteredEditor;

    @UiField(provided = true)
    @Path("isPlugged.entity")
    @WithElementId("isPlugged")
    EntityModelCheckBoxEditor isPluggedEditor;

    @UiField(provided = true)
    @Path("isAttachDisk.entity")
    @WithElementId("attachDisk")
    EntityModelCheckBoxEditor attachEditor;

    @UiField(provided = true)
    @Ignore
    InfoIcon interfaceInfoIcon;

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
    ValidatedPanelWidget innerAttachDiskPanel;

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

    private final Driver driver = GWT.create(Driver.class);

    boolean isNewLunDiskEnabled;
    StorageModel storageModel;
    IscsiStorageModel iscsiStorageModel;
    FcpStorageModel fcpStorageModel;
    SanStorageModel sanStorageModel;

    public VmDiskPopupWidget(CommonApplicationConstants constants,
                             CommonApplicationResources resources,
                             CommonApplicationTemplates templates,
                             boolean isLunDiskEnabled) {
        this.isNewLunDiskEnabled = isLunDiskEnabled;
        this.progressContent = createProgressContentWidget();
        initManualWidgets(constants, resources, templates);
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        localize(constants);
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initAttachPanelWidget();
        initInternalDiskTable(constants, resources, templates);
        initExternalDiskTable(constants, resources, templates);
        driver.initialize(this);
    }

    private void localize(CommonApplicationConstants constants) {
        aliasEditor.setLabel(constants.aliasVmDiskPopup());
        sizeEditor.setLabel(constants.sizeVmDiskPopup());
        sizeExtendEditor.setLabel(constants.extendImageSizeBy());
        descriptionEditor.setLabel(constants.descriptionVmDiskPopup());
        datacenterEditor.setLabel(constants.dcVmDiskPopup());
        storageDomainEditor.setLabel(constants.storageDomainVmDiskPopup());
        hostListEditor.setLabel(constants.hostVmDiskPopup());
        quotaEditor.setLabel(constants.quotaVmDiskPopup());
        interfaceEditor.setLabel(constants.interfaceVmDiskPopup());
        volumeTypeEditor.setLabel(constants.allocationDisk());
        storageTypeEditor.setLabel(constants.storageTypeVmDiskPopup());
        plugDiskToVmEditor.setLabel(constants.activateVmDiskPopup());
        wipeAfterDeleteEditor.setLabel(constants.wipeAfterDeleteVmDiskPopup());
        isBootableEditor.setLabel(constants.isBootableVmDiskPopup());
        isShareableEditor.setLabel(constants.isShareableVmDiskPopup());
        isReadOnlyEditor.setLabel(constants.isReadOnlyVmDiskPopup());
        isScsiPassthroughEditor.setLabel(constants.isScsiPassthroughEditor());
        isSgIoUnfilteredEditor.setLabel(constants.isSgIoUnfilteredEditor());
        attachEditor.setLabel(constants.attachDiskVmDiskPopup());
        isPluggedEditor.setLabel(constants.activateVmDiskPopup());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void initManualWidgets(CommonApplicationConstants constants,
                                   CommonApplicationResources resources,
                                   CommonApplicationTemplates templates) {
        storageDomainEditor = new ListModelListBoxEditor<StorageDomain>(new StorageDomainFreeSpaceRenderer());

        hostListEditor = new ListModelListBoxEditor<VDS>(new AbstractRenderer<VDS>() {
            @Override
            public String render(VDS vds) {
                return vds == null ? "" : vds.getName(); //$NON-NLS-1$
            }
        });

        quotaEditor = new ListModelListBoxEditor<Quota>(new NullSafeRenderer<Quota>() {
            @Override
            public String renderNullSafe(Quota quota) {
                return quota.getQuotaName();
            }
        });

        interfaceEditor = new ListModelListBoxEditor<DiskInterface>(new EnumRenderer());

        datacenterEditor = new ListModelListBoxEditor<StoragePool>(new NullSafeRenderer<StoragePool>() {
            @Override
            public String renderNullSafe(StoragePool storagePool) {
                return storagePool.getName();
            }
        });

        volumeTypeEditor = new ListModelListBoxEditor<VolumeType>(new EnumRenderer());
        storageTypeEditor = new ListModelListBoxEditor<StorageType>(new EnumRenderer());
        plugDiskToVmEditor = new EntityModelCheckBoxEditor(Align.RIGHT);

        wipeAfterDeleteEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        isBootableEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        isShareableEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        isReadOnlyEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        isScsiPassthroughEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        isSgIoUnfilteredEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        isPluggedEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        attachEditor = new EntityModelCheckBoxEditor(Align.RIGHT);

        internalDiskTable = new EntityModelCellTable<ListModel>(true);
        externalDiskTable = new EntityModelCellTable<ListModel>(true);

        interfaceInfoIcon = new InfoIcon(templates.italicText(constants.diskInterfaceInfo()), resources);
    }

    private void initAttachPanelWidget() {
        // Create tables container
        VerticalPanel verticalPanel = new VerticalPanel();
        verticalPanel.add(internalDiskTable);
        verticalPanel.add(externalDiskTable);

        // Create ValidatedPanelWidget and add tables container
        innerAttachDiskPanel.setWidget(verticalPanel);
    }

    private void initInternalDiskTable(final CommonApplicationConstants constants,
            final CommonApplicationResources resources,
            final CommonApplicationTemplates templates) {
        internalDiskTable.enableColumnResizing();

        TextColumnWithTooltip<EntityModel> aliasColumn = new TextColumnWithTooltip<EntityModel>() {
            @Override
            public String getValue(EntityModel object) {
                DiskImage diskImage = (DiskImage) (((DiskModel) (object.getEntity())).getDisk());
                return diskImage.getDiskAlias();
            }
        };
        internalDiskTable.addColumn(aliasColumn, constants.aliasVmDiskTable(), "85px"); //$NON-NLS-1$

        TextColumnWithTooltip<EntityModel> descriptionColumn = new TextColumnWithTooltip<EntityModel>() {
            @Override
            public String getValue(EntityModel object) {
                DiskImage diskImage = (DiskImage) (((DiskModel) (object.getEntity())).getDisk());
                return diskImage.getDiskDescription();
            }
        };
        internalDiskTable.addColumn(descriptionColumn, constants.descriptionVmDiskTable(), "85px"); //$NON-NLS-1$

        TextColumnWithTooltip<EntityModel> idColumn = new TextColumnWithTooltip<EntityModel>() {
            @Override
            public String getValue(EntityModel object) {
                DiskImage diskImage = (DiskImage) (((DiskModel) (object.getEntity())).getDisk());
                return diskImage.getId().toString();
            }
        };
        internalDiskTable.addColumn(idColumn, constants.idVmDiskTable(), "85px"); //$NON-NLS-1$

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
                return diskImage.getActualSizeInBytes();
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
        internalDiskTable.addColumn(storageDomainColumn, constants.storageDomainVmDiskTable(), "115px"); //$NON-NLS-1$

        TextColumnWithTooltip<EntityModel> interfaceColumn = new EnumColumn<EntityModel, DiskInterface>() {
            @Override
            protected DiskInterface getRawValue(EntityModel object) {
                Disk disk = (((DiskModel) (object.getEntity())).getDisk());
                return disk.getDiskInterface();
            }
        };
        internalDiskTable.addColumn(interfaceColumn, constants.interfaceVmDiskPopup(), "95px"); //$NON-NLS-1$

        SafeHtml readOnlyColumnHeader = templates.imageWithTitle(SafeHtmlUtils.fromTrustedString(
                AbstractImagePrototype.create(resources.readOnlyDiskIcon()).getHTML()), constants.readOnly()
        );
        internalDiskTable.addColumn(DisksViewColumns.readOnlyCheckboxColumn, readOnlyColumnHeader, "30px"); //$NON-NLS-1$

        SafeHtml bootableColumnHeader = templates.imageWithTitle(SafeHtmlUtils.fromTrustedString(
                AbstractImagePrototype.create(resources.bootableDiskIcon()).getHTML()), constants.bootable()
        );
        internalDiskTable.addColumn(new ImageResourceColumn<EntityModel>() {
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
        internalDiskTable.addColumn(new ImageResourceColumn<EntityModel>() {
            @Override
            public ImageResource getValue(EntityModel object) {
                Disk disk = (((DiskModel) (object.getEntity())).getDisk());
                setTitle(disk.isShareable() ? constants.shareable() : null);
                return disk.isShareable() ? resources.shareableDiskIcon() : null;
            }
        }, shareableColumnHeader, "30px"); //$NON-NLS-1$

        internalDiskTable.setWidth("100%", true); //$NON-NLS-1$
        internalDiskTable.setHeight("100%"); //$NON-NLS-1$
    }

    private void initExternalDiskTable(final CommonApplicationConstants constants,
            final CommonApplicationResources resources,
            final CommonApplicationTemplates templates) {
        externalDiskTable.enableColumnResizing();

        TextColumnWithTooltip<EntityModel> aliasColumn = new TextColumnWithTooltip<EntityModel>() {
            @Override
            public String getValue(EntityModel object) {
                LunDisk disk = (LunDisk) (((DiskModel) (object.getEntity())).getDisk());
                return disk.getDiskAlias();
            }
        };
        externalDiskTable.addColumn(aliasColumn, constants.aliasVmDiskTable(), "60px"); //$NON-NLS-1$

        TextColumnWithTooltip<EntityModel> descriptionColumn = new TextColumnWithTooltip<EntityModel>() {
            @Override
            public String getValue(EntityModel object) {
                LunDisk disk = (LunDisk) (((DiskModel) (object.getEntity())).getDisk());
                return disk.getDiskDescription();
            }
        };
        externalDiskTable.addColumn(descriptionColumn, constants.descriptionVmDiskTable(), "85px"); //$NON-NLS-1$

        TextColumnWithTooltip<EntityModel> lunIdColumn = new TextColumnWithTooltip<EntityModel>() {
            @Override
            public String getValue(EntityModel object) {
                LunDisk disk = (LunDisk) (((DiskModel) (object.getEntity())).getDisk());
                return disk.getLun().getLUN_id();
            }
        };
        externalDiskTable.addColumn(lunIdColumn, constants.lunIdSanStorage(), "60px"); //$NON-NLS-1$

        TextColumnWithTooltip<EntityModel> idColumn = new TextColumnWithTooltip<EntityModel>() {
            @Override
            public String getValue(EntityModel object) {
                LunDisk disk = (LunDisk) (((DiskModel) (object.getEntity())).getDisk());
                return disk.getId().toString();
            }
        };
        externalDiskTable.addColumn(idColumn, constants.idVmDiskTable(), "60px"); //$NON-NLS-1$

        DiskSizeColumn<EntityModel> sizeColumn = new DiskSizeColumn<EntityModel>(SizeConverter.SizeUnit.GB) {
            @Override
            protected Long getRawValue(EntityModel object) {
                LunDisk disk = (LunDisk) (((DiskModel) (object.getEntity())).getDisk());
                return (long) disk.getLun().getDeviceSize();
            }
        };
        externalDiskTable.addColumn(sizeColumn, constants.devSizeSanStorage(), "70px"); //$NON-NLS-1$

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
        externalDiskTable.addColumn(vendorIdColumn, constants.vendorIdSanStorage(), "70px"); //$NON-NLS-1$

        TextColumnWithTooltip<EntityModel> productIdColumn = new TextColumnWithTooltip<EntityModel>() {
            @Override
            public String getValue(EntityModel object) {
                LunDisk disk = (LunDisk) (((DiskModel) (object.getEntity())).getDisk());
                return disk.getLun().getProductId();
            }
        };
        externalDiskTable.addColumn(productIdColumn, constants.productIdSanStorage(), "70px"); //$NON-NLS-1$

        TextColumnWithTooltip<EntityModel> serialColumn = new TextColumnWithTooltip<EntityModel>() {
            @Override
            public String getValue(EntityModel object) {
                LunDisk disk = (LunDisk) (((DiskModel) (object.getEntity())).getDisk());
                return disk.getLun().getSerial();
            }
        };
        externalDiskTable.addColumn(serialColumn, constants.serialSanStorage(), "70px"); //$NON-NLS-1$

        TextColumnWithTooltip<EntityModel> interfaceColumn = new EnumColumn<EntityModel, DiskInterface>() {
            @Override
            protected DiskInterface getRawValue(EntityModel object) {
                Disk disk = (((DiskModel) (object.getEntity())).getDisk());
                return disk.getDiskInterface();
            }
        };
        externalDiskTable.addColumn(interfaceColumn, constants.interfaceVmDiskPopup(), "90px"); //$NON-NLS-1$

        SafeHtml readOnlyColumnHeader = templates.imageWithTitle(SafeHtmlUtils.fromTrustedString(
                AbstractImagePrototype.create(resources.readOnlyDiskIcon()).getHTML()), constants.readOnly()
        );
        externalDiskTable.addColumn(DisksViewColumns.readOnlyCheckboxColumn, readOnlyColumnHeader, "30px"); //$NON-NLS-1$

        SafeHtml bootableColumnHeader = templates.imageWithTitle(SafeHtmlUtils.fromTrustedString(
                AbstractImagePrototype.create(resources.bootableDiskIcon()).getHTML()), constants.bootable()
        );
        externalDiskTable.addColumn(new ImageResourceColumn<EntityModel>() {
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
        externalDiskTable.addColumn(new ImageResourceColumn<EntityModel>() {
            @Override
            public ImageResource getValue(EntityModel object) {
                Disk disk = (((DiskModel) (object.getEntity())).getDisk());
                setTitle(disk.isShareable() ? constants.shareable() : null);
                return disk.isShareable() ? resources.shareableDiskIcon() : null;
            }
        }, shareableColumnHeader, "30px"); //$NON-NLS-1$

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
    public void edit(final AbstractDiskModel disk) {
        driver.edit(disk);

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

        disk.getIsVirtioScsiEnabled().getEntityChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                if (disk.getVm() == null) {
                    // not relevant for floating disks
                    return;
                }

                boolean isVirtioScsiEnabled = Boolean.TRUE.equals(((EntityModel) sender).getEntity());
                Version clusterVersion = disk.getVm().getVdsGroupCompatibilityVersion();

                // Show the info icon if VirtIO-SCSI is supported by the cluster but disabled for the VM
                interfaceInfoIcon.setVisible(clusterVersion.compareTo(Version.v3_3) >= 0 && !isVirtioScsiEnabled);
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

        iscsiStorageView = new IscsiStorageView(false, 107, 201, 240, 275, 135, 55, -62);
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
                String propName = ((PropertyChangedEventArgs) args).propertyName;
                if (propName.equals("IsValid")) { //$NON-NLS-1$
                    if (disk.getIsValid()) {
                        innerAttachDiskPanel.markAsValid();
                    } else {
                        innerAttachDiskPanel.markAsInvalid(disk.getInvalidityReasons());
                    }
                }
            }
        });

        revealDiskPanel(disk);
    }

    private void revealDiskPanel(final AbstractDiskModel disk) {
        boolean isAttachDisk = disk.getIsAttachDisk().getEntity();
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
                internalDiskTable.asEditor().edit(disk.getInternalAttachableDisks());
            }
            else {
                // Show and edit external disk table
                externalDiskTable.setVisible(true);
                externalDiskTable.asEditor().edit(disk.getExternalAttachableDisks());
            }
        }
        else {
            externalDiskPanel.setVisible(isNewLunDiskEnabled && !isInternal);
        }

        topPanel.setVisible(isInVm && disk.getIsNew());
        aliasEditor.setFocus(!isInVm);
    }

    private void revealStorageView(final AbstractDiskModel diskModel) {
        if (!diskModel.getIsNew()) {
            return;
        }

        StorageType storageType = diskModel.getStorageType().getSelectedItem();

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
        sanStorageModel.getUpdateCommand().execute();
    }

    public boolean handleEnterKeyDisabled() {
        return storageView != null && storageView.isSubViewFocused();
    }

    final IEventListener progressEventHandler = new IEventListener() {
        @Override
        public void eventRaised(Event ev, Object sender, EventArgs args) {
            PropertyChangedEventArgs pcArgs = (PropertyChangedEventArgs) args;
            if (PropertyChangedEventArgs.PROGRESS.equals(pcArgs.propertyName)) {
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
    public AbstractDiskModel flush() {
        return driver.flush();
    }

    @Override
    public int setTabIndexes(int nextTabIndex) {
        attachEditor.setTabIndex(nextTabIndex++);

        internalDiskRadioButton.setTabIndex(nextTabIndex++);
        externalDiskRadioButton.setTabIndex(nextTabIndex++);
        sizeEditor.setTabIndex(nextTabIndex++);
        sizeExtendEditor.setTabIndex(nextTabIndex++);
        aliasEditor.setTabIndex(nextTabIndex++);
        descriptionEditor.setTabIndex(nextTabIndex++);
        interfaceEditor.setTabIndex(nextTabIndex++);
        volumeTypeEditor.setTabIndex(nextTabIndex++);
        datacenterEditor.setTabIndex(nextTabIndex++);
        storageDomainEditor.setTabIndex(nextTabIndex++);
        quotaEditor.setTabIndex(nextTabIndex++);
        hostListEditor.setTabIndex(nextTabIndex++);
        storageTypeEditor.setTabIndex(nextTabIndex++);
        plugDiskToVmEditor.setTabIndex(nextTabIndex++);
        wipeAfterDeleteEditor.setTabIndex(nextTabIndex++);
        isPluggedEditor.setTabIndex(nextTabIndex++);
        isBootableEditor.setTabIndex(nextTabIndex++);
        isShareableEditor.setTabIndex(nextTabIndex++);
        isReadOnlyEditor.setTabIndex(nextTabIndex++);
        isScsiPassthroughEditor.setTabIndex(nextTabIndex++);
        isSgIoUnfilteredEditor.setTabIndex(nextTabIndex++);

        return nextTabIndex;
    }
}

package org.ovirt.engine.ui.common.widget.uicommon.popup.vm;

import org.ovirt.engine.core.common.businessentities.Disk.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.LunDisk;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.EntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.renderer.EnumRenderer;
import org.ovirt.engine.ui.common.widget.renderer.NullSafeRenderer;
import org.ovirt.engine.ui.common.widget.table.column.DiskSizeColumn;
import org.ovirt.engine.ui.common.widget.table.column.ImageResourceColumn;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.common.widget.uicommon.popup.AbstractModelBoundPopupWidget;
import org.ovirt.engine.ui.common.widget.uicommon.storage.AbstractStorageView;
import org.ovirt.engine.ui.common.widget.uicommon.storage.FcpStorageView;
import org.ovirt.engine.ui.common.widget.uicommon.storage.IscsiStorageView;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.IscsiStorageModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.NewEditStorageModelBehavior;
import org.ovirt.engine.ui.uicommonweb.models.storage.SanStorageModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.DiskModel;

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
import com.google.gwt.user.client.ui.VerticalPanel;

public class VmDiskPopupWidget extends AbstractModelBoundPopupWidget<DiskModel> {

    interface Driver extends SimpleBeanEditorDriver<DiskModel, VmDiskPopupWidget> {
        Driver driver = GWT.create(Driver.class);
    }

    interface ViewUiBinder extends UiBinder<FlowPanel, VmDiskPopupWidget> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    @Path("size.entity")
    EntityModelTextBoxEditor sizeEditor;

    @UiField
    @Path("alias.entity")
    EntityModelTextBoxEditor aliasEditor;

    @UiField
    @Path("description.entity")
    EntityModelTextBoxEditor descriptionEditor;

    @UiField(provided = true)
    @Path("dataCenter.selectedItem")
    ListModelListBoxEditor<Object> datacenterEditor;

    @UiField(provided = true)
    @Path("storageDomain.selectedItem")
    ListModelListBoxEditor<Object> storageDomainEditor;

    @UiField(provided = true)
    @Path(value = "host.selectedItem")
    @WithElementId("host")
    ListModelListBoxEditor<Object> hostListEditor;

    @UiField(provided = true)
    @Path("quota.selectedItem")
    ListModelListBoxEditor<Object> quotaEditor;

    @UiField(provided = true)
    @Path("interface.selectedItem")
    ListModelListBoxEditor<Object> interfaceEditor;

    @UiField(provided = true)
    @Path("volumeType.selectedItem")
    ListModelListBoxEditor<Object> volumeTypeEditor;

    @UiField(provided = true)
    @Path(value = "storageType.selectedItem")
    ListModelListBoxEditor<Object> storageTypeEditor;

    @UiField(provided = true)
    @Path("wipeAfterDelete.entity")
    EntityModelCheckBoxEditor wipeAfterDeleteEditor;

    @UiField(provided = true)
    @Path("isBootable.entity")
    EntityModelCheckBoxEditor isBootableEditor;

    @UiField(provided = true)
    @Path("isShareable.entity")
    EntityModelCheckBoxEditor isShareableEditor;

    @UiField(provided = true)
    @Path("isPlugged.entity")
    EntityModelCheckBoxEditor isPluggedEditor;

    @UiField(provided = true)
    @Path("attachDisk.entity")
    EntityModelCheckBoxEditor attachEditor;

    @UiField
    @Ignore
    RadioButton internalDiskRadioButton;

    @UiField
    @Ignore
    RadioButton externalDiskRadioButton;

    @UiField
    VerticalPanel createDiskPanel;

    @UiField
    VerticalPanel attachDiskPanel;

    @UiField
    FlowPanel externalDiskPanel;

    @UiField
    HorizontalPanel topPanel;

    @UiField
    HorizontalPanel diskTypePanel;

    @UiField(provided = true)
    @Ignore
    EntityModelCellTable<ListModel> internalDiskTable;

    @UiField(provided = true)
    @Ignore
    EntityModelCellTable<ListModel> externalDiskTable;

    @UiField
    Label message;

    boolean isNewLunDiskEnabled;

    public VmDiskPopupWidget(CommonApplicationConstants constants, CommonApplicationResources resources,
            boolean isLunDiskEnabled) {
        this.isNewLunDiskEnabled = isLunDiskEnabled;
        initManualWidgets();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        localize(constants);
        initInternalDiskTable(constants, resources);
        initExternalDiskTable(constants, resources);
        Driver.driver.initialize(this);
    }

    // TODO: Localize
    private void localize(CommonApplicationConstants constants) {
        aliasEditor.setLabel(constants.aliasVmDiskPopup());
        sizeEditor.setLabel(constants.sizeVmDiskPopup());
        descriptionEditor.setLabel(constants.descriptionVmDiskPopup());
        datacenterEditor.setLabel(constants.dcVmDiskPopup());
        storageDomainEditor.setLabel(constants.storageDomainVmDiskPopup());
        hostListEditor.setLabel(constants.hostVmDiskPopup());
        quotaEditor.setLabel(constants.quotaVmDiskPopup());
        interfaceEditor.setLabel(constants.interfaceVmDiskPopup());
        volumeTypeEditor.setLabel(constants.formatVmDiskPopup());
        storageTypeEditor.setLabel(constants.storageTypeVmDiskPopup());
        wipeAfterDeleteEditor.setLabel(constants.wipeAfterDeleteVmDiskPopup());
        isBootableEditor.setLabel(constants.isBootableVmDiskPopup());
        isShareableEditor.setLabel(constants.isShareableVmDiskPopup());
        attachEditor.setLabel(constants.attachDiskVmDiskPopup());
        isPluggedEditor.setLabel(constants.activateVmDiskPopup());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void initManualWidgets() {
        storageDomainEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return ((storage_domains) object).getstorage_name();
            }
        });

        hostListEditor = new ListModelListBoxEditor<Object>(new AbstractRenderer<Object>() {
            @Override
            public String render(Object object) {
                return object == null ? "" : ((VDS) object).getvds_name(); //$NON-NLS-1$
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
                return diskImage.getsize();
            }
        };
        internalDiskTable.addColumn(sizeColumn, constants.provisionedSizeVmDiskTable(), "105px"); //$NON-NLS-1$

        DiskSizeColumn<EntityModel> actualSizeColumn = new DiskSizeColumn<EntityModel>() {
            @Override
            protected Long getRawValue(EntityModel object) {
                DiskImage diskImage = (DiskImage) (((DiskModel) (object.getEntity())).getDisk());
                return diskImage.getactual_size();
            }
        };
        internalDiskTable.addColumn(actualSizeColumn, constants.sizeVmDiskTable());

        TextColumnWithTooltip<EntityModel> storageDomainColumn = new TextColumnWithTooltip<EntityModel>() {
            @Override
            public String getValue(EntityModel object) {
                DiskImage diskImage = (DiskImage) (((DiskModel) (object.getEntity())).getDisk());
                return diskImage.getStoragesNames().get(0);
            }
        };
        internalDiskTable.addColumn(storageDomainColumn, constants.storageDomainVmDiskTable());

        internalDiskTable.addColumn(new ImageResourceColumn<EntityModel>() {
            @Override
            public ImageResource getValue(EntityModel object) {
                DiskImage diskImage = (DiskImage) (((DiskModel) (object.getEntity())).getDisk());
                return diskImage.isShareable() ? resources.shareableDiskIcon() : null;
            }
        }, constants.shareable(), "70px"); //$NON-NLS-1$

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

        DiskSizeColumn<EntityModel> sizeColumn = new DiskSizeColumn<EntityModel>() {
            @Override
            protected Long getRawValue(EntityModel object) {
                LunDisk disk = (LunDisk) (((DiskModel) (object.getEntity())).getDisk());
                return (long) disk.getLun().getDeviceSize();
            }
        };
        externalDiskTable.addColumn(sizeColumn, constants.devSizeSanStorage());

        TextColumnWithTooltip<EntityModel> pathColumn = new TextColumnWithTooltip<EntityModel>() {
            @Override
            public String getValue(EntityModel object) {
                LunDisk disk = (LunDisk) (((DiskModel) (object.getEntity())).getDisk());
                return String.valueOf(disk.getLun().getPathCount());
            }
        };
        externalDiskTable.addColumn(pathColumn, constants.pathSanStorage());

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

        externalDiskTable.addColumn(new ImageResourceColumn<EntityModel>() {
            @Override
            public ImageResource getValue(EntityModel object) {
                LunDisk disk = (LunDisk) (((DiskModel) (object.getEntity())).getDisk());
                return disk.isShareable() ? resources.shareableDiskIcon() : null;
            }
        }, constants.shareable(), "70px"); //$NON-NLS-1$

        externalDiskTable.setWidth("100%", true); //$NON-NLS-1$
        externalDiskTable.setHeight("100%"); //$NON-NLS-1$
    }

    @Override
    public void focusInput() {
        sizeEditor.setFocus(true);
    }

    @Override
    public void edit(final DiskModel disk) {
        Driver.driver.edit(disk);

        disk.getAttachDisk().getEntityChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                boolean isAttach = (Boolean) ((EntityModel) sender).getEntity();
                createDiskPanel.setVisible(!isAttach);
                attachDiskPanel.setVisible(isAttach);
                revealDiskPanel(disk);
            }
        });

        disk.getIsInVm().getEntityChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                boolean isInVm = (Boolean) ((EntityModel) sender).getEntity();
                topPanel.setVisible(isInVm);
                aliasEditor.setFocus(!isInVm);
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

        disk.getStorageType().getSelectedItemChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                revealStorageView(disk);
            }
        });

        revealDiskPanel(disk);
    }

    private void revealDiskPanel(final DiskModel disk) {
        boolean isAttachDisk = (Boolean) disk.getAttachDisk().getEntity();
        boolean isInternal = internalDiskRadioButton.getValue();

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
    }

    private void revealStorageView(DiskModel disk) {
        StorageModel storageModel = new StorageModel(new NewEditStorageModelBehavior());
        storageModel.setHost(disk.getHost());

        final SanStorageModel model = new IscsiStorageModel();
        model.setContainer(storageModel);
        model.setIsGrouppedByTarget(true);
        model.setIgnoreGrayedOut(true);

        disk.setSanStorageModel(model);
        disk.getHost().getItemsChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                model.getUpdateCommand().Execute();
            }
        });

        AbstractStorageView storageView = null;
        StorageType storageType = (StorageType) disk.getStorageType().getSelectedItem();

        // Reveal view by storge type
        if (storageType == StorageType.ISCSI) {
            storageView = new IscsiStorageView(false, 110, 210, 244, 268, 275, 125, 50, -43);
        }
        else if (storageType == StorageType.FCP) {
            storageView = new FcpStorageView(false);
        }

        // Clear the current storage view
        externalDiskPanel.clear();

        // Add the new storage view and call focus on it if needed
        if (storageView != null) {
            storageView.edit(model);
            externalDiskPanel.add(storageView);
        }
    }

    @Override
    public DiskModel flush() {
        return Driver.driver.flush();
    }

}

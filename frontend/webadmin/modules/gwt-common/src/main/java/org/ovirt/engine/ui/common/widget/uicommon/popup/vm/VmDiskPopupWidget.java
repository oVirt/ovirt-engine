package org.ovirt.engine.ui.common.widget.uicommon.popup.vm;

import java.util.ArrayList;

import org.gwtbootstrap3.client.ui.Container;
import org.gwtbootstrap3.client.ui.constants.ColumnSize;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.profiles.DiskProfile;
import org.ovirt.engine.core.common.businessentities.storage.DiskInterface;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.RadioButtonPanel;
import org.ovirt.engine.ui.common.widget.dialog.InfoIcon;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.IntegerEntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.renderer.EnumRenderer;
import org.ovirt.engine.ui.common.widget.renderer.NameRenderer;
import org.ovirt.engine.ui.common.widget.renderer.StorageDomainFreeSpaceRenderer;
import org.ovirt.engine.ui.common.widget.uicommon.popup.AbstractModelBoundPopupWidget;
import org.ovirt.engine.ui.common.widget.uicommon.storage.AbstractStorageView;
import org.ovirt.engine.ui.common.widget.uicommon.storage.FcpStorageView;
import org.ovirt.engine.ui.common.widget.uicommon.storage.IscsiStorageView;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.FcpStorageModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.IStorageModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.IscsiStorageModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.NewEditStorageModelBehavior;
import org.ovirt.engine.ui.uicommonweb.models.storage.SanStorageModelBase;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.AbstractDiskModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

public class VmDiskPopupWidget extends AbstractModelBoundPopupWidget<AbstractDiskModel> {

    interface Driver extends UiCommonEditorDriver<AbstractDiskModel, VmDiskPopupWidget> {
    }

    interface ViewUiBinder extends UiBinder<Container, VmDiskPopupWidget> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<VmDiskPopupWidget> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @UiField
    @Path("size.entity")
    @WithElementId("size")
    IntegerEntityModelTextBoxEditor sizeEditor;

    @UiField(provided = true)
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
    @Path("dataCenter.selectedItem")
    @WithElementId("dataCenter")
    ListModelListBoxEditor<StoragePool> datacenterEditor;

    @UiField(provided = true)
    @Path("storageDomain.selectedItem")
    @WithElementId("storageDomain")
    ListModelListBoxEditor<StorageDomain> storageDomainEditor;

    @UiField(provided = true)
    @Path("volumeType.selectedItem")
    @WithElementId("volumeType")
    ListModelListBoxEditor<VolumeType> volumeTypeEditor;

    @UiField(provided = true)
    @Path("diskProfile.selectedItem")
    @WithElementId("diskProfile")
    ListModelListBoxEditor<DiskProfile> diskProfileEditor;

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
    @Path("passDiscard.entity")
    @WithElementId("passDiscard")
    EntityModelCheckBoxEditor passDiscardEditor;

    @UiField(provided = true)
    @Path("isUsingScsiReservation.entity")
    @WithElementId("isUsingScsiReservation")
    EntityModelCheckBoxEditor isUsingScsiReservationEditor;

    @UiField(provided = true)
    @Path("isScsiPassthrough.entity")
    @WithElementId("isScsiPassthrough")
    EntityModelCheckBoxEditor isScsiPassthroughEditor;

    @UiField(provided = true)
    @Path("isSgIoUnfiltered.entity")
    @WithElementId("isSgIoUnfiltered")
    EntityModelCheckBoxEditor isSgIoUnfilteredEditor;

    @UiField(provided = true)
    @Path("isIncrementalBackup.entity")
    @WithElementId("isIncrementalBackup")
    EntityModelCheckBoxEditor isIncrementalBackupEditor;

    @UiField(provided = true)
    @Ignore
    InfoIcon interfaceInfoIcon;

    @UiField(provided = true)
    @Ignore
    InfoIcon scsiReservationInfoIcon;

    @UiField
    FlowPanel externalDiskPanel;

    @UiField
    RadioButtonPanel radioButtonPanel;

    @UiField
    Label message;

    @UiField(provided = true)
    @Ignore
    InfoIcon hostInfoIcon;

    @Ignore
    IscsiStorageView iscsiStorageView;

    @Ignore
    FcpStorageView fcpStorageView;

    @Ignore
    AbstractStorageView<? extends IStorageModel> storageView;

    private static final CommonApplicationTemplates templates = AssetProvider.getTemplates();
    @UiField(provided = true)
    static final CommonApplicationConstants constants = AssetProvider.getConstants();

    private final Driver driver = GWT.create(Driver.class);

    boolean isNewLunDiskEnabled;
    StorageModel storageModel;
    IscsiStorageModel iscsiStorageModel;
    FcpStorageModel fcpStorageModel;
    SanStorageModelBase sanStorageModelBase;

    public VmDiskPopupWidget(boolean isLunDiskEnabled) {
        this.isNewLunDiskEnabled = isLunDiskEnabled;
        initManualWidgets();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        driver.initialize(this);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void initManualWidgets() {
        sizeExtendEditor = StringEntityModelTextBoxEditor.newTrimmingEditor();

        storageDomainEditor = new ListModelListBoxEditor<>(new StorageDomainFreeSpaceRenderer());

        hostListEditor = new ListModelListBoxEditor<>(new NameRenderer<VDS>());

        diskProfileEditor = new ListModelListBoxEditor<>(new NameRenderer<DiskProfile>());

        quotaEditor = new ListModelListBoxEditor<>(new NameRenderer<Quota>());

        interfaceEditor = new ListModelListBoxEditor<>(new EnumRenderer());

        datacenterEditor = new ListModelListBoxEditor<>(new NameRenderer<StoragePool>());

        volumeTypeEditor = new ListModelListBoxEditor<>(new EnumRenderer());
        storageTypeEditor = new ListModelListBoxEditor<>(new EnumRenderer());
        plugDiskToVmEditor = new EntityModelCheckBoxEditor(Align.RIGHT);

        wipeAfterDeleteEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        isBootableEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        isShareableEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        isReadOnlyEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        passDiscardEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        isUsingScsiReservationEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        isScsiPassthroughEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        isSgIoUnfilteredEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        isIncrementalBackupEditor = new EntityModelCheckBoxEditor(Align.RIGHT);

        hostInfoIcon = new InfoIcon(SafeHtmlUtils.fromString(constants.hostToUseToolTip()));
        interfaceInfoIcon = new InfoIcon(templates.italicText(constants.diskInterfaceInfo()));
        scsiReservationInfoIcon = new InfoIcon(templates.italicText(constants.scsiReservationInfoIcon()));
    }

    @Override
    public void focusInput() {
        sizeEditor.setFocus(true);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void edit(final AbstractDiskModel disk) {
        driver.edit(disk);

        disk.getIsDirectLunDiskAvaialable().getEntityChangedEvent().addListener((ev, sender, args) -> {
            boolean isDirectLunDiskAvaialable = ((EntityModel<Boolean>) sender).getEntity();
            externalDiskPanel.setVisible(isDirectLunDiskAvaialable);
        });

        disk.getIsUsingScsiReservation().getPropertyChangedEvent().addListener((ev, sender, args) -> {
            if ("Entity".equals(args.propertyName) || "IsAvailable".equals(args.propertyName)) { //$NON-NLS-1$ $NON-NLS-2$
                EntityModel<Boolean> entity = disk.getIsUsingScsiReservation();
                scsiReservationInfoIcon.setVisible(entity.getEntity() && entity.getIsAvailable());
            }
        });

        disk.getIsVirtioScsiEnabled().getEntityChangedEvent().addListener((ev, sender, args) -> {
            if (disk.getVm() == null) {
                // not relevant for floating disks
                return;
            }

            boolean isVirtioScsiEnabled = ((EntityModel<Boolean>) sender).getEntity();

            // Show the info icon if VirtIO-SCSI is supported by the cluster but disabled for the VM
            interfaceInfoIcon.setVisible(!isVirtioScsiEnabled);

            // Make room for it by making the control widget narrower
            interfaceEditor.removeWidgetColSize(isVirtioScsiEnabled ? ColumnSize.SM_7 : ColumnSize.SM_8);
            interfaceEditor.addWidgetColSize(!isVirtioScsiEnabled ? ColumnSize.SM_7 : ColumnSize.SM_8);
        });

        disk.getIsModelDisabled().getEntityChangedEvent().addListener((ev, sender, args) -> {
            if (disk.getIsModelDisabled().getEntity()) {
                disableWidget(getWidget());
                enableWidget(radioButtonPanel);
                enableWidget(datacenterEditor);
                disk.getDefaultCommand().setIsExecutionAllowed(false);
                disk.setIsChangeable(false);
            } else {
                enableWidget(getWidget());
                disk.getDefaultCommand().setIsExecutionAllowed(true);
                disk.setIsChangeable(true);
                driver.edit(disk);
            }
        });

        radioButtonPanel.addRadioButton(constants.imageDisk(),
                disk.getDisk() == null || disk.getDisk().getDiskStorageType() == DiskStorageType.IMAGE,
                disk.getIsNew() || disk.getDisk().getDiskStorageType() == DiskStorageType.IMAGE,
                event -> {
                    if (disk.getIsNew()) {
                        disk.getDiskStorageType().setEntity(DiskStorageType.IMAGE);
                        revealDiskPanel(disk);
                    }
                });

        radioButtonPanel.addRadioButton(constants.directLunDisk(),
                disk.getDisk() != null && disk.getDisk().getDiskStorageType() == DiskStorageType.LUN,
                disk.getIsNew() || disk.getDisk().getDiskStorageType() == DiskStorageType.LUN,
                event -> {
                    if (disk.getIsNew()) {
                        disk.getDiskStorageType().setEntity(DiskStorageType.LUN);
                        revealStorageView(disk);
                        revealDiskPanel(disk);
                    }
                });

        radioButtonPanel.addRadioButton(constants.managedBlockDisk(),
                disk.getDisk() != null && disk.getDisk().getDiskStorageType() == DiskStorageType.MANAGED_BLOCK_STORAGE,
                disk.getIsNew() || disk.getDisk().getDiskStorageType() == DiskStorageType.MANAGED_BLOCK_STORAGE,
                event -> {
                    if (disk.getIsNew()) {
                        disk.getDiskStorageType().setEntity(DiskStorageType.MANAGED_BLOCK_STORAGE);
                        revealDiskPanel(disk);
                    }
                });

        if (disk.getStorageModel() == null) {
            storageModel = new StorageModel(new NewEditStorageModelBehavior());

            // Create IscsiStorageModel
            iscsiStorageModel = new IscsiStorageModel();
            iscsiStorageModel.setContainer(storageModel);
            iscsiStorageModel.setIsGroupedByTarget(true);
            iscsiStorageModel.setIgnoreGrayedOut(true);

            // Create FcpStorageModel
            fcpStorageModel = new FcpStorageModel();
            fcpStorageModel.setContainer(storageModel);
            fcpStorageModel.setIsGroupedByTarget(false);
            fcpStorageModel.setIgnoreGrayedOut(true);

            // Set 'StorageModel' items
            ArrayList<IStorageModel> items = new ArrayList<>();
            items.add(iscsiStorageModel);
            items.add(fcpStorageModel);
            storageModel.setStorageModels(items);
            storageModel.setHost(disk.getHost());

            disk.setStorageModel(storageModel);
        } else {
            storageModel = disk.getStorageModel();

            iscsiStorageModel = Linq.findByType(storageModel.getStorageModels(), IscsiStorageModel.class);
            iscsiStorageModel.getPropertyChangedEvent().clearListeners();

            fcpStorageModel = Linq.findByType(storageModel.getStorageModels(), FcpStorageModel.class);
            fcpStorageModel.getPropertyChangedEvent().clearListeners();
        }

        iscsiStorageView = new IscsiStorageView(false, 196, 204, 244, 100, 142, 55, -67);
        iscsiStorageView.setBarTop(0, Unit.PX);
        iscsiStorageView.edit(iscsiStorageModel);

        fcpStorageView = new FcpStorageView(false, 278, 240);
        fcpStorageView.edit(fcpStorageModel);

        // SelectedItemChangedEvent handlers
        disk.getStorageType().getSelectedItemChangedEvent().addListener((ev, sender, args) -> revealStorageView(disk));

        disk.getHost().getSelectedItemChangedEvent().addListener((ev, sender, args) -> revealStorageView(disk));

        disk.getDiskStorageType().getPropertyChangedEvent().addListener((ev, sender, args) -> {
            String propName = args.propertyName;
            if ("IsChangable".equals(propName)) { //$NON-NLS-1$
                if (disk.getDiskStorageType().getIsChangable() && disk.isEditEnabled()) {
                    enableWidget(radioButtonPanel);
                } else {
                    disableWidget(radioButtonPanel);
                }
            }
        });

        revealStorageView(disk);
        revealDiskPanel(disk);
    }

    private void revealDiskPanel(final AbstractDiskModel disk) {
        boolean isInVm = disk.getVm() != null;

        // Disk type (internal/external) selection panel is visible only when
        // 'Attach disk' mode is enabled or new LunDisk creation is enabled
        radioButtonPanel.setVisible(isNewLunDiskEnabled);
        externalDiskPanel.setVisible(isNewLunDiskEnabled && disk.getDiskStorageType().getEntity() == DiskStorageType.LUN);

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
            sanStorageModelBase = iscsiStorageModel;
        } else if (storageType == StorageType.FCP) {
            storageView = fcpStorageView;
            sanStorageModelBase = fcpStorageModel;
        }

        storageModel.setCurrentStorageItem(sanStorageModelBase);
        diskModel.setSanStorageModelBase(sanStorageModelBase);

        // Execute 'UpdateCommand' to call 'GetDeviceList'
        sanStorageModelBase.getUpdateCommand().execute();

        sanStorageModelBase.setWidgetModel(diskModel);
        externalDiskPanel.clear();
        externalDiskPanel.add(storageView);
    }

    public boolean handleEnterKeyDisabled() {
        return storageView != null && storageView.isSubViewFocused();
    }

    @Override
    public AbstractDiskModel flush() {
        return driver.flush();
    }

    @Override
    public void cleanup() {
        driver.cleanup();
    }

    @Override
    public int setTabIndexes(int nextTabIndex) {
        sizeEditor.setTabIndex(nextTabIndex++);
        sizeExtendEditor.setTabIndex(nextTabIndex++);
        aliasEditor.setTabIndex(nextTabIndex++);
        descriptionEditor.setTabIndex(nextTabIndex++);
        interfaceEditor.setTabIndex(nextTabIndex++);
        datacenterEditor.setTabIndex(nextTabIndex++);
        storageDomainEditor.setTabIndex(nextTabIndex++);
        volumeTypeEditor.setTabIndex(nextTabIndex++);
        diskProfileEditor.setTabIndex(nextTabIndex++);
        quotaEditor.setTabIndex(nextTabIndex++);
        hostListEditor.setTabIndex(nextTabIndex++);
        hostInfoIcon.setTabIndex(nextTabIndex++);
        storageTypeEditor.setTabIndex(nextTabIndex++);
        plugDiskToVmEditor.setTabIndex(nextTabIndex++);
        wipeAfterDeleteEditor.setTabIndex(nextTabIndex++);
        isBootableEditor.setTabIndex(nextTabIndex++);
        isShareableEditor.setTabIndex(nextTabIndex++);
        isReadOnlyEditor.setTabIndex(nextTabIndex++);
        passDiscardEditor.setTabIndex(nextTabIndex++);
        isScsiPassthroughEditor.setTabIndexes(nextTabIndex++);
        isSgIoUnfilteredEditor.setTabIndex(nextTabIndex++);
        isUsingScsiReservationEditor.setTabIndex(nextTabIndex++);
        isIncrementalBackupEditor.setTabIndex(nextTabIndex++);

        return nextTabIndex;
    }
}

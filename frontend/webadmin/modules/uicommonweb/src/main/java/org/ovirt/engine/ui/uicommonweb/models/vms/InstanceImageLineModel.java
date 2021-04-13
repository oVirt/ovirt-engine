package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.businessentities.BiosType;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.validation.VmActionByVmOriginTypeValidator;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicompat.UIConstants;
import org.ovirt.engine.ui.uicompat.UIMessages;

public class InstanceImageLineModel extends EntityModel {

    public static final String CANCEL_DISK = "CancelDisk"; //$NON-NLS-1$

    public static final String DISK = "_Disk"; //$NON-NLS-1$

    private UIMessages messages = ConstantsManager.getInstance().getMessages();

    private UIConstants constants = ConstantsManager.getInstance().getConstants();

    private UICommand attachCommand;

    private CreateEditCommand createEditCommand;

    private EntityModel<AbstractDiskModel> diskModel = new EntityModel<>();

    // if the disk already exists in the engine or is just created here but not yet submitted
    private boolean diskExists;

    private EntityModel<String> name = new EntityModel<>();

    private InstanceImagesModel parentModel;

    private VM vm;

    private boolean active = true;

    private boolean changed;

    public InstanceImageLineModel(InstanceImagesModel parentModel) {
        this.parentModel = parentModel;

        attachCommand = new UICommand("attachCommand", this); //$NON-NLS-1$
        createEditCommand = new CreateEditCommand("createEditCommand", this); //$NON-NLS-1$
    }

    private void fillData() {
        if (diskModel.getEntity() == null) {
            return;
        }

        if (diskModel.getEntity() instanceof InstanceImagesAttachDiskModel) {
            List<EntityModel<DiskModel>> disks = ((InstanceImagesAttachDiskModel) diskModel.getEntity()).getSelectedDisks();
            if (disks.size() != 0) {
                updateName(disks.get(0).getEntity());
            }
        } else {
            updateName(diskModel.getEntity());
        }
    }

    private void updateName(DiskModel diskModel) {
        Disk disk = diskModel.getDisk();
        if (disk == null) {
            return;
        }

        String diskName = disk.getDiskAlias();
        String size = Long.toString(disk.getSize());

        if (disk.getDiskStorageType() == DiskStorageType.IMAGE
                || disk.getDiskStorageType() == DiskStorageType.MANAGED_BLOCK_STORAGE) {
            size = Long.toString(((DiskImage) disk).getSizeInGigabytes());
        }

        String type;
        if (diskExists) {
            type = constants.existingDisk();
        } else if (getDiskModel().getEntity() instanceof InstanceImagesAttachDiskModel) {
            type = constants.attachingDisk();
        } else {
            type = constants.creatingDisk();
        }

        name.setEntity(messages.vmDialogDisk(diskName, size, type, diskModel.getIsBootable().getEntity() ? constants.bootDisk() : ""));
    }

    private void toggleActive() {
        if (vm != null && !VmActionByVmOriginTypeValidator.isCommandAllowed(vm, ActionType.UpdateDisk)) {
            active = false;
        }
    }

    public void initialize(Disk disk, VM vm) {
        this.vm = vm;
        active = true;
        diskExists = disk != null;

        toggleActive();
        attachCommand.setIsAvailable(!diskExists && active);

        if (disk == null) {
            return;
        }

        final AbstractDiskModel model = new EditVmDiskModel() {
            @Override
            public void onSave() {
                if (validate()) {
                    flush();
                    getDiskModel().setEntity(this);

                    // Flagging the model as 'changed'
                    // Todo: perform a deep differentiation analyze on the disks (original/edited) objects.
                    InstanceImageLineModel.this.setChanged(true);

                    // needed because the "entity" instances are the same so the event is not fired
                    fillData();

                    setDiskModel(null);
                }
            }

            @Override
            public void updateInterface(Version clusterVersion) {
                InstanceImageLineModel.this.updateInterface(clusterVersion, this);
            }

            @Override
            protected void updateBootableDiskAvailable() {
                updateBootableFrom(parentModel.getAllCurrentDisksModels());
            }
        };

        model.setDisk(disk);
        model.setVm(vm);

        setupModelAsDialog(model,
                ConstantsManager.getInstance().getConstants().editVirtualDiskTitle(),
                HelpTag.edit_virtual_disk, "edit_virtual_disk"); //$NON-NLS-1$

        model.initialize();
        diskModel.setEntity(model);
        fillData();
    }

    public EntityModel<AbstractDiskModel> getDiskModel() {
        return diskModel;
    }

    public EntityModel<String> getName() {
        return name;
    }

    public void setName(EntityModel<String> name) {
        this.name = name;
    }

    public boolean isGhost() {
        return diskModel.getEntity() == null;
    }

    public void attachDisk() {
        if (parentModel.getUnitVmModel().getSelectedCluster() == null || parentModel.getUnitVmModel().getSelectedDataCenter() == null) {
            return;
        }

        InstanceImagesAttachDiskModel model = new InstanceImagesAttachDiskModel() {
            @Override
            public void onSave() {

                if (validate()) {
                    flush();
                    List<EntityModel<DiskModel>> selectedDisks = getSelectedDisks();
                    if (selectedDisks.size() == 1) {
                        // only 0 or 1 is allowed
                        setDisk(selectedDisks.iterator().next().getEntity().getDisk());
                    }

                    getDiskModel().setEntity(this);
                    InstanceImageLineModel.this.setChanged(true);

                    setDiskModel(null);
                    // from now on only editing is possible
                    attachCommand.setIsAvailable(false);

                    fillData();
                }
            }

            @Override
            public void updateInterface(Version clusterVersion) {
                InstanceImageLineModel.this.updateInterface(clusterVersion, this);
            }

            @Override
            protected void updateBootableDiskAvailable() {
                updateBootableFrom(parentModel.getAllCurrentDisksModels());
            }

            @Override
            protected List<Disk> getAttachedNotSubmittedDisks() {
                return parentModel.getNotYetAttachedNotAttachableDisks();
            }
        };

        VM realOrFakeVm = vm;
        Version compatibilityVersion = parentModel.getUnitVmModel().getSelectedCluster().getCompatibilityVersion();
        BiosType biosType = parentModel.getUnitVmModel().getSelectedCluster().getBiosType();
        if (realOrFakeVm == null) {
            realOrFakeVm = new VM();
            realOrFakeVm.setId(null);
            realOrFakeVm.setClusterId(parentModel.getUnitVmModel().getSelectedCluster().getId());
            realOrFakeVm.setStoragePoolId(parentModel.getUnitVmModel().getSelectedDataCenter().getId());
            realOrFakeVm.setClusterCompatibilityVersion(compatibilityVersion);
            realOrFakeVm.setBiosType(parentModel.getUnitVmModel().getBiosType().getSelectedItem());
            realOrFakeVm.setClusterBiosType(biosType);
        }

        model.setVm(realOrFakeVm);

        setupModelAsDialog(model,
                ConstantsManager.getInstance().getConstants().attachVirtualDiskTitle(),
                HelpTag.attach_virtual_disk, "attach_virtual_disk"); //$NON-NLS-1$
        setDiskModel(model);
        model.initialize(parentModel.getAllCurrentDisksModels());
        maybeLoadAttachableDisks(model);
    }

    private void maybeLoadAttachableDisks(InstanceImagesAttachDiskModel model) {
        if (model.getVm().getId() == null) {
            Integer osType = parentModel.getUnitVmModel().getOSType().getSelectedItem();
            Version compatibilityVersion = parentModel.getUnitVmModel().getSelectedCluster().getCompatibilityVersion();
            model.loadAttachableDisks(osType, compatibilityVersion, getDisk());
        } else {
            model.loadAttachableDisks(getDisk());
        }
    }

    public void createEditDisk() {
        if (parentModel.getUnitVmModel().getSelectedCluster() == null || parentModel.getUnitVmModel().getSelectedDataCenter() == null) {
            return;
        }

        if (getDiskModel().getEntity() == null) {
            showNewDialog();
        } else {
            showPreviouslyShownDialog();
        }
    }

    private void showPreviouslyShownDialog() {
        getDiskModel().getEntity().updateBootableFrom(parentModel.getAllCurrentDisksModels());
        if (getDiskModel().getEntity() instanceof InstanceImagesAttachDiskModel) {
            // needed to re-filter in case the OS or the compatibility version changed
            maybeLoadAttachableDisks((InstanceImagesAttachDiskModel) getDiskModel().getEntity());
        }
        setDiskModel(getDiskModel().getEntity());
    }

    private void showNewDialog() {
        final AbstractDiskModel model = new NewDiskModel() {
            @Override
            public void onSave() {
                if (validate()) {
                    flush();
                    getDiskModel().setEntity(this);
                    InstanceImageLineModel.this.setChanged(true);

                    setDiskModel(null);
                    // the "new" turns into "edit" - no need for attach anymore
                    attachCommand.setIsAvailable(false);

                    fillData();

                    Disk disk = super.getDisk();
                    if (disk.getDiskStorageType() == DiskStorageType.IMAGE
                            || disk.getDiskStorageType() == DiskStorageType.MANAGED_BLOCK_STORAGE) {
                        ((DiskImage) disk).setActive(true);
                    }
                }
            }

            @Override
            public void updateInterface(Version clusterVersion) {
                InstanceImageLineModel.this.updateInterface(clusterVersion, this);
            }

            @Override
            protected void updateBootableDiskAvailable() {
                updateBootableFrom(parentModel.getAllCurrentDisksModels());
            }
        };

        VM vm = new VM();
        vm.setClusterId(parentModel.getUnitVmModel().getSelectedCluster().getId());
        vm.setStoragePoolId(parentModel.getUnitVmModel().getSelectedDataCenter().getId());
        vm.setClusterCompatibilityVersion(parentModel.getUnitVmModel().getSelectedCluster().getCompatibilityVersion());
        vm.setBiosType(parentModel.getUnitVmModel().getBiosType().getSelectedItem());
        vm.setClusterBiosType(parentModel.getUnitVmModel().getSelectedCluster().getBiosType());
        vm.setVmOs(parentModel.getUnitVmModel().getOSType().getSelectedItem());

        Quota selectedQuota = parentModel.getUnitVmModel().getQuota().getSelectedItem();
        vm.setQuotaId(selectedQuota == null ? null : selectedQuota.getId());
        model.setVm(vm);
        model.getSizeExtend().setIsAvailable(false);

        setupModelAsDialog(model,
                ConstantsManager.getInstance().getConstants().newVirtualDiskTitle(),
                HelpTag.new_virtual_disk, "new_virtual_disk"); //$NON-NLS-1$

        setDiskModel(model);

        model.initialize(parentModel.getAllCurrentDisksModels());

        if (model.getIsBootable().getIsChangable()) {
            model.getIsBootable().setEntity(true);
        }

        if (getVm() != null) {
            model.setVm(getVm());
            ((NewDiskModel)model).updateSuggestedDiskAliasFromServer();
        } else {
            String currentVmName = parentModel.getUnitVmModel().getName().getEntity();
            if (StringHelper.isNotNullOrEmpty(currentVmName)) {
                // if already set the VM name on the new VM dialog, suggest the name according to the name
                model.getAlias().setEntity(suggestAliasForNewVm(currentVmName));
            }
        }
    }

    private String suggestAliasForNewVm(String currentVmName) {
        Set<String> aliases = createDiskAliasesList();
        String suggestedAlias;
        int i = 0;
        do {
            i++;
            suggestedAlias = currentVmName + DISK + i;
        } while (aliases.contains(suggestedAlias));

        return suggestedAlias;
    }

    private Set<String> createDiskAliasesList() {
        Set<String> res = new HashSet<>();
        for (Disk disk : parentModel.getAllCurrentDisks()) {
            res.add(disk.getDiskAlias());
        }

        return res;
    }

    private void setupModelAsDialog(AbstractDiskModel model, String title, HelpTag helpTag, String hashName) {
        model.setTitle(title);
        model.setHelpTag(helpTag);
        model.setHashName(hashName);

        UICommand cancelCommand = new UICommand(CANCEL_DISK, this);
        cancelCommand.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        cancelCommand.setIsCancel(true);
        model.setCancelCommand(cancelCommand);
    }

    private void setDiskModel(AbstractDiskModel model) {
        if (parentModel.getParentListModel() instanceof HasDiskWindow) {
            ((HasDiskWindow) parentModel.getParentListModel()).setDiskWindow(model);
        }
    }

    @Override
    public void executeCommand(UICommand command) {
        if (!active) {
            // don't listen to this commands anymore - no need to show any more windows
            return;
        }
        if (CANCEL_DISK.equals(command.getName())) {
            setDiskModel(null);
        } else if (command == createEditCommand) {
            createEditDisk();
        } else if (command == attachCommand) {
            attachDisk();
        } else {
            super.executeCommand(command);
        }
    }

    public boolean isBootable() {
        if (isGhost()) {
            return false;
        }

        return diskModel.getEntity().getIsBootable().getEntity();
    }

    public Disk getDisk() {
        AbstractDiskModel diskModel = getDiskModel().getEntity();

        if (diskModel == null) {
            return null;
        }

        if (diskModel.getDisk() != null) {
            return diskModel.getDisk();
        }

        DiskStorageType diskStorageType = diskModel.getDiskStorageType().getEntity();

        if (diskStorageType == DiskStorageType.IMAGE) {
            return diskModel.getDiskImage();
        }

        if (diskStorageType == DiskStorageType.LUN) {
            return diskModel.getLunDisk();
        }

        return null;
    }

    public boolean isDiskExists() {
        return diskExists;
    }

    public VM getVm() {
        return vm;
    }

    public UICommand getAttachCommand() {
        return attachCommand;
    }

    public CreateEditCommand getCreateEditCommand() {
        return createEditCommand;
    }


    public void setEnabled(boolean enabled) {
        attachCommand.setIsExecutionAllowed(enabled);
        createEditCommand.setIsExecutionAllowed(enabled);
    }

    public void deactivate() {
        active = false;
    }

    public void updateInterface(Version clusterVersion, AbstractDiskModel model) {
        model.getIsVirtioScsiEnabled().setEntity(Boolean.TRUE.equals(parentModel.getUnitVmModel().getIsVirtioScsiEnabled().getEntity()));
        model.updateInterfaceList(clusterVersion);
    }

    public boolean isChanged() {
        return changed;
    }

    public void setChanged(boolean changed) {
        this.changed = changed;
    }

    public boolean isCreateAllowed() {
        return parentModel.getUnitVmModel() == null || parentModel.getUnitVmModel().getSelectedCluster() == null
                || parentModel.getUnitVmModel().getSelectedCluster().isManaged();
    }

    public boolean isEditAllowed() {
        return true;
    }

    public static class CreateEditCommand extends UICommand {

        private boolean createAllowed = true;

        private boolean editAllowed = true;

        public CreateEditCommand(String name, InstanceImageLineModel target) {
            super(name, target);
        }

        public boolean isCreateAllowed() {
            return createAllowed;
        }

        public void setCreateAllowed(boolean value) {
            if (!Objects.equals(createAllowed, value)) {
                createAllowed = value;
                onPropertyChanged(new PropertyChangedEventArgs("CreateAllowed")); //$NON-NLS-1$
            }
        }

        public boolean isEditAllowed() {
            return editAllowed;
        }

        public void setEditAllowed(boolean value) {
            if (!Objects.equals(editAllowed, value)) {
                editAllowed = value;
                onPropertyChanged(new PropertyChangedEventArgs("EditAllowed")); //$NON-NLS-1$
            }
        }
    }
}

package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.validation.VmActionByVmOriginTypeValidator;
import org.ovirt.engine.ui.uicommonweb.ICommandTarget;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;

public class InstanceImagesModel extends ListModel<InstanceImageLineModel> {

    private Model parentListModel;

    private UnitVmModel unitVmModel;

    private List<RemoveDiskModel> removeDiskModels = new ArrayList<>();

    private RemoveDiskModel removeDiskModel;

    private RemoveApprovedCallback callback;

    private VM vm;

    public InstanceImagesModel(UnitVmModel unitVmModel, Model parentListModel) {
        this.parentListModel = parentListModel;
        this.unitVmModel = unitVmModel;
    }

    public Model getParentListModel() {
        return parentListModel;
    }

    public UnitVmModel getUnitVmModel() {
        return unitVmModel;
    }

    public void approveRemoveDisk(InstanceImageLineModel lineModel, RemoveApprovedCallback callback) {
        if (!lineModel.isDiskExists() || lineModel.getVm() == null) {
            // no problem, the item can be removed without explicitly asking the user
            callback.removeApproved(true);
            return;
        }

        this.callback = callback;
        removeDiskModel = new RemoveDiskModel();

        List<Disk> disksToRemove = Arrays.asList(lineModel.getDiskModel().getEntity().getDisk());
        VM vm = lineModel.getVm();

        if (getParentListModel() instanceof HasDiskWindow) {
            ((HasDiskWindow) getParentListModel()).setDiskWindow(removeDiskModel);
        }

        removeDiskModel.initialize(vm, disksToRemove, this);
    }

    private void onRemove() {
        if (!removeDiskModel.validate()) {
            return;
        }

        removeDiskModels.add(removeDiskModel);
        callback.removeApproved(true);
        hideRemoveDiskAndShowEditVm();
    }

    private void onCancel() {
        callback.removeApproved(false);
        hideRemoveDiskAndShowEditVm();
    }

    private void hideRemoveDiskAndShowEditVm() {
        if (getParentListModel() instanceof HasDiskWindow) {
            ((HasDiskWindow) getParentListModel()).setDiskWindow(null);
        }
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (RemoveDiskModel.ON_REMOVE.equals(command.getName())) {
            onRemove();
        } else if (RemoveDiskModel.CANCEL_REMOVE.equals(command.getName())) {
            onCancel();
        }
    }

    public void setVm(VM vm) {
        this.vm = vm;
    }

    public VM getVm() {
        return vm;
    }

    public static interface RemoveApprovedCallback {
        void removeApproved(boolean approved);
    }

    public void executeDiskModifications(VM vm) {
        // this is done on the background - the window is not visible anymore
        disableLineModels();

        if (isDiskUpdateAllowed(vm)) {
            executeDeleteAndCallNew(vm);
        }
    }

    private boolean isDiskUpdateAllowed(VM vm) {
        return VmActionByVmOriginTypeValidator.isCommandAllowed(vm, ActionType.UpdateDisk);
    }

    private void disableLineModels() {
        for (InstanceImageLineModel model : getItems()) {
            model.deactivate();
        }
    }

    private void executeDeleteAndCallNew(final VM vm) {
        if (removeDiskModels.size() == 0) {
            executeNewAndEdit(vm);
            return;
        }

        for (RemoveDiskModel removeDisk : removeDiskModels ) {
            removeDisk.onRemove(new ICommandTarget() {
                @Override
                public void executeCommand(UICommand command) {
                    executeNewAndEdit(vm);
                }

                @Override
                public void executeCommand(UICommand uiCommand, Object... parameters) {
                    executeNewAndEdit(vm);
                }
            });
        }
    }

    private void executeNewAndEdit(final VM vm) {
        if (getItems() == null) {
            return;
        }

        AsyncDataProvider.getInstance().getVmDiskList(new AsyncQuery<>(disks -> {
            Iterator<InstanceImageLineModel> lineModelIterator = orderedDisksIterator(disks, vm);
            storeNextDisk(lineModelIterator, vm);
        }), vm.getId());
    }

    /**
     * Finds the disk which is boot on the VM but has been configured to be non boot. If finds such a disk, the resulting
     * list will contain as a first command the one which executes this operation.
     *
     * It is needed because they can be other which make an another disk boot and the VM can not have more than one boot
     * disk - so the validation on server would fail.
     */
    private Iterator<InstanceImageLineModel> orderedDisksIterator(List<Disk> disks, VM vm) {
        if (disks.size() == 0) {
            return getItems().iterator();
        }

        Disk previouslyBootDisk = findBoot(disks, vm);
        if (previouslyBootDisk == null) {
            return getItems().iterator();
        }

        InstanceImageLineModel fromBootToNonBoot = findBecomeNonBoot(previouslyBootDisk);
        if (fromBootToNonBoot == null) {
            return getItems().iterator();
        }

        // now we know that the disk changed from boot to non boot so this command has to be executed as first
        Set<InstanceImageLineModel> res = new LinkedHashSet<>();
        res.add(fromBootToNonBoot);
        res.addAll(getItems());
        return res.iterator();
    }

    private InstanceImageLineModel findBecomeNonBoot(Disk bootDisk) {
        for (InstanceImageLineModel model : getItems()) {
            if (model.isGhost()) {
                // this is not an actual item
                continue;
            }

            Disk disk = model.getDiskModel().getEntity().getDisk();
            if (disk.getId() == null) {
                // does not yet exist on the server so no need to edit it
                continue;
            }
            if (disk.getId().equals(bootDisk.getId())) {
                if (disk.getDiskVmElementForVm(getVm().getId()).isBoot()) {
                    return null;
                } else {
                    // removed boot flag, this command has to be executed first so if other disk is marked as boot,
                    // it will not fail with the message that you can have only one boot
                    return model;
                }
            }
        }

        return null;
    }

    private Disk findBoot(List<Disk> disks, VM vm) {
        for (Disk disk : disks) {
            DiskVmElement dve = disk.getDiskVmElementForVm(vm.getId());
            if (dve != null && dve.isBoot()) {
                return disk;
            }
        }

        return null;
    }

    private void storeNextDisk(final Iterator<InstanceImageLineModel> lineModelIterator, final VM vm) {
        if (!lineModelIterator.hasNext()) {
            return;
        }

        InstanceImageLineModel instanceImageLineModel = lineModelIterator.next();
        AbstractDiskModel diskModel = instanceImageLineModel.getDiskModel().getEntity();

        if (diskModel == null || !instanceImageLineModel.isChanged()) {
            storeNextDisk(lineModelIterator, vm);
        } else {
            diskModel.setVm(vm);
            diskModel.getDiskVmElement().getId().setVmId(vm.getId());
            diskModel.store(result -> {
                // have to wait until the previous returned because the operation needs a lock on the VM
                storeNextDisk(lineModelIterator, vm);
            });
        }

    }

    public List<Disk> getAllCurrentDisks() {
        if (getItems() == null) {
            return Collections.emptyList();
        }
        List<Disk> res = new ArrayList<>();
        for (InstanceImageLineModel line : getItems()) {
            if (line.isGhost()) {
                continue;
            }

            res.add(line.getDisk());
        }

        return res;
    }

    public List<DiskModel> getAllCurrentDisksModels() {
        if (getItems() == null) {
            return Collections.emptyList();
        }
        List<DiskModel> diskModels = new ArrayList<>();
        for (InstanceImageLineModel line : getItems()) {
            if (line.isGhost()) {
                continue;
            }

            diskModels.add(line.getDiskModel().getEntity());
        }

        return diskModels;
    }

    /**
     * Returns a list of non-sharable disks which have been set as to attach in the new/edit VM dialog but the dialog has not yet been submitted
     */
    public List<Disk> getNotYetAttachedNotAttachableDisks() {
        List<Disk> res = new ArrayList<>();
        for (InstanceImageLineModel line : getItems()) {
            if (line.isGhost()) {
                continue;
            }

            EntityModel<AbstractDiskModel> diskModel = line.getDiskModel();
            if (diskModel == null) {
                continue;
            }

            // it will be InstanceImagesAttachDiskModel only if not yet submitted
            if (!(diskModel.getEntity() instanceof InstanceImagesAttachDiskModel)) {
                continue;
            }

            Disk disk = line.getDisk();
            if (disk == null || disk.isShareable()) {
                continue;
            }

            res.add(disk);

        }

        return res;
    }

    public void updateActionsAvailability() {
        boolean clusterSelected = unitVmModel.getSelectedCluster() != null;
        boolean isKubevirt = clusterSelected && !unitVmModel.getSelectedCluster().isManaged();
        boolean osSelected = unitVmModel.getOSType().getSelectedItem() != null;

        if (getItems() == null) {
            return;
        }

        for (InstanceImageLineModel model : getItems()) {
            model.getCreateEditCommand().setCreateAllowed(!isKubevirt);
            model.getCreateEditCommand().setEditAllowed(true);
            if (model.isGhost()) {
                continue;
            }

            model.setEnabled(clusterSelected && osSelected);
        }

        setIsChangeable(clusterSelected && osSelected);
    }
}

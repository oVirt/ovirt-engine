package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AttachDetachVmDiskParameters;
import org.ovirt.engine.core.common.action.RemoveDiskParameters;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.ICommandTarget;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class RemoveDiskModel extends ConfirmationModel {

    public static final String ON_REMOVE = "OnRemoveDisk"; //$NON-NLS-1$

    public static final String CANCEL_REMOVE = "CancelRemoveDisk"; //$NON-NLS-1$

    private VM vm;
    private List<Disk> disksToRemove;

    private UICommand cancelCommand;

    public RemoveDiskModel() {
        setLatch(new EntityModel<>());
        getLatch().setIsAvailable(true);
    }

    public void initialize(VM vm, List<Disk> disksToRemove, ICommandTarget target) {
        // the VM can be null if the disks do not belong to a specific VM
        this.vm = vm;
        this.disksToRemove = disksToRemove;

        setTitle(ConstantsManager.getInstance().getConstants().removeDisksTitle());
        setHelpTag(HelpTag.remove_disk);
        setHashName("remove_disk"); //$NON-NLS-1$

        getLatch().setEntity(false);

        List<DiskModel> items = new ArrayList<>();
        for (Disk disk : disksToRemove) {
            DiskModel diskModel = new DiskModel();
            diskModel.setDisk(disk);
            diskModel.setVm(vm);

            items.add(diskModel);

            // A shared disk or a disk snapshot can only be detached
            if (disk.getNumberOfVms() > 1) {
                getLatch().setIsChangeable(false);
            }
        }
        setItems(items);

        UICommand tempVar = UICommand.createDefaultOkUiCommand(ON_REMOVE, target);
        getCommands().add(tempVar);
        cancelCommand = UICommand.createCancelUiCommand(CANCEL_REMOVE, target);
        getCommands().add(cancelCommand);
    }

    public void onRemove(final ICommandTarget target) {
        boolean removeDisk = getLatch().getEntity();
        ActionType actionType = removeDisk ? ActionType.RemoveDisk : ActionType.DetachDiskFromVm;

        List<ActionParametersBase> parameterList = disksToRemove.stream()
                .map(disk -> removeDisk ? new RemoveDiskParameters(disk.getId()) :
                        new AttachDetachVmDiskParameters(new DiskVmElement(disk.getId(), vm.getId())))
                .collect(Collectors.toList());

        startProgress();

        Frontend.getInstance().runMultipleAction(actionType, parameterList,
                result -> {
                    stopProgress();
                    target.executeCommand(cancelCommand);
                },
                this);
    }

    @Override
    public boolean validate() {
        return true;
    }

}

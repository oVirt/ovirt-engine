package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.action.AttachDetachVmDiskParameters;
import org.ovirt.engine.core.common.action.RemoveDiskParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
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
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;

@SuppressWarnings("unused")
public class RemoveDiskModel extends ConfirmationModel {

    public static final String ON_REMOVE = "OnRemoveDisk";

    public static final String CANCEL_REMOVE = "CancelRemoveDisk";

    private VM vm;
    private List<Disk> disksToRemove;

    private UICommand cancelCommand;

    public RemoveDiskModel() {
        setLatch(new EntityModel<Boolean>());
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

        ArrayList<DiskModel> items = new ArrayList<>();
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
        VdcActionType actionType = removeDisk ? VdcActionType.RemoveDisk : VdcActionType.DetachDiskFromVm;
        ArrayList<VdcActionParametersBase> paramerterList = new ArrayList<>();

        for (Disk disk : disksToRemove) {
            VdcActionParametersBase parameters = removeDisk ?
                    new RemoveDiskParameters(disk.getId()) :
                    new AttachDetachVmDiskParameters(new DiskVmElement(disk.getId(), vm.getId()));
            paramerterList.add(parameters);
        }

        startProgress();

        Frontend.getInstance().runMultipleAction(actionType, paramerterList,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void executed(FrontendMultipleActionAsyncResult result) {
                        stopProgress();
                        target.executeCommand(cancelCommand);
                    }
                },
                this);
    }

    @Override
    public boolean validate() {
        return true;
    }

}

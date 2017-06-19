package org.ovirt.engine.ui.uicommonweb.models.storage;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.RegisterDiskParameters;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.ICommandTarget;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.vms.DiskModel;

public class RegisterDiskModel extends DisksAllocationModel {

    public void init() {
        ICommandTarget target = (ICommandTarget) getEntity();

        UICommand actionCommand = new UICommand("OnExecute", this); //$NON-NLS-1$
        actionCommand.setTitle(constants.ok());
        actionCommand.setIsDefault(true);
        getCommands().add(actionCommand);
        UICommand cancelCommand = new UICommand("Cancel", target); //$NON-NLS-1$
        cancelCommand.setTitle(constants.cancel());
        cancelCommand.setIsCancel(true);
        getCommands().add(cancelCommand);
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if ("OnExecute".equals(command.getName())) { //$NON-NLS-1$
            onExecute();
        }
    }

    void updateStorageDomain(StorageDomain storageDomain) {
        for (DiskModel diskModel : getDisks()) {
            diskModel.getStorageDomain().setItems(Collections.singletonList(storageDomain));
        }
    }

    private void onExecute() {
        if (getProgress() != null) {
            return;
        }

        startProgress();

        for (DiskModel item : getDisks()) {
            DiskImage disk = (DiskImage) item.getDisk();
            if (item.getQuota().getSelectedItem() != null) {
                disk.setQuotaId(item.getQuota().getSelectedItem().getId());
            }
            RegisterDiskParameters registerDiskParams =
                    new RegisterDiskParameters(disk, disk.getStorageIds().get(0));
            registerDiskParams.setRefreshFromStorage(true);
            Frontend.getInstance().runMultipleAction(ActionType.RegisterDisk, Arrays.asList(registerDiskParams));
        }

        stopProgress();
        getCancelCommand().execute();
    }

    @Override
    protected void setDefaultVolumeInformationSelection(List<DiskModel> diskModels) {
        // do nothing
    }
}

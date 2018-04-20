package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.GuideModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.UIConstants;

public class VmGuideModel extends GuideModel<VM> {
    public final UIConstants constants = ConstantsManager.getInstance().getConstants();

    @Override
    protected void onEntityChanged() {
        super.onEntityChanged();

        if (getEntity() != null) {
            startProgress();
            AsyncDataProvider.getInstance().getVmDiskList(new AsyncQuery<>(disks -> updateOptions(!disks.isEmpty())), getEntity().getId());
        }
    }

    public void updateOptions(boolean containsDisks) {
        getCompulsoryActions().clear();
        getOptionalActions().clear();
        startProgress();

        // Add disk action.
        UICommand newDiskAction = new UICommand("NewDisk", this); //$NON-NLS-1$
        newDiskAction.setTitle(constants.vmCreateVirtualDiskAction());

        UICommand attachDiskAction = new UICommand("AttachDisk", this); //$NON-NLS-1$
        attachDiskAction.setTitle(constants.vmAttachVirtualDisksAction());

        if (!containsDisks) {
            getCompulsoryActions().add(newDiskAction);
            getCompulsoryActions().add(attachDiskAction);
        } else {
            getOptionalActions().add(newDiskAction);
            getOptionalActions().add(attachDiskAction);
        }

        stopProgress();
    }

    public void newDisk() {
        if (getEntity() == null) {
            return;
        }

        addDisk(new NewDiskGuideModel(this),
                ConstantsManager.getInstance().getConstants().newVirtualDiskTitle(),
                HelpTag.new_virtual_disk, "new_virtual_disk"); //$NON-NLS-1$
    }

    public void attachDisk() {
        if (getEntity() == null) {
            return;
        }

        addDisk(new AttachDiskGuideModel(this),
                ConstantsManager.getInstance().getConstants().attachVirtualDiskTitle(),
                HelpTag.attach_virtual_disk, "attach_virtual_disk"); //$NON-NLS-1$
    }

    private void addDisk(AbstractDiskModel model, String title, HelpTag helpTag, String hashName) {
        model.setTitle(title);
        model.setHelpTag(helpTag);
        model.setHashName(hashName); //$NON-NLS-1$
        model.setVm(getEntity());
        setWindow(model);

        UICommand cancelCommand = UICommand.createCancelUiCommand("Cancel", this); //$NON-NLS-1$
        model.setCancelCommand(cancelCommand);

        model.initialize();
    }

    protected void cancel() {
        setWindow(null);
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if ("NewDisk".equals(command.getName())) { //$NON-NLS-1$
            newDisk();
        } else if ("AttachDisk".equals(command.getName())) { //$NON-NLS-1$
            attachDisk();
        } else if ("Cancel".equals(command.getName())) { //$NON-NLS-1$
            cancel();
        }
    }
}

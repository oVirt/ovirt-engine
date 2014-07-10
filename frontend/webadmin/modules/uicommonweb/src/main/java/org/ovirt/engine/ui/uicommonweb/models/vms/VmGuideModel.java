package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.Collection;

import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.GuideModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class VmGuideModel extends GuideModel
{
    public final String VmConfigureVirtualDisksAction = ConstantsManager.getInstance()
            .getConstants()
            .vmConfigureVirtualDisksAction();
    public final String VmAddAnotherVirtualDiskAction = ConstantsManager.getInstance()
            .getConstants()
            .vmAddAnotherVirtualDiskAction();

    @Override
    public VM getEntity()
    {
        return (VM) super.getEntity();
    }

    @Override
    protected void onEntityChanged()
    {
        super.onEntityChanged();

        if (getEntity() != null) {
            startProgress(null);
            AsyncDataProvider.getInstance().getVmDiskList(new AsyncQuery(this,  new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {
                        Collection<Disk> disks = (Collection<Disk>) returnValue;
                        updateOptions(!disks.isEmpty());
                    }
                }), getEntity().getId());
        }
    }

    public void updateOptions(boolean containsDisks) {
        getCompulsoryActions().clear();
        getOptionalActions().clear();
        startProgress(null);

        // Add disk action.
        UICommand addDiskAction = new UICommand("AddDisk", this); //$NON-NLS-1$

        if (!containsDisks)
        {
            addDiskAction.setTitle(VmConfigureVirtualDisksAction);
            getCompulsoryActions().add(addDiskAction);
        }
        else
        {
            addDiskAction.setTitle(VmAddAnotherVirtualDiskAction);
            getOptionalActions().add(addDiskAction);
        }

        stopProgress();
    }

    public void addDisk()
    {
        if (getEntity() == null) {
            return;
        }

        NewDiskModel model = new NewGuideDiskModel(this);
        model.setTitle(ConstantsManager.getInstance().getConstants().addVirtualDiskTitle());
        model.setHelpTag(HelpTag.new_virtual_disk);
        model.setHashName("new_virtual_disk"); //$NON-NLS-1$
        model.setVm(getEntity());
        setWindow(model);

        UICommand cancelCommand = new UICommand("Cancel", this); //$NON-NLS-1$
        cancelCommand.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        cancelCommand.setIsCancel(true);
        model.setCancelCommand(cancelCommand);

        model.initialize();
    }

    public void cancel()
    {
        setWindow(null);
        Frontend.getInstance().unsubscribe();
    }

    @Override
    public void executeCommand(UICommand command)
    {
        super.executeCommand(command);

        if ("AddDisk".equals(command.getName())) //$NON-NLS-1$
        {
            addDisk();
        }
        if ("Cancel".equals(command.getName())) //$NON-NLS-1$
        {
            cancel();
        }
    }
}

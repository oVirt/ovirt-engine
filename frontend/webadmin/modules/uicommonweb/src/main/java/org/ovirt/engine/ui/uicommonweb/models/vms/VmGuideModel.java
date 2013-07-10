package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.Collection;

import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
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
        updateOptions();
    }

    private void updateOptionsPostData(Collection<Disk> disks) {
        // Add disk action.
        UICommand addDiskAction = new UICommand("AddDisk", this); //$NON-NLS-1$

        if (disks.isEmpty())
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

    public void updateOptions()
    {
        getCompulsoryActions().clear();
        getOptionalActions().clear();

        if (getEntity() != null)
        {
            startProgress(null);

            AsyncDataProvider.getVmDiskList(new AsyncQuery(this,
                    new INewAsyncCallback() {
                        @Override
                        public void onSuccess(Object target, Object returnValue) {
                            updateOptionsPostData((Collection<Disk>) returnValue);
                        }
                    }), getEntity().getId());
        }
    }

    public void addDisk()
    {
        if (getEntity() == null) {
            return;
        }

        NewDiskModel model = new NewGuideDiskModel(this);
        model.setTitle(ConstantsManager.getInstance().getConstants().addVirtualDiskTitle());
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

        if (StringHelper.stringsEqual(command.getName(), "AddDisk")) //$NON-NLS-1$
        {
            addDisk();
        }
        if (StringHelper.stringsEqual(command.getName(), "Cancel")) //$NON-NLS-1$
        {
            cancel();
        }
    }
}

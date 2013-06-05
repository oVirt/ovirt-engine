package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.GuideModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

@SuppressWarnings("unused")
public class VmGuideModel extends GuideModel
{
    public final String VmConfigureNetworkInterfacesAction = ConstantsManager.getInstance()
            .getConstants()
            .vmConfigureNetworkInterfacesAction();
    public final String VmAddAnotherNetworkInterfaceAction = ConstantsManager.getInstance()
            .getConstants()
            .vmAddAnotherNetworkInterfaceAction();
    public final String VmConfigureVirtualDisksAction = ConstantsManager.getInstance()
            .getConstants()
            .vmConfigureVirtualDisksAction();
    public final String VmAddAnotherVirtualDiskAction = ConstantsManager.getInstance()
            .getConstants()
            .vmAddAnotherVirtualDiskAction();

    private ArrayList<VmNetworkInterface> nics;
    private ArrayList<Disk> disks;

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

    private void updateOptionsData() {
        nics = null;
        disks = null;
        AsyncDataProvider.getVmNicList(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {
                        VmGuideModel vmGuideModel = (VmGuideModel) target;
                        ArrayList<VmNetworkInterface> nics =
                                (ArrayList<VmNetworkInterface>) returnValue;
                        vmGuideModel.nics = nics;
                        vmGuideModel.updateOptionsPostData();
                    }
                }), getEntity().getId());
        AsyncDataProvider.getVmDiskList(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {
                        VmGuideModel vmGuideModel = (VmGuideModel) target;
                        ArrayList<Disk> disks = (ArrayList<Disk>) returnValue;
                        vmGuideModel.disks = disks;
                        vmGuideModel.updateOptionsPostData();
                    }
                }), getEntity().getId());
    }

    private void updateOptionsPostData() {
        if (nics == null || disks == null) {
            return;
        }

        // Add NIC action.
        UICommand addNicAction = new UICommand("AddNetwork", this); //$NON-NLS-1$

        if (nics.isEmpty())
        {
            addNicAction.setTitle(VmConfigureNetworkInterfacesAction);
            getCompulsoryActions().add(addNicAction);
        }
        else
        {
            addNicAction.setTitle(VmAddAnotherNetworkInterfaceAction);
            getOptionalActions().add(addNicAction);
        }

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

    private void updateOptions()
    {
        getCompulsoryActions().clear();
        getOptionalActions().clear();

        if (getEntity() != null)
        {
            startProgress(null);

            updateOptionsData();
        }
    }

    public void resetData() {
        nics = null;
        disks = null;
    }

    private void addNetworkUpdateData() {
        nics = null;
        AsyncDataProvider.getVmNicList(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {
                        VmGuideModel vmGuideModel = (VmGuideModel) target;
                        ArrayList<VmNetworkInterface> nics =
                                (ArrayList<VmNetworkInterface>) returnValue;
                        vmGuideModel.nics = nics;
                        vmGuideModel.addNetworkPostData();
                    }
                }), getEntity().getId());
    }

    private void addNetworkPostData() {
        if (nics == null) {
            return;
        }

        VmInterfaceModel model =
                NewVmInterfaceModel.createInstance(getEntity().getStaticData(),
                        getEntity().getVdsGroupCompatibilityVersion(),
                        nics,
                        this);
        setWindow(model);

        stopProgress();
    }

    public void addNetwork()
    {
        if (getEntity() != null)
        {
            startProgress(null);

            addNetworkUpdateData();
        }
    }

    public void addDisk()
    {
        if (getEntity() == null) {
            return;
        }

        NewDiskModel model = new NewDiskModel();
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

    public void postAction()
    {
        resetData();
        updateOptions();
    }

    public void cancel()
    {
        resetData();
        setWindow(null);
        Frontend.Unsubscribe();
    }

    @Override
    public void executeCommand(UICommand command)
    {
        super.executeCommand(command);

        if (StringHelper.stringsEqual(command.getName(), "AddNetwork")) //$NON-NLS-1$
        {
            addNetwork();
        }
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

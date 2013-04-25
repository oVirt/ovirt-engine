package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.action.RemoveVmInterfaceParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;

public class RemoveVmInterfaceModel extends ConfirmationModel {

    private final List<VmNetworkInterface> vnics;
    private final boolean fullMsg;
    private final ListModel sourceListModel;

    public RemoveVmInterfaceModel(ListModel sourceListModel, List<VmNetworkInterface> vnics, boolean isFullMsg) {
        setTitle(ConstantsManager.getInstance().getConstants().removeNetworkInterfacesTitle());
        setHashName("remove_network_interface_vms"); //$NON-NLS-1$
        setMessage(ConstantsManager.getInstance().getConstants().networkInterfacesMsg());

        this.sourceListModel = sourceListModel;
        this.vnics = vnics;
        this.fullMsg = isFullMsg;

        ArrayList<String> items = new ArrayList<String>();
        for (VmNetworkInterface vnic : vnics)
        {
            if (isFullMsg) {
                items.add(getRemoveVnicFullMsg(vnic));
            } else {
                items.add(vnic.getName());
            }
        }
        setItems(items);

        UICommand tempVar = new UICommand("OnRemove", this); //$NON-NLS-1$
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
        tempVar.setIsDefault(true);
        getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("Cancel", this); //$NON-NLS-1$
        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        tempVar2.setIsCancel(true);
        getCommands().add(tempVar2);
    }

    private void onRemove()
    {
        if (getProgress() != null)
        {
            return;
        }

        ArrayList<VdcActionParametersBase> list = new ArrayList<VdcActionParametersBase>();
        for (VmNetworkInterface vnic : getVnics())
        {
            VdcActionParametersBase parameters = getRemoveVmInterfaceParams(vnic);
            list.add(parameters);

        }

        StartProgress(null);

        Frontend.RunMultipleAction(getActionType(), list,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void executed(FrontendMultipleActionAsyncResult result) {

                        StopProgress();
                        cancel();

                    }
                }, null);
    }

    protected String getRemoveVnicFullMsg(VmNetworkInterface vnic) {
        return ConstantsManager.getInstance().getMessages().vnicFromVm(vnic.getName(), vnic.getVmName());
    }

    protected VdcActionParametersBase getRemoveVmInterfaceParams(VmNetworkInterface vnic) {
        return new RemoveVmInterfaceParameters(vnic.getVmId().getValue(), vnic.getId());
    }

    protected VdcActionType getActionType() {
        return VdcActionType.RemoveVmInterface;
    }

    public List<VmNetworkInterface> getVnics() {
        return vnics;
    }

    public boolean isFullMsg() {
        return fullMsg;
    }

    private void cancel()
    {
        sourceListModel.setWindow(null);
    }

    @Override
    public void executeCommand(UICommand command)
    {
        super.executeCommand(command);

        if (StringHelper.stringsEqual(command.getName(), "Cancel")) //$NON-NLS-1$
        {
            cancel();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnRemove")) //$NON-NLS-1$
        {
            onRemove();
        }
    }

}

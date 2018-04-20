package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.RemoveVmInterfaceParameters;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class RemoveVmInterfaceModel extends ConfirmationModel {

    private final List<VmNetworkInterface> vnics;
    private final boolean fullMsg;
    private final ListModel sourceListModel;

    public RemoveVmInterfaceModel(ListModel sourceListModel, List<VmNetworkInterface> vnics, boolean isFullMsg) {
        setTitle(ConstantsManager.getInstance().getConstants().removeNetworkInterfacesTitle());
        setHelpTag(HelpTag.remove_network_interface_vms);
        setHashName("remove_network_interface_vms"); //$NON-NLS-1$

        this.sourceListModel = sourceListModel;
        this.vnics = vnics;
        this.fullMsg = isFullMsg;

        ArrayList<String> items = new ArrayList<>();
        for (VmNetworkInterface vnic : vnics) {
            if (isFullMsg) {
                items.add(getRemoveVnicFullMsg(vnic));
            } else {
                items.add(vnic.getName());
            }
        }
        setItems(items);

        UICommand tempVar = UICommand.createDefaultOkUiCommand("OnRemove", this); //$NON-NLS-1$
        getCommands().add(tempVar);
        UICommand tempVar2 = UICommand.createCancelUiCommand("Cancel", this); //$NON-NLS-1$
        getCommands().add(tempVar2);
    }

    private void onRemove() {
        if (getProgress() != null) {
            return;
        }

        ArrayList<ActionParametersBase> list = new ArrayList<>();
        for (VmNetworkInterface vnic : getVnics()) {
            ActionParametersBase parameters = getRemoveVmInterfaceParams(vnic);
            list.add(parameters);

        }

        startProgress();

        Frontend.getInstance().runMultipleAction(getActionType(), list,
                result -> {

                    stopProgress();
                    cancel();

                }, null);
    }

    protected String getRemoveVnicFullMsg(VmNetworkInterface vnic) {
        return ConstantsManager.getInstance().getMessages().vnicFromVm(vnic.getName(), vnic.getVmName());
    }

    protected ActionParametersBase getRemoveVmInterfaceParams(VmNetworkInterface vnic) {
        return new RemoveVmInterfaceParameters(vnic.getVmId(), vnic.getId());
    }

    protected ActionType getActionType() {
        return ActionType.RemoveVmInterface;
    }

    public List<VmNetworkInterface> getVnics() {
        return vnics;
    }

    public boolean isFullMsg() {
        return fullMsg;
    }

    private void cancel() {
        sourceListModel.setWindow(null);
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if ("Cancel".equals(command.getName())) { //$NON-NLS-1$
            cancel();
        } else if ("OnRemove".equals(command.getName())) { //$NON-NLS-1$
            onRemove();
        }
    }

}

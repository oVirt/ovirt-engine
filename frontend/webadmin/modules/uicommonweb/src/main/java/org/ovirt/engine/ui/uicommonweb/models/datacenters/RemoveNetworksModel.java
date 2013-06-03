package org.ovirt.engine.ui.uicommonweb.models.datacenters;

import java.util.ArrayList;

import org.ovirt.engine.core.common.action.AddNetworkStoragePoolParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkView;
import org.ovirt.engine.core.compat.StringFormat;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class RemoveNetworksModel extends ConfirmationModel {

    private final ListModel sourceListModel;

    public RemoveNetworksModel(ListModel sourceListModel) {
        this.sourceListModel = sourceListModel;

        setTitle(ConstantsManager.getInstance().getConstants().removeLogicalNetworkTitle());
        setHashName("remove_logical_network"); //$NON-NLS-1$
        setMessage(ConstantsManager.getInstance().getConstants().logicalNetworksMsg());

        ArrayList<String> list = new ArrayList<String>();
        for (Object a : sourceListModel.getSelectedItems())
        {
            if (a instanceof NetworkView) {
                NetworkView netView = (NetworkView) a;
                if (netView.getDescription() == null
                        || netView.getDescription().trim().equals("")) { //$NON-NLS-1$
                    list.add(ConstantsManager.getInstance()
                            .getMessages()
                            .networkDc(netView.getName(), netView.getDataCenterName()));
                } else {
                    list.add(ConstantsManager.getInstance()
                            .getMessages()
                            .networkDcDescription(netView.getName(),
                                    netView.getDataCenterName(),
                                    netView.getDescription()));
                }

            } else if (a instanceof Network) {
                Network network = (Network) a;
                if (network.getDescription() == null || "".equals(network.getDescription().trim())) { //$NON-NLS-1$
                    list.add(network.getName());
                } else {
                    list.add(StringFormat.format("%1$s (%2$s)", network.getName(), network.getDescription())); //$NON-NLS-1$
                }
            }
        }
        setItems(list);

        UICommand tempVar = new UICommand("onRemove", this); //$NON-NLS-1$
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
        tempVar.setIsDefault(true);
        getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("cancel", this); //$NON-NLS-1$
        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        tempVar2.setIsCancel(true);
        getCommands().add(tempVar2);
    }

    public void onRemove()
    {
        ArrayList<VdcActionParametersBase> pb = new ArrayList<VdcActionParametersBase>();

        for (Object a : sourceListModel.getSelectedItems())
        {
            Network network = (Network) a;
            pb.add(new AddNetworkStoragePoolParameters(network.getDataCenterId(), network));
        }
        Frontend.RunMultipleAction(VdcActionType.RemoveNetwork, pb);

        sourceListModel.setConfirmWindow(null);
    }

    private void cancel() {
        sourceListModel.setConfirmWindow(null);
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);
        if ("onRemove".equals(command.getName())) //$NON-NLS-1$
        {
            onRemove();
        }
        else if ("cancel".equals(command.getName())) //$NON-NLS-1$
        {
            cancel();
        }
    }
}

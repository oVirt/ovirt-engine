package org.ovirt.engine.ui.uicommonweb.models.datacenters;

import java.util.ArrayList;

import org.ovirt.engine.core.common.action.RemoveNetworkParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkView;
import org.ovirt.engine.core.compat.StringFormat;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class RemoveNetworksModel extends ConfirmationModel {

    private final ListModel sourceListModel;

    @SuppressWarnings("unchecked")
    public RemoveNetworksModel(ListModel sourceListModel) {
        this.sourceListModel = sourceListModel;

        setTitle(ConstantsManager.getInstance().getConstants().removeLogicalNetworkTitle());
        setHelpTag(HelpTag.remove_logical_network);
        setHashName("remove_logical_network"); //$NON-NLS-1$

        ArrayList<String> list = new ArrayList<String>();
        boolean externalNetworksWillBeRemoved = false;
        for (Network network : (Iterable<Network>) sourceListModel.getSelectedItems())
        {
            if (network instanceof NetworkView) {
                NetworkView netView = (NetworkView) network;
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

            } else {
                if (network.getDescription() == null || "".equals(network.getDescription().trim())) { //$NON-NLS-1$
                    list.add(network.getName());
                } else {
                    list.add(StringFormat.format("%1$s (%2$s)", network.getName(), network.getDescription())); //$NON-NLS-1$
                }
            }

            if (network.isExternal() && !externalNetworksWillBeRemoved) {
                externalNetworksWillBeRemoved = true;
            }
        }
        setItems(list);

        if (externalNetworksWillBeRemoved) {
            getForce().setIsAvailable(true);
            getForce().setEntity(true);
            setForceLabel(ConstantsManager.getInstance().getConstants().removeNetworkFromProvider());
        }

        UICommand tempVar = UICommand.createDefaultOkUiCommand("onRemove", this); //$NON-NLS-1$
        getCommands().add(tempVar);
        UICommand tempVar2 = UICommand.createCancelUiCommand("cancel", this); //$NON-NLS-1$
        getCommands().add(tempVar2);
    }

    public void onRemove()
    {
        ArrayList<VdcActionParametersBase> pb = new ArrayList<VdcActionParametersBase>();

        for (Object a : sourceListModel.getSelectedItems())
        {
            Network network = (Network) a;
            if (network.isExternal()) {
                pb.add(new RemoveNetworkParameters(network.getId(), (Boolean) getForce().getEntity()));
            } else {
                pb.add(new RemoveNetworkParameters(network.getId()));
            }
        }
        Frontend.getInstance().runMultipleAction(VdcActionType.RemoveNetwork, pb);

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

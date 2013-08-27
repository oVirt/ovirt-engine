package org.ovirt.engine.ui.uicommonweb.models.datacenters;


import org.ovirt.engine.core.common.action.NetworkQoSParametersBase;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.network.NetworkQoS;
import org.ovirt.engine.core.common.businessentities.network.VnicProfileView;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendMultipleQueryAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleQueryAsyncCallback;

import java.util.ArrayList;

public class RemoveNetworkQoSModel extends ConfirmationModel {

    private final ListModel sourceListModel;

    public RemoveNetworkQoSModel(ListModel sourceListModel) {
        this.sourceListModel = sourceListModel;

        setTitle(ConstantsManager.getInstance().getConstants().removeNetworkQoSTitle());
        setMessage();
        UICommand tempVar = new UICommand("onRemove", this); //$NON-NLS-1$
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
        tempVar.setIsDefault(true);
        getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("cancel", this); //$NON-NLS-1$
        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        tempVar2.setIsCancel(true);
        getCommands().add(tempVar2);
    }

    private void setMessage() {

        ArrayList<VdcQueryParametersBase> parameters = new ArrayList<VdcQueryParametersBase>();
        ArrayList<VdcQueryType> queryTypes = new ArrayList<VdcQueryType>();
        for (Object networkQoS : sourceListModel.getSelectedItems()) {
            VdcQueryParametersBase parameter = new IdQueryParameters(((NetworkQoS) networkQoS).getId());
            parameters.add(parameter);
            queryTypes.add(VdcQueryType.GetVnicProfilesByNetworkQosId);
        }
        Frontend.RunMultipleQueries(queryTypes, parameters, new IFrontendMultipleQueryAsyncCallback() {

            @Override
            public void executed(FrontendMultipleQueryAsyncResult result) {
                ArrayList<VnicProfileView> vnicProfiles = new ArrayList<VnicProfileView>();

                for (VdcQueryReturnValue returnValue : result.getReturnValues()) {
                    vnicProfiles.addAll((ArrayList<VnicProfileView>) returnValue.getReturnValue());
                }
                if (vnicProfiles.isEmpty()) {
                    setHashName("remove_network_qos"); //$NON-NLS-1$

                    ArrayList<String> list = new ArrayList<String>();
                    for (Object item : sourceListModel.getSelectedItems()) {
                        NetworkQoS i = (NetworkQoS) item;
                        list.add(i.getName());
                    }
                    setItems(list);
                } else {
                    // name start with underscore to prevent default message
                    setHashName("_remove_network_qos"); //$NON-NLS-1$
                    setMessage(ConstantsManager.getInstance().getMessages().removeNetworkQoSMessage(vnicProfiles.size()));

                    ArrayList<String> list = new ArrayList<String>();
                    for (VnicProfileView item : vnicProfiles) {
                        list.add(item.getName());
                    }
                    setItems(list);
                }
            }
        });
    }

    public void onRemove() {
        ArrayList<VdcActionParametersBase> parameters = new ArrayList<VdcActionParametersBase>();

        for (Object networkQoS : sourceListModel.getSelectedItems()) {
            NetworkQoSParametersBase parameter = new NetworkQoSParametersBase();
            parameter.setNetworkQoS((NetworkQoS) networkQoS);
            parameters.add(parameter);
        }
        Frontend.RunMultipleAction(VdcActionType.RemoveNetworkQoS, parameters);

        cancel();
    }

    private void cancel() {
        sourceListModel.setConfirmWindow(null);
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);
        if ("onRemove".equals(command.getName())) { //$NON-NLS-1$
            onRemove();
        } else if ("cancel".equals(command.getName())) { //$NON-NLS-1$
            cancel();
        }
    }
}

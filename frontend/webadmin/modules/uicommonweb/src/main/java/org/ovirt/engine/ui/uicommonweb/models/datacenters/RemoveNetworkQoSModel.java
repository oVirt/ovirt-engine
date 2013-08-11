package org.ovirt.engine.ui.uicommonweb.models.datacenters;


import org.ovirt.engine.core.common.action.NetworkQoSParametersBase;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.network.NetworkQoS;
import org.ovirt.engine.core.common.businessentities.network.VnicProfileView;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

import java.util.ArrayList;

public class RemoveNetworkQoSModel extends ConfirmationModel {

    private final ListModel sourceListModel;

    public RemoveNetworkQoSModel(ListModel sourceListModel) {
        this.sourceListModel = sourceListModel;

        setTitle(ConstantsManager.getInstance().getConstants().removeNetworkQoSTitle());
        // name start with underscore to prevent default message
        setHashName("_remove_network_qos"); //$NON-NLS-1$
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

        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object ReturnValue) {
                ArrayList<VnicProfileView> vnicProfiles =
                        (ArrayList<VnicProfileView>) ((VdcQueryReturnValue) ReturnValue).getReturnValue();
                if (vnicProfiles == null || vnicProfiles.isEmpty()) {
                    setMessage(ConstantsManager.getInstance().getConstants().removeNetworkQoSMessage());
                } else {
                    StringBuilder stringBuilder = new StringBuilder(
                            ConstantsManager.getInstance().getMessages().removeNetworkQoSMessage(vnicProfiles.size()));
                    for (int i = 0; i < vnicProfiles.size(); i++) {
                        stringBuilder.append("  - ").append(vnicProfiles.get(i).getName())  //$NON-NLS-1$
                                .append(" (").append(vnicProfiles.get(i).getNetworkName()).append(")\n"); //$NON-NLS-1$  //$NON-NLS-2$
                        if (i >= 10) {
                            stringBuilder.append("    ...");  //$NON-NLS-1$
                            break;
                        }
                    }
                    setMessage(stringBuilder.toString());
                }
            }
        };

        IdQueryParameters queryParams = new IdQueryParameters(((NetworkQoS)sourceListModel.getSelectedItem()).getId());
        Frontend.RunQuery(VdcQueryType.GetVnicProfilesByNetworkQosId, queryParams, _asyncQuery);

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

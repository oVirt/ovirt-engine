package org.ovirt.engine.ui.uicommonweb.models.datacenters;

import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.QosParametersBase;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.network.NetworkQoS;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class NewNetworkQoSModel extends NetworkQoSModel {
    public NewNetworkQoSModel(Model sourceModel, StoragePool dataCenter) {
        super(sourceModel, dataCenter);
        setTitle(ConstantsManager.getInstance().getConstants().newNetworkQoSTitle());
        setHelpTag(HelpTag.new_network_qos);
        setHashName("new_network_qos"); //$NON-NLS-1$
        networkQoS = new NetworkQoS();
    }

    @Override
    protected void executeSave() {
        // New network QoS
        final QosParametersBase<NetworkQoS> parameters = new QosParametersBase<>();
        parameters.setQos(networkQoS);
        Frontend.getInstance().runAction(ActionType.AddNetworkQoS, parameters, result -> {
            ActionReturnValue retVal = result.getReturnValue();
            boolean succeeded = false;
            if (retVal != null && retVal.getSucceeded()) {
                succeeded = true;
                networkQoS.setId((Guid) retVal.getActionReturnValue());
            }
            postSaveAction(succeeded);
        });
    }
}

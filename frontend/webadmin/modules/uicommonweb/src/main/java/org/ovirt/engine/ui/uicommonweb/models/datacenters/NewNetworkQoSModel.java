package org.ovirt.engine.ui.uicommonweb.models.datacenters;

import org.ovirt.engine.core.common.action.NetworkQoSParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.network.NetworkQoS;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;

public class NewNetworkQoSModel extends NetworkQoSModel {
    public NewNetworkQoSModel(Model sourceModel, StoragePool dataCenter) {
        super(sourceModel, dataCenter);
        setTitle(ConstantsManager.getInstance().getConstants().newNetworkQoSTitle());
        setHashName("new_network_qos"); //$NON-NLS-1$
        networkQoS = new NetworkQoS();
    }

    @Override
    protected void executeSave() {
        // New network QoS
        final NetworkQoSParametersBase parameters = new NetworkQoSParametersBase();
        parameters.setNetworkQoS(networkQoS);
        Frontend.getInstance().runAction(VdcActionType.AddNetworkQoS, parameters, new IFrontendActionAsyncCallback() {
            @Override
            public void executed(FrontendActionAsyncResult result1) {
                VdcReturnValueBase retVal = result1.getReturnValue();
                boolean succeeded = false;
                if (retVal != null && retVal.getSucceeded()) {
                    succeeded = true;
                    networkQoS.setId((Guid) retVal.getActionReturnValue());
                }
                postSaveAction(succeeded);
            }
        });
    }
}

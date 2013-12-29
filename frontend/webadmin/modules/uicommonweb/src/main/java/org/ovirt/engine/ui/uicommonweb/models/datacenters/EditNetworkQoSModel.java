package org.ovirt.engine.ui.uicommonweb.models.datacenters;

import org.ovirt.engine.core.common.action.NetworkQoSParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.network.NetworkQoS;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;

public class EditNetworkQoSModel extends NetworkQoSModel {

    public EditNetworkQoSModel(NetworkQoS networkQoS, Model sourceModel, StoragePool dataCenter) {
        super(sourceModel, dataCenter);
        this.networkQoS = networkQoS;
        init();
    }

    private void init() {
        setTitle(ConstantsManager.getInstance().getConstants().editNetworkQoSTitle());
        setHashName("edit_network_qos"); //$NON-NLS-1$
        getName().setEntity(networkQoS.getName());

        if (networkQoS.getInboundAverage() == null
                || networkQoS.getInboundPeak() == null
                || networkQoS.getInboundBurst() == null) {
            getInbound().getEnabled().setEntity(false);
        } else {
            getInbound().getAverage().setEntity(networkQoS.getInboundAverage());
            getInbound().getPeak().setEntity(networkQoS.getInboundPeak());
            getInbound().getBurst().setEntity(networkQoS.getInboundBurst());
        }

        if (networkQoS.getOutboundAverage() == null
                || networkQoS.getOutboundPeak() == null
                || networkQoS.getOutboundBurst() == null) {
            getOutbound().getEnabled().setEntity(false);
        } else {
            getOutbound().getAverage().setEntity(networkQoS.getOutboundAverage());
            getOutbound().getPeak().setEntity(networkQoS.getOutboundPeak());
            getOutbound().getBurst().setEntity(networkQoS.getOutboundBurst());
        }
    }

    @Override
    public void executeSave() {
        NetworkQoSParametersBase parameters = new NetworkQoSParametersBase();
        parameters.setNetworkQoS(networkQoS);
        Frontend.getInstance().runAction(VdcActionType.UpdateNetworkQoS, parameters, new IFrontendActionAsyncCallback() {
            @Override
            public void executed(FrontendActionAsyncResult result1) {
                VdcReturnValueBase retVal = result1.getReturnValue();
                boolean succeeded = false;
                if (retVal != null && retVal.getSucceeded()) {
                    succeeded = true;
                }
                postSaveAction(succeeded);
            }
        });
    }
}

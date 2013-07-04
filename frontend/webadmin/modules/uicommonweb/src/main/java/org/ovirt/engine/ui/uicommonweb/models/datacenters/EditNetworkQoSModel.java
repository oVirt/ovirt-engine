package org.ovirt.engine.ui.uicommonweb.models.datacenters;


import org.ovirt.engine.core.common.action.NetworkQoSParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.network.NetworkQoS;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;

public class EditNetworkQoSModel extends NetworkQoSModel {

    public EditNetworkQoSModel(NetworkQoS networkQoS, DataCenterNetworkQoSListModel sourceListModel) {
        super(sourceListModel);
        this.networkQoS = networkQoS;
        init();
    }

    private void init() {
        setTitle(ConstantsManager.getInstance().getConstants().editNetworkQoSTitle());
        setHashName("edit_network_qos"); //$NON-NLS-1$
        getName().setEntity(networkQoS.getName());

        if(networkQoS.getInboundAverage() == null
                || networkQoS.getInboundPeak() == null
                || networkQoS.getInboundBurst() == null) {
            getInboundEnabled().setEntity(Boolean.FALSE);
        } else {
            getInboundAverage().setEntity(networkQoS.getInboundAverage().toString());
            getInboundPeak().setEntity(networkQoS.getInboundPeak().toString());
            getInboundBurst().setEntity(networkQoS.getInboundBurst().toString());
        }

        if(networkQoS.getOutboundAverage() == null
                || networkQoS.getOutboundPeak() == null
                || networkQoS.getOutboundBurst() == null) {
            getOutboundEnabled().setEntity(Boolean.FALSE);
        } else {
            getOutboundAverage().setEntity(networkQoS.getOutboundAverage().toString());
            getOutboundPeak().setEntity(networkQoS.getOutboundPeak().toString());
            getOutboundBurst().setEntity(networkQoS.getOutboundBurst().toString());
        }
    }

    @Override
    public void executeSave() {
        NetworkQoSParametersBase parameters = new NetworkQoSParametersBase();
        parameters.setNetworkQoS(networkQoS);
        Frontend.RunAction(VdcActionType.UpdateNetworkQoS, parameters, new IFrontendActionAsyncCallback() {
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

package org.ovirt.engine.core.vdsbroker.gluster;

import java.util.List;

import org.ovirt.engine.core.common.vdscommands.gluster.GlusterHookVDSParameters;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterServiceVDSParameters;
import org.ovirt.engine.core.vdsbroker.vdsbroker.StatusForXmlRpc;

public class ManageGlusterServiceVDSCommand<P extends GlusterHookVDSParameters> extends AbstractGlusterBrokerCommand<GlusterServiceVDSParameters> {

    protected GlusterServicesReturnForXmlRpc returnValue;

    public ManageGlusterServiceVDSCommand(GlusterServiceVDSParameters params) {
        super(params);
    }

    @Override
    protected StatusForXmlRpc getReturnStatus() {
        return returnValue.mStatus;
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
        List<String> serviceList = getParameters().getServiceList();
        String[] serviceNameList = new String[serviceList.size()];
        serviceList.toArray(serviceNameList);
        returnValue =
                getBroker().glusterServicesAction(getParameters().getVdsId(),
                        serviceNameList,
                        getParameters().getActionType());
        ProceedProxyReturnValue();
        if (getVDSReturnValue().getSucceeded()) {
            setReturnValue(returnValue.getServices());
        }
    }
}

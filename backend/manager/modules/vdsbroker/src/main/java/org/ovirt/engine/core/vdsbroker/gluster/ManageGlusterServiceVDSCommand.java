package org.ovirt.engine.core.vdsbroker.gluster;

import java.util.List;

import org.ovirt.engine.core.common.vdscommands.gluster.GlusterHookVDSParameters;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterServiceVDSParameters;
import org.ovirt.engine.core.vdsbroker.vdsbroker.Status;

public class ManageGlusterServiceVDSCommand<P extends GlusterHookVDSParameters> extends AbstractGlusterBrokerCommand<GlusterServiceVDSParameters> {

    protected GlusterServicesReturn returnValue;

    public ManageGlusterServiceVDSCommand(GlusterServiceVDSParameters params) {
        super(params);
    }

    @Override
    protected Status getReturnStatus() {
        return returnValue.getStatus();
    }

    @Override
    protected void executeVdsBrokerCommand() {
        List<String> serviceList = getParameters().getServiceList();
        String[] serviceNameList = new String[serviceList.size()];
        serviceList.toArray(serviceNameList);
        returnValue =
                getBroker().glusterServicesAction(getParameters().getVdsId(),
                        serviceNameList,
                        getParameters().getActionType());
        proceedProxyReturnValue();
        if (getVDSReturnValue().getSucceeded()) {
            setReturnValue(returnValue.getServices());
        }
    }
}

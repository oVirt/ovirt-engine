package org.ovirt.engine.core.vdsbroker.gluster;

import org.ovirt.engine.core.common.vdscommands.gluster.GlusterServicesListVDSParameters;
import org.ovirt.engine.core.vdsbroker.vdsbroker.Status;

/**
 * VDS command to fetch list of services with their status
 *
 * @param <P>
 *            The parameters class to be used with this command
 */
public class GlusterServicesListVDSCommand<P extends GlusterServicesListVDSParameters> extends AbstractGlusterBrokerCommand<P> {
    private GlusterServicesReturn glusterServices;

    public GlusterServicesListVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        glusterServices =
                getBroker().glusterServicesList(getParameters().getVdsId(),
                        getParameters().getServiceNames().toArray(new String[0]));
        proceedProxyReturnValue();
        if (getVDSReturnValue().getSucceeded()) {
            setReturnValue(glusterServices.getServices());
        }
    }

    @Override
    protected Status getReturnStatus() {
        return glusterServices.getStatus();
    }
}

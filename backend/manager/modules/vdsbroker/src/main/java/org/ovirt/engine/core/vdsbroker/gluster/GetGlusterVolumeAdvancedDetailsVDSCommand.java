package org.ovirt.engine.core.vdsbroker.gluster;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeAdvancedDetails;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeStatusOption;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeAdvancedDetailsVDSParameters;
import org.ovirt.engine.core.vdsbroker.vdsbroker.Status;

public class GetGlusterVolumeAdvancedDetailsVDSCommand<P extends GlusterVolumeAdvancedDetailsVDSParameters> extends AbstractGlusterBrokerCommand<P> {
    private GlusterVolumeStatusReturn result;

    public GetGlusterVolumeAdvancedDetailsVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected Status getReturnStatus() {
        return result.getStatus();
    }

    private boolean getSucceeded() {
        return result.getStatus().code == 0;
    }

    @Override
    protected void executeVdsBrokerCommand() {
        executeVolumeStatusInfo("");
        GlusterVolumeAdvancedDetails volumeAdvancedDetails = result.getVolumeAdvancedDetails();
        if (getParameters().isCapacityInfoRequired() || getParameters().isDetailRequired()) {
            if (getSucceeded()) {
                executeVolumeStatusInfo(GlusterVolumeStatusOption.DETAIL.name().toLowerCase());
                if (getSucceeded()) {
                    volumeAdvancedDetails.copyDetailsFrom(result.getVolumeAdvancedDetails());
                }
            }
        }
        if (getParameters().isDetailRequired()) {
            if (getSucceeded()) {
                executeVolumeStatusInfo(GlusterVolumeStatusOption.CLIENTS.name().toLowerCase());
                if (getSucceeded()) {
                    volumeAdvancedDetails.copyClientsFrom(result.getVolumeAdvancedDetails());
                    executeVolumeStatusInfo(GlusterVolumeStatusOption.MEM.name().toLowerCase());

                    if (getSucceeded()) {
                        volumeAdvancedDetails.copyMemoryFrom(result.getVolumeAdvancedDetails());
                    }
                }
            }
        }
        setReturnValue(volumeAdvancedDetails);
    }

    private void executeVolumeStatusInfo(String volumeStatusOption) {
        result =
                getBroker().glusterVolumeStatus(getParameters().getClusterId(),
                        getParameters().getVolumeName(),
                        getParameters().getBrickName() == null ? "" : getParameters().getBrickName(),
                        volumeStatusOption);
        proceedProxyReturnValue();
    }
}

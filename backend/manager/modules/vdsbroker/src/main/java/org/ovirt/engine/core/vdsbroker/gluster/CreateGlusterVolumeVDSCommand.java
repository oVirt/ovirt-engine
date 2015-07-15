package org.ovirt.engine.core.vdsbroker.gluster;

import java.util.Set;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.TransportType;
import org.ovirt.engine.core.common.gluster.GlusterFeatureSupported;
import org.ovirt.engine.core.common.vdscommands.gluster.CreateGlusterVolumeVDSParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.vdsbroker.irsbroker.OneUuidReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.vdsbroker.StatusForXmlRpc;

/**
 * VDS command to create a gluster volume
 */
public class CreateGlusterVolumeVDSCommand<P extends CreateGlusterVolumeVDSParameters> extends AbstractGlusterBrokerCommand<P> {
    private OneUuidReturnForXmlRpc uuidReturn;

    public CreateGlusterVolumeVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected StatusForXmlRpc getReturnStatus() {
        return uuidReturn.getXmlRpcStatus();
    }

    @Override
    protected void executeVdsBrokerCommand() {
        GlusterVolumeEntity volume = getParameters().getVolume();

        boolean isForce = getParameters().isForce();
        boolean supportForceCreateVolume =
                GlusterFeatureSupported.glusterForceCreateVolumeSupported(getParameters().getClusterVersion());

        uuidReturn = supportForceCreateVolume ?
                getBroker().glusterVolumeCreate(volume.getName(),
                        volume.getBrickDirectories().toArray(new String[0]),
                        volume.getReplicaCount(),
                        volume.getStripeCount(),
                        getTransportTypeArr(volume),
                        isForce) :
                getBroker().glusterVolumeCreate(volume.getName(),
                        volume.getBrickDirectories().toArray(new String[0]),
                        volume.getReplicaCount(),
                        volume.getStripeCount(),
                        getTransportTypeArr(volume));

        // Handle errors if any
        proceedProxyReturnValue();

        if(getVDSReturnValue().getSucceeded()) {
            // set the volume updated with id as the return value
            volume.setId(Guid.createGuidFromStringDefaultEmpty(uuidReturn.uuid));
            setReturnValue(volume);
        }
    }

    private String[] getTransportTypeArr(GlusterVolumeEntity volume) {
        Set<TransportType> transportTypes = volume.getTransportTypes();
        if(transportTypes == null) {
            return null;
        }

        String[] transportTypeArr = new String[transportTypes.size()];
        int index = 0;
        for(TransportType transportType : transportTypes) {
            transportTypeArr[index++] = transportType.name();
        }

        return transportTypeArr;
    }
}

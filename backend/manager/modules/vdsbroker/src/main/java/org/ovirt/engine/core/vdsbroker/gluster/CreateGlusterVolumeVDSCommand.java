package org.ovirt.engine.core.vdsbroker.gluster;

import java.util.Set;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.TransportType;
import org.ovirt.engine.core.common.vdscommands.gluster.CreateGlusterVolumeVDSParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.vdsbroker.irsbroker.OneUuidReturn;
import org.ovirt.engine.core.vdsbroker.vdsbroker.Status;

/**
 * VDS command to create a gluster volume
 */
public class CreateGlusterVolumeVDSCommand<P extends CreateGlusterVolumeVDSParameters> extends AbstractGlusterBrokerCommand<P> {
    private OneUuidReturn uuidReturn;

    public CreateGlusterVolumeVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected Status getReturnStatus() {
        return uuidReturn.getStatus();
    }

    @Override
    protected void executeVdsBrokerCommand() {
        GlusterVolumeEntity volume = getParameters().getVolume();

        boolean isForce = getParameters().isForce();
        uuidReturn = getBroker().glusterVolumeCreate(volume.getName(),
                        volume.getBrickDirectories().toArray(new String[0]),
                        volume.getReplicaCount(),
                        volume.getStripeCount(),
                        getTransportTypeArr(volume),
                        isForce,
                volume.getIsArbiter());

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

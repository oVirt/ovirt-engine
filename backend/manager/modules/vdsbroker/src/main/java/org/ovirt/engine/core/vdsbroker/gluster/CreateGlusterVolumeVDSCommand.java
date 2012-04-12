package org.ovirt.engine.core.vdsbroker.gluster;

import java.util.Set;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeOptionEntity;
import org.ovirt.engine.core.common.businessentities.gluster.TransportType;
import org.ovirt.engine.core.common.constants.gluster.GlusterConstants;
import org.ovirt.engine.core.common.vdscommands.gluster.CreateGlusterVolumeVDSParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.vdsbroker.irsbroker.OneUuidReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.vdsbroker.StatusForXmlRpc;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsBrokerCommand;

/**
 * VDS command to create a gluster volume
 */
public class CreateGlusterVolumeVDSCommand<P extends CreateGlusterVolumeVDSParameters> extends VdsBrokerCommand<P> {
    private StatusForXmlRpc status;

    public CreateGlusterVolumeVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected StatusForXmlRpc getReturnStatus() {
        return status;
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
        GlusterVolumeEntity volume = getParameters().getVolume();

        OneUuidReturnForXmlRpc uuidReturn =
                getBroker().glusterVolumeCreate(volume.getName(),
                        volume.getBrickDirectories().toArray(new String[0]),
                        volume.getReplicaCount(),
                        volume.getStripeCount(),
                        getTransportTypeArr(volume));
        status = uuidReturn.mStatus;
        volume.setId(Guid.createGuidFromString(uuidReturn.mUuid));

        // Proceed only if there were no errors in the VDS command
        ProceedProxyReturnValue();

        // volume creation succeeded. Proceed with other work.

        if(!volume.isNfsEnabled()) {
            status = getBroker().glusterVolumeSet(volume.getName(), GlusterConstants.OPTION_NFS_DISABLE, "on").mStatus;
            ProceedProxyReturnValue();
        }

        for(GlusterVolumeOptionEntity option : volume.getOptions()) {
            status = getBroker().glusterVolumeSet(volume.getName(), option.getKey(), option.getValue()).mStatus;
            ProceedProxyReturnValue();
        }

        // set the volume updated with id as the return value
        setReturnValue(volume);
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

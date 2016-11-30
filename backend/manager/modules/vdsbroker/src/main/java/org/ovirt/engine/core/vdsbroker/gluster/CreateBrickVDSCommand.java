package org.ovirt.engine.core.vdsbroker.gluster;

import java.util.HashSet;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.gluster.StorageDevice;
import org.ovirt.engine.core.common.vdscommands.gluster.CreateBrickVDSParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.vdsbroker.vdsbroker.Status;

/**
 * VDS command to create a gluster brick
 */
public class CreateBrickVDSCommand<P extends CreateBrickVDSParameters> extends AbstractGlusterBrokerCommand<P> {
    private OneStorageDeviceReturn storageDeviceReturn;

    public CreateBrickVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        CreateBrickVDSParameters parameters = getParameters();
        Set<String> diskNames = new HashSet<>();

        for (StorageDevice storageDevice : parameters.getStorageDevices()) {
            diskNames.add(storageDevice.getName());
        }

        storageDeviceReturn =
                getBroker().glusterCreateBrick(parameters.getLvName(),
                        parameters.getMountPoint(),
                        parameters.getRaidParams(),
                        parameters.getFsType(), diskNames.toArray(new String[0]));

        proceedProxyReturnValue();
        if (getVDSReturnValue().getSucceeded()) {
            StorageDevice storageDevice = storageDeviceReturn.getStorageDevice();
            storageDevice.setVdsId(getParameters().getVdsId());
            storageDevice.setId(Guid.newGuid());
            setReturnValue(storageDevice);
        }
    }

    @Override
    protected Status getReturnStatus() {
        return storageDeviceReturn.getStatus();
    }

}

package org.ovirt.engine.core.bll.gluster;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.StorageDevice;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetUnusedGlusterBricksQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

    public GetUnusedGlusterBricksQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        List<StorageDevice> storageDevicesInHost =
                getDbFacade().getStorageDeviceDao().getStorageDevicesInHost(getParameters().getId());
        getQueryReturnValue().setReturnValue(getUnUsedBricks(storageDevicesInHost));
    }

    private List<StorageDevice> getUnUsedBricks(List<StorageDevice> storageDevicesInHost) {
        List<GlusterBrickEntity> usedBricks =
                getDbFacade().getGlusterBrickDao().getGlusterVolumeBricksByServerId(getParameters().getId());
        List<StorageDevice> freeBricks = new ArrayList<>();
        for (StorageDevice storageDevice : storageDevicesInHost) {
            if (storageDevice.getMountPoint() != null
                    && !storageDevice.getMountPoint().isEmpty()
                    && (storageDevice.getMountPoint()
                            .startsWith(Config.<String> getValue(ConfigValues.GlusterDefaultBrickMountPoint))
                    || storageDevice.isGlusterBrick())
                    && !isBrickUsed(usedBricks, storageDevice.getMountPoint())) {
                freeBricks.add(storageDevice);
            }
        }

        return freeBricks;
    }

    private boolean isBrickUsed(List<GlusterBrickEntity> usedBricks, String mountPoint) {
        for (GlusterBrickEntity brick : usedBricks) {
            // Checks if the given mount point is already part of any Gluster brick directory.
            // Brick directory may be any directory inside the mount points, so we are using brickDir.startsWith()
            if (brick.getBrickDirectory().startsWith(mountPoint)) {
                return true;
            }
        }
        return false;
    }

}

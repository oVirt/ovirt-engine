package org.ovirt.engine.core.bll.gluster;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.StorageDevice;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.gluster.GlusterBrickDao;
import org.ovirt.engine.core.dao.gluster.StorageDeviceDao;

public class GetUnusedGlusterBricksQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

    @Inject
    private StorageDeviceDao storageDeviceDao;

    @Inject
    private GlusterBrickDao glusterBrickDao;

    public GetUnusedGlusterBricksQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        List<StorageDevice> storageDevicesInHost = storageDeviceDao.getStorageDevicesInHost(getParameters().getId());
        getQueryReturnValue().setReturnValue(getUnUsedBricks(storageDevicesInHost));
    }

    private List<StorageDevice> getUnUsedBricks(List<StorageDevice> storageDevicesInHost) {
        List<GlusterBrickEntity> usedBricks = glusterBrickDao.getGlusterVolumeBricksByServerId(getParameters().getId());
        return storageDevicesInHost.stream().filter
                (storageDevice -> storageDevice.getMountPoint() != null
                    && !storageDevice.getMountPoint().isEmpty()
                    && (storageDevice.getMountPoint()
                            .startsWith(Config.getValue(ConfigValues.GlusterDefaultBrickMountPoint))
                    || storageDevice.isGlusterBrick())
                    && !isBrickUsed(usedBricks, storageDevice.getMountPoint()))
                .collect(Collectors.toList());
    }

    private boolean isBrickUsed(List<GlusterBrickEntity> usedBricks, String mountPoint) {
        // Checks if the given mount point is already part of any Gluster brick directory.
        // Brick directory may be any directory inside the mount points, so we are using brickDir.startsWith()
        return usedBricks.stream().anyMatch(brick -> brick.getBrickDirectory().startsWith(mountPoint));
    }

}

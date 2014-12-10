package org.ovirt.engine.core.bll.gluster;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.StorageDevice;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.queries.VdsIdParametersBase;

public class GetUnusedGlusterBricksQuery<P extends VdsIdParametersBase> extends QueriesCommandBase<P> {

    public GetUnusedGlusterBricksQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        List<StorageDevice> storageDevicesInHost =
                getDbFacade().getStorageDeviceDao().getStorageDevicesInHost(getParameters().getVdsId());
        getQueryReturnValue().setReturnValue(getUnUsedBricks(storageDevicesInHost));

    }

    private List<StorageDevice> getUnUsedBricks(List<StorageDevice> bricksFromServer) {
        List<GlusterBrickEntity> usedBricks =
                getDbFacade().getGlusterBrickDao().getGlusterVolumeBricksByServerId(getParameters().getVdsId());
        List<StorageDevice> freeBricks = new ArrayList<StorageDevice>();
        Set<String> bricksDir = new HashSet<String>();
        for (GlusterBrickEntity brick : usedBricks) {
            bricksDir.add(brick.getBrickDirectory());
        }
        for (StorageDevice brick : bricksFromServer) {
            if (brick.getMountPoint() != null && !brick.getMountPoint().isEmpty()
                    && brick.getMountPoint()
                            .startsWith(Config.<String> getValue(ConfigValues.DefaultGlusterBrickMountPoint))
                    && !bricksDir.contains(brick.getMountPoint())) {
                freeBricks.add(brick);
            }
        }

        return freeBricks;
    }

}

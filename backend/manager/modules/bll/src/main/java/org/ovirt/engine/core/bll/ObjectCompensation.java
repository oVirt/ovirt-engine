package org.ovirt.engine.core.bll;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.bll.network.macpool.MacPool;
import org.ovirt.engine.core.bll.network.macpool.MacPoolPerCluster;
import org.ovirt.engine.core.common.businessentities.BusinessEntitySnapshot;
import org.ovirt.engine.core.common.businessentities.ReleaseMacsTransientCompensation;
import org.ovirt.engine.core.common.businessentities.TransientCompensationBusinessEntity;

@Singleton
public class ObjectCompensation {
    @Inject
    private MacPoolPerCluster macPoolPerCluster;

    public void compensate(String commandType, TransientCompensationBusinessEntity entity) {
        switch (entity.getTransientEntityType()) {
        case RELEASE_MACS:
            handleReleaseMacsCompensation((ReleaseMacsTransientCompensation) entity);
            break;
        default:
            throw new IllegalArgumentException(String.format(
                    "Unable to compensate type %s, please handle this compensation in command %s.",
                    BusinessEntitySnapshot.SnapshotType.TRANSIENT_ENTITY,
                    commandType));
        }
    }

    private void handleReleaseMacsCompensation(ReleaseMacsTransientCompensation releaseMacs) {
        MacPool macPool = macPoolPerCluster.getMacPoolById(releaseMacs.getMacPoolId());
        macPool.freeMacs(releaseMacs.getMacs());
    }
}

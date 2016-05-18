package org.ovirt.engine.core.bll;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.bll.network.macpool.MacPool;
import org.ovirt.engine.core.bll.network.macpool.MacPoolPerDc;
import org.ovirt.engine.core.common.businessentities.BusinessEntitySnapshot;
import org.ovirt.engine.core.common.businessentities.ReleaseMacsTransientCompensation;
import org.ovirt.engine.core.common.businessentities.TransientCompensationBusinessEntity;

@Singleton
public class ObjectCompensation {
    @Inject
    private MacPoolPerDc macPoolPerDc;

    public void compensate(CommandBase command, TransientCompensationBusinessEntity entity) {
        switch (entity.getTransientEntityType()) {
        case RELEASE_MACS:
            handleReleaseMacsCompensation((ReleaseMacsTransientCompensation) entity);
            break;
        default:
            throw new IllegalArgumentException(String.format(
                    "Unable to compensate type %s, please handle this compensation in command %s.",
                    BusinessEntitySnapshot.SnapshotType.TRANSIENT_ENTITY,
                    command));
        }
    }

    private void handleReleaseMacsCompensation(ReleaseMacsTransientCompensation releaseMacs) {
        MacPool macPool = macPoolPerDc.getMacPoolById(releaseMacs.getMacPoolId());
        macPool.freeMacs(releaseMacs.getMacs());
    }
}

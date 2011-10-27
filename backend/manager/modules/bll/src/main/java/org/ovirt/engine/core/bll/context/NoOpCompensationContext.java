package org.ovirt.engine.core.bll.context;

import org.ovirt.engine.core.common.businessentities.BusinessEntity;

/**
 * An implementation of COmpensation Context that does nothing - will be used by commands that do not implement
 * compensation.
 */
public class NoOpCompensationContext implements CompensationContext {

    @Override
    public void snapshotEntity(BusinessEntity<?> entity) {
    }

    @Override
    public void snapshotNewEntity(BusinessEntity<?> entity) {
    }

    @Override
    public void snapshotEntityStatus(BusinessEntity<?> entity, Enum<?> status) {
    }

    @Override
    public void stateChanged() {
    }

    @Override
    public void resetCompensation() {
    }
}

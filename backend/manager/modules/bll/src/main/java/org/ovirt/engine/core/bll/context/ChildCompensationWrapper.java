package org.ovirt.engine.core.bll.context;

import java.util.Collection;

import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.BusinessEntityWithStatus;
import org.ovirt.engine.core.common.businessentities.TransientCompensationBusinessEntity;
import org.ovirt.engine.core.compat.Guid;

public class ChildCompensationWrapper implements CompensationContext {

    private CompensationContext context;

    public ChildCompensationWrapper(CompensationContext context) {
        this.context = context;
    }

    @Override
    public boolean isCompensationEnabled() {
        return context.isCompensationEnabled();
    }

    @Override
    public Guid getCommandId() {
        return context.getCommandId();
    }

    @Override
    public void snapshotEntity(BusinessEntity<?> entity) {
        context.snapshotEntity(entity);
    }

    @Override
    public void snapshotEntityUpdated(BusinessEntity<?> entity) {
        context.snapshotEntityUpdated(entity);
    }

    @Override
    public void snapshotEntities(Collection<? extends BusinessEntity<?>> entities) {
        context.snapshotEntities(entities);
    }

    @Override
    public void snapshotNewEntity(BusinessEntity<?> entity) {
        context.snapshotNewEntity(entity);
    }

    @Override
    public void snapshotNewEntities(Collection<? extends BusinessEntity<?>> entities) {
        context.snapshotNewEntities(entities);
    }

    @Override
    public <T extends Enum<?>> void snapshotEntityStatus(BusinessEntityWithStatus<?, T> entity, T status) {
        context.snapshotEntityStatus(entity, status);
    }

    @Override
    public <T extends Enum<?>> void snapshotEntityStatus(BusinessEntityWithStatus<?, T> entity) {
        context.snapshotEntityStatus(entity);
    }

    @Override
    public void snapshotObject(TransientCompensationBusinessEntity entity) {
        context.snapshotObject(entity);
    }

    @Override
    public void stateChanged() {
        context.stateChanged();
    }

    @Override
    public void afterCompensationCleanup() {
        context.afterCompensationCleanup();
    }

    @Override
    public void cleanupCompensationDataAfterSuccessfulCommand() {
        // NO-OP - The clenup will be done once the parent command succeeds
    }

    @Override
    public void addListener(CompensationListener compensationListener) {
        context.addListener(compensationListener);
    }
}

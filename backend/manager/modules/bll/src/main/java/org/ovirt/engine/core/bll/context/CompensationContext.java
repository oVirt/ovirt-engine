package org.ovirt.engine.core.bll.context;

import java.util.Collection;

import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.BusinessEntityWithStatus;

/**
 * The compensation context contains information needed for compensating failed command executions.
 */
public interface CompensationContext {

    /**
     * Save a snapshot of the entire entity before it is changed/deleted in the DB, so that it can be restored later on
     * in case of compensation.
     *
     * @param entity
     *            The entity state before the change.
     */
    public void snapshotEntity(BusinessEntity<?> entity);

    /**
     * For each entity in the collection saves a snapshot of the entire data before it is changed/deleted in the DB, so
     * it can be restored later on in case of compensation
     *
     * @param entities
     *            The entities before the changes.
     */
    public void snapshotEntities(Collection<? extends BusinessEntity<?>> entities);

    /**
     * Save a snapshot of a new entity that was added to the DB, so that if there's need for compensation it will be
     * deleted from the DB.
     *
     * @param entity
     *            The new entity which was added.
     */
    public void snapshotNewEntity(BusinessEntity<?> entity);

    /**
     * Save snapshots of new entities that were added to the DB, so that if there's a need for compensation they will be
     * deleted from the DB.
     *
     * @param entities
     *            the entities that were added.
     */
    public void snapshotNewEntities(Collection<? extends BusinessEntity<?>> entities);

    /**
     * Snapshot the entity status only, so that in case of compensation for the entity, the status will be updated to
     * it's original value.
     *
     * @param entity
     *            The entity for which to save the status snapshot.
     * @param status
     *            The status to snapshot.
     */
    public <T extends Enum<?>> void  snapshotEntityStatus(BusinessEntityWithStatus<?, T> entity, T status);

    /**
     * Snapshot the entity status only, so that in case of compensation for the entity, the status will be updated to
     * it's original value.
     *
     * @param entity
     *            The entity for which to save the status snapshot.
     */
    public <T extends Enum<?>> void snapshotEntityStatus(BusinessEntityWithStatus<?, T> entity);

    /**
     * Signify that the command state had changed and the transaction is about to end, so that the snapshots can
     * be saved to the DB (in order to reduce lock time on the compensations table).
     */
    public void stateChanged();

    /**
     * Signify that the command does not need the compensation data which has been recorded up to this point, and if an
     * error occurs after this point then compensation will handle only the entities which were snapshot after this
     * point.
     */
    public void resetCompensation();
}

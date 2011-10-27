package org.ovirt.engine.core.bll.context;

import org.ovirt.engine.core.common.businessentities.BusinessEntity;

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
     * Save a snapshot of a new entity that was added to the DB, so that if there's need for compensation it will be
     * deleted from the DB.
     *
     * @param entity
     *            The new entity which was added.
     */
    public void snapshotNewEntity(BusinessEntity<?> entity);

    /**
     * Snapshot the entity status only, so that in case of compensation for the entity, the status will be updated to
     * it's original value.
     *
     * @param entity
     *            The entity for which to save the status snapshot.
     * @param status
     *            The status to snapshot.
     */
    public void snapshotEntityStatus(BusinessEntity<?> entity, Enum<?> status);

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

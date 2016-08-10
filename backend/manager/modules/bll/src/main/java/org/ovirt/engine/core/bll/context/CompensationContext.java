package org.ovirt.engine.core.bll.context;

import java.util.Collection;

import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.BusinessEntityWithStatus;
import org.ovirt.engine.core.common.businessentities.TransientCompensationBusinessEntity;
import org.ovirt.engine.core.compat.Guid;

/**
 * The compensation context contains information needed for compensating failed command executions.
 */
public interface CompensationContext {

    /**
     * @return true, if this CompensationContext does compensation logic. False if compensation is not performed.
     */
    boolean isCompensationEnabled();

    /**
     * @return  the command id for the compensation context
     */
    Guid getCommandId();

    /**
     * Save a snapshot of the entire entity before it is changed/deleted in the DB, so that it can be restored later on
     * in case of compensation.
     *
     * @param entity
     *            The entity state before the change.
     */
    void snapshotEntity(BusinessEntity<?> entity);

    /**
     * Save a snapshot of the entire entity before it was changed in the DB, so that it can be restored later on
     * in case of compensation.
     *
     * @param entity
     *            The entity state before the change.
     */
    void snapshotEntityUpdated(BusinessEntity<?> entity);

    /**
     * For each entity in the collection saves a snapshot of the entire data before it is changed/deleted in the DB, so
     * it can be restored later on in case of compensation
     *
     * @param entities
     *            The entities before the changes.
     */
    void snapshotEntities(Collection<? extends BusinessEntity<?>> entities);

    /**
     * Save a snapshot of a new entity that was added to the DB, so that if there's need for compensation it will be
     * deleted from the DB.
     *
     * @param entity
     *            The new entity which was added.
     */
    void snapshotNewEntity(BusinessEntity<?> entity);

    /**
     * Save snapshots of new entities that were added to the DB, so that if there's a need for compensation they will be
     * deleted from the DB.
     *
     * @param entities
     *            the entities that were added.
     */
    void snapshotNewEntities(Collection<? extends BusinessEntity<?>> entities);

    /**
     * Snapshot the entity status only, so that in case of compensation for the entity, the status will be updated to
     * it's original value.
     *
     * @param entity
     *            The entity for which to save the status snapshot.
     * @param status
     *            The status to snapshot.
     */
    <T extends Enum<?>> void  snapshotEntityStatus(BusinessEntityWithStatus<?, T> entity, T status);

    /**
     * Snapshot the entity status only, so that in case of compensation for the entity, the status will be updated to
     * it's original value.
     *
     * @param entity
     *            The entity for which to save the status snapshot.
     */
    <T extends Enum<?>> void snapshotEntityStatus(BusinessEntityWithStatus<?, T> entity);


    /**
     * @param entity entity representing BLL data to be bounded to compensation mechanism.
     */
    void snapshotObject(TransientCompensationBusinessEntity entity);

    /**
     * Signify that the command state had changed and the transaction is about to end, so that the snapshots can
     * be saved to the DB (in order to reduce lock time on the compensations table).
     */
    void stateChanged();


    /**
     * After compensation was called(because command failed or thrown exception), this method is invoked to
     * clean up all data, which was just compensated.
     */
    void afterCompensationCleanup();

    /**
     * After command using compensation ended in success, this method is invoked to
     * clean up all data, which was recorded for potential compensation.
     */
    void cleanupCompensationDataAfterSuccessfulCommand();

    void addListener(CompensationListener compensationListener);

    interface CompensationListener {
        void afterCompensation();
        void cleaningCompensationDataAfterSuccess();
    }

}

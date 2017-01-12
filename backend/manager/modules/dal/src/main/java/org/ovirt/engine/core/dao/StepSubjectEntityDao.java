package org.ovirt.engine.core.dao;

import java.util.Collection;
import java.util.List;

import org.ovirt.engine.core.common.job.StepSubjectEntity;
import org.ovirt.engine.core.compat.Guid;

public interface StepSubjectEntityDao extends Dao {
    /**
     * Saves the provided StepSubjectEntity collection.
     *
     * @param entities
     *            the step subject entities to save
     */
    void saveAll(Collection<StepSubjectEntity> entities);

    /**
     * Removes the provided StepSubjectEntity.
     *
     * @param entityId
     *            the entity id
     * @param stepId
     *            the step id
     */
    void remove(Guid entityId, Guid stepId);

    /**
     * Retrieves a collection of the entities for a specific step.
     *
     * @param stepId
     *            The identifier of the step
     * @return a collection of {@link StepSubjectEntity} associated with the given step
     */
    List<StepSubjectEntity> getStepSubjectEntitiesByStepId(Guid stepId);
}

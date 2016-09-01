package org.ovirt.engine.core.dao;

import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.compat.Guid;

/**
 * Defines CRUD operations for {@code JobSubjectEntity}, the satellite table of {@code Job}.
 */
public interface JobSubjectEntityDao extends Dao {

    /**
     * Persists a collection of entities to database
     *
     * @param jobId
     *            the job ID
     * @param entityId
     *            the id of the entity associated with the job
     * @param entityType
     *            the type of the entity associated with the job
     */
    void save(Guid jobId, Guid entityId, VdcObjectType entityType);

    /**
     * Retrieves a collection of entities for a specific job.
     *
     * @param jobId
     *            The identifier of the job
     * @return a collection of entities associated with the given job
     */
    Map<Guid, VdcObjectType> getJobSubjectEntityByJobId(Guid jobId);

    /**
     * Retrieves a collections of job IDs which are associated with a given entity
     *
     * @param entityId
     *            the id of the entity
     * @return a list of Job IDs
     */
    List<Guid> getJobIdByEntityId(Guid entityId);

}

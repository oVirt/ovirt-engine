package org.ovirt.engine.core.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.job.Job;
import org.ovirt.engine.core.common.job.JobExecutionStatus;
import org.ovirt.engine.core.compat.Guid;

public class JobDaoTest extends BaseGenericDaoTestCase<Guid, Job, JobDao> {

    private static final Guid EXISTING_JOB_ID = new Guid("54947df8-0e9e-4471-a2f9-9af509fb5889");
    private static final Guid NO_VDSM_TASKS_JOB_ID = new Guid("54947df8-0e9e-4471-a2f9-9af509fb5333");
    private static final String EXISTING_CORRELATION_ID = "54947df8-job1";
    private static final long ENGINE_SESSION_SEQ_ID = 137L;
    private static final int NUMBER_OF_JOBS_FOR_EXISTING_CORRELATION_ID = 1;
    private static final int NUMBER_OF_JOBS_FOR_ENGINE_SESSION_SEQ_ID_AND_STATUS = 5;
    private static final int TOTAL_JOBS = 6;

    @Override
    protected Guid getExistingEntityId() {
        return EXISTING_JOB_ID;
    }

    @Override
    protected Guid generateNonExistingId() {
        return Guid.newGuid();
    }

    @Override
    protected int getEntitiesTotalCount() {
        return TOTAL_JOBS;
    }

    @Override
    protected Job generateNewEntity() {
        Job job = new Job();
        job.setId(Guid.newGuid());
        job.setActionType(ActionType.ActivateStorageDomain);
        job.setDescription(ActionType.ActivateStorageDomain.name());
        job.setStatus(JobExecutionStatus.STARTED);
        job.setOwnerId(Guid.newGuid());
        job.setVisible(true);
        job.setStartTime(new Date());
        job.setLastUpdateTime(new Date());
        job.setCorrelationId(Guid.newGuid().toString());
        return job;
    }

    @Override
    protected void updateExistingEntity() {
        existingEntity.setEndTime(new Date());
        existingEntity.setStatus(JobExecutionStatus.FINISHED);
    }

    @Test
    public void existStep() {
        assertTrue(dao.exists(EXISTING_JOB_ID));
    }

    @Test
    public void nonExistStep() {
        assertFalse(dao.exists(Guid.newGuid()));
    }

    @Test
    public void getJobsByOffsetAndPageSize() {
        List<Job> jobsList = dao.getJobsByOffsetAndPageSize(0, 20);
        assertTrue(!jobsList.isEmpty());
    }

    @Test
    public void getJobsByNonExistsCorrelationID() {
        List<Job> jobsList = dao.getJobsByCorrelationId("NO_CORRELATION_ID");
        assertTrue(jobsList.isEmpty(), "Verify no jobs associate with non existed correlation ID");
    }

    @Test
    public void getJobsByCorrelationID() {
        List<Job> jobsList = dao.getJobsByCorrelationId(EXISTING_CORRELATION_ID);
        assertEquals(NUMBER_OF_JOBS_FOR_EXISTING_CORRELATION_ID, jobsList.size(),
                "Verify a job is associated with the correlation-ID");
    }

    @Test
    public void getJobsBySessionSeqIdAndStatus() {
        List<Job> jobsList = dao.getJobsBySessionSeqIdAndStatus(ENGINE_SESSION_SEQ_ID, JobExecutionStatus.STARTED);
        assertEquals(NUMBER_OF_JOBS_FOR_ENGINE_SESSION_SEQ_ID_AND_STATUS, jobsList.size(),
                "Verify jobs are associated with the engine-session-seq-ID and status");
    }

    @Test
    public void updateJobLastUpdateTime() throws ParseException {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date updateDate = df.parse("2012-10-01 10:00:00");
        Job job = dao.get(getExistingEntityId());
        Date lastUpdateTime = job.getLastUpdateTime();
        dao.updateJobLastUpdateTime(getExistingEntityId(), updateDate);
        Job jobAfterUpdate = dao.get(getExistingEntityId());
        assertTrue(!lastUpdateTime.equals(jobAfterUpdate.getLastUpdateTime()),
                "Compare the previous date is differ than new one");
        assertEquals(updateDate, jobAfterUpdate.getLastUpdateTime(),
                "Compare date was persisted by reading it from database");
    }

    @Test
    public void deleteJobOlderThanDateWithStatus() throws ParseException {
        int sizeBeforeDelete = dao.getAll().size();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dao.deleteJobOlderThanDateWithStatus(
                df.parse("2012-10-02 10:00:00"), Collections.singletonList(JobExecutionStatus.FAILED));
        int sizeAfterDelete = dao.getAll().size();
        assertTrue(sizeBeforeDelete > sizeAfterDelete, "Check an entry was deleted");
    }

    @Test
    public void deleteCompletedJobs() throws ParseException {
        int sizeBeforeDelete = dao.getAll().size();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date dateToDelete = df.parse("2013-02-9 10:11:00");
        dao.deleteCompletedJobs(dateToDelete, dateToDelete);
        int sizeAfterDelete = dao.getAll().size();
        assertNotNull(dao.get(new Guid("54947df8-0e9e-4471-a2f9-9af509fb0002")),
                "Check job with step and no async task are not deleted");
        assertTrue(sizeBeforeDelete > sizeAfterDelete, "Check an entry was deleted");
    }

    @Test
    public void checkIfJobHasTasks() {
        assertTrue(dao.checkIfJobHasTasks(EXISTING_JOB_ID), "Job has step for VDSM task");
    }

    @Test
    public void checkIfJobHasNoTasks() {
        assertFalse(dao.checkIfJobHasTasks(NO_VDSM_TASKS_JOB_ID), "Job has no steps for VDSM tasks");
    }
}

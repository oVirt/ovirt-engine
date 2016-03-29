package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.common.action.VdcActionType;
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
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected Guid getExistingEntityId() {
        return EXISTING_JOB_ID;
    }

    @Override
    protected JobDao prepareDao() {
        return dbFacade.getJobDao();
    }

    @Override
    protected Guid generateNonExistingId() {
        return Guid.newGuid();
    }

    @Override
    protected int getEneitiesTotalCount() {
        return TOTAL_JOBS;
    }

    @Override
    protected Job generateNewEntity() {
        Job job = new Job();
        job.setId(Guid.newGuid());
        job.setActionType(VdcActionType.ActivateStorageDomain);
        job.setDescription(VdcActionType.ActivateStorageDomain.name());
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
        assertTrue("Verify no jobs associate with non existed correlation ID", jobsList.isEmpty());
    }

    @Test
    public void getJobsByCorrelationID() {
        List<Job> jobsList = dao.getJobsByCorrelationId(EXISTING_CORRELATION_ID);
        assertEquals("Verify a job is associated with the correlation-ID",
                NUMBER_OF_JOBS_FOR_EXISTING_CORRELATION_ID,
                jobsList.size());
    }

    @Test
    public void getJobsBySessionSeqIdAndStatus() {
        List<Job> jobsList = dao.getJobsBySessionSeqIdAndStatus(ENGINE_SESSION_SEQ_ID, JobExecutionStatus.STARTED);
        assertEquals("Verify jobs are associated with the engine-session-seq-ID and status",
                NUMBER_OF_JOBS_FOR_ENGINE_SESSION_SEQ_ID_AND_STATUS,
                jobsList.size());
    }

    @Test
    public void updateJobLastUpdateTime() throws ParseException {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date updateDate = df.parse("2012-10-01 10:00:00");
        Job job = dao.get(getExistingEntityId());
        Date lastUpdateTime = job.getLastUpdateTime();
        dao.updateJobLastUpdateTime(getExistingEntityId(), updateDate);
        Job jobAfterUpdate = dao.get(getExistingEntityId());
        assertTrue("Compare the previous date is differ than new one",
                !lastUpdateTime.equals(jobAfterUpdate.getLastUpdateTime()));
        assertEquals("Compare date was persisted by reading it from database",
                updateDate,
                jobAfterUpdate.getLastUpdateTime());
    }

    @Test
    public void deleteJobOlderThanDateWithStatus() throws ParseException {
        int sizeBeforeDelete = dao.getAll().size();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dao.deleteJobOlderThanDateWithStatus(df.parse("2012-10-02 10:00:00"), Arrays.asList(JobExecutionStatus.FAILED));
        int sizeAfterDelete = dao.getAll().size();
        assertTrue("Check an entry was deleted", sizeBeforeDelete > sizeAfterDelete);
    }

    @Test
    public void deleteCompletedJobs() throws ParseException {
        int sizeBeforeDelete = dao.getAll().size();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date dateToDelete = df.parse("2013-02-9 10:11:00");
        dao.deleteCompletedJobs(dateToDelete, dateToDelete);
        int sizeAfterDelete = dao.getAll().size();
        assertNotNull("Check job with step and no async task are not deleted",
                dao.get(new Guid("54947df8-0e9e-4471-a2f9-9af509fb0002")));
        assertTrue("Check an entry was deleted", sizeBeforeDelete > sizeAfterDelete);
    }

    @Test
    public void checkIfJobHasTasks() {
        assertTrue("Job has step for VDSM task", dao.checkIfJobHasTasks(EXISTING_JOB_ID));
    }

    @Test
    public void checkIfJobHasNoTasks() {
        assertFalse("Job has no steps for VDSM tasks", dao.checkIfJobHasTasks(NO_VDSM_TASKS_JOB_ID));
    }
}

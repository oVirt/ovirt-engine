package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.job.Job;
import org.ovirt.engine.core.common.job.JobExecutionStatus;
import org.ovirt.engine.core.common.job.Step;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.job.JobRepository;
import org.ovirt.engine.core.dal.job.JobRepositoryImpl;

public class JobRepositoryDaoTest extends BaseDAOTestCase {

    private static final Guid EXISTING_JOB_ID = new Guid("54947df8-0e9e-4471-a2f9-9af509fb5889");
    private static final Guid EXISTING_JOB_ENTITY_ID = new Guid("77296e00-0cad-4e5a-9299-008a7b6f4354");
    private static final Guid IN_PROGRESS_JOB_ID = new Guid("54947df8-0e9e-4471-a2f9-9af509fb5333");

    private JobDao jobDao;
    private JobSubjectEntityDao jobSubjectEntityDao;
    private StepDao stepDao;
    private JobRepository jobRepository;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        jobDao = prepareDAO(dbFacade.getJobDao());
        jobSubjectEntityDao = prepareDAO(dbFacade.getJobSubjectEntityDao());
        stepDao = prepareDAO(dbFacade.getStepDao());

        jobRepository = new JobRepositoryImpl(jobDao, jobSubjectEntityDao, stepDao);
    }

    @Test
    public void prettyPrint() {
        Job jobWithSteps = jobRepository.getJobWithSteps(EXISTING_JOB_ID);
        assertNotNull(jobWithSteps);
    }

    @Test
    public void getJobById() {
        Job job = jobRepository.getJob(EXISTING_JOB_ID);
        assertNotNull(job);
    }

    @Test
    public void getJobByEntityIdAndActionType() {
        Job job = jobRepository.getJob(EXISTING_JOB_ID);
        Map<Guid, VdcObjectType> jobSubjectEntities = job.getJobSubjectEntities();
        assertTrue(!jobSubjectEntities.isEmpty());

        List<Job> searchedJob =
                jobRepository.getJobsByEntityAndAction(EXISTING_JOB_ENTITY_ID, job.getActionType());
        assertTrue(!searchedJob.isEmpty());
    }

    @Test
    public void finalizeJobs() {
        Job job = jobDao.get(IN_PROGRESS_JOB_ID);
        List<Step> steps = stepDao.getStepsByJobId(IN_PROGRESS_JOB_ID);

        assertTrue(job.getStatus() == JobExecutionStatus.STARTED);
        for (Step step : steps) {
            assertTrue(step.getStatus() == JobExecutionStatus.STARTED);
        }

        Date updateTime = new Date();
        jobDao.updateStartedExecutionEntitiesToUnknown(updateTime);

        job = jobDao.get(IN_PROGRESS_JOB_ID);
        steps = stepDao.getStepsByJobId(IN_PROGRESS_JOB_ID);

        assertEquals(job.getStatus(), JobExecutionStatus.UNKNOWN);
        assertEquals(job.getEndTime(), updateTime);
        assertEquals(job.getLastUpdateTime(), updateTime);

        for (Step step : steps) {
            assertEquals(step.getStatus(), JobExecutionStatus.UNKNOWN);
            assertEquals(step.getEndTime(), updateTime);
        }
    }
}

package org.ovirt.engine.core.bll.job;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.job.Job;
import org.ovirt.engine.core.common.job.Step;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.JobDao;
import org.ovirt.engine.core.dao.JobSubjectEntityDao;
import org.ovirt.engine.core.dao.StepDao;

public class JobRepositoryTest {

    private static final Map<Guid, VdcObjectType> JOB_SUBJECT_ENTITIES_MAP =
            Collections.<Guid, VdcObjectType> singletonMap(Guid.newGuid(), VdcObjectType.VM);

    @Mock
    private JobDao jobDao;

    @Mock
    private JobSubjectEntityDao jobSubjectEntityDao;

    @Mock
    private StepDao stepDao;

    private JobRepository jobRepository;

    private Job job;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        jobRepository = new JobRepositoryImpl(jobDao, jobSubjectEntityDao, stepDao);
        job = createJob();
        mockDaos(job);
    }

    @Test
    public void getJobWithSteps() {
        Job jobWithSteps = jobRepository.getJobWithSteps(job.getId());
        assertNotNull(jobWithSteps);
        assertNotNull(jobWithSteps.getSteps());
        assertTrue(!jobWithSteps.getSteps().isEmpty());
        assertEquals(JOB_SUBJECT_ENTITIES_MAP, jobWithSteps.getJobSubjectEntities());
    }

    @Test
    public void loadJobSteps() {
        jobRepository.loadJobSteps(job);
        assertNotNull(job.getSteps());
        assertTrue(!job.getSteps().isEmpty());
    }

    @Test
    public void getJobById() {
        assertNotNull(jobRepository.getJob(job.getId()));
        assertEquals(JOB_SUBJECT_ENTITIES_MAP, job.getJobSubjectEntities());
    }

    @Test
    public void getJobByEntityIdAndActionType() {
        job.setActionType(VdcActionType.UpdateVm);
        List<Job> searchedJob =
                jobRepository.getJobsByEntityAndAction(Guid.Empty, job.getActionType());
        assertNotNull(searchedJob);
        assertTrue(searchedJob.contains(job));
    }

    private void mockJobDao(Job job) {
        when(jobDao.get(any(Guid.class))).thenReturn(job);
    }

    private void mockStepDao(Guid jobId) {
        Step step = new Step();
        step.setJobId(jobId);
        when(stepDao.getStepsByJobId(eq(jobId))).thenReturn(Arrays.asList(step));
    }

    private void mockJobSubjectEntityDao(Guid jobId) {
        when(jobSubjectEntityDao.getJobSubjectEntityByJobId(eq(jobId)))
                .thenReturn(JOB_SUBJECT_ENTITIES_MAP);

        when(jobSubjectEntityDao.getJobIdByEntityId(any(Guid.class))).thenReturn(Arrays.<Guid> asList(jobId));
    }

    private void mockDaos(Job job) {
        mockJobDao(job);
        mockStepDao(job.getId());
        mockJobSubjectEntityDao(job.getId());
    }

    private Job createJob() {
        Job job = new Job();
        job.setId(Guid.newGuid());
        return job;
    }
}

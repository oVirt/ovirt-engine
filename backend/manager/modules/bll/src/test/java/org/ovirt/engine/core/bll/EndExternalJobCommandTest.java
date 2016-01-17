package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.common.action.EndExternalJobParameters;
import org.ovirt.engine.core.common.job.Job;
import org.ovirt.engine.core.common.job.JobExecutionStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.JobDao;
import org.slf4j.Logger;

public class EndExternalJobCommandTest extends BaseCommandTest {
    private static final Guid jobId = Guid.newGuid();
    private static final Guid nonExternalJobId = Guid.newGuid();
    private static final Guid nonExistingJobId = Guid.newGuid();
    private EndExternalJobParameters parameters;
    @Mock
    private JobDao jobDaoMock;
    @Mock
    private EndExternalJobCommand<EndExternalJobParameters> commandMock;
    @Mock
    private Logger log;


    @Before
    public void createParameters() {
        parameters = new EndExternalJobParameters(jobId, JobExecutionStatus.FINISHED, false);
    }

    private Job makeExternalTestJob(Guid jobId) {
        Job job = new Job();
        job.setId(jobId);
        job.setDescription("Sample Job");
        job.setExternal(true);
        return job;
    }

    private Job makeNonExternalTestJob(Guid jobId) {
        Job job = new Job();
        job.setId(jobId);
        job.setDescription("Sample Job");
        job.setExternal(false);
        return job;
    }

    private void setupMock() throws Exception {
        commandMock = spy(new EndExternalJobCommand<>(parameters, null));
        when(commandMock.getParameters()).thenReturn(parameters);
        doReturn(jobDaoMock).when(commandMock).getJobDao();
        when(jobDaoMock.get(jobId)).thenReturn(makeExternalTestJob(jobId));
        when(jobDaoMock.get(nonExternalJobId)).thenReturn(makeNonExternalTestJob(nonExternalJobId));
        when(jobDaoMock.get(nonExistingJobId)).thenReturn(null);
    }

    @Test
    public void validateOkSucceeds() throws Exception {
        setupMock();
        assertTrue(commandMock.validate());
    }

    @Test
    public void validateNonExistingJobFails() throws Exception {
        setupMock();
        parameters.setJobId(nonExistingJobId);
        assertTrue(! commandMock.validate());
    }

    @Test
    public void validateNonExternalJobFails() throws Exception {
        setupMock();
        parameters.setJobId(nonExternalJobId);
        assertTrue(! commandMock.validate());
    }
}

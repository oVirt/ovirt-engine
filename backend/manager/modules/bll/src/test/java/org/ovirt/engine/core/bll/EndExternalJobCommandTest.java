package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.ovirt.engine.core.common.action.EndExternalJobParameters;
import org.ovirt.engine.core.common.job.Job;
import org.ovirt.engine.core.common.job.JobExecutionStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.JobDao;
import org.slf4j.Logger;

public class EndExternalJobCommandTest extends BaseCommandTest {
    private static final Guid jobId = Guid.newGuid();
    private EndExternalJobParameters parameters =
            new EndExternalJobParameters(jobId, JobExecutionStatus.FINISHED, false);
    @Mock
    private JobDao jobDaoMock;

    @Spy
    @InjectMocks
    private EndExternalJobCommand<EndExternalJobParameters> commandMock = new EndExternalJobCommand<>(parameters, null);
    @Mock
    private Logger log;

    private Job makeExternalTestJob() {
        Job job = new Job();
        job.setId(jobId);
        job.setDescription("Sample Job");
        job.setExternal(true);
        return job;
    }

    private Job makeNonExternalTestJob() {
        Job job = new Job();
        job.setId(jobId);
        job.setDescription("Sample Job");
        job.setExternal(false);
        return job;
    }

    private void setupMock() throws Exception {
        doReturn(jobDaoMock).when(commandMock).getJobDao();
    }

    @Test
    public void validateOkSucceeds() throws Exception {
        setupMock();
        when(jobDaoMock.get(jobId)).thenReturn(makeExternalTestJob());
        assertTrue(commandMock.validate());
    }

    @Test
    public void validateNonExistingJobFails() throws Exception {
        setupMock();
        when(jobDaoMock.get(jobId)).thenReturn(null);
        assertTrue(! commandMock.validate());
    }

    @Test
    public void validateNonExternalJobFails() throws Exception {
        setupMock();
        when(jobDaoMock.get(jobId)).thenReturn(makeNonExternalTestJob());
        assertTrue(! commandMock.validate());
    }
}

package org.ovirt.engine.core.bll;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.ovirt.engine.core.common.action.EndExternalJobParameters;
import org.ovirt.engine.core.common.job.Job;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.JobDao;

public class EndExternalJobCommandTest extends BaseCommandTest {
    private static final Guid jobId = Guid.newGuid();
    private EndExternalJobParameters parameters = new EndExternalJobParameters(jobId, true, false);
    @Mock
    private JobDao jobDaoMock;

    @InjectMocks
    private EndExternalJobCommand<EndExternalJobParameters> commandMock = new EndExternalJobCommand<>(parameters, null);

    private Job makeJob() {
        Job job = new Job();
        job.setId(jobId);
        job.setDescription("Sample Job");
        return job;
    }

    private Job makeExternalTestJob() {
        Job job = makeJob();
        job.setExternal(true);
        return job;
    }

    private Job makeNonExternalTestJob() {
        Job job = makeJob();
        job.setExternal(false);
        return job;
    }

    @Test
    public void validateOkSucceeds() {
        when(jobDaoMock.get(jobId)).thenReturn(makeExternalTestJob());
        assertTrue(commandMock.validate());
    }

    @Test
    public void validateNonExistingJobFails() {
        when(jobDaoMock.get(jobId)).thenReturn(null);
        assertTrue(! commandMock.validate());
    }

    @Test
    public void validateNonExternalJobFails() {
        when(jobDaoMock.get(jobId)).thenReturn(makeNonExternalTestJob());
        assertTrue(! commandMock.validate());
    }
}

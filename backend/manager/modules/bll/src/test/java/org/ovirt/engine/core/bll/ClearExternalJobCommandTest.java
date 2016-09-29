package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.job.Job;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.JobDao;

public class ClearExternalJobCommandTest extends BaseCommandTest {

    private static final Guid jobId = Guid.newGuid();
    private static final Guid nonExistingJobId = Guid.newGuid();

    @Mock
    private JobDao jobDaoMock;

    @InjectMocks
    private ClearExternalJobCommand<VdcActionParametersBase> command =
            new ClearExternalJobCommand<>(new VdsActionParameters(), null);

    private Job makeTestJob(Guid jobId) {
        Job job = new Job();
        job.setId(jobId);
        job.setDescription("Sample Job");
        job.setExternal(true);
        return job;
    }

    @Before
    public void setupMock() throws Exception {
        when(jobDaoMock.get(jobId)).thenReturn(makeTestJob(jobId));
    }

    @Test
    public void validateOkSucceeds() throws Exception {
        command.getParameters().setJobId(jobId);
        assertTrue(command.validate());
    }

    @Test
    public void validateNonExistingJobFails() throws Exception {
        command.getParameters().setJobId(nonExistingJobId);
        assertTrue(!command.validate());
    }
}

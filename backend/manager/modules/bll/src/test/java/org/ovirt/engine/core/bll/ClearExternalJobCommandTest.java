package org.ovirt.engine.core.bll;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.job.Job;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.JobDao;

@MockitoSettings(strictness = Strictness.LENIENT)
public class ClearExternalJobCommandTest extends BaseCommandTest {

    private static final Guid jobId = Guid.newGuid();
    private static final Guid nonExistingJobId = Guid.newGuid();

    @Mock
    private JobDao jobDaoMock;

    @InjectMocks
    private ClearExternalJobCommand<ActionParametersBase> command =
            new ClearExternalJobCommand<>(new VdsActionParameters(), null);

    private Job makeTestJob(Guid jobId) {
        Job job = new Job();
        job.setId(jobId);
        job.setDescription("Sample Job");
        job.setExternal(true);
        return job;
    }

    @BeforeEach
    public void setupMock() {
        when(jobDaoMock.get(jobId)).thenReturn(makeTestJob(jobId));
    }

    @Test
    public void validateOkSucceeds() {
        command.getParameters().setJobId(jobId);
        assertTrue(command.validate());
    }

    @Test
    public void validateNonExistingJobFails() {
        command.getParameters().setJobId(nonExistingJobId);
        assertTrue(!command.validate());
    }
}

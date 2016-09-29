package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.ovirt.engine.core.common.action.AddExternalStepParameters;
import org.ovirt.engine.core.common.job.Job;
import org.ovirt.engine.core.common.job.StepEnum;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.JobDao;
import org.ovirt.engine.core.dao.StepDao;

public class AddExternalStepCommandTest extends BaseCommandTest {

    private static final Guid jobId = Guid.newGuid();
    private static final Guid nonExternalJobId = Guid.newGuid();
    private static final Guid nonExistingJobId = Guid.newGuid();
    @Mock
    private JobDao jobDaoMock;
    @Mock
    private StepDao stepDaoMock;

    @InjectMocks
    private AddExternalStepCommand<AddExternalStepParameters> command =
            new AddExternalStepCommand<>(new AddExternalStepParameters(jobId, "step 1", StepEnum.VALIDATING), null);

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

    @Before
    public void setupMock() throws Exception {
        when(jobDaoMock.get(jobId)).thenReturn(makeExternalTestJob(jobId));
        when(jobDaoMock.get(nonExternalJobId)).thenReturn(makeNonExternalTestJob(nonExternalJobId));
    }

    @Test
    public void validateOkSucceeds() throws Exception {
        assertTrue(command.validate());
    }

    @Test
    public void validateEmptyDescriptionFails() throws Exception {
        command.getParameters().setDescription("");
        assertTrue(! command.validate());
    }

    @Test
    public void validateBlankDescriptionFails() throws Exception {
        command.getParameters().setDescription("      ");
        assertTrue(! command.validate());
    }

    @Test
    public void validateNonExistingJobFails() throws Exception {
        command.getParameters().setParentId(nonExistingJobId);
        assertTrue(! command.validate());
    }

    @Test
    public void validateNonExternalJobFails() throws Exception {
        command.getParameters().setParentId(nonExternalJobId);
        command.getParameters().setStepId(null);
        assertTrue(! command.validate());
    }
}

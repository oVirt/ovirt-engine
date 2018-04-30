package org.ovirt.engine.core.bll;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.common.action.AddExternalStepParameters;
import org.ovirt.engine.core.common.job.Job;
import org.ovirt.engine.core.common.job.StepEnum;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.JobDao;
import org.ovirt.engine.core.dao.StepDao;

@MockitoSettings(strictness = Strictness.LENIENT)
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

    @BeforeEach
    public void setupMock() {
        when(jobDaoMock.get(jobId)).thenReturn(makeExternalTestJob(jobId));
        when(jobDaoMock.get(nonExternalJobId)).thenReturn(makeNonExternalTestJob(nonExternalJobId));
    }

    @Test
    public void validateOkSucceeds() {
        assertTrue(command.validate());
    }

    @Test
    public void validateEmptyDescriptionFails() {
        command.getParameters().setDescription("");
        assertTrue(! command.validate());
    }

    @Test
    public void validateBlankDescriptionFails() {
        command.getParameters().setDescription("      ");
        assertTrue(! command.validate());
    }

    @Test
    public void validateNonExistingJobFails() {
        command.getParameters().setParentId(nonExistingJobId);
        assertTrue(! command.validate());
    }

    @Test
    public void validateNonExternalJobFails() {
        command.getParameters().setParentId(nonExternalJobId);
        command.getParameters().setStepId(null);
        assertTrue(! command.validate());
    }
}

package org.ovirt.engine.core.bll;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.common.action.EndExternalStepParameters;
import org.ovirt.engine.core.common.job.Job;
import org.ovirt.engine.core.common.job.Step;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.JobDao;
import org.ovirt.engine.core.dao.StepDao;

@MockitoSettings(strictness = Strictness.LENIENT)
public class EndExternalStepCommandTest extends BaseCommandTest {

    private static final Guid jobId = Guid.newGuid();
    private static final Guid stepId = Guid.newGuid();
    private static final Guid nonExistingJobId = Guid.newGuid();
    private static final Guid nonExistingStepId = Guid.newGuid();
    private static final Guid nonExternalJobId = Guid.newGuid();
    private static final Guid nonExternalStepId = Guid.newGuid();
    private static final Guid nonExtistingJobstepId = Guid.newGuid();
    private static final Guid nonExternalJobstepId = Guid.newGuid();

    @Mock
    private JobDao jobDaoMock;
    @Mock
    private StepDao stepDaoMock;

    private EndExternalStepParameters parameters = new EndExternalStepParameters(stepId, true);

    @InjectMocks
    private EndExternalStepCommand<EndExternalStepParameters> command = new EndExternalStepCommand<>(parameters, null);

    private Job makeExternalTestJob(Guid jobId) {
        Job job = new Job();
        job.setId(jobId);
        job.setDescription("Sample Job");
        job.setExternal(true);
        return job;
    }

    private Step makeExternalTestStep(Guid id, Guid stepId) {
        Step step = new Step();
        step.setId(stepId);
        step.setJobId(id);
        step.setDescription("Sample Step");
        step.setExternal(true);
        return step;
    }

    private Job makeNonExternalTestJob(Guid jobId) {
        Job job = new Job();
        job.setId(jobId);
        job.setDescription("Sample Job");
        job.setExternal(false);
        return job;
    }

    private Step makeNonExternalTestStep(Guid id, Guid stepId) {
        Step step = new Step();
        step.setId(stepId);
        step.setJobId(id);
        step.setDescription("Sample Step");
        step.setExternal(false);
        return step;
    }

    @BeforeEach
    public void setupMock() {
        when(jobDaoMock.get(jobId)).thenReturn(makeExternalTestJob(jobId));
        when(jobDaoMock.get(nonExternalJobId)).thenReturn(makeNonExternalTestJob(nonExternalJobId));
        when(stepDaoMock.get(stepId)).thenReturn(makeExternalTestStep(jobId, stepId));
        when(stepDaoMock.get(nonExternalStepId)).thenReturn(makeNonExternalTestStep(nonExternalJobId, nonExternalStepId));
        when(stepDaoMock.get(nonExtistingJobstepId)).thenReturn(makeExternalTestStep(nonExistingJobId, nonExtistingJobstepId));
        when(stepDaoMock.get(nonExternalJobstepId)).thenReturn(makeExternalTestStep(nonExternalJobId, nonExternalJobstepId));
    }

    @Test
    public void validateOkSucceeds() {
        parameters.setId(stepId);
        parameters.setJobId(jobId);
        assertTrue(command.validate());
    }

    @Test
    public void validateNonExistingJobFails() {
        parameters.setId(nonExistingStepId);
        parameters.setJobId(jobId);
        assertTrue(! command.validate());
    }

    @Test
    public void validateNonExternalJobFails() {
        parameters.setId(nonExternalStepId);
        parameters.setJobId(jobId);
        assertTrue(! command.validate());
    }

    @Test
    public void validateNonExistingStepFails() {
        parameters.setId(nonExistingStepId);
        parameters.setJobId(jobId);
        assertTrue(! command.validate());
    }

    @Test
    public void validateNonExternalStepFails() {
        parameters.setJobId(jobId);
        parameters.setId(nonExternalStepId);
        assertTrue(! command.validate());
    }
}

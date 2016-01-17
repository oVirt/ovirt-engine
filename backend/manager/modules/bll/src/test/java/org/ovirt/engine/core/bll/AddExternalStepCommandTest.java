package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.common.action.AddExternalStepParameters;
import org.ovirt.engine.core.common.job.Job;
import org.ovirt.engine.core.common.job.StepEnum;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.JobDao;
import org.ovirt.engine.core.dao.StepDao;
import org.slf4j.Logger;

public class AddExternalStepCommandTest extends BaseCommandTest {

    private AddExternalStepParameters parameters;
    private static final Guid jobId = Guid.newGuid();
    private static final Guid nonExternalJobId = Guid.newGuid();
    private static final Guid nonExistingJobId = Guid.newGuid();
    @Mock
    private JobDao jobDaoMock;
    @Mock
    private StepDao stepDaoMock;

    @Mock
    private AddExternalStepCommand<AddExternalStepParameters> commandMock;
    @Mock
    private Logger log;

    @Before
    public void createParameters() {
        parameters = new AddExternalStepParameters(jobId, "step 1", StepEnum.VALIDATING);
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
        commandMock = spy(new AddExternalStepCommand<>(parameters, null));
        when(commandMock.getParameters()).thenReturn(parameters);
        doReturn(jobDaoMock).when(commandMock).getJobDao();
        doReturn(stepDaoMock).when(commandMock).getStepDao();
        when(jobDaoMock.get(jobId)).thenReturn(makeExternalTestJob(jobId));
        when(jobDaoMock.get(nonExternalJobId)).thenReturn(makeNonExternalTestJob(nonExternalJobId));
        when(jobDaoMock.get(nonExistingJobId)).thenReturn(null);
        when(stepDaoMock.get(any(Guid.class))).thenReturn(null);
    }

    @Test
    public void validateOkSucceeds() throws Exception {
        setupMock();
        assertTrue(commandMock.validate());
    }

    @Test
    public void validateEmptyDescriptionFails() throws Exception {
        setupMock();
        parameters.setDescription("");
        assertTrue(! commandMock.validate());
    }

    @Test
    public void validateBlankDescriptionFails() throws Exception {
        setupMock();
        parameters.setDescription("      ");
        assertTrue(! commandMock.validate());
    }

    @Test
    public void validateNonExistingJobFails() throws Exception {
        setupMock();
        parameters.setParentId(nonExistingJobId);
        assertTrue(! commandMock.validate());
    }

    @Test
    public void validateNonExternalJobFails() throws Exception {
        setupMock();
        parameters.setParentId(nonExternalJobId);
        parameters.setStepId(null);
        assertTrue(! commandMock.validate());
    }
}

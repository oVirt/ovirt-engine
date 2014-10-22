package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.action.AddExternalStepParameters;
import org.ovirt.engine.core.common.job.Job;
import org.ovirt.engine.core.common.job.StepEnum;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.JobDao;
import org.ovirt.engine.core.dao.StepDao;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class AddExternalStepCommandTest {

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
        commandMock = spy(new AddExternalStepCommand<AddExternalStepParameters>(parameters));
        when(commandMock.getParameters()).thenReturn(parameters);
        doReturn(jobDaoMock).when(commandMock).getJobDao();
        doReturn(stepDaoMock).when(commandMock).getStepDao();
        when(jobDaoMock.get(jobId)).thenReturn(makeExternalTestJob(jobId));
        when(jobDaoMock.get(nonExternalJobId)).thenReturn(makeNonExternalTestJob(nonExternalJobId));
        when(jobDaoMock.get(nonExistingJobId)).thenReturn(null);
        when(stepDaoMock.get(any(Guid.class))).thenReturn(null);
    }

    @Test
    public void canDoActionOkSucceeds() throws Exception {
        setupMock();
        assertTrue(commandMock.canDoAction());
    }

    @Test
    public void canDoActionEmptyDescriptionFails() throws Exception {
        setupMock();
        parameters.setDescription("");
        assertTrue(! commandMock.canDoAction());
    }

    @Test
    public void canDoActionBlankDescriptionFails() throws Exception {
        setupMock();
        parameters.setDescription("      ");
        assertTrue(! commandMock.canDoAction());
    }

    @Test
    public void canDoActionNonExistingJobFails() throws Exception {
        setupMock();
        parameters.setParentId(nonExistingJobId);
        assertTrue(! commandMock.canDoAction());
    }

    @Test
    public void canDoActionNonExternalJobFails() throws Exception {
        setupMock();
        parameters.setParentId(nonExternalJobId);
        parameters.setStepId(null);
        assertTrue(! commandMock.canDoAction());
    }
}

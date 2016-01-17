package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.job.Job;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.JobDao;
import org.slf4j.Logger;

public class ClearExternalJobCommandTest extends BaseCommandTest {

    private static final Guid jobId = Guid.newGuid();
    private static final Guid nonExistingJobId = Guid.newGuid();
    private VdcActionParametersBase parameters;
    @Mock
    private JobDao jobDaoMock;
    @Mock
    private ClearExternalJobCommand<VdcActionParametersBase> commandMock;
    @Mock
    private Logger log;

    @Before
    public void createParameters() {
        parameters = new VdcActionParametersBase();
        parameters.setJobId(jobId);
    }

    private Job makeTestJob(Guid jobId) {
        Job job = new Job();
        job.setId(jobId);
        job.setDescription("Sample Job");
        job.setExternal(true);
        return job;
    }

    private void setupMock() throws Exception {
        commandMock = spy(new ClearExternalJobCommand<>(parameters, null));
        when(commandMock.getParameters()).thenReturn(parameters);
        doReturn(jobDaoMock).when(commandMock).getJobDao();
        when(jobDaoMock.get(jobId)).thenReturn(makeTestJob(jobId));
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
}

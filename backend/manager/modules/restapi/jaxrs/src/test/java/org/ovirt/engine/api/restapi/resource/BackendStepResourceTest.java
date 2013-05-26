package org.ovirt.engine.api.restapi.resource;

import java.util.Date;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.junit.Test;
import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.Step;
import org.ovirt.engine.core.common.action.EndExternalStepParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendStepResourceTest
        extends AbstractBackendSubResourceTest<Step, org.ovirt.engine.core.common.job.Step, BackendStepResource> {


    public BackendStepResourceTest() {
        super(new BackendStepResource(GUIDS[0].toString(), new BackendStepsResource(GUIDS[1])));
    }

    @Test
    public void testGetNotFound() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(true);
        control.replay();
        try {
            resource.get();
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testGet() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations();
        control.replay();
        verifyModel(resource.get(), 0);
    }

    @Test
    public void testEnd() throws Exception {
        setUriInfo(setUpActionExpectations(VdcActionType.EndExternalStep,
                EndExternalStepParameters.class,
              new String[] { "Id", "Status"},
              new Object[] { GUIDS[0], true}, true, true));
        Action action = new Action();
        action.setSucceeded(true);
        verifyActionResponse(resource.end(action));
    }

    protected void setUpGetEntityExpectations() throws Exception {
        setUpGetEntityExpectations(false);
    }

    protected void setUpGetEntityExpectations(boolean notFound) throws Exception {
        setUpGetEntityExpectations(VdcQueryType.GetStepByStepId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[0] },
                notFound ? null : getEntity(0));
    }

    @Override
    protected org.ovirt.engine.core.common.job.Step getEntity(int index) {
        int parentIndex = 1;
        org.ovirt.engine.core.common.job.Step step = new org.ovirt.engine.core.common.job.Step();
        step.setId(GUIDS[index]);
        step.setStepType(org.ovirt.engine.core.common.job.StepEnum.VALIDATING);
        step.setJobId(GUIDS[parentIndex]);
        step.setDescription(DESCRIPTIONS[index]);
        step.setExternal(true);
        step.setStartTime(new Date());
        return step;
    }

    @Override
    protected void verifyModel(Step model, int index) {
        assertEquals(GUIDS[index].toString(), model.getId());
        assertEquals(DESCRIPTIONS[index], model.getDescription());
        assertTrue(model.isExternal());
        assertNotNull(model.getJob());
        verifyLinks(model);
    }

    private void verifyActionResponse(Response r) throws Exception {
        verifyActionResponse(r, "jobs/" + GUIDS[1] + "/steps/" + GUIDS[0]  , false);
    }
}


package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.Step;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.EndExternalStepParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendStepResourceTest
        extends AbstractBackendSubResourceTest<Step, org.ovirt.engine.core.common.job.Step, BackendStepResource> {


    public BackendStepResourceTest() {
        super(new BackendStepResource(GUIDS[0].toString(), new BackendStepsResource(GUIDS[1])));
    }

    @Test
    public void testGetNotFound() {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(true);
        verifyNotFoundException(assertThrows(WebApplicationException.class, resource::get));
    }

    @Test
    public void testGet() {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations();
        verifyModel(resource.get(), 0);
    }

    @Test
    public void testEnd() {
        setUriInfo(setUpActionExpectations(ActionType.EndExternalStep,
                EndExternalStepParameters.class,
              new String[] { "Id", "Status"},
              new Object[] { GUIDS[0], true}, true, true));
        Action action = new Action();
        action.setSucceeded(true);
        verifyActionResponse(resource.end(action));
    }

    protected void setUpGetEntityExpectations() {
        setUpGetEntityExpectations(false);
    }

    protected void setUpGetEntityExpectations(boolean notFound) {
        setUpGetEntityExpectations(QueryType.GetStepWithSubjectEntitiesByStepId,
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

    private void verifyActionResponse(Response r) {
        verifyActionResponse(r, "jobs/" + GUIDS[1] + "/steps/" + GUIDS[0]  , false);
    }
}


package org.ovirt.engine.api.restapi.resource;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.ws.rs.core.Response;

import org.junit.Ignore;
import org.junit.Test;
import org.ovirt.engine.api.model.Job;
import org.ovirt.engine.api.model.Step;
import org.ovirt.engine.api.model.StepEnum;
import org.ovirt.engine.api.model.StepStatus;
import org.ovirt.engine.core.common.action.AddExternalStepParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.queries.GetStepsByJobIdQueryParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendStepsResourceTest extends AbstractBackendCollectionResourceTest<Step, org.ovirt.engine.core.common.job.Step, BackendStepsResource> {

   public BackendStepsResourceTest() {
       super(new BackendStepsResource(GUIDS[1]), null, "");
    }
    @Override
    protected List<Step> getCollection() {
        return collection.list().getSteps();
    }

    @Test
    @Ignore
    @Override
    public void testQuery() throws Exception {
    }

    @Override
    protected void setUpQueryExpectations(String query, Object failure) throws Exception {
        assertEquals("", query);

        setUpEntityQueryExpectations(VdcQueryType.GetStepsByJobId,
                GetStepsByJobIdQueryParameters.class,
                                     new String[] {"JobId" },
                                     new Object[] {GUIDS[1] },
                                     setUpSteps(),
                                     failure);

        control.replay();
    }

    @Override
    protected org.ovirt.engine.core.common.job.Step getEntity(int index) {
        int jobIndex = index == 0 ? 1 : index -1;
        org.ovirt.engine.core.common.job.Step step = new org.ovirt.engine.core.common.job.Step();
        step.setId(GUIDS[index]);
        step.setJobId(GUIDS[jobIndex]);
        step.setDescription(DESCRIPTIONS[index]);
        step.setStepType(org.ovirt.engine.core.common.job.StepEnum.EXECUTING);
        step.setStartTime(new Date());
        step.setExternal(true);
        return step;
    }

    @Override
    protected void verifyModel(Step model, int index) {
        assertEquals(GUIDS[index].toString(), model.getId());
        assertEquals(DESCRIPTIONS[index], model.getDescription());
        assertTrue(model.isExternal());
        verifyLinks(model);
     }

    protected List<org.ovirt.engine.core.common.job.Step> setUpSteps() {
        List<org.ovirt.engine.core.common.job.Step> steps = new ArrayList<>();
        for (int i = 0; i < NAMES.length; i++) {
            steps.add(getEntity(i));
        }
        return steps;
    }

    @Test
    public void testAddStep() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpCreationExpectations(VdcActionType.AddExternalStep,
                                  AddExternalStepParameters.class,
                                  new String[] { "Description", "ParentId"},
                                  new Object[] { DESCRIPTIONS[0], GUIDS[1] },
                                  true,
                                  true,
                                  GUIDS[0],
                                  VdcQueryType.GetStepByStepId,
                                  IdQueryParameters.class,
                                  new String[] { "Id" },
                                  new Object[] { GUIDS[0] },
                                  getEntity(0));
        Step model = new Step();
        Job job = new Job();
        job.setId(GUIDS[1].toString());
        job.setDescription(DESCRIPTIONS[1]);
        model.setJob(job);
        model.setDescription(DESCRIPTIONS[0]);
        model.setStatus(StepStatus.STARTED);
        model.setType(StepEnum.EXECUTING);
        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof Step);
        verifyModel((Step) response.getEntity(), 0);
    }
}

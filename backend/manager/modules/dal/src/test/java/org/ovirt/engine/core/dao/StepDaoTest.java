package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.common.job.ExternalSystemType;
import org.ovirt.engine.core.common.job.JobExecutionStatus;
import org.ovirt.engine.core.common.job.Step;
import org.ovirt.engine.core.common.job.StepEnum;
import org.ovirt.engine.core.compat.Guid;

public class StepDaoTest extends BaseGenericDaoTestCase<Guid, Step, StepDao> {

    private static final Guid EXISTING_JOB_WITH_MULTIPLE_STEPS = new Guid("54947df8-0e9e-4471-a2f9-9af509fb5889");
    private static final int TOTAL_STEPS_OF_MULTI_STEP_JOB = 8;
    private static final Guid EXISTING_JOB_ID = new Guid("54947df8-0e9e-4471-a2f9-9af509fb5111");
    private static final Guid EXISTING_STEP_ID = new Guid("54947df8-0e9e-4471-a2f9-9af509111111");
    private static final int TOTAL_STEPS = 13;
    private static final Guid EXISTING_STEP_WITH_SUB_STEPS = new Guid("54947df8-0e9e-4471-a2f9-9af509fb5223");
    private static final int TOTAL_STEPS_OF_PARENT_STEP = 3;
    private static final Guid IN_PROGRESS_JOB_ID = new Guid("54947df8-0e9e-4471-a2f9-9af509fb5333");
    private static final Guid IN_PROGRESS_STEP_ID = new Guid("54947df8-0e9e-4471-a2f9-9af509111333");
    private static final Guid IN_PROGRESS_REBALANCING_GLUSTER_VOLUME_TASK_ID = new Guid("44f714ed-2818-4350-b94a-8c3927e53f7c");
    private static final Guid REBALANCING_GLUSTER_VOLUME_STEP_ID = new Guid("cd75984e-1fd4-48fb-baf8-e45800a61a66");
    private static final int TOTAL_STEPS_OF_REBALANCING_GLUSTER_VOLUME = 1;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected Guid getExistingEntityId() {
        return EXISTING_STEP_ID;
    }

    @Override
    protected StepDao prepareDao() {
        return dbFacade.getStepDao();
    }

    @Override
    protected Guid generateNonExistingId() {
        return Guid.newGuid();
    }

    @Override
    protected int getEneitiesTotalCount() {
        return TOTAL_STEPS;
    }

    @Override
    protected Step generateNewEntity() {
        Step step = new Step(StepEnum.EXECUTING);
        step.setJobId(EXISTING_JOB_ID);
        step.setStepNumber(1);
        step.setDescription("Execution step");
        step.setCorrelationId("Some correlation id");
        return step;
    }

    @Override
    protected void updateExistingEntity() {
        existingEntity.setStatus(JobExecutionStatus.FINISHED);
        existingEntity.setEndTime(new Date());
    }

    @Test
    public void existStep() {
        assertTrue(dao.exists(EXISTING_STEP_ID));
    }

    @Test
    public void nonExistStep() {
        assertFalse(dao.exists(Guid.newGuid()));
    }

    @Test
    public void getStepsByJobId() {
        List<Step> steps = dao.getStepsByJobId(EXISTING_JOB_WITH_MULTIPLE_STEPS);
        assertEquals("Verify Job has steps", TOTAL_STEPS_OF_MULTI_STEP_JOB, steps.size());
    }

    @Test
    public void getStepsByParentStepId() {
        List<Step> steps = dao.getStepsByParentStepId(EXISTING_STEP_WITH_SUB_STEPS);
        assertEquals("Verify Job has steps", TOTAL_STEPS_OF_PARENT_STEP, steps.size());
    }

    @Test
    public void updateJobStepsCompleted() {
        Step step = dao.get(IN_PROGRESS_STEP_ID);
        assertNotNull("Started step with ID " + IN_PROGRESS_STEP_ID, step);
        step.setStatus(JobExecutionStatus.FINISHED);
        Date endTime = new Date();
        step.setEndTime(endTime);
        dao.updateJobStepsCompleted(IN_PROGRESS_JOB_ID, JobExecutionStatus.FINISHED, endTime);
        Step afterUpdate = dao.get(IN_PROGRESS_STEP_ID);
        assertEquals("Compare step to itself after update in DB", step, afterUpdate);

    }

    @Test
    public void getStepsByExternalId(){
        List<Step> steps = dao.getStepsByExternalId(IN_PROGRESS_REBALANCING_GLUSTER_VOLUME_TASK_ID);
        assertEquals("Verify Rebalancing Gluster Volume Job has steps", TOTAL_STEPS_OF_REBALANCING_GLUSTER_VOLUME, steps.size());
        Step step = steps.get(0);
        assertTrue("Verify the Step Type status", StepEnum.REBALANCING_VOLUME == step.getStepType());
        assertTrue("Verify the Step status", JobExecutionStatus.STARTED == step.getStatus());
        assertEquals("Invalid Step", REBALANCING_GLUSTER_VOLUME_STEP_ID, step.getId());
    }

    @Test
    public void getExternalIdsForRunningSteps(){
        List<Guid> externalIds = dao.getExternalIdsForRunningSteps(ExternalSystemType.GLUSTER);
        assertEquals("Verify external ids present", 1, externalIds.size());
        assertEquals("Invalid TaskId", IN_PROGRESS_REBALANCING_GLUSTER_VOLUME_TASK_ID, externalIds.get(0));
    }
}

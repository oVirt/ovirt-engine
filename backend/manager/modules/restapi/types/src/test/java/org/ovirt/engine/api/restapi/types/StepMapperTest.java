package org.ovirt.engine.api.restapi.types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.api.model.Step;
import org.ovirt.engine.api.model.StepEnum;
import org.ovirt.engine.api.model.StepStatus;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.job.StepSubjectEntity;
import org.ovirt.engine.core.compat.DateTime;
import org.ovirt.engine.core.compat.Guid;

public class StepMapperTest extends AbstractInvertibleMappingTest<Step, org.ovirt.engine.core.common.job.Step, org.ovirt.engine.core.common.job.Step> {

    public StepMapperTest() {
        super(org.ovirt.engine.api.model.Step.class,
                org.ovirt.engine.core.common.job.Step.class,
                org.ovirt.engine.core.common.job.Step.class);
    }

    @Override
    protected void verify(Step model, Step transform) {
        assertNotNull(transform);
        assertEquals(model.getId(), transform.getId());
        assertEquals(model.getDescription(), transform.getDescription());
        assertEquals(model.getJob().getId(), transform.getJob().getId());
        if (model.getParentStep() != null) {
            assertEquals(model.getParentStep().getId(), transform.getParentStep().getId());
        }
        assertEquals(model.getProgress(), transform.getProgress(), "unexpected progress");
    }

    @Override
    protected Step postPopulate(Step model) {
        model.setStatus(StepStatus.STARTED);
        model.setType(StepEnum.VALIDATING);
        return super.postPopulate(model);
    }

    @Test
    public void testSubjectEntities() {
        org.ovirt.engine.core.common.job.Step bllStep = createBLLStep();
        Guid executionHostId = Guid.newGuid();
        bllStep.setSubjectEntities(
                Arrays.asList(new StepSubjectEntity(bllStep.getId(), VdcObjectType.EXECUTION_HOST, executionHostId),
                        new StepSubjectEntity(bllStep.getId(), VdcObjectType.Disk, Guid.Empty)));

        Step model = StepMapper.map(bllStep, null);
        assertNotNull(model.getExecutionHost());
        assertEquals(executionHostId.toString(), model.getExecutionHost().getId());

        bllStep = StepMapper.map(model, null);
        assertNull(bllStep.getSubjectEntities(), "subject entities shouldn't be mapped back to the model");
    }

    private org.ovirt.engine.core.common.job.Step createBLLStep() {
        org.ovirt.engine.core.common.job.Step bllStep = new org.ovirt.engine.core.common.job.Step();
        bllStep.setId(Guid.Empty);
        bllStep.setJobId(Guid.Empty);
        bllStep.setStartTime(DateTime.getNow());
        bllStep.setEndTime(DateTime.getNow());
        bllStep.setStepType(org.ovirt.engine.core.common.job.StepEnum.EXECUTING);
        return bllStep;
    }
}


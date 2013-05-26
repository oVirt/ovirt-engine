package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.Step;

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
    }

    @Override
    protected Step postPopulate(Step model) {
        model.getStatus().setState("started");
        model.setType("validating");
        return super.postPopulate(model);
    }

}


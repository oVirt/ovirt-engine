package org.ovirt.engine.api.restapi.types;


import org.ovirt.engine.api.model.Job;

public class JobMapperTest extends AbstractInvertibleMappingTest<Job, org.ovirt.engine.core.common.job.Job, org.ovirt.engine.core.common.job.Job> {

    public JobMapperTest() {
        super(org.ovirt.engine.api.model.Job.class,
                org.ovirt.engine.core.common.job.Job.class,
                org.ovirt.engine.core.common.job.Job.class);
    }

    @Override
    protected void verify(Job model, Job transform) {
        assertNotNull(transform);
        assertEquals(model.getId(), transform.getId());
        assertEquals(model.getDescription(), transform.getDescription());
    }

    @Override
    protected Job postPopulate(Job model) {
        model.getStatus().setState("started");
        return super.postPopulate(model);
    }

}


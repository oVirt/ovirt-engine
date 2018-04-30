package org.ovirt.engine.api.restapi.types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.ovirt.engine.api.model.Job;
import org.ovirt.engine.api.model.JobStatus;

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
        model.setStatus(JobStatus.STARTED);
        return super.postPopulate(model);
    }

}


package org.ovirt.engine.core.dao;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.VmBlockJob;
import org.ovirt.engine.core.common.businessentities.VmBlockJobType;
import org.ovirt.engine.core.common.businessentities.VmJob;
import org.ovirt.engine.core.common.businessentities.VmJobState;
import org.ovirt.engine.core.common.businessentities.VmJobType;
import org.ovirt.engine.core.compat.Guid;

public class VmJobDaoTest extends BaseDaoTestCase {
    private VmJobDao dao;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        dao = dbFacade.getVmJobDao();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetForwardCompatibility() {
        dao.get(Guid.newGuid());
        fail("VmJobDao.get(Guid) isn't implemented yet. If you implement it, don't forget to implement this test too");
    }

    @Test
    public void testGetAll() {
        List<Guid> ids = dao.getAll().stream().map(VmJob::getId).collect(toList());
        assertTrue(ids.remove(FixturesTool.EXISTING_VM_JOB));
        assertTrue(ids.remove(FixturesTool.EXISTING_VM_BLOCK_JOB));
        assertTrue(ids.isEmpty());
    }

    @Test
    public void testGetAllForVmWithJob() {
        List<VmJob> jobs = dao.getAll().stream()
                .filter(job -> job.getVmId().equals(FixturesTool.VM_RHEL5_POOL_57))
                .collect(toList());
        assertEquals(1, jobs.size());
        assertEquals(FixturesTool.EXISTING_VM_JOB, jobs.get(0).getId());
    }

    @Test
    public void testGetAllForVmWithBlockJob() {
        List<VmJob> jobs = dao.getAll().stream()
                .filter(job -> job.getVmId().equals(FixturesTool.VM_RHEL5_POOL_59))
                .collect(toList());
        assertEquals(1, jobs.size());
        assertEquals(FixturesTool.EXISTING_VM_BLOCK_JOB, jobs.get(0).getId());
        assertTrue(jobs.get(0) instanceof VmBlockJob);
    }

    @Test
    public void testGetAllForVmWithNonExistentVm() {
        List<VmJob> jobs = dao.getAll().stream()
                .filter(job -> job.getVmId().equals(Guid.Empty))
                .collect(toList());
        assertTrue(jobs.isEmpty());
    }

    @Test
    public void testDelete() {
        dao.remove(FixturesTool.EXISTING_VM_JOB);
        List<Guid> ids = dao.getAll().stream().map(VmJob::getId).collect(toList());
        assertTrue(ids.remove(FixturesTool.EXISTING_VM_BLOCK_JOB));
        assertTrue(ids.isEmpty());
    }

    @Test
    public void testInsertVmJob() {
        // Create a job in memory
        VmJob job = new VmJob();
        job.setId(Guid.newGuid());
        job.setVmId(FixturesTool.VM_RHEL5_POOL_60);
        job.setJobState(VmJobState.NORMAL);
        job.setJobType(VmJobType.UNKNOWN);

        assertInsert(job);
    }

    @Test
    public void testInsertVmBlockJob() {
        // Create a job in memory
        VmBlockJob job = new VmBlockJob();
        job.setId(Guid.newGuid());
        job.setVmId(FixturesTool.VM_RHEL5_POOL_50);
        job.setJobState(VmJobState.NORMAL);

        // Update block job fields
        job.setBlockJobType(VmBlockJobType.COMMIT);
        job.setBandwidth(16L);
        job.setCursorCur(11L);
        job.setCursorEnd(1981L);
        job.setImageGroupId(FixturesTool.IMAGE_GROUP_ID);

        assertInsert(job);
    }

    private void assertInsert(VmJob job) {
        // Save it
        dao.save(job);

        // Retrieve it
        List<VmJob> jobs = dao.getAll();
        assertThat(jobs, hasItem(job));

        // Be kind, rewind
        dao.remove(job.getId());
    }

    @Test
    public void testUpdate() {
        // Get a block job
        VmBlockJob job = (VmBlockJob) dao.getAll().stream()
                .filter(j -> j.getId().equals(FixturesTool.EXISTING_VM_BLOCK_JOB))
                .findFirst().orElse(null);

        // Make some changes
        job.setBandwidth(1981L);
        job.setJobState(VmJobState.UNKNOWN);
        dao.update(job);

        // Get the job again
        VmBlockJob updatedJob =  (VmBlockJob) dao.getAll().stream()
                .filter(j -> j.getId().equals(FixturesTool.EXISTING_VM_BLOCK_JOB))
                .findFirst().orElse(null);

        assertEquals(job, updatedJob);
    }

}

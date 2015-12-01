package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.compat.Guid;

public class VmAndTemplatesGenerationsDaoTest extends BaseDaoTestCase{

    private VmAndTemplatesGenerationsDao vmAndTemplatesGenerationsDao;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        vmAndTemplatesGenerationsDao = dbFacade.getVmAndTemplatesGenerationsDao();
    }

    @Test
    public void testGetOvfGenerations() {
        Long value = vmAndTemplatesGenerationsDao.getOvfGeneration(FixturesTool.VM_RHEL5_POOL_50);
        assertNotNull("ovf generation wasn't retrieved succesfully", value);
        assertEquals("ovf generation was retrieved but it's value isn't as expected", 1, value.longValue());
    }

    @Test
    public void testUpdateOvfGenerations() {
        List<Guid> vmsGuids = new LinkedList<>();
        vmsGuids.add(FixturesTool.VM_RHEL5_POOL_50);
        vmsGuids.add(FixturesTool.VM_RHEL5_POOL_51);
        long updatedOvfForVm50 = 1000;
        long updatedOvfForVm51 = 1001;
        List<Long> ovfVersions = new LinkedList<>();
        ovfVersions.add(updatedOvfForVm50);
        ovfVersions.add(updatedOvfForVm51);
        List<String> ovfData = Arrays.asList("a", "b");
        vmAndTemplatesGenerationsDao.updateOvfGenerations(vmsGuids, ovfVersions, ovfData);

        long dbRecievedOvfVer = vmAndTemplatesGenerationsDao.getOvfGeneration(FixturesTool.VM_RHEL5_POOL_50);
        assertEquals("ovf generations weren't updated properly", updatedOvfForVm50, dbRecievedOvfVer);

        dbRecievedOvfVer = vmAndTemplatesGenerationsDao.getOvfGeneration(FixturesTool.VM_RHEL5_POOL_51);
        assertEquals("ovf generations weren't updated properly", updatedOvfForVm51, dbRecievedOvfVer);
    }

    @Test
    public void testDeleteOvfGenerations() {
        List<Guid> vmsGuids = new LinkedList<>();
        vmsGuids.add(FixturesTool.VM_RHEL5_POOL_50);
        vmsGuids.add(FixturesTool.VM_RHEL5_POOL_51);

        VmDao vmDao = dbFacade.getVmDao();
        vmDao.remove(FixturesTool.VM_RHEL5_POOL_50);
        vmDao.remove(FixturesTool.VM_RHEL5_POOL_51);
        vmAndTemplatesGenerationsDao.deleteOvfGenerations(vmsGuids);
        Long value = vmAndTemplatesGenerationsDao.getOvfGeneration(FixturesTool.VM_RHEL5_POOL_50);
        assertNull("ovf generation wasn't deleted succesfully", value);
        value = vmAndTemplatesGenerationsDao.getOvfGeneration(FixturesTool.VM_RHEL5_POOL_51);
        assertNull("ovf generation wasn't deleted succesfully", value);
    }

    @Test
    public void testGetVmTemplatesIdsForOvfUpdateNoForUpdate() {
        List<Guid> guids = vmAndTemplatesGenerationsDao.getVmTemplatesIdsForOvfUpdate(FixturesTool.STORAGE_POOL_RHEL6_ISCSI_OTHER);
        assertTrue("there shouldn't be any templates that needs ovf update", guids.isEmpty());
    }

    @Test
    public void testGetVmTemplatesIdsForOvfUpdateOneTemplate() {
        vmAndTemplatesGenerationsDao.updateOvfGenerations(Collections.singletonList(FixturesTool.VM_TEMPLATE_RHEL5), Collections.singletonList(Long.valueOf(0)), Arrays.asList("a"));

        List<Guid> guids = vmAndTemplatesGenerationsDao.getVmTemplatesIdsForOvfUpdate(FixturesTool.STORAGE_POOL_RHEL6_ISCSI_OTHER);
        assertEquals("one template should need ovf update", 1, guids.size());
        assertEquals("wrong template returned as in need for ovf update", guids.get(0), FixturesTool.VM_TEMPLATE_RHEL5);
    }

    @Test
    public void testGetVmTemplatesIdsForOvfUpdateTwoTemplatesSamePool() {
        List<Guid> templates = new LinkedList<>();
        templates.add(FixturesTool.VM_TEMPLATE_RHEL5);
        templates.add(FixturesTool.VM_TEMPLATE_RHEL5_2);

        List<Long> values = new LinkedList<>();
        values.add(0L);
        values.add(0L);

        List<String> ovfData = new LinkedList<>();
        ovfData.add("a");
        ovfData.add("b");

        vmAndTemplatesGenerationsDao.updateOvfGenerations(templates, values, ovfData);

        List<Guid> guids = vmAndTemplatesGenerationsDao.getVmTemplatesIdsForOvfUpdate(FixturesTool.STORAGE_POOL_RHEL6_ISCSI_OTHER);
        assertEquals("two templates should need ovf update", 2, guids.size());
        assertEquals("templates ids for ovf update didn't contain expected id", guids.contains(FixturesTool.VM_TEMPLATE_RHEL5) , true);
        assertEquals("templates ids for ovf update didn't contain expected id", guids.contains(FixturesTool.VM_TEMPLATE_RHEL5_2) , true);
    }

    @Test
    public void testGetVmTemplatesIdsForOvfUpdateTemplatesDifferentPools() {
        List<Guid> templates = new LinkedList<>();
        // POOL : STORAGE_POOL_NFS
        templates.add(FixturesTool.VM_TEMPLATE_RHEL6_1);
        templates.add(FixturesTool.VM_TEMPLATE_RHEL6_2);
        // POOL : STORAGE_POOL_RHEL6_ISCSI_OTHER
        templates.add(FixturesTool.VM_TEMPLATE_RHEL5);
        templates.add(FixturesTool.VM_TEMPLATE_RHEL5_2);

        List<Long> values = new LinkedList<>();
        values.add(0L);
        values.add(0L);
        values.add(0L);
        values.add(0L);

        List<String> ovfData = new LinkedList<>();
        ovfData.add("a");
        ovfData.add("b");
        ovfData.add("c");
        ovfData.add("d");

        vmAndTemplatesGenerationsDao.updateOvfGenerations(templates, values, ovfData);

        List<Guid> guids = vmAndTemplatesGenerationsDao.getVmTemplatesIdsForOvfUpdate(FixturesTool.STORAGE_POOL_RHEL6_ISCSI_OTHER);
        assertEquals("two templates should need ovf update", 2, guids.size());
        assertEquals("templates ids for ovf update didn't contain expected id", guids.contains(FixturesTool.VM_TEMPLATE_RHEL5), true);
        assertEquals("templates ids for ovf update didn't contain expected id", guids.contains(FixturesTool.VM_TEMPLATE_RHEL5_2), true);


        guids = vmAndTemplatesGenerationsDao.getVmTemplatesIdsForOvfUpdate(FixturesTool.STORAGE_POOL_MIXED_TYPES);
        assertEquals("two templates should need ovf update", 2, guids.size());
        assertEquals("templates ids for ovf update didn't contain expected id", guids.contains(FixturesTool.VM_TEMPLATE_RHEL6_1), true);
        assertEquals("templates ids for ovf update didn't contain expected id", guids.contains(FixturesTool.VM_TEMPLATE_RHEL6_2), true);
    }

    @Test
    public void testGetVmsIdsForOvfUpdateNoneForUpdate() {
        List<Guid> guids = vmAndTemplatesGenerationsDao.getVmsIdsForOvfUpdate(FixturesTool.STORAGE_POOL_RHEL6_ISCSI_OTHER);
        assertTrue("there shouldn't be any vms that needs ovf update", guids.isEmpty());
    }

    @Test
    public void testGetVmssIdsForOvfUpdateOneVm() {
        vmAndTemplatesGenerationsDao.updateOvfGenerations(Collections.singletonList(FixturesTool.VM_RHEL5_POOL_50), Collections.singletonList(Long.valueOf(0)), Arrays.asList("a"));

        List<Guid> guids = vmAndTemplatesGenerationsDao.getVmsIdsForOvfUpdate(FixturesTool.STORAGE_POOL_RHEL6_ISCSI_OTHER);
        assertEquals("one vm should need ovf update", 1, guids.size());
        assertEquals("wrong vm returned as in need for ovf update", guids.get(0), FixturesTool.VM_RHEL5_POOL_50);
    }

    @Test
    public void testGetVmTemplatesIdsForOvfUpdateTwoVmsSamePool() {
        List<Guid> vms = new LinkedList<>();
        vms.add(FixturesTool.VM_RHEL5_POOL_50);
        vms.add(FixturesTool.VM_RHEL5_POOL_51);

        List<Long> values = new LinkedList<>();
        values.add(0L);
        values.add(0L);

        List<String> ovfData = new LinkedList<>();
        ovfData.add("a");
        ovfData.add("b");

        vmAndTemplatesGenerationsDao.updateOvfGenerations(vms, values, ovfData);

        List<Guid> guids = vmAndTemplatesGenerationsDao.getVmsIdsForOvfUpdate(FixturesTool.STORAGE_POOL_RHEL6_ISCSI_OTHER);
        assertEquals("two vms should need ovf update", 2, guids.size());
        assertEquals("templates ids for ovf update didn't contain expected id", guids.contains(FixturesTool.VM_RHEL5_POOL_50), true);
        assertEquals("templates ids for ovf update didn't contain expected id", guids.contains(FixturesTool.VM_RHEL5_POOL_51), true);
    }

    @Test
    public void testGetVmsIdsForOvfUpdateDifferentPools() {
        List<Guid> vms = new LinkedList<>();
        // POOL : STORAGE_POOL_RHEL6_ISCSI_OTHER
        vms.add(FixturesTool.VM_RHEL5_POOL_50);
        vms.add(FixturesTool.VM_RHEL5_POOL_51);
        // POOL : STORAGE_POOL_NFS
        vms.add(FixturesTool.VM_RHEL5_POOL_59);
        vms.add(FixturesTool.VM_RHEL5_POOL_60);

        List<Long> values = new LinkedList<>();
        values.add(0L);
        values.add(0L);
        values.add(0L);
        values.add(0L);

        List<String> ovfData = new LinkedList<>();
        ovfData.add("a");
        ovfData.add("b");
        ovfData.add("c");
        ovfData.add("d");

        vmAndTemplatesGenerationsDao.updateOvfGenerations(vms, values, ovfData);

        List<Guid> guids = vmAndTemplatesGenerationsDao.getVmsIdsForOvfUpdate(FixturesTool.STORAGE_POOL_RHEL6_ISCSI_OTHER);
        assertEquals("two vms should need ovf update in pool:" +FixturesTool.STORAGE_POOL_RHEL6_ISCSI_OTHER , 2, guids.size());
        assertEquals("vms ids for ovf update didn't contain expected id", guids.contains(FixturesTool.VM_RHEL5_POOL_50), true);
        assertEquals("vms ids for ovf update didn't contain expected id", guids.contains(FixturesTool.VM_RHEL5_POOL_51), true);


        guids = vmAndTemplatesGenerationsDao.getVmsIdsForOvfUpdate(FixturesTool.STORAGE_POOL_MIXED_TYPES);
        assertEquals("two vms should need ovf update in pool" + FixturesTool.STORAGE_POOL_MIXED_TYPES, 2, guids.size());
        assertEquals("vm ids for ovf update didn't contain expected id", guids.contains(FixturesTool.VM_RHEL5_POOL_59), true);
        assertEquals("vm ids for ovf update didn't contain expected id", guids.contains(FixturesTool.VM_RHEL5_POOL_60), true);
    }

    @Test
    public void testGetVmsAndTemplatesIdsForOvfUpdateMixedInPool() {
        List<Guid> toUpdate = new LinkedList<>();
        // POOL : STORAGE_POOL_RHEL6_ISCSI_OTHER
        toUpdate.add(FixturesTool.VM_TEMPLATE_RHEL5);
        toUpdate.add(FixturesTool.VM_RHEL5_POOL_50);

        // POOL : STORAGE_POOL_MIXED_TYPES
        toUpdate.add(FixturesTool.VM_TEMPLATE_RHEL6_1);
        toUpdate.add(FixturesTool.VM_RHEL5_POOL_60);

        List<Long> values = new LinkedList<>();
        values.add(0L);
        values.add(0L);
        values.add(0L);
        values.add(0L);

        List<String> ovfData = new LinkedList<>();
        ovfData.add("a");
        ovfData.add("b");
        ovfData.add("c");
        ovfData.add("d");

        vmAndTemplatesGenerationsDao.updateOvfGenerations(toUpdate, values, ovfData);

        List<Guid> guids =
                vmAndTemplatesGenerationsDao.getVmTemplatesIdsForOvfUpdate(FixturesTool.STORAGE_POOL_RHEL6_ISCSI_OTHER);
        assertEquals("one template should need ovf update in pool " + FixturesTool.STORAGE_POOL_RHEL6_ISCSI_OTHER,
                1,
                guids.size());
        assertEquals("templates ids for ovf update didn't contain expected id in pool"
                + FixturesTool.STORAGE_POOL_RHEL6_ISCSI_OTHER
                , guids.contains(FixturesTool.VM_TEMPLATE_RHEL5), true);

        guids = vmAndTemplatesGenerationsDao.getVmsIdsForOvfUpdate(FixturesTool.STORAGE_POOL_RHEL6_ISCSI_OTHER);
        assertEquals("one vm should need ovf update in pool " + FixturesTool.STORAGE_POOL_RHEL6_ISCSI_OTHER,
                1,
                guids.size());
        assertEquals("vm ids for ovf update didn't contain expected id in pool"
                + FixturesTool.STORAGE_POOL_RHEL6_ISCSI_OTHER
                , guids.contains(FixturesTool.VM_RHEL5_POOL_50), true);

        guids = vmAndTemplatesGenerationsDao.getVmTemplatesIdsForOvfUpdate(FixturesTool.STORAGE_POOL_MIXED_TYPES);
        assertEquals("one template should need ovf update in pool" + FixturesTool.STORAGE_POOL_MIXED_TYPES, 1, guids.size());
        assertEquals("templates ids for ovf update didn't contain expected id in pool" + FixturesTool.STORAGE_POOL_NFS,
                guids.contains(FixturesTool.VM_TEMPLATE_RHEL6_1), true);

        guids = vmAndTemplatesGenerationsDao.getVmsIdsForOvfUpdate(FixturesTool.STORAGE_POOL_MIXED_TYPES);
        assertEquals("one vm should need ovf update in pool" + FixturesTool.STORAGE_POOL_MIXED_TYPES, 1, guids.size());
        assertEquals("vm ids for ovf update didn't contain expected id in pool" + FixturesTool.STORAGE_POOL_NFS,
                guids.contains(FixturesTool.VM_RHEL5_POOL_60),
                true);
    }

    @Test
    public void testGetIdsForOvfDeletionNoToDelete() {
        List<Guid> guidsToDelete = vmAndTemplatesGenerationsDao.getIdsForOvfDeletion(FixturesTool.STORAGE_POOL_RHEL6_ISCSI_OTHER);
        assertTrue("there shouldn't be any ovfs for deletion", guidsToDelete.isEmpty());
    }

    @Test
    public void testGetIdsForOvfDeletionOneToDelete() {
        VmDao vmDao= dbFacade.getVmDao();
        vmDao.remove(FixturesTool.VM_RHEL5_POOL_50);
        List<Guid> guidsToDelete = vmAndTemplatesGenerationsDao.getIdsForOvfDeletion(FixturesTool.STORAGE_POOL_RHEL6_ISCSI_OTHER);
        assertEquals("there should be 1 ovf for deletion", 1, guidsToDelete.size());
        assertTrue("the list of guids for deletion doesn't contain the expected guid",
                guidsToDelete.contains(FixturesTool.VM_RHEL5_POOL_50));
    }

    @Test
    public void testGetIdsForOvfDeletionTwoToDelete() {
        VmDao vmDao= dbFacade.getVmDao();
        vmDao.remove(FixturesTool.VM_RHEL5_POOL_50);
        vmDao.remove(FixturesTool.VM_RHEL5_POOL_51);
        List<Guid> guidsToDelete = vmAndTemplatesGenerationsDao.getIdsForOvfDeletion(FixturesTool.STORAGE_POOL_RHEL6_ISCSI_OTHER);
        assertEquals("there should be 2 ovfs for deletion", 2, guidsToDelete.size());
        assertTrue("the list of guids for deletion doesn't contain an expected guid",
                guidsToDelete.contains(FixturesTool.VM_RHEL5_POOL_50));
        assertTrue("the list of guids for deletion doesn't contain an expected guid",
                guidsToDelete.contains(FixturesTool.VM_RHEL5_POOL_51));
    }

    @Test
    public void testGetIdsForOvfDeletionOneInEachPool() {
        VmDao vmDao= dbFacade.getVmDao();
        vmDao.remove(FixturesTool.VM_RHEL5_POOL_50);
        vmDao.remove(FixturesTool.VM_RHEL5_POOL_60);

        List<Guid> guidsToDelete = vmAndTemplatesGenerationsDao.getIdsForOvfDeletion(FixturesTool.STORAGE_POOL_RHEL6_ISCSI_OTHER);
        assertEquals("the list of ovfs for deletion wasn't in the expected size", 1, guidsToDelete.size());
        assertTrue("the list of guids for deletion doesn't contain an expected guid",
                guidsToDelete.contains(FixturesTool.VM_RHEL5_POOL_50));

        guidsToDelete = vmAndTemplatesGenerationsDao.getIdsForOvfDeletion(FixturesTool.STORAGE_POOL_MIXED_TYPES);
        assertEquals("the list of ovfs for deletion wasn't in the expected size", 1, guidsToDelete.size());
        assertTrue("the list of guids for deletion doesn't contain an expected guid",
                guidsToDelete.contains(FixturesTool.VM_RHEL5_POOL_60));
    }

    @Test
    public void testGetIdsForOvfDeletionMultipleInEachPool() {
        VmDao vmDao= dbFacade.getVmDao();
        vmDao.remove(FixturesTool.VM_RHEL5_POOL_51);
        vmDao.remove(FixturesTool.VM_RHEL5_POOL_50);
        vmDao.remove(FixturesTool.VM_RHEL5_POOL_59);
        vmDao.remove(FixturesTool.VM_RHEL5_POOL_60);
        List<Guid> guidsToDelete = vmAndTemplatesGenerationsDao.getIdsForOvfDeletion(FixturesTool.STORAGE_POOL_RHEL6_ISCSI_OTHER);
        assertEquals("the list of ovfs for deletion wasn't in the expected size", 2, guidsToDelete.size());
        assertTrue("the list of guids for deletion doesn't contain an expected guid",
                guidsToDelete.contains(FixturesTool.VM_RHEL5_POOL_51));
        assertTrue("the list of guids for deletion doesn't contain an expected guid",
                guidsToDelete.contains(FixturesTool.VM_RHEL5_POOL_50));

        guidsToDelete = vmAndTemplatesGenerationsDao.getIdsForOvfDeletion(FixturesTool.STORAGE_POOL_MIXED_TYPES);
        assertEquals("the list of ovfs for deletion wasn't in the expected size", 2, guidsToDelete.size());
        assertTrue("the list of guids for deletion doesn't contain an expected guid",
                guidsToDelete.contains(FixturesTool.VM_RHEL5_POOL_60));
        assertTrue("the list of guids for deletion doesn't contain an expected guid",
                guidsToDelete.contains(FixturesTool.VM_RHEL5_POOL_59));
    }

    @Test
    public void testGetIdsForOvfDeletionMultipleVmsAndTemplatesInDifferentPools() {
        VmDao vmDao= dbFacade.getVmDao();
        VmTemplateDao templateDao = dbFacade.getVmTemplateDao();
        vmDao.remove(FixturesTool.VM_RHEL5_POOL_51);
        vmDao.remove(FixturesTool.VM_RHEL5_POOL_50);
        vmDao.remove(FixturesTool.VM_RHEL5_POOL_59);
        vmDao.remove(FixturesTool.VM_RHEL5_POOL_60);
        templateDao.remove(FixturesTool.VM_TEMPLATE_RHEL6_1);
        templateDao.remove(FixturesTool.VM_TEMPLATE_RHEL6_2);


        List<Guid> guidsToDelete = vmAndTemplatesGenerationsDao.getIdsForOvfDeletion(FixturesTool.STORAGE_POOL_RHEL6_ISCSI_OTHER);
        assertEquals("unexpected number of ovfs for deletion", 2, guidsToDelete.size());
        assertTrue("the list of guids for deletion doesn't contain an expected guid",
                guidsToDelete.contains(FixturesTool.VM_RHEL5_POOL_51));
        assertTrue("the list of guids for deletion doesn't contain an expected guid",
                guidsToDelete.contains(FixturesTool.VM_RHEL5_POOL_50));

        guidsToDelete = vmAndTemplatesGenerationsDao.getIdsForOvfDeletion(FixturesTool.STORAGE_POOL_MIXED_TYPES);
        assertEquals("unexpected number of ovfs for deletion", 4, guidsToDelete.size());
        assertTrue("the list of guids for deletion doesn't contain an expected guid",
                guidsToDelete.contains(FixturesTool.VM_RHEL5_POOL_60));
        assertTrue("the list of guids for deletion doesn't contain an expected guid",
                guidsToDelete.contains(FixturesTool.VM_RHEL5_POOL_59));
        assertTrue("the list of guids for deletion doesn't contain an expected guid",
                guidsToDelete.contains(FixturesTool.VM_TEMPLATE_RHEL6_1));
        assertTrue("the list of guids for deletion doesn't contain an expected guid",
                guidsToDelete.contains(FixturesTool.VM_TEMPLATE_RHEL6_2));
    }
}

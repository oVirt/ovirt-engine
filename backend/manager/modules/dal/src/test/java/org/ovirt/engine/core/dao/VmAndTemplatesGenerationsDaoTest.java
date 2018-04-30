package org.ovirt.engine.core.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.compat.Guid;

public class VmAndTemplatesGenerationsDaoTest extends BaseDaoTestCase<VmAndTemplatesGenerationsDao> {
    @Inject
    private VmDao vmDao;
    @Inject
    private VmTemplateDao templateDao;

    @Test
    public void testGetOvfGenerations() {
        Long value = dao.getOvfGeneration(FixturesTool.VM_RHEL5_POOL_50);
        assertNotNull(value, "ovf generation wasn't retrieved succesfully");
        assertEquals(1, value.longValue(), "ovf generation was retrieved but it's value isn't as expected");
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
        dao.updateOvfGenerations(vmsGuids, ovfVersions, ovfData);

        long dbRecievedOvfVer = dao.getOvfGeneration(FixturesTool.VM_RHEL5_POOL_50);
        assertEquals(updatedOvfForVm50, dbRecievedOvfVer, "ovf generations weren't updated properly");

        dbRecievedOvfVer = dao.getOvfGeneration(FixturesTool.VM_RHEL5_POOL_51);
        assertEquals(updatedOvfForVm51, dbRecievedOvfVer, "ovf generations weren't updated properly");
    }

    @Test
    public void testDeleteOvfGenerations() {
        List<Guid> vmsGuids = new LinkedList<>();
        vmsGuids.add(FixturesTool.VM_RHEL5_POOL_50);
        vmsGuids.add(FixturesTool.VM_RHEL5_POOL_51);

        vmDao.remove(FixturesTool.VM_RHEL5_POOL_50);
        vmDao.remove(FixturesTool.VM_RHEL5_POOL_51);
        dao.deleteOvfGenerations(vmsGuids);
        Long value = dao.getOvfGeneration(FixturesTool.VM_RHEL5_POOL_50);
        assertNull(value, "ovf generation wasn't deleted succesfully");
        value = dao.getOvfGeneration(FixturesTool.VM_RHEL5_POOL_51);
        assertNull(value, "ovf generation wasn't deleted succesfully");
    }

    @Test
    public void testGetVmTemplatesIdsForOvfUpdateNoForUpdate() {
        List<Guid> guids = dao.getVmTemplatesIdsForOvfUpdate(FixturesTool.STORAGE_POOL_RHEL6_ISCSI_OTHER);
        assertTrue(guids.isEmpty(), "there shouldn't be any templates that needs ovf update");
    }

    @Test
    public void testGetVmTemplatesIdsForOvfUpdateOneTemplate() {
        dao.updateOvfGenerations(Collections.singletonList(FixturesTool.VM_TEMPLATE_RHEL5), Collections.singletonList(0L), Collections.singletonList("a"));

        List<Guid> guids = dao.getVmTemplatesIdsForOvfUpdate(FixturesTool.STORAGE_POOL_RHEL6_ISCSI_OTHER);
        assertEquals(1, guids.size(), "one template should need ovf update");
        assertEquals(FixturesTool.VM_TEMPLATE_RHEL5, guids.get(0), "wrong template returned as in need for ovf update");
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

        dao.updateOvfGenerations(templates, values, ovfData);

        List<Guid> guids = dao.getVmTemplatesIdsForOvfUpdate(FixturesTool.STORAGE_POOL_RHEL6_ISCSI_OTHER);
        assertEquals(2, guids.size(), "two templates should need ovf update");
        assertTrue(guids.contains(FixturesTool.VM_TEMPLATE_RHEL5), "templates ids for ovf update didn't contain expected id");
        assertTrue(guids.contains(FixturesTool.VM_TEMPLATE_RHEL5_2), "templates ids for ovf update didn't contain expected id");
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

        dao.updateOvfGenerations(templates, values, ovfData);

        List<Guid> guids = dao.getVmTemplatesIdsForOvfUpdate(FixturesTool.STORAGE_POOL_RHEL6_ISCSI_OTHER);
        assertEquals(2, guids.size(), "two templates should need ovf update");
        assertTrue(guids.contains(FixturesTool.VM_TEMPLATE_RHEL5), "templates ids for ovf update didn't contain expected id");
        assertTrue(guids.contains(FixturesTool.VM_TEMPLATE_RHEL5_2), "templates ids for ovf update didn't contain expected id");


        guids = dao.getVmTemplatesIdsForOvfUpdate(FixturesTool.STORAGE_POOL_MIXED_TYPES);
        assertEquals(2, guids.size(), "two templates should need ovf update");
        assertTrue(guids.contains(FixturesTool.VM_TEMPLATE_RHEL6_1), "templates ids for ovf update didn't contain expected id");
        assertTrue(guids.contains(FixturesTool.VM_TEMPLATE_RHEL6_2), "templates ids for ovf update didn't contain expected id");
    }

    @Test
    public void testGetVmsIdsForOvfUpdateNoneForUpdate() {
        List<Guid> guids = dao.getVmsIdsForOvfUpdate(FixturesTool.STORAGE_POOL_RHEL6_ISCSI_OTHER);
        assertTrue(guids.isEmpty(), "there shouldn't be any vms that needs ovf update");
    }

    @Test
    public void testGetVmssIdsForOvfUpdateOneVm() {
        dao.updateOvfGenerations(Collections.singletonList(FixturesTool.VM_RHEL5_POOL_50), Collections.singletonList(0L), Collections.singletonList("a"));

        List<Guid> guids = dao.getVmsIdsForOvfUpdate(FixturesTool.STORAGE_POOL_RHEL6_ISCSI_OTHER);
        assertEquals(1, guids.size(), "one vm should need ovf update");
        assertEquals(FixturesTool.VM_RHEL5_POOL_50, guids.get(0), "wrong vm returned as in need for ovf update");
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

        dao.updateOvfGenerations(vms, values, ovfData);

        List<Guid> guids = dao.getVmsIdsForOvfUpdate(FixturesTool.STORAGE_POOL_RHEL6_ISCSI_OTHER);
        assertEquals(2, guids.size(), "two vms should need ovf update");
        assertTrue(guids.contains(FixturesTool.VM_RHEL5_POOL_50), "templates ids for ovf update didn't contain expected id");
        assertTrue(guids.contains(FixturesTool.VM_RHEL5_POOL_51), "templates ids for ovf update didn't contain expected id");
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

        dao.updateOvfGenerations(vms, values, ovfData);

        List<Guid> guids = dao.getVmsIdsForOvfUpdate(FixturesTool.STORAGE_POOL_RHEL6_ISCSI_OTHER);
        assertEquals(2, guids.size(), "two vms should need ovf update in pool:" + FixturesTool.STORAGE_POOL_RHEL6_ISCSI_OTHER);
        assertTrue(guids.contains(FixturesTool.VM_RHEL5_POOL_50), "vms ids for ovf update didn't contain expected id");
        assertTrue(guids.contains(FixturesTool.VM_RHEL5_POOL_51), "vms ids for ovf update didn't contain expected id");


        guids = dao.getVmsIdsForOvfUpdate(FixturesTool.STORAGE_POOL_MIXED_TYPES);
        assertEquals(2, guids.size(), "two vms should need ovf update in pool" + FixturesTool.STORAGE_POOL_MIXED_TYPES);
        assertTrue(guids.contains(FixturesTool.VM_RHEL5_POOL_59), "vms ids for ovf update didn't contain expected id");
        assertTrue(guids.contains(FixturesTool.VM_RHEL5_POOL_60), "vms ids for ovf update didn't contain expected id");
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

        dao.updateOvfGenerations(toUpdate, values, ovfData);

        List<Guid> guids = dao.getVmTemplatesIdsForOvfUpdate(FixturesTool.STORAGE_POOL_RHEL6_ISCSI_OTHER);
        assertEquals(1, guids.size(),
                "one template should need ovf update in pool " + FixturesTool.STORAGE_POOL_RHEL6_ISCSI_OTHER);
        assertTrue(guids.contains(FixturesTool.VM_TEMPLATE_RHEL5),
                "templates ids for ovf update didn't contain expected id in pool" +
                        FixturesTool.STORAGE_POOL_RHEL6_ISCSI_OTHER);

        guids = dao.getVmsIdsForOvfUpdate(FixturesTool.STORAGE_POOL_RHEL6_ISCSI_OTHER);
        assertEquals(1, guids.size(),
                "one vm should need ovf update in pool " + FixturesTool.STORAGE_POOL_RHEL6_ISCSI_OTHER);
        assertTrue(guids.contains(FixturesTool.VM_RHEL5_POOL_50),
                "vm ids for ovf update didn't contain expected id in pool" +
                        FixturesTool.STORAGE_POOL_RHEL6_ISCSI_OTHER);

        guids = dao.getVmTemplatesIdsForOvfUpdate(FixturesTool.STORAGE_POOL_MIXED_TYPES);
        assertEquals(1, guids.size(), "one template should need ovf update in pool" + FixturesTool.STORAGE_POOL_MIXED_TYPES);
        assertTrue(guids.contains(FixturesTool.VM_TEMPLATE_RHEL6_1), "templates ids for ovf update didn't contain expected id in pool" + FixturesTool.STORAGE_POOL_NFS);

        guids = dao.getVmsIdsForOvfUpdate(FixturesTool.STORAGE_POOL_MIXED_TYPES);
        assertEquals(1, guids.size(), "one vm should need ovf update in pool" + FixturesTool.STORAGE_POOL_MIXED_TYPES);
        assertTrue(guids.contains(FixturesTool.VM_RHEL5_POOL_60), "vm ids for ovf update didn't contain expected id in pool" + FixturesTool.STORAGE_POOL_NFS);
    }

    @Test
    public void testGetIdsForOvfDeletionNoToDelete() {
        List<Guid> guidsToDelete = dao.getIdsForOvfDeletion(FixturesTool.STORAGE_POOL_RHEL6_ISCSI_OTHER);
        assertTrue(guidsToDelete.isEmpty(), "there shouldn't be any ovfs for deletion");
    }

    @Test
    public void testGetIdsForOvfDeletionOneToDelete() {
        vmDao.remove(FixturesTool.VM_RHEL5_POOL_50);
        List<Guid> guidsToDelete = dao.getIdsForOvfDeletion(FixturesTool.STORAGE_POOL_RHEL6_ISCSI_OTHER);
        assertEquals(1, guidsToDelete.size(), "there should be 1 ovf for deletion");
        assertTrue(guidsToDelete.contains(FixturesTool.VM_RHEL5_POOL_50),
                "the list of guids for deletion doesn't contain the expected guid");
    }

    @Test
    public void testGetIdsForOvfDeletionTwoToDelete() {
        vmDao.remove(FixturesTool.VM_RHEL5_POOL_50);
        vmDao.remove(FixturesTool.VM_RHEL5_POOL_51);
        List<Guid> guidsToDelete = dao.getIdsForOvfDeletion(FixturesTool.STORAGE_POOL_RHEL6_ISCSI_OTHER);
        assertEquals(2, guidsToDelete.size(), "there should be 2 ovfs for deletion");
        assertTrue(guidsToDelete.contains(FixturesTool.VM_RHEL5_POOL_50),
                "the list of guids for deletion doesn't contain an expected guid");
        assertTrue(guidsToDelete.contains(FixturesTool.VM_RHEL5_POOL_51),
                "the list of guids for deletion doesn't contain an expected guid");
    }

    @Test
    public void testGetIdsForOvfDeletionOneInEachPool() {
        vmDao.remove(FixturesTool.VM_RHEL5_POOL_50);
        vmDao.remove(FixturesTool.VM_RHEL5_POOL_60);

        List<Guid> guidsToDelete = dao.getIdsForOvfDeletion(FixturesTool.STORAGE_POOL_RHEL6_ISCSI_OTHER);
        assertEquals(1, guidsToDelete.size(), "the list of ovfs for deletion wasn't in the expected size");
        assertTrue(guidsToDelete.contains(FixturesTool.VM_RHEL5_POOL_50),
                "the list of guids for deletion doesn't contain an expected guid");

        guidsToDelete = dao.getIdsForOvfDeletion(FixturesTool.STORAGE_POOL_MIXED_TYPES);
        assertEquals(1, guidsToDelete.size(), "the list of ovfs for deletion wasn't in the expected size");
        assertTrue(guidsToDelete.contains(FixturesTool.VM_RHEL5_POOL_60),
                "the list of guids for deletion doesn't contain an expected guid");
    }

    @Test
    public void testGetIdsForOvfDeletionMultipleInEachPool() {
        vmDao.remove(FixturesTool.VM_RHEL5_POOL_51);
        vmDao.remove(FixturesTool.VM_RHEL5_POOL_50);
        vmDao.remove(FixturesTool.VM_RHEL5_POOL_59);
        vmDao.remove(FixturesTool.VM_RHEL5_POOL_60);
        List<Guid> guidsToDelete = dao.getIdsForOvfDeletion(FixturesTool.STORAGE_POOL_RHEL6_ISCSI_OTHER);
        assertEquals(2, guidsToDelete.size(), "the list of ovfs for deletion wasn't in the expected size");
        assertTrue(guidsToDelete.contains(FixturesTool.VM_RHEL5_POOL_51),
                "the list of guids for deletion doesn't contain an expected guid");
        assertTrue(guidsToDelete.contains(FixturesTool.VM_RHEL5_POOL_50),
                "the list of guids for deletion doesn't contain an expected guid");

        guidsToDelete = dao.getIdsForOvfDeletion(FixturesTool.STORAGE_POOL_MIXED_TYPES);
        assertEquals(2, guidsToDelete.size(), "the list of ovfs for deletion wasn't in the expected size");
        assertTrue(guidsToDelete.contains(FixturesTool.VM_RHEL5_POOL_60),
                "the list of guids for deletion doesn't contain an expected guid");
        assertTrue(guidsToDelete.contains(FixturesTool.VM_RHEL5_POOL_59),
                "the list of guids for deletion doesn't contain an expected guid");
    }

    @Test
    public void testGetIdsForOvfDeletionMultipleVmsAndTemplatesInDifferentPools() {
        vmDao.remove(FixturesTool.VM_RHEL5_POOL_51);
        vmDao.remove(FixturesTool.VM_RHEL5_POOL_50);
        vmDao.remove(FixturesTool.VM_RHEL5_POOL_59);
        vmDao.remove(FixturesTool.VM_RHEL5_POOL_60);
        templateDao.remove(FixturesTool.VM_TEMPLATE_RHEL6_1);
        templateDao.remove(FixturesTool.VM_TEMPLATE_RHEL6_2);


        List<Guid> guidsToDelete = dao.getIdsForOvfDeletion(FixturesTool.STORAGE_POOL_RHEL6_ISCSI_OTHER);
        assertEquals(2, guidsToDelete.size(), "unexpected number of ovfs for deletion");
        assertTrue(guidsToDelete.contains(FixturesTool.VM_RHEL5_POOL_51),
                "the list of guids for deletion doesn't contain an expected guid");
        assertTrue(guidsToDelete.contains(FixturesTool.VM_RHEL5_POOL_50),
                "the list of guids for deletion doesn't contain an expected guid");

        guidsToDelete = dao.getIdsForOvfDeletion(FixturesTool.STORAGE_POOL_MIXED_TYPES);
        assertEquals(4, guidsToDelete.size(), "unexpected number of ovfs for deletion");
        assertTrue(guidsToDelete.contains(FixturesTool.VM_RHEL5_POOL_60),
                "the list of guids for deletion doesn't contain an expected guid");
        assertTrue(guidsToDelete.contains(FixturesTool.VM_RHEL5_POOL_59),
                "the list of guids for deletion doesn't contain an expected guid");
        assertTrue(guidsToDelete.contains(FixturesTool.VM_TEMPLATE_RHEL6_1),
                "the list of guids for deletion doesn't contain an expected guid");
        assertTrue(guidsToDelete.contains(FixturesTool.VM_TEMPLATE_RHEL6_2),
                "the list of guids for deletion doesn't contain an expected guid");
    }
}

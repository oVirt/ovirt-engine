package org.ovirt.engine.core.dao;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.BiosType;
import org.ovirt.engine.core.common.businessentities.ImageType;
import org.ovirt.engine.core.common.businessentities.InstanceType;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmTemplateStatus;
import org.ovirt.engine.core.compat.Guid;

public class VmTemplateDaoTest extends BaseGenericDaoTestCase<Guid, VmTemplate, VmTemplateDao> {
    private static final int NUMBER_OF_TEMPLATES_FOR_PRIVELEGED_USER = 1;
    private static final int NUMBER_OF_INSTANCE_TYPES_FOR_PRIVELEGED_USER = 1;
    private static final int NUMBER_OF_TEMPLATES_IN_DB = 8;
    private static final Guid EXISTING_IMAGE_TYPE_ID = new Guid("5849b030-626e-47cb-ad90-3ce782d831b3");
    protected static final Guid[] HOST_GUIDS = {FixturesTool.VDS_RHEL6_NFS_SPM,
            FixturesTool.HOST_ID,
            FixturesTool.GLUSTER_BRICK_SERVER1};

    @Override
    protected VmTemplate generateNewEntity() {
        VmTemplate newVmTemplate = new VmTemplate();
        newVmTemplate.setId(Guid.newGuid());
        newVmTemplate.setName("NewVmTemplate");
        newVmTemplate.setClusterId(FixturesTool.CLUSTER);
        newVmTemplate.setBiosType(BiosType.Q35_SEA_BIOS);
        newVmTemplate.setClusterArch(ArchitectureType.x86_64);
        newVmTemplate.setCpuProfileId(FixturesTool.CPU_PROFILE_2);
        newVmTemplate.setSmallIconId(FixturesTool.SMALL_ICON_ID);
        newVmTemplate.setLargeIconId(FixturesTool.LARGE_ICON_ID);
        return newVmTemplate;
    }

    @Override
    protected void updateExistingEntity() {
        existingEntity.setDescription("This is an updated description");

        List<Guid> hostGuidsList = Collections.singletonList(HOST_GUIDS[0]);
        existingEntity.setDedicatedVmForVdsList(hostGuidsList);
    }

    @Override
    protected Guid getExistingEntityId() {
        return FixturesTool.VM_TEMPLATE_RHEL5;
    }

    @Override
    protected Guid generateNonExistingId() {
        return Guid.newGuid();
    }

    /**
     * Note the {@link VmTemplateDao#getAll()} returns only <strong>templates</strong>, while
     * {@link VmTemplateDao#getCount()} also includes image types and and instance types.
     */
    @Override
    protected int getEntitiesTotalCount() {
        return 6;
    }

    /**
     * Ensures that the template returned is instance type.
     */
    @Test
    public void testGetInstanceType() {
        InstanceType result = dao.getInstanceType(FixturesTool.INSTANCE_TYPE);

        assertNotNull(result);
        assertEquals(FixturesTool.INSTANCE_TYPE, result.getId());
    }

    /**
     * Ensures that the template returned is image type.
     */
    @Test
    public void testGetImageType() {
        ImageType result = dao.getImageType(EXISTING_IMAGE_TYPE_ID);

        assertNotNull(result);
        assertEquals(EXISTING_IMAGE_TYPE_ID, result.getId());
    }

    @Test
    public void testGetVmTemplatesByIds() {
        List<VmTemplate> result = dao.getVmTemplatesByIds(Arrays.asList(FixturesTool.VM_TEMPLATE_RHEL5,
                FixturesTool.VM_TEMPLATE_RHEL5_2));
        assertEquals(2, result.size(), "loaded templates list isn't in the expected size");
        Collection<Guid> recieved = result.stream().map(VmTemplate::getId).collect(Collectors.toList());
        assertTrue(recieved.contains(FixturesTool.VM_TEMPLATE_RHEL5), "the received list didn't contain an expected Template");
        assertTrue(recieved.contains(FixturesTool.VM_TEMPLATE_RHEL5_2), "the received list didn't contain an expected Template");
    }

    /**
     * Asserts that the right collection containing the vm templates is returned for a privileged user with filtering enabled
     */
    @Test
    public void testGetAllWithPermissionsForPriviligedUser() {
        List<VmTemplate> result = dao.getAll(PRIVILEGED_USER_ID, true, VmEntityType.TEMPLATE);

        assertNotNull(result);
        assertEquals(NUMBER_OF_TEMPLATES_FOR_PRIVELEGED_USER, result.size());
        assertEquals(result.iterator().next(), existingEntity);
    }

    /**
     * Asserts that the right collection containing the vm templates is returned for a non privileged user with filtering disabled
     */
    @Test
    public void testGetAllWithPermissionsDisabledForUnpriviligedUser() {
        List<VmTemplate> result = dao.getAll(UNPRIVILEGED_USER_ID, false, VmEntityType.TEMPLATE);
        assertGetAllResult(result);
    }

    /**
     * Asserts that an empty collection is returned for a non privileged user with filtering enabled
     */
    @Test
    public void testGetAllWithPermissionsForUnpriviligedUser() {
        List<VmTemplate> result = dao.getAll(UNPRIVILEGED_USER_ID, true, VmEntityType.TEMPLATE);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Asserts that the right collection containing the instnace types is returned for a privileged user with filtering enabled
     */
    @Test
    public void testGetAllInstanceTypesWithPermissionsForPriviligedUser() {
        VmTemplate existingInstanceType = dao.get(FixturesTool.INSTANCE_TYPE);
        List<VmTemplate> result = dao.getAll(PRIVILEGED_USER_ID, true, VmEntityType.INSTANCE_TYPE);

        assertNotNull(result);
        assertEquals(NUMBER_OF_INSTANCE_TYPES_FOR_PRIVELEGED_USER, result.size());
        assertEquals(result.iterator().next(), existingInstanceType);
    }

    /**
     * Asserts that the right collection containing the instance type is returned for a non privileged user with filtering disabled
     */
    @Test
    public void testGetAllInstanceTypesWithPermissionsDisabledForUnpriviligedUser() {
        List<VmTemplate> result = dao.getAll(UNPRIVILEGED_USER_ID, false, VmEntityType.INSTANCE_TYPE);
        assertGetAllResult(result);
    }

    /**
     * Asserts that an empty collection is returned for a non privileged user with filtering enabled
     */
    @Test
    public void testGetAllInstanceTypesWithPermissionsForUnpriviligedUser() {
        List<VmTemplate> result = dao.getAll(UNPRIVILEGED_USER_ID, true, VmEntityType.INSTANCE_TYPE);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Ensures the right set of templates are returned.
     */
    @Test
    public void testGetAllForStorageDomain() {
        List<VmTemplate> result = dao.getAllForStorageDomain(FixturesTool.STORAGE_DOMAIN_SCALE_SD5);

        assertGetAllResult(result);
    }

    /**
     * Asserts that the right collection containing the vm templates is returned for a privileged user with filtering enabled
     */
    @Test
    public void testGetAllForStorageDomainWithPermissionsForPriviligedUser() {
        List<VmTemplate> result = dao.getAllForStorageDomain(FixturesTool.STORAGE_DOMAIN_SCALE_SD5, PRIVILEGED_USER_ID, true);
        assertGetAllResult(result);
    }

    /**
     * Asserts that an empty collection is returned for a non privileged user with filtering enabled
     */
    @Test
    public void testGetAllForStorageDomainWithPermissionsForUnpriviligedUser() {
        List<VmTemplate> result = dao.getAllForStorageDomain(FixturesTool.STORAGE_DOMAIN_SCALE_SD5, UNPRIVILEGED_USER_ID, true);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Asserts that the right collection containing the vm templates is returned for a non privileged user with filtering disabled
     */
    @Test
    public void testGetAllForStorageDomainWithPermissionsDisabledForUnpriviligedUser() {
        List<VmTemplate> result = dao.getAllForStorageDomain(FixturesTool.STORAGE_DOMAIN_SCALE_SD5, UNPRIVILEGED_USER_ID, false);
        assertGetAllResult(result);
    }

    /**
     * Ensures the right set of templates are returned.
     */
    @Test
    public void testGetAllTemplatesRelatedToQuotaId() {
        List<VmTemplate> result = dao.getAllTemplatesRelatedToQuotaId(FixturesTool.QUOTA_GENERAL);

        assertGetAllResult(result);
    }

    /**
     * Ensures that the templates for the given vds group are returned.
     */
    @Test
    public void testGetAllForCluster() {
        List<VmTemplate> result = dao.getAllForCluster(FixturesTool.CLUSTER);

        assertGetAllResult(result);
        for (VmTemplate template : result) {
            assertEquals(FixturesTool.CLUSTER, template.getClusterId());
        }
    }

    /**
     * Ensures that the templates for the give image are returned.
     */
    @Test
    public void testGetAllForImage() {
        Map<Boolean, VmTemplate> result = dao.getAllForImage(FixturesTool.TEMPLATE_IMAGE_ID);
        assertEquals(1, result.size(), "Wrong number of keys");
        assertNotNull(result.values());
    }

    /**
     * Ensures updating the status aspect of the VM Template works.
     */
    @Test
    public void testUpdateStatus() {
        VmTemplate before = dao.get(existingEntity.getId());

        before.setStatus(VmTemplateStatus.Locked);
        dao.updateStatus(existingEntity.getId(), VmTemplateStatus.Locked);

        VmTemplate after = dao.get(existingEntity.getId());

        assertEquals(before, after);
    }

    /**
     * Ensures that removing a template works as expected.
     */
    @Test
    public void testRemove() {
        VmTemplate before = dao.get(FixturesTool.VM_TEMPLATE_RHEL5_2);

        assertNotNull(before);

        dao.remove(FixturesTool.VM_TEMPLATE_RHEL5_2);

        VmTemplate after = dao.get(FixturesTool.VM_TEMPLATE_RHEL5_2);

        assertNull(after);
    }

    /**
    * Asserts that the right template is returned for a privileged user with filtering enabled
    */
    @Test
    public void testGetWithPermissionsForPriviligedUser() {
        VmTemplate result = dao.get(FixturesTool.VM_TEMPLATE_RHEL5, PRIVILEGED_USER_ID, true);
        assertGetResult(result);
    }

    /**
    * Asserts that no result is returned for a non privileged user with filtering enabled
    */
    @Test
    public void testGetWithPermissionsForUnpriviligedUser() {
        VmTemplate result = dao.get(FixturesTool.VM_TEMPLATE_RHEL5, UNPRIVILEGED_USER_ID, true);
        assertNull(result);
    }

    /**
    * Asserts that the right template is returned for a non privileged user with filtering disabled
    */
    @Test
    public void testGetWithPermissionsDisabledForUnpriviligedUser() {
        VmTemplate result = dao.get(FixturesTool.VM_TEMPLATE_RHEL5, UNPRIVILEGED_USER_ID, false);
        assertGetResult(result);
    }

    /**
     * Asserts that the correct template is returned for the given network id
     */
    @Test
    public void testGetAllForNetwork() {
        List<VmTemplate> result = dao.getAllForNetwork(FixturesTool.NETWORK_ENGINE);
        assertEquals(existingEntity, result.get(0));
    }

    /**
     * Asserts that the correct template is returned for the given network id
     */
    @Test
    public void testGetAllVnicProfile() {
        List<VmTemplate> result = dao.getAllForVnicProfile(FixturesTool.VM_NETWORK_INTERFACE_PROFILE);
        assertEquals(existingEntity, result.get(0));
    }

    /**
     * Asserts that the no templates are fetched
     */
    @Test
    public void testGetAllVnicProfileWithNo() {
        List<VmTemplate> result = dao.getAllForVnicProfile(FixturesTool.VM_NETWORK_INTERFACE_PROFILE_NOT_USED);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    private static void assertGetResult(VmTemplate result) {
        assertNotNull(result);
        assertEquals(FixturesTool.VM_TEMPLATE_RHEL5, result.getId());
        assertEquals(1, result.getDbGeneration(), "Template generation wasn't loaded as expected");
    }

    private static void assertGetAllResult(List<VmTemplate> result) {
        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (VmTemplate template : result){
            assertEquals(1, template.getDbGeneration(), "Template generation wasn't loaded as expected");
        }
    }

    /**
     * Ensures that the count is fetching the right number of Templates
     */
    @Test
    public void testCountTemplates() {
        assertEquals(NUMBER_OF_TEMPLATES_IN_DB, dao.getCount());
    }

    /**
     * Assert that all versions (VM_TEMPLATE_RHEL5_V2) returns for base template (VM_TEMPLATE_RHEL5)
     */
    @Test
    public void testGetTemplateVersionsForBaseTemplate() {
        List<VmTemplate> tVersions = dao.getTemplateVersionsForBaseTemplate(FixturesTool.VM_TEMPLATE_RHEL5);
        assertEquals(1, tVersions.size());
        assertEquals(FixturesTool.VM_TEMPLATE_RHEL5_V2, tVersions.get(0).getId());
    }

    /**
     * Assert that latest version (VM_TEMPLATE_RHEL5_V2) returns for base template (VM_TEMPLATE_RHEL5)
     */
    @Test
    public void testGetTemplateWithLatestVersionInChain() {
        assertEquals(FixturesTool.VM_TEMPLATE_RHEL5_V2, dao.getTemplateWithLatestVersionInChain(FixturesTool.VM_TEMPLATE_RHEL5).getId());
    }

    @Test
    public void testEmptyGetAllForCpuProfile() {
        List<VmTemplate> result = dao.getAllForCpuProfile(Guid.newGuid());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetAllForCpuProfile() {
        List<VmTemplate> result = dao.getAllForCpuProfile(FixturesTool.CPU_PROFILE_1);

        assertNotNull(result);
        assertEquals(4, result.size());
    }

    @Test
    public void testEmptyGetAllForDiskProfile() {
        List<VmTemplate> result = dao.getAllForDiskProfile(Guid.newGuid());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetAllForDiskProfile() {
        List<VmTemplate> result = dao.getAllForDiskProfile(FixturesTool.DISK_PROFILE_1);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    public void testGetAllWithLeaseOnStorageDomain() {
        List<Guid> vmAndTemplatesWithLeasesIds = dao.getAllWithLeaseOnStorageDomain(FixturesTool.STORAGE_DOMAIN_NFS2_1)
                .stream().map(VmTemplate::getId).collect(Collectors.toList());
        assertThat(vmAndTemplatesWithLeasesIds,
                Matchers.contains(FixturesTool.VM_TEMPLATE_RHEL5_2));
    }
}

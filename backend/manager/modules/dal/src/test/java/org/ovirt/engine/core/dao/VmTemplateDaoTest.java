package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.ImageType;
import org.ovirt.engine.core.common.businessentities.InstanceType;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmTemplateStatus;
import org.ovirt.engine.core.compat.Guid;

public class VmTemplateDaoTest extends BaseDaoTestCase {
    private static final int NUMBER_OF_TEMPLATES_FOR_PRIVELEGED_USER = 1;
    private static final int NUMBER_OF_INSTANCE_TYPES_FOR_PRIVELEGED_USER = 1;
    private static final int NUMBER_OF_TEMPLATES_IN_DB = 8;
    private static final Guid EXISTING_TEMPLATE_ID = new Guid("1b85420c-b84c-4f29-997e-0eb674b40b79");
    private static final Guid DELETABLE_TEMPLATE_ID = new Guid("1b85420c-b84c-4f29-997e-0eb674b40b80");
    private static final Guid STORAGE_DOMAIN_ID = new Guid("72e3a666-89e1-4005-a7ca-f7548004a9ab");
    private static final Guid CLUSTER_ID = new Guid("b399944a-81ab-4ec5-8266-e19ba7c3c9d1");
    private static final Guid EXISTING_INSTANCE_TYPE_ID = new Guid("99408929-82cf-4dc7-a532-9d998063fa95");
    private static final Guid EXISTING_IMAGE_TYPE_ID = new Guid("5849b030-626e-47cb-ad90-3ce782d831b3");
    private static final Guid SMALL_ICON_ID = new Guid("38fc5e1a-f96b-339b-9894-def6f366daf5");
    private static final Guid LARGE_ICON_ID = new Guid("a3b954f0-31ff-3166-b7a1-28b23202b198");
    protected static final Guid[] HOST_GUIDS = {new Guid("afce7a39-8e8c-4819-ba9c-796d316592e6"),
        new Guid("afce7a39-8e8c-4819-ba9c-796d316592e7"),
        new Guid("23f6d691-5dfb-472b-86dc-9e1d2d3c18f3")};

    private VmTemplateDao dao;

    private VmTemplate newVmTemplate;
    private VmTemplate existingTemplate;
    private VmTemplate existingInstanceType;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        dao = dbFacade.getVmTemplateDao();

        existingTemplate = dao.get(
                new Guid("1b85420c-b84c-4f29-997e-0eb674b40b79"));

        existingInstanceType = dao.get(
                new Guid("99408929-82cf-4dc7-a532-9d998063fa95"));

        newVmTemplate = new VmTemplate();
        newVmTemplate.setId(Guid.newGuid());
        newVmTemplate.setName("NewVmTemplate");
        newVmTemplate.setClusterId(CLUSTER_ID);
        newVmTemplate.setClusterArch(ArchitectureType.x86_64);
        newVmTemplate.setCpuProfileId(FixturesTool.CPU_PROFILE_2);
        newVmTemplate.setSmallIconId(SMALL_ICON_ID);
        newVmTemplate.setLargeIconId(LARGE_ICON_ID);
    }

    /**
     * Ensures an id is required.
     */
    @Test
    public void testGetWithInvalidId() {
        VmTemplate result = dao.get(Guid.newGuid());

        assertNull(result);
    }

    /**
     * Ensures that the right template is returned.
     */
    @Test
    public void testGet() {
        VmTemplate result = dao.get(EXISTING_TEMPLATE_ID);

        assertGetResult(result);
    }

    /**
     * Ensures that the template returned is instance type.
     */
    @Test
    public void testGetInstanceType() {
        InstanceType result = dao.getInstanceType(EXISTING_INSTANCE_TYPE_ID);

        assertNotNull(result);
        assertEquals(EXISTING_INSTANCE_TYPE_ID, result.getId());
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

    /**
     * Ensures that all templates are returned.
     */
    @Test
    public void testGetAll() {
        List<VmTemplate> result = dao.getAll();

        assertGetAllResult(result);
    }

    @Test
    public void testGetVmTemplatesByIds() {
        List<VmTemplate> result = dao.getVmTemplatesByIds(Arrays.asList(EXISTING_TEMPLATE_ID, DELETABLE_TEMPLATE_ID));
        assertEquals("loaded templates list isn't in the expected size", 2, result.size());
        Collection<Guid> recieved = result.stream().map(VmTemplate::getId).collect(Collectors.toList());
        assertTrue("the received list didn't contain an expected Template", recieved.contains(EXISTING_TEMPLATE_ID));
        assertTrue("the received list didn't contain an expected Template", recieved.contains(DELETABLE_TEMPLATE_ID));
    }

    /**
     * Asserts that the right collection containing the vm templates is returned for a privileged user with filtering enabled
     */
    @Test
    public void testGetAllWithPermissionsForPriviligedUser() {
        List<VmTemplate> result = dao.getAll(PRIVILEGED_USER_ID, true, VmEntityType.TEMPLATE);

        assertNotNull(result);
        assertEquals(NUMBER_OF_TEMPLATES_FOR_PRIVELEGED_USER, result.size());
        assertEquals(result.iterator().next(), existingTemplate);
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
        List<VmTemplate> result = dao.getAllForStorageDomain(STORAGE_DOMAIN_ID);

        assertGetAllResult(result);
    }

    /**
     * Asserts that the right collection containing the vm templates is returned for a privileged user with filtering enabled
     */
    @Test
    public void testGetAllForStorageDomainWithPermissionsForPriviligedUser() {
        List<VmTemplate> result = dao.getAllForStorageDomain(STORAGE_DOMAIN_ID, PRIVILEGED_USER_ID, true);
        assertGetAllResult(result);
    }

    /**
     * Asserts that an empty collection is returned for a non privileged user with filtering enabled
     */
    @Test
    public void testGetAllForStorageDomainWithPermissionsForUnpriviligedUser() {
        List<VmTemplate> result = dao.getAllForStorageDomain(STORAGE_DOMAIN_ID, UNPRIVILEGED_USER_ID, true);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Asserts that the right collection containing the vm templates is returned for a non privileged user with filtering disabled
     */
    @Test
    public void testGetAllForStorageDomainWithPermissionsDisabledForUnpriviligedUser() {
        List<VmTemplate> result = dao.getAllForStorageDomain(STORAGE_DOMAIN_ID, UNPRIVILEGED_USER_ID, false);
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
        List<VmTemplate> result = dao.getAllForCluster(CLUSTER_ID);

        assertGetAllResult(result);
        for (VmTemplate template : result) {
            assertEquals(CLUSTER_ID, template.getClusterId());
        }
    }

    /**
     * Ensures that the templates for the give image are returned.
     */
    @Test
    public void testGetAllForImage() {
        Map<Boolean, VmTemplate> result = dao.getAllForImage(FixturesTool.TEMPLATE_IMAGE_ID);
        assertEquals("Wrong number of keys", 1, result.size());
        assertNotNull(result.values());
    }

    /**
     * Ensures that saving a template works as expected.
     */
    @Test
    public void testSave() {
        newVmTemplate.setDedicatedVmForVdsList(Arrays.asList(HOST_GUIDS));
        dao.save(newVmTemplate);

        VmTemplate result = dbFacade.getVmTemplateDao().get(newVmTemplate.getId());

        assertNotNull(result);
        assertTrue(CollectionUtils.isEqualCollection(result.getDedicatedVmForVdsList(),
                newVmTemplate.getDedicatedVmForVdsList()));
        assertEquals(newVmTemplate, result);
    }

    /**
     * Ensures that updating a template works as expected.
     */
    @Test
    public void testUpdate() {
        existingTemplate.setDescription("This is an updated description");

        List<Guid> hostGuidsList = Arrays.asList(HOST_GUIDS);
        hostGuidsList = new LinkedList<>();
        hostGuidsList.add(HOST_GUIDS[0]);
        existingTemplate.setDedicatedVmForVdsList(hostGuidsList);
        dao.update(existingTemplate);

        VmTemplate result = dbFacade.getVmTemplateDao().get(existingTemplate.getId());

        assertNotNull(result);
        assertTrue(CollectionUtils.isEqualCollection(result.getDedicatedVmForVdsList(),
                existingTemplate.getDedicatedVmForVdsList()));
        assertEquals(existingTemplate, result);
    }

    /**
     * Ensures updating the status aspect of the VM Template works.
     */
    @Test
    public void testUpdateStatus() {
        VmTemplate before = dao.get(existingTemplate.getId());

        before.setStatus(VmTemplateStatus.Locked);
        dao.updateStatus(existingTemplate.getId(), VmTemplateStatus.Locked);

        VmTemplate after = dao.get(existingTemplate.getId());

        assertEquals(before, after);
    }

    /**
     * Ensures that removing a template works as expected.
     */
    @Test
    public void testRemove() {
        VmTemplate before = dbFacade.getVmTemplateDao().get(DELETABLE_TEMPLATE_ID);

        assertNotNull(before);

        dao.remove(DELETABLE_TEMPLATE_ID);

        VmTemplate after = dbFacade.getVmTemplateDao().get(DELETABLE_TEMPLATE_ID);

        assertNull(after);
    }

    /**
    * Asserts that the right template is returned for a privileged user with filtering enabled
    */
    @Test
    public void testGetWithPermissionsForPriviligedUser() {
        VmTemplate result = dao.get(EXISTING_TEMPLATE_ID, PRIVILEGED_USER_ID, true);
        assertGetResult(result);
    }

    /**
    * Asserts that no result is returned for a non privileged user with filtering enabled
    */
    @Test
    public void testGetWithPermissionsForUnpriviligedUser() {
        VmTemplate result = dao.get(EXISTING_TEMPLATE_ID, UNPRIVILEGED_USER_ID, true);
        assertNull(result);
    }

    /**
    * Asserts that the right template is returned for a non privileged user with filtering disabled
    */
    @Test
    public void testGetWithPermissionsDisabledForUnpriviligedUser() {
        VmTemplate result = dao.get(EXISTING_TEMPLATE_ID, UNPRIVILEGED_USER_ID, false);
        assertGetResult(result);
    }

    /**
     * Asserts that the correct template is returned for the given network id
     */
    @Test
    public void testGetAllForNetwork() {
        List<VmTemplate> result = dao.getAllForNetwork(FixturesTool.NETWORK_ENGINE);
        assertEquals(existingTemplate, result.get(0));
    }

    /**
     * Asserts that the correct template is returned for the given network id
     */
    @Test
    public void testGetAllVnicProfile() {
        List<VmTemplate> result = dao.getAllForVnicProfile(FixturesTool.VM_NETWORK_INTERFACE_PROFILE);
        assertEquals(existingTemplate, result.get(0));
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
        assertEquals(EXISTING_TEMPLATE_ID, result.getId());
        assertEquals("Template generation wasn't loaded as expected", 1, result.getDbGeneration());
    }

    private static void assertGetAllResult(List<VmTemplate> result) {
        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (VmTemplate template : result){
            assertEquals("Template generation wasn't loaded as expected", 1, template.getDbGeneration());
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
        assertEquals(1, result.size());
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

}

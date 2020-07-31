package org.ovirt.engine.core.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainSharedStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage.BaseDisk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskInterface;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.businessentities.storage.ImageStorageDomainMap;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.MockConfigDescriptor;
import org.ovirt.engine.core.utils.MockedConfig;
import org.ovirt.engine.core.utils.RandomUtils;

public class StorageDomainDaoTest extends BaseDaoTestCase<StorageDomainDao> {
    private static final int NUMBER_OF_STORAGE_DOMAINS_FOR_PRIVELEGED_USER = 1;
    private static final String EXISTING_CONNECTION = "10.35.64.25:/export/share";
    private static final long NUMBER_OF_IMAGES_ON_EXISTING_DOMAIN = 5;

    @Inject
    private VmDao vmDao;
    @Inject
    private VmTemplateDao vmTemplateDao;
    @Inject
    private BaseDiskDao baseDiskDao;
    @Inject
    private ImageDao imageDao;
    @Inject
    private ImageStorageDomainMapDao imageStorageDomainMapDao;
    @Inject
    private VmDeviceDao vmDeviceDao;
    @Inject
    private DiskVmElementDao diskVmElementDao;
    @Inject
    private VmStaticDao vmStaticDao;

    private StorageDomain existingDomain;

     public static Stream<MockConfigDescriptor<?>> mockConfiguration() {
         return Stream.of(
                 MockConfigDescriptor.of(ConfigValues.PropagateDiskErrors, false)
         );
     }


    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();

        existingDomain = dao.get(FixturesTool.STORAGE_DOMAIN_SCALE_SD5);
    }

    /**
     * Ensures that retrieving the id works.
     */
    @Test
    public void testGetMasterStorageDomainIdForPool() {
        Guid result = dao.getMasterStorageDomainIdForPool(FixturesTool.DATA_CENTER);

        assertNotNull(result);
        assertEquals(FixturesTool.STORAGE_DOMAIN_SCALE_SD5, result);
    }

    @Test
    public void testGetStorageDomainByTypeForStoragePoolId() {
        List<StorageDomain> result = dao.getStorageDomains(FixturesTool.DATA_CENTER, StorageDomainType.Master);
        assertGetResult(result.get(0));
    }

    @Test
    public void testGetStorageDomainWithStatusForExistingPool() {
        List<StorageDomain> result = dao.getStorageDomains(FixturesTool.DATA_CENTER,
                existingDomain.getStorageDomainType(), StorageDomainStatus.Active);

        assertGetResult(result.get(0));
    }

    @Test
    public void testGetStorageDomainWithStatusForInvalidPool() {
        List<StorageDomain> result = dao.getStorageDomains(Guid.newGuid(),
                existingDomain.getStorageDomainType(), existingDomain.getStatus());

        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetStorageDomainWithCorrectStatus() {
        List<StorageDomain> result = dao.getStorageDomains(FixturesTool.STORAGE_POOL_NFS_INACTIVE_ISO,
                StorageDomainType.ISO, StorageDomainStatus.Inactive);

        StorageDomain domain = result.get(0);
        assertEquals(FixturesTool.STORAGE_DOMAIN_NFS_INACTIVE_ISO, domain.getId());
        assertEquals(0, domain.getCommittedDiskSize(), "Wrong committed disk size");
        assertEquals(0, domain.getActualImagesSize(), "Wrong actual disk size");
    }

    @Test
    public void testGetStorageDomainWithWrongStatus() {
        List<StorageDomain> result = dao.getStorageDomains(FixturesTool.STORAGE_POOL_NFS_INACTIVE_ISO,
                StorageDomainType.ISO, StorageDomainStatus.Active);

        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetStorageDomainWithNoStatus() {
        List<StorageDomain> result = dao.getStorageDomains(FixturesTool.STORAGE_POOL_NFS_INACTIVE_ISO,
                StorageDomainType.ISO, null);

        StorageDomain domain = result.get(0);
        assertEquals(FixturesTool.STORAGE_DOMAIN_NFS_INACTIVE_ISO, domain.getId());
        assertEquals(0, domain.getCommittedDiskSize(), "Wrong committed disk size");
        assertEquals(0, domain.getActualImagesSize(), "Wrong actual disk size");
        assertEquals(StorageDomainSharedStatus.Inactive, domain.getStorageDomainSharedStatus(), "Wrong shared status");
    }

    @Test
    public void testGetStorageDomainsWithAndWithoutStatusAreEqual() {
        List<StorageDomain> resultWithoutStatus = dao.getStorageDomains(FixturesTool.STORAGE_POOL_NFS_INACTIVE_ISO,
                StorageDomainType.ISO);
        List<StorageDomain >resultWithStatus = dao.getStorageDomains(FixturesTool.STORAGE_POOL_NFS_INACTIVE_ISO,
                StorageDomainType.ISO, null);

        assertFalse(resultWithoutStatus.isEmpty());
        assertFalse(resultWithStatus.isEmpty());
        assertEquals(resultWithoutStatus, resultWithStatus);
    }

    /**
     * Ensures that nothing is returned when the id is invalid.
     */
    @Test
    public void testGetWithInvalidId() {
        StorageDomain result = dao.get(Guid.newGuid());
        assertNull(result);
    }

    /**
     * Ensures that retrieving a domain works.
     */
    @Test
    public void testGet() {
        StorageDomain result = dao.get(existingDomain.getId());
        assertGetResult(result);
    }

    /**
     * Ensures that the right collection is returned for a given storage pool with filtering for a privileged user.
     */
    @Test
    public void testGetWithPermissionsPrivilegedUser() {
        StorageDomain result = dao.get(existingDomain.getId(), PRIVILEGED_USER_ID, true);
        assertGetResult(result);
    }

    /**
     * Ensures that the right collection is returned for a given storage pool with filtering disabled for an unprivileged user.
     */
    @Test
    public void testGetWithPermissionsDisabledUnprivilegedUser() {
        StorageDomain result = dao.get(existingDomain.getId(), UNPRIVILEGED_USER_ID, false);
        assertGetResult(result);
    }

    /**
     * Ensures that an empty collection is returned for a given storage pool for an unprivileged user.
     */
    @Test
    public void testGetWithPermissionsUnprivilegedUser() {
        StorageDomain result = dao.get(existingDomain.getId(), UNPRIVILEGED_USER_ID, true);
        assertNull(result);
    }

    /**
     * Ensures that null is returned when the specified id does not exist.
     */
    @Test
    public void testGetForStoragePoolWithInvalidId() {
        StorageDomain result = dao.getForStoragePool(Guid.newGuid(), FixturesTool.STORAGE_POOL_NFS);
        assertNull(result);
    }

    /**
     * Test getting storage for existing image id.
     */
    @Test
    public void testGetAllStorageDomainsByImageId() {
        List<StorageDomain> result = dao.getAllStorageDomainsByImageId(FixturesTool.TEMPLATE_IMAGE_ID);
        assertEquals(1, result.size());
        StorageDomain domain = result.get(0);
        assertEquals(FixturesTool.STORAGE_DOMAIN_SCALE_SD5, domain.getId());
        assertEquals(8, domain.getCommittedDiskSize(), "Wrong committed disk size");
        assertEquals(4, domain.getActualImagesSize(), "Wrong actual disk size");
        assertEquals(StorageDomainSharedStatus.Active, domain.getStorageDomainSharedStatus(), "Wrong shared status");
    }

    /**
     * Test getting storage for not existing image id. The expected result should be an empty list.
     */
    @Test
    public void testGetAllStorageDomainsByNotExistingImageId() {
        List<StorageDomain> result = dao.getAllStorageDomainsByImageId(Guid.newGuid());
        assertTrue(result.isEmpty());
    }

    /**
     * Asserts the result of {@link StorageDomainDao#get(Guid)} returns the correct domain
     */
    private void assertGetResult(StorageDomain result) {
        assertNotNull(result);
        assertEquals(existingDomain, result);
        assertEquals(8, result.getCommittedDiskSize(), "Wrong committed disk size");
        assertEquals(4, result.getActualImagesSize(), "Wrong actual disk size");
        assertEquals(FixturesTool.LUN_ID_OF_DOMAIN_METADATA, result.getFirstMetadataDevice(),
                "Wrong first metadata device");
        assertEquals(FixturesTool.LUN_ID_OF_DOMAIN_VG_METADATA, result.getVgMetadataDevice(), "Wrong vg metadata device");
        assertEquals(StorageDomainSharedStatus.Active, result.getStorageDomainSharedStatus(), "Wrong shared status");
        assertFalse(result.isBackup(), "Wrong backup flag status");
    }

    /**
     * Ensures that null is returned if the pool doesn't exist.
     */
    @Test
    public void testGetForStoragePoolWithInvalidPool() {
        StorageDomain result = dao.getForStoragePool(existingDomain.getId(), Guid.newGuid());
        assertNull(result);
    }

    /**
     * Ensures the call works as expected.
     */
    @Test
    public void testGetForStoragePool() {
        StorageDomain result = dao.getForStoragePool(existingDomain.getId(), FixturesTool.DATA_CENTER);
        assertGetResult(result);
    }

    /**
     * Ensures that all instances are returned.
     */
    @Test
    public void testGetAll() {
        List<StorageDomain> result = dao.getAll();
        assertFalse(result.isEmpty());
    }

    /**
     * Ensures that retrieving storage domains works as expected for a privileged user.
     */
    @Test
    public void testGetAllWithPermissionsPrivilegedUser() {
        List<StorageDomain> result = dao.getAll(PRIVILEGED_USER_ID, true);
        assertFalse(result.isEmpty());
        assertEquals(NUMBER_OF_STORAGE_DOMAINS_FOR_PRIVELEGED_USER, result.size());
        StorageDomain domain = result.get(0);
        assertEquals(existingDomain, domain);
        assertEquals(8, domain.getCommittedDiskSize(), "Wrong committed disk size");
        assertEquals(4, domain.getActualImagesSize(), "Wrong actual disk size");
        assertEquals(StorageDomainSharedStatus.Active, domain.getStorageDomainSharedStatus(), "Wrong shared status");
    }

    /**
     * Ensures that retrieving storage domains works as expected with filtering disabled for an unprivileged user.
     */
    @Test
    public void testGetAllWithPermissionsDisabledUnprivilegedUser() {
        List<StorageDomain> result = dao.getAll(UNPRIVILEGED_USER_ID, false);
        assertFalse(result.isEmpty());
    }

    /**
     * Ensures that no storage domains retrieved for an unprivileged user with filtering enabled.
     */
    @Test
    public void testGetAllWithPermissionsUnprivilegedUser() {
        List<StorageDomain> result = dao.getAll(UNPRIVILEGED_USER_ID, true);
        assertTrue(result.isEmpty());
    }

    /**
     * Ensures a null collection is returned.
     */
    @Test
    public void testGetAllForStorageDomainWithInvalidDomain() {
        List<StorageDomain> result = dao.getAllForStorageDomain(Guid.newGuid(), PRIVILEGED_USER_ID, false);
        assertTrue(result.isEmpty());
    }

    /**
     * Ensures that the right set of domains are returned.
     */
    @Test
    public void testGetAllForStorageDomain() {
        List<StorageDomain> result = dao.getAllForStorageDomain(existingDomain.getId(), PRIVILEGED_USER_ID, false);
        assertFalse(result.isEmpty());
    }

    /**
     * Ensures that the right set of domains are returned for Unprivileged User.
     */
    @Test
    public void testGetAllForStorageDomainWithPermissionsUnprivilegedUser() {
        List<StorageDomain> result = dao.getAllForStorageDomain(existingDomain.getId(), UNPRIVILEGED_USER_ID, true);
        assertTrue(result.isEmpty());
    }

    /**
     * Ensures an empty list is returned for an invalid connection.
     */
    @Test
    public void testGetAllForConnectionWithInvalidConnection() {
        List<StorageDomain> result = dao.getAllForConnection(RandomUtils.instance().nextString(10));
        assertTrue(result.isEmpty());
    }

    /**
     * Ensures that the right collection is returned for a given connection.
     */
    @Test
    public void testGetAllForConnection() {
        List<StorageDomain> result = dao.getAllForConnection(EXISTING_CONNECTION);
        assertGetAllForStoragePoolResult(result, FixturesTool.STORAGE_POOL_NFS);
    }

    /**
     * Ensures an empty list is returned for an invalid connection.
     */
    @Test
    public void testGetAllByStoragePoolAndConnectionWithInvalidConnection() {
        List<StorageDomain> result =
                dao.getAllByStoragePoolAndConnection(FixturesTool.STORAGE_POOL_NFS, RandomUtils.instance().nextString(10));

        assertTrue(result.isEmpty());
    }

    /**
     * Ensures an empty list is returned for an invalid storage pool id.
     */
    @Test
    public void testGetAllByStoragePoolAndConnectionWithInvalidStoragePool() {
        List<StorageDomain> result =
                dao.getAllByStoragePoolAndConnection(Guid.newGuid(), EXISTING_CONNECTION);

        assertTrue(result.isEmpty());
    }

    /**
     * Ensures an empty list is returned for an invalid storage pool id and connection.
     */
    @Test
    public void testGetAllByStoragePoolAndConnectionWithInvalidInput() {
        List<StorageDomain> result =
                dao.getAllByStoragePoolAndConnection(Guid.newGuid(), RandomUtils.instance().nextString(10));

        assertTrue(result.isEmpty());
    }

    /**
     * Ensures that the right collection is returned for a given connection.
     */
    @Test
    public void testGetAllByStoragePoolAndConnection() {
        List<StorageDomain> result =
                dao.getAllByStoragePoolAndConnection(FixturesTool.STORAGE_POOL_NFS, EXISTING_CONNECTION);

        assertGetAllForStoragePoolResult(result, FixturesTool.STORAGE_POOL_NFS);
    }

    /**
     * Ensures an empty list is returned for an invalid pool.
     */
    @Test
    public void testGetAllForStoragePoolWithInvalidPool() {
        List<StorageDomain> result = dao.getAllForStoragePool(Guid.newGuid());
        assertTrue(result.isEmpty());
    }

    /**
     * Ensures that the right collection is returned for a given storage pool.
     */
    @Test
    public void testGetAllForStoragePool() {
        List<StorageDomain> result = dao.getAllForStoragePool(FixturesTool.STORAGE_POOL_NFS);
        assertGetAllForStoragePoolResult(result, FixturesTool.STORAGE_POOL_NFS);
    }

    /**
     * Ensures that the right collection is returned for a given storage pool with filtering for a privileged user.
     */
    @Test
    public void testGetAllForStoragePoolWithPermissionsPrivilegedUser() {
        List<StorageDomain> result = dao.getAllForStoragePool(FixturesTool.DATA_CENTER, PRIVILEGED_USER_ID, true);
        assertGetAllForStoragePoolResult(result, FixturesTool.DATA_CENTER);
    }

    /**
     * Ensures that the right collection is returned for a given storage pool with filtering disabled for an unprivileged user.
     */
    @Test
    public void testGetAllForStoragePoolWithPermissionsDisabledUnprivilegedUser() {
        List<StorageDomain> result =
                dao.getAllForStoragePool(FixturesTool.STORAGE_POOL_NFS, UNPRIVILEGED_USER_ID, false);

        assertGetAllForStoragePoolResult(result, FixturesTool.STORAGE_POOL_NFS);
    }

    /**
     * Ensures that an empty collection is returned for a given storage pool for an unprivileged user.
     */
    @Test
    public void testGetAllForStoragePoolWithPermissionsUnprivilegedUser() {
        List<StorageDomain> result = dao.getAllForStoragePool(FixturesTool.STORAGE_POOL_NFS, UNPRIVILEGED_USER_ID, true);
        assertTrue(result.isEmpty());
    }

    /**
     * Asserts the result returned from {@link StorageDomainDao#getAllForStoragePool(Guid)} is correct
     * @param result The result to check
     */
    private static void assertGetAllForStoragePoolResult(List<StorageDomain> result, Guid expectedStoragePoolId) {
        assertFalse(result.isEmpty());
        for (StorageDomain domain : result) {
            assertEquals(expectedStoragePoolId, domain.getStoragePoolId());
            assertEquals(StorageDomainSharedStatus.Active, domain.getStorageDomainSharedStatus(), "Wrong shared status");
        }
    }

    @Test
    public void testGetPermittedStorageDomains() {
        List<StorageDomain> result =
                dao.getPermittedStorageDomainsByStoragePool(FixturesTool.USER_EXISTING_ID,
                        ActionGroup.CONFIGURE_VM_STORAGE,
                        FixturesTool.DATA_CENTER);
        assertFalse(result.isEmpty());
        StorageDomain domain = result.get(0);
        assertEquals(existingDomain.getId(), domain.getId());
        assertEquals(8, domain.getCommittedDiskSize(), "Wrong committed disk size");
        assertEquals(4, domain.getActualImagesSize(), "Wrong actual disk size");
        assertEquals(StorageDomainSharedStatus.Active, domain.getStorageDomainSharedStatus(), "Wrong shared status");
    }

    @Test
    public void testGetNonePermittedStorageDomains() {
        List<StorageDomain> result =
                dao.getPermittedStorageDomainsByStoragePool(FixturesTool.USER_EXISTING_ID,
                        ActionGroup.CONSUME_QUOTA,
                        FixturesTool.DATA_CENTER);
        assertTrue(result.isEmpty());
    }

    /**
     * Asserts that the existing Storage Domain exists and has VMs and VM Templates, the after remove asserts
     * that the existing domain is removed along with the VM and VM Templates
     */
    @Test
    public void testRemove() {
        List<VM> vms = vmDao.getAllForStorageDomain(FixturesTool.STORAGE_DOMAIN_SCALE_SD5);
        List<VmTemplate> templates = vmTemplateDao.getAllForStorageDomain(FixturesTool.STORAGE_DOMAIN_SCALE_SD5);
        BaseDisk diskImage = baseDiskDao.get(FixturesTool.DISK_ID);

        assertNotNull(diskImage);
        assertFalse(vms.isEmpty());
        assertFalse(templates.isEmpty());

        assertNotNull(dao.get(FixturesTool.STORAGE_DOMAIN_SCALE_SD5));

        dao.remove(existingDomain.getId());

        assertNull(dao.get(FixturesTool.STORAGE_DOMAIN_SCALE_SD5));

        for (VM vm : vms) {
            assertNull(vmDao.get(vm.getId()));
        }

        for (VmTemplate template : templates) {
            assertNull(vmTemplateDao.get(template.getId()));
        }
        assertNull(baseDiskDao.get(FixturesTool.DISK_ID));
    }

    @Test
    public void testGetNumberOfImagesInExistingDomain() {
        long numOfImages = dao.getNumberOfImagesInStorageDomain(FixturesTool.STORAGE_DOMAIN_SCALE_SD5);
        assertEquals(NUMBER_OF_IMAGES_ON_EXISTING_DOMAIN, numOfImages, "Number of images on storage domain different than expected");
    }

    @Test
    public void testGetNumberOfImagesInNonExistingDomain() {
        long numOfImages = dao.getNumberOfImagesInStorageDomain(Guid.newGuid());
        assertEquals(0, numOfImages, "Number of images on a non existing domain should be 0");
    }

    /**
     * Asserts that the existing Storage Domain exists and has VMs and VM Templates, the after remove asserts
     * that the existing domain is removed along with the VM and VM Templates
     */
    @Test
    @MockedConfig("mockConfiguration")
    public void testRemoveEntitesFromStorageDomain() {
        List<VM> vms = vmDao.getAllForStorageDomain(FixturesTool.STORAGE_DOMAIN_SCALE_SD5);
        List<VmTemplate> templates = vmTemplateDao.getAllForStorageDomain(FixturesTool.STORAGE_DOMAIN_SCALE_SD5);
        BaseDisk diskImage = baseDiskDao.get(FixturesTool.DISK_ID);

        assertNotNull(diskImage);
        assertFalse(vms.isEmpty());
        assertFalse(templates.isEmpty());

        assertNotNull(dao.get(FixturesTool.STORAGE_DOMAIN_SCALE_SD5));

        dao.removeEntitesFromStorageDomain(existingDomain.getId());

        for (VM vm : vms) {
            assertNull(vmDao.get(vm.getId()));
        }

        for (VmTemplate template : templates) {
            assertNull(vmTemplateDao.get(template.getId()));
        }
        assertNull(baseDiskDao.get(FixturesTool.DISK_ID));
    }

    @Test
    public void testAllByConnectionId() {
        List<StorageDomain> domains = dao.getAllByConnectionId(new Guid("0cc146e8-e5ed-482c-8814-270bc48c297f"));
        assertEquals(1, domains.size(), "Unexpected number of storage domains by connection id");
        assertEquals(FixturesTool.STORAGE_DOMAIN_NFS_MASTER, domains.get(0).getId(),
                "Wrong storage domain id for search by connection id");
    }

    @Test
    public void testContainsUnregisteredEntities() {
        StorageDomain storageDomain = dao.get(FixturesTool.STORAGE_DOMAIN_NFS2_1);
        assertTrue(storageDomain.isContainsUnregisteredEntities());
    }

    @Test
    public void testNotContainsUnregisteredEntities() {
        StorageDomain storageDomain = dao.get(FixturesTool.STORAGE_DOMAIN_SCALE_SD5);
        assertFalse(storageDomain.isContainsUnregisteredEntities());
    }

    @Test
    @MockedConfig("mockConfiguration")
    public void testIsHostedEngineStorage() {
        // create hosted engine vm
        VmStatic vm = new VmStatic();
        vm.setId(Guid.newGuid());
        vm.setOrigin(OriginType.HOSTED_ENGINE);
        vmStaticDao.save(vm);

        // create disk for HE
        DiskImage disk = new DiskImage();
        disk.setId(Guid.newGuid());
        disk.setImageId(Guid.newGuid());
        disk.setActive(true);
        disk.setVolumeType(VolumeType.Preallocated);
        disk.setVolumeFormat(VolumeFormat.RAW);

        imageDao.save(disk.getImage());
        baseDiskDao.save(disk);

        ImageStorageDomainMap map = new ImageStorageDomainMap(
                disk.getImageId(), existingDomain.getId(), null, null);

        imageStorageDomainMapDao.save(map);

        // attach disk
        VmDevice device = new VmDevice(
                new VmDeviceId(disk.getId(), vm.getId()),
                VmDeviceGeneralType.DISK,
                VmDeviceType.DISK.getName(),
                "",
                null,
                true,
                false,
                false,
                "",
                null,
                null,
                null);

        vmDeviceDao.save(device);

        DiskVmElement diskVmElement = new DiskVmElement(device.getId());
        diskVmElement.setDiskInterface(DiskInterface.IDE);
        diskVmElementDao.save(diskVmElement);

        // run test
        StorageDomain domain = dao.get(existingDomain.getId());
        assertTrue(domain.isHostedEngineStorage());

        // change origin
        vm.setOrigin(OriginType.MANAGED_HOSTED_ENGINE);
        vmStaticDao.update(vm);

        // run test again
        domain = dao.get(existingDomain.getId());
        assertTrue(domain.isHostedEngineStorage());
    }

    @Test
    public void testIsNotHostedEngineStorage() {
        assertFalse(existingDomain.isHostedEngineStorage());
    }
}

package org.ovirt.engine.core.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.UnregisteredDisk;
import org.ovirt.engine.core.common.businessentities.storage.UnregisteredDiskId;
import org.ovirt.engine.core.compat.Guid;
import org.springframework.util.StringUtils;

public class UnregisteredDisksDaoTest extends BaseDaoTestCase<UnregisteredDisksDao> {
    @Test
    public void testGetWithDiskId() {
        List<UnregisteredDisk> unregisteredDisk =
                dao.getByDiskIdAndStorageDomainId(FixturesTool.UNREGISTERED_DISK, FixturesTool.STORAGE_DOMAIN_NFS2_1);
        assertTrue(!unregisteredDisk.isEmpty(), "Disk should exists in the UnregisteredDisks table");
        assertTrue(unregisteredDisk.get(0).getVms().isEmpty(),
                "Vms id should be empty list in the UnregisteredDisks table");
    }

    @Test
    public void testGetWithNotExistingDiskId() {
        List<UnregisteredDisk> unregisteredDisk =
                dao.getByDiskIdAndStorageDomainId(Guid.newGuid(), FixturesTool.STORAGE_DOMAIN_NFS2_1);
        assertTrue(unregisteredDisk.isEmpty(), "Disk should not exists in the UnregisteredDisks table");
    }

    @Test
    public void testGetDiskAttachedToVm() {
        List<UnregisteredDisk> unregisteredDisk =
                dao.getByDiskIdAndStorageDomainId(FixturesTool.UNREGISTERED_DISK2, FixturesTool.STORAGE_DOMAIN_NFS2_1);
        assertTrue(!unregisteredDisk.isEmpty(), "Disk should exists in the UnregisteredDisks table");
        assertEquals(1, unregisteredDisk.get(0).getVms().size(), "Disk should have one vm attached");
    }

    @Test
    public void testGetDiskAttachedToMultipleVms() {
        List<UnregisteredDisk> unregisteredDisk =
                dao.getByDiskIdAndStorageDomainId(FixturesTool.UNREGISTERED_DISK3, FixturesTool.STORAGE_DOMAIN_NFS2_1);
        assertTrue(!unregisteredDisk.isEmpty(), "Disk should exists in the UnregisteredDisks table");
        assertEquals(1, unregisteredDisk.get(0).getVms().size(), "Disk should be attached to VM");
    }

    @Test
    public void testGetDiskAttachedToMultipleVmsWithoutDescription() {
        List<UnregisteredDisk> unregisteredDisk =
                dao.getByDiskIdAndStorageDomainId(FixturesTool.UNREGISTERED_DISK4, FixturesTool.STORAGE_DOMAIN_NFS2_1);
        assertTrue(!unregisteredDisk.isEmpty(), "Disk should exists in the UnregisteredDisks table");
        assertEquals(1, unregisteredDisk.get(0).getVms().size(), "Disk should be attached to VM");
        assertFalse(unregisteredDisk.get(0).getDiskImage().getDiskAlias().isEmpty(), "Disk should have disk alias");
        assertTrue(StringUtils.isEmpty(unregisteredDisk.get(0).getDiskImage().getDiskDescription()),
                "Disk should have an empty disk description");
    }

    @Test
    public void testGetDiskAttachedToMultipleVmsWithoutAliasAndDescription() {
        List<UnregisteredDisk> unregisteredDisk =
                dao.getByDiskIdAndStorageDomainId(FixturesTool.UNREGISTERED_DISK5, FixturesTool.STORAGE_DOMAIN_NFS2_1);
        assertTrue(!unregisteredDisk.isEmpty(), "Disk should exists in the UnregisteredDisks table");
        assertEquals(1, unregisteredDisk.get(0).getVms().size(), "Disk should be attached to VM");
        assertTrue(unregisteredDisk.get(0).getDiskAlias().isEmpty(), "Disk should have an empty disk alias");
        assertTrue(StringUtils.isEmpty(unregisteredDisk.get(0).getDiskDescription()),
                "Disk should have an empty disk description");
    }

    @Test
    public void testGetDiskForAllStorageDomain() {
        List<UnregisteredDisk> unregisteredDisk =
                dao.getByDiskIdAndStorageDomainId(FixturesTool.UNREGISTERED_DISK, null);
        assertTrue(!unregisteredDisk.isEmpty(), "Disk should exists in the UnregisteredDisks table");
        assertTrue(unregisteredDisk.get(0).getVms().isEmpty(), "Disk should exists in the UnregisteredDisks table");
    }

    @Test
    public void testGetAllDisksForStorageDomain() {
        List<UnregisteredDisk> unregisteredDisk =
                dao.getByDiskIdAndStorageDomainId(null, FixturesTool.STORAGE_DOMAIN_NFS2_1);
        assertTrue(!unregisteredDisk.isEmpty(), "Disk should exists in the UnregisteredDisks table");
    }

    @Test
    public void testDeleteUnregisteredDiskRelatedToVM() {
        List<UnregisteredDisk> unregisteredDisk2 =
                dao.getByDiskIdAndStorageDomainId(FixturesTool.UNREGISTERED_DISK2, FixturesTool.STORAGE_DOMAIN_NFS2_1);
        List<UnregisteredDisk> unregisteredDisk3 =
                dao.getByDiskIdAndStorageDomainId(FixturesTool.UNREGISTERED_DISK3, FixturesTool.STORAGE_DOMAIN_NFS2_1);
        assertTrue(!unregisteredDisk2.isEmpty(), "Disk should exists in the UnregisteredDisks table.");
        assertTrue(!unregisteredDisk3.isEmpty(), "Disk should exists in the UnregisteredDisks table.");
        dao.removeUnregisteredDiskRelatedToVM(FixturesTool.VM_RHEL5_POOL_57, FixturesTool.STORAGE_DOMAIN_NFS2_1);
        unregisteredDisk2 =
                dao.getByDiskIdAndStorageDomainId(FixturesTool.UNREGISTERED_DISK2, FixturesTool.STORAGE_DOMAIN_NFS2_1);
        unregisteredDisk3 =
                dao.getByDiskIdAndStorageDomainId(FixturesTool.UNREGISTERED_DISK3, FixturesTool.STORAGE_DOMAIN_NFS2_1);
        assertTrue(unregisteredDisk2.isEmpty(), "Disk should exists in the UnregisteredDisks table.");
        assertTrue(unregisteredDisk3.isEmpty(), "Disk should exists in the UnregisteredDisks table.");
    }

    @Test
    public void testRemoveUnregisteredDiskRelatedToVM() {
        dao.removeUnregisteredDisk(FixturesTool.UNREGISTERED_DISK, FixturesTool.STORAGE_DOMAIN_NFS2_1);
        List<UnregisteredDisk> unregisteredDisk =
                dao.getByDiskIdAndStorageDomainId(FixturesTool.UNREGISTERED_DISK, null);
        assertTrue(unregisteredDisk.isEmpty(),
                "Disk should not exists in the UnregisteredDisks table after being deleted.");
    }

    @Test
    public void testSaveDiskWithAliasAndDescription() {
        ArrayList<VmBase> vms = new ArrayList<>();
        UnregisteredDisk unregisteredDisk = initUnregisteredDisks(vms);
        dao.saveUnregisteredDisk(unregisteredDisk);
        List<UnregisteredDisk> fetchedUnregisteredDisk =
                dao.getByDiskIdAndStorageDomainId(unregisteredDisk.getDiskId(),
                        FixturesTool.STORAGE_DOMAIN_NFS2_1);

        assertTrue(!fetchedUnregisteredDisk.isEmpty(), "Disk should exists in the UnregisteredDisks table");
        assertTrue(fetchedUnregisteredDisk.get(0).getVms().isEmpty(), "Disk should not have multiple vms attached");
        assertEquals("Disk Alias", fetchedUnregisteredDisk.get(0).getDiskAlias(), "Disk alias should be the same as initialized");
        assertEquals("Disk Description", fetchedUnregisteredDisk.get(0).getDiskDescription(), "Disk description should be the same as initialized");
        assertEquals(0,
                fetchedUnregisteredDisk.get(0).getStorageDomainId().compareTo(FixturesTool.STORAGE_DOMAIN_NFS2_1),
                "Storage Domain id should be the same as initialized");
    }

    private UnregisteredDisk initUnregisteredDisks(ArrayList<VmBase> vms) {
        DiskImage diskImage = new DiskImage();
        diskImage.setId(Guid.newGuid());
        diskImage.setDiskAlias("Disk Alias");
        diskImage.setDiskDescription("Disk Description");
        diskImage.setStorageIds(new ArrayList<>(Collections.singletonList(FixturesTool.STORAGE_DOMAIN_NFS2_1)));
        UnregisteredDiskId id = new UnregisteredDiskId(diskImage.getId(), diskImage.getStorageIds().get(0));
        return new UnregisteredDisk(id, diskImage, vms);
    }

    @Test
    public void testSaveAttachedDiskWithoutAliasAndDescription() {
        VmBase vm1 = new VmBase();
        vm1.setId(Guid.newGuid());
        vm1.setName("First VM");
        ArrayList<VmBase> vms = new ArrayList<>();
        vms.add(vm1);

        // Set new disk image.
        DiskImage diskImage = new DiskImage();
        diskImage.setId(Guid.newGuid());
        diskImage.setStorageIds(new ArrayList<>(Collections.singletonList(FixturesTool.STORAGE_DOMAIN_NFS2_1)));
        UnregisteredDiskId id = new UnregisteredDiskId(diskImage.getId(), diskImage.getStorageIds().get(0));
        UnregisteredDisk unregDisk = new UnregisteredDisk(id, diskImage, vms);
        dao.saveUnregisteredDisk(unregDisk);
        List<UnregisteredDisk> fetchedUnregisteredDisk =
                dao.getByDiskIdAndStorageDomainId(unregDisk.getDiskId(), FixturesTool.STORAGE_DOMAIN_NFS2_1);
        assertTrue(!fetchedUnregisteredDisk.isEmpty(), "Disk should exists in the UnregisteredDisks table");
        assertEquals(1, fetchedUnregisteredDisk.get(0).getVms().size(), "Disk should have vm attached");
        assertTrue(fetchedUnregisteredDisk.get(0).getDiskAlias().isEmpty(), "Disk alias should not be initialized");
        assertTrue(StringUtils.isEmpty(fetchedUnregisteredDisk.get(0).getDiskDescription()),
                "Disk description should not be initialized");
        assertEquals(0,
                fetchedUnregisteredDisk.get(0).getStorageDomainId().compareTo(FixturesTool.STORAGE_DOMAIN_NFS2_1),
                "Storage Domain id should be the same as initialized");
    }
}

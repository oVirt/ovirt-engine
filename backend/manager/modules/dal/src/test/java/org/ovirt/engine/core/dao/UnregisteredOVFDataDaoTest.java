package org.ovirt.engine.core.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.OvfEntityData;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.compat.Version;

public class UnregisteredOVFDataDaoTest extends BaseDaoTestCase<UnregisteredOVFDataDao> {
    @Test
    public void testGetWithEntityId() {
        List<OvfEntityData> ovfEntityDataList =
                dao.getByEntityIdAndStorageDomain(FixturesTool.UNREGISTERED_VM, FixturesTool.STORAGE_DOMAIN_NFS2_1);
        assertFalse(ovfEntityDataList.isEmpty(), "VM should exists in the UnregisteredOVFData");
    }

    @Test
    public void testGetWithNotExistingEntityId() {
        List<OvfEntityData> ovfEntityDataList =
                dao.getByEntityIdAndStorageDomain(FixturesTool.VM_RHEL5_POOL_51, FixturesTool.STORAGE_DOMAIN_NFS2_1);
        assertTrue(ovfEntityDataList.isEmpty(), "VM should not exists in the UnregisteredOVFData");
    }

    @Test
    public void testGetWithVMOnWrongStorageDomainId() {
        List<OvfEntityData> ovfEntityDataList =
                dao.getByEntityIdAndStorageDomain(FixturesTool.UNREGISTERED_VM, FixturesTool.STORAGE_DOMAIN_NFS2_2);
        assertTrue(ovfEntityDataList.isEmpty(),
                "VM should not exists in the UnregisteredOVFData for the specific Storage Domain");
    }

    @Test
    public void testGetVMsForStorageDomain() {
        List<OvfEntityData> ovfEntityDataList =
                dao.getAllForStorageDomainByEntityType(FixturesTool.STORAGE_DOMAIN_NFS2_1, VmEntityType.VM);
        assertTrue(!ovfEntityDataList.isEmpty(), "A VM should be fetched for the specified storage domain");
    }

    @Test
    public void testGetTemplatesForStorageDomain() {
        List<OvfEntityData> ovfEntityDataList =
                dao.getAllForStorageDomainByEntityType(FixturesTool.STORAGE_DOMAIN_NFS2_1, VmEntityType.TEMPLATE);
        assertTrue(!ovfEntityDataList.isEmpty(), "A Template should be fetched for the specified storage domain");
    }

    @Test
    public void testGetEntitiesForNotRelatedStorageDomain() {
        List<OvfEntityData> ovfEntityDataList =
                dao.getAllForStorageDomainByEntityType(FixturesTool.STORAGE_DOMAIN_NFS2_2, VmEntityType.VM);
        assertTrue(ovfEntityDataList.isEmpty(), "No VM should be fetched for the specified storage domain");
    }

    @Test
    public void testGetAllEntitiesForStorageDomain() {
        List<OvfEntityData> ovfEntityDataList =
                dao.getAllForStorageDomainByEntityType(FixturesTool.STORAGE_DOMAIN_NFS2_1, null);
        assertEquals(2, ovfEntityDataList.size(),
                "A Template and a VM should be fetched for the specified storage domain");
    }

    @Test
    public void testGetAllEntitiesForStorageDomainWithNoUnregisteredEntities() {
        List<OvfEntityData> ovfEntityDataList =
                dao.getAllForStorageDomainByEntityType(FixturesTool.STORAGE_DOMAIN_NFS2_2, null);
        assertTrue(ovfEntityDataList.isEmpty(), "No entities should be fetched for the specified storage domain");
    }

    @Test
    public void testGetTemplatesForNotRelatedStorageDomain() {
        List<OvfEntityData> ovfEntityDataList =
                dao.getAllForStorageDomainByEntityType(FixturesTool.STORAGE_DOMAIN_NFS2_2, VmEntityType.TEMPLATE);
        assertTrue(ovfEntityDataList.isEmpty(), "No Template should be fetched for the specified storage domain");
    }

    @Test
    public void testInsertTemplateToUnregisteredEntity() {
        final String ovfExtraData = "<ovf> Some extra OVF data </ovf>";
        OvfEntityData ovfEntityData = new OvfEntityData(FixturesTool.VM_TEMPLATE_RHEL5,
                "AnyVM",
                VmEntityType.TEMPLATE,
                ArchitectureType.x86_64,
                Version.getLast(),
                FixturesTool.STORAGE_DOMAIN_NFS2_1,
                null,
                ovfExtraData);

        dao.saveOVFData(ovfEntityData);

        List<OvfEntityData> fetchedOvfEntityData =
                dao.getByEntityIdAndStorageDomain(FixturesTool.VM_TEMPLATE_RHEL5, FixturesTool.STORAGE_DOMAIN_NFS2_1);
        assertEquals(1, fetchedOvfEntityData.size());
        assertTrue(fetchedOvfEntityData.get(0).getEntityType().isTemplateType(), "The entity type should be template");
        assertEquals(ovfExtraData, fetchedOvfEntityData.get(0).getOvfExtraData(), "The entity OVF extra data should be updated");
    }

    @Test
    public void testDeleteUnregisteredEntity() {
        List<OvfEntityData> ovfEntityDataList =
                dao.getByEntityIdAndStorageDomain(FixturesTool.UNREGISTERED_VM, FixturesTool.STORAGE_DOMAIN_NFS2_1);
        assertEquals(1, ovfEntityDataList.size());
        assertFalse(ovfEntityDataList.isEmpty());
        dao.removeEntity(FixturesTool.UNREGISTERED_VM, FixturesTool.STORAGE_DOMAIN_NFS2_1);
        List<OvfEntityData> ovfEntityDataList2 =
                dao.getByEntityIdAndStorageDomain(FixturesTool.UNREGISTERED_VM, FixturesTool.STORAGE_DOMAIN_NFS2_1);
        assertTrue(ovfEntityDataList2.isEmpty());
    }

    @Test
    public void testGetUnregisteredEntitiesWithStorageDomainNull() {
        List<OvfEntityData> ovfEntityDataList =
                dao.getByEntityIdAndStorageDomain(FixturesTool.UNREGISTERED_TEMPLATE, null);
        assertEquals(2, ovfEntityDataList.size());
        assertEquals(FixturesTool.UNREGISTERED_TEMPLATE, ovfEntityDataList.get(0).getEntityId());
        assertEquals(FixturesTool.UNREGISTERED_TEMPLATE, ovfEntityDataList.get(1).getEntityId());
        assertNotEquals(ovfEntityDataList.get(0).getStorageDomainId(), ovfEntityDataList.get(1).getStorageDomainId());
    }
}

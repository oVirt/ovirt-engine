package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.OvfEntityData;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.compat.Version;

public class UnregisteredOVFDataDaoTest extends BaseDaoTestCase {
    private UnregisteredOVFDataDao dao;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        dao = dbFacade.getUnregisteredOVFDataDao();
    }

    @Test
    public void testGetWithEntityId() {
        List<OvfEntityData> ovfEntityDataList =
                dao.getByEntityIdAndStorageDomain(FixturesTool.UNREGISTERED_VM, FixturesTool.STORAGE_DOAMIN_NFS2_1);
        assertFalse("VM should exists in the UnregisteredOVFData", ovfEntityDataList.isEmpty());
    }

    @Test
    public void testGetWithNotExistingEntityId() {
        List<OvfEntityData> ovfEntityDataList =
                dao.getByEntityIdAndStorageDomain(FixturesTool.VM_RHEL5_POOL_51, FixturesTool.STORAGE_DOAMIN_NFS2_1);
        assertTrue("VM should not exists in the UnregisteredOVFData", ovfEntityDataList.isEmpty());
    }

    @Test
    public void testGetWithVMOnWrongStorageDomainId() {
        List<OvfEntityData> ovfEntityDataList =
                dao.getByEntityIdAndStorageDomain(FixturesTool.UNREGISTERED_VM, FixturesTool.STORAGE_DOAMIN_NFS2_2);
        assertTrue("VM should not exists in the UnregisteredOVFData for the specific Storage Domain",
                ovfEntityDataList.isEmpty());
    }

    @Test
    public void testGetVMsForStorageDomain() {
        List<OvfEntityData> ovfEntityDataList =
                dao.getAllForStorageDomainByEntityType(FixturesTool.STORAGE_DOAMIN_NFS2_1, VmEntityType.VM);
        assertTrue("A VM should be fetched for the specified storage domain", !ovfEntityDataList.isEmpty());
    }

    @Test
    public void testGetTemplatesForStorageDomain() {
        List<OvfEntityData> ovfEntityDataList =
                dao.getAllForStorageDomainByEntityType(FixturesTool.STORAGE_DOAMIN_NFS2_1, VmEntityType.TEMPLATE);
        assertTrue("A Template should be fetched for the specified storage domain", !ovfEntityDataList.isEmpty());
    }

    @Test
    public void testGetEntitiesForNotRelatedStorageDomain() {
        List<OvfEntityData> ovfEntityDataList =
                dao.getAllForStorageDomainByEntityType(FixturesTool.STORAGE_DOAMIN_NFS2_2, VmEntityType.VM);
        assertTrue("No VM should be fetched for the specified storage domain", ovfEntityDataList.isEmpty());
    }

    @Test
    public void testGetAllEntitiesForStorageDomain() {
        List<OvfEntityData> ovfEntityDataList =
                dao.getAllForStorageDomainByEntityType(FixturesTool.STORAGE_DOAMIN_NFS2_1, null);
        assertEquals("A Template and a VM should be fetched for the specified storage domain",
                2,
                ovfEntityDataList.size());
    }

    @Test
    public void testGetAllEntitiesForStorageDomainWithNoUnregisteredEntities() {
        List<OvfEntityData> ovfEntityDataList =
                dao.getAllForStorageDomainByEntityType(FixturesTool.STORAGE_DOAMIN_NFS2_2, null);
        assertTrue("No entities should be fetched for the specified storage domain", ovfEntityDataList.isEmpty());
    }

    @Test
    public void testGetTemplatesForNotRelatedStorageDomain() {
        List<OvfEntityData> ovfEntityDataList =
                dao.getAllForStorageDomainByEntityType(FixturesTool.STORAGE_DOAMIN_NFS2_2, VmEntityType.TEMPLATE);
        assertTrue("No Template should be fetched for the specified storage domain", ovfEntityDataList.isEmpty());
    }

    @Test
    public void testInsertTemplateToUnregisteredEntity() {
        final String ovfExtraData = "<ovf> Some extra OVF data </ovf>";
        OvfEntityData ovfEntityData = new OvfEntityData(FixturesTool.VM_TEMPLATE_RHEL5,
                "AnyVM",
                VmEntityType.TEMPLATE,
                ArchitectureType.x86_64,
                Version.getLast(),
                FixturesTool.STORAGE_DOAMIN_NFS2_1,
                null,
                ovfExtraData);

        dao.saveOVFData(ovfEntityData);

        List<OvfEntityData> fetchedOvfEntityData =
                dao.getByEntityIdAndStorageDomain(FixturesTool.VM_TEMPLATE_RHEL5, FixturesTool.STORAGE_DOAMIN_NFS2_1);
        assertTrue(fetchedOvfEntityData.size() == 1);
        assertTrue("The entity type should be template", fetchedOvfEntityData.get(0).getEntityType().isTemplateType());
        assertTrue("The entity OVF extra data should be updated", fetchedOvfEntityData.get(0).getOvfExtraData()
                .equals(ovfExtraData));
    }

    @Test
    public void testDeleteUnregisteredEntity() {
        List<OvfEntityData> ovfEntityDataList =
                dao.getByEntityIdAndStorageDomain(FixturesTool.UNREGISTERED_VM, FixturesTool.STORAGE_DOAMIN_NFS2_1);
        assertTrue(ovfEntityDataList.size() == 1);
        assertFalse(ovfEntityDataList.isEmpty());
        dao.removeEntity(FixturesTool.UNREGISTERED_VM, FixturesTool.STORAGE_DOAMIN_NFS2_1);
        List<OvfEntityData> ovfEntityDataList2 =
                dao.getByEntityIdAndStorageDomain(FixturesTool.UNREGISTERED_VM, FixturesTool.STORAGE_DOAMIN_NFS2_1);
        assertTrue(ovfEntityDataList2.isEmpty());
    }

    @Test
    public void testGetUnregisteredEntitiesWithStorageDomainNull() {
        List<OvfEntityData> ovfEntityDataList =
                dao.getByEntityIdAndStorageDomain(FixturesTool.UNREGISTERED_TEMPLATE, null);
        assertTrue(ovfEntityDataList.size() == 2);
        assertEquals(ovfEntityDataList.get(0).getEntityId(), FixturesTool.UNREGISTERED_TEMPLATE);
        assertEquals(ovfEntityDataList.get(1).getEntityId(), FixturesTool.UNREGISTERED_TEMPLATE);
        assertFalse(ovfEntityDataList.get(0).getStorageDomainId().equals(ovfEntityDataList.get(1).getStorageDomainId()));
    }
}

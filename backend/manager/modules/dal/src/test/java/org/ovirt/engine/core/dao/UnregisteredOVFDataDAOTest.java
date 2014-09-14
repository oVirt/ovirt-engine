package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.OvfEntityData;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.compat.Version;

public class UnregisteredOVFDataDAOTest extends BaseDAOTestCase {
    private UnregisteredOVFDataDAO dao;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        dao = dbFacade.getUnregisteredOVFDataDao();
    }

    @Test
    public void testGetWithEntityId() {
        OvfEntityData ovfEntityData =
                dao.getByEntityIdAndStorageDomain(FixturesTool.UNREGISTERED_VM, FixturesTool.STORAGE_DOAMIN_NFS2_1);
        assertNotNull("VM should exists in the UnregisteredOVFData", ovfEntityData);
    }

    @Test
    public void testGetWithNotExistingEntityId() {
        OvfEntityData ovfEntityData =
                dao.getByEntityIdAndStorageDomain(FixturesTool.VM_RHEL5_POOL_51, FixturesTool.STORAGE_DOAMIN_NFS2_1);
        assertNull("VM should not exists in the UnregisteredOVFData", ovfEntityData);
    }

    @Test
    public void testGetWithVMOnWrongStorageDomainId() {
        OvfEntityData ovfEntityData =
                dao.getByEntityIdAndStorageDomain(FixturesTool.UNREGISTERED_VM, FixturesTool.STORAGE_DOAMIN_NFS2_2);
        assertNull("VM should not exists in the UnregisteredOVFData for the specific Storage Domain", ovfEntityData);
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
                Version.v3_4,
                FixturesTool.STORAGE_DOAMIN_NFS2_1,
                null,
                ovfExtraData);

        dao.saveOVFData(ovfEntityData);

        OvfEntityData fetchedOvfEntityData =
                dao.getByEntityIdAndStorageDomain(FixturesTool.VM_TEMPLATE_RHEL5, FixturesTool.STORAGE_DOAMIN_NFS2_1);
        assertNotNull(fetchedOvfEntityData);
        assertTrue("The entity type should be template", fetchedOvfEntityData.getEntityType().isTemplateType());
        assertTrue("The entity OVF extra data should be updated", fetchedOvfEntityData.getOvfExtraData()
                .equals(ovfExtraData));
    }

    @Test
    public void testDeleteUnregisteredEntity() {
        OvfEntityData ovfEntityData =
                dao.getByEntityIdAndStorageDomain(FixturesTool.UNREGISTERED_VM, FixturesTool.STORAGE_DOAMIN_NFS2_1);
        assertNotNull(ovfEntityData);
        dao.removeEntity(FixturesTool.UNREGISTERED_VM, FixturesTool.STORAGE_DOAMIN_NFS2_1);
        ovfEntityData =
                dao.getByEntityIdAndStorageDomain(FixturesTool.UNREGISTERED_VM, FixturesTool.STORAGE_DOAMIN_NFS2_1);
        assertNull(ovfEntityData);
    }
}

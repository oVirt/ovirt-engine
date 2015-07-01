package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.ExternalStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainDynamic;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageFormatType;
import org.ovirt.engine.core.compat.Guid;

public class StorageDomainDynamicDaoTest extends BaseDaoTestCase {
    private static final Guid EXISTING_DOMAIN_ID = new Guid("72e3a666-89e1-4005-a7ca-f7548004a9ab");
    private static final int USED_DISK_SIZE = 1000;

    private StorageDomainDynamicDao dao;
    private StorageDomainStaticDao staticDao;
    private StorageDomainDynamic newDynamicDomain;
    private StorageDomainStatic newStaticDomain;
    private StorageDomainDynamic existingDynamic;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        dao = dbFacade.getStorageDomainDynamicDao();
        staticDao = dbFacade.getStorageDomainStaticDao();

        existingDynamic = dao.get(EXISTING_DOMAIN_ID);

        newStaticDomain = new StorageDomainStatic();
        newStaticDomain.setStorage("fDMzhE-wx3s-zo3q-Qcxd-T0li-yoYU-QvVePl");
        newStaticDomain.setStorageFormat(StorageFormatType.V1);
        newStaticDomain.setWipeAfterDelete(true);
        newDynamicDomain = new StorageDomainDynamic();
        newDynamicDomain.setAvailableDiskSize(USED_DISK_SIZE);
    }

    /**
     * Ensures that retrieving the dynamic domain works as expected.
     */
    @Test
    public void testGet() {
        StorageDomainDynamic result = dao.get(EXISTING_DOMAIN_ID);

        assertNotNull(result);
        assertEquals(EXISTING_DOMAIN_ID, result.getId());
    }

    /**
     * Ensures that get all is not implemented.
     */
    @Test
    public void testGetAll() {
        List<StorageDomainDynamic> result = dao.getAll();

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    /**
     * Ensures that saving a domain works as expected.
     */
    @Test
    public void testSave() {
        staticDao.save(newStaticDomain);
        newDynamicDomain.setId(newStaticDomain.getId());
        dao.save(newDynamicDomain);

        StorageDomainDynamic result = dao.get(newDynamicDomain.getId());

        assertNotNull(result);
    }

    /**
     * Ensures that updating the static and dynamic portions works as expected.
     */
    @Test
    public void testUpdate() {
        existingDynamic.setUsedDiskSize(USED_DISK_SIZE);
        dao.update(existingDynamic);

        StorageDomainDynamic result = dao.get(existingDynamic.getId());

        assertEquals(existingDynamic, result);
    }

    @Test
    public void testUpdateStorageDomainExternalStatus() {
        StorageDomainDynamic before = dao.get(existingDynamic.getId());
        before.setExternalStatus(ExternalStatus.Error);
        dao.updateExternalStatus(before.getId(), before.getExternalStatus());
        StorageDomainDynamic after = dao.get(existingDynamic.getId());
        assertEquals(before.getExternalStatus(), after.getExternalStatus());
    }
    // testRemove is already tested as part of the StorageDomainStaticDaoTest
}

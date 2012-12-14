package org.ovirt.engine.core.dao;

import java.util.List;
import static org.junit.Assert.*;

import org.junit.Test;

import org.ovirt.engine.core.common.businessentities.StorageDomainDynamic;
import org.ovirt.engine.core.common.businessentities.storage_domain_static;
import org.ovirt.engine.core.compat.Guid;

public class StorageDomainDynamicDAOTest extends BaseDAOTestCase {
    private static final Guid EXISTING_DOMAIN_ID = new Guid("72e3a666-89e1-4005-a7ca-f7548004a9ab");
    private static final int USED_DISK_SIZE = 1000;

    private StorageDomainDynamicDAO dao;
    private StorageDomainStaticDAO staticDao;
    private StorageDomainDynamic newDynamicDomain;
    private storage_domain_static newStaticDomain;
    private StorageDomainDynamic existingDynamic;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        dao = prepareDAO(dbFacade.getStorageDomainDynamicDao());
        staticDao = prepareDAO(dbFacade.getStorageDomainStaticDao());

        existingDynamic = dao.get(EXISTING_DOMAIN_ID);

        newStaticDomain = new storage_domain_static();
        newStaticDomain.setstorage("fDMzhE-wx3s-zo3q-Qcxd-T0li-yoYU-QvVePl");
        newDynamicDomain = new StorageDomainDynamic();
        newDynamicDomain.setavailable_disk_size(USED_DISK_SIZE);
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
        existingDynamic.setused_disk_size(USED_DISK_SIZE);
        dao.update(existingDynamic);

        StorageDomainDynamic result = dao.get(existingDynamic.getId());

        assertEquals(existingDynamic, result);
    }

    // testRemove is already tested as part of the StorageDomainStaticDAOTest
}

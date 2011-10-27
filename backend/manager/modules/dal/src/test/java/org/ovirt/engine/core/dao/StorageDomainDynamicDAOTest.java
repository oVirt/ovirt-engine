package org.ovirt.engine.core.dao;

import java.util.List;
import static org.junit.Assert.*;

import org.junit.Test;

import org.ovirt.engine.core.common.businessentities.storage_domain_dynamic;
import org.ovirt.engine.core.common.businessentities.storage_domain_static;
import org.ovirt.engine.core.compat.Guid;

public class StorageDomainDynamicDAOTest extends BaseDAOTestCase {
    private static final Guid EXISTING_DOMAIN_ID = new Guid("72e3a666-89e1-4005-a7ca-f7548004a9ab");
    private static final int USED_DISK_SIZE = 1000;

    private StorageDomainDynamicDAO dao;
    private StorageDomainStaticDAO staticDao;
    private storage_domain_dynamic newDynamicDomain;
    private storage_domain_static newStaticDomain;
    private storage_domain_dynamic existingDynamic;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        dao = prepareDAO(dbFacade.getStorageDomainDynamicDAO());
        staticDao = prepareDAO(dbFacade.getStorageDomainStaticDAO());

        existingDynamic = dao.get(EXISTING_DOMAIN_ID);

        newStaticDomain = new storage_domain_static();
        newStaticDomain.setstorage("fDMzhE-wx3s-zo3q-Qcxd-T0li-yoYU-QvVePl");
        newDynamicDomain = new storage_domain_dynamic();
        newDynamicDomain.setavailable_disk_size(USED_DISK_SIZE);
    }

    /**
     * Ensures that retrieving the dynamic domain works as expected.
     */
    @Test
    public void testGet() {
        storage_domain_dynamic result = dao.get(EXISTING_DOMAIN_ID);

        assertNotNull(result);
        assertEquals(EXISTING_DOMAIN_ID, result.getId());
    }

    /**
     * Ensures that get all is not implemented.
     */
    @Test
    public void testGetAll() {
        List<storage_domain_dynamic> result = dao.getAll();

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

        storage_domain_dynamic result = dao.get(newDynamicDomain.getId());

        assertNotNull(result);
    }

    /**
     * Ensures that updating the static and dynamic portions works as expected.
     */
    @Test
    public void testUpdate() {
        existingDynamic.setused_disk_size(USED_DISK_SIZE);
        dao.update(existingDynamic);

        storage_domain_dynamic result = dao.get(existingDynamic.getId());

        assertEquals(existingDynamic, result);
    }

    // testRemove is already tested as part of the StorageDomainStaticDAOTest
}

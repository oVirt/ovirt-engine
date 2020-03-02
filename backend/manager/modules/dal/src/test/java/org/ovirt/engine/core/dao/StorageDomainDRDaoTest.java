package org.ovirt.engine.core.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.StorageDomainDR;
import org.ovirt.engine.core.compat.Guid;

public class StorageDomainDRDaoTest extends BaseDaoTestCase<StorageDomainDRDao> {

    private StorageDomainDR storageDomainDR;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();

        storageDomainDR = new StorageDomainDR();
        storageDomainDR.setStorageDomainId(FixturesTool.POSIX_STORAGE_DOMAIN_ID);
        storageDomainDR.setGeoRepSessionId(FixturesTool.GLUSTER_GEOREP_SESSION_ID2);
        storageDomainDR.setScheduleCronExpression("0 30 22 * * ? *");
        storageDomainDR.setJobId(Guid.createGuidFromString("77569427-9fbe-41db-ae91-fb96fab17141"));
    }

    @Test
    public void testGetStorageDomainDR() {
        StorageDomainDR result = dao.get(FixturesTool.POSIX_STORAGE_DOMAIN_ID, FixturesTool.GLUSTER_GEOREP_SESSION_ID2);
        assertEquals(storageDomainDR , result);
    }

    @Test
    public void testSaveorUpdate() {
        storageDomainDR.setScheduleCronExpression(null);
        dao.saveOrUpdate(storageDomainDR);

        StorageDomainDR result = dao.get(FixturesTool.POSIX_STORAGE_DOMAIN_ID, FixturesTool.GLUSTER_GEOREP_SESSION_ID2);
        assertEquals(storageDomainDR , result);
    }

    @Test
    public void testGetStorageDomainDRs() {
        List<StorageDomainDR> result = dao.getAllForStorageDomain(FixturesTool.POSIX_STORAGE_DOMAIN_ID);
        assertEquals(1, result.size());
    }

    @Test
    public void testGetStorageDomainDRWithGeoRep() {
        List<StorageDomainDR> result = dao.getWithGeoRepSession(FixturesTool.GLUSTER_GEOREP_SESSION_ID2);
        assertEquals(1, result.size());
    }

    @Test
    public void testUpdate() {
        storageDomainDR.setJobId(Guid.createGuidFromString("afce7a39-8e8c-4819-ba9c-796d316592e7"));
        dao.update(storageDomainDR);
        StorageDomainDR result = dao.get(FixturesTool.POSIX_STORAGE_DOMAIN_ID, FixturesTool.GLUSTER_GEOREP_SESSION_ID2);
        assertEquals(storageDomainDR, result);
    }

    @Test
    public void testRemoveAndSave() {
        dao.remove(FixturesTool.POSIX_STORAGE_DOMAIN_ID, FixturesTool.GLUSTER_GEOREP_SESSION_ID2);
        StorageDomainDR result = dao.get(FixturesTool.POSIX_STORAGE_DOMAIN_ID, FixturesTool.GLUSTER_GEOREP_SESSION_ID2);
        assertNull(result);
        dao.save(storageDomainDR);
        result = dao.get(FixturesTool.POSIX_STORAGE_DOMAIN_ID, FixturesTool.GLUSTER_GEOREP_SESSION_ID2);
        assertEquals(storageDomainDR, result);
    }
}

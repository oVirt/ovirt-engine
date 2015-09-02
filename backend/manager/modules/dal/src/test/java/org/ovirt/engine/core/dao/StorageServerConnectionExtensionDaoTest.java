package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.storage.StorageServerConnectionExtension;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.RandomUtils;
import org.ovirt.engine.core.utils.RandomUtilsSeedingRule;

public class StorageServerConnectionExtensionDaoTest extends BaseDaoTestCase {

    @Rule
    public final RandomUtilsSeedingRule rusr = new RandomUtilsSeedingRule();

    private StorageServerConnectionExtensionDao dao;

    private static final Guid EXISTING_STORAGE_SERVER_CONNECTION_EXTENSION_ID = new Guid("9f0852ba-7c96-4974-9cb8-b214a6bf90d8");
    private static final int NUM_OF_EXISTING_STORAGE_SERVER_CONNECTION_EXTENSIONS = 2;

    @Override public void setUp() throws Exception {
        super.setUp();
        dao = dbFacade.getStorageServerConnectionExtensionDao();
    }

    @Test
    public void testGetExisting() {
        StorageServerConnectionExtension ssce = dao.get(EXISTING_STORAGE_SERVER_CONNECTION_EXTENSION_ID);
        assertNotNull(ssce);
        assertEquals(EXISTING_STORAGE_SERVER_CONNECTION_EXTENSION_ID, ssce.getId());
    }

    @Test
    public void testGetNonExisting() {
        StorageServerConnectionExtension ssce = dao.get(Guid.newGuid());
        assertNull(ssce);
    }

    @Test
    public void testUpdate() {
        StorageServerConnectionExtension ssce = dao.get(EXISTING_STORAGE_SERVER_CONNECTION_EXTENSION_ID);
        fillWithRandomData(ssce);
        dao.update(ssce);

        StorageServerConnectionExtension ssceFromDb = dao.get(EXISTING_STORAGE_SERVER_CONNECTION_EXTENSION_ID);
        assertEquals(ssce, ssceFromDb);
    }

    @Test
    public void testRemove() {
        dao.remove(EXISTING_STORAGE_SERVER_CONNECTION_EXTENSION_ID);
        StorageServerConnectionExtension ssce = dao.get(EXISTING_STORAGE_SERVER_CONNECTION_EXTENSION_ID);
        assertNull(ssce);
    }

    @Test
    public void testInsert() {
        Guid newId = Guid.newGuid();
        StorageServerConnectionExtension newssce = new StorageServerConnectionExtension();
        newssce.setId(newId);
        fillWithRandomData(newssce);
        dao.save(newssce);

        StorageServerConnectionExtension ssceFromDb = dao.get(newId);
        assertEquals(newssce, ssceFromDb);
    }

    @Test
    public void testGetAll() {
        List<StorageServerConnectionExtension> results = dao.getAll();
        assertEquals(NUM_OF_EXISTING_STORAGE_SERVER_CONNECTION_EXTENSIONS, results.size());
    }

    private void fillWithRandomData(StorageServerConnectionExtension ssce) {
        ssce.setHostId(Guid.newGuid());
        ssce.setIqn(RandomUtils.instance().nextXmlString(10));
        ssce.setUserName(RandomUtils.instance().nextXmlString(10));
        ssce.setPassword(RandomUtils.instance().nextXmlString(10));
    }
}

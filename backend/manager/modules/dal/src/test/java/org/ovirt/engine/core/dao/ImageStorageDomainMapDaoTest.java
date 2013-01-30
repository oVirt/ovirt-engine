package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.image_storage_domain_map;
import org.ovirt.engine.core.common.businessentities.ImageStorageDomainMapId;
import org.ovirt.engine.core.compat.Guid;

public class ImageStorageDomainMapDaoTest extends BaseDAOTestCase {

    private static final Guid EXISTING_DOMAIN_ID = new Guid("72e3a666-89e1-4005-a7ca-f7548004a9ab");
    private static final Guid EXISTING_IMAGE_ID = new Guid("c9a559d9-8666-40d1-9967-759502b19f0b");
    private static final Guid EXISTING_IMAGE_ID_WITH_NO_MAP_ENTRY = new Guid("f9a559d9-8666-40d1-9967-759502b19f0f");
    private ImageStorageDomainMapDao dao;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        dao = dbFacade.getImageStorageDomainMapDao();
    }

    @Test
    public void testGetAllByImageId() {
        List<image_storage_domain_map> result =
                dao.getAllByImageId(EXISTING_IMAGE_ID);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (image_storage_domain_map mapping : result) {
            assertEquals(EXISTING_IMAGE_ID, mapping.getimage_id());
        }
    }

    @Test
    public void testGetAllByStorageDomainId() {
        List<image_storage_domain_map> result =
                dao.getAllByStorageDomainId(EXISTING_DOMAIN_ID);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (image_storage_domain_map mapping : result) {
            assertEquals(EXISTING_DOMAIN_ID, mapping.getstorage_domain_id());
        }
    }

    @Test
    public void testSave() {
        image_storage_domain_map entry =
                new image_storage_domain_map(EXISTING_IMAGE_ID_WITH_NO_MAP_ENTRY, EXISTING_DOMAIN_ID);
        dao.save(entry);
        List<image_storage_domain_map> entries = dao.getAllByImageId(EXISTING_IMAGE_ID_WITH_NO_MAP_ENTRY);
        assertNotNull(entries);
        assertTrue(entries.size() == 1);
        image_storage_domain_map entryFromDb = entries.get(0);
        assertEquals(entry, entryFromDb);
    }

    @Test
    public void testRemoveByImageId() {
        dao.remove(EXISTING_IMAGE_ID);
        List<image_storage_domain_map> entries = dao.getAllByStorageDomainId(EXISTING_IMAGE_ID);
        assertNotNull(entries);
        assertTrue(entries.isEmpty());
    }

    @Test
    public void testRemoveById() {
        dao.remove(new ImageStorageDomainMapId(EXISTING_IMAGE_ID, EXISTING_DOMAIN_ID));
        List<image_storage_domain_map> entries = dao.getAllByStorageDomainId(EXISTING_IMAGE_ID);
        for (image_storage_domain_map entry : entries) {
            assertFalse(entry.getstorage_domain_id().equals(EXISTING_DOMAIN_ID));
        }
        assertNotNull(entries);
        assertTrue(entries.isEmpty());
    }

}

package org.ovirt.engine.core.dao;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.storage.ImageStorageDomainMap;
import org.ovirt.engine.core.common.businessentities.storage.ImageStorageDomainMapId;
import org.ovirt.engine.core.compat.Guid;

public class ImageStorageDomainMapDaoTest extends BaseDaoTestCase {

    private static final Guid EXISTING_DOMAIN_ID = new Guid("72e3a666-89e1-4005-a7ca-f7548004a9ab");
    private static final Guid EXISTING_DISK_ID = new Guid("1b26a52b-b60f-44cb-9f46-3ef333b04a35");
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
        List<ImageStorageDomainMap> result =
                dao.getAllByImageId(EXISTING_IMAGE_ID);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (ImageStorageDomainMap mapping : result) {
            assertEquals(EXISTING_IMAGE_ID, mapping.getImageId());
        }
    }

    @Test
    public void testGetAllByStorageDomainId() {
        List<ImageStorageDomainMap> result =
                dao.getAllByStorageDomainId(EXISTING_DOMAIN_ID);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (ImageStorageDomainMap mapping : result) {
            assertEquals(EXISTING_DOMAIN_ID, mapping.getStorageDomainId());
        }
    }

    @Test
    public void testSave() {
        ImageStorageDomainMap entry =
                new ImageStorageDomainMap(EXISTING_IMAGE_ID_WITH_NO_MAP_ENTRY,
                        EXISTING_DOMAIN_ID,
                        FixturesTool.DEFAULT_QUOTA_GENERAL,
                        FixturesTool.DISK_PROFILE_1);
        dao.save(entry);
        List<ImageStorageDomainMap> entries = dao.getAllByImageId(EXISTING_IMAGE_ID_WITH_NO_MAP_ENTRY);
        assertNotNull(entries);
        assertTrue(entries.size() == 1);
        ImageStorageDomainMap entryFromDb = entries.get(0);
        assertEquals(entry, entryFromDb);
    }

    @Test
    public void testRemoveByImageId() {
        dao.remove(EXISTING_IMAGE_ID);
        List<ImageStorageDomainMap> entries = dao.getAllByStorageDomainId(EXISTING_IMAGE_ID);
        assertNotNull(entries);
        assertTrue(entries.isEmpty());
    }

    @Test
    public void testRemoveById() {
        dao.remove(new ImageStorageDomainMapId(EXISTING_IMAGE_ID, EXISTING_DOMAIN_ID));
        List<ImageStorageDomainMap> entries = dao.getAllByStorageDomainId(EXISTING_IMAGE_ID);
        for (ImageStorageDomainMap entry : entries) {
            assertFalse(entry.getStorageDomainId().equals(EXISTING_DOMAIN_ID));
        }
        assertNotNull(entries);
        assertTrue(entries.isEmpty());
    }

    @Test
    public void testChangeQuotaForDisk() {
        // fetch image
        ImageStorageDomainMap imageStorageDomainMap = dao.getAllByImageId(EXISTING_IMAGE_ID).get(0);
        Guid quotaId = imageStorageDomainMap.getQuotaId();
        // test that the current quota doesn't equal with the new quota
        if (quotaId.equals(FixturesTool.DEFAULT_QUOTA_GENERAL)) {
            fail("Same source and dest quota id, cannot perform test");
        }
        // change quota to the new quota 91
        dao.updateQuotaForImageAndSnapshots(EXISTING_DISK_ID, EXISTING_DOMAIN_ID, FixturesTool.DEFAULT_QUOTA_GENERAL);
        // fetch the image again
        imageStorageDomainMap = dao.getAllByImageId(EXISTING_IMAGE_ID).get(0);
        quotaId = imageStorageDomainMap.getQuotaId();
        // check that the new quota is the inserted one
        assertEquals("quota wasn't changed", quotaId, FixturesTool.DEFAULT_QUOTA_GENERAL);
    }

    @Test
    public void testChangeDiskProfileForDisk() {
        // fetch image
        ImageStorageDomainMap imageStorageDomainMap = dao.getAllByImageId(EXISTING_IMAGE_ID).get(0);
        // test that the current disk profile doesn't equal with the new disk profile
        assertThat("Same source and dest disk profile id, cannot perform test",
                imageStorageDomainMap.getDiskProfileId(), not(equalTo(FixturesTool.DISK_PROFILE_2)));
        // change to newDiskProfileId
        dao.updateDiskProfileByImageGroupIdAndStorageDomainId(EXISTING_DISK_ID, EXISTING_DOMAIN_ID, FixturesTool.DISK_PROFILE_2);
        // fetch the image again
        imageStorageDomainMap = dao.getAllByImageId(EXISTING_IMAGE_ID).get(0);
        // check that the new disk profile is the inserted one
        assertEquals("disk profile wasn't changed",
                imageStorageDomainMap.getDiskProfileId(),
                FixturesTool.DISK_PROFILE_2);
    }
}

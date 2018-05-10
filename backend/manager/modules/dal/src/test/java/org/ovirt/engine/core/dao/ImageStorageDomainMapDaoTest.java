package org.ovirt.engine.core.dao;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.storage.ImageStorageDomainMap;
import org.ovirt.engine.core.common.businessentities.storage.ImageStorageDomainMapId;
import org.ovirt.engine.core.compat.Guid;

public class ImageStorageDomainMapDaoTest extends BaseDaoTestCase<ImageStorageDomainMapDao> {

    private static final Guid EXISTING_IMAGE_ID = new Guid("c9a559d9-8666-40d1-9967-759502b19f0b");
    private static final Guid EXISTING_IMAGE_ID_WITH_NO_MAP_ENTRY = new Guid("f9a559d9-8666-40d1-9967-759502b19f0f");

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
                dao.getAllByStorageDomainId(FixturesTool.STORAGE_DOMAIN_SCALE_SD5);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (ImageStorageDomainMap mapping : result) {
            assertEquals(FixturesTool.STORAGE_DOMAIN_SCALE_SD5, mapping.getStorageDomainId());
        }
    }

    @Test
    public void testSave() {
        ImageStorageDomainMap entry =
                new ImageStorageDomainMap(EXISTING_IMAGE_ID_WITH_NO_MAP_ENTRY,
                        FixturesTool.STORAGE_DOMAIN_SCALE_SD5,
                        FixturesTool.DEFAULT_QUOTA_GENERAL,
                        FixturesTool.DISK_PROFILE_1);
        dao.save(entry);
        List<ImageStorageDomainMap> entries = dao.getAllByImageId(EXISTING_IMAGE_ID_WITH_NO_MAP_ENTRY);
        assertNotNull(entries);
        assertEquals(1, entries.size());
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
        dao.remove(new ImageStorageDomainMapId(EXISTING_IMAGE_ID, FixturesTool.STORAGE_DOMAIN_SCALE_SD5));
        List<ImageStorageDomainMap> entries = dao.getAllByStorageDomainId(EXISTING_IMAGE_ID);
        for (ImageStorageDomainMap entry : entries) {
            assertNotEquals(FixturesTool.STORAGE_DOMAIN_SCALE_SD5, entry.getStorageDomainId());
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
        assertNotEquals
                (FixturesTool.DEFAULT_QUOTA_GENERAL, quotaId, "Same source and dest quota id, cannot perform test");
        // change quota to the new quota 91
        dao.updateQuotaForImageAndSnapshots(FixturesTool.IMAGE_GROUP_ID, FixturesTool.STORAGE_DOMAIN_SCALE_SD5, FixturesTool.DEFAULT_QUOTA_GENERAL);
        // fetch the image again
        imageStorageDomainMap = dao.getAllByImageId(EXISTING_IMAGE_ID).get(0);
        quotaId = imageStorageDomainMap.getQuotaId();
        // check that the new quota is the inserted one
        assertEquals(FixturesTool.DEFAULT_QUOTA_GENERAL, quotaId, "quota wasn't changed");
    }

    @Test
    public void testChangeDiskProfileForDisk() {
        // fetch image
        ImageStorageDomainMap imageStorageDomainMap = dao.getAllByImageId(EXISTING_IMAGE_ID).get(0);
        // test that the current disk profile doesn't equal with the new disk profile
        assertThat("Same source and dest disk profile id, cannot perform test",
                imageStorageDomainMap.getDiskProfileId(), not(equalTo(FixturesTool.DISK_PROFILE_2)));
        // change to newDiskProfileId
        dao.updateDiskProfileByImageGroupIdAndStorageDomainId(FixturesTool.IMAGE_GROUP_ID,
                FixturesTool.STORAGE_DOMAIN_SCALE_SD5, FixturesTool.DISK_PROFILE_2);
        // fetch the image again
        imageStorageDomainMap = dao.getAllByImageId(EXISTING_IMAGE_ID).get(0);
        // check that the new disk profile is the inserted one
        assertEquals(FixturesTool.DISK_PROFILE_2, imageStorageDomainMap.getDiskProfileId(),
                "disk profile wasn't changed");
    }
}

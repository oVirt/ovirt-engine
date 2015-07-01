package org.ovirt.engine.core.dao.profiles;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.profiles.DiskProfile;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.BaseDaoTestCase;
import org.ovirt.engine.core.dao.FixturesTool;

public class DiskProfileDaoTest extends BaseDaoTestCase {

    private DiskProfile diskProfile;
    private DiskProfileDao dao;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        dao = dbFacade.getDiskProfileDao();
        diskProfile = new DiskProfile();
        diskProfile.setId(Guid.newGuid());
        diskProfile.setName("new_profile");
        diskProfile.setStorageDomainId(FixturesTool.STORAGE_DOAMIN_SCALE_SD5);
        diskProfile.setQosId(FixturesTool.QOS_ID_1);
    }

    /**
     * Ensures null is returned.
     */
    @Test
    public void testGetWithNonExistingId() {
        DiskProfile result = dao.get(Guid.newGuid());

        assertNull(result);
    }

    /**
     * Ensures that the interface profile is returned.
     */
    @Test
    public void testGet() {
        DiskProfile result = dao.get(FixturesTool.DISK_PROFILE_1);

        assertNotNull(result);
        assertEquals(FixturesTool.DISK_PROFILE_1, result.getId());
    }

    /**
     * Ensures that an empty collection is returned.
     */
    @Test
    public void testGetAllForStorageEmpty() {
        List<DiskProfile> result = dao.getAllForStorageDomain(Guid.newGuid());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Ensures that profiles are returned.
     */
    @Test
    public void testGetAllForStorageDomainFull() {
        checkResults(dao.getAllForStorageDomain(FixturesTool.STORAGE_DOAMIN_SCALE_SD5));
    }

    private void checkResults(List<DiskProfile> result) {
        assertNotNull(result);
        assertEquals(2, result.size());
        for (DiskProfile diskProfile : result) {
            assertEquals(FixturesTool.STORAGE_DOAMIN_SCALE_SD5, diskProfile.getStorageDomainId());
        }
    }

    @Test
    public void testGetAll() {
        List<DiskProfile> result = dao.getAll();

        assertNotNull(result);
        assertEquals(5, result.size());
    }

    /**
     * Ensures that the save is working correctly
     */
    @Test
    public void testSave() {
        assertNull(dao.get(diskProfile.getId()));
        dao.save(diskProfile);
        DiskProfile result = dao.get(diskProfile.getId());
        assertNotNull(result);
        assertEquals(diskProfile, result);
    }

    /**
     * Ensures that the update is working correctly
     */
    @Test
    public void testUpdate() {
        DiskProfile profile = dao.get(FixturesTool.DISK_PROFILE_1);
        assertNotNull(profile);
        assertFalse(FixturesTool.QOS_ID_2.equals(profile.getQosId()));
        profile.setQosId(FixturesTool.QOS_ID_2);
        profile.setDescription("Kramer goes to a fantasy camp? His whole life is a fantasy camp. "
                + "People should plunk down $2000 to live like him for a week. Sleep, do nothing, "
                + "fall ass-backwards into money, mooch food off your neighbors and have sex without dating... THAT'S a fantasy camp.");
        dao.update(profile);
        DiskProfile result = dao.get(profile.getId());
        assertNotNull(result);
        assertEquals(profile, result);
    }

    /**
     * Ensures that the remove is working correctly
     */
    @Test
    public void testRemove() {
        dao.save(diskProfile);
        DiskProfile result = dao.get(diskProfile.getId());
        assertNotNull(result);
        dao.remove(diskProfile.getId());
        assertNull(dao.get(diskProfile.getId()));
    }

    @Test
    public void nullifyQosForStorageDomain() {
        testAllQosValuesEqualToNull(false);
        dao.nullifyQosForStorageDomain(FixturesTool.STORAGE_DOAMIN_SCALE_SD5);
        testAllQosValuesEqualToNull(true);
    }

    private void testAllQosValuesEqualToNull(boolean isAllNull) {
        boolean allValues = true;
        List<DiskProfile> allForStorageDomain = dao.getAllForStorageDomain(FixturesTool.STORAGE_DOAMIN_SCALE_SD5);
        assertNotNull(allForStorageDomain);
        assertFalse(allForStorageDomain.isEmpty());
        for (DiskProfile diskProfile : allForStorageDomain) {
            allValues &= diskProfile.getQosId() == null;
        }
        assertEquals(isAllNull, allValues);
    }

    @Test
    public void testGetByQos() {
        List<DiskProfile> allForQos = dao.getAllForQos(FixturesTool.QOS_ID_1);
        assertNotNull(allForQos);
        assertEquals(2, allForQos.size());
        for (DiskProfile diskProfile : allForQos) {
            assertEquals(FixturesTool.QOS_ID_1, diskProfile.getQosId());
        }
    }

    @Test
    public void testGetFilteredByPermissions() {
        checkResults(dao.getAllForStorageDomain(FixturesTool.STORAGE_DOAMIN_SCALE_SD5, PRIVILEGED_USER_ID, true));
    }

    @Test
    public void testGetFilteredByPermissionsForUnprivilegedUser() {
        List<DiskProfile> result =
                dao.getAllForStorageDomain(FixturesTool.STORAGE_DOAMIN_SCALE_SD5, UNPRIVILEGED_USER_ID, true);
        assertTrue(result.isEmpty());
    }
}

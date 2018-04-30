package org.ovirt.engine.core.dao.profiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.profiles.DiskProfile;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.BaseGenericDaoTestCase;
import org.ovirt.engine.core.dao.FixturesTool;

public class DiskProfileDaoTest extends BaseGenericDaoTestCase<Guid, DiskProfile, DiskProfileDao> {
    @Override
    protected DiskProfile generateNewEntity() {
        DiskProfile diskProfile = new DiskProfile();
        diskProfile.setId(Guid.newGuid());
        diskProfile.setName("new_profile");
        diskProfile.setStorageDomainId(FixturesTool.STORAGE_DOMAIN_SCALE_SD5);
        diskProfile.setQosId(FixturesTool.QOS_ID_1);
        return diskProfile;
    }

    @Override
    protected void updateExistingEntity() {
        existingEntity.setQosId(FixturesTool.QOS_ID_2);
        existingEntity.setDescription("Kramer goes to a fantasy camp? His whole life is a fantasy camp. "
                + "People should plunk down $2000 to live like him for a week. Sleep, do nothing, "
                + "fall ass-backwards into money, mooch food off your neighbors and have sex without dating... THAT'S a fantasy camp.");
    }

    @Override
    protected Guid getExistingEntityId() {
        return FixturesTool.DISK_PROFILE_1;
    }

    @Override
    protected Guid generateNonExistingId() {
        return Guid.newGuid();
    }

    @Override
    protected int getEntitiesTotalCount() {
        return 5;
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
        checkResults(dao.getAllForStorageDomain(FixturesTool.STORAGE_DOMAIN_SCALE_SD5));
    }

    private void checkResults(List<DiskProfile> result) {
        assertNotNull(result);
        assertEquals(2, result.size());
        for (DiskProfile diskProfile : result) {
            assertEquals(FixturesTool.STORAGE_DOMAIN_SCALE_SD5, diskProfile.getStorageDomainId());
        }
    }

    @Test
    public void testGetAll() {
        List<DiskProfile> result = dao.getAll();

        assertNotNull(result);
        assertEquals(5, result.size());
    }

    @Test
    public void nullifyQosForStorageDomain() {
        testAllQosValuesEqualToNull(false);
        dao.nullifyQosForStorageDomain(FixturesTool.STORAGE_DOMAIN_SCALE_SD5);
        testAllQosValuesEqualToNull(true);
    }

    private void testAllQosValuesEqualToNull(boolean isAllNull) {
        List<DiskProfile> allForStorageDomain = dao.getAllForStorageDomain(FixturesTool.STORAGE_DOMAIN_SCALE_SD5);
        assertNotNull(allForStorageDomain);
        assertFalse(allForStorageDomain.isEmpty());
        assertEquals(isAllNull, allForStorageDomain.stream().allMatch(d -> d.getQosId() == null));
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
        checkResults(dao.getAllForStorageDomain(FixturesTool.STORAGE_DOMAIN_SCALE_SD5, PRIVILEGED_USER_ID, true));
    }

    @Test
    public void testGetFilteredByPermissionsForUnprivilegedUser() {
        List<DiskProfile> result =
                dao.getAllForStorageDomain(FixturesTool.STORAGE_DOMAIN_SCALE_SD5, UNPRIVILEGED_USER_ID, true);
        assertTrue(result.isEmpty());
    }
}

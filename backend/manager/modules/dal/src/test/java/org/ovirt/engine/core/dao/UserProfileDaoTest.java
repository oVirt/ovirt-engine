package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.UserProfile;
import org.ovirt.engine.core.compat.Guid;

public class UserProfileDaoTest extends BaseDaoTestCase {
    private UserProfileDao dao;
    private UserProfile existingProfile;
    private UserProfile deletableProfile;
    private UserProfile newProfile;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        dao = dbFacade.getUserProfileDao();

        existingProfile = dao.get(new Guid("38cb5663-96bc-485c-834a-cbbc03acc820"));
        deletableProfile = dao.get(new Guid("38cb5663-96bc-485c-834a-cbbc03acc821"));

        newProfile = new UserProfile();

        newProfile.setId(Guid.newGuid());
        newProfile.setUserId(new Guid("81940459-2ec4-4afa-bbaa-22549555293c"));
        newProfile.setSshPublicKeyId(new Guid("67bc5484-d4eb-47ce-8b6a-e9624b70b676"));
        newProfile.setSshPublicKey("key3");
        newProfile.setLoginName("userportal4@testportal.redhat.com@testportal.redhat.com");
        // FIXME: this user@domain@domain is ugly, but it is harmless for our test.
    }

    /**
     * Ensures that trying to get a user profile using an invalid id fails.
     */
    @Test
    public void testGetWithInvalidId() {
        UserProfile result = dao.get(Guid.newGuid());

        assertNull(result);
    }

    /**
     * Ensures that retrieving an user profile by id works as expected.
     */
    @Test
    public void testGet() {
        UserProfile result = dao.get(existingProfile.getId());

        assertNotNull(result);
        assertEquals(existingProfile, result);
    }

    /**
     * Ensures that retrieving an user profile by user id works as expected.
     */
    @Test
    public void testGetByUserId() {
        UserProfile result = dao.getByUserId(existingProfile.getUserId());

        assertNotNull(result);
        assertEquals(existingProfile, result);
    }

    @Test
    public void testGetAll() {
        List<UserProfile> result = dao.getAll();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(2, result.size());
    }

    /**
     * Ensures that saving a user profile works as expected.
     */
    @Test
    public void testSave() {
        dao.save(newProfile);

        UserProfile result = dao.get(newProfile.getId());

        assertEquals(newProfile, result);
    }

    /**
     * Ensures that updating a user profile works as expected.
     */
    @Test
    public void testUpdate() {
        existingProfile.setSshPublicKey("key4");

        dao.update(existingProfile);

        UserProfile result = dao.get(existingProfile.getId());

        assertEquals(existingProfile, result);
    }

    /**
     * Ensures that removing user profiles works as expected.
     */
    @Test
    public void testRemove() {
        dao.remove(deletableProfile.getId());

        UserProfile result = dao.get(deletableProfile.getId());

        assertNull(result);
    }
}

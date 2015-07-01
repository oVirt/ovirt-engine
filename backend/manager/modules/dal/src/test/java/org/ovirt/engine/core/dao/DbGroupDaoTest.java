package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.aaa.DbGroup;
import org.ovirt.engine.core.compat.Guid;

/**
 * Performs tests against the {@link DbGroupDao} type.
 */
public class DbGroupDaoTest extends BaseDaoTestCase {
    private static final int GROUP_COUNT = 10;
    private DbGroupDao dao;
    private DbGroup newGroup;
    private DbGroup existingGroup;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        dao = dbFacade.getDbGroupDao();

        // create some test data
        newGroup = new DbGroup();
        newGroup.setId(Guid.newGuid());
        newGroup.setExternalId("0");
        newGroup.setDomain("domain");
        newGroup.setName("name");
        newGroup.setNamespace("*");
        existingGroup = dao.get(new Guid("b399944a-81ab-4ec5-8266-e19ba7c3c9d1"));
    }

    /**
     * Ensures that if the id is invalid then no group is returned.
     */
    @Test
    public void testGetWithInvalidId() {
        DbGroup result = dao.get(Guid.newGuid());

        assertNull(result);
    }

    /**
     * Ensures that, if the id is valid, then retrieving a group works as expected.
     */
    @Test
    public void testGet() {
        DbGroup result = dao.get(existingGroup.getId());

        assertNotNull(result);
        assertEquals(existingGroup, result);
    }


    /**
     * Ensures that trying to get a group with an invalid external id fails.
     */
    @Test
    public void testGetWithInvalidExternalId() {
        DbGroup result = dao.getByExternalId("rhel", "0");
        assertNull(result);
    }

    /**
     * Ensures that retrieving an group by external id works as expected.
     */
    @Test
    public void testGetByExternalId() {
        DbGroup result = dao.getByExternalId("rhel", "a");
        assertNotNull(result);
    }

    /**
     * Ensures that update cycle doesn't change the external identifier.
     */
    @Test
    public void testUpdateDoesntChangeExternalId() {
        DbGroup groupBefore = dao.get(existingGroup.getId());
        dao.update(groupBefore);
        DbGroup groupAfter = dao.get(existingGroup.getId());
        assertEquals(groupBefore.getExternalId(), groupAfter.getExternalId());
    }

    /**
     * Ensures that, if the supplied name is invalid, then no group is returned.
     */
    @Test
    public void testGetByNameWithInvalidName() {
        DbGroup result = dao.getByName("thisnameisinvalid");

        assertNull(result);
    }

    /**
     * Ensures that finding by name works as expected.
     */
    @Test
    public void testGetByName() {
        DbGroup result = dao.getByName(existingGroup.getName());

        assertNotNull(result);
        assertEquals(existingGroup, result);
    }

    /**
     * Ensures that finding all groups works as expected.
     */
    @Test
    public void testGetAll() {
        List<DbGroup> result = dao.getAll();

        assertEquals(GROUP_COUNT, result.size());
    }

    /**
     * Ensures that saving a ad_group works as expected.
     */
    @Test
    public void testSave() {
        dao.save(newGroup);

        DbGroup result = dao.getByName(newGroup.getName());

        assertEquals(newGroup, result);
    }

    /**
     * Ensures that updating a ad_group works as expected.
     */
    @Test
    public void testUpdate() {
        existingGroup.setName(existingGroup.getName().toUpperCase());
        existingGroup.setDomain(existingGroup.getDomain().toUpperCase());
        dao.update(existingGroup);

        DbGroup result = dao.get(existingGroup.getId());

        assertNotNull(result);
        assertEquals(existingGroup, result);
    }


    /**
     * Ensures that inserting a group with no external id fails, as it has a
     * not null constraint.
     */
    @Test(expected = RuntimeException.class)
    public void testSaveGroupWithoutExternalIdFails() {
        newGroup.setExternalId(null);
        dao.save(newGroup);
    }

    /**
     * Ensures that inserting a group with the same external id and domain than
     * an existing group fails, as there is a unique constraint for that pair
     * of attributes.
     */
    @Test(expected = RuntimeException.class)
    public void testSaveGroupDuplicatedDomainAndExternalId() {
        newGroup.setDomain(existingGroup.getDomain());
        newGroup.setExternalId(existingGroup.getExternalId());
        dao.save(newGroup);
    }

    /**
     * Ensures that removing a ad_group works as expected.
     */
    @Test
    public void testRemove() {
        dao.remove(existingGroup.getId());

        DbGroup result = dao.get(existingGroup.getId());

        assertNull(result);
    }
}

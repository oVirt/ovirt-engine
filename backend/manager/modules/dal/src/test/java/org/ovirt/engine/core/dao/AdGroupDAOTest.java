package org.ovirt.engine.core.dao;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.ovirt.engine.core.common.businessentities.AdRefStatus;
import org.ovirt.engine.core.common.businessentities.LdapGroup;
import org.ovirt.engine.core.compat.Guid;

/**
 * <code>AdGroupDAOTest</code> performs tests against the {@link AdGroupDAO} type.
 *
 *
 */
public class AdGroupDAOTest extends BaseDAOTestCase {
    private static final int AD_GROUP_COUNT = 10;
    private AdGroupDAO dao;
    private LdapGroup newAdGroup;
    private LdapGroup existingAdGroup;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        dao = prepareDAO(dbFacade.getAdGroupDao());

        // create some test data
        newAdGroup = new LdapGroup();
        newAdGroup.setid(Guid.NewGuid());
        newAdGroup.setdomain("domain");
        newAdGroup.setname("name");
        newAdGroup.setstatus(AdRefStatus.Active);

        existingAdGroup = dao.get(new Guid("b399944a-81ab-4ec5-8266-e19ba7c3c9d1"));
    }

    /**
     * Ensures that if the id is invalid then no adGroup is returned.
     */
    @Test
    public void testGetWithInvalidId() {
        LdapGroup result = dao.get(Guid.NewGuid());

        assertNull(result);
    }

    /**
     * Ensures that, if the id is valid, then retrieving a adGroup works as expected.
     */
    @Test
    public void testGet() {
        LdapGroup result = dao.get(existingAdGroup.getid());

        assertNotNull(result);
        assertEquals(existingAdGroup, result);
    }

    /**
     * Ensures that, if the supplied name is invalid, then no adGroup is returned.
     */
    @Test
    public void testGetByNameWithInvalidName() {
        LdapGroup result = dao.getByName("thisnameisinvalid");

        assertNull(result);
    }

    /**
     * Ensures that finding by name works as expected.
     */
    @Test
    public void testGetByName() {
        LdapGroup result = dao.getByName(existingAdGroup.getname());

        assertNotNull(result);
        assertEquals(existingAdGroup, result);
    }

    /**
     * Ensures that finding all adGroups works as expected.
     */
    @Test
    public void testGetAll() {
        List<LdapGroup> result = dao.getAll();

        assertEquals(AD_GROUP_COUNT, result.size());
    }

    /**
     * Ensures that saving a ad_group works as expected.
     */
    @Test
    public void testSave() {
        dao.save(newAdGroup);

        LdapGroup result = dao.getByName(newAdGroup.getname());

        assertEquals(newAdGroup, result);
    }

    /**
     * Ensures that updating a ad_group works as expected.
     */
    @Test
    public void testUpdate() {
        existingAdGroup.setname(existingAdGroup.getname().toUpperCase());
        existingAdGroup.setdomain(existingAdGroup.getdomain().toUpperCase());
        existingAdGroup.setstatus(AdRefStatus.Inactive);

        dao.update(existingAdGroup);

        LdapGroup result = dao.get(existingAdGroup.getid());

        assertNotNull(result);
        assertEquals(existingAdGroup, result);
    }

    /**
     * Ensures that removing a ad_group works as expected.
     */
    @Test
    public void testRemove() {
        dao.remove(existingAdGroup.getid());

        LdapGroup result = dao.get(existingAdGroup.getid());

        assertNull(result);
    }
}

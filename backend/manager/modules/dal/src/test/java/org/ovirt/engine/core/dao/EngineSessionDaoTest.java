package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.EngineSession;
import org.ovirt.engine.core.compat.Guid;

/**
 * <code>EngineSessionDaoTest</code> performs tests against the {@link org.ovirt.engine.core.dao.EngineSessionDao} type.
 *
 *
 */
public class EngineSessionDaoTest extends BaseDaoTestCase {
    private EngineSessionDao dao;
    private EngineSession newEngineSession;
    private EngineSession existingEngineSession;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        dao = dbFacade.getEngineSessionDao();

        // create some test data
        newEngineSession = new EngineSession();
        newEngineSession.setEngineSessionId(Guid.newGuid().toString());
        newEngineSession.setUserId(Guid.newGuid());
        newEngineSession.setRoleIds(new HashSet<>(Arrays.asList(FixturesTool.EXISTING_GROUP_ID)));
        newEngineSession.setGroupIds(new HashSet<>(Arrays.asList(FixturesTool.EXISTING_GROUP_ID)));
        newEngineSession.setAuthzName("");
        newEngineSession.setUserName("");

        existingEngineSession = dao.getBySessionId(PRIVILEGED_USER_ENGINE_SESSION_ID);
    }

    /**
     * Ensures that if the id is invalid then no EngineSession is returned.
     */
    @Test
    public void testGetWithInvalidId() {
        EngineSession result = dao.getBySessionId(Guid.newGuid().toString());

        assertNull(result);
    }

    /**
     * Ensures that, if the id is valid, then retrieving a EngineSession works as expected.
     */
    @Test
    public void testGet() {
        EngineSession result = dao.get(existingEngineSession.getId());

        assertNotNull(result);
        assertEquals(existingEngineSession, result);
    }

    /**
     * Ensures that, if the id is valid, then retrieving a EngineSession works as expected.
     */
    @Test
    public void testGetBySessionId() {
        EngineSession result = dao.getBySessionId(existingEngineSession.getEngineSessionId());

        assertNotNull(result);
        assertEquals(existingEngineSession, result);
    }

    /**
     * Ensures that removing a session works as expected.
     */
    @Test
    public void testRemove() {
        EngineSession result = dao.get(existingEngineSession.getId());
        assertNotNull(result);

        assertEquals(dao.remove(existingEngineSession.getId()), 1);
        result = dao.get(existingEngineSession.getId());

        assertNull(result);
        assertEquals(dao.remove(existingEngineSession.getId()), 0);

    }

    /**
     * Ensures that removing all sessions works as expected.
     */
    @Test
    public void testRemoveAll() {
        EngineSession result = dao.get(existingEngineSession.getId());
        assertNotNull(result);

        assertEquals(dao.removeAll(), 2);
        result = dao.get(existingEngineSession.getId());

        assertNull(result);
        assertEquals(dao.removeAll(), 0);

    }

    @Test
    public void testSaveOrUpdate() {
        EngineSession sessionFromDb = dao.getBySessionId(newEngineSession.getEngineSessionId());
        assertNull(sessionFromDb);
        dao.save(newEngineSession);
        sessionFromDb = dao.getBySessionId(newEngineSession.getEngineSessionId());
        assertNotNull(sessionFromDb);
        assertEquals(sessionFromDb, newEngineSession);
    }

}

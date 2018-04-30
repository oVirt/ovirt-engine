package org.ovirt.engine.core.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Collections;
import java.util.HashSet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.EngineSession;
import org.ovirt.engine.core.compat.Guid;

/**
 * {@code EngineSessionDaoTest} performs tests against the {@link org.ovirt.engine.core.dao.EngineSessionDao} type.
 */
public class EngineSessionDaoTest extends BaseDaoTestCase<EngineSessionDao> {
    private EngineSession newEngineSession;
    private EngineSession existingEngineSession;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();

        // create some test data
        newEngineSession = new EngineSession();
        newEngineSession.setEngineSessionId(Guid.newGuid().toString());
        newEngineSession.setUserId(Guid.newGuid());
        newEngineSession.setRoleIds(new HashSet<>(Collections.singletonList(FixturesTool.EXISTING_GROUP_ID)));
        newEngineSession.setGroupIds(new HashSet<>(Collections.singletonList(FixturesTool.EXISTING_GROUP_ID)));
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

        assertEquals(1, dao.remove(existingEngineSession.getId()));
        result = dao.get(existingEngineSession.getId());

        assertNull(result);
        assertEquals(0, dao.remove(existingEngineSession.getId()));

    }

    /**
     * Ensures that removing all sessions works as expected.
     */
    @Test
    public void testRemoveAll() {
        EngineSession result = dao.get(existingEngineSession.getId());
        assertNotNull(result);

        assertEquals(2, dao.removeAll());
        result = dao.get(existingEngineSession.getId());

        assertNull(result);
        assertEquals(0, dao.removeAll());

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

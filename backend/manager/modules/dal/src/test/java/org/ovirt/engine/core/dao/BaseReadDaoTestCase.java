package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.Serializable;
import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.BusinessEntity;

public abstract class BaseReadDaoTestCase<ID extends Serializable, T extends BusinessEntity<ID>,
D extends ReadDao<T, ID>> extends BaseDaoTestCase {

    protected D dao;
    protected T existingEntity;

    public BaseReadDaoTestCase() {
        super();
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        dao = prepareDao();

        existingEntity = dao.get(getExistingEntityId());
    }

    protected abstract ID getExistingEntityId();

    protected abstract D prepareDao();

    /**
     * Ensures that fetching an entity with an invalid id fails.
     */
    @Test
    public void testGetByIdWithInvalidId() {
        T result = dao.get(generateNonExistingId());

        assertNull(result);
    }

    protected abstract ID generateNonExistingId();

    /**
     * Ensures that retrieving an entity by id works as expected.
     */
    @Test
    public void testGet() {
        T result = dao.get(existingEntity.getId());

        assertNotNull(result);
        assertEquals(existingEntity, result);
    }

    /**
     * Ensures that retrieving all entities works.
     */
    @Test
    public void testGetAll() {
        List<T> result = dao.getAll();

        assertFalse(result.isEmpty());
        assertEquals(getEneitiesTotalCount(), result.size());
    }

    protected abstract int getEneitiesTotalCount();

}

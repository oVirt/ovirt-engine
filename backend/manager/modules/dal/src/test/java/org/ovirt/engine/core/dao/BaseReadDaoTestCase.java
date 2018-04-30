package org.ovirt.engine.core.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.Serializable;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.BusinessEntity;

public abstract class BaseReadDaoTestCase<ID extends Serializable, T extends BusinessEntity<ID>,
D extends ReadDao<T, ID>> extends BaseDaoTestCase<D> {

    protected T existingEntity;

    public BaseReadDaoTestCase() {
        super();
    }

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        existingEntity = dao.get(getExistingEntityId());
    }

    protected abstract ID getExistingEntityId();

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
        assertEquals(getEntitiesTotalCount(), result.size());
    }

    protected abstract int getEntitiesTotalCount();

}

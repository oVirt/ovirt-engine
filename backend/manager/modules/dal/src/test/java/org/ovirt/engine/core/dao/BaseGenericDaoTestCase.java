package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.Serializable;
import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.compat.Guid;

public abstract class BaseGenericDaoTestCase<ID extends Serializable, T extends BusinessEntity<ID>, D extends GenericDao<T, ID>> extends BaseDAOTestCase {

    protected static final Guid PRIVILEGED_USER_ID   = new Guid("9bf7c640-b620-456f-a550-0348f366544b");
    protected static final Guid UNPRIVILEGED_USER_ID = new Guid("9bf7c640-b620-456f-a550-0348f366544a");

    protected D dao;

    protected T existingEntity;

    public BaseGenericDaoTestCase() {
        super();
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        dao = prepareDao();

        existingEntity = dao.get(getExistingEntityId());
    }


    abstract protected ID getExistingEntityId();


    abstract protected D prepareDao();

    /**
     * Ensures that fetching a disk image with an invalid id fails.
     */
    @Test
    public void testGetByIdWithInvalidId() {
        T result = dao.get(generateNonExistingId());

        assertNull(result);
    }

    abstract protected ID generateNonExistingId();

    /**
     * Ensures that retrieving a disk image by name works as expected.
     */
    @Test
    public void testGet() {
        T result = dao.get(existingEntity.getId());

        assertNotNull(result);
        assertEquals(existingEntity, result);
    }

    /**
     * Ensures that retrieving all disk images works.
     */
    @Test
    public void testGetAll() {
        List<T> result = dao.getAll();

        assertFalse(result.isEmpty());
        assertEquals(getEneitiesTotalCount(), result.size());
    }

    abstract protected int getEneitiesTotalCount();

    /**
     * Ensures that saving a disk image works as expected.
     */
    @Test
    public void testSave() {
        T newEntity = generateNewEntity();
        dao.save(newEntity);
        T result = dao.get(newEntity.getId());

        assertNotNull(result);
        assertEquals(newEntity, result);
    }

    abstract protected T generateNewEntity();

    /**
    * Ensures that updating a disk image works as expected.
    */
    @Test
    public void testUpdate() {
        updateExistingEntity();

        dao.update(existingEntity);

        T result = dao.get(existingEntity.getId());

        assertNotNull(result);
        assertEquals(existingEntity, result);
    }

    abstract protected void updateExistingEntity();

    /**
    * Ensures that removing a disk image works as expected.
    */
    @Test
    public void testRemove() {
        dao.remove(existingEntity.getId());

        T result = dao.get(existingEntity.getId());

        assertNull(result);
    }

}

package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.Serializable;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.BusinessEntity;

public abstract class BaseGenericDaoTestCase<ID extends Serializable, T extends BusinessEntity<ID>,
D extends GenericDao<T, ID>> extends BaseReadDaoTestCase<ID, T, D> {
    public BaseGenericDaoTestCase() {
        super();
    }

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

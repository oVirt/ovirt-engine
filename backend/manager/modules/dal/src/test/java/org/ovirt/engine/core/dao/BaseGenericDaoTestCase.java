package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.Serializable;

import org.dbunit.dataset.IDataSet;
import org.dbunit.operation.DatabaseOperation;
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

    protected abstract T generateNewEntity();

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

    protected abstract void updateExistingEntity();

    /**
    * Ensures that removing a disk image works as expected.
    */
    @Test
    public void testRemove() {
        dao.remove(existingEntity.getId());

        T result = dao.get(existingEntity.getId());

        assertNull(result);
    }

    protected void reinitializeDatabase() {
        try {
            final IDataSet dataset = initDataSet();
            DatabaseOperation.CLEAN_INSERT.execute(getConnection(), dataset);
        } catch (Exception ex) {
            throw new RuntimeException("Database reinitialization failed", ex);
        }
    }

}

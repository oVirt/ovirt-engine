package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.Serializable;
import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.springframework.test.context.transaction.TransactionConfiguration;

@TransactionConfiguration(transactionManager = "transactionManagerJPA", defaultRollback = true)
public abstract class BaseHibernateDaoTestCase<T extends GenericDao<E, ID>, E extends BusinessEntity<ID>, ID extends Serializable>
        extends BaseDAOTestCase {

    protected abstract T getDao();

    protected abstract E getExistingEntity();

    protected abstract E getNonExistentEntity();

    protected abstract int getAllEntitiesCount();

    protected abstract E modifyEntity(E entity);

    protected abstract void verifyEntityModification(E result);

    /**
     * Ensures that, if the id is valid, then retrieving a bookmark works as expected.
     */
    @Test
    public void testGet() {
        E result = getDao().get(getExistingEntity().getId());

        assertNotNull(result);
        assertEquals(getExistingEntity(), result);
    }

    /**
     * Ensures that, if an entity not found upon the given id, <code>null</code> is returned.
     */
    @Test
    public void testGetNotFound() {
        E result = getDao().get(getNonExistentEntity().getId());

        assertNull(result);
    }

    /**
     * Ensures that finding all entities are loaded as expected.
     */
    @Test
    public void testGetAll() {
        List<E> result = getDao().getAll();

        assertEquals(getAllEntitiesCount(), result.size());
    }

    /**
     * Ensures that saving an entities works as expected.
     */
    @Test
    public void testSave() {
        getDao().save(getNonExistentEntity());

        E result = getDao().get(getNonExistentEntity().getId());

        assertNotNull(result);
        assertEquals(getNonExistentEntity(), result);
    }

    /**
     * Ensures that removing an entity works as expected.
     */
    @Test
    public void testRemove() {
        final ID existingId = getExistingEntity().getId();
        getDao().remove(existingId);

        E result = getDao().get(existingId);

        assertNull(result);
    }

    /**
     * Ensures that updating an existing entity works as expected.
     */
    @Test
    public void testUpdate() {
        final E modifiedEntity = modifyEntity(getExistingEntity());

        getDao().update(modifiedEntity);

        E result = getDao().get(getExistingEntity().getId());

        assertNotNull(result);
        verifyEntityModification(result);
    }
}

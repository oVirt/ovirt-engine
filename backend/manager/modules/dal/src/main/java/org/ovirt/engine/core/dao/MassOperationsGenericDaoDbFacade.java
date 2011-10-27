package org.ovirt.engine.core.dao;

import java.io.Serializable;
import java.util.Collection;

import org.ovirt.engine.core.common.businessentities.BusinessEntity;

/**
 * Implementation for the {@link MassOperationsDao} which provides a default
 * {@link MassOperationsDao#updateAll(Collection)} method that uses the
 * {@link DefaultGenericDaoDbFacade#update(SimpleJdbcCallOperations, BusinessEntity)} method in a more smart way,
 * reusing the {@link SimpleJdbcCallOperations}.
 *
 * @param <T>
 *            The type of entity.
 * @param <ID>
 *            The type of the entity's id.
 */
public abstract class MassOperationsGenericDaoDbFacade<T extends BusinessEntity<ID>, ID extends Serializable>
        extends DefaultGenericDaoDbFacade<T, ID> implements MassOperationsDao<T> {

    @Override
    public void updateAll(Collection<T> entities) {
        for (T entity : entities) {
            getCallsHandler().executeModification(getProcedureNameForUpdate(),
                    createFullParametersMapper(entity));
        }
    }
}

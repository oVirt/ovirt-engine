package org.ovirt.engine.core.dao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.dal.dbbroker.EntityToMapSqlParameterMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

/**
 * Implementation for the {@link MassOperationsDao} which provides a default
 * {@link MassOperationsDao#updateAll(Collection)} method that uses the {@link DefaultGenericDaoDbFacade#update} method
 * in a more smart way, reusing the {@link org.springframework.jdbc.core.simple.SimpleJdbcCallOperations}.
 *
 * @param <T>
 *            The type of entity.
 * @param <ID>
 *            The type of the entity's id.
 */
public abstract class MassOperationsGenericDaoDbFacade<T extends BusinessEntity<ID>, ID extends Serializable>
        extends DefaultGenericDaoDbFacade<T, ID> implements MassOperationsDao<T, ID> {

    public MassOperationsGenericDaoDbFacade(String entityStoredProcedureName) {
        super(entityStoredProcedureName);
    }

    @Override
    public void updateAll(Collection<T> entities) {
        updateAll(getProcedureNameForUpdate(),entities);
    }

    @Override
    /**
     * Enables to send update procedure name as a parameter that overrides the default
     * one.
     * In case this parameter is null the default procedure is used.
     */
    public void updateAll(String procedureName, Collection<T> entities) {
        for (T entity : entities) {
            update(entity, procedureName == null ? getProcedureNameForUpdate() : procedureName);
        }
    }

    @Override
    /**
     * Enables to send update procedure name as a parameter that overrides the default
     * one.
     * In case this parameter is null the default procedure is used.
     */
    public void updateAllInBatch(String procedureName,
            Collection<T> paramValues,
            EntityToMapSqlParameterMapper<T> mapper) {
        List<MapSqlParameterSource> sqlParams = new ArrayList<>();

        for (T param : paramValues) {
            sqlParams.add(mapper.map(param));
        }

        getCallsHandler().executeStoredProcAsBatch(procedureName == null ? getProcedureNameForUpdate() : procedureName,
                sqlParams);
    }

    @Override
    public void removeAll(Collection<ID> ids) {
        for (ID id : ids) {
            remove(id);
        }
    }
}

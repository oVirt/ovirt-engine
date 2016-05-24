package org.ovirt.engine.core.dao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.comparators.BusinessEntityComparator;
import org.ovirt.engine.core.dal.dbbroker.MapSqlParameterMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

/**
 * Implementation for the {@link MassOperationsDao} which provides a default
 * {@link MassOperationsDao#updateAll(Collection)} method that uses the {@link DefaultGenericDao#update} method
 * in a more smart way, reusing the {@link org.springframework.jdbc.core.simple.SimpleJdbcCallOperations}.
 *
 * @param <T>
 *            The type of entity.
 * @param <ID>
 *            The type of the entity's id.
 */
public abstract class MassOperationsGenericDao<T extends BusinessEntity<ID>, ID extends Serializable & Comparable<ID>>
        extends DefaultGenericDao<T, ID> implements MassOperationsDao<T, ID> {

    public MassOperationsGenericDao(String entityStoredProcedureName) {
        super(entityStoredProcedureName);
    }

    @Override
    public void updateAll(Collection<T> entities) {
        updateAll(getProcedureNameForUpdate(), entities);
    }

    @Override
    /**
     * Enables to send update procedure name as a parameter that overrides the default
     * one. In case this parameter is null the default procedure is used.
     */
    public void updateAll(String procedureName, Collection<T> entities) {
        if (procedureName == null) {
            procedureName = getProcedureNameForUpdate();
        }
        for (T entity : entities) {
            update(entity, procedureName);
        }
    }

    /**
     * Enables to send update procedure name as a parameter that overrides the default one. In case this parameter is
     * null the default procedure is used.
     */
    protected void updateAllInBatch(String procedureName,
            Collection<T> paramValues,
            MapSqlParameterMapper<T> mapper) {

        // To overcome possible deadlocks, we need to sort the collection
        List<T> sortedParamValues = new ArrayList<>(paramValues);
        Collections.sort(sortedParamValues, BusinessEntityComparator.newInstance());
        getCallsHandler().executeStoredProcAsBatch(procedureName == null ? getProcedureNameForUpdate() : procedureName,
                sortedParamValues, mapper);
    }

    @Override
    public void removeAll(Collection<ID> ids) {
        for (ID id : ids) {
            remove(id);
        }
    }


    /**
     * Enables to send remove procedure name as a parameter that overrides the default one. In case this parameter is
     * null, default procedure is used.
     */
    protected void removeAllInBatch(String procedureName, Collection<T> paramValues, MapSqlParameterMapper<T> mapper) {
        getCallsHandler().executeStoredProcAsBatch(procedureName == null ? getProcedureNameForRemove() : procedureName,
                paramValues,
                mapper);
    }

    @Override
    public void removeAllInBatch(Collection<T> entities) {
        if (entities.isEmpty()) {
            return;
        }
        // TODO: batch remove all should probably use only IDs (and id parameter source) as its non-batch counterpart
        removeAllInBatch(getProcedureNameForRemove(), entities, getBatchMapper());
    }

    @Override
    public void saveAll(Collection<T> entities) {
        for (T entity : entities) {
            save(entity);
        }
    }

    @Override
    public void saveAllInBatch(Collection<T> entities) {
        if (entities.isEmpty()) {
            return;
        }
        getCallsHandler().executeStoredProcAsBatch(getProcedureNameForSave(), entities, getBatchMapper());
    }

    @Override
    public void updateAllInBatch(Collection<T> entities) {
        if (entities.isEmpty()) {
            return;
        }
        updateAllInBatch(getProcedureNameForUpdate(), entities, getBatchMapper());
    }

    public MapSqlParameterMapper<T> getBatchMapper() {
        return new MapSqlParameterMapper<T>() {
            @Override
            public MapSqlParameterSource map(T entity) {
                return createFullParametersMapper(entity);
            }
        };
    }
}

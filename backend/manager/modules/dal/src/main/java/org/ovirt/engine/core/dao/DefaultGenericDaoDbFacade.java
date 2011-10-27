package org.ovirt.engine.core.dao;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.dal.dbbroker.DbFacadeUtils;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcCallOperations;

/**
 * Implementation for the {@link GenericDao} which provides a default implementation for all the methods which only
 * requires extending classes to provide procedure names and relevant mapper classes.
 *
 * @param <T>
 *            The type of entity.
 * @param <ID>
 *            The type of the entity's id.
 */
public abstract class DefaultGenericDaoDbFacade<T extends BusinessEntity<ID>, ID extends Serializable>
        extends BaseDAODbFacade implements GenericDao<T, ID> {

    public DefaultGenericDaoDbFacade() {
        super();
    }

    /**
     * Update the entity in the DB using the given {@link SimpleJdbcCallOperations}.
     *
     * @param entity
     *            The image dynamic data.
     */

    public void update(T entity) {
        modify(getProcedureNameForUpdate(), createFullParametersMapper(entity));
    }

    /**
     * @return The name of the procedure used to update the entity.
     */
    protected abstract String getProcedureNameForUpdate();

    protected void update(SimpleJdbcCallOperations callToUpdate, T entity) {
        callToUpdate.execute(createFullParametersMapper(entity));
    }

    @SuppressWarnings("unchecked")
    @Override
    public T get(ID id) {
        Map<String, Object> dbResults = createQueryCall(getProcedureNameForGet())
                .returningResultSet("RETURN_VALUE", createEntityRowMapper())
                .execute(createIdParameterMapper(id));

        return (T) DbFacadeUtils
                .asSingleResult((List<?>) (dbResults.get("RETURN_VALUE")));
    }

    /**
     * @return The name of the procedure used to fetch the entity.
     */
    protected abstract String getProcedureNameForGet();

    @SuppressWarnings("unchecked")
    @Override
    public List<T> getAll() {
        Map<String, Object> dbResults = createQueryCall(getProcedureNameForGetAll())
                .returningResultSet("RETURN_VALUE", createEntityRowMapper())
                .execute(getCustomMapSqlParameterSource());

        return (List<T>) dbResults.get("RETURN_VALUE");
    }

    /**
     * @return The name of the procedure used to fetch all the entities.
     */
    protected abstract String getProcedureNameForGetAll();

    protected void modify(String procedureName, MapSqlParameterSource paramSource) {
        getCallsHandler().executeModification(procedureName, paramSource);
    }

    @Override
    public void save(T entity) {
        modify(getProcedureNameForSave(), createFullParametersMapper(entity));
    }

    /**
     * @return The name of the procedure used to save the entity.
     */
    protected abstract String getProcedureNameForSave();

    @Override
    public void remove(ID id) {
        modify(getProcedureNameForRemove(), createIdParameterMapper(id));
    }

    /**
     * @return The name of the procedure used to remove the entity.
     */
    protected abstract String getProcedureNameForRemove();

    /**
     * Create a parameter mapper to map the entity id to the id in the procedure parameters.
     *
     * @param id
     *            The entity to map id for.
     * @return The mapper for the id from the entity.
     */
    protected abstract MapSqlParameterSource createIdParameterMapper(ID id);

    /**
     * Create a parameter mapper to map all entity fields to procedure parameters.
     *
     * @param entity
     *            The entity to map parameters for.
     * @return The mapper for the parameters from the entity.
     */
    protected abstract MapSqlParameterSource createFullParametersMapper(T entity);

    /**
     * Create a row mapper to map results to the entity.
     *
     * @return A row mapper which can map results to an entity.
     */
    protected abstract ParameterizedRowMapper<T> createEntityRowMapper();

    /**
     * Create a {@link SimpleJdbcCallOperations} used to call the given query procedure.<br>
     * <b>Warning:</b> This call should be used only for fetching entities.
     *
     * @param procedureName
     *            The name of the stored procedure to call.
     * @return The {@link SimpleJdbcCallOperations} which can be used to call the procedure.
     */
    protected SimpleJdbcCallOperations createQueryCall(String procedureName) {
        return dialect.createJdbcCallForQuery(jdbcTemplate).withProcedureName(procedureName);
    }

}

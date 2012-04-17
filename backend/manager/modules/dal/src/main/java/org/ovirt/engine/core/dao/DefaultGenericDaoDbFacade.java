package org.ovirt.engine.core.dao;

import java.io.Serializable;
import java.text.MessageFormat;
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

    private static final String DEFAULT_GET_PROCEDURE_FORMAT = "Get{0}By{0}Id";

    private static final String DEFAULT_GET_ALL_PROCEDURE_FORMAT = "GetAllFrom{0}s";

    private static final String DEFAULT_SAVE_PROCEDURE_FORMAT = "Insert{0}";

    private static final String DEFAULT_UPDATE_PROCEDURE_FORMAT = "Update{0}";

    private static final String DEFAULT_REMOVE_PROCEDURE_FORMAT = "Delete{0}";

    /**
     * This is the name of the stored procedure used for {@link GenericDao#get(Serializable)}.
     */
    private String procedureNameForGet;

    /**
     * This is the name of the stored procedure used for {@link GenericDao#getAll()}.
     */
    private String procedureNameForGetAll;

    /**
     * This is the name of the stored procedure used for {@link GenericDao#save(BusinessEntity)}.
     */
    private String procedureNameForSave;

    /**
     * This is the name of the stored procedure used for {@link GenericDao#update(BusinessEntity)}.
     */
    private String procedureNameForUpdate;

    /**
     * This is the name of the stored procedure used for {@link GenericDao#remove(Serializable)}.
     */
    private String procedureNameForRemove;

    /**
     * Initialize default procedure names with the given entity name.<br>
     * The default procedure names are determined by the following formats:
     * <ul>
     * <li>For {@link GenericDao#get(Serializable)}: {@link DefaultGenericDaoDbFacade#DEFAULT_GET_PROCEDURE_FORMAT}</li>
     * <li>For {@link GenericDao#getAll()}: {@link DefaultGenericDaoDbFacade#DEFAULT_GET_ALL_PROCEDURE_FORMAT}</li>
     * <li>For {@link GenericDao#save(BusinessEntity)}: {@link DefaultGenericDaoDbFacade#DEFAULT_SAVE_PROCEDURE_FORMAT}</li>
     * <li>For {@link GenericDao#update(BusinessEntity)}:
     * {@link DefaultGenericDaoDbFacade#DEFAULT_UPDATE_PROCEDURE_FORMAT}</li>
     * <li>For {@link GenericDao#remove(Serializable)}:
     * {@link DefaultGenericDaoDbFacade#DEFAULT_REMOVE_PROCEDURE_FORMAT}</li>
     * </ul>
     *
     * If you wish to use a procedure name different than the default one, please set it accordingly.
     *
     * @param entityStoredProcedureName
     *            The name to use in the default procedure names templates.
     */
    public DefaultGenericDaoDbFacade(String entityStoredProcedureName) {
        procedureNameForGet = MessageFormat.format(DEFAULT_GET_PROCEDURE_FORMAT, entityStoredProcedureName);
        procedureNameForGetAll = MessageFormat.format(DEFAULT_GET_ALL_PROCEDURE_FORMAT, entityStoredProcedureName);
        procedureNameForSave = MessageFormat.format(DEFAULT_SAVE_PROCEDURE_FORMAT, entityStoredProcedureName);
        procedureNameForUpdate = MessageFormat.format(DEFAULT_UPDATE_PROCEDURE_FORMAT, entityStoredProcedureName);
        procedureNameForRemove = MessageFormat.format(DEFAULT_REMOVE_PROCEDURE_FORMAT, entityStoredProcedureName);
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

    protected final String getProcedureNameForUpdate() {
        return procedureNameForUpdate;
    }

    protected void setProcedureNameForUpdate(String procedureNameForUpdate) {
        this.procedureNameForUpdate = procedureNameForUpdate;
    }

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

    protected final String getProcedureNameForGet() {
        return procedureNameForGet;
    }

    protected void setProcedureNameForGet(String procedureNameForGet) {
        this.procedureNameForGet = procedureNameForGet;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<T> getAll() {
        Map<String, Object> dbResults = createQueryCall(getProcedureNameForGetAll())
                .returningResultSet("RETURN_VALUE", createEntityRowMapper())
                .execute(getCustomMapSqlParameterSource());

        return (List<T>) dbResults.get("RETURN_VALUE");
    }

    protected final String getProcedureNameForGetAll() {
        return procedureNameForGetAll;
    }

    protected void setProcedureNameForGetAll(String procedureNameForGetAll) {
        this.procedureNameForGetAll = procedureNameForGetAll;
    }

    protected void modify(String procedureName, MapSqlParameterSource paramSource) {
        getCallsHandler().executeModification(procedureName, paramSource);
    }

    @Override
    public void save(T entity) {
        modify(getProcedureNameForSave(), createFullParametersMapper(entity));
    }

    protected final String getProcedureNameForSave() {
        return procedureNameForSave;
    }

    protected void setProcedureNameForSave(String procedureNameForSave) {
        this.procedureNameForSave = procedureNameForSave;
    }

    @Override
    public void remove(ID id) {
        modify(getProcedureNameForRemove(), createIdParameterMapper(id));
    }

    protected final String getProcedureNameForRemove() {
        return procedureNameForRemove;
    }

    protected void setProcedureNameForRemove(String procedureNameForRemove) {
        this.procedureNameForRemove = procedureNameForRemove;
    }

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

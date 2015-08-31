package org.ovirt.engine.core.dao;

import java.io.Serializable;
import java.text.MessageFormat;

import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

/**
 * Implementation for the {@link GenericDao} which provides a default implementation for all the methods which only
 * requires extending classes to provide procedure names and relevant mapper classes.
 *
 * @param <T>
 *            The type of entity.
 * @param <ID>
 *            The type of the entity's id.
 */
public abstract class DefaultGenericDao<T extends BusinessEntity<ID>, ID extends Serializable>
        extends DefaultReadDao<T, ID> implements GenericDao<T, ID> {

    protected static final String DEFAULT_SAVE_PROCEDURE_FORMAT = "Insert{0}";

    protected static final String DEFAULT_UPDATE_PROCEDURE_FORMAT = "Update{0}";

    private static final String DEFAULT_REMOVE_PROCEDURE_FORMAT = "Delete{0}";

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
     * <li>For {@link GenericDao#get(Serializable)}: {@link DefaultGenericDao#DEFAULT_GET_PROCEDURE_FORMAT}</li>
     * <li>For {@link GenericDao#getAll()}: {@link DefaultGenericDao#DEFAULT_GET_ALL_PROCEDURE_FORMAT}</li>
     * <li>For {@link GenericDao#save(BusinessEntity)}: {@link DefaultGenericDao#DEFAULT_SAVE_PROCEDURE_FORMAT}</li>
     * <li>For {@link GenericDao#update(BusinessEntity)}:
     * {@link DefaultGenericDao#DEFAULT_UPDATE_PROCEDURE_FORMAT}</li>
     * <li>For {@link GenericDao#remove(Serializable)}:
     * {@link DefaultGenericDao#DEFAULT_REMOVE_PROCEDURE_FORMAT}</li>
     * </ul>
     *
     * If you wish to use a procedure name different than the default one, please set it accordingly.
     *
     * @param entityStoredProcedureName
     *            The name to use in the default procedure names templates.
     */
    public DefaultGenericDao(String entityStoredProcedureName) {
        super(entityStoredProcedureName);
        procedureNameForSave = MessageFormat.format(DEFAULT_SAVE_PROCEDURE_FORMAT, entityStoredProcedureName);
        procedureNameForUpdate = MessageFormat.format(DEFAULT_UPDATE_PROCEDURE_FORMAT, entityStoredProcedureName);
        procedureNameForRemove = MessageFormat.format(DEFAULT_REMOVE_PROCEDURE_FORMAT, entityStoredProcedureName);
    }

    protected final String getProcedureNameForUpdate() {
        return procedureNameForUpdate;
    }

    protected void setProcedureNameForUpdate(String procedureNameForUpdate) {
        this.procedureNameForUpdate = procedureNameForUpdate;
    }

    protected final String getProcedureNameForSave() {
        return procedureNameForSave;
    }

    protected void setProcedureNameForSave(String procedureNameForSave) {
        this.procedureNameForSave = procedureNameForSave;
    }

    protected final String getProcedureNameForRemove() {
        return procedureNameForRemove;
    }

    protected void setProcedureNameForRemove(String procedureNameForRemove) {
        this.procedureNameForRemove = procedureNameForRemove;
    }

    @Override
    public void save(T entity) {
        getCallsHandler().executeModification(getProcedureNameForSave(), createFullParametersMapper(entity));
    }

    @Override
    public void update(T entity) {
        update(entity, getProcedureNameForUpdate());
    }

    protected void update(T entity, String procedureName) {
        getCallsHandler().executeModification(procedureName, createFullParametersMapper(entity));
    }

    @Override
    public void remove(ID id) {
        getCallsHandler().executeModification(getProcedureNameForRemove(), createIdParameterMapper(id));
    }

    /**
     * Create a parameter mapper to map all entity fields to procedure parameters.
     *
     * @param entity
     *            The entity to map parameters for.
     * @return The mapper for the parameters from the entity.
     */
    protected abstract MapSqlParameterSource createFullParametersMapper(T entity);
}

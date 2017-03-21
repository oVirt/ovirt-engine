package org.ovirt.engine.core.dao;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

/**
 * Implementation for the {@link ReadDao} which provides a default implementation for all the methods which only
 * requires extending classes to provide procedure names and relevant mapper classes.
 *
 * @param <T>
 *            The type of entity.
 * @param <ID>
 *            The type of the entity's id.
 */
public abstract class DefaultReadDao<T extends BusinessEntity<ID>, ID extends Serializable>
        extends BaseDao implements ReadDao<T, ID> {

    protected static final String DEFAULT_GET_PROCEDURE_FORMAT = "Get{0}By{0}Id";
    protected static final String DEFAULT_GET_ALL_PROCEDURE_FORMAT = "GetAllFrom{0}s";
    /**
     * This is the name of the stored procedure used for {@link GenericDao#get(Serializable)}.
     */
    protected String procedureNameForGet;
    /**
     * This is the name of the stored procedure used for {@link GenericDao#getAll()}.
     */
    protected String procedureNameForGetAll;

    /**
     * Initialize default procedure names with the given entity name.<br>
     * The default procedure names are determined by the following formats:
     * <ul>
     * <li>For {@link GenericDao#get(Serializable)}: {@link DefaultGenericDao#DEFAULT_GET_PROCEDURE_FORMAT}</li>
     * <li>For {@link GenericDao#getAll()}: {@link DefaultGenericDao#DEFAULT_GET_ALL_PROCEDURE_FORMAT}</li>
     * </ul>
     *
     * If you wish to use a procedure name different than the default one, please set it accordingly.
     *
     * @param entityStoredProcedureName
     *            The name to use in the default procedure names templates.
     */
    public DefaultReadDao(String entityStoredProcedureName) {
        procedureNameForGet = MessageFormat.format(DEFAULT_GET_PROCEDURE_FORMAT, entityStoredProcedureName);
        procedureNameForGetAll = MessageFormat.format(DEFAULT_GET_ALL_PROCEDURE_FORMAT, entityStoredProcedureName);
    }

    protected final String getProcedureNameForGet() {
        return procedureNameForGet;
    }

    protected void setProcedureNameForGet(String procedureNameForGet) {
        this.procedureNameForGet = procedureNameForGet;
    }

    protected final String getProcedureNameForGetAll() {
        return procedureNameForGetAll;
    }

    protected void setProcedureNameForGetAll(String procedureNameForGetAll) {
        this.procedureNameForGetAll = procedureNameForGetAll;
    }

    @Override
    public T get(ID id) {
        if (id == null) {
            return null;
        } else {
            return getCallsHandler().executeRead(getProcedureNameForGet(),
                    createEntityRowMapper(),
                    createIdParameterMapper(id));
        }
    }

    @Override
    public List<T> getAll() {
        return getCallsHandler().executeReadList(getProcedureNameForGetAll(),
                createEntityRowMapper(),
                getCustomMapSqlParameterSource());
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
     * Create a row mapper to map results to the entity.
     *
     * @return A row mapper which can map results to an entity.
     */
    protected abstract RowMapper<T> createEntityRowMapper();
}

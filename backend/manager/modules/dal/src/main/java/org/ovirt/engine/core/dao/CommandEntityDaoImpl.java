package org.ovirt.engine.core.dao;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.businessentities.CommandAssociatedEntity;
import org.ovirt.engine.core.common.businessentities.CommandEntity;
import org.ovirt.engine.core.common.utils.PersistedCommandContext;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.CustomMapSqlParameterSource;
import org.ovirt.engine.core.dal.dbbroker.DbFacadeUtils;
import org.ovirt.engine.core.dal.dbbroker.MapSqlParameterMapper;
import org.ovirt.engine.core.utils.ReflectionUtils;
import org.ovirt.engine.core.utils.SerializationFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

@Named
@Singleton
public class CommandEntityDaoImpl extends DefaultGenericDao<CommandEntity, Guid> implements CommandEntityDao {

    private static final RowMapper<Guid> idRowMapper = (rs, rowNum) -> getGuidDefaultEmpty(rs, "id");

    private static final RowMapper<CommandAssociatedEntity> coCoCmdEntityRowMapper = (rs, rowNum) -> {
        CommandAssociatedEntity entity = new CommandAssociatedEntity(getGuid(rs, "command_id"),
                VdcObjectType.valueOf(rs.getString("entity_type")),
                getGuid(rs, "entity_id"));
        entity.setEntityType(VdcObjectType.valueOf(rs.getString("entity_type")));
        return entity;
    };

    private static RowMapper<CommandEntity> mapper = (resultSet, rowNum) -> {
        CommandEntity result = new CommandEntity();
        result.setEngineSessionSeqId(resultSet.getLong("engine_session_seq_id"));
        result.setUserId(Guid.createGuidFromString(resultSet.getString("user_id")));
        result.setId(Guid.createGuidFromString(resultSet.getString("command_id")));
        result.setCommandContext(SerializationFactory.getDeserializer().deserialize(
                resultSet.getString("command_context"), PersistedCommandContext.class));
        result.setCreatedAt(DbFacadeUtils.fromDate(resultSet.getTimestamp("created_at")));
        result.setCommandType(ActionType.forValue(resultSet.getInt("command_type")));
        result.setParentCommandId(Guid.createGuidFromString(resultSet.getString("parent_command_id")));
        result.setRootCommandId(Guid.createGuidFromString(resultSet.getString("root_command_id")));
        result.setCommandParameters(deserializeParameters(resultSet.getString("command_parameters"), resultSet.getString("command_params_class")));
        result.setReturnValue(deserializeReturnValue(resultSet.getString("return_value"), resultSet.getString("return_value_class")));
        result.setCommandStatus(getCommandStatus(resultSet.getString("status")));
        result.setExecuted(resultSet.getBoolean("executed"));
        result.setCallbackEnabled(resultSet.getBoolean("callback_enabled"));
        result.setCallbackNotified(resultSet.getBoolean("callback_notified"));
        result.setData(SerializationFactory.getDeserializer().deserialize(resultSet.getString("data"), HashMap.class));
        return result;
    };

    private MapSqlParameterMapper<CommandAssociatedEntity> cocoCmdEntityMapper = entity -> {
        CustomMapSqlParameterSource paramSource = getCustomMapSqlParameterSource();
        paramSource.addValue("command_id", entity.getCommandId()).
                addValue("entity_id", entity.getEntityId()).
                addValue("entity_type", entity.getEntityType().toString());
        return paramSource;

    };

    private static CommandStatus getCommandStatus(String statusStr) {
        CommandStatus status = CommandStatus.UNKNOWN;
        if (!StringUtils.isEmpty(statusStr)) {
            status = CommandStatus.valueOf(statusStr);
        }
        return status;
    }

    public CommandEntityDaoImpl() {
        super("CommandEntity");
        setProcedureNameForGetAll("GetAllFromCommandEntities");
    }

    @Override
    protected MapSqlParameterSource createFullParametersMapper(CommandEntity entity) {
        return getCustomMapSqlParameterSource().addValue("engine_session_seq_id", entity.getEngineSessionSeqId())
                .addValue("user_id", Guid.isNullOrEmpty(entity.getUserId()) ? Guid.Empty : entity.getUserId())
                .addValue("command_id", Guid.isNullOrEmpty(entity.getId()) ? Guid.Empty : entity.getId())
                .addValue("command_type", entity.getCommandType().getValue())
                .addValue("parent_command_id", entity.getParentCommandId())
                .addValue("root_command_id", Guid.isNullOrEmpty(entity.getRootCommandId()) ? Guid.Empty : entity.getRootCommandId())
                .addValue("command_context", SerializationFactory.getSerializer().serialize(entity.getCommandContext()))
                .addValue("command_parameters", serializeParameters(entity.getCommandParameters()))
                .addValue("command_params_class", entity.getCommandParameters() == null ? null : entity.getCommandParameters().getClass().getName())
                .addValue("created_at", entity.getCreatedAt())
                .addValue("status", entity.getCommandStatus().toString())
                .addValue("executed", entity.isExecuted())
                .addValue("callback_enabled", entity.isCallbackEnabled())
                .addValue("return_value", serializeReturnValue(entity.getReturnValue()))
                .addValue("return_value_class", entity.getReturnValue() == null ? null : entity.getReturnValue().getClass().getName())
                .addValue("data", SerializationFactory.getSerializer().serialize(entity.getData()));
    }

    private String serializeReturnValue(ActionReturnValue retVal) {
        return SerializationFactory.getSerializer().serialize(retVal);
    }

    private String serializeParameters(ActionParametersBase params) {
        return SerializationFactory.getSerializer().serialize(params);
    }

    @SuppressWarnings("unchecked")
    private static ActionReturnValue deserializeReturnValue(String payload, String className) {
        if (className == null) {
            return null;
        }
        Class<Serializable> retValueClass = (Class<Serializable>) ReflectionUtils.getClassFor(className);
        return (ActionReturnValue) SerializationFactory.getDeserializer().deserialize(payload,
                retValueClass);
    }

    @SuppressWarnings("unchecked")
    private static ActionParametersBase deserializeParameters(String payload, String className) {
        if (className == null) {
            return null;
        }
        Class<Serializable> actionParamsClass = (Class<Serializable>) ReflectionUtils.getClassFor(className);
        return (ActionParametersBase) SerializationFactory.getDeserializer().deserialize(payload,
                actionParamsClass);
    }

    @Override
    protected MapSqlParameterSource createIdParameterMapper(Guid id) {
        return getCustomMapSqlParameterSource().addValue("command_id", id);
    }

    @Override
    protected RowMapper<CommandEntity> createEntityRowMapper() {
        return mapper;
    }

    @Override
    public void saveOrUpdate(CommandEntity commandEntity) {
        MapSqlParameterSource parameterSource = createFullParametersMapper(commandEntity);
        getCallsHandler().executeModification("InsertOrUpdateCommandEntity", parameterSource);
    }

    @Override
    public void updateStatus(Guid id, CommandStatus status) {
        getCallsHandler().executeModification("UpdateCommandEntityStatus", createIdParameterMapper(id).addValue("status", status.toString()));
    }

    @Override
    public void updateNotified(Guid id) {
        getCallsHandler().executeModification("UpdateCommandEntityNotified", createIdParameterMapper(id).addValue("callback_notified", true));
    }

    @Override
    public void updateExecuted(Guid id) {
        getCallsHandler().executeModification("UpdateCommandEntityExecuted", createIdParameterMapper(id).addValue("executed", true));
    }

    @Override
    public void removeAllBeforeDate(Date cutoff) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("date", cutoff);

        getCallsHandler().executeModification("DeleteCommandEntitiesOlderThanDate", parameterSource);
    }

    @Override
    public List<CommandEntity> getCmdEntitiesByParentCmdId(Guid parentId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("root_command_id", parentId);
        return getCallsHandler().executeReadList("GetCommandEntitiesByParentCmdId",
                mapper,
                parameterSource);
    }

    @Override
    public List<Guid> getCommandIdsByEntity(Guid entityId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("entity_id", entityId);
        return getCallsHandler().executeReadList("GetCommandIdsByEntityId",
                idRowMapper,
                parameterSource);
    }

    public void insertCommandAssociatedEntities(Collection<CommandAssociatedEntity> cmdAssociatedEntities) {
        getCallsHandler().executeStoredProcAsBatch("InsertCommandAssociatedEntities",
                cmdAssociatedEntities,
                cocoCmdEntityMapper);
    }

    @Override
    public List<CommandAssociatedEntity> getAllCommandAssociatedEntities(Guid cmdId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("command_id", cmdId);
        return getCallsHandler().executeReadList("GetCommandAssociatedEntities",
                coCoCmdEntityRowMapper,
                parameterSource);
    }

}

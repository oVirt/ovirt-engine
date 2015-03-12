package org.ovirt.engine.core.dao;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.CommandEntity;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacadeUtils;
import org.ovirt.engine.core.utils.ReflectionUtils;
import org.ovirt.engine.core.utils.SerializationFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

public class CommandEntityDaoDbFacadeImpl extends DefaultGenericDaoDbFacade<CommandEntity, Guid> implements CommandEntityDao {

    private static RowMapper<CommandEntity> mapper = new RowMapper<CommandEntity>() {

        @Override
        public CommandEntity mapRow(ResultSet resultSet, int rowNum) throws SQLException {
            CommandEntity result = new CommandEntity();
            result.setUserId(Guid.createGuidFromString(resultSet.getString("user_id")));
            result.setId(Guid.createGuidFromString(resultSet.getString("command_id")));
            result.setJobId(Guid.createGuidFromString(resultSet.getString("job_id")));
            result.setStepId(Guid.createGuidFromString(resultSet.getString("step_id")));
            result.setCreatedAt(DbFacadeUtils.fromDate(resultSet.getTimestamp("created_at")));
            result.setCommandType(VdcActionType.forValue(resultSet.getInt("command_type")));
            result.setRootCommandId(Guid.createGuidFromString(resultSet.getString("root_command_id")));
            result.setCommandParameters(deserializeParameters(resultSet.getString("command_parameters"), resultSet.getString("command_params_class")));
            result.setReturnValue(deserializeReturnValue(resultSet.getString("return_value"), resultSet.getString("return_value_class")));
            result.setCommandStatus(getCommandStatus(resultSet.getString("status")));
            result.setExecuted(resultSet.getBoolean("executed"));
            result.setCallbackEnabled(resultSet.getBoolean("callback_enabled"));
            result.setCallbackNotified(resultSet.getBoolean("callback_notified"));
            return result;
        }
    };

    private static CommandStatus getCommandStatus(String statusStr) {
        CommandStatus status = CommandStatus.UNKNOWN;
        if (!StringUtils.isEmpty(statusStr)) {
            status = CommandStatus.valueOf(statusStr);
        }
        return status;
    }

    public CommandEntityDaoDbFacadeImpl() {
        super("CommandEntity");
        setProcedureNameForGetAll("GetAllFromCommandEntities");
    }

    @Override
    protected MapSqlParameterSource createFullParametersMapper(CommandEntity entity) {
        return getCustomMapSqlParameterSource().addValue("user_id", Guid.isNullOrEmpty(entity.getUserId()) ? Guid.Empty : entity.getUserId())
                .addValue("command_id", Guid.isNullOrEmpty(entity.getId()) ? Guid.Empty : entity.getId())
                .addValue("command_type", entity.getCommandType().getValue())
                .addValue("root_command_id", Guid.isNullOrEmpty(entity.getRootCommandId()) ? Guid.Empty : entity.getRootCommandId())
                .addValue("job_id", Guid.isNullOrEmpty(entity.getJobId()) ? Guid.Empty : entity.getJobId())
                .addValue("step_id", Guid.isNullOrEmpty(entity.getStepId()) ? Guid.Empty : entity.getStepId())
                .addValue("command_parameters", serializeParameters(entity.getCommandParameters()))
                .addValue("command_params_class", entity.getCommandParameters() == null ? null : entity.getCommandParameters().getClass().getName())
                .addValue("status", entity.getCommandStatus().toString())
                .addValue("executed", entity.isExecuted())
                .addValue("callback_enabled", entity.isCallbackEnabled())
                .addValue("return_value", serializeReturnValue(entity.getReturnValue()))
                .addValue("return_value_class", entity.getReturnValue() == null ? null : entity.getReturnValue().getClass().getName());
    }

    private String serializeReturnValue(VdcReturnValueBase retVal) {
        return SerializationFactory.getSerializer().serialize(retVal);
    }

    private String serializeParameters(VdcActionParametersBase params) {
        return SerializationFactory.getSerializer().serialize(params);
    }

    @SuppressWarnings("unchecked")
    private static VdcReturnValueBase deserializeReturnValue(String payload, String className) {
        if (className == null) {
            return null;
        }
        Class<Serializable> retValueClass = (Class<Serializable>) ReflectionUtils.getClassFor(className);
        return (VdcReturnValueBase) SerializationFactory.getDeserializer().deserialize(payload,
                retValueClass);
    }

    @SuppressWarnings("unchecked")
    private static VdcActionParametersBase deserializeParameters(String payload, String className) {
        if (className == null) {
            return null;
        }
        Class<Serializable> actionParamsClass = (Class<Serializable>) ReflectionUtils.getClassFor(className);
        return (VdcActionParametersBase) SerializationFactory.getDeserializer().deserialize(payload,
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

}

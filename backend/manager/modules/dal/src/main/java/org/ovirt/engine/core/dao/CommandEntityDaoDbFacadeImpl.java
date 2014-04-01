package org.ovirt.engine.core.dao;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
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
            result.setId(Guid.createGuidFromString(resultSet.getString("command_id")));
            result.setCreatedAt(DbFacadeUtils.fromDate(resultSet.getTimestamp("created_at")));
            result.setCommandType(VdcActionType.forValue(resultSet.getInt("command_type")));
            result.setRootCommandId(Guid.createGuidFromString(resultSet.getString("root_command_id")));
            result.setActionParameters(deserializeParameters(resultSet.getString("action_parameters"), resultSet.getString("action_parameters_class")));
            result.setCommandStatus(getCommandStatus(resultSet.getString("status")));
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
        return getCustomMapSqlParameterSource().addValue("command_id", entity.getId())
                .addValue("command_type", entity.getCommandType().getValue())
                .addValue("root_command_id", entity.getRootCommandId())
                .addValue("action_parameters", serializeParameters(entity.getActionParameters()))
                .addValue("action_parameters_class", entity.getActionParameters() == null ? null : entity.getActionParameters().getClass().getName())
                .addValue("status", entity.getCommandStatus().toString());
    }

    private String serializeParameters(VdcActionParametersBase params) {
        return SerializationFactory.getSerializer().serialize(params);
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
    public void removeAllBeforeDate(Date cutoff) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("date", cutoff);

        getCallsHandler().executeModification("DeleteCommandEntitiesOlderThanDate", parameterSource);
    }

}

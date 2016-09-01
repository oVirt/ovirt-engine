package org.ovirt.engine.core.dao;

import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.collections.KeyValue;
import org.apache.commons.collections.keyvalue.DefaultKeyValue;
import org.ovirt.engine.core.common.businessentities.BusinessEntitySnapshot;
import org.ovirt.engine.core.common.businessentities.BusinessEntitySnapshot.SnapshotType;
import org.ovirt.engine.core.compat.Guid;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

/**
 * JDBC-Template Dao for business entity snapshots
 */
@Named
@Singleton
public class BusinessEntitySnapshotDaoImpl extends BaseDao implements BusinessEntitySnapshotDao {
    public BusinessEntitySnapshotDaoImpl() {
    }

    @Override
    public List<BusinessEntitySnapshot> getAllForCommandId(Guid commandID) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("command_id", commandID);

        return getCallsHandler().executeReadList("get_entity_snapshot_by_command_id",
                (rs, rowNum) -> {
                    BusinessEntitySnapshot result = new BusinessEntitySnapshot();
                    result.setId(getGuidDefaultEmpty(rs, "id"));
                    result.setCommandId(getGuidDefaultEmpty(rs, "command_id"));
                    result.setCommandType(rs.getString("command_type"));
                    result.setEntityId(rs.getString("entity_id"));
                    result.setEntityType(rs.getString("entity_type"));
                    result.setEntitySnapshot(rs.getString("entity_snapshot"));
                    result.setSnapshotClass(rs.getString("snapshot_class"));
                    result.setSnapshotType(SnapshotType.values()[rs.getInt("snapshot_type")]);
                    result.setInsertionOrder(rs.getInt("insertion_order"));
                    return result;
                },
                parameterSource);
    }

    @Override
    public void removeAllForCommandId(Guid commandID) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("command_id", commandID);
        getCallsHandler().executeModification("delete_entity_snapshot_by_command_id", parameterSource);
    }

    @Override
    public void save(BusinessEntitySnapshot entitySnapshot) {
        Guid id = entitySnapshot.getId();
        if (Guid.isNullOrEmpty(id)) {
            id = Guid.newGuid();
            entitySnapshot.setId(id);
        }
        MapSqlParameterSource parameterSource =
                getCustomMapSqlParameterSource()
                        .addValue("id", entitySnapshot.getId())
                        .addValue("command_id", entitySnapshot.getCommandId())
                        .addValue("command_type", entitySnapshot.getCommandType())
                        .addValue("entity_id", entitySnapshot.getEntityId())
                        .addValue("entity_type", entitySnapshot.getEntityType())
                        .addValue("entity_snapshot", entitySnapshot.getEntitySnapshot())
                        .addValue("snapshot_class", entitySnapshot.getSnapshotClass())
                        .addValue("snapshot_type", entitySnapshot.getSnapshotType())
                        .addValue("insertion_order", entitySnapshot.getInsertionOrder());
        getCallsHandler().executeModification("insert_entity_snapshot", parameterSource);
    }

    @Override
    public List<KeyValue> getAllCommands() {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource();

        return getCallsHandler().executeReadList("get_all_commands",
                (rs, rowNum) -> {
                    DefaultKeyValue result = new DefaultKeyValue();
                    result.setKey(getGuidDefaultEmpty(rs, "command_id"));
                    result.setValue(rs.getString("command_type"));
                    return result;
                },
                parameterSource);
    }
}

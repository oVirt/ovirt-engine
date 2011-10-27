package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.HypervisorType;
import org.ovirt.engine.core.common.businessentities.MigrateOnErrorOptions;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VdsSelectionAlgorithm;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacadeUtils;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

/**
 * <code>VdsGroupDAODbFacadeImpl</code> provides an implementation of {@link VdsGroupDAO} that uses code previously
 * found in {@link org.ovirt.engine.core.dal.dbbroker.DbFacade}.
 *
 */
public class VdsGroupDAODbFacadeImpl extends BaseDAODbFacade implements VdsGroupDAO {

    private final class VdsGroupRawMapper implements ParameterizedRowMapper<VDSGroup> {
        @Override
        public VDSGroup mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
            VDSGroup entity = new VDSGroup();
            entity.setdescription(rs.getString("description"));
            entity.setname(rs.getString("name"));
            entity.setID(Guid.createGuidFromString(rs
                        .getString("vds_group_id")));
            entity.setcpu_name(rs.getString("cpu_name"));
            entity.setselection_algorithm(VdsSelectionAlgorithm.forValue(rs
                        .getInt("selection_algorithm")));
            entity.sethigh_utilization(rs.getInt("high_utilization"));
            entity.setlow_utilization(rs.getInt("low_utilization"));
            entity.setcpu_over_commit_duration_minutes(rs
                        .getInt("cpu_over_commit_duration_minutes"));
            entity.sethypervisor_type(HypervisorType.forValue(rs
                        .getInt("hypervisor_type")));
            entity.setstorage_pool_id(NGuid.createGuidFromString(rs
                        .getString("storage_pool_id")));
            entity.setmax_vds_memory_over_commit(rs
                        .getInt("max_vds_memory_over_commit"));
            entity.setTransparentHugepages(rs
                        .getBoolean("transparent_hugepages"));
            entity.setcompatibility_version(new Version(rs
                        .getString("compatibility_version")));
            entity.setMigrateOnError(MigrateOnErrorOptions.forValue(rs.getInt("migrate_on_error")));
            return entity;
        }
    }

    @Override
    public VDSGroup get(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                    .addValue("vds_group_id", id);

        return getCallsHandler().executeRead("GetVdsGroupByVdsGroupId", new VdsGroupRawMapper(), parameterSource);
    }

    @Override
    public VDSGroup getWithRunningVms(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vds_group_id", id);

        ParameterizedRowMapper<VDSGroup> mapper = new ParameterizedRowMapper<VDSGroup>() {
            @Override
            public VDSGroup mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
                VDSGroup entity = new VDSGroup();
                entity.setdescription(rs.getString("description"));
                entity.setname(rs.getString("name"));
                entity.setID(Guid.createGuidFromString(rs.getString("vds_group_id")));
                entity.setcpu_name(rs.getString("cpu_name"));
                entity.setselection_algorithm(VdsSelectionAlgorithm.forValue(rs.getInt("selection_algorithm")));
                entity.sethigh_utilization(rs.getInt("high_utilization"));
                entity.setlow_utilization(rs.getInt("low_utilization"));
                entity.setcpu_over_commit_duration_minutes(rs.getInt("cpu_over_commit_duration_minutes"));
                entity.sethypervisor_type(HypervisorType.forValue(rs.getInt("hypervisor_type")));
                entity.setstorage_pool_id(NGuid.createGuidFromString(rs.getString("storage_pool_id")));
                entity.setmax_vds_memory_over_commit(rs.getInt("max_vds_memory_over_commit"));
                entity.setTransparentHugepages(rs.getBoolean("transparent_hugepages"));
                entity.setcompatibility_version(new Version(rs.getString("compatibility_version")));
                entity.setMigrateOnError(MigrateOnErrorOptions.forValue(rs.getInt("migrate_on_error")));
                return entity;
            }
        };
        return getCallsHandler().executeRead("GetVdsGroupWithRunningVms", mapper, parameterSource);
    }

    @Override
    public VDSGroup getByName(String name) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vds_group_name", name);

        ParameterizedRowMapper<VDSGroup> mapper = new ParameterizedRowMapper<VDSGroup>() {
            @Override
            public VDSGroup mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
                VDSGroup entity = new VDSGroup();
                entity.setdescription(rs.getString("description"));
                entity.setname(rs.getString("name"));
                entity.setID(Guid.createGuidFromString(rs
                        .getString("vds_group_id")));
                entity.setcpu_name(rs.getString("cpu_name"));
                entity.setselection_algorithm(VdsSelectionAlgorithm.forValue(rs
                        .getInt("selection_algorithm")));
                entity.sethigh_utilization(rs.getInt("high_utilization"));
                entity.setlow_utilization(rs.getInt("low_utilization"));
                entity.setcpu_over_commit_duration_minutes(rs
                        .getInt("cpu_over_commit_duration_minutes"));
                entity.sethypervisor_type(HypervisorType.forValue(rs
                        .getInt("hypervisor_type")));
                entity.setstorage_pool_id(NGuid.createGuidFromString(rs
                        .getString("storage_pool_id")));
                entity.setmax_vds_memory_over_commit(rs
                        .getInt("max_vds_memory_over_commit"));
                entity.setTransparentHugepages(rs
                        .getBoolean("transparent_hugepages"));
                entity.setcompatibility_version(new Version(rs
                        .getString("compatibility_version")));
                entity.setMigrateOnError(MigrateOnErrorOptions.forValue(rs.getInt("migrate_on_error")));
                return entity;
            }
        };


        return (VDSGroup) DbFacadeUtils.asSingleResult(
                getCallsHandler().executeReadList("GetVdsGroupByVdsGroupName", mapper, parameterSource));
    }

    @Override
    public List<VDSGroup> getAllForStoragePool(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("storage_pool_id", id);

        ParameterizedRowMapper<VDSGroup> mapper = new ParameterizedRowMapper<VDSGroup>() {
            @Override
            public VDSGroup mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
                VDSGroup entity = new VDSGroup();
                entity.setdescription(rs.getString("description"));
                entity.setname(rs.getString("name"));
                entity.setID(Guid.createGuidFromString(rs
                        .getString("vds_group_id")));
                entity.setcpu_name(rs.getString("cpu_name"));
                entity.setselection_algorithm(VdsSelectionAlgorithm.forValue(rs
                        .getInt("selection_algorithm")));
                entity.sethigh_utilization(rs.getInt("high_utilization"));
                entity.setlow_utilization(rs.getInt("low_utilization"));
                entity.setcpu_over_commit_duration_minutes(rs
                        .getInt("cpu_over_commit_duration_minutes"));
                entity.sethypervisor_type(HypervisorType.forValue(rs
                        .getInt("hypervisor_type")));
                entity.setstorage_pool_id(NGuid.createGuidFromString(rs
                        .getString("storage_pool_id")));
                entity.setmax_vds_memory_over_commit(rs
                        .getInt("max_vds_memory_over_commit"));
                entity.setTransparentHugepages(rs
                        .getBoolean("transparent_hugepages"));
                entity.setcompatibility_version(new Version(rs
                        .getString("compatibility_version")));
                entity.setMigrateOnError(MigrateOnErrorOptions.forValue(rs.getInt("migrate_on_error")));
                return entity;
            }
        };

        return getCallsHandler().executeReadList("GetVdsGroupsByStoragePoolId", mapper, parameterSource);
    }

    @Override
    public List<VDSGroup> getAllWithQuery(String query) {
        ParameterizedRowMapper<VDSGroup> mapper = new ParameterizedRowMapper<VDSGroup>() {
            @Override
            public VDSGroup mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
                VDSGroup entity = new VDSGroup();
                entity.setdescription(rs.getString("description"));
                entity.setname(rs.getString("name"));
                entity.setID(Guid.createGuidFromString(rs
                        .getString("vds_group_id")));
                entity.setcpu_name(rs.getString("cpu_name"));
                entity.setselection_algorithm(VdsSelectionAlgorithm.forValue(rs
                        .getInt("selection_algorithm")));
                entity.sethigh_utilization(rs.getInt("high_utilization"));
                entity.setlow_utilization(rs.getInt("low_utilization"));
                entity.setcpu_over_commit_duration_minutes(rs
                        .getInt("cpu_over_commit_duration_minutes"));
                entity.sethypervisor_type(HypervisorType.forValue(rs
                        .getInt("hypervisor_type")));
                entity.setstorage_pool_id(NGuid.createGuidFromString(rs
                        .getString("storage_pool_id")));
                entity.setmax_vds_memory_over_commit(rs
                        .getInt("max_vds_memory_over_commit"));
                entity.setTransparentHugepages(rs
                        .getBoolean("transparent_hugepages"));
                entity.setcompatibility_version(new Version(rs
                        .getString("compatibility_version")));
                entity.setMigrateOnError(MigrateOnErrorOptions.forValue(rs.getInt("migrate_on_error")));
                return entity;
            }
        };

        return new SimpleJdbcTemplate(jdbcTemplate).query(query, mapper);
    }

    @Override
    public List<VDSGroup> getAll() {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource();

        ParameterizedRowMapper<VDSGroup> mapper = new ParameterizedRowMapper<VDSGroup>() {
            @Override
            public VDSGroup mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
                VDSGroup entity = new VDSGroup();
                entity.setdescription(rs.getString("description"));
                entity.setname(rs.getString("name"));
                entity.setID(Guid.createGuidFromString(rs
                        .getString("vds_group_id")));
                entity.setcpu_name(rs.getString("cpu_name"));
                entity.setselection_algorithm(VdsSelectionAlgorithm.forValue(rs
                        .getInt("selection_algorithm")));
                entity.sethigh_utilization(rs.getInt("high_utilization"));
                entity.setlow_utilization(rs.getInt("low_utilization"));
                entity.setcpu_over_commit_duration_minutes(rs
                        .getInt("cpu_over_commit_duration_minutes"));
                entity.sethypervisor_type(HypervisorType.forValue(rs
                        .getInt("hypervisor_type")));
                entity.setstorage_pool_id(NGuid.createGuidFromString(rs
                        .getString("storage_pool_id")));
                entity.setmax_vds_memory_over_commit(rs
                        .getInt("max_vds_memory_over_commit"));
                entity.setTransparentHugepages(rs
                        .getBoolean("transparent_hugepages"));
                entity.setcompatibility_version(new Version(rs
                        .getString("compatibility_version")));
                entity.setMigrateOnError(MigrateOnErrorOptions.forValue(rs.getInt("migrate_on_error")));
                return entity;
            }
        };

        return getCallsHandler().executeReadList("GetAllFromVdsGroups", mapper, parameterSource);
    }

    @Override
    public void save(VDSGroup group) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("description", group.getdescription())
                .addValue("name", group.getname())
                .addValue("cpu_name", group.getcpu_name())
                .addValue("selection_algorithm", group.getselection_algorithm())
                .addValue("high_utilization", group.gethigh_utilization())
                .addValue("low_utilization", group.getlow_utilization())
                .addValue("cpu_over_commit_duration_minutes",
                        group.getcpu_over_commit_duration_minutes())
                .addValue("hypervisor_type", group.gethypervisor_type())
                .addValue("storage_pool_id", group.getstorage_pool_id())
                .addValue("max_vds_memory_over_commit",
                        group.getmax_vds_memory_over_commit())
                .addValue("transparent_hugepages",
                        group.getTransparentHugepages())
                .addValue("compatibility_version",
                        group.getcompatibility_version())
                .addValue("vds_group_id", group.getID())
                .addValue("migrate_on_error", group.getMigrateOnError());

        Map<String, Object> dbResults = getCallsHandler().executeModification("InsertVdsGroups", parameterSource);

        group.setvds_group_id(new Guid(dbResults.get("vds_group_id").toString()));
    }

    @Override
    public void update(VDSGroup group) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("description", group.getdescription())
                .addValue("name", group.getname())
                .addValue("vds_group_id", group.getID())
                .addValue("cpu_name", group.getcpu_name())
                .addValue("selection_algorithm", group.getselection_algorithm())
                .addValue("high_utilization", group.gethigh_utilization())
                .addValue("low_utilization", group.getlow_utilization())
                .addValue("cpu_over_commit_duration_minutes",
                        group.getcpu_over_commit_duration_minutes())
                .addValue("hypervisor_type", group.gethypervisor_type())
                .addValue("storage_pool_id", group.getstorage_pool_id())
                .addValue("max_vds_memory_over_commit",
                        group.getmax_vds_memory_over_commit())
                .addValue("transparent_hugepages",
                        group.getTransparentHugepages())
                .addValue("compatibility_version",
                        group.getcompatibility_version())
                .addValue("migrate_on_error", group.getMigrateOnError());

        getCallsHandler().executeModification("UpdateVdsGroup", parameterSource);
    }

    @Override
    public void remove(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vds_group_id", id);

        getCallsHandler().executeModification("DeleteVdsGroup", parameterSource);
    }

    @Override
    public List<VDSGroup> getClustersWithPermittedAction(Guid userId, ActionGroup actionGroup) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("user_id", userId).addValue("action_group_id", actionGroup.getId());

        ParameterizedRowMapper<VDSGroup> mapper = new VdsGroupRawMapper();

        return getCallsHandler().executeReadList("fn_perms_get_vds_groups_with_permitted_action",
                mapper,
                parameterSource);
    }
}

package org.ovirt.engine.core.dao.gluster;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterHookEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterHookStage;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterHookStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServerHook;
import org.ovirt.engine.core.common.utils.EnumUtils;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.MassOperationsGenericDao;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

/**
 * Implementation of the DB Facade for Gluster Hooks.
 */
@Named
@Singleton
public class GlusterHooksDaoImpl extends MassOperationsGenericDao<GlusterHookEntity, Guid> implements
        GlusterHooksDao {

    private static final RowMapper<GlusterHookEntity> glusterHookRowMapper = new GlusterHookRowMapper();

    private static final RowMapper<GlusterServerHook> glusterServerHookRowMapper =
            new GlusterServerHookRowMapper();

    private static final RowMapper<String>  GlusterHookContentRowMapper = new GlusterHookContentRowMapper();

    public GlusterHooksDaoImpl() {
        super("GlusterHook");
        setProcedureNameForGet("GetGlusterHookById");
    }

    @Override
    public void save(GlusterHookEntity glusterHook) {
        getCallsHandler().executeModification("InsertGlusterHook", createFullParametersMapper(glusterHook));
        if (glusterHook.getServerHooks() != null) {
            for (GlusterServerHook serverHook : glusterHook.getServerHooks()) {
                saveGlusterServerHook(serverHook);
            }
        }
    }

    @Override
    public GlusterHookEntity getById(Guid id) {
        return getById(id, false);
    }

    @Override
    public GlusterHookEntity getById(Guid id, boolean eagerLoad) {
        GlusterHookEntity glusterHook = getCallsHandler().executeRead("GetGlusterHookById", glusterHookRowMapper,
                createIdParameterMapper(id)
                .addValue("includeContent", eagerLoad));
        if (eagerLoad) {
            List<GlusterServerHook> serverHooks = getCallsHandler().executeReadList("GetGlusterServerHooksById", glusterServerHookRowMapper,
                    createIdParameterMapper(id));
            if (serverHooks != null) {
                glusterHook.setServerHooks(serverHooks);
            }
        }
        return glusterHook;
    }

    @Override
    public GlusterHookEntity getGlusterHook(Guid clusterId, String glusterCommand, GlusterHookStage stage, String hookName) {
        GlusterHookEntity glusterHook = getCallsHandler().executeRead(
                "GetGlusterHook", glusterHookRowMapper,
                getCustomMapSqlParameterSource()
                        .addValue("cluster_id", clusterId)
                        .addValue("gluster_command", glusterCommand)
                        .addValue("stage", EnumUtils.nameOrNull(stage))
                        .addValue("name", hookName)
                        .addValue("includeContent", false));
        return glusterHook;
    }

    @Override
    public List<GlusterServerHook> getGlusterServerHooks(Guid hookId) {
        List<GlusterServerHook> serverHooks =
                getCallsHandler().executeReadList("GetGlusterServerHooksById", glusterServerHookRowMapper,
                        createIdParameterMapper(hookId));
        return serverHooks;
    }

    @Override
    public GlusterServerHook getGlusterServerHook(Guid hookId, Guid serverId) {
        GlusterServerHook serverHook = getCallsHandler().executeRead("GetGlusterServerHook",
                                    glusterServerHookRowMapper,
                                    getCustomMapSqlParameterSource()
                                    .addValue("hook_id", hookId)
                                    .addValue("server_id", serverId));
        return serverHook;
    }

    @Override
    public List<GlusterHookEntity> getByClusterId(Guid clusterId) {
        List<GlusterHookEntity> glusterHooks =
                getCallsHandler().executeReadList("GetGlusterHooksByClusterId",
                        glusterHookRowMapper,
                        getCustomMapSqlParameterSource().addValue("cluster_id", clusterId));
        return glusterHooks;
    }

    @Override
    public String getGlusterHookContent(Guid hookId) {
        String content = getCallsHandler().executeRead("GetGlusterHookContentById", GlusterHookContentRowMapper,
                createIdParameterMapper(hookId));
        return content;
    }

    @Override
    @SuppressWarnings("deprecation")
    public List<GlusterHookEntity> getAllWithQuery(String query) {
        List<GlusterHookEntity> glusterHooks = getJdbcTemplate().query(query, glusterHookRowMapper);
        return glusterHooks;
    }

    @Override
    public void remove(Guid id) {
        getCallsHandler().executeModification("DeleteGlusterHookById",
                createIdParameterMapper(id));
    }

       @Override
    public void removeAll(Collection<Guid> ids) {
        getCallsHandler().executeModification("DeleteGlusterHooksByIds",
                getCustomMapSqlParameterSource().addValue("ids", StringUtils.join(ids, ',')));
    }

    @Override
    public void updateGlusterHook(GlusterHookEntity hook) {
        getCallsHandler().executeModification("UpdateGlusterHook",
                getCustomMapSqlParameterSource()
                .addValue("id", hook.getId())
                .addValue("hook_status", EnumUtils.nameOrNull(hook.getStatus()))
                .addValue("content_type", EnumUtils.nameOrNull(hook.getContentType()))
                .addValue("checksum", hook.getChecksum())
                .addValue("content", hook.getContent())
                .addValue("conflict_status", hook.getConflictStatus()));
    }

    @Override
    public void updateGlusterHookStatus(Guid id, GlusterHookStatus status) {
        getCallsHandler().executeModification("UpdateGlusterHookStatus",
                createIdParameterMapper(id)
                        .addValue("hook_status", EnumUtils.nameOrNull(status)));
    }

    @Override
    public void updateGlusterServerHookStatus(Guid id, Guid serverId, GlusterHookStatus status) {
        getCallsHandler().executeModification("UpdateGlusterServerHookStatus",
                getCustomMapSqlParameterSource()
                        .addValue("hook_id", id)
                        .addValue("server_id", serverId)
                        .addValue("hook_status", EnumUtils.nameOrNull(status)));
    }

    @Override
    public void updateGlusterHookContent(Guid id, String checksum, String content) {
        getCallsHandler().executeModification("UpdateGlusterHookContent",
                createIdParameterMapper(id)
                        .addValue("checksum", checksum)
                        .addValue("content", content));
    }

    @Override
    public void updateGlusterHookConflictStatus(Guid hookId, Integer conflictStatus) {
        getCallsHandler().executeModification("UpdateGlusterHookConflictStatus",
                createIdParameterMapper(hookId)
                        .addValue("conflict_status", conflictStatus));
    }


    @Override
    public void saveGlusterServerHook(GlusterServerHook serverHook) {
        getCallsHandler().executeModification("InsertGlusterServerHook",
                getCustomMapSqlParameterSource()
                        .addValue("hook_id", serverHook.getHookId())
                        .addValue("server_id", serverHook.getServerId())
                        .addValue("hook_status", EnumUtils.nameOrNull(serverHook.getStatus()))
                        .addValue("content_type", EnumUtils.nameOrNull(serverHook.getContentType()))
                        .addValue("checksum", serverHook.getChecksum()));

    }

    @Override
    public void saveOrUpdateGlusterServerHook(GlusterServerHook serverHook) {
        if (getGlusterServerHook(serverHook.getHookId(), serverHook.getServerId()) == null) {
            saveGlusterServerHook(serverHook);
        } else {
            updateGlusterServerHook(serverHook);
        }

    }


    @Override
    public void updateGlusterServerHookChecksum(Guid hookId, Guid serverId, String checksum) {
        getCallsHandler().executeModification("UpdateGlusterServerHookChecksum",
                getCustomMapSqlParameterSource()
                        .addValue("hook_id", hookId)
                        .addValue("server_id", serverId)
                        .addValue("checksum", checksum));
    }

    @Override
    public void updateGlusterServerHook(GlusterServerHook serverHook) {
        getCallsHandler().executeModification("UpdateGlusterServerHook",
                getCustomMapSqlParameterSource()
                        .addValue("hook_id", serverHook.getHookId())
                        .addValue("server_id", serverHook.getServerId())
                        .addValue("hook_status", EnumUtils.nameOrNull(serverHook.getStatus()))
                        .addValue("content_type", EnumUtils.nameOrNull(serverHook.getContentType()))
                        .addValue("checksum", serverHook.getChecksum()));
    }

    @Override
    public void removeGlusterServerHooks(Guid id) {
        getCallsHandler().executeModification("DeleteGlusterServerHookById",
                getCustomMapSqlParameterSource().addValue("hook_id", id));
    }

    @Override
    public void removeGlusterServerHook(Guid id, Guid serverId) {
        getCallsHandler().executeModification("DeleteGlusterServerHook",
                getCustomMapSqlParameterSource()
                .addValue("hook_id", id)
                .addValue("server_id", serverId));
    }

    private static final class GlusterHookRowMapper implements RowMapper<GlusterHookEntity> {
        @Override
        public GlusterHookEntity mapRow(ResultSet rs, int rowNum)
                throws SQLException {
            GlusterHookEntity entity = new GlusterHookEntity();
            entity.setId(getGuidDefaultEmpty(rs, "id"));
            entity.setClusterId(getGuidDefaultEmpty(rs, "cluster_id"));
            entity.setGlusterCommand(rs.getString("gluster_command"));
            entity.setStage(rs.getString("stage"));
            entity.setName(rs.getString("name"));
            entity.setStatus(rs.getString("hook_status"));
            entity.setContentType(rs.getString("content_type"));
            entity.setChecksum(rs.getString("checksum"));
            entity.setContent(rs.getString("content"));
            entity.setConflictStatus(rs.getInt("conflict_status"));
            return entity;
        }
    }

    private static final class GlusterServerHookRowMapper implements RowMapper<GlusterServerHook> {
        @Override
        public GlusterServerHook mapRow(ResultSet rs, int rowNum)
                throws SQLException {
            GlusterServerHook entity = new GlusterServerHook();
            entity.setHookId(getGuidDefaultEmpty(rs, "hook_id"));
            entity.setServerId(getGuidDefaultEmpty(rs, "server_id"));
            entity.setStatus(rs.getString("hook_status"));
            entity.setContentType(rs.getString("content_type"));
            entity.setChecksum(rs.getString("checksum"));
            entity.setServerName(rs.getString("server_name"));
            return entity;
        }
    }

    private static final class GlusterHookContentRowMapper implements RowMapper<String> {
        @Override
        public String mapRow(ResultSet rs, int rowNum)
                throws SQLException {
            String content = rs.getString(1);
            return content;
        }
    }

    @Override
    protected RowMapper<GlusterHookEntity> createEntityRowMapper() {
        return glusterHookRowMapper;
    }

    @Override
    protected MapSqlParameterSource createFullParametersMapper(GlusterHookEntity hook) {
        return createIdParameterMapper(hook.getId())
                .addValue("cluster_id", hook.getClusterId())
                .addValue("gluster_command", hook.getGlusterCommand())
                .addValue("stage", EnumUtils.nameOrNull(hook.getStage()))
                .addValue("name", hook.getName())
                .addValue("hook_status", EnumUtils.nameOrNull(hook.getStatus()))
                .addValue("content_type", EnumUtils.nameOrNull(hook.getContentType()))
                .addValue("checksum", hook.getChecksum())
                .addValue("content", hook.getContent())
                .addValue("conflict_status", hook.getConflictStatus());
    }

    @Override
    protected MapSqlParameterSource createIdParameterMapper(Guid id) {
        return getCustomMapSqlParameterSource().addValue("id", id);
    }

    @Override
    public void removeAllInCluster(Guid clusterId) {
        getCallsHandler().executeModification("DeleteAllGlusterHooks",
                getCustomMapSqlParameterSource()
                .addValue("cluster_id", clusterId));
    }
}

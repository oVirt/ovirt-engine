package org.ovirt.engine.core.dao;

import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.pm.FenceAgent;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacadeUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

@Named
@Singleton
public class FenceAgentDaoImpl extends BaseDao implements FenceAgentDao {

    @Override
    public List<FenceAgent> getFenceAgentsForHost(Guid hostId) {
        return getCallsHandler().executeReadList("getFenceAgentsByVdsId",
                fenceAgentRowMapper,
                getCustomMapSqlParameterSource()
                        .addValue("vds_guid", hostId));
    }

    @Override
    public FenceAgent get(Guid id) {
        return getCallsHandler().executeRead("getFenceAgentById",
                fenceAgentRowMapper,
                getCustomMapSqlParameterSource()
                        .addValue("guid", id));
    }

    @Override
    public List<FenceAgent> getAll() {
        throw new UnsupportedOperationException("Fence agents are always retrieved in context of a host. Getting all agentsis not supported.");
    }

    @Override
    public void save(FenceAgent agent) {
        MapSqlParameterSource params = getInsertOrUpdateParams(agent);
        Guid id = Guid.newGuid();
        params.addValue("id", id); // create random ID for the new agent.
        agent.setId(id);
        getCallsHandler().executeModification("InsertFenceAgent", params);
    }

    @Override
    public void update(FenceAgent agent) {
        MapSqlParameterSource params = getInsertOrUpdateParams(agent);
        params.addValue("guid", agent.getId());
        getCallsHandler().executeModification("UpdateFenceAgent", params);
    }

    @Override
    public void remove(Guid id) {
        getCallsHandler().executeModification("DeleteFenceAgent",
                getCustomMapSqlParameterSource().addValue("guid", id));
    }

    @Override
    public void removeByVdsId(Guid vdsId) {
        getCallsHandler().executeModification("DeleteFenceAgentsByVdsId",
                getCustomMapSqlParameterSource().addValue("vds_guid", vdsId));
    }

    private MapSqlParameterSource getInsertOrUpdateParams(FenceAgent agent) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource();
        if (!Guid.isNullOrEmpty(agent.getHostId())) {
            parameterSource.addValue("vds_id", agent.getHostId());
        }
        parameterSource.addValue("agent_order", agent.getOrder());
        if (agent.getIp() != null) {
            parameterSource.addValue("ip", agent.getIp());
        }
        if (agent.getType() != null) {
            parameterSource.addValue("type", agent.getType());
        }
        if (agent.getUser() != null) {
            parameterSource.addValue("agent_user", agent.getUser());
        }
        if (agent.getPassword() != null) {
            parameterSource.addValue("agent_password", DbFacadeUtils.encryptPassword(agent.getPassword()));
        }
        if (agent.getPort() != null) {
            parameterSource.addValue("port", agent.getPort());
        } else {
            parameterSource.addValue("port", null);
        }
        parameterSource.addValue("encrypt_options", agent.getEncryptOptions());
        if (agent.getOptions() != null) {
            if (agent.getEncryptOptions()) {
                parameterSource.addValue("options", DbFacadeUtils.encryptPassword(agent.getOptions()));
            } else {
                parameterSource.addValue("options", agent.getOptions());
            }
        } else {
            parameterSource.addValue("options", "");
        }
        return parameterSource;
    }

    private static final RowMapper<FenceAgent> fenceAgentRowMapper = (rs, rowNum) -> {
        FenceAgent entity = new FenceAgent();
        entity.setId(getGuid(rs, "id"));
        entity.setHostId(getGuid(rs, "vds_id"));
        entity.setOrder(rs.getInt("agent_order"));
        entity.setType(rs.getString("type"));
        entity.setUser(rs.getString("agent_user"));
        entity.setPassword(DbFacadeUtils.decryptPassword(rs.getString("agent_password")));
        int port = rs.getInt("port");
        entity.setPort(port == 0 ? null : port);
        entity.setEncryptOptions(rs.getBoolean("encrypt_options"));
        final String options = rs.getString("options");
        if (entity.getEncryptOptions() && !options.isEmpty()) {
            entity.setOptions(DbFacadeUtils.decryptPassword(options));
        } else {
            entity.setOptions(options);
        }
        entity.setIp(rs.getString("ip"));
        return entity;
    };
}

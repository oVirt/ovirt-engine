package org.ovirt.engine.core.dao.network;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.ovirt.engine.core.common.businessentities.network.HostNicVfsConfig;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DefaultGenericDaoDbFacade;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

public class HostNicVfsConfigDaoDbFacadeImpl extends DefaultGenericDaoDbFacade<HostNicVfsConfig, Guid> implements HostNicVfsConfigDao {

    public HostNicVfsConfigDaoDbFacadeImpl() {
        super("HostNicVfsConfig");
        setProcedureNameForGet("GetHostNicVfsConfigById");
    }

    @Override
    protected MapSqlParameterSource createFullParametersMapper(HostNicVfsConfig hostNicVfsConfig) {
        return createIdParameterMapper(hostNicVfsConfig.getId())
                .addValue("nic_id", hostNicVfsConfig.getNicId())
                .addValue("is_all_networks_allowed", hostNicVfsConfig.isAllNetworksAllowed());
    }

    @Override
    protected MapSqlParameterSource createIdParameterMapper(Guid id) {
        return getCustomMapSqlParameterSource().addValue("id", id);
    }

    @Override
    protected RowMapper<HostNicVfsConfig> createEntityRowMapper() {
        return HostNicVfsConfigRowMapper.INSTANCE;
    }

    private static class HostNicVfsConfigRowMapper implements RowMapper<HostNicVfsConfig> {

        public final static HostNicVfsConfigRowMapper INSTANCE = new HostNicVfsConfigRowMapper();

        private HostNicVfsConfigRowMapper() {
        }

        @Override
        public HostNicVfsConfig mapRow(ResultSet rs, int index) throws SQLException {
            HostNicVfsConfig entity = new HostNicVfsConfig();
            entity.setId(getGuid(rs, "id"));
            entity.setNicId(getGuid(rs, "nic_id"));
            entity.setAllNetworksAllowed(rs.getBoolean("is_all_networks_allowed"));

            return entity;
        }
    }
}

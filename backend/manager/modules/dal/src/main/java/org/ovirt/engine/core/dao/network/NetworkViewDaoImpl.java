package org.ovirt.engine.core.dao.network;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.network.NetworkView;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.BaseDao;
import org.ovirt.engine.core.dao.VersionRowMapper;
import org.ovirt.engine.core.dao.network.NetworkDaoImpl.NetworkRowMapperBase;

@Named
@Singleton
public class NetworkViewDaoImpl extends BaseDao implements NetworkViewDao {

    @Inject
    private DnsResolverConfigurationDao dnsResolverConfigurationDao;
    private NetworkViewRowMapper networkViewRowMapper;

    @PostConstruct
    public void init() {
        networkViewRowMapper = new NetworkViewRowMapper(dnsResolverConfigurationDao);
    }

    @Override
    public List<NetworkView> getAllWithQuery(String query) {
        return getJdbcTemplate().query(query, networkViewRowMapper);
    }

    @Override
    public List<NetworkView> getAllForProvider(Guid id) {
        return getCallsHandler().executeReadList("GetAllNetworkViewsByNetworkProviderId",
                networkViewRowMapper,
                getCustomMapSqlParameterSource().addValue("id", id));
    }

    private static class NetworkViewRowMapper extends NetworkRowMapperBase<NetworkView> {

        public NetworkViewRowMapper(DnsResolverConfigurationDao dnsResolverConfigurationDao) {
            super(dnsResolverConfigurationDao);
        }

        @Override
        public NetworkView mapRow(ResultSet rs, int rowNum) throws SQLException {
            NetworkView entity = super.mapRow(rs, rowNum);
            entity.setStoragePoolName(rs.getString("storage_pool_name"));
            entity.setCompatibilityVersion(new VersionRowMapper("compatibility_version").mapRow(rs, rowNum));
            entity.setProviderName(rs.getString("provider_name"));
            entity.setQosName(rs.getString("qos_name"));
            return entity;
        }

        protected NetworkView createNetworkEntity() {
            return new NetworkView();
        }
    }
}

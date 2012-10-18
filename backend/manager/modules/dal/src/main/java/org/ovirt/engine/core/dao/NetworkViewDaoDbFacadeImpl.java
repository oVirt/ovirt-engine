package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.NetworkView;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.NetworkDAODbFacadeImpl.NetworkRowMapper;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

/**
 * <code>NetworkViewDaoDbFacadeImpl</code> provides a concrete implementation of {@link #NetworViewkDao}.
 */
public class NetworkViewDaoDbFacadeImpl extends BaseDAODbFacade implements NetworkViewDao {

    @Override
    public List<NetworkView> getAllWithQuery(String query) {
        return new SimpleJdbcTemplate(jdbcTemplate).query(query, NetworkViewRowMapper.instance);
    }

    private static class NetworkViewRowMapper implements ParameterizedRowMapper<NetworkView> {
        public final static NetworkViewRowMapper instance = new NetworkViewRowMapper();

        @Override
        public NetworkView mapRow(ResultSet rs, int rowNum) throws SQLException {
            NetworkView entity = new NetworkView();
            entity.setNetwork(NetworkRowMapper.instance.mapRow(rs, rowNum));
            entity.setStoragePoolName(rs.getString("storage_pool_name"));
            entity.setCompatabilityVersion(new Version(rs.getString("compatibility_version")));
            return entity;
        }
    }
}

package org.ovirt.engine.core.dao.network;

import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.network.NetworkFilter;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.DefaultReadDao;
import org.ovirt.engine.core.dao.VersionRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

@Named
@Singleton
public class NetworkFilterDaoImpl extends DefaultReadDao<NetworkFilter, Guid>implements NetworkFilterDao {

    private static final String FILTER_ID = "filter_id";
    private static final String FILTER_NAME = "filter_name";
    private static final String FILTER_VERSION = "version";

    private static final RowMapper<NetworkFilter> networkFilterRowMapper = (rs, rowNum) -> {
        NetworkFilter entity = new NetworkFilter();
        entity.setId(getGuid(rs, FILTER_ID));
        entity.setName(rs.getString(FILTER_NAME));
        final VersionRowMapper versionRowMapper = new VersionRowMapper(FILTER_VERSION);
        Version version = versionRowMapper.mapRow(rs, rowNum);
        entity.setVersion(version);
        return entity;
    };

    public NetworkFilterDaoImpl() {
        super("NetworkFilter");
    }

    @Override
    public List<NetworkFilter> getAllNetworkFilters() {
        return getCallsHandler().executeReadList("GetAllNetworkFilters",
                networkFilterRowMapper,
                getCustomMapSqlParameterSource());
    }

    @Override
    public List<NetworkFilter> getAllSupportedNetworkFiltersByVersion(Version version) {
        return getCallsHandler().executeReadList("GetAllSupportedNetworkFiltersByVersion",
                networkFilterRowMapper,
                getCustomMapSqlParameterSource().addValue(FILTER_VERSION, version));
    }

    @Override
    public NetworkFilter getNetworkFilterById(Guid id) {
        return getCallsHandler().executeRead("GetNetworkFilterById",
                networkFilterRowMapper,
                getCustomMapSqlParameterSource().addValue(FILTER_ID, id));
    }

    @Override
    public NetworkFilter getNetworkFilterByName(String networkFilterName) {
        return getCallsHandler().executeRead("GetNetworkFilterByName",
                networkFilterRowMapper,
                getCustomMapSqlParameterSource().addValue(FILTER_NAME, networkFilterName));
    }

    @Override
    protected MapSqlParameterSource createIdParameterMapper(Guid id) {
        return getCustomMapSqlParameterSource().addValue(FILTER_ID, id);
    }

    @Override
    protected RowMapper<NetworkFilter> createEntityRowMapper() {
        return networkFilterRowMapper;
    }
}

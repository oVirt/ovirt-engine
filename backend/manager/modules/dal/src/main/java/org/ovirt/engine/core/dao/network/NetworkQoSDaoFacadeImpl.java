package org.ovirt.engine.core.dao.network;

import org.ovirt.engine.core.common.businessentities.network.NetworkQoS;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DefaultGenericDaoDbFacade;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class NetworkQoSDaoFacadeImpl extends DefaultGenericDaoDbFacade<NetworkQoS, Guid> implements NetworkQoSDao {

    protected final RowMapper<NetworkQoS> mapper = createEntityRowMapper();

    public NetworkQoSDaoFacadeImpl(){
        super("NetworkQos");
    }

    private static Integer getIntegerOrNull(ResultSet rs, String columnName) throws SQLException {
        int i = rs.getInt(columnName);
        return rs.wasNull() ? null : i;
    }

    @Override
    protected MapSqlParameterSource createIdParameterMapper(Guid guid) {
        return getCustomMapSqlParameterSource()
                .addValue("id", guid);
    }

    @Override
    protected RowMapper<NetworkQoS> createEntityRowMapper() {
        return new RowMapper<NetworkQoS>() {
            @Override
            public NetworkQoS mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
                NetworkQoS entity = new NetworkQoS();
                entity.setId(getGuid(rs, "id"));
                entity.setName(rs.getString("name"));
                entity.setStoragePoolId(getGuid(rs, "storage_pool_id"));
                entity.setInboundAverage(getIntegerOrNull(rs, "inbound_average"));
                entity.setInboundPeak(getIntegerOrNull(rs, "inbound_peak"));
                entity.setInboundBurst(getIntegerOrNull(rs, "inbound_burst"));
                entity.setOutboundAverage(getIntegerOrNull(rs, "outbound_average"));
                entity.setOutboundPeak(getIntegerOrNull(rs, "outbound_peak"));
                entity.setOutboundBurst(getIntegerOrNull(rs, "outbound_burst"));
                return entity;
            }
        };
    }

    @Override
    public List<NetworkQoS> getAllForStoragePoolId(Guid storagePoolId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("storage_pool_id", storagePoolId.getUuid());
        return getCallsHandler().executeReadList("GetAllNetworkQosForStoragePool", mapper, parameterSource);
    }

    @Override
    protected MapSqlParameterSource createFullParametersMapper(NetworkQoS networkQoS) {
        return getCustomMapSqlParameterSource()
                .addValue("id", networkQoS.getId())
                .addValue("name", networkQoS.getName())
                .addValue("storage_pool_id", networkQoS.getStoragePoolId())
                .addValue("inbound_average", networkQoS.getInboundAverage())
                .addValue("inbound_peak", networkQoS.getInboundPeak())
                .addValue("inbound_burst", networkQoS.getInboundBurst())
                .addValue("outbound_average", networkQoS.getOutboundAverage())
                .addValue("outbound_peak", networkQoS.getOutboundPeak())
                .addValue("outbound_burst", networkQoS.getOutboundBurst());

    }
}

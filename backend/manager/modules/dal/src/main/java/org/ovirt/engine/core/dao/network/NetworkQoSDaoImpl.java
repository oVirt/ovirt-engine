package org.ovirt.engine.core.dao.network;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.network.NetworkQoS;
import org.ovirt.engine.core.common.businessentities.qos.QosType;
import org.ovirt.engine.core.dao.qos.QosBaseDaoImpl;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

@Named
@Singleton
public class NetworkQoSDaoImpl extends QosBaseDaoImpl<NetworkQoS> implements NetworkQoSDao {

    protected final RowMapper<NetworkQoS> mapper = createEntityRowMapper();

    public NetworkQoSDaoImpl(){
        super(QosType.NETWORK);
    }

    @Override
    protected MapSqlParameterSource createFullParametersMapper(NetworkQoS networkQos) {
        MapSqlParameterSource map = super.createFullParametersMapper(networkQos);
        map.addValue("inbound_average", networkQos.getInboundAverage());
        map.addValue("inbound_peak", networkQos.getInboundPeak());
        map.addValue("inbound_burst", networkQos.getInboundBurst());
        map.addValue("outbound_average", networkQos.getOutboundAverage());
        map.addValue("outbound_peak", networkQos.getOutboundPeak());
        map.addValue("outbound_burst", networkQos.getOutboundBurst());
        return map;
    }

    @Override
    protected RowMapper<NetworkQoS> createEntityRowMapper() {
        return NetworkQosDaoDbFacadaeImplMapper.MAPPER;
    }

    public static class NetworkQosDaoDbFacadaeImplMapper extends QosBaseDaoFacadaeImplMapper<NetworkQoS> {
        public static final NetworkQosDaoDbFacadaeImplMapper MAPPER = new NetworkQosDaoDbFacadaeImplMapper();

        @Override
        public NetworkQoS createQosEntity(ResultSet rs) throws SQLException {
            NetworkQoS entity = new NetworkQoS();
            entity.setInboundAverage(getInteger(rs, "inbound_average"));
            entity.setInboundPeak(getInteger(rs, "inbound_peak"));
            entity.setInboundBurst(getInteger(rs, "inbound_burst"));
            entity.setOutboundAverage(getInteger(rs, "outbound_average"));
            entity.setOutboundPeak(getInteger(rs, "outbound_peak"));
            entity.setOutboundBurst(getInteger(rs, "outbound_burst"));
            return entity;
        }
    }
}

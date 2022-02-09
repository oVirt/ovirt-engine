package org.ovirt.engine.core.dao.network;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.network.InterfaceStatus;
import org.ovirt.engine.core.common.businessentities.network.NetworkStatistics;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.MassOperationsGenericDao;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

public abstract class NetworkStatisticsDaoImpl<T extends NetworkStatistics> extends MassOperationsGenericDao<T, Guid> {

    protected NetworkStatisticsDaoImpl(String entityStoredProcedureName) {
        super(entityStoredProcedureName);
    }

    @Override
    protected MapSqlParameterSource createFullParametersMapper(T stats) {
        NetworkStatisticsParametersMapper<T> mapper = new NetworkStatisticsParametersMapper<>();
        return getCustomMapSqlParameterSource().addValues(mapper.createParametersMap(stats));
    }

    @Override
    protected MapSqlParameterSource createIdParameterMapper(Guid id) {
        return getCustomMapSqlParameterSource().addValue("id", id);
    }

    public static class NetworkStatisticsParametersMapper<T extends NetworkStatistics> {

        public Map<String, Object> createParametersMap(T stats) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", stats.getId());
            map.put("rx_drop", stats.getReceiveDrops());
            map.put("rx_rate", stats.getReceiveRate());
            map.put("rx_total", stats.getReceivedBytes());
            map.put("rx_offset", stats.getReceivedBytesOffset());
            map.put("tx_drop", stats.getTransmitDrops());
            map.put("tx_rate", stats.getTransmitRate());
            map.put("tx_total", stats.getTransmittedBytes());
            map.put("tx_offset", stats.getTransmittedBytesOffset());
            map.put("iface_status", stats.getStatus());
            map.put("sample_time", stats.getSampleTime());
            return map;
        }
    }

    protected abstract static class NetworkStatisticsRowMapper<T extends NetworkStatistics> implements RowMapper<T> {

        protected abstract T createEntity();

        @Override
        public T mapRow(ResultSet rs, int rowNum) throws SQLException {
            T entity = createEntity();
            entity.setId(getGuidDefaultEmpty(rs, "id"));
            entity.setReceiveRate(rs.getDouble("rx_rate"));
            entity.setTransmitRate(rs.getDouble("tx_rate"));
            entity.setReceivedBytes(getBigInteger(rs, "rx_total"));
            entity.setTransmittedBytes(getBigInteger(rs, "tx_total"));
            entity.setReceivedBytesOffset(getBigInteger(rs, "rx_offset"));
            entity.setTransmittedBytesOffset(getBigInteger(rs, "tx_offset"));
            entity.setReceiveDrops(getBigInteger(rs, "rx_drop"));
            entity.setTransmitDrops(getBigInteger(rs, "tx_drop"));
            entity.setStatus(InterfaceStatus.forValue(rs.getInt("iface_status")));
            entity.setSampleTime(getDouble(rs, "sample_time"));
            return entity;
        }
    }

}

package org.ovirt.engine.core.dao.network;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.network.InterfaceStatus;
import org.ovirt.engine.core.common.businessentities.network.NetworkStatistics;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.MassOperationsGenericDaoDbFacade;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

public abstract class NetworkStatisticsDaoDbFacadeImpl<T extends NetworkStatistics> extends MassOperationsGenericDaoDbFacade<T, Guid> {

    protected NetworkStatisticsDaoDbFacadeImpl(String entityStoredProcedureName) {
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
            map.put("rx_drop", stats.getReceiveDropRate());
            map.put("rx_rate", stats.getReceiveRate());
            map.put("tx_drop", stats.getTransmitDropRate());
            map.put("tx_rate", stats.getTransmitRate());
            map.put("iface_status", stats.getStatus());
            return map;
        }
    }

    protected static abstract class NetworkStatisticsRowMapper<T extends NetworkStatistics> implements RowMapper<T> {

        protected abstract T createEntity();

        @Override
        public T mapRow(ResultSet rs, int rowNum) throws SQLException {
            T entity = createEntity();
            entity.setId(getGuidDefaultEmpty(rs, "id"));
            entity.setReceiveRate(rs.getDouble("rx_rate"));
            entity.setTransmitRate(rs.getDouble("tx_rate"));
            entity.setReceiveDropRate(rs.getDouble("rx_drop"));
            entity.setTransmitDropRate(rs.getDouble("tx_drop"));
            entity.setStatus(InterfaceStatus.forValue(rs.getInt("iface_status")));
            return entity;
        }
    }

}

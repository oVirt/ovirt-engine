package org.ovirt.engine.core.dao.network;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.ovirt.engine.core.common.businessentities.network.InterfaceStatus;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkStatistics;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.MassOperationsGenericDaoDbFacade;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

public class VmNetworkStatisticsDaoDbFacadeImpl extends MassOperationsGenericDaoDbFacade<VmNetworkStatistics, Guid>
        implements VmNetworkStatisticsDao {

    public VmNetworkStatisticsDaoDbFacadeImpl() {
        super("vm_interface_statistics");
        setProcedureNameForGet("Getvm_interface_statisticsById");
    }

    @Override
    public List<VmNetworkStatistics> getAll() {
        throw new NotImplementedException();
    }

    @Override
    protected MapSqlParameterSource createIdParameterMapper(Guid id) {
        return getCustomMapSqlParameterSource().addValue("id", id);
    }

    @Override
    protected MapSqlParameterSource createFullParametersMapper(VmNetworkStatistics stats) {
        return createIdParameterMapper(stats.getId())
                .addValue("rx_drop", stats.getReceiveDropRate())
                .addValue("rx_rate", stats.getReceiveRate())
                .addValue("tx_drop", stats.getTransmitDropRate())
                .addValue("tx_rate", stats.getTransmitRate())
                .addValue("iface_status", stats.getStatus())
                .addValue("vm_id", stats.getVmId());
    }

    @Override
    protected RowMapper<VmNetworkStatistics> createEntityRowMapper() {
        return new RowMapper<VmNetworkStatistics>() {
            @Override
            public VmNetworkStatistics mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
                VmNetworkStatistics entity = new VmNetworkStatistics();
                entity.setId(getGuidDefaultEmpty(rs, "id"));
                entity.setReceiveRate(rs.getDouble("rx_rate"));
                entity.setTransmitRate(rs.getDouble("tx_rate"));
                entity.setReceiveDropRate(rs.getDouble("rx_drop"));
                entity.setTransmitDropRate(rs.getDouble("tx_drop"));
                entity.setStatus(InterfaceStatus.forValue(rs.getInt("iface_status")));
                return entity;
            }
        };
    }
}

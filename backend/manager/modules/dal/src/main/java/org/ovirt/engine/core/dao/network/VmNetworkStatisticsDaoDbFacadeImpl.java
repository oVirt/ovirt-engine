package org.ovirt.engine.core.dao.network;

import java.util.List;

import javax.inject.Named;

import org.apache.commons.lang.NotImplementedException;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkStatistics;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

@Named
public class VmNetworkStatisticsDaoDbFacadeImpl extends NetworkStatisticsDaoDbFacadeImpl<VmNetworkStatistics>
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
    protected MapSqlParameterSource createFullParametersMapper(VmNetworkStatistics stats) {
        return super.createFullParametersMapper(stats)
                .addValue("vm_id", stats.getVmId());
    }

    @Override
    protected RowMapper<VmNetworkStatistics> createEntityRowMapper() {
        return VmNetworkStatisticsRowMapper.INSTANCE;
    }

    public static class VmNetworkStatisticsRowMapper extends NetworkStatisticsRowMapper<VmNetworkStatistics> {

        protected final static VmNetworkStatisticsRowMapper INSTANCE = new VmNetworkStatisticsRowMapper();

        @Override
        protected VmNetworkStatistics createEntity() {
            return new VmNetworkStatistics();
        }
    }
}

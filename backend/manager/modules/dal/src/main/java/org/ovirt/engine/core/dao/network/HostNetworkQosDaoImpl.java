package org.ovirt.engine.core.dao.network;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.network.HostNetworkQos;
import org.ovirt.engine.core.common.businessentities.qos.QosType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.qos.QosBaseDaoImpl;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

@Named
@Singleton
public class HostNetworkQosDaoImpl extends QosBaseDaoImpl<HostNetworkQos> implements HostNetworkQosDao {

    private static final String OUT_AVERAGE_LINKSHARE = "out_average_linkshare";
    private static final String OUT_AVERAGE_UPPERLIMIT = "out_average_upperlimit";
    private static final String OUT_AVERAGE_REALTIME = "out_average_realtime";

    public HostNetworkQosDaoImpl() {
        super(QosType.HOSTNETWORK);
    }

    @Override
    protected MapSqlParameterSource createFullParametersMapper(HostNetworkQos obj) {
        MapSqlParameterSource map = super.createFullParametersMapper(obj);
        map.addValue(OUT_AVERAGE_LINKSHARE, obj.getOutAverageLinkshare());
        map.addValue(OUT_AVERAGE_UPPERLIMIT, obj.getOutAverageUpperlimit());
        map.addValue(OUT_AVERAGE_REALTIME, obj.getOutAverageRealtime());
        return map;
    }

    @Override
    protected RowMapper<HostNetworkQos> createEntityRowMapper() {
        return HostNetworkQosDaoDbFacadaeImplMapper.MAPPER;
    }

    @Override
    public HostNetworkQos getHostNetworkQosOfMigrationNetworkByClusterId(Guid clusterId) {
        final MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("cluster_id", clusterId);
        return getCallsHandler().executeRead("GetHostNetworkQosOfMigrationNetworkByClusterId",
                createEntityRowMapper(),
                parameterSource);
    }

    @Override
    public void persistQosChanges(Guid qosId, HostNetworkQos qos) {
        HostNetworkQos oldQos = get(qosId);
        boolean qosOfGivenIdAlreadyExist = oldQos != null;

        if (qos == null) {
            if (qosOfGivenIdAlreadyExist) {
                remove(qosId);
            }
        } else {
            qos.setId(qosId);
            if (!qosOfGivenIdAlreadyExist) {
                save(qos);
            } else {
                boolean qosChanged = !qos.equals(oldQos);
                if (qosChanged) {
                    update(qos);
                }
            }
        }
    }

    public static class HostNetworkQosDaoDbFacadaeImplMapper extends QosBaseDaoFacadaeImplMapper<HostNetworkQos> {

        public static final HostNetworkQosDaoDbFacadaeImplMapper MAPPER = new HostNetworkQosDaoDbFacadaeImplMapper();

        @Override
        public HostNetworkQos createQosEntity(ResultSet rs) throws SQLException {
            HostNetworkQos entity = new HostNetworkQos();
            entity.setOutAverageLinkshare(getInteger(rs, OUT_AVERAGE_LINKSHARE));
            entity.setOutAverageUpperlimit(getInteger(rs, OUT_AVERAGE_UPPERLIMIT));
            entity.setOutAverageRealtime(getInteger(rs, OUT_AVERAGE_REALTIME));
            return entity;
        }
    }

}

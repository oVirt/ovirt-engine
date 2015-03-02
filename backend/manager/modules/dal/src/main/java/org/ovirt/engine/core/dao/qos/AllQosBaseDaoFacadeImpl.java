package org.ovirt.engine.core.dao.qos;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.qos.QosBase;
import org.ovirt.engine.core.common.businessentities.qos.QosType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.network.HostNetworkQosDaoDbFacadeImpl;
import org.ovirt.engine.core.dao.network.NetworkQoSDaoFacadeImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

public class AllQosBaseDaoFacadeImpl extends QosBaseDaoFacadeImpl<QosBase> implements QosBaseDao {
    private static final Logger log = LoggerFactory.getLogger(AllQosBaseDaoFacadeImpl.class);

    public AllQosBaseDaoFacadeImpl() {
        super(QosType.ALL);
    }

    @Override
    public List<QosBase> getAllForStoragePoolId(Guid storagePoolId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("storage_pool_id", storagePoolId);
        return getCallsHandler().executeReadList("GetAllQosForStoragePool",
                createEntityRowMapper(),
                parameterSource);
    }

    @Override
    protected RowMapper<QosBase> createEntityRowMapper() {
        return AllQosBaseDaoFacadaeImplMapper.MAPPER;
    }

    protected static class AllQosBaseDaoFacadaeImplMapper extends QosBaseDaoFacadaeImplMapper<QosBase> {
        public final static AllQosBaseDaoFacadaeImplMapper MAPPER = new AllQosBaseDaoFacadaeImplMapper();

        @Override
        public QosBase mapRow(ResultSet rs, int rowNum) throws SQLException {
            return super.mapRow(rs, rowNum);
        }

        @Override
        protected QosBase createQosEntity(ResultSet rs) throws SQLException {
            QosType qosType = QosType.forValue(rs.getInt("qos_type"));
            switch (qosType) {
            case STORAGE:
                return StorageQosDaoDbFacadeImpl.StorageDaoDbFacadaeImplMapper.MAPPER.createQosEntity(rs);
            case CPU:
                return CpuQosDaoDbFacadeImpl.CpuDaoDbFacadaeImplMapper.MAPPER.createQosEntity(rs);
            case NETWORK:
                return NetworkQoSDaoFacadeImpl.NetworkQosDaoDbFacadaeImplMapper.MAPPER.createQosEntity(rs);
            case HOSTNETWORK:
                return HostNetworkQosDaoDbFacadeImpl.HostNetworkQosDaoDbFacadaeImplMapper.MAPPER.createQosEntity(rs);
            default:
                log.debug("not handled/missing qos_type '{}'", qosType);
                break;
            }

            return null;
        }
    }

}

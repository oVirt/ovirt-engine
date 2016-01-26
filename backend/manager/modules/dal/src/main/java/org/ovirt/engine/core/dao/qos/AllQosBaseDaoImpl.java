package org.ovirt.engine.core.dao.qos;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.qos.QosBase;
import org.ovirt.engine.core.common.businessentities.qos.QosType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.network.HostNetworkQosDaoImpl;
import org.ovirt.engine.core.dao.network.NetworkQoSDaoImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

@Named
@Singleton
public class AllQosBaseDaoImpl extends QosBaseDaoImpl<QosBase> implements QosBaseDao {
    private static final Logger log = LoggerFactory.getLogger(AllQosBaseDaoImpl.class);

    public AllQosBaseDaoImpl() {
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
        public static final AllQosBaseDaoFacadaeImplMapper MAPPER = new AllQosBaseDaoFacadaeImplMapper();

        @Override
        protected QosBase createQosEntity(ResultSet rs) throws SQLException {
            QosType qosType = QosType.forValue(rs.getInt("qos_type"));
            switch (qosType) {
            case STORAGE:
                return StorageQosDaoImpl.StorageDaoDbFacadaeImplMapper.MAPPER.createQosEntity(rs);
            case CPU:
                return CpuQosDaoImpl.CpuDaoDbFacadaeImplMapper.MAPPER.createQosEntity(rs);
            case NETWORK:
                return NetworkQoSDaoImpl.NetworkQosDaoDbFacadaeImplMapper.MAPPER.createQosEntity(rs);
            case HOSTNETWORK:
                return HostNetworkQosDaoImpl.HostNetworkQosDaoDbFacadaeImplMapper.MAPPER.createQosEntity(rs);
            default:
                log.debug("not handled/missing qos_type '{}'", qosType);
                break;
            }

            return null;
        }
    }

}

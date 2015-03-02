package org.ovirt.engine.core.dao.qos;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.ovirt.engine.core.common.businessentities.qos.CpuQos;
import org.ovirt.engine.core.common.businessentities.qos.QosType;
import org.ovirt.engine.core.compat.Guid;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

public class CpuQosDaoDbFacadeImpl extends QosBaseDaoFacadeImpl<CpuQos> implements CpuQosDao {
    public CpuQosDaoDbFacadeImpl() {
        super(QosType.CPU);
    }

    @Override
    protected MapSqlParameterSource createFullParametersMapper(CpuQos obj) {
        MapSqlParameterSource map = super.createFullParametersMapper(obj);
        map.addValue("cpu_limit", obj.getCpuLimit());

        return map;
    }

    @Override
    public CpuQos getCpuQosByVmId(Guid vmId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vm_id", vmId);
        return getCallsHandler().executeRead("GetQosByVmId",
                createEntityRowMapper(),
                parameterSource);
    }

    @Override
    protected RowMapper<CpuQos> createEntityRowMapper() {
        return CpuDaoDbFacadaeImplMapper.MAPPER;
    }

    protected static class CpuDaoDbFacadaeImplMapper extends QosBaseDaoFacadaeImplMapper<CpuQos> {
        public static final CpuDaoDbFacadaeImplMapper MAPPER = new CpuDaoDbFacadaeImplMapper();

        @Override
        protected CpuQos createQosEntity(ResultSet rs) throws SQLException {
            CpuQos entity = new CpuQos();
            entity.setCpuLimit(getInteger(rs, "cpu_limit"));
            return entity;
        }
    }

}

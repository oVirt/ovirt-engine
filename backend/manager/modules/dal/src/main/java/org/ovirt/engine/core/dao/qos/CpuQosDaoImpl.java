package org.ovirt.engine.core.dao.qos;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.qos.CpuQos;
import org.ovirt.engine.core.common.businessentities.qos.QosType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

@Named
@Singleton
public class CpuQosDaoImpl extends QosBaseDaoImpl<CpuQos> implements CpuQosDao {
    public CpuQosDaoImpl() {
        super(QosType.CPU);
    }

    @Override
    protected MapSqlParameterSource createFullParametersMapper(CpuQos obj) {
        MapSqlParameterSource map = super.createFullParametersMapper(obj);
        map.addValue("cpu_limit", obj.getCpuLimit());

        return map;
    }

    @Override
    public Map<Guid, CpuQos> getCpuQosByVmIds(Collection<Guid> vmIds) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vm_ids", createArrayOfUUIDs(vmIds));

        List<Pair<Guid, CpuQos>> pairs = getCallsHandler().executeReadList("GetQosByVmIds",
                cpuQosMultipleMapper,
                parameterSource);

        Map<Guid, CpuQos> qosMap = new HashMap<>();
        for (Pair<Guid, CpuQos> pair : pairs) {
            qosMap.put(pair.getFirst(), pair.getSecond());
        }

        return qosMap;
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

    private static final RowMapper<Pair<Guid, CpuQos>> cpuQosMultipleMapper = (rs, rowNum) -> {
        CpuQos qos = CpuDaoDbFacadaeImplMapper.MAPPER.mapRow(rs, rowNum);
        Guid guid = new Guid(rs.getString("vm_id"));
        return new Pair<>(guid, qos);
    };
}

package org.ovirt.engine.core.dao.qos;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.qos.QosType;
import org.ovirt.engine.core.common.businessentities.qos.StorageQos;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

@Named
@Singleton
public class StorageQosDaoImpl extends QosBaseDaoImpl<StorageQos> implements StorageQosDao {
    public StorageQosDaoImpl() {
        super(QosType.STORAGE);
    }

    @Override
    public StorageQos getQosByDiskProfileId(Guid diskProfileId) {
        return getQosByDiskProfileIds(Collections.singleton(diskProfileId)).get(diskProfileId);
    }

    @Override
    public Map<Guid, StorageQos> getQosByDiskProfileIds(Collection<Guid> diskProfileIds) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("disk_profile_ids", createArrayOfUUIDs(diskProfileIds));

        List<Pair<Guid, StorageQos>> pairs = getCallsHandler().executeReadList("GetQosByDiskProfiles",
                storageQosMultipleProfilesMapper,
                parameterSource);

        return pairs.stream().collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
    }

    @Override
    protected MapSqlParameterSource createFullParametersMapper(StorageQos obj) {
        MapSqlParameterSource map = super.createFullParametersMapper(obj);
        map.addValue("max_throughput", obj.getMaxThroughput());
        map.addValue("max_read_throughput", obj.getMaxReadThroughput());
        map.addValue("max_write_throughput", obj.getMaxWriteThroughput());
        map.addValue("max_iops", obj.getMaxIops());
        map.addValue("max_read_iops", obj.getMaxReadIops());
        map.addValue("max_write_iops", obj.getMaxWriteIops());

        return map;
    }

    @Override
    protected RowMapper<StorageQos> createEntityRowMapper() {
        return StorageDaoDbFacadaeImplMapper.MAPPER;
    }

    protected static class StorageDaoDbFacadaeImplMapper extends QosBaseDaoFacadaeImplMapper<StorageQos> {
        public static final StorageDaoDbFacadaeImplMapper MAPPER = new StorageDaoDbFacadaeImplMapper();

        @Override
        protected StorageQos createQosEntity(ResultSet rs) throws SQLException {
            StorageQos entity = new StorageQos();
            entity.setMaxThroughput(getInteger(rs, "max_throughput"));
            entity.setMaxReadThroughput(getInteger(rs, "max_read_throughput"));
            entity.setMaxWriteThroughput(getInteger(rs, "max_write_throughput"));
            entity.setMaxIops(getInteger(rs, "max_iops"));
            entity.setMaxReadIops(getInteger(rs, "max_read_iops"));
            entity.setMaxWriteIops(getInteger(rs, "max_write_iops"));
            return entity;
        }
    }

    private static RowMapper<Pair<Guid, StorageQos>> storageQosMultipleProfilesMapper = (rs, rowNum) -> {
        StorageQos qos = StorageDaoDbFacadaeImplMapper.MAPPER.mapRow(rs, rowNum);
        Guid guid = new Guid(rs.getString("disk_profile_id"));
        return new Pair<>(guid, qos);
    };
}

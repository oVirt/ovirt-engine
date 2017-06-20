package org.ovirt.engine.core.dao;

import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.storage.DiskLunMap;
import org.ovirt.engine.core.common.businessentities.storage.DiskLunMapId;
import org.ovirt.engine.core.compat.Guid;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

@Named
@Singleton
public class DiskLunMapDaoImpl extends DefaultGenericDao<DiskLunMap, DiskLunMapId>
        implements DiskLunMapDao {

    public DiskLunMapDaoImpl() {
        super("DiskLunMap");
    }

    @Override
    public void update(DiskLunMap entity) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected MapSqlParameterSource createIdParameterMapper(DiskLunMapId id) {
        return getCustomMapSqlParameterSource().addValue("disk_id", id.getDiskId()).addValue("lun_id", id.getLunId());
    }

    @Override
    protected MapSqlParameterSource createFullParametersMapper(DiskLunMap entity) {
        return createIdParameterMapper(entity.getId());
    }

    @Override
    protected RowMapper<DiskLunMap> createEntityRowMapper() {
        return (rs, rowNum) -> {
            DiskLunMap diskLunMap = new DiskLunMap();

            diskLunMap.setDiskId(getGuidDefaultEmpty(rs, "disk_id"));
            diskLunMap.setLunId(rs.getString("lun_id"));

            return diskLunMap;
        };
    }

    @Override
    public DiskLunMap getDiskIdByLunId(String lunId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("lun_id", lunId);

        return getCallsHandler().executeRead("GetDiskLunMapByLunId", createEntityRowMapper(), parameterSource);
    }

    @Override
    public DiskLunMap getDiskLunMapByDiskId(Guid diskId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("disk_id", diskId);

        return getCallsHandler().executeRead("GetDiskLunMapByDiskId", createEntityRowMapper(), parameterSource);
    }

    @Override
    public List<DiskLunMap> getDiskLunMapsForVmsInPool(Guid storagePoolId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("storage_pool_id", storagePoolId);

        return getCallsHandler().executeReadList("GetDiskLunMapsForVmsInPool", createEntityRowMapper(),
                parameterSource);
    }
}

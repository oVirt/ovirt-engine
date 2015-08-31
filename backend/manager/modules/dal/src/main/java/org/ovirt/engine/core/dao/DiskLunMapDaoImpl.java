package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.storage.DiskLunMap;
import org.ovirt.engine.core.common.businessentities.storage.DiskLunMapId;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

@Named
@Singleton
public class DiskLunMapDaoImpl extends DefaultGenericDao<DiskLunMap, DiskLunMapId>
        implements DiskLunMapDao {

    private static final class DiskLunMapRowMapper implements RowMapper<DiskLunMap> {
        public static final DiskLunMapRowMapper instance = new DiskLunMapRowMapper();

        @Override
        public DiskLunMap mapRow(ResultSet rs, int rowNum) throws SQLException {
            DiskLunMap diskLunMap = new DiskLunMap();

            diskLunMap.setDiskId(getGuidDefaultEmpty(rs, "disk_id"));
            diskLunMap.setLunId(rs.getString("lun_id"));

            return diskLunMap;
        }
    }

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
        return DiskLunMapRowMapper.instance;
    }

    @Override
    public DiskLunMap getDiskIdByLunId(String lunId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("lun_id", lunId);

        return getCallsHandler().executeRead("GetDiskLunMapByLunId", createEntityRowMapper(), parameterSource);
    }
}

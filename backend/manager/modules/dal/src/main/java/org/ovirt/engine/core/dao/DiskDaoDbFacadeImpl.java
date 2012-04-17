package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.Disk.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.LunDisk;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskImageDAODbFacadeImpl.DiskImageRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

public class DiskDaoDbFacadeImpl extends DefaultReadDaoDbFacade<Disk, Guid> implements DiskDao {

    public DiskDaoDbFacadeImpl() {
        super("Disk");
    }

    @Override
    protected MapSqlParameterSource createIdParameterMapper(Guid id) {
        return getCustomMapSqlParameterSource().addValue("disk_id", id);
    }

    @Override
    protected ParameterizedRowMapper<Disk> createEntityRowMapper() {
        return DiskRowMapper.instance;
    }

    private static class DiskRowMapper implements ParameterizedRowMapper<Disk> {

        public static DiskRowMapper instance = new DiskRowMapper();

        private DiskRowMapper() {
        }

        @Override
        public Disk mapRow(ResultSet rs, int rowNum) throws SQLException {
            Disk disk = null;
            String diskStorageTypeString = rs.getString("disk_storage_type");
            DiskStorageType diskStorageType = Disk.DiskStorageType.valueOf(diskStorageTypeString);

            if (diskStorageType == null) {
                throwUnsupportedDiskStorageTypeException(diskStorageTypeString);
            }

            switch (diskStorageType) {
            case IMAGE:
                disk = DiskImageRowMapper.instance.mapRow(rs, rowNum);
                break;

            case LUN:
                disk = LunDiskRowMapper.instance.mapRow(rs, rowNum);
                break;

            default:
                throwUnsupportedDiskStorageTypeException(diskStorageTypeString);
                break;
            }

            return disk;
        }

        private void throwUnsupportedDiskStorageTypeException(String diskStorageTypeString) {
            throw new IllegalStateException(String.format("Unsupported %s: %s",
                    DiskStorageType.class.getSimpleName(),
                    diskStorageTypeString));
        }

    }

    private static class LunDiskRowMapper extends AbstractDiskRowMapper<LunDisk> {

        public static LunDiskRowMapper instance = new LunDiskRowMapper();

        private LunDiskRowMapper() {
        }

        @Override
        public LunDisk mapRow(ResultSet rs, int rowNum) throws SQLException {
            LunDisk disk = super.mapRow(rs, rowNum);
            disk.setVmEntityType(VmEntityType.valueOf(rs.getString("entity_type")));
            disk.setLun(LunDAODbFacadeImpl.MAPPER.mapRow(rs, rowNum));
            return disk;
        }

        @Override
        protected LunDisk createDiskEntity() {
            return new LunDisk();
        }
    }
}

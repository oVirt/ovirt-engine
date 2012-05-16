package org.ovirt.engine.core.dao;

import org.ovirt.engine.core.common.businessentities.BaseDisk;
import org.ovirt.engine.core.common.utils.EnumUtils;
import org.ovirt.engine.core.compat.Guid;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

public class BaseDiskDaoDbFacadeImpl extends DefaultGenericDaoDbFacade<BaseDisk, Guid> implements BaseDiskDao {

    public BaseDiskDaoDbFacadeImpl() {
        super("BaseDisk");
    }

    @Override
    protected MapSqlParameterSource createIdParameterMapper(Guid id) {
        return getCustomMapSqlParameterSource().addValue("disk_id", id);
    }

    @Override
    protected MapSqlParameterSource createFullParametersMapper(BaseDisk entity) {
        return createIdParameterMapper(entity.getId())
                .addValue("internal_drive_mapping", entity.getInternalDriveMapping())
                .addValue("disk_alias", entity.getDiskAlias())
                .addValue("disk_description", entity.getDiskDescription())
                .addValue("disk_interface", EnumUtils.nameOrNull(entity.getDiskInterface()))
                .addValue("wipe_after_delete", entity.isWipeAfterDelete())
                .addValue("propagate_errors", EnumUtils.nameOrNull(entity.getPropagateErrors()))
                .addValue("shareable", entity.isShareable())
                .addValue("boot", entity.isBoot())
                .addValue("allow_snapshot", entity.isAllowSnapshot());
    }

    @Override
    protected ParameterizedRowMapper<BaseDisk> createEntityRowMapper() {
        return BaseDiskRowMapper.instance;
    }

    @Override
    public boolean exists(Guid id) {
        return get(id) != null;
    }

    private static class BaseDiskRowMapper extends AbstractBaseDiskRowMapper<BaseDisk> {
        public static BaseDiskRowMapper instance = new BaseDiskRowMapper();

        private BaseDiskRowMapper() {
        }

        @Override
        protected BaseDisk createDiskEntity() {
            return new BaseDisk();
        }
    }
}

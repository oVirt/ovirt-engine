package org.ovirt.engine.core.dao;

import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.storage.BaseDisk;
import org.ovirt.engine.core.common.utils.EnumUtils;
import org.ovirt.engine.core.compat.Guid;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

@Named
@Singleton
public class BaseDiskDaoImpl extends DefaultGenericDao<BaseDisk, Guid> implements BaseDiskDao {

    public BaseDiskDaoImpl() {
        super("BaseDisk");
    }

    @Override
    protected MapSqlParameterSource createIdParameterMapper(Guid id) {
        return getCustomMapSqlParameterSource().addValue("disk_id", id);
    }

    @Override
    protected MapSqlParameterSource createFullParametersMapper(BaseDisk entity) {
        return createIdParameterMapper(entity.getId())
                .addValue("disk_alias", entity.getDiskAlias())
                .addValue("disk_description", entity.getDiskDescription())
                .addValue("wipe_after_delete", entity.isWipeAfterDelete())
                .addValue("propagate_errors", EnumUtils.nameOrNull(entity.getPropagateErrors()))
                .addValue("shareable", entity.isShareable())
                .addValue("sgio", entity.getSgio())
                .addValue("disk_storage_type", entity.getDiskStorageType())
                .addValue("cinder_volume_type", entity.getCinderVolumeType())
                .addValue("disk_content_type", entity.getContentType().getValue())
                .addValue("backup", entity.getBackup().getName())
                .addValue("backup_mode", EnumUtils.nameOrNull(entity.getBackupMode()));
    }

    @Override
    protected RowMapper<BaseDisk> createEntityRowMapper() {
        return BaseDiskRowMapper.instance;
    }

    @Override
    public boolean exists(Guid id) {
        return get(id) != null;
    }

    @Override public List<BaseDisk> getDisksByAlias(String alias) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("disk_alias", alias);
        return getCallsHandler().executeReadList("GetBaseDisksByAlias", BaseDiskRowMapper.instance, parameterSource);
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

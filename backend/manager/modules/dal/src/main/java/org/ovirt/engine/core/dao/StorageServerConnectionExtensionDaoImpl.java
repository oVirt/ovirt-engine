package org.ovirt.engine.core.dao;

import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.storage.StorageServerConnectionExtension;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacadeUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

@Named
@Singleton
public class StorageServerConnectionExtensionDaoImpl extends DefaultGenericDao<StorageServerConnectionExtension, Guid> implements StorageServerConnectionExtensionDao {

    public StorageServerConnectionExtensionDaoImpl() {
        super("StorageServerConnectionExtension");
        // This is needed since the max storage procedure name length is 64 chars and the default generated one exceeds that length
        setProcedureNameForGet("GetStorageServerConnectionExtensionById");
    }

    @Override
    public List<StorageServerConnectionExtension> getByHostId(Guid hostId) {
        return getCallsHandler().executeReadList("GetStorageServerConnectionExtensionsByHostId",
                createEntityRowMapper(), getCustomMapSqlParameterSource().addValue("vds_id", hostId));
    }

    @Override
    public StorageServerConnectionExtension getByHostIdAndTarget(Guid hostId, String target) {
        return getCallsHandler().executeRead("GetStorageServerConnectionExtensionsByHostIdAndTarget",
                createEntityRowMapper(),
                getCustomMapSqlParameterSource().addValue("vds_id", hostId).addValue("iqn", target));
    }

    @Override
    protected MapSqlParameterSource createFullParametersMapper(StorageServerConnectionExtension entity) {
        return createIdParameterMapper(entity.getId())
                .addValue("vds_id", entity.getHostId())
                .addValue("iqn", entity.getIqn())
                .addValue("user_name", entity.getUserName())
                .addValue("password", DbFacadeUtils.encryptPassword(entity.getPassword()));
    }

    @Override
    protected MapSqlParameterSource createIdParameterMapper(Guid id) {
        return getCustomMapSqlParameterSource().addValue("id", id);
    }

    @Override
    protected RowMapper<StorageServerConnectionExtension> createEntityRowMapper() {
        return (rs, rowNum) -> {
            StorageServerConnectionExtension ssce = new StorageServerConnectionExtension();
            ssce.setId(getGuidDefaultEmpty(rs, "id"));
            ssce.setHostId(getGuidDefaultEmpty(rs, "vds_id"));
            ssce.setIqn(rs.getString("iqn"));
            ssce.setUserName(rs.getString("user_name"));
            ssce.setPassword(DbFacadeUtils.decryptPassword(rs.getString("password")));
            return ssce;
        };
    }
}

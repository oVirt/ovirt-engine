package org.ovirt.engine.core.dao.gluster;

import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.gluster.StorageDevice;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.MassOperationsGenericDao;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

/**
 * Implementation of the DB Facade for Storage Device.
 */
@Named
@Singleton
public class StorageDeviceDaoImpl extends MassOperationsGenericDao<StorageDevice, Guid> implements
        StorageDeviceDao {

    private static final RowMapper<StorageDevice> storageDeviceRowMapper = (rs, rowNum) -> {
        StorageDevice entity = new StorageDevice();
        entity.setId(getGuidDefaultEmpty(rs, "id"));
        entity.setName(rs.getString("name"));
        entity.setDevUuid(rs.getString("device_uuid"));
        entity.setFsUuid(rs.getString("filesystem_uuid"));
        entity.setVdsId(getGuidDefaultEmpty(rs, "vds_id"));
        entity.setDescription(rs.getString("description"));
        entity.setDevType(rs.getString("device_type"));
        entity.setDevPath(rs.getString("device_path"));
        entity.setFsType(rs.getString("filesystem_type"));
        entity.setMountPoint(rs.getString("mount_point"));
        entity.setSize(rs.getLong("size"));
        entity.setCanCreateBrick(rs.getBoolean("is_free"));
        entity.setGlusterBrick(rs.getBoolean("is_gluster_brick"));
        return entity;
    };

    public StorageDeviceDaoImpl() {
        super("StorageDevice");
        setProcedureNameForGet("GetStorageDeviceById");
        setProcedureNameForRemove("DeleteStorageDeviceById");
    }

    @Override
    public StorageDevice get(Guid id) {
        return getCallsHandler().executeRead("GetStorageDeviceById", storageDeviceRowMapper,
                createIdParameterMapper(id));
    }

    @Override
    public List<StorageDevice> getStorageDevicesInHost(Guid hostId) {
        return getCallsHandler().executeReadList("GetStorageDevicesByVdsId",
                storageDeviceRowMapper,
                getCustomMapSqlParameterSource().addValue("vds_id", hostId));
    }

    @Override
    protected MapSqlParameterSource createFullParametersMapper(StorageDevice entity) {
        return getCustomMapSqlParameterSource()
                .addValue("id", entity.getId())
                .addValue("name", entity.getName())
                .addValue("device_uuid", entity.getDevUuid())
                .addValue("filesystem_uuid", entity.getFsUuid())
                .addValue("vds_id", entity.getVdsId())
                .addValue("description", entity.getDescription())
                .addValue("device_type", entity.getDevType())
                .addValue("device_path", entity.getDevPath())
                .addValue("filesystem_type", entity.getFsType())
                .addValue("mount_point", entity.getMountPoint())
                .addValue("size", entity.getSize())
                .addValue("is_free", entity.getCanCreateBrick())
                .addValue("is_gluster_brick", entity.isGlusterBrick());
    }

    @Override
    protected MapSqlParameterSource createIdParameterMapper(Guid id) {
        return getCustomMapSqlParameterSource().addValue("id", id);
    }

    @Override
    protected RowMapper<StorageDevice> createEntityRowMapper() {
        return storageDeviceRowMapper;
    }

    @Override
    public void remove(Guid id) {
        getCallsHandler().executeModification("DeleteStorageDeviceById",
                createIdParameterMapper(id));
    }

    @Override
    public void update(StorageDevice storageDevice) {
        getCallsHandler().executeModification("UpdateStorageDevice",
                getCustomMapSqlParameterSource()
                .addValue("id", storageDevice.getId())
                .addValue("name", storageDevice.getName())
                .addValue("device_uuid", storageDevice.getDevUuid())
                .addValue("filesystem_uuid", storageDevice.getFsUuid())
                .addValue("description", storageDevice.getDescription())
                .addValue("device_type", storageDevice.getDevType())
                .addValue("device_path", storageDevice.getDevPath())
                .addValue("filesystem_type", storageDevice.getFsType())
                .addValue("mount_point", storageDevice.getMountPoint())
                .addValue("size", storageDevice.getSize())
                .addValue("is_free", storageDevice.getCanCreateBrick()));

    }

    @Override
    public void updateIsFreeFlag(Guid deviceId, boolean isFree) {
        getCallsHandler().executeModification("UpdateIsFreeFlagById",
                getCustomMapSqlParameterSource()
                        .addValue("id", deviceId)
                        .addValue("is_free", isFree));
    }

}

package org.ovirt.engine.core.dao;

import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.UnregisteredDisk;
import org.ovirt.engine.core.common.businessentities.storage.UnregisteredDiskId;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacadeUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

@Named
@Singleton
public class UnregisteredDisksDaoImpl extends MassOperationsGenericDao<UnregisteredDisk, UnregisteredDiskId>
        implements UnregisteredDisksDao {

    public UnregisteredDisksDaoImpl() {
        super("unregistereddisk");
    }

    public UnregisteredDisksDaoImpl(String entityStoredProcedureName) {
        super(entityStoredProcedureName);
    }

    @Override
    public List<UnregisteredDisk> getByDiskIdAndStorageDomainId(Guid diskId, Guid storageDomainId) {
        List<UnregisteredDisk> unregisteredDisks =
                getCallsHandler().executeReadList("GetDiskByDiskIdAndStorageDomainId",
                        unregisteredDiskRowMapper,
                        createIdParameterMapper(new UnregisteredDiskId(diskId, storageDomainId)));
        for (UnregisteredDisk unregDisk : unregisteredDisks) {
            List<VmBase> vms = getCallsHandler().executeReadList("GetEntitiesByDiskId",
                    vmsForUnregisteredDiskRowMapper,
                    getCustomMapSqlParameterSource().addValue("disk_id", unregDisk.getDiskId()));
            unregDisk.getVms().addAll(vms);
        }
        return unregisteredDisks;
    }

    @Override
    public void removeUnregisteredDisk(Guid diskId, Guid storageDomainId) {
        getCallsHandler().executeModification("RemoveDiskFromUnregistered",
                createIdParameterMapper(new UnregisteredDiskId(diskId, storageDomainId)));
    }

    @Override
    public void removeUnregisteredDiskRelatedToVM(Guid vmId, Guid storageDomainId) {
        getCallsHandler().executeModification("RemoveDiskFromUnregisteredRelatedToVM", getCustomMapSqlParameterSource()
                .addValue("vm_id", vmId)
                .addValue("storage_domain_id", storageDomainId));
    }

    @Override
    public void saveUnregisteredDisk(UnregisteredDisk disk) {
        // OVF data is not included since it is being updated in the stored procedure.
        getCallsHandler().executeModification("InsertUnregisteredDisk",
                createFullParametersMapper(disk));

        for (VmBase vmBase : disk.getVms()) {
            getCallsHandler().executeModification("InsertUnregisteredDisksToVms",
                    createIdParameterMapper(disk.getId())
                            .addValue("entity_id", vmBase.getId())
                            .addValue("entity_name", vmBase.getName()));
        }
    }

    private static final RowMapper<UnregisteredDisk> unregisteredDiskRowMapper = (rs, rowNum) -> {
        UnregisteredDisk entity = new UnregisteredDisk();
        DiskImage diskImage = new DiskImage();
        diskImage.setId(getGuid(rs, "disk_id"));
        diskImage.setImageId(getGuid(rs, "image_id"));
        diskImage.setDiskAlias(rs.getString("disk_alias"));
        diskImage.setDiskDescription(rs.getString("disk_description"));
        diskImage.setActualSizeInBytes(rs.getLong("actual_size"));
        diskImage.setSize(rs.getLong("size"));
        diskImage.setCreationDate(DbFacadeUtils.fromDate(rs.getTimestamp("creation_date")));
        diskImage.setLastModified(DbFacadeUtils.fromDate(rs.getTimestamp("last_modified")));
        diskImage.setStorageIds(Guid.createGuidListFromString(rs.getString("storage_domain_id")));
        diskImage.setVolumeType(VolumeType.forValue(rs.getInt("volume_type")));
        diskImage.setVolumeFormat(VolumeFormat.forValue(rs.getInt("volume_format")));
        entity.setDiskImage(diskImage);
        entity.setId(new UnregisteredDiskId(diskImage.getId(), diskImage.getStorageIds().get(0)));
        return entity;
    };

    private static final RowMapper<VmBase> vmsForUnregisteredDiskRowMapper = (rs, rowNum) -> {
        VmBase vmBase = new VmBase();
        vmBase.setId(getGuidDefaultEmpty(rs, "entity_id"));
        vmBase.setName(rs.getString("entity_name"));
        return vmBase;
    };

    @Override
    protected MapSqlParameterSource createFullParametersMapper(UnregisteredDisk entity) {
        return createIdParameterMapper(entity.getId())
                .addValue("disk_alias", entity.getDiskAlias())
                .addValue("disk_description", entity.getDiskDescription())
                .addValue("creation_date", entity.getDiskImage().getCreationDate())
                .addValue("last_modified", entity.getDiskImage().getLastModified())
                .addValue("volume_type", entity.getDiskImage().getVolumeType())
                .addValue("volume_format", entity.getDiskImage().getVolumeFormat())
                .addValue("actual_size", entity.getDiskImage().getActualSizeInBytes())
                .addValue("size", entity.getDiskImage().getSize())
                .addValue("image_id", entity.getDiskImage().getImageId());

    }

    @Override
    protected MapSqlParameterSource createIdParameterMapper(UnregisteredDiskId unregisteredDiskId) {
        return getCustomMapSqlParameterSource()
                .addValue("disk_id", unregisteredDiskId.getDiskId())
                .addValue("storage_domain_id", unregisteredDiskId.getStorageDomainId());
    }

    @Override
    protected RowMapper<UnregisteredDisk> createEntityRowMapper() {
        return unregisteredDiskRowMapper;
    }
}

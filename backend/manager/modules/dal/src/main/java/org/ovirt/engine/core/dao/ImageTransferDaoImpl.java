package org.ovirt.engine.core.dao;

import java.util.Date;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.businessentities.storage.ImageTransfer;
import org.ovirt.engine.core.common.businessentities.storage.ImageTransferBackend;
import org.ovirt.engine.core.common.businessentities.storage.ImageTransferPhase;
import org.ovirt.engine.core.common.businessentities.storage.TimeoutPolicyType;
import org.ovirt.engine.core.common.businessentities.storage.TransferClientType;
import org.ovirt.engine.core.common.businessentities.storage.TransferType;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.compat.Guid;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

@Named
@Singleton
public class ImageTransferDaoImpl extends DefaultGenericDao<ImageTransfer, Guid> implements ImageTransferDao {

    public ImageTransferDaoImpl() {
        super("ImageUploads");
        setProcedureNameForGet("GetImageUploadsByCommandId");
        setProcedureNameForGetAll("GetAllFromImageUploads");
    }

    @Override
    public ImageTransfer getByDiskId(Guid diskId) {
        return getCallsHandler().executeRead("GetImageUploadsByDiskId",
                createEntityRowMapper(),
                createIdParameterMapper(diskId, "disk_id"));
    }

    @Override
    public List<ImageTransfer> getByVdsId(Guid vdsId) {
        return getCallsHandler().executeReadList("GetImageTransfersByVdsId",
                createEntityRowMapper(),
                createIdParameterMapper(vdsId, "vds_id"));
    }

    @Override
    public ImageTransfer get(Guid id, Guid userId, boolean isFiltered) {
        MapSqlParameterSource sqlParams = createIdParameterMapper(id);
        sqlParams.addValue("user_id", userId);
        sqlParams.addValue("is_filtered", isFiltered);

        return getCallsHandler().executeRead("GetImageUploadsByCommandId",
                createEntityRowMapper(),
                sqlParams);
    }

    @Override
    public ImageTransfer get(Guid id) {
        return get(id, null, false);
    }


    @Override
    public List<ImageTransfer> getByStorageId(Guid storageId) {
        return getCallsHandler().executeReadList("GetImageTransfersByStorageId",
                createEntityRowMapper(),
                createIdParameterMapper(storageId, "storage_id"));
    }

    @Override
    protected MapSqlParameterSource createIdParameterMapper(Guid id) {
        return createIdParameterMapper(id, "command_id");
    }

    protected MapSqlParameterSource createIdParameterMapper(Guid id, String paramName) {
        return getCustomMapSqlParameterSource().addValue(paramName, id);
    }

    @Override
    protected MapSqlParameterSource createFullParametersMapper(ImageTransfer entity) {
        MapSqlParameterSource mapper = createIdParameterMapper(entity.getId());
        mapper.addValue("command_id", entity.getId());
        mapper.addValue("command_type", entity.getCommandType());
        mapper.addValue("phase", entity.getPhase());
        mapper.addValue("type", entity.getType());
        mapper.addValue("active", entity.getActive());
        mapper.addValue("last_updated", entity.getLastUpdated());
        mapper.addValue("message", entity.getMessage());
        mapper.addValue("vds_id", entity.getVdsId() == null ? null : entity.getVdsId().toString());
        mapper.addValue("disk_id", entity.getDiskId() == null ? null : entity.getDiskId().toString());
        mapper.addValue("imaged_ticket_id", entity.getImagedTicketId() == null ? null : entity.getImagedTicketId().toString());
        mapper.addValue("proxy_uri", entity.getProxyUri());
        mapper.addValue("daemon_uri", entity.getDaemonUri());
        mapper.addValue("bytes_sent", entity.getBytesSent());
        mapper.addValue("bytes_total", entity.getBytesTotal());
        mapper.addValue("client_inactivity_timeout", entity.getClientInactivityTimeout());
        mapper.addValue("timeout_policy", entity.getTimeoutPolicy() == null ?
                TimeoutPolicyType.LEGACY.toString() : entity.getTimeoutPolicy().toString());
        mapper.addValue("image_format", entity.getImageFormat());
        mapper.addValue("backend", entity.getBackend());
        mapper.addValue("backup_id", entity.getBackupId());
        mapper.addValue("client_type", entity.getTransferClientType() == null ?
                TransferClientType.UNKNOWN.getValue() : entity.getTransferClientType().getValue());
        mapper.addValue("shallow", entity.isShallow() == null ? false : entity.isShallow());
        return mapper;
    }

    @Override
    protected RowMapper<ImageTransfer> createEntityRowMapper() {
        return (rs, rowNum) -> {
            ImageTransfer entity = new ImageTransfer();
            entity.setId(getGuidDefaultEmpty(rs, "command_id"));
            entity.setCommandType(ActionType.forValue(rs.getInt("command_type")));
            entity.setPhase(ImageTransferPhase.forValue(rs.getInt("phase")));
            entity.setType(TransferType.forValue(rs.getInt("type")));
            entity.setActive(rs.getBoolean("active"));
            entity.setLastUpdated(new Date(rs.getTimestamp("last_updated").getTime()));
            entity.setMessage(rs.getString("message"));
            entity.setVdsId(getGuid(rs, "vds_id"));
            entity.setDiskId(getGuid(rs, "disk_id"));
            entity.setImagedTicketId(getGuid(rs, "imaged_ticket_id"));
            entity.setProxyUri(rs.getString("proxy_uri"));
            entity.setDaemonUri(rs.getString("daemon_uri"));
            entity.setBytesSent(rs.getLong("bytes_sent"));
            entity.setBytesTotal(rs.getLong("bytes_total"));
            entity.setClientInactivityTimeout((Integer) rs.getObject("client_inactivity_timeout"));
            entity.setTimeoutPolicy(TimeoutPolicyType.forString(rs.getString("timeout_policy")));
            entity.setImageFormat(VolumeFormat.forValue(rs.getInt("image_format")));
            entity.setBackend(ImageTransferBackend.forValue(rs.getInt("backend")));
            entity.setBackupId(getGuid(rs, "backup_id"));
            entity.setTransferClientType(TransferClientType.forValue(rs.getInt("client_type")));
            entity.setShallow(rs.getBoolean("shallow"));
            return entity;
        };
    }

    @Override
    public List<ImageTransfer> getAllWithQuery(String query) {
        return getJdbcTemplate().query(query, createEntityRowMapper());
    }

    @Override
    public void deleteCompletedImageTransfers(Date succeededImageTransfers, Date failedImageTransfers) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("succeeded_end_time", succeededImageTransfers)
                .addValue("failed_end_time", failedImageTransfers);
        getCallsHandler().executeModification("DeleteCompletedImageTransfersOlderThanDate", parameterSource);
    }
}

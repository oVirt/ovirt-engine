package org.ovirt.engine.core.dao;

import java.util.Date;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.storage.ImageTransfer;
import org.ovirt.engine.core.common.businessentities.storage.ImageTransferPhase;
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
                createDiskIdParameterMapper(diskId));
    }

    @Override
    protected MapSqlParameterSource createIdParameterMapper(Guid id) {
        return getCustomMapSqlParameterSource().addValue("command_id", id);
    }

    protected MapSqlParameterSource createDiskIdParameterMapper(Guid diskId) {
        return getCustomMapSqlParameterSource().addValue("disk_id", diskId);
    }

    @Override
    protected MapSqlParameterSource createFullParametersMapper(ImageTransfer entity) {
        MapSqlParameterSource mapper = createIdParameterMapper(entity.getId());
        mapper.addValue("command_id", entity.getId());
        mapper.addValue("command_type", entity.getCommandType());
        mapper.addValue("phase", entity.getPhase());
        mapper.addValue("last_updated", entity.getLastUpdated());
        mapper.addValue("message", entity.getMessage());
        mapper.addValue("vds_id", entity.getVdsId() == null ? null : entity.getVdsId().toString());
        mapper.addValue("disk_id", entity.getDiskId() == null ? null : entity.getDiskId().toString());
        mapper.addValue("imaged_ticket_id", entity.getImagedTicketId() == null ? null : entity.getImagedTicketId().toString());
        mapper.addValue("proxy_uri", entity.getProxyUri());
        mapper.addValue("signed_ticket", entity.getSignedTicket());
        mapper.addValue("bytes_sent", entity.getBytesSent());
        mapper.addValue("bytes_total", entity.getBytesTotal());
        return mapper;
    }

    @Override
    protected RowMapper<ImageTransfer> createEntityRowMapper() {
        return (rs, rowNum) -> {
            ImageTransfer entity = new ImageTransfer();
            entity.setId(getGuidDefaultEmpty(rs, "command_id"));
            entity.setCommandType(VdcActionType.forValue(rs.getInt("command_type")));
            entity.setPhase(ImageTransferPhase.forValue(rs.getInt("phase")));
            entity.setLastUpdated(new Date(rs.getTimestamp("last_updated").getTime()));
            entity.setMessage(rs.getString("message"));
            entity.setVdsId(getGuid(rs, "vds_id"));
            entity.setDiskId(getGuid(rs, "disk_id"));
            entity.setImagedTicketId(getGuid(rs, "imaged_ticket_id"));
            entity.setProxyUri(rs.getString("proxy_uri"));
            entity.setSignedTicket(rs.getString("signed_ticket"));
            entity.setBytesSent(rs.getLong("bytes_sent"));
            entity.setBytesTotal(rs.getLong("bytes_total"));
            return entity;
        };
    }

    @Override
    public List<ImageTransfer> getAllWithQuery(String query) {
        return getJdbcTemplate().query(query, createEntityRowMapper());
    }
}

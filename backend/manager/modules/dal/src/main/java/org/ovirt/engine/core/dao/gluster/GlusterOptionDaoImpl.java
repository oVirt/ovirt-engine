package org.ovirt.engine.core.dao.gluster;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeOptionEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.MassOperationsGenericDao;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

@Named
@Singleton
public class GlusterOptionDaoImpl extends MassOperationsGenericDao<GlusterVolumeOptionEntity, Guid> implements GlusterOptionDao {
    private static final RowMapper<GlusterVolumeOptionEntity> optionRowMapper = new VolumeOptionRowMapper();

    public GlusterOptionDaoImpl() {
        super("GlusterOption");
        setProcedureNameForGet("GetGlusterOptionById");
    }

    @Override
    public void save(GlusterVolumeOptionEntity option) {
        getCallsHandler().executeModification("InsertGlusterVolumeOption", createVolumeOptionParams(option));
    }

    @Override
    public void updateVolumeOption(Guid optionId, String optionValue) {
        getCallsHandler().executeModification("UpdateGlusterVolumeOption",
                getCustomMapSqlParameterSource()
                        .addValue("id", optionId)
                        .addValue("option_val", optionValue));
    }

    @Override
    public void removeVolumeOption(Guid optionId) {
        getCallsHandler().executeModification("DeleteGlusterVolumeOption",
                getCustomMapSqlParameterSource().addValue("id", optionId));
    }

    @Override
    public void removeAll(Collection<Guid> ids) {
        getCallsHandler().executeModification("DeleteGlusterVolumeOptions",
                getCustomMapSqlParameterSource().addValue("ids", StringUtils.join(ids, ',')));
    }

    @Override
    public GlusterVolumeOptionEntity getById(Guid id) {
        return getCallsHandler().executeRead("GetGlusterOptionById", optionRowMapper, createIdParameterMapper(id));
    }

    @Override
    public List<GlusterVolumeOptionEntity> getOptionsOfVolume(Guid volumeId) {
        return getCallsHandler().executeReadList(
                "GetOptionsByGlusterVolumeGuid", optionRowMapper,
                getCustomMapSqlParameterSource().addValue("volume_id", volumeId));
    }

    private MapSqlParameterSource createVolumeOptionParams(GlusterVolumeOptionEntity option) {
        return getCustomMapSqlParameterSource()
                .addValue("id", option.getId())
                .addValue("volume_id", option.getVolumeId())
                .addValue("option_key", option.getKey())
                .addValue("option_val", option.getValue());
    }

    private static final class VolumeOptionRowMapper implements RowMapper<GlusterVolumeOptionEntity> {
        @Override
        public GlusterVolumeOptionEntity mapRow(ResultSet rs, int rowNum)
                throws SQLException {
            GlusterVolumeOptionEntity option = new GlusterVolumeOptionEntity();
            option.setId(getGuidDefaultEmpty(rs, "id"));
            option.setVolumeId(getGuidDefaultEmpty(rs, "volume_id"));
            option.setKey(rs.getString("option_key"));
            option.setValue(rs.getString("option_val"));
            return option;
        }
    }

    @Override
    protected MapSqlParameterSource createFullParametersMapper(GlusterVolumeOptionEntity option) {
        return createVolumeOptionParams(option);
    }

    @Override
    protected MapSqlParameterSource createIdParameterMapper(Guid id) {
        return getCustomMapSqlParameterSource().addValue("id", id);
    }

    @Override
    protected RowMapper<GlusterVolumeOptionEntity> createEntityRowMapper() {
        return optionRowMapper;
    }
}

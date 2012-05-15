package org.ovirt.engine.core.dao.gluster;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeOptionEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.BaseDAODbFacade;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

public class GlusterOptionDaoDbFacadeImpl extends BaseDAODbFacade implements GlusterOptionDao {
    private static final ParameterizedRowMapper<GlusterVolumeOptionEntity> optionRowMapper = new VolumeOptionRowMapper();

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
    public GlusterVolumeOptionEntity getById(Guid id) {
        return getCallsHandler().executeRead(
                "GetGlusterOptionById", optionRowMapper,
                getCustomMapSqlParameterSource().addValue("id", id));
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

    private static final class VolumeOptionRowMapper implements ParameterizedRowMapper<GlusterVolumeOptionEntity> {
        @Override
        public GlusterVolumeOptionEntity mapRow(ResultSet rs, int rowNum)
                throws SQLException {
            GlusterVolumeOptionEntity option = new GlusterVolumeOptionEntity();
            option.setId(Guid.createGuidFromString(rs.getString("id")));
            option.setVolumeId(Guid.createGuidFromString(rs.getString("volume_id")));
            option.setKey(rs.getString("option_key"));
            option.setValue(rs.getString("option_val"));
            return option;
        }
    }
}

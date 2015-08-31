package org.ovirt.engine.core.dao.gluster;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSnapshotConfig;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.BaseDao;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

@Named
@Singleton
public class GlusterVolumeSnapshotConfigDaoImpl extends BaseDao implements GlusterVolumeSnapshotConfigDao {

    private static final RowMapper<GlusterVolumeSnapshotConfig> snapshotConfigRowMapper =
            new GlusterVolumeSnapshotConfigRowMapper();

    public void save(GlusterVolumeSnapshotConfig config) {
        getCallsHandler().executeModification("InsertGlusterVolumeSnapshotConfig", createFullParametersMapper(config));
    }

    public List<GlusterVolumeSnapshotConfig> getConfigByClusterId(Guid clusterId) {
        List<GlusterVolumeSnapshotConfig> configs =
                getCallsHandler().executeReadList("GetGlusterVolumeSnapshotConfigByClusterId", snapshotConfigRowMapper,
                        getCustomMapSqlParameterSource()
                                .addValue("cluster_id", clusterId));
        return configs;
    }

    public List<GlusterVolumeSnapshotConfig> getConfigByVolumeId(Guid clusterId, Guid volumeId) {
        List<GlusterVolumeSnapshotConfig> configs =
                getCallsHandler().executeReadList("GetGlusterVolumeSnapshotConfigByVolumeId", snapshotConfigRowMapper,
                        getCustomMapSqlParameterSource()
                                .addValue("cluster_id", clusterId)
                                .addValue("volume_id", volumeId));
        return configs;
    }

    public GlusterVolumeSnapshotConfig getConfigByClusterIdAndName(Guid clusterId,
            String paramName) {
        GlusterVolumeSnapshotConfig config =
                getCallsHandler().executeRead("GetGlusterVolumeSnapshotConfigByClusterIdAndName",
                        snapshotConfigRowMapper,
                        getCustomMapSqlParameterSource()
                                .addValue("cluster_id", clusterId)
                                .addValue("param_name", paramName));
        return config;
    }

    public GlusterVolumeSnapshotConfig getConfigByVolumeIdAndName(Guid clusterId,
            Guid volumeId,
            String paramName) {
        GlusterVolumeSnapshotConfig config =
                getCallsHandler().executeRead("GetGlusterVolumeSnapshotConfigByVolumeIdAndName",
                        snapshotConfigRowMapper,
                        getCustomMapSqlParameterSource()
                                .addValue("cluster_id", clusterId)
                                .addValue("volume_id", volumeId)
                                .addValue("param_name", paramName));

        return config;
    }

    private static final class GlusterVolumeSnapshotConfigRowMapper implements RowMapper<GlusterVolumeSnapshotConfig> {
        @Override
        public GlusterVolumeSnapshotConfig mapRow(ResultSet rs, int rowNum)
                throws SQLException {
            GlusterVolumeSnapshotConfig config = new GlusterVolumeSnapshotConfig();
            config.setClusterId(getGuidDefaultEmpty(rs, "cluster_id"));
            config.setVolumeId(getGuidDefaultEmpty(rs, "volume_id"));
            config.setParamName(rs.getString("param_name"));
            config.setParamValue(rs.getString("param_value"));
            return config;
        }
    }

    protected MapSqlParameterSource createFullParametersMapper(GlusterVolumeSnapshotConfig config) {
        return getCustomMapSqlParameterSource()
                .addValue("cluster_id", config.getClusterId())
                .addValue("volume_id", config.getVolumeId())
                .addValue("param_name", config.getParamName())
                .addValue("param_value", config.getParamValue());
    }

    @Override
    public List<GlusterVolumeSnapshotConfig> getAllWithQuery(String query) {
        List<GlusterVolumeSnapshotConfig> configs = getJdbcTemplate().query(query, snapshotConfigRowMapper);
        return configs;
    }

    @Override
    public void updateConfigByClusterIdAndName(Guid clusterId, String paramName, String paramValue) {
        getCallsHandler().executeModification("UpdateConfigByClusterIdAndName",
                getCustomMapSqlParameterSource()
                        .addValue("cluster_id", clusterId)
                        .addValue("param_name", paramName)
                        .addValue("param_value", paramValue));
    }

    @Override
    public void updateConfigByVolumeIdAndName(Guid clusterId, Guid volumeId, String paramName, String paramValue) {
        getCallsHandler().executeModification("UpdateConfigByVolumeIdIdAndName",
                getCustomMapSqlParameterSource()
                        .addValue("cluster_id", clusterId)
                        .addValue("volume_id", volumeId)
                        .addValue("param_name", paramName)
                        .addValue("param_value", paramValue));
    }
}

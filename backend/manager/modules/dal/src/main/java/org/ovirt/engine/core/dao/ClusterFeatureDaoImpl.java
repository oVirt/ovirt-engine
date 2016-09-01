package org.ovirt.engine.core.dao;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.AdditionalFeature;
import org.ovirt.engine.core.common.businessentities.SupportedAdditionalClusterFeature;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.compat.Guid;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

@Named
@Singleton
public class ClusterFeatureDaoImpl extends BaseDao implements ClusterFeatureDao {
    private static final RowMapper<AdditionalFeature> clusterFeatureRowMapper = (rs, rowNum) -> {
        AdditionalFeature feature = new AdditionalFeature();
        feature.setId(getGuidDefaultEmpty(rs, "feature_id"));
        feature.setName(rs.getString("feature_name"));
        feature.setDescription(rs.getString("description"));
        feature.setCategory(ApplicationMode.from(rs.getInt("category")));
        feature.setVersion(new VersionRowMapper("version").mapRow(rs, rowNum));
        return feature;
    };

    private static final RowMapper<SupportedAdditionalClusterFeature> supportedClusterFeatureRowMapper = (rs, rowNum) -> {
        AdditionalFeature feature = clusterFeatureRowMapper.mapRow(rs, rowNum);
        SupportedAdditionalClusterFeature supportedClusterFeature = new SupportedAdditionalClusterFeature();
        supportedClusterFeature.setFeature(feature);
        supportedClusterFeature.setClusterId(getGuidDefaultEmpty(rs, "cluster_id"));
        supportedClusterFeature.setEnabled(rs.getBoolean("is_enabled"));
        return supportedClusterFeature;
    };

    @Override
    public void addSupportedClusterFeature(SupportedAdditionalClusterFeature feature) {
        getCallsHandler().executeModification("InsertSupportedClusterFeature",
                createSupportedClusterFeatureParameterMapper(feature));
    }

    @Override
    public void updateSupportedClusterFeature(SupportedAdditionalClusterFeature feature) {
        getCallsHandler().executeModification("UpdateSupportedClusterFeature",
                createSupportedClusterFeatureParameterMapper(feature));
    }

    private MapSqlParameterSource createSupportedClusterFeatureParameterMapper(SupportedAdditionalClusterFeature clusterFeature) {
        return getCustomMapSqlParameterSource()
                .addValue("cluster_id", clusterFeature.getClusterId())
                .addValue("feature_id", clusterFeature.getFeature().getId())
                .addValue("is_enabled", clusterFeature.isEnabled());
    }

    @Override
    public void addAllSupportedClusterFeature(Collection<SupportedAdditionalClusterFeature> features) {
        getCallsHandler().executeStoredProcAsBatch("InsertSupportedClusterFeature",
                features,
                this::createSupportedClusterFeatureParameterMapper);
    }

    @Override
    public Set<SupportedAdditionalClusterFeature> getSupportedFeaturesByClusterId(Guid clusterId) {
        List<SupportedAdditionalClusterFeature> features =
                getCallsHandler().executeReadList("GetSupportedClusterFeaturesByClusterId",
                supportedClusterFeatureRowMapper,
                getCustomMapSqlParameterSource().addValue("cluster_id", clusterId));
        return new HashSet<>(features);
    }

    @Override
    public Set<AdditionalFeature> getClusterFeaturesForVersionAndCategory(String version, ApplicationMode category) {
        List<AdditionalFeature> features =
                getCallsHandler().executeReadList("GetClusterFeaturesByVersionAndCategory",
                clusterFeatureRowMapper,
                getCustomMapSqlParameterSource().addValue("version", version)
                        .addValue("category", category));
        return new HashSet<>(features);
    }

}

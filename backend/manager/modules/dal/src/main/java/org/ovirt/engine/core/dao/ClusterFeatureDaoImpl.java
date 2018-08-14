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
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

@Named
@Singleton
public class ClusterFeatureDaoImpl extends DefaultGenericDao<SupportedAdditionalClusterFeature, Pair<Guid, Guid>> implements ClusterFeatureDao {

    public ClusterFeatureDaoImpl() {
        super("SupportedClusterFeature");
        setProcedureNameForGet("GetSupportedClusterFeature");
        setProcedureNameForGetAll("GetAllSupportedClusterFeatures");
    }

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
    protected MapSqlParameterSource createIdParameterMapper(Pair<Guid, Guid> id) {
        Guid featureId = id.getFirst();
        Guid clusterId = id.getSecond();

        return getCustomMapSqlParameterSource()
                .addValue("feature_id", featureId)
                .addValue("cluster_id", clusterId);
    }

    @Override
    protected MapSqlParameterSource createFullParametersMapper(SupportedAdditionalClusterFeature entity) {
        return getCustomMapSqlParameterSource()
                .addValue("cluster_id", entity.getClusterId())
                .addValue("feature_id", entity.getFeature().getId())
                .addValue("is_enabled", entity.isEnabled());
    }

    @Override
    protected RowMapper<SupportedAdditionalClusterFeature> createEntityRowMapper() {
        return supportedClusterFeatureRowMapper;
    }

    @Override
    public void saveAll(Collection<SupportedAdditionalClusterFeature> features) {
        getCallsHandler().executeStoredProcAsBatch(getProcedureNameForSave(),
                features,
                this::createFullParametersMapper);
    }

    @Override
    public Set<SupportedAdditionalClusterFeature> getAllByClusterId(Guid clusterId) {
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

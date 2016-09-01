package org.ovirt.engine.core.dao;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.compat.Guid;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

@Named
@Singleton
public class SupportedHostFeatureDaoImpl extends BaseDao implements SupportedHostFeatureDao {

    private static final RowMapper<String> supportedHostFeatureRowMapper = (rs, rowNum) -> rs.getString("feature_name");

    private MapSqlParameterSource createSupportedHostFeatureParameterMapper(String feature, Guid hostId) {
        return getCustomMapSqlParameterSource()
                .addValue("host_id", hostId)
                .addValue("feature_name", feature);
    }

    @Override
    public void addSupportedHostFeature(Guid hostId, String feature) {
        getCallsHandler().executeModification("InsertSupportedHostFeature",
                createSupportedHostFeatureParameterMapper(feature, hostId));
    }

    @Override
    public void addAllSupportedHostFeature(final Guid hostId, Set<String> features) {
        getCallsHandler().executeStoredProcAsBatch("InsertSupportedHostFeature", features,
                feature -> createSupportedHostFeatureParameterMapper(feature, hostId));
    }

    @Override
    public Set<String> getSupportedHostFeaturesByHostId(Guid hostId) {
        List<String> featureList = getCallsHandler().executeReadList("GetSupportedHostFeaturesByHostId",
                supportedHostFeatureRowMapper,
                getCustomMapSqlParameterSource().addValue("host_id", hostId));
        return new HashSet<>(featureList);
    }

    @Override
    public void removeAllSupportedHostFeature(final Guid hostId, Set<String> features) {
        getCallsHandler().executeStoredProcAsBatch("RemoveSupportedHostFeature", features,
                feature -> createSupportedHostFeatureParameterMapper(feature, hostId));

    }
}

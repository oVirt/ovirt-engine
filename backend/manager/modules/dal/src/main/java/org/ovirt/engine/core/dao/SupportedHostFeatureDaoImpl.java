package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.MapSqlParameterMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

@Named
@Singleton
public class SupportedHostFeatureDaoImpl extends BaseDao implements SupportedHostFeatureDao {

    private static class SupportedHostFeatureRowMapper implements RowMapper<String> {
        public static final SupportedHostFeatureRowMapper instance = new SupportedHostFeatureRowMapper();
        @Override
        public String mapRow(ResultSet rs, int rowNum) throws SQLException {
            return rs.getString("feature_name");
        }
    }

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
                new MapSqlParameterMapper<String>() {
                    @Override
                    public MapSqlParameterSource map(String feature) {
                        return createSupportedHostFeatureParameterMapper(feature, hostId);
                    }
                });
    }

    @Override
    public Set<String> getSupportedHostFeaturesByHostId(Guid hostId) {
        List<String> featureList = getCallsHandler().executeReadList("GetSupportedHostFeaturesByHostId",
                SupportedHostFeatureRowMapper.instance,
                getCustomMapSqlParameterSource().addValue("host_id", hostId));
        return new HashSet<>(featureList);
    }

    @Override
    public void removeAllSupportedHostFeature(final Guid hostId, Set<String> features) {
        getCallsHandler().executeStoredProcAsBatch("RemoveSupportedHostFeature", features,
                new MapSqlParameterMapper<String>() {
                    @Override
                    public MapSqlParameterSource map(String feature) {
                        return createSupportedHostFeatureParameterMapper(feature, hostId);
                    }
                });

    }
}

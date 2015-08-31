package org.ovirt.engine.core.dao.profiles;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.profiles.DiskProfile;
import org.ovirt.engine.core.compat.Guid;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

@Named
@Singleton
public class DiskProfileDaoImpl extends ProfileBaseDaoImpl<DiskProfile> implements DiskProfileDao {
    private static final DiskProfileDaoDbFacadaeImplMapper MAPPER = new DiskProfileDaoDbFacadaeImplMapper();

    public DiskProfileDaoImpl() {
        super("DiskProfile");
    }

    @Override
    public List<DiskProfile> getAllForStorageDomain(Guid storageDomainId) {
        return getAllForStorageDomain(storageDomainId, null, false);
    }

    @Override
    public List<DiskProfile> getAllForStorageDomain(Guid storageDomainId, Guid userId, boolean isFiltered) {
        return getCallsHandler().executeReadList("GetDiskProfilesByStorageDomainId",
                createEntityRowMapper(),
                getCustomMapSqlParameterSource().addValue("storage_domain_id", storageDomainId)
                        .addValue("user_id", userId)
                        .addValue("is_filtered", isFiltered));
    }

    @Override
    public void nullifyQosForStorageDomain(Guid storageDomainId) {
        getCallsHandler().executeModification("nullifyQosForStorageDomain",
                getCustomMapSqlParameterSource().addValue("storage_domain_id", storageDomainId.getUuid()));
    }

    @Override
    public List<DiskProfile> getAllForQos(Guid qosId) {
        return getCallsHandler().executeReadList("GetDiskProfilesByQosId",
                createEntityRowMapper(),
                getCustomMapSqlParameterSource().addValue("qos_id", qosId));
    }

    @Override
    protected RowMapper<DiskProfile> createEntityRowMapper() {
        return MAPPER;
    }

    @Override
    protected MapSqlParameterSource createFullParametersMapper(DiskProfile obj) {
        MapSqlParameterSource map = super.createFullParametersMapper(obj);
        map.addValue("storage_domain_id", obj.getStorageDomainId());
        return map;
    }

    protected static class DiskProfileDaoDbFacadaeImplMapper extends ProfileBaseDaoFacadaeImplMapper<DiskProfile> {

        @Override
        protected DiskProfile createProfileEntity(ResultSet rs) throws SQLException {
            DiskProfile diskProfile = new DiskProfile();
            diskProfile.setStorageDomainId(getGuid(rs, "storage_domain_id"));
            return diskProfile;
        }

    }
}

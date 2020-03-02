package org.ovirt.engine.core.dao;

import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.StorageDomainDR;
import org.ovirt.engine.core.compat.Guid;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

/**
 * {@code StorageDomainDRDaoImpl} provides an implementation of {@link StorageDomainDRDao}.
 */
@Named
@Singleton
@SuppressWarnings("synthetic-access")
public class StorageDomainDRDaoImpl extends BaseDao implements StorageDomainDRDao {


    /**
     * Row mapper to map a returned row to a {@link StorageDomainDR} object.
     */
    private static final RowMapper<StorageDomainDR> storageDomainDRRowMapper = (rs, rowNum) -> {
        final StorageDomainDR entity = new StorageDomainDR();
        entity.setStorageDomainId(getGuidDefaultEmpty(rs, "storage_domain_id"));
        entity.setGeoRepSessionId(getGuidDefaultEmpty(rs, "georep_session_id"));
        entity.setScheduleCronExpression(rs.getString("sync_schedule"));
        entity.setJobId(Guid.createGuidFromString(rs.getString("gluster_scheduler_job_id")));
        return entity;
    };

    private MapSqlParameterSource createFullParametersSource(StorageDomainDR storageDomainDR) {
        return getCustomMapSqlParameterSource().addValue("storage_domain_id", storageDomainDR.getStorageDomainId())
                .addValue("georep_session_id", storageDomainDR.getGeoRepSessionId())
                .addValue("sync_schedule", storageDomainDR.getScheduleCronExpression())
                .addValue("gluster_scheduler_job_id", storageDomainDR.getJobId());
    }

    @Override
    public StorageDomainDR get(Guid storageDomainId, Guid georepSessionId) {
        return getCallsHandler().executeRead(
                "GetStorageDomainDR", storageDomainDRRowMapper,
                getCustomMapSqlParameterSource().addValue("storage_domain_id", storageDomainId)
                .addValue("georep_session_id", georepSessionId));
    }

    @Override
    public List<StorageDomainDR> getAllForStorageDomain(Guid storageDomainId) {
        return getCallsHandler().executeReadList(
                "GetStorageDomainDRList", storageDomainDRRowMapper,
                getCustomMapSqlParameterSource().addValue("storage_domain_id", storageDomainId));
    }


    @Override
    public List<StorageDomainDR> getWithGeoRepSession(Guid geoRepSessionId) {
        return getCallsHandler().executeReadList(
                "GetStorageDomainDRWithGeoRep", storageDomainDRRowMapper,
                getCustomMapSqlParameterSource().addValue("georep_session_id", geoRepSessionId));
    }

    @Override
    public void save(StorageDomainDR storageDomainDR) {
        getCallsHandler().executeModification("InsertStorageDomainDR", createFullParametersSource(storageDomainDR));
    }

    @Override
    public void update(StorageDomainDR storageDomainDR) {
        getCallsHandler().executeModification("UpdateStorageDomainDR", createFullParametersSource(storageDomainDR));
    }

    @Override
    public void saveOrUpdate(StorageDomainDR storageDomainDR) {
       if (get(storageDomainDR.getStorageDomainId(), storageDomainDR.getGeoRepSessionId()) != null) {
           update(storageDomainDR);
       } else {
           save(storageDomainDR);
       }

    }

    @Override
    public void remove(Guid storageDomainId, Guid georepSessionId) {
        getCallsHandler().executeModification("DeleteStorageDomainDR",
                getCustomMapSqlParameterSource().addValue("storage_domain_id", storageDomainId)
                        .addValue("georep_session_id", georepSessionId));
    }
}

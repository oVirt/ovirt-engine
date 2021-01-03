package org.ovirt.engine.core.dao;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.storage.ExternalLease;
import org.ovirt.engine.core.compat.Guid;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

@Named
@Singleton
public class ExternalLeaseDaoImpl extends DefaultGenericDao<ExternalLease, Guid> implements ExternalLeaseDao {

    public ExternalLeaseDaoImpl() {
        super("ExternalLease");
        setProcedureNameForGet("GetExternalLease");
    }

    @Override
    protected MapSqlParameterSource createFullParametersMapper(ExternalLease lease) {
        return createIdParameterMapper(lease.getId())
                .addValue("storage_domain_id", lease.getStorageDomainId());
    }

    @Override
    protected MapSqlParameterSource createIdParameterMapper(Guid guid) {
        return getCustomMapSqlParameterSource().addValue("lease_id", guid);
    }

    @Override
    protected RowMapper<ExternalLease> createEntityRowMapper() {
        return externalLeaseRowMapper;
    }

    private static final RowMapper<ExternalLease> externalLeaseRowMapper = (rs, rowNum) -> {
        ExternalLease entity = new ExternalLease();
        entity.setId(getGuidDefaultNewGuid(rs, "lease_id"));
        entity.setStorageDomainId(getGuid(rs, "storage_domain_id"));
        return entity;
    };

}

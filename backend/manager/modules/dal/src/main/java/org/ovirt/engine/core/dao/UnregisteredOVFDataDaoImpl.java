package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.OvfEntityData;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.springframework.jdbc.core.RowMapper;

@Named
@Singleton
public class UnregisteredOVFDataDaoImpl extends BaseDao implements UnregisteredOVFDataDao {

    @Override
    public List<OvfEntityData> getAllForStorageDomainByEntityType(Guid storageDomainId, VmEntityType entityType) {
        return getCallsHandler().executeReadList("GetAllOVFEntitiesForStorageDomainByEntityType",
                OvfEntityDataRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("storage_domain_id", storageDomainId)
                        .addValue("entity_type", entityType != null ? entityType.name() : null));
    }

    @Override
    public List<OvfEntityData> getByEntityIdAndStorageDomain(Guid entityId, Guid storageDomainId) {
        return getCallsHandler().executeReadList("GetOVFDataByEntityIdAndStorageDomain",
                OvfEntityDataRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("entity_guid", entityId)
                        .addValue("storage_domain_id", storageDomainId));
    }

    @Override
    public void removeEntity(Guid entityId, Guid storageDomainId) {
        getCallsHandler().executeModification("RemoveEntityFromUnregistered", getCustomMapSqlParameterSource()
                .addValue("entity_guid", entityId)
                .addValue("storage_domain_id", storageDomainId));
    }

    @Override
    public void saveOVFData(OvfEntityData ovfEntityData) {
        // OVF data is not included since it is being updated in the stored procedure.
        getCallsHandler().executeModification("InsertOVFDataForEntities",
                getCustomMapSqlParameterSource()
                        .addValue("entity_guid", ovfEntityData.getEntityId())
                        .addValue("entity_name", ovfEntityData.getEntityName())
                        .addValue("entity_type", ovfEntityData.getEntityType().name())
                        .addValue("architecture",
                                ovfEntityData.getArchitecture() != null ? ovfEntityData.getArchitecture().getValue()
                                        : null)
                        .addValue("lowest_comp_version",
                                ovfEntityData.getLowestCompVersion() != null ? ovfEntityData.getLowestCompVersion()
                                        .getValue() : null)
                        .addValue("storage_domain_id", ovfEntityData.getStorageDomainId())
                        .addValue("ovf_data", ovfEntityData.getOvfData())
                        .addValue("ovf_extra_data", ovfEntityData.getOvfExtraData()));
    }

    private static class OvfEntityDataRowMapper implements RowMapper<OvfEntityData> {
        public static final OvfEntityDataRowMapper instance = new OvfEntityDataRowMapper();

        @Override
        public OvfEntityData mapRow(ResultSet rs, int rowNum) throws SQLException {
            OvfEntityData entity = new OvfEntityData();
            entity.setEntityId(getGuid(rs, "entity_guid"));
            entity.setEntityName(rs.getString("entity_name"));
            entity.setEntityType(VmEntityType.valueOf(rs.getString("entity_type")));
            entity.setArchitecture(ArchitectureType.forValue(rs.getInt("architecture")));
            entity.setLowestCompVersion(new Version(rs.getString("lowest_comp_version")));
            entity.setStorageDomainId(getGuid(rs, "storage_domain_id"));
            entity.setOvfData(rs.getString("ovf_data"));
            entity.setOvfExtraData(rs.getString("ovf_extra_data"));
            return entity;
        }
    }
}

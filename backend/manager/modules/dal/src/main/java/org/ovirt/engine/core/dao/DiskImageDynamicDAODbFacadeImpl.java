package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.storage.DiskImageDynamic;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.MapSqlParameterMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

/**
 * JDBC template based implementation of DiskImageDynamicDAO
 *
 */
@Named
@Singleton
public class DiskImageDynamicDAODbFacadeImpl extends MassOperationsGenericDaoDbFacade<DiskImageDynamic, Guid>
        implements DiskImageDynamicDAO {

    private static final class DiskImageDynamicRowMapper implements RowMapper<DiskImageDynamic> {
        public static final DiskImageDynamicRowMapper instance = new DiskImageDynamicRowMapper();

        @Override
        public DiskImageDynamic mapRow(ResultSet rs, int rowNum)
                throws SQLException {
            DiskImageDynamic entity = new DiskImageDynamic();
            entity.setId(getGuidDefaultEmpty(rs, "image_id"));
            entity.setread_rate((Integer) rs.getObject("read_rate"));
            entity.setwrite_rate((Integer) rs.getObject("write_rate"));
            entity.setactual_size(rs.getLong("actual_size"));
            entity.setReadLatency(rs.getObject("read_latency_seconds") != null ? rs.getDouble("read_latency_seconds")
                    : null);
            entity.setWriteLatency(rs.getObject("write_latency_seconds") != null ? rs.getDouble("write_latency_seconds")
                    : null);
            entity.setFlushLatency(rs.getObject("flush_latency_seconds") != null ? rs.getDouble("flush_latency_seconds")
                    : null);
            return entity;
        }
    }

    public DiskImageDynamicDAODbFacadeImpl() {
        super("disk_image_dynamic");
        setProcedureNameForGet("Getdisk_image_dynamicByimage_id");
        setProcedureNameForGetAll("GetAllFromdisk_image_dynamic");
    }

    @Override
    protected MapSqlParameterSource createIdParameterMapper(Guid id) {
        return getCustomMapSqlParameterSource()
                .addValue("image_id", id);
    }

    @Override
    protected MapSqlParameterSource createFullParametersMapper(DiskImageDynamic entity) {
        return createIdParameterMapper(entity.getId())
                .addValue("read_rate", entity.getread_rate())
                .addValue("write_rate", entity.getwrite_rate())
                .addValue("actual_size", entity.getactual_size())
                .addValue("read_latency_seconds", entity.getReadLatency())
                .addValue("write_latency_seconds", entity.getWriteLatency())
                .addValue("flush_latency_seconds", entity.getFlushLatency());
    }

    @Override
    protected RowMapper<DiskImageDynamic> createEntityRowMapper() {
        return DiskImageDynamicRowMapper.instance;
    }

    public MapSqlParameterMapper<Pair<Guid, DiskImageDynamic>> getBatchImageGroupMapper() {
        return new MapSqlParameterMapper<Pair<Guid, DiskImageDynamic>>() {

            @Override
            public MapSqlParameterSource map(Pair<Guid, DiskImageDynamic> entity) {
                Guid vmId = entity.getFirst();
                DiskImageDynamic diskImageDynamic = entity.getSecond();
                MapSqlParameterSource paramValue = new MapSqlParameterSource()
                        .addValue("vm_id", vmId)
                        .addValue("image_group_id", diskImageDynamic.getId())
                        .addValue("read_rate", diskImageDynamic.getread_rate())
                        .addValue("write_rate", diskImageDynamic.getwrite_rate())
                        .addValue("actual_size", diskImageDynamic.getactual_size())
                        .addValue("read_latency_seconds", diskImageDynamic.getReadLatency())
                        .addValue("write_latency_seconds", diskImageDynamic.getWriteLatency())
                        .addValue("flush_latency_seconds", diskImageDynamic.getFlushLatency());

                return paramValue;
            }
        };
    }

    @Override
    public void updateAllDiskImageDynamicWithDiskIdByVmId(Collection<Pair<Guid, DiskImageDynamic>> diskImageDynamicForVm) {
        getCallsHandler().executeStoredProcAsBatch("Updatedisk_image_dynamic_by_disk_id_and_vm_id",
                diskImageDynamicForVm, getBatchImageGroupMapper());
    }
}

package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import org.ovirt.engine.core.common.businessentities.DiskImageDynamic;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.MapSqlParameterMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

/**
 * JDBC template based implementation of DiskImageDynamicDAO
 *
 */
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

    @Override
    public MapSqlParameterMapper<DiskImageDynamic> getBatchMapper() {
        return new MapSqlParameterMapper<DiskImageDynamic>() {

            @Override
            public MapSqlParameterSource map(DiskImageDynamic entity) {
                MapSqlParameterSource paramValue = new MapSqlParameterSource()
                        .addValue("image_id", entity.getId())
                        .addValue("read_rate", entity.getread_rate())
                        .addValue("write_rate", entity.getwrite_rate())
                        .addValue("actual_size", entity.getactual_size())
                        .addValue("read_latency_seconds", entity.getReadLatency())
                        .addValue("write_latency_seconds", entity.getWriteLatency())
                        .addValue("flush_latency_seconds", entity.getFlushLatency());

                return paramValue;
            }
        };
    }

    public MapSqlParameterMapper<DiskImageDynamic> getBatchImageGroupMapper() {
        return new MapSqlParameterMapper<DiskImageDynamic>() {

            @Override
            public MapSqlParameterSource map(DiskImageDynamic entity) {
                MapSqlParameterSource paramValue = new MapSqlParameterSource()
                        .addValue("image_group_id", entity.getId())
                        .addValue("read_rate", entity.getread_rate())
                        .addValue("write_rate", entity.getwrite_rate())
                        .addValue("actual_size", entity.getactual_size())
                        .addValue("read_latency_seconds", entity.getReadLatency())
                        .addValue("write_latency_seconds", entity.getWriteLatency())
                        .addValue("flush_latency_seconds", entity.getFlushLatency());

                return paramValue;
            }
        };
    }

    @Override
    public void updateAllDiskImageDynamicWithDiskId(Collection<DiskImageDynamic> diskImageDynamic) {
        updateAllInBatch("Updatedisk_image_dynamic_by_disk_id", diskImageDynamic, getBatchImageGroupMapper());
    }
}

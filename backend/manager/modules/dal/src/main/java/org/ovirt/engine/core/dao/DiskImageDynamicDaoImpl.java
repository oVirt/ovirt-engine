package org.ovirt.engine.core.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.storage.DiskImageDynamic;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.MapSqlParameterMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

/**
 * JDBC template based implementation of {@link DiskImageDynamicDao}.
 */
@Named
@Singleton
public class DiskImageDynamicDaoImpl extends MassOperationsGenericDao<DiskImageDynamic, Guid>
        implements DiskImageDynamicDao {

    public DiskImageDynamicDaoImpl() {
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
                .addValue("read_rate", entity.getReadRate())
                .addValue("read_ops", entity.getReadOps())
                .addValue("write_rate", entity.getWriteRate())
                .addValue("write_ops", entity.getWriteOps())
                .addValue("actual_size", entity.getActualSize())
                .addValue("read_latency_seconds", entity.getReadLatency())
                .addValue("write_latency_seconds", entity.getWriteLatency())
                .addValue("flush_latency_seconds", entity.getFlushLatency());
    }

    @Override
    protected RowMapper<DiskImageDynamic> createEntityRowMapper() {
        return (rs, rowNum) -> {
            DiskImageDynamic entity = new DiskImageDynamic();
            entity.setId(getGuidDefaultEmpty(rs, "image_id"));
            entity.setReadRate((Integer) rs.getObject("read_rate"));
            entity.setReadOps((Long) rs.getObject("read_ops"));
            entity.setWriteRate((Integer) rs.getObject("write_rate"));
            entity.setWriteOps((Long) rs.getObject("write_ops"));
            entity.setActualSize(rs.getLong("actual_size"));
            entity.setReadLatency(rs.getObject("read_latency_seconds") != null ? rs.getDouble("read_latency_seconds")
                    : null);
            entity.setWriteLatency(rs.getObject("write_latency_seconds") != null ? rs.getDouble("write_latency_seconds")
                    : null);
            entity.setFlushLatency(rs.getObject("flush_latency_seconds") != null ? rs.getDouble("flush_latency_seconds")
                    : null);
            return entity;
        };
    }

    public MapSqlParameterMapper<Pair<Guid, DiskImageDynamic>> getBatchImageGroupMapper() {
        return entity -> {
            Guid vmId = entity.getFirst();
            DiskImageDynamic diskImageDynamic = entity.getSecond();
            return new MapSqlParameterSource()
                    .addValue("vm_id", vmId)
                    .addValue("image_group_id", diskImageDynamic.getId())
                    .addValue("read_rate", diskImageDynamic.getReadRate())
                    .addValue("read_ops", diskImageDynamic.getReadOps())
                    .addValue("write_rate", diskImageDynamic.getWriteRate())
                    .addValue("write_ops", diskImageDynamic.getWriteOps())
                    .addValue("actual_size", diskImageDynamic.getActualSize())
                    .addValue("read_latency_seconds", diskImageDynamic.getReadLatency())
                    .addValue("write_latency_seconds", diskImageDynamic.getWriteLatency())
                    .addValue("flush_latency_seconds", diskImageDynamic.getFlushLatency());
        };
    }

    public static List<Pair<Guid, DiskImageDynamic>> sortDiskImageDynamicForUpdate(Collection<Pair<Guid,
            DiskImageDynamic>> diskImageDynamicForVm) {
        List<Pair<Guid, DiskImageDynamic>> sortedDisks = new ArrayList<>();
        sortedDisks.addAll(diskImageDynamicForVm);
        sortedDisks.sort(Comparator.comparing(x -> x.getSecond().getId()));
        return sortedDisks;
    }

    @Override
    public void updateAllDiskImageDynamicWithDiskIdByVmId(Collection<Pair<Guid, DiskImageDynamic>> diskImageDynamicForVm) {
        getCallsHandler().executeStoredProcAsBatch("Updatedisk_image_dynamic_by_disk_id_and_vm_id",
                sortDiskImageDynamicForUpdate(diskImageDynamicForVm), getBatchImageGroupMapper());
    }
}

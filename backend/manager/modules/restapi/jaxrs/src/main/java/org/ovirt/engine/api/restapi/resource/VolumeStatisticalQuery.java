package org.ovirt.engine.api.restapi.resource;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.api.model.GlusterVolume;
import org.ovirt.engine.api.model.Statistic;
import org.ovirt.engine.api.model.ValueType;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSizeInfo;
import org.ovirt.engine.core.compat.Guid;

public class VolumeStatisticalQuery extends AbstractStatisticalQuery<GlusterVolume, GlusterVolumeEntity> {
    private static final Statistic MEM_TOTAL_SIZE   = create("memory.total.size", "Total size", GAUGE, BYTES, ValueType.INTEGER);
    private static final Statistic MEM_FREE_SIZE   = create("memory.free.size", "Free size", GAUGE, BYTES, ValueType.INTEGER);
    private static final Statistic MEM_USED_SIZE   = create("memory.used.size", "Used size", GAUGE, BYTES, ValueType.INTEGER);

    public VolumeStatisticalQuery(GlusterVolume parent ) {
        this(null, parent);
    }

    public VolumeStatisticalQuery(AbstractBackendResource<GlusterVolume, GlusterVolumeEntity>.EntityIdResolver<Guid> entityResolver, GlusterVolume parent) {
        super(GlusterVolume.class, parent, entityResolver);
    }

    @Override
    public List<Statistic> getStatistics(GlusterVolumeEntity entity) {
        GlusterVolumeSizeInfo sizeInfo = entity.getAdvancedDetails().getCapacityInfo();
        if (sizeInfo == null) {
            return new ArrayList<>();
        }
        return asList(setDatum(clone(MEM_TOTAL_SIZE), sizeInfo.getTotalSize()),
                setDatum(clone(MEM_FREE_SIZE), sizeInfo.getFreeSize()),
                setDatum(clone(MEM_USED_SIZE), sizeInfo.getUsedSize()));

    }

    @Override
    public Statistic adopt(Statistic statistic) {
        statistic.setGlusterVolume(parent);
        return statistic;
    }
}

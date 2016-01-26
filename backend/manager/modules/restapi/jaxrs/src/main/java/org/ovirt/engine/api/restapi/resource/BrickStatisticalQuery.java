package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import org.ovirt.engine.api.model.Cluster;
import org.ovirt.engine.api.model.GlusterBrick;
import org.ovirt.engine.api.model.GlusterVolume;
import org.ovirt.engine.api.model.Statistic;
import org.ovirt.engine.api.model.StatisticUnit;
import org.ovirt.engine.api.model.ValueType;
import org.ovirt.engine.core.common.businessentities.gluster.BrickDetails;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.MallInfo;
import org.ovirt.engine.core.compat.Guid;

public class BrickStatisticalQuery extends AbstractStatisticalQuery<GlusterBrick, GlusterBrickEntity> {
    private static final Statistic MEM_TOTAL_SIZE   = create("memory.total.size", "Total size", GAUGE, BYTES, ValueType.INTEGER);
    private static final Statistic MEM_FREE_SIZE   = create("memory.free.size", "Free size", GAUGE, BYTES, ValueType.INTEGER);
    private static final Statistic MEM_BLOCK_SIZE   = create("memory.block.size", "Block size", GAUGE, BYTES, ValueType.INTEGER);
    private static final Statistic MEM_MALL_ARENA   = create("malloc.arena", "Total memory allocated - Non mmapped", GAUGE, BYTES, ValueType.INTEGER);
    private static final Statistic MEM_MALL_ORDBLKS   = create("malloc.ordblks", "No. of ordinary free blocks", COUNTER, StatisticUnit.NONE, ValueType.INTEGER);
    private static final Statistic MEM_MALL_SMBLKS   = create("malloc.smblks", "No. of free fastbin blocks", COUNTER, StatisticUnit.NONE, ValueType.INTEGER);
    private static final Statistic MEM_MALL_HBLKS   = create("malloc.hblks", "No. of mmapped blocks allocated", COUNTER, StatisticUnit.NONE, ValueType.INTEGER);
    private static final Statistic MEM_MALL_HBLKSHD   = create("malloc.hblkhd", "Space allocated in mmapped blocks", GAUGE, StatisticUnit.BYTES, ValueType.INTEGER);
    private static final Statistic MEM_MALL_USMBLKS   = create("malloc.usmblks", "Maximum total allocated space", GAUGE, StatisticUnit.BYTES, ValueType.INTEGER);
    private static final Statistic MEM_MALL_FSMBLKS   = create("malloc.fsmblks", "Space in free fastbin blocks", GAUGE, StatisticUnit.BYTES, ValueType.INTEGER);
    private static final Statistic MEM_MALL_UORDBLKS   = create("malloc.uordblks", "Total allocated space", GAUGE, StatisticUnit.BYTES, ValueType.INTEGER);
    private static final Statistic MEM_MALL_FORDBLKS   = create("malloc.fordblks", "Total free space", GAUGE, StatisticUnit.BYTES, ValueType.INTEGER);
    private static final Statistic MEM_MALL_KEEPCOST   = create("malloc.keepcost", "Releasable free space", GAUGE, StatisticUnit.BYTES, ValueType.INTEGER);

    public BrickStatisticalQuery(GlusterBrick parent ) {
        this(null, parent);
    }

    public BrickStatisticalQuery(AbstractBackendResource<GlusterBrick, GlusterBrickEntity>.EntityIdResolver<Guid> entityResolver, GlusterBrick parent) {
        super(GlusterBrick.class, parent, entityResolver);
    }

    @Override
    public List<Statistic> getStatistics(GlusterBrickEntity entity) {
        BrickDetails brickDetails = entity.getBrickDetails();
        MallInfo mallInfo = brickDetails.getMemoryStatus().getMallInfo();
        return asList(setDatum(clone(MEM_TOTAL_SIZE), brickDetails.getBrickProperties().getTotalSize() * Mb),
                      setDatum(clone(MEM_FREE_SIZE), brickDetails.getBrickProperties().getFreeSize() * Mb),
                      setDatum(clone(MEM_BLOCK_SIZE), brickDetails.getBrickProperties().getBlockSize() * Mb),
                      setDatum(clone(MEM_MALL_ARENA),   mallInfo.getArena()),
                      setDatum(clone(MEM_MALL_ORDBLKS), mallInfo.getOrdblks()),
                      setDatum(clone(MEM_MALL_SMBLKS), mallInfo.getSmblks()),
                      setDatum(clone(MEM_MALL_HBLKS), mallInfo.getHblks()),
                      setDatum(clone(MEM_MALL_HBLKSHD), mallInfo.getHblkhd()),
                      setDatum(clone(MEM_MALL_USMBLKS), mallInfo.getUsmblks()),
                      setDatum(clone(MEM_MALL_FSMBLKS), mallInfo.getFsmblks()),
                      setDatum(clone(MEM_MALL_UORDBLKS), mallInfo.getUordblks()),
                      setDatum(clone(MEM_MALL_FORDBLKS), mallInfo.getFordblks()),
                      setDatum(clone(MEM_MALL_KEEPCOST), mallInfo.getKeepcost()));
    }

    @Override
    public Statistic adopt(Statistic statistic) {
        statistic.setBrick(clone(parent));
        return statistic;
    }

    private GlusterBrick clone(GlusterBrick brick) {
        //Needed to avoid NPE in LinkHelper due to unsetting of grandparent in LinkHelper#addLinks.
        GlusterBrick cloned = new GlusterBrick();
        cloned.setId(brick.getId());
        cloned.setGlusterVolume(new GlusterVolume());
        cloned.getGlusterVolume().setId(brick.getGlusterVolume().getId());
        cloned.getGlusterVolume().setCluster(new Cluster());
        cloned.getGlusterVolume().getCluster().setId(brick.getGlusterVolume().getCluster().getId());
        return cloned;
    }

}

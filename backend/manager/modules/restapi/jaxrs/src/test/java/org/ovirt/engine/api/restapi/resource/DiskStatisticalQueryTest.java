package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.Statistic;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.compat.Guid;

public class DiskStatisticalQueryTest {

    private static final double EPSILON = 0.1;
    private static final double READ_LATENCY = 1.1;
    private static final double WRITE_LATENCY = 2.2;
    private static final double FLUSH_LATENCY = 3.3;
    private static final int READ_RATE = 4;
    private static final long READ_OPS = 5L;
    private static final int WRITE_RATE = 6;
    private static final long WRITE_OPS = 7L;

    private DiskStatisticalQuery query = new DiskStatisticalQuery(getParent());

    @Test
    public void testQuery() {
        List<Statistic> statistics = query.getStatistics(getDisk());
        assertEquals(7, statistics.size());
        for (Statistic statistic : statistics) {
            verifyStatistic(statistic);
        }
    }

    private Disk getParent() {
        Disk disk = new Disk();
        disk.setId(Guid.Empty.toString());
        return disk;
    }

    private DiskImage getDisk() {
        DiskImage disk = new DiskImage();
        disk.setReadRate(READ_RATE);
        disk.setReadOps(READ_OPS);
        disk.setWriteRate(WRITE_RATE);
        disk.setWriteOps(WRITE_OPS);
        disk.setReadLatency(READ_LATENCY);
        disk.setWriteLatency(WRITE_LATENCY);
        disk.setFlushLatency(FLUSH_LATENCY);
        return disk;
    }

    private void verifyStatistic(Statistic statistic) {
        if (statistic.getName().equals(DiskStatisticalQuery.DATA_READ.getName())) {
            assertEquals(READ_RATE, statistic.getValues().getValues().get(0).getDatum().intValue());
        }
        if (statistic.getName().equals(DiskStatisticalQuery.DATA_READ_OPS.getName())) {
            assertEquals(READ_OPS, statistic.getValues().getValues().get(0).getDatum().longValue());
        }
        if (statistic.getName().equals(DiskStatisticalQuery.DATA_WRITE.getName())) {
            assertEquals(WRITE_RATE, statistic.getValues().getValues().get(0).getDatum().intValue());
        }
        if (statistic.getName().equals(DiskStatisticalQuery.DATA_WRITE_OPS.getName())) {
            assertEquals(WRITE_OPS, statistic.getValues().getValues().get(0).getDatum().longValue());
        }
        if (statistic.getName().equals(DiskStatisticalQuery.READ_LATENCY.getName())) {
            assertEquals(READ_LATENCY, statistic.getValues().getValues().get(0).getDatum().doubleValue(), EPSILON);
        }
        if (statistic.getName().equals(DiskStatisticalQuery.WRITE_LATENCY.getName())) {
            assertEquals(WRITE_LATENCY, statistic.getValues().getValues().get(0).getDatum().doubleValue(), EPSILON);
        }
        if (statistic.getName().equals(DiskStatisticalQuery.FLUSH_LATENCY.getName())) {
            assertEquals(FLUSH_LATENCY, statistic.getValues().getValues().get(0).getDatum().doubleValue(), EPSILON);
        }
    }
}

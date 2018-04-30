package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.api.model.Job;
import org.ovirt.engine.api.model.Statistic;
import org.ovirt.engine.api.model.Step;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeTaskStatusEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeTaskStatusForHost;
import org.ovirt.engine.core.common.job.JobExecutionStatus;
import org.ovirt.engine.core.compat.Guid;

public class GlusterTaskStatisticalQueryTest {

    private static final Guid[] GUIDS = {new Guid("afce7a39-8e8c-4819-ba9c-796d316592e6"),
        new Guid("afce7a39-8e8c-4819-ba9c-796d316592e7")};
    private static final double EPSILON = 1000;
    private static final long[] FILES_FAILED = {4576, 7890};
    private static final long[] FILES_SKIPPED = {56, 89};
    private static final long[] FILES_MOVED = {24576, 3679891};
    private static final long[] SIZE_MOVED = {24576345L, 3247389L};
    private static final double[] RUN_TIME = {14.1, 34.5};

    private StepStatisticalQuery query = new StepStatisticalQuery(null, getParent());

    @Test
    public void testQuery() {
        List<Statistic> statistics = query.getStatistics(getTaskEntity());
        assertEquals(14, statistics.size());
        for (Statistic statistic : statistics) {
            verifyStatistic(statistic);
        }
    }

    private GlusterVolumeTaskStatusEntity getTaskEntity() {
        GlusterVolumeTaskStatusEntity entity = new GlusterVolumeTaskStatusEntity();
        List<GlusterVolumeTaskStatusForHost> hostStatusList = new ArrayList<>();
        hostStatusList.add(getTaskStatusForHost(0));
        hostStatusList.add(getTaskStatusForHost(1));
        entity.setHostwiseStatusDetails(hostStatusList);
        return entity;
    }

    private GlusterVolumeTaskStatusForHost getTaskStatusForHost(int index) {
        GlusterVolumeTaskStatusForHost hostStatus = new GlusterVolumeTaskStatusForHost();
        hostStatus.setFilesFailed(FILES_FAILED[index]);
        hostStatus.setFilesSkipped(FILES_SKIPPED[index]);
        hostStatus.setFilesMoved(FILES_MOVED[index]);
        hostStatus.setTotalSizeMoved(SIZE_MOVED[index]);
        hostStatus.setRunTime(RUN_TIME[index]);
        hostStatus.setStatus(JobExecutionStatus.STARTED);
        hostStatus.setHostId(GUIDS[index]);
        return hostStatus;
    }

    private Step getParent() {
        Step step = new Step();
        step.setId(Guid.Empty.toString());
        step.setJob(new Job());
        step.getJob().setId(Guid.Empty.toString());
        return step;
    }


    private void verifyStatistic(Statistic statistic) {
        if (statistic.getName().equals(StepStatisticalQuery.FILES_FAILED.getName())) {
            if (GUIDS[0].toString().equals(statistic.getHost().getId())) {
                assertEquals(statistic.getValues().getValues().get(0).getDatum().longValue(), FILES_FAILED[0]);
            } else {
                assertEquals(statistic.getValues().getValues().get(0).getDatum().longValue(), FILES_FAILED[1]);
            }
        }
        if (statistic.getName().equals(StepStatisticalQuery.FILES_MOVED.getName())) {
            if (GUIDS[0].toString().equals(statistic.getHost().getId())) {
                assertEquals(statistic.getValues().getValues().get(0).getDatum().longValue(), FILES_MOVED[0]);
            } else {
                assertEquals(statistic.getValues().getValues().get(0).getDatum().longValue(), FILES_MOVED[1]);
            }
        }
        if (statistic.getName().equals(StepStatisticalQuery.SIZE_MOVED.getName())) {
            if (GUIDS[0].toString().equals(statistic.getHost().getId())) {
                assertEquals(statistic.getValues().getValues().get(0).getDatum().longValue(), SIZE_MOVED[0]);
            } else {
                assertEquals(statistic.getValues().getValues().get(0).getDatum().longValue(), SIZE_MOVED[1]);
            }
        }
        if (statistic.getName().equals(StepStatisticalQuery.RUN_TIME.getName())) {
            if (GUIDS[0].toString().equals(statistic.getHost().getId())) {
                assertEquals(statistic.getValues().getValues().get(0).getDatum().doubleValue(), RUN_TIME[0], EPSILON);
            } else {
                assertEquals(statistic.getValues().getValues().get(0).getDatum().doubleValue(), RUN_TIME[1], EPSILON);
            }
        }
        if (statistic.getName().equals(StepStatisticalQuery.STATUS.getName())) {
            assertEquals(statistic.getValues().getValues().get(0).getDetail(), JobExecutionStatus.STARTED.toString());
        }
    }
}

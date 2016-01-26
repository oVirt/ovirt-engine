package org.ovirt.engine.api.restapi.resource;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.Job;
import org.ovirt.engine.api.model.Statistic;
import org.ovirt.engine.api.model.StatisticUnit;
import org.ovirt.engine.api.model.Step;
import org.ovirt.engine.api.model.ValueType;
import org.ovirt.engine.api.restapi.resource.BaseBackendResource.BackendFailureException;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeTaskStatusEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeTaskStatusForHost;
import org.ovirt.engine.core.compat.Guid;

public class StepStatisticalQuery extends AbstractStatisticalQuery<Step, GlusterVolumeTaskStatusEntity> {
    public static final Statistic FILES_MOVED   = create("files.moved", "Number of files moved", COUNTER, StatisticUnit.NONE, ValueType.INTEGER);
    public static final Statistic SIZE_MOVED   = create("size.moved", "size of files moved", GAUGE, BYTES, ValueType.INTEGER);
    public static final Statistic FILES_SKIPPED   = create("files.skipped", "Number of files skipped", COUNTER, StatisticUnit.NONE, ValueType.INTEGER);
    public static final Statistic FILES_SCANNED   = create("files.scanned", "Number of files scanned", COUNTER, StatisticUnit.NONE, ValueType.INTEGER);
    public static final Statistic FILES_FAILED   = create("files.failed", "Number of files failed", COUNTER, StatisticUnit.NONE, ValueType.INTEGER);
    public static final Statistic RUN_TIME   = create("run.time", "Time for which task is running", COUNTER, StatisticUnit.SECONDS, ValueType.DECIMAL);
    public static final Statistic STATUS   = create("status", "Status of task", GAUGE, StatisticUnit.NONE, ValueType.STRING);


    protected IResolver<Guid, GlusterVolumeTaskStatusEntity> taskStatusResolver;

    public StepStatisticalQuery(IResolver<Guid, GlusterVolumeTaskStatusEntity> taskStatusResolver, Step parent) {
        super(Step.class, parent, null);
        this.taskStatusResolver = taskStatusResolver;
    }

    @Override
    public GlusterVolumeTaskStatusEntity resolve(Guid id) throws BackendFailureException {
        return taskStatusResolver.resolve(id);
    }

    @Override
    public List<Statistic> getStatistics(GlusterVolumeTaskStatusEntity entity) {
        List<Statistic> list = new ArrayList<>();
        if (entity == null) {
            return list;
        }

        for (GlusterVolumeTaskStatusForHost hostStatus: entity.getHostwiseStatusDetails()) {
            Guid hostId = hostStatus.getHostId();
            list.add(adopt(setHostDatum(FILES_MOVED, hostStatus.getFilesMoved(), hostId)));
            list.add(adopt(setHostDatum(SIZE_MOVED, hostStatus.getTotalSizeMoved(), hostId)));
            list.add(adopt(setHostDatum(FILES_SKIPPED, hostStatus.getFilesSkipped(), hostId)));
            list.add(adopt(setHostDatum(FILES_SCANNED, hostStatus.getFilesScanned(), hostId)));
            list.add(adopt(setHostDatum(FILES_FAILED, hostStatus.getFilesFailed(), hostId)));
            list.add(adopt(setHostDatum(RUN_TIME, hostStatus.getRunTime(), hostId)));
            list.add(adopt(setHostDatum(STATUS, hostStatus.getStatus().toString(), hostId)));
        }
        return list;
    }

    private Statistic setHostDatum(Statistic stat, long value, Guid hostId) {
        return setHostDatum(stat, new BigDecimal(value, new MathContext(0)), hostId);
    }

    private Statistic setHostDatum(Statistic stat, BigDecimal value, Guid hostId) {
        Statistic statistic = setDatum(clone(stat), value);
        statistic.setHost(getHost(hostId));
        return statistic;
    }

    private Statistic setHostDatum(Statistic stat, String value, Guid hostId) {
        Statistic statistic = setDatum(clone(stat), value);
        statistic.setHost(getHost(hostId));
        return statistic;
    }

    public Statistic setHostDatum(Statistic statistic, double datum, Guid hostId) {
        return setHostDatum(statistic, new BigDecimal(datum, new MathContext(0)), hostId);
    }

    private Host getHost(Guid hostId) {
        Host host = new Host();
        host.setId(hostId.toString());
        return host;
    }

    @Override
    public Statistic adopt(Statistic statistic) {
        statistic.setStep(clone(parent));
        return statistic;
    }

    private Step clone(Step step) {
        //Needed to avoid NPE in LinkHelper due to unsetting of grandparent in LinkHelper#addLinks.
        Step cloned = new Step();
        cloned.setId(step.getId());
        cloned.setJob(new Job());
        cloned.getJob().setId(step.getJob().getId());
        return cloned;
    }


}

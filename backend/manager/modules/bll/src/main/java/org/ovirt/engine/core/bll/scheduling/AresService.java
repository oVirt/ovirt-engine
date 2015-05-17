package org.ovirt.engine.core.bll.scheduling;

import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.di.qualifier.Created;
import org.ovirt.engine.core.common.di.qualifier.Deleted;
import org.ovirt.engine.core.common.di.qualifier.Updated;
import org.ovirt.engine.core.common.scheduling.AffinityGroup;
import org.ovirt.engine.core.utils.timer.OnTimerMethodAnnotation;
import org.ovirt.engine.core.utils.timer.SchedulerUtilQuartzImpl;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.inject.Singleton;
import java.util.concurrent.TimeUnit;

@Singleton
public class AresService {

    private String jobId;

    @PostConstruct
    private void init() {

        long x = 3000; //TODO take from config - ConfigValues.AresServiceInterval
        long y = 3000; //TODO take from config - ConfigValues.AresServiceInterval

        /* initialize structures */
        scheduleJobs(x, y);

    }

    @OnTimerMethodAnnotation("refresh")
    public void refresh() {
        //
    }

    public void onChange(@Observes @Updated @Created @Deleted AffinityGroup affinityGroup) {
        // handle affinity group changes
    }

    public void onChange(@Observes @Updated @Created @Deleted VDSGroup cluster) {
        // handle cluster changes
    }

    private void scheduleJobs(long x, long y) {
    /* start the interval refreshing */
        jobId = SchedulerUtilQuartzImpl.getInstance().scheduleAFixedDelayJob(
                this,
                "onTimer",
                new Class[0],
                new Object[0],
                x,
                y,
                TimeUnit.MILLISECONDS);
    }

}

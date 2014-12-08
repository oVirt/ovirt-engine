package org.ovirt.engine.core.utils.timer;

import static org.quartz.TriggerBuilder.newTrigger;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.quartz.Trigger;
import org.quartz.TriggerKey;

/**
 * The FixedDelayJobListener is a JobListener implementation to turn a job into
 * a fixed delay job. A fixed delay job means that the delay is fixed between
 * any 2 successive executions of the job.
 *
 */
public class FixedDelayJobListener implements JobListener {

    // const
    public static final String FIXED_JOB_LISTENER_NAME = "fixedJobListenerName";
    public SchedulerUtil sched;

    /**
     * create a new Job Listener.
     *
     * @param scheduler
     *            - the scheduler used rescheduling the next job
     */
    public FixedDelayJobListener(SchedulerUtil scheduler) {
        sched = scheduler;
    }

    /**
     * @return the jobListener constant name
     */
    @Override
    public String getName() {
        return FIXED_JOB_LISTENER_NAME;
    }

    /**
     * reschedule the job with a new trigger. The new trigger will fire within a
     * fixed time from the method execution.
     *
     * @see org.quartz.JobListener#jobWasExecuted(JobExecutionContext,
     *      JobExecutionException)
     */
    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException exception) {
        // Get the details of the job:
        JobDetail jobdetail = context.getJobDetail();
        JobDataMap data = jobdetail.getJobDataMap();

        // This is being called for all our jobs, so first check if this is a fixed delay
        // job and if not just exit:
        if (!data.containsKey(SchedulerUtilBaseImpl.FIXED_DELAY_VALUE)) {
            return;
        }

        // generate the new trigger time
        String configValueName = data.getString(SchedulerUtilBaseImpl.CONFIGURABLE_DELAY_KEY_NAME);
        long delay;

        if (StringUtils.isEmpty(configValueName)) {
            delay = data.getLongValue(SchedulerUtilBaseImpl.FIXED_DELAY_VALUE);
        } else {
            ConfigValues configDelay = ConfigValues.valueOf(configValueName);
            delay = Config.<Integer> getValue(configDelay).longValue();
        }

        TimeUnit delayUnit = (TimeUnit) data.getWrappedMap().get(SchedulerUtilBaseImpl.FIXED_DELAY_TIME_UNIT);
        Date runTime = SchedulerUtilQuartzImpl.getFutureDate(delay, delayUnit);

        // generate the new trigger
        Trigger oldTrigger = context.getTrigger();
        TriggerKey oldTriggerKey = oldTrigger.getKey();
        Trigger newTrigger = newTrigger()
            .withIdentity(oldTriggerKey)
            .startAt(runTime)
            .build();

        // schedule the new trigger
        sched.rescheduleAJob(oldTriggerKey.getName(), oldTriggerKey.getGroup(), newTrigger);
        // SchedulerUtilQuartzImpl.getInstance().rescheduleAJob(oldTriggerName,
        // oldTriggerGroup, newTrigger);

    }

    @Override
    public void jobExecutionVetoed(JobExecutionContext arg0) {
        // empty implementation

    }

    @Override
    public void jobToBeExecuted(JobExecutionContext arg0) {
        // empty implementation

    }
}

package org.ovirt.engine.core.utils.timer;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;

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

        // generate the new trigger time
        JobDetail jobdetail = context.getJobDetail();
        JobDataMap data = jobdetail.getJobDataMap();
        long delay = data.getLongValue(SchedulerUtilQuartzImpl.FIXED_DELAY_VALUE);
        TimeUnit delayUnit = (TimeUnit) data.getWrappedMap().get(SchedulerUtilQuartzImpl.FIXED_DELAY_TIME_UNIT);
        Date runTime = SchedulerUtilQuartzImpl.getFutureDate(delay, delayUnit);

        // generate the new trigger
        Trigger oldTrigger = context.getTrigger();
        String oldTriggerName = oldTrigger.getName();
        String oldTriggerGroup = oldTrigger.getGroup();
        SimpleTrigger newTrigger = new SimpleTrigger(oldTriggerName, oldTriggerGroup, runTime);
        newTrigger.setJobGroup(jobdetail.getGroup());
        newTrigger.setJobName(jobdetail.getName());

        // schedule the new trigger
        sched.rescheduleAJob(oldTriggerName, oldTriggerGroup, newTrigger);
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

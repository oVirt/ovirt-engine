package org.ovirt.engine.core.utils.timer;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobKey.jobKey;
import static org.quartz.TriggerBuilder.newTrigger;
import static org.quartz.TriggerKey.triggerKey;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SchedulerUtilBaseImpl implements SchedulerUtil {

    public static final String RUNNABLE_INSTANCE = "runnable.instance";
    public static final String RUN_METHOD_NAME = "method.name";
    public static final String RUN_METHOD_PARAM_TYPE = "method.paramType";
    public static final String RUN_METHOD_PARAM = "method.param";
    public static final String FIXED_DELAY_VALUE = "fixedDelayValue";
    public static final String FIXED_DELAY_TIME_UNIT = "fixedDelayTimeUnit";
    public static final String CONFIGURABLE_DELAY_KEY_NAME = "configDelayKeyName";
    private static final String TRIGGER_PREFIX = "trigger";
    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected Scheduler sched;
    private final AtomicLong sequenceNumber = new AtomicLong(Long.MIN_VALUE);

    public static Date getFutureDate(long delay, TimeUnit timeUnit) {
        return new Date(System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(delay, timeUnit));
    }

    /**
     * schedules a fixed delay job.
     *
     * @param instance
     *            - the instance to activate the method on timeout
     * @param methodName
     *            - the name of the method to activate on the instance
     * @param inputTypes
     *            - the method input types
     * @param inputParams
     *            - the method input parameters
     * @param initialDelay
     *            - the initial delay before the first activation
     * @param taskDelay
     *            - the delay between jobs
     * @param timeUnit
     *            - the unit of time used for initialDelay and taskDelay.
     * @return the scheduled job id
     */
    @Override
    public String scheduleAFixedDelayJob(Object instance,
            String methodName,
            Class<?>[] inputTypes,
            Object[] inputParams,
            long initialDelay,
            long taskDelay,
            TimeUnit timeUnit) {
        JobDetail job = createJobForDelayJob(instance, methodName, inputTypes, inputParams, taskDelay, timeUnit);
        scheduleJobWithTrigger(initialDelay, timeUnit, instance, job);
        return job.getKey().getName();
    }

    private void scheduleJobWithTrigger(long initialDelay, TimeUnit timeUnit, Object instance, JobDetail job) {
        Trigger trigger = createSimpleTrigger(initialDelay, timeUnit, instance);
        try {
            sched.scheduleJob(job, trigger);
        } catch (SchedulerException se) {
            log.error("failed to schedule job: {}", se.getMessage());
            log.debug("Exception", se);
        }
    }

    private JobDetail createJobForDelayJob(Object instance,
            String methodName,
            Class<?>[] inputTypes,
            Object[] inputParams,
            long taskDelay,
            TimeUnit timeUnit) {
        JobDetail job = createJobWithBasicMapValues(instance, methodName, inputTypes, inputParams);
        JobDataMap data = job.getJobDataMap();
        setupDataMapForDelayJob(data, taskDelay, timeUnit);
        return job;
    }

    /**
     * schedules a job with a configurable delay.
     *
     * @param instance
     *            - the instance to activate the method on timeout
     * @param methodName
     *            - the name of the method to activate on the instance
     * @param inputTypes
     *            - the method input types
     * @param inputParams
     *            - the method input parameters
     * @param initialDelay
     *            - the initial delay before the first activation
     * @param taskDelay
     *            - the name of the config value that sets the delay between jobs
     * @param timeUnit
     *            - the unit of time used for initialDelay and taskDelay.
     * @return the scheduled job id
     */
    @Override
    public String scheduleAConfigurableDelayJob(Object instance,
            String methodName,
            Class<?>[] inputTypes,
            Object[] inputParams,
            long initialDelay,
            String configurableDelayKeyName,
            TimeUnit timeUnit) {
        long configurableDelay = getConfigurableDelay(configurableDelayKeyName);
        JobDetail job =
                createJobForDelayJob(instance, methodName, inputTypes, inputParams, configurableDelay, timeUnit);
        JobDataMap data = job.getJobDataMap();
        data.put(CONFIGURABLE_DELAY_KEY_NAME, configurableDelayKeyName);
        scheduleJobWithTrigger(initialDelay, timeUnit, instance, job);
        return job.getKey().getName();
    }

    /**
     * get the configurable delay value from the DB according to given key
     */
    private long getConfigurableDelay(String configurableDelayKeyName) {
        ConfigValues configDelay = ConfigValues.valueOf(configurableDelayKeyName);
        return Config.<Integer> getValue(configDelay).longValue();
    }

    /**
     * setup the values in the data map that are relevant for jobs with delay
     */
    private void setupDataMapForDelayJob(JobDataMap data, long taskDelay, TimeUnit timeUnit) {
        data.put(FIXED_DELAY_TIME_UNIT, timeUnit);
        data.put(FIXED_DELAY_VALUE, taskDelay);
    }

    protected abstract JobDetail createJobWithBasicMapValues(Object instance,
            String methodName,
            Class<?>[] inputTypes,
            Object[] inputParams);

    private Trigger createSimpleTrigger(long initialDelay, TimeUnit timeUnit, Object instance) {
        Date runTime = getFutureDate(initialDelay, timeUnit);
        String triggerName = generateUniqueNameForInstance(instance, TRIGGER_PREFIX);
        Trigger trigger = newTrigger()
                .withIdentity(triggerName, Scheduler.DEFAULT_GROUP)
                .startAt(runTime)
                .build();
        return trigger;
    }

    /**
     * schedules a one time job.
     *
     * @param instance
     *            - the instance to activate the method on timeout
     * @param methodName
     *            - the name of the method to activate on the instance
     * @param inputTypes
     *            - the method input types
     * @param inputParams
     *            - the method input parameters
     * @param initialDelay
     *            - the initial delay before the job activation
     * @param timeUnit
     *            - the unit of time used for initialDelay and taskDelay.
     * @return the scheduled job id
     */
    @Override
    public String scheduleAOneTimeJob(Object instance,
            String methodName,
            Class<?>[] inputTypes,
            Object[] inputParams,
            long initialDelay,
            TimeUnit timeUnit) {
        JobDetail job = createJobWithBasicMapValues(instance, methodName, inputTypes, inputParams);
        scheduleJobWithTrigger(initialDelay, timeUnit, instance, job);
        return job.getKey().getName();
    }

    /**
     * schedules a cron job.
     *
     * @param instance
     *            - the instance to activate the method on timeout
     * @param methodName
     *            - the name of the method to activate on the instance
     * @param inputTypes
     *            - the method input types
     * @param inputParams
     *            - the method input parameters
     * @param cronExpression
     *            - cron expression to run this job
     * @return the scheduled job id
     */
    @Override
    public String scheduleACronJob(Object instance,
            String methodName,
            Class<?>[] inputTypes,
            Object[] inputParams,
            String cronExpression) {
        JobDetail job = createJobWithBasicMapValues(instance, methodName, inputTypes, inputParams);
        try {
            String triggerName = generateUniqueNameForInstance(instance, TRIGGER_PREFIX);
            Trigger trigger = newTrigger()
                    .withIdentity(triggerName, Scheduler.DEFAULT_GROUP)
                    .withSchedule(cronSchedule(cronExpression))
                    .build();
            sched.scheduleJob(job, trigger);
        } catch (Exception se) {
            log.error("failed to schedule job: {}", se.getMessage());
            log.debug("Exception", se);
            return null;
        }
        return job.getKey().getName();
    }

    /**
     * Schedules a cron job with specific delay and end by value
     *
     * @param instance
     *            - the instance to activate the method on timeout
     * @param methodName
     *            - the name of the method to activate on the instance
     * @param inputTypes
     *            - the method input types
     * @param inputParams
     *            - the method input parameters
     * @param cronExpression
     *            - cron expression to run this job
     * @param startAt
     *            - when to start the task
     * @param endBy
     *            - when to end the task
     * @return the scheduled job id
     */
    public String scheduleACronJob(Object instance,
            String methodName,
            Class<?>[] inputTypes,
            Object[] inputParams,
            String cronExpression,
            Date startAt,
            Date endBy) {
        JobDetail job = createJobWithBasicMapValues(instance, methodName, inputTypes, inputParams);
        try {
            String triggerName = generateUniqueNameForInstance(instance, TRIGGER_PREFIX);
            Trigger trigger = newTrigger()
                    .withIdentity(triggerName, Scheduler.DEFAULT_GROUP)
                    .withSchedule(cronSchedule(cronExpression))
                    .startAt(startAt)
                    .endAt(endBy)
                    .build();
            sched.scheduleJob(job, trigger);
        } catch (Exception se) {
            log.error("failed to schedule job: {}", se.getMessage());
            log.debug("Exception", se);
            throw new RuntimeException(se);
        }
        return job.getKey().getName();
    }


    /**
     * reschedule the job associated with the given old trigger with the new trigger.
     *
     * @param oldTriggerName
     *            - the name of the trigger to remove.
     * @param oldTriggerGroup
     *            - the group of the trigger to remove.
     * @param newTrigger
     *            - the new Trigger to associate the job with
     */
    @Override
    public void rescheduleAJob(String oldTriggerName, String oldTriggerGroup, Trigger newTrigger) {
        try {
            if (!sched.isShutdown()) {
                sched.rescheduleJob(triggerKey(oldTriggerName, oldTriggerGroup), newTrigger);
            }
        } catch (SchedulerException se) {
            log.error("failed to reschedule the job: {}", se.getMessage());
            log.debug("Exception", se);
        }
    }

    /**
     * pauses a job with the given jobId assuming the job is in the default quartz group
     */
    @Override
    public void pauseJob(String jobId) {
        try {
            sched.pauseJob(jobKey(jobId, Scheduler.DEFAULT_GROUP));
        } catch (SchedulerException se) {
            log.error("failed to pause a job with id={}: {}", jobId, se.getMessage());
            log.debug("Exception", se);
        }
    }

    /**
     * Delete the identified Job from the Scheduler
     *
     * @param jobId
     *            - the id of the job to delete
     */
    @Override
    public void deleteJob(String jobId) {
        try {
            sched.deleteJob(jobKey(jobId, Scheduler.DEFAULT_GROUP));
        } catch (SchedulerException se) {
            log.error("failed to delete a job with id={}: {}", jobId, se.getMessage());
            log.debug("Exception", se);
        }
    }

    /**
     * resume a job with the given jobId assuming the job is in the default quartz group
     */
    @Override
    public void resumeJob(String jobId) {
        try {
            sched.resumeJob(jobKey(jobId, Scheduler.DEFAULT_GROUP));
        } catch (SchedulerException se) {
            log.error("failed to pause a job with id={}: {}", jobId, se.getMessage());
            log.debug("Exception", se);
        }
    }

    @Override
    public void triggerJob(String jobId) {
        try {
            List<? extends Trigger> existingTriggers = sched.getTriggersOfJob(jobKey(jobId, Scheduler.DEFAULT_GROUP));

            if (!existingTriggers.isEmpty()) {
                // Note: we assume that every job has exactly one trigger
                Trigger oldTrigger = existingTriggers.get(0);
                TriggerKey oldTriggerKey = oldTrigger.getKey();
                Trigger newTrigger = newTrigger()
                        .withIdentity(oldTriggerKey)
                        .startAt(getFutureDate(0, TimeUnit.MILLISECONDS))
                        .build();

                rescheduleAJob(oldTriggerKey.getName(), oldTriggerKey.getGroup(), newTrigger);
            } else {
                log.error("failed to trigger a job with id={}, job has no trigger", jobId);
            }
        } catch (SchedulerException se) {
            log.error("failed to trigger a job with id={}: {}", jobId, se.getMessage());
            log.debug("Exception", se);
        }
    }

    /**
     * Halts the Scheduler, and cleans up all resources associated with the Scheduler. The scheduler cannot be
     * re-started.
     *
     * @see org.quartz.Scheduler#shutdown(boolean waitForJobsToComplete)
     */
    @Override
    public void shutDown() {
        try {
            if (sched != null) {
                sched.shutdown(true);
            }
        } catch (SchedulerException se) {
            log.error("failed to shut down the scheduler: {}", se.getMessage());
            log.debug("Exception", se);
        }
    }

    /**
     * @return the quartz scheduler wrapped by this SchedulerUtil
     */
    @Override
    public Scheduler getRawScheduler() {
        return sched;
    }

    protected String generateUniqueNameForInstance(Object instance, String nestedName) {
        String name = instance.getClass().getName() + "." + nestedName + "#" + sequenceNumber.incrementAndGet();
        return name;
    }

}

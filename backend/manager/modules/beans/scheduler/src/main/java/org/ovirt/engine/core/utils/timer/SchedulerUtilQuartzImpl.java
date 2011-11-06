package org.ovirt.engine.core.utils.timer;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ovirt.engine.core.utils.ejb.BeanProxyType;
import org.ovirt.engine.core.utils.ejb.BeanType;
import org.ovirt.engine.core.utils.ejb.EjbUtils;
import org.quartz.CronTrigger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

// Here we use a Singleton bean, names Scheduler.
// The @Startup annotation is to make sure the bean is initialized on startup.
// @ConcurrencyManagement - we use bean managed concurrency:
// Singletons that use bean-managed concurrency allow full concurrent access to all the
// business and timeout methods in the singleton.
// The developer of the singleton is responsible for ensuring that the state of the singleton is synchronized across all clients.
@Singleton(name = "Scheduler")
@Startup
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class SchedulerUtilQuartzImpl implements SchedulerUtil {

    // consts
    public static final String RUNNABLE_INSTANCE = "runnable.instance";
    public static final String RUN_METHOD_NAME = "method.name";
    public static final String RUN_METHOD_PARAM_TYPE = "method.paramType";
    public static final String RUN_METHOD_PARAM = "method.param";
    public static final String FIXED_DELAY_VALUE = "fixedDelayValue";
    public static final String FIXED_DELAY_TIME_UNIT = "fixedDelayTimeUnit";
    private static final String TRIGGER_PREFIX = "trigger";

    // members
    private final Log log = LogFactory.getLog(SchedulerUtilQuartzImpl.class);
    private Scheduler sched;

    private final AtomicLong sequenceNumber = new AtomicLong(Long.MIN_VALUE);


    /**
     * This method is called upon the bean creation as part
     * of the management Service bean lifecycle.
     */
    @PostConstruct
    public void create(){
        setup();
    }

    /*
     * retrieving the quartz scheduler from the factory.
     */
    public void setup() {
        try {
            SchedulerFactory sf = new StdSchedulerFactory();
            sched = sf.getScheduler();
            sched.start();
            sched.addJobListener(new FixedDelayJobListener(this));
        } catch (SchedulerException se) {
            log.error("there is a problem with the underlying Scheduler.", se);
        }
    }

    @PreDestroy
    public void teardown() {
        try {
            if (sched != null) {
                sched.shutdown();
            }
        } catch (SchedulerException e) {
            log.error("Failed to shutdown Quartz service", e);
        }
    }

    /**
     * Returns the single instance of this Class.
     *
     * @return a SchedulerUtil instance
     */
    public static SchedulerUtil getInstance() {
        return EjbUtils.findBean(BeanType.SCHEDULER, BeanProxyType.LOCAL);
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
    @SuppressWarnings("unchecked")
    @Override
    public String scheduleAFixedDelayJob(Object instance,
                                         String methodName,
                                         Class[] inputTypes,
                                         Object[] inputParams,
                                         long initialDelay,
                                         long taskDelay,
                                         TimeUnit timeUnit) {
        String jobName = generateUniqueNameForInstance(instance, methodName);
        JobDetail job = new JobDetail(jobName, Scheduler.DEFAULT_GROUP, JobWrapper.class);
        job.addJobListener(FixedDelayJobListener.FIXED_JOB_LISTENER_NAME);
        JobDataMap data = job.getJobDataMap();
        data.put(RUNNABLE_INSTANCE, instance);
        data.put(RUN_METHOD_NAME, methodName);
        data.put(RUN_METHOD_PARAM_TYPE, inputTypes);
        data.put(RUN_METHOD_PARAM, inputParams);
        data.put(FIXED_DELAY_VALUE, taskDelay);
        data.put(FIXED_DELAY_TIME_UNIT, timeUnit);

        Date runTime = getFutureDate(initialDelay, timeUnit);
        String triggerName = generateUniqueNameForInstance(instance, TRIGGER_PREFIX);
        SimpleTrigger trigger = new SimpleTrigger(triggerName, Scheduler.DEFAULT_GROUP, runTime);

        try {
            sched.scheduleJob(job, trigger);
        } catch (SchedulerException se) {
            log.error("failed to schedule job", se);
        }

        return jobName;
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
    @SuppressWarnings("unchecked")
    @Override
    public String scheduleAOneTimeJob(Object instance,
                                      String methodName,
                                      Class[] inputTypes,
                                      Object[] inputParams,
                                      long initialDelay,
                                      TimeUnit timeUnit) {
        String jobName = generateUniqueNameForInstance(instance, methodName);
        JobDetail job = new JobDetail(jobName, Scheduler.DEFAULT_GROUP, JobWrapper.class);
        JobDataMap data = job.getJobDataMap();
        data.put(RUNNABLE_INSTANCE, instance);
        data.put(RUN_METHOD_NAME, methodName);
        data.put(RUN_METHOD_PARAM, inputParams);
        data.put(RUN_METHOD_PARAM_TYPE, inputTypes);
        Date runTime = getFutureDate(initialDelay, timeUnit);
        String triggerName = generateUniqueNameForInstance(instance, TRIGGER_PREFIX);
        SimpleTrigger trigger = new SimpleTrigger(triggerName, Scheduler.DEFAULT_GROUP, runTime);
        try {
            sched.scheduleJob(job, trigger);
        } catch (SchedulerException se) {
            log.error("failed to schedule job", se);
        }

        return jobName;
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
    @SuppressWarnings("unchecked")
    @Override
    public String scheduleACronJob(Object instance,
                                   String methodName,
                                   Class[] inputTypes,
                                   Object[] inputParams,
                                   String cronExpression) {
        String jobName = generateUniqueNameForInstance(instance, methodName);
        JobDetail job = new JobDetail(jobName, Scheduler.DEFAULT_GROUP, JobWrapper.class);
        JobDataMap data = job.getJobDataMap();
        data.put(RUNNABLE_INSTANCE, instance);
        data.put(RUN_METHOD_NAME, methodName);
        data.put(RUN_METHOD_PARAM, inputParams);
        data.put(RUN_METHOD_PARAM_TYPE, inputTypes);
        try {
            String triggerName = generateUniqueNameForInstance(instance, TRIGGER_PREFIX);
            Trigger trigger = new CronTrigger(triggerName, Scheduler.DEFAULT_GROUP, cronExpression);
            sched.scheduleJob(job, trigger);
        } catch (Exception se) {
            log.error("failed to schedule job", se);
        }

        return jobName;
    }

    /**
     * reschedule the job associated with the given old trigger with the new
     * trigger.
     *
     * @param oldTriggerName
     *            - the name of the trigger to remove.
     * @param oldTriggerGroup
     *            - the group of the trigger to remove.
     * @param newTrigger
     *            - the new Trigger to associate the job with
     */
    public void rescheduleAJob(String oldTriggerName, String oldTriggerGroup, Trigger newTrigger) {
        try {
            sched.rescheduleJob(oldTriggerName, oldTriggerGroup, newTrigger);
        } catch (SchedulerException se) {
            log.error("failed to reschedule the job", se);
        }
    }

    /**
     * pauses a job with the given jobId assuming the job is in the default
     * quartz group
     *
     * @param jobId
     */
    @Override
    public void pauseJob(String jobId) {
        try {
            sched.pauseJob(jobId, Scheduler.DEFAULT_GROUP);
        } catch (SchedulerException se) {
            log.error("failed to pause a job with id=" + jobId, se);
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
            sched.deleteJob(jobId, Scheduler.DEFAULT_GROUP);
        } catch (SchedulerException se) {
            log.error("failed to delete a job with id=" + jobId, se);
        }

    }

    /**
     * resume a job with the given jobId assuming the job is in the default
     * quartz group
     *
     * @param jobId
     */
    @Override
    public void resumeJob(String jobId) {
        try {
            sched.resumeJob(jobId, Scheduler.DEFAULT_GROUP);
        } catch (SchedulerException se) {
            log.error("failed to pause a job with id=" + jobId, se);
        }

    }

    /**
     * Halts the Scheduler, and cleans up all resources associated with the
     * Scheduler. The scheduler cannot be re-started.
     *
     * @see org.quartz.Scheduler#shutdown(boolean waitForJobsToComplete)
     */
    @Override
    public void shutDown() {
        try {
            sched.shutdown(true);
        } catch (SchedulerException se) {
            log.error("failed to shut down the scheduler", se);
        }
    }

    /**
     * @return the quartz scheduler wrapped by this SchedulerUtil
     */
    public Scheduler getRawScheduler() {
        return sched;
    }

    /*
     * returns a future date with the given delay. the delay is being calculated
     * according to the given Time units
     */
    public static Date getFutureDate(long delay, TimeUnit timeUnit) {
        if (delay > 0) {
            return new Date(new Date().getTime() + TimeUnit.MILLISECONDS.convert(delay, timeUnit));
        } else {
            return new Date();
        }
    }

    /*
     * generate a unique name for the given instance, using a sequence number.
     */
    private String generateUniqueNameForInstance(Object instance, String nestedName) {
        String name = instance.getClass().getName() + "." + nestedName + "#" + sequenceNumber.incrementAndGet();
        return name;
    }

}

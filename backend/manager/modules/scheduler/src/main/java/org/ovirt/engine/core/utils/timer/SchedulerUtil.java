package org.ovirt.engine.core.utils.timer;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.quartz.Scheduler;
import org.quartz.Trigger;

public interface SchedulerUtil {

    /**
     * Schedules a fixed-delay job to run the method with the given name on the
     * given instance.
     *
     * @param instance
     *            - the instance to activate a method on upon timeout
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
    public String scheduleAFixedDelayJob(Object instance,
                                         String methodName,
                                         Class<?>[] inputTypes,
                                         Object[] inputParams,
                                         long initialDelay,
                                         long taskDelay,
                                         TimeUnit timeUnit);

    /**
     * Schedules a one time job.
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
    public String scheduleAOneTimeJob(Object instance,
                                      String methodName,
                                      Class<?>[] inputTypes,
                                      Object[] inputParams,
                                      long initialDelay,
                                      TimeUnit timeUnit);

    /**
     * Reschedule the job associated with the given old trigger with the new
     * trigger.
     *
     * @param oldTriggerName
     *            - the name of the trigger to remove.
     * @param oldTriggerGroup
     *            - the group of the trigger to remove.
     * @param newTrigger
     *            - the new Trigger to associate the job with
     */
    void rescheduleAJob(String oldTriggerName, String oldTriggerGroup, Trigger newTrigger);

    /**
     * Pauses the job with the given jobId.
     *
     * @param jobId
     *            - the id of the job to be paused
     */
    public void pauseJob(String jobId);

    /**
     * Delete the identified Job from the Scheduler
     *
     * @param jobId
     *            - the id of the job to delete
     */
    public void deleteJob(String jobId);

    /**
     * Resumes the job with the given jobId.
     *
     * @param jobId
     *            - the id of the job to be resumed
     */
    public void resumeJob(String jobId);

    /**
     * Execute immediately the job with the given jobId.
     *
     * @param jobId
     *            - the id of the job to be triggered
     */
    public void triggerJob(String jobId);

    /**
     * Halts the Scheduler, and cleans up all resources associated with the
     * Scheduler. The scheduler cannot be re-started.
     */
    public void shutDown();

    /**
     * Starts the scheduler.
     */
    public void create();

    /**
     * @return the quartz scheduler wrapped by this SchedulerUtil
     */
    Scheduler getRawScheduler();

    /**
     * Schedules a cron job.
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
    String scheduleACronJob(Object instance,
                            String methodName,
                            Class<?>[] inputTypes,
                            Object[] inputParams,
                            String cronExpression);

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
    String scheduleACronJob(Object instance,
                            String methodName,
                            Class<?>[] inputTypes,
                            Object[] inputParams,
                            String cronExpression,
                            Date startAt,
                            Date endBy);


    /**
     * Schedules a job with a configurable delay.
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
     * @param timeUnit
     *            - the unit of time used for initialDelay and taskDelay.
     * @return the scheduled job id
     */
    String scheduleAConfigurableDelayJob(Object instance,
            String methodName,
            Class<?>[] inputTypes,
            Object[] inputParams,
            long initialDelay,
            String configurableDelayKeyName,
            TimeUnit timeUnit);

}

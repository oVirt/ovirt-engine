package org.ovirt.engine.core.utils.timer;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.impl.matchers.GroupMatcher.jobGroupEquals;

import java.io.IOException;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Singleton;

import org.apache.commons.lang.ClassUtils;
import org.ovirt.engine.core.utils.ResourceUtils;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;

@Singleton
public class DBSchedulerUtilQuartzImpl extends SchedulerUtilBaseImpl implements SchedulerUtil {

    @Override
    @PostConstruct
    public void create() {
        setup();
    }

    /*
     * retrieving the quartz scheduler from the factory.
     */
    public void setup() {
        final String QUARTZ_DB_PROPERTIES = "ovirt-db-scheduler.properties";
        Properties props = null;
        try {
            props = ResourceUtils.loadProperties(SchedulerUtil.class, QUARTZ_DB_PROPERTIES);
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Can't load properties from resource \"" +
                            QUARTZ_DB_PROPERTIES + "\".", exception);
        }
        setup(props);
    }

    public void setup(Properties props) {
        try {

            SchedulerFactory sf = new StdSchedulerFactory(props);
            sched = sf.getScheduler();
            if (sched != null) {
                sched.start();
                sched.getListenerManager().addJobListener(new FixedDelayJobListener(this),
                        jobGroupEquals(Scheduler.DEFAULT_GROUP));
            } else {
                log.error("there is a problem with the underlying Scheduler: null returned");
            }

        } catch (SchedulerException se) {
            log.error("there is a problem with the underlying Scheduler: {}", se.getMessage());
            log.debug("Exception", se);
        }
    }

    @PreDestroy
    public void teardown() {
        super.shutDown();
    }

    /**
     * To avoid data serialization issues for jobdata that is persisted in the database inputParams should be of
     * primitive or String type
     */
    @Override
    public String scheduleAFixedDelayJob(Object instance,
            String methodName,
            Class<?>[] inputTypes,
            Object[] inputParams,
            long initialDelay,
            long taskDelay,
            TimeUnit timeUnit) {
        if (!validate(instance, inputTypes)) {
            return null;
        }
        return super.scheduleAFixedDelayJob(instance,
                methodName,
                inputTypes,
                inputParams,
                initialDelay,
                taskDelay,
                timeUnit);
    }

    /**
     * To avoid data serialization issues for jobdata that is persisted in the database inpurParams should be of
     * primitive or String type
     */
    @Override
    public String scheduleAConfigurableDelayJob(Object instance,
            String methodName,
            Class<?>[] inputTypes,
            Object[] inputParams,
            long initialDelay,
            String configurableDelayKeyName,
            TimeUnit timeUnit) {
        if (!validate(instance, inputTypes)) {
            return null;
        }
        return super.scheduleAConfigurableDelayJob(instance,
                methodName,
                inputTypes,
                inputParams,
                initialDelay,
                configurableDelayKeyName,
                timeUnit);
    }

    /**
     * To avoid data serialization issues for jobdata that is persisted in the database inpurParams should be of
     * primitive or String type
     */
    @Override
    public String scheduleAOneTimeJob(Object instance,
            String methodName,
            Class<?>[] inputTypes,
            Object[] inputParams,
            long initialDelay,
            TimeUnit timeUnit) {
        if (!validate(instance, inputTypes)) {
            return null;
        }
        return super.scheduleAOneTimeJob(instance, methodName, inputTypes, inputParams, initialDelay, timeUnit);
    }

    /**
     * To avoid data serialization issues for jobdata that is persisted in the database inpurParams should be of
     * primitive or String type
     */
    @Override
    public String scheduleACronJob(Object instance,
            String methodName,
            Class<?>[] inputTypes,
            Object[] inputParams,
            String cronExpression) {
        if (!validate(instance, inputTypes)) {
            return null;
        }
        return super.scheduleACronJob(instance, methodName, inputTypes, inputParams, cronExpression);
    }

    @Override
    public String scheduleACronJob(Object instance,
            String methodName,
            Class<?>[] inputTypes,
            Object[] inputParams,
            String cronExpression,
            Date startAt,
            Date endBy) {
        if (!validate(instance, inputTypes)) {
            throw new RuntimeException("Failed to validate input parameters for scheduling. Only primitives or String values are allowed.");
        }
        return super.scheduleACronJob(instance, methodName, inputTypes, inputParams, cronExpression, startAt, endBy);
    }

    @Override
    protected JobDetail createJobWithBasicMapValues(Object instance,
            String methodName,
            Class<?>[] inputTypes,
            Object[] inputParams) {
        String jobName = generateUniqueNameForInstance(instance, methodName);

        boolean allowsConcurrent = JobWrapper.methodAllowsConcurrent(instance, methodName);
        Class<? extends Job> jobType = allowsConcurrent ? PersistentJobWrapper.class : PersistentSequentialJobWrapper.class;

        JobDetail job = newJob()
                .withIdentity(jobName, Scheduler.DEFAULT_GROUP)
                .ofType(jobType)
                .build();
        setBasicMapValues(job.getJobDataMap(), instance, methodName, inputTypes, inputParams);
        return job;
    }

    private boolean validate(Object instance, Class<?>[] inputTypes) {
        boolean validation = true;
        for (Class<?> cls : inputTypes) {
            if (!(cls.isPrimitive() || ClassUtils.wrapperToPrimitive(cls) != null || cls.isAssignableFrom(String.class))) {
                validation = false;
                log.error("Only primitives or String parameter types are supported for persistent jobs. '{}' is not supported",
                        cls.getSimpleName());
            }
        }
        return validation;
    }

    /*
     * The JobData for persistent jobs should contain only primitives We do NOT store the instance of the class passed,
     * only the name.
     */
    private void setBasicMapValues(JobDataMap data,
            Object instance,
            String methodName,
            Class<?>[] inputTypes,
            Object[] inputParams) {
        data.put(RUNNABLE_INSTANCE, instance.getClass().getName());
        data.put(RUN_METHOD_NAME, methodName);
        data.put(RUN_METHOD_PARAM, inputParams);
        data.put(RUN_METHOD_PARAM_TYPE, inputTypes);
    }

}

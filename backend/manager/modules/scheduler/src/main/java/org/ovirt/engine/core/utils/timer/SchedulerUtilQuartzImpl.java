package org.ovirt.engine.core.utils.timer;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.impl.matchers.GroupMatcher.jobGroupEquals;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Singleton;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;

@Singleton
public class SchedulerUtilQuartzImpl extends SchedulerUtilBaseImpl {

    /**
     * This method is called upon the bean creation as part
     * of the management Service bean lifecycle.
     */
    @Override
    @PostConstruct
    public void create() {
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
            sched.getListenerManager()
                    .addJobListener(new FixedDelayJobListener(this), jobGroupEquals(Scheduler.DEFAULT_GROUP));
        } catch (SchedulerException se) {
            log.error("there is a problem with the underlying Scheduler: {}", se.getMessage());
            log.debug("Exception", se);
        }
    }

    @PreDestroy
    public void teardown() {
        try {
            if (sched != null) {
                sched.shutdown();
            }
        } catch (SchedulerException e) {
            log.error("Failed to shutdown Quartz service: {}", e.getMessage());
            log.debug("Exception", e);
        }
    }

    @Override
    protected JobDetail createJobWithBasicMapValues(Object instance,
            String methodName,
            Class<?>[] inputTypes,
            Object[] inputParams) {

        boolean allowsConcurrent = JobWrapper.methodAllowsConcurrent(instance, methodName);
        Class<? extends Job> jobType = allowsConcurrent ? JobWrapper.class : SequentialJobWrapper.class;

        String jobName = generateUniqueNameForInstance(instance, methodName);
        JobDetail job = newJob()
                .withIdentity(jobName, Scheduler.DEFAULT_GROUP)
                .ofType(jobType)
                .build();
        setBasicMapValues(job.getJobDataMap(), instance, methodName, inputTypes, inputParams);
        return job;
    }

    private void setBasicMapValues(JobDataMap data,
            Object instance,
            String methodName,
            Class<?>[] inputTypes,
            Object[] inputParams) {
        data.put(RUNNABLE_INSTANCE, instance);
        data.put(RUN_METHOD_NAME, methodName);
        data.put(RUN_METHOD_PARAM, inputParams);
        data.put(RUN_METHOD_PARAM_TYPE, inputTypes);
    }

}

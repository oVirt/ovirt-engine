package org.ovirt.engine.core.utils.timer;

import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Singleton;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.impl.matchers.GroupMatcher.jobGroupEquals;

@Singleton
public class SchedulerUtilQuartzImpl extends SchedulerUtilBaseImpl {
    // for backward compatibility.
    private static SchedulerUtil instance;

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
        instance = this;
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

    /**
     * @deprecated prefer injecting with
     * <pre>
     *     {@code @Inject                        }<br>
     *     {@code SchedulerUtilQuartzImpl taskScheduler;    }
     * </pre>
     * or fetching one using {@linkplain org.ovirt.engine.di.Injector}
     * <pre>
     *     {@code Injector.get(SchedulerUtilQuartzImpl.class)        }
     * </pre>
     * @return a {@code SchedulerUtilQuartzImpl} instance
     */
    @Deprecated
    public static SchedulerUtil getInstance() {
        return instance;
    }

    @Override
    protected JobDetail createJobWithBasicMapValues(Object instance,
            String methodName,
            Class<?>[] inputTypes,
            Object[] inputParams) {
        String jobName = generateUniqueNameForInstance(instance, methodName);
        JobDetail job = newJob()
                .withIdentity(jobName, Scheduler.DEFAULT_GROUP)
                .ofType(JobWrapper.class)
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

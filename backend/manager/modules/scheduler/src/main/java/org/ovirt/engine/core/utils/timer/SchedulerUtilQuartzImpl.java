package org.ovirt.engine.core.utils.timer;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.impl.matchers.GroupMatcher.jobGroupEquals;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.DependsOn;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.ovirt.engine.core.utils.ejb.BeanProxyType;
import org.ovirt.engine.core.utils.ejb.BeanType;
import org.ovirt.engine.core.utils.ejb.EjbUtils;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;

// Here we use a Singleton bean, names Scheduler.
// The @Startup annotation is to make sure the bean is initialized on startup.
// @ConcurrencyManagement - we use bean managed concurrency:
// Singletons that use bean-managed concurrency allow full concurrent access to all the
// business and timeout methods in the singleton.
// The developer of the singleton is responsible for ensuring that the state of the singleton is synchronized across all clients.
@Singleton(name = "Scheduler")
@DependsOn("LockManager")
@Startup
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class SchedulerUtilQuartzImpl extends SchedulerUtilBaseImpl {
    /**
     * This method is called upon the bean creation as part
     * of the management Service bean lifecycle.
     */
    @Override
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
            sched.getListenerManager().addJobListener(new FixedDelayJobListener(this), jobGroupEquals(Scheduler.DEFAULT_GROUP));
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
     * Returns the single instance of this Class.
     *
     * @return a SchedulerUtil instance
     */
    public static SchedulerUtil getInstance() {
        return EjbUtils.findBean(BeanType.SCHEDULER, BeanProxyType.LOCAL);
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

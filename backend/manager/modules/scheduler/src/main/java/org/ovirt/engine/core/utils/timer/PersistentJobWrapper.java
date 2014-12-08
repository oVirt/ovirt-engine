package org.ovirt.engine.core.utils.timer;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class PersistentJobWrapper extends JobWrapper {

    private static final Logger log = LoggerFactory.getLogger(PersistentJobWrapper.class);

    /**
     * execute a method within an instance. The instance name and the method name are expected to be in the context
     * given object.
     * @param context
     *            the context for this job.
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        super.execute(context);
    }

    @Override
    protected Object getInstanceToRun(Map paramsMap) {
        String instanceName = (String) paramsMap.get(SchedulerUtilBaseImpl.RUNNABLE_INSTANCE);
        try {
            Class<?> clazz = Class.forName(instanceName);
            Constructor<?> constructor = clazz.getConstructor();
            Object instance = constructor.newInstance();
            return instance;
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException
                | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException e) {
            log.error("could not instantiate class '{}' due to error '{}'", instanceName, e.getMessage());
            log.debug("Exception", e);
            return null;
        }
    }

}

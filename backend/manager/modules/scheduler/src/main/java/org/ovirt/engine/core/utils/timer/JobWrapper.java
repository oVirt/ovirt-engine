package org.ovirt.engine.core.utils.timer;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The JobWrapper gives 2 functionalities: 1. Enable running a method within an
 * instance. 2. Enable running more than one method within a Class as a
 * scheduled method.
 */
public class JobWrapper implements Job {

    // static data members
    private static Map<String, Method> cachedMethods = new HashMap<String, Method>();
    private final Logger log = LoggerFactory.getLogger(SchedulerUtilQuartzImpl.class);

    /**
     * execute a method within an instance. The instance and the method name are
     * expected to be in the context given object.
     *
     * @param context
     *            the context for this job.
     */
    @SuppressWarnings("unchecked")
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        String methodName = null;
        try {
            JobDataMap data = context.getJobDetail().getJobDataMap();
            Map paramsMap = data.getWrappedMap();
            methodName = (String) paramsMap.get(SchedulerUtilQuartzImpl.RUN_METHOD_NAME);
            Object instance = paramsMap.get(SchedulerUtilQuartzImpl.RUNNABLE_INSTANCE);
            Object[] methodParams = (Object[]) paramsMap.get(SchedulerUtilQuartzImpl.RUN_METHOD_PARAM);
            String methodKey = getMethodKey(instance.getClass().getName(), methodName);
            Method methodToRun = cachedMethods.get(methodKey);
            if (methodToRun == null) {
                synchronized (cachedMethods) {
                    if (cachedMethods.containsKey(methodKey)) {
                        methodToRun = cachedMethods.get(methodKey);
                    } else {
                        methodToRun = getMethodToRun(instance, methodName);
                        if (methodToRun == null) {
                            log.error("could not find the required method '{}' on instance of {}",
                                    methodName, instance.getClass().getSimpleName());
                            return;
                        }

                        cachedMethods.put(methodKey, methodToRun);
                    }
                }
            }
            methodToRun.invoke(instance, methodParams);
        } catch (Exception e) {
            log.error("Failed to invoke scheduled method {}: {}", methodName, e.getMessage());
            log.debug("Exception", e);
            JobExecutionException jee = new JobExecutionException("failed to execute job");
            jee.setStackTrace(e.getStackTrace());
            throw jee;
        }
    }

    /**
     * go over the class methods and find the method with the
     * OnTimerMethodAnnotation and the methodId
     *
     * @param instance
     *            the instance of the class to look the methods on
     * @param methodId
     *            the id of the method as stated in the OnTimerMethodAnnotation
     *            annotation
     * @return the Method to run
     */
    private Method getMethodToRun(Object instance, String methodId) {
        Method methodToRun = null;
        Method[] methods = instance.getClass().getMethods();
        for (Method method : methods) {
            OnTimerMethodAnnotation annotation = method.getAnnotation(OnTimerMethodAnnotation.class);
            if (annotation != null && methodId.equals(annotation.value())) {
                methodToRun = method;
                break;
            }
        }
        return methodToRun;
    }

    /*
     * returns the key of the given method in the given class
     */
    private String getMethodKey(String className, String methodName) {
        return className + "." + methodName;
    }
}

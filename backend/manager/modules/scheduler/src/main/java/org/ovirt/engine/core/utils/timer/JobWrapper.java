package org.ovirt.engine.core.utils.timer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.ovirt.engine.core.utils.transaction.TransactionSupport;
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
    private static ConcurrentMap<String, Method> cachedMethods = new ConcurrentHashMap<>();
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
            methodName = (String) paramsMap.get(SchedulerUtilBaseImpl.RUN_METHOD_NAME);
            final Object instance = getInstanceToRun(paramsMap);
            final Object[] methodParams = (Object[]) paramsMap.get(SchedulerUtilBaseImpl.RUN_METHOD_PARAM);
            String methodKey = getMethodKey(instance.getClass().getName(), methodName);
            final Method methodToRun;
            if (!cachedMethods.containsKey(methodKey)) {
                cachedMethods.putIfAbsent(methodKey, getMethodToRun(instance, methodName));
            }
            methodToRun = cachedMethods.get(methodKey);
            invokeMethod(instance, methodToRun, methodParams);
        } catch (Exception e) {
            log.error("Failed to invoke scheduled method {}: {}", methodName, e.getMessage());
            log.debug("Exception", e);
            JobExecutionException jee = new JobExecutionException("failed to execute job");
            jee.setStackTrace(e.getStackTrace());
            throw jee;
        }
    }

    private void invokeMethod(final Object instance, final Method methodToRun, final Object[] methodParams)
            throws Exception, IllegalAccessException, InvocationTargetException {
        OnTimerMethodAnnotation annotation = methodToRun.getAnnotation(OnTimerMethodAnnotation.class);
        if (annotation.transactional()) {
            Exception e = TransactionSupport.executeInNewTransaction(() -> {
                try {
                    methodToRun.invoke(instance, methodParams);
                } catch (Exception e1) {
                    return e1;
                }
                return null;
            });
            if (e != null) {
                throw e;
            }
        } else {
            methodToRun.invoke(instance, methodParams);
        }
    }

    protected Object getInstanceToRun(Map paramsMap) {
        return paramsMap.get(SchedulerUtilBaseImpl.RUNNABLE_INSTANCE);
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
    protected static Method getMethodToRun(Object instance, String methodId) {
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

    /**
     * Check whether the specified timed method (id must match the OnTimerMethodAnnotation)
     * allows concurrent execution.
     *
     * @param instance
     *            the instance of the class to look the methods on
     * @param methodId
     *            the id of the method as stated in the OnTimerMethodAnnotation
     *            annotation
     * @return true when concurrent executions are allowed or false when they are not
     */
    protected static boolean methodAllowsConcurrent(Object instance, String methodId) {
        Method method = getMethodToRun(instance, methodId);
        OnTimerMethodAnnotation methodToRunAnnotation = method.getAnnotation(OnTimerMethodAnnotation.class);
        return methodToRunAnnotation == null || methodToRunAnnotation.allowsConcurrent();
    }

    /*
     * returns the key of the given method in the given class
     */
    private String getMethodKey(String className, String methodName) {
        return className + "." + methodName;
    }
}

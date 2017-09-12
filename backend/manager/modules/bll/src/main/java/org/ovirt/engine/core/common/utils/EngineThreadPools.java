package org.ovirt.engine.core.common.utils;

import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.enterprise.concurrent.ManagedThreadFactory;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.ovirt.engine.core.common.BackendService;
import org.ovirt.engine.core.utils.EngineLocalConfig;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;
import org.ovirt.engine.core.utils.threadpool.ThreadPools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class EngineThreadPools implements BackendService {

    private static final Logger log = LoggerFactory.getLogger(EngineThreadPools.class);

    public static final String COMMAND_COORDINATOR_POOL_NAME = "java:jboss/ee/concurrency/executor/commandCoordinator";
    public static final String HOST_UPDATES_CHECKER_POOL_NAME = "java:jboss/ee/concurrency/executor/hostUpdatesChecker";
    public static final String ENGINE_SCHEDULED_POOL_NAME =
            "java:jboss/ee/concurrency/scheduler/engineScheduledThreadPool";
    public static final String ENGINE_THREAD_MONITORING_POOL_NAME =
            "java:jboss/ee/concurrency/scheduler/engineThreadMonitoringThreadPool";
    public static final String ENGINE_THREAD_FACTORY_NAME = "java:jboss/ee/concurrency/factory/engine";

    @Resource(lookup = EngineThreadPools.COMMAND_COORDINATOR_POOL_NAME)
    private ManagedExecutorService cocoPool;

    @Resource(lookup = EngineThreadPools.HOST_UPDATES_CHECKER_POOL_NAME)
    private ManagedExecutorService hostUpdatesCheckerPool;

    @Resource(lookup = EngineThreadPools.ENGINE_SCHEDULED_POOL_NAME)
    private ManagedScheduledExecutorService engineScheduledThreadPool;

    @Resource(lookup = EngineThreadPools.ENGINE_THREAD_MONITORING_POOL_NAME)
    private ManagedScheduledExecutorService engineThreadMonitoringThreadPool;

    @Resource(lookup = ENGINE_THREAD_FACTORY_NAME)
    private static ManagedThreadFactory threadFactory;

    @PostConstruct
    private void init() {
        // initialize ThreadPoolUtil
        ThreadPoolUtil.setExecutorService(
                new InternalThreadExecutor(
                        "EngineThreadPool",
                        EngineLocalConfig.getInstance().getInteger("ENGINE_THREAD_POOL_MIN_SIZE"),
                        EngineLocalConfig.getInstance().getInteger("ENGINE_THREAD_POOL_MAX_SIZE"),
                        EngineLocalConfig.getInstance().getInteger("ENGINE_THREAD_POOL_QUEUE_SIZE")));
    }

    @Produces
    @ThreadPools(ThreadPools.ThreadPoolType.CoCo)
    public ManagedExecutorService cocoPoolProducer() {
        return cocoPool;
    }

    @Produces
    @ThreadPools(ThreadPools.ThreadPoolType.HostUpdatesChecker)
    public ManagedExecutorService hostUpdatesCheckerPoolProducer() {
        return hostUpdatesCheckerPool;
    }

    @Produces
    @ThreadPools(ThreadPools.ThreadPoolType.EngineScheduledThreadPool)
    public ManagedScheduledExecutorService engineScheduledThreadPoolProducer() {
        return engineScheduledThreadPool;
    }

    @Produces
    @ThreadPools(ThreadPools.ThreadPoolType.EngineThreadMonitoringThreadPool)
    public ManagedScheduledExecutorService engineThreadMonitoringThreadPoolProducer() {
        return engineThreadMonitoringThreadPool;
    }

    private static class InternalThreadExecutor extends ThreadPoolExecutor implements DynamicMBean {

        private static final String CORE_THREADS = "CORE_THREADS";
        private static final String MAX_THREADS = "MAX_THREADS";
        private static final String CORE_THREADS_DESC = "The number of threads to keep in the executor's pool";
        private static final String MAX_THREADS_DESC = "The maximum number of threads used by the executor";

        private Map<String, String> descriptions = new HashMap<>();
        private Map<String, Integer> properties = new HashMap<>();

        public InternalThreadExecutor(String name, int coreThreads, int maxThreads, int queueSize) {
            super(coreThreads,
                    maxThreads,
                    60L,
                    TimeUnit.SECONDS,
                    new ArrayBlockingQueue<>(queueSize),
                    threadFactory,
                    new ThreadPoolExecutor.CallerRunsPolicy());
            init(name, coreThreads, maxThreads);
        }

        private void init(String name, int coreThreads, int maxThreads) {
            descriptions.put(CORE_THREADS, CORE_THREADS_DESC);
            descriptions.put(MAX_THREADS, MAX_THREADS_DESC);
            properties.put(CORE_THREADS, coreThreads);
            properties.put(MAX_THREADS, maxThreads);
            this.allowCoreThreadTimeOut(true);
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            String mbeanName = name + ":type=" + this.getClass().getName();
            try {
                mbs.registerMBean(this, new ObjectName(mbeanName));
            } catch (Exception e) {
                log.info("Problem during registration of {} into JMX: {}",
                        name,
                        ExceptionUtils.getRootCauseMessage(e));
                log.debug("Exception", e);
                throw new IllegalStateException("Problem during registration of EngineThreadPool into JMX:" + e);
            }
        }

        @Override
        public Object getAttribute(String name) throws AttributeNotFoundException, MBeanException, ReflectionException {
            Integer value = this.properties.get(name);
            if (value != null) {
                return value;
            }
            throw new AttributeNotFoundException("No such property: " + name);
        }

        @Override
        public void setAttribute(Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
            String name = attribute.getName();
            if (this.properties.get(name) == null) {
                throw new AttributeNotFoundException(name);
            }
            Object value = attribute.getValue();
            if (!(value instanceof Integer)) {
                throw new InvalidAttributeValueException("Attribute value not a integer: " + value);
            }
            switch(name) {
                case CORE_THREADS:
                    this.setCorePoolSize((Integer) value);
                    break;
                case MAX_THREADS:
                    this.setMaximumPoolSize((Integer) value);
                    break;
            }
            this.properties.put(name, (Integer) value);
        }

        @Override
        public AttributeList getAttributes(String[] names) {
            AttributeList list = new AttributeList();
            Arrays.asList(names)
                    .stream()
                    .filter(name -> properties.containsKey(name))
                    .map(name -> new Attribute(name, properties.get(name)))
                    .forEach(attr -> list.add(attr));
            return list;
        }

        @Override
        public AttributeList setAttributes(AttributeList list) {
            AttributeList retlist = new AttributeList();
            list.asList()
                    .stream()
                    .filter(attr -> attr.getValue() instanceof Integer)
                    .forEach(attr -> {
                        properties.put(attr.getName(), (Integer) attr.getValue());
                        retlist.add(new Attribute(attr.getName(), attr.getValue()));
                    });
            return retlist;
        }

        @Override
        public Object invoke(String s, Object[] objects, String[] strings) throws MBeanException, ReflectionException {
            return null;
        }

        @Override
        public MBeanInfo getMBeanInfo() {
            List<MBeanAttributeInfo> attrs =
                    properties.keySet()
                            .stream()
                            .map(name -> new MBeanAttributeInfo(name,
                                    "java.lang.Integer",
                                    descriptions.get(name),
                                    true,
                                    true,
                                    false))
                            .collect(Collectors.toList());
            return new MBeanInfo(getClass().getName(),
                    "Property Manager MBean",
                    attrs.toArray(new MBeanAttributeInfo[0]),
                    null,
                    null,
                    null);
        }
    }
}

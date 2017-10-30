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

import javax.enterprise.concurrent.ManagedThreadFactory;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InternalThreadExecutor extends ThreadPoolExecutor implements DynamicMBean {

    private static final Logger log = LoggerFactory.getLogger(InternalThreadExecutor.class);

    private static final String CORE_THREADS = "CORE_THREADS";
    private static final String MAX_THREADS = "MAX_THREADS";
    private static final String CORE_THREADS_DESC = "The number of threads to keep in the executor's pool";
    private static final String MAX_THREADS_DESC = "The maximum number of threads used by the executor";

    private Map<String, String> descriptions = new HashMap<>();
    private Map<String, Integer> properties = new HashMap<>();

    public InternalThreadExecutor(String name,
            ManagedThreadFactory threadFactory,
            int coreThreads,
            int maxThreads,
            int queueSize) {
        super(coreThreads,
                maxThreads,
                60L,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(queueSize),
                threadFactory,
                new CallerRunsPolicy());
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
    public void setAttribute(Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException,
            MBeanException, ReflectionException {
        String name = attribute.getName();
        if (this.properties.get(name) == null) {
            throw new AttributeNotFoundException(name);
        }
        Object value = attribute.getValue();
        if (!(value instanceof Integer)) {
            throw new InvalidAttributeValueException("Attribute value not a integer: " + value);
        }
        switch (name) {
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

    public int getTasksInQueue() {
        return super.getQueue().size();
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

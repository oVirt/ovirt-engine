/**
 *
 */
package org.ovirt.engine.core.utils.ejb;


/**
 *
 */
public class EngineEJBUtilsStrategy extends EJBUtilsStrategy {

    public static final String ENGINE_CONTEXT_PREFIX = "java:global/engine/";

    @Override
    protected void addJNDIBeans() {
        addBeanJNDIName(BeanType.BACKEND, ENGINE_CONTEXT_PREFIX.concat("bll/Backend"));
        addBeanJNDIName(BeanType.SCHEDULER, ENGINE_CONTEXT_PREFIX.concat("scheduler/Scheduler"));
        addBeanJNDIName(BeanType.PERSISTENT_SCHEDULER, ENGINE_CONTEXT_PREFIX.concat("scheduler/PersistentScheduler"));
        addBeanJNDIName(BeanType.VDS_EVENT_LISTENER, ENGINE_CONTEXT_PREFIX.concat("bll/VdsEventListener"));
        addBeanJNDIName(BeanType.LOCK_MANAGER, ENGINE_CONTEXT_PREFIX.concat("bll/LockManager"));
        addBeanJNDIName(BeanType.EVENTQUEUE_MANAGER,  ENGINE_CONTEXT_PREFIX.concat("bll/EventQueue"));
        addBeanJNDIName(BeanType.CACHE_CONTAINER,  "java:jboss/infinispan/ovirt-engine");
    }

    @Override
    protected String getBeanSuffix(BeanType beanType, BeanProxyType proxyType) {
        String suffix = "";
        if (beanType.equals(BeanType.BACKEND)) {
            if (proxyType.equals(BeanProxyType.LOCAL)) {
                suffix = "!org.ovirt.engine.core.bll.interfaces.BackendInternal";
            } else {
                suffix = "!org.ovirt.engine.core.bll.BackendRemote";
            }
        }

        return suffix;
    }
}

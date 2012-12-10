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
        addBeanJNDIName(BeanType.BACKEND, ENGINE_CONTEXT_PREFIX.concat("engine-bll/Backend"));
        addBeanJNDIName(BeanType.SCHEDULER, ENGINE_CONTEXT_PREFIX.concat("engine-scheduler/Scheduler"));
        addBeanJNDIName(BeanType.USERS_DOMAINS_CACHE,
                ENGINE_CONTEXT_PREFIX.concat("engine-bll/UsersDomainsCacheManagerService"));
        addBeanJNDIName(BeanType.VDS_EVENT_LISTENER, ENGINE_CONTEXT_PREFIX.concat("engine-bll/VdsEventListener"));
        addBeanJNDIName(BeanType.LOCK_MANAGER, ENGINE_CONTEXT_PREFIX.concat("engine-bll/LockManager"));
        addBeanJNDIName(BeanType.EVENTQUEUE_MANAGER,  ENGINE_CONTEXT_PREFIX.concat("engine-bll/EventQueue"));
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

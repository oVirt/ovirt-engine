/**
 *
 */
package org.ovirt.engine.core.utils.ejb;

import org.ovirt.engine.core.utils.StringUtils;

/**
 *
 */
public class EngineEJBUtilsStrategy extends EJBUtilsStrategy {

    public static final String ENGINE_CONTEXT_PREFIX = "java:global/engine/";

    @Override
    protected void addJNDIBeans() {
        addBeanJNDIName(BeanType.BACKEND,
                        StringUtils.concat(ENGINE_CONTEXT_PREFIX, "engine-bll/Backend"));
        addBeanJNDIName(BeanType.VDS_BROKER, StringUtils.concat(ENGINE_CONTEXT_PREFIX, "engine-vdsbroker/VdsBroker"));
        addBeanJNDIName(BeanType.SCHEDULER, StringUtils.concat(ENGINE_CONTEXT_PREFIX, "engine-scheduler/Scheduler"));
        addBeanJNDIName(BeanType.USERS_DOMAINS_CACHE,
                        StringUtils.concat(ENGINE_CONTEXT_PREFIX, "engine-bll/UsersDomainsCacheManagerService"));
        addBeanJNDIName(BeanType.VDS_EVENT_LISTENER,
                StringUtils.concat(ENGINE_CONTEXT_PREFIX, "engine-bll/VdsEventListener"));
        addBeanJNDIName(BeanType.LOCK_MANAGER, StringUtils.concat(ENGINE_CONTEXT_PREFIX, "engine-bll/LockManager"));
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

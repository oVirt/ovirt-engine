/**
 *
 */
package org.ovirt.engine.core.utils.ejb;

import org.ovirt.engine.core.utils.StringUtils;

/**
 *
 */
public class EngineEJBUtilsStrategy extends EJBUtilsStrategy {

    public static final String ENGINE_CONTEXT_PREFIX = "engine/";

    @Override
    protected void addJNDIBeans() {
        addBeanJNDIName(BeanType.BACKEND, StringUtils.concat(ENGINE_CONTEXT_PREFIX, "Backend"));
        addBeanJNDIName(BeanType.VDS_BROKER, StringUtils.concat(ENGINE_CONTEXT_PREFIX, "VdsBroker"));
        addBeanJNDIName(BeanType.SCHEDULER, StringUtils.concat(ENGINE_CONTEXT_PREFIX, "Scheduler"));
        addBeanJNDIName(BeanType.USERS_DOMAINS_CACHE,
                StringUtils.concat(ENGINE_CONTEXT_PREFIX, "UsersDomainsCacheManagerService"));
        addBeanJNDIName(BeanType.VDS_EVENT_LISTENER, StringUtils.concat(ENGINE_CONTEXT_PREFIX, "VdsEventListener"));
    }

}

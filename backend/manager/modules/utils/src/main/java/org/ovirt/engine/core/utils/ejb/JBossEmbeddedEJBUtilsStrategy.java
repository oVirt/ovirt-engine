/**
 *
 */
package org.ovirt.engine.core.utils.ejb;



/**
 * Strategy for JNDI lookups under jboss embedded
 *
 *
 */
public class JBossEmbeddedEJBUtilsStrategy extends EJBUtilsStrategy {

    @Override
    protected void addJNDIBeans() {
        addBeanJNDIName(BeanType.BACKEND, "Backend");
        addBeanJNDIName(BeanType.VDS_BROKER, "VdsBroker");
        addBeanJNDIName(BeanType.SCHEDULER, "Scheduler");
        addBeanJNDIName(BeanType.USERS_DOMAINS_CACHE, "UsersDomainsCacheManagerService");
        addBeanJNDIName(BeanType.VDS_EVENT_LISTENER, "VdsEventListener");
        addBeanJNDIName(BeanType.LOCK_MANAGER, "LockManager");
    }

    @Override
    protected String getBeanSuffix(BeanType beanType, BeanProxyType proxyType) {
        return "";
    }

}

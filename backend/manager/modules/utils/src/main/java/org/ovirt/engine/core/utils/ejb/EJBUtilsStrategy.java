package org.ovirt.engine.core.utils.ejb;

import java.util.HashMap;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * EJBUtils strategy to fine tune the lookup process of beans
 *
 *
 */
public abstract class EJBUtilsStrategy {

    private static final Logger log = LoggerFactory.getLogger(EJBUtilsStrategy.class);

    // Map from resource types (for example ResourceTypeEnum.DATA_SOURCE ) to
    // their JNDI names
    protected HashMap<ContainerManagedResourceType, String> resourceTypeToJNDINameMap = new HashMap<>();

    // Map from bean types (for example BeanTypeEnum.BACKEND) to their JNDI
    // names
    protected HashMap<BeanType, String> beanTypeToJNDINameMap = new HashMap<>();

    private static final Map<String, Object> cachedJNDIReferences = new HashMap<>();

    protected EJBUtilsStrategy() {
        // Adds JNDI resources,
        addJNDIResources();
        // Adds JNDI beans - the implementation is in base classes
        addJNDIBeans();

    }

    protected abstract void addJNDIBeans();

    protected abstract String getBeanSuffix(BeanType beanType, BeanProxyType proxyType);

    protected void addJNDIResources() {
        addResourceJNDIName(ContainerManagedResourceType.TRANSACTION_MANAGER, "java:jboss/TransactionManager");
        addResourceJNDIName(ContainerManagedResourceType.DATA_SOURCE, "java:/ENGINEDataSource");
    }

    private static InitialContext context;

    private static synchronized InitialContext getInitialContext() throws NamingException {

        if (context == null) {
            context = new InitialContext();
        }

        return context;
    }

    /**
     * Finds a bean according to its JNDI name
     *
     * @param <T>
     *            interface that the bean implements
     * @return proxy to bean
     */
    public <T> T findResource(ContainerManagedResourceType resourceValue) {
        String jndiNameFromMap = getResourceJNDIName(resourceValue);
        if (jndiNameFromMap == null) {
            log.error("No JNDI name for '{}'", resourceValue);
            return null;
        }

        try {
            return getReference(jndiNameFromMap);
        } catch (NamingException ex) {
            log.error("Error looking up resource '{}'", resourceValue);
            return null;
        }
    }

    /**
     * Finds a bean according to the bean type and proxy type
     */
    public <T> T findBean(BeanType beanType, BeanProxyType proxyType) {

        String jndiNameFromMap = null;
        StringBuilder jndiNameSB = null;
        Context context = null;

        try {
            jndiNameFromMap = getBeanJNDIName(beanType);
            if (jndiNameFromMap == null) {
                log.error("No JNDI name for '{}'", beanType);
                return null;
            }

            jndiNameSB = new StringBuilder();
            jndiNameSB.append(jndiNameFromMap);
            jndiNameSB.append(getBeanSuffix(beanType, proxyType));

            if (proxyType == BeanProxyType.LOCAL) {
                context = getInitialContext();
            }

            // appends "local" or "remote" to the jndi name, depends on the
            // proxy type
            if (context != null) {
                return getReference(jndiNameSB.toString());
            } else {
                log.error("Failed to create InitialContext which is currently null," +
                        " possibly because given BeanProxyType is null. Given BeanProxyType: {}",
                        proxyType == null ? "is null" : proxyType.toString());
                throw new NullPointerException();
            }

        } catch (Exception e) {
            log.error("Failed to lookup resource type '{}'. JNDI name '{}'. Error: {}",
                    beanType, jndiNameSB, e.getMessage());
            log.debug("Exception", e);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private synchronized <T> T getReference(String refName) throws NamingException {
        T reference = (T) cachedJNDIReferences.get(refName);
        if (reference != null) {
            return reference;
        }

        reference = (T) getInitialContext().lookup(refName);
        cachedJNDIReferences.put(refName, reference);

        return reference;

    }

    protected void addResourceJNDIName(ContainerManagedResourceType aResourceEnumValue, String aJNDIName) {
        resourceTypeToJNDINameMap.put(aResourceEnumValue, aJNDIName);
    }

    protected void addBeanJNDIName(BeanType aEnumValue, String aJNDIName) {
        beanTypeToJNDINameMap.put(aEnumValue, aJNDIName);
    }

    protected String getResourceJNDIName(ContainerManagedResourceType aResourceEnumValue) {
        return resourceTypeToJNDINameMap.get(aResourceEnumValue);
    }

    protected String getBeanJNDIName(BeanType aEnumValue) {
        return beanTypeToJNDINameMap.get(aEnumValue);
    }

}

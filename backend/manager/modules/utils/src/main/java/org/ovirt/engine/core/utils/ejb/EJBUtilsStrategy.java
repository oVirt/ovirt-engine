package org.ovirt.engine.core.utils.ejb;

import java.util.HashMap;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

/**
 * EJBUtils strategy to fine tune the lookup process of beans
 *
 *
 */
public abstract class EJBUtilsStrategy {

    private static Log log = LogFactory.getLog(EJBUtilsStrategy.class);

    // Map from resource types (for example ResourceTypeEnum.DATA_SOURCE ) to
    // their JNDI names
    protected HashMap<ContainerManagedResourceType, String> resourceTypeToJNDINameMap =
            new HashMap<ContainerManagedResourceType, String>();

    // Map from bean types (for example BeanTypeEnum.BACKEND) to their JNDI
    // names
    protected HashMap<BeanType, String> beanTypeToJNDINameMap = new HashMap<BeanType, String>();

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
     * @param name
     *            JNDI name
     * @return proxy to bean
     */
    public <T> T findResource(ContainerManagedResourceType resourceValue) {
        String jndiNameFromMap = getResourceJNDIName(resourceValue);
        if (jndiNameFromMap == null) {
            log.error("No JNDI name for : " + resourceValue);
            return null;
        }

        try {
            return getReference(jndiNameFromMap);
        } catch (NamingException ex) {
            log.error("Error looking up resource " + resourceValue);
            return null;
        }
    }

    /**
     * Finds a bean according to the bean type and proxy type
     *
     * @param <T>
     * @param aBeanType
     * @param aType
     * @return
     */
    public <T> T findBean(BeanType beanType, BeanProxyType proxyType) {

        String jndiNameFromMap = null;
        StringBuilder jndiNameSB = null;
        Context context = null;

        try {
            jndiNameFromMap = getBeanJNDIName(beanType);
            if (jndiNameFromMap == null) {
                log.error("No JNDI name for : " + beanType);
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
                log.errorFormat("Failed to create InitialContext which is currently null," +
                        " possibly because given BeanProxyType is null. Given BeanProxyType: {0}",
                        ((proxyType == null) ? "is null" : proxyType.toString()));
                throw new NullPointerException();
            }

        } catch (Exception e) {
            StringBuilder errorMsgSb = new StringBuilder();
            errorMsgSb.append("Failed to lookup resource type: ").append(beanType).append(". JNDI name: ")
                    .append(jndiNameSB);
            log.error(errorMsgSb.toString(), e);
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

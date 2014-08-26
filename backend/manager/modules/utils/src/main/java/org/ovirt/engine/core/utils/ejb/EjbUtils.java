package org.ovirt.engine.core.utils.ejb;

public class EjbUtils {

    private static EJBUtilsStrategy strategy = new EngineEJBUtilsStrategy();

    /**
     * looks up a bean
     *
     * @param <T>
     *            type of bean
     * @param aBeanEnumValue
     *            enum literal representing the bean
     * @param aType
     *            proxy type (local or remote)
     * @return proxy to the bean
     */
    public static <T> T findBean(BeanType aBeanEnumValue, BeanProxyType aType) {
        return strategy.<T> findBean(aBeanEnumValue, aType);
    }

    /**
     * Finds a resource managed by the container (data source, transaction manager...)
     *
     * @param <T>
     *            Type of the resource
     * @param aResourceValue
     *            enum literal representing the resource
     * @return proxy to the resource
     */
    public static <T> T findResource(ContainerManagedResourceType aResourceValue) {
        return strategy.<T> findResource(aResourceValue);
    }

    /**
     * Sets a new strategy object that will affect the behavior of EJB utils lookup
     *
     * @param strategyToSet
     *            the new strategy to set
     */
    public static void setStrategy(EJBUtilsStrategy strategyToSet) {
        strategy = strategyToSet;
    }

    /**
     * @return the strategy object that will affect the behavior of EJB utils lookup
     */
    public static EJBUtilsStrategy getStrategy() {
        return strategy;
    }
}

package org.ovirt.engine.core.di;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.util.AnnotationLiteral;

/**
 * an application wide interaction point with the CDI container mostly to gap all the existing unmanaged code
 * or for unmanaged code which wants interaction with managed beans.
 * Typically this injector could be used anywhere to get a manage instance from instances which
 * aren't managed like some utility singletons etc
 */
public class Injector {

    private static Map<Class, InjectionTarget> injectionTargets = new ConcurrentHashMap<>();

    /**
     * This method will take an instance and will fulfill all its dependencies, which are members
     * annotated with <code>@Inject</code>.
     * @param instance unmanaged CDI bean, essentially a regular object which is not managed by
     *                  the CDI container.
     * @param <T> an unmanaged CDI instance with some members containing <code>@Inject</code> annotated
     *           members
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T> T injectMembers(T instance) {
        InjectionTarget injectionTarget = injectionTargets.computeIfAbsent(
                instance.getClass(),
                clazz -> {
                    AnnotatedType type = CDI.current().getBeanManager().createAnnotatedType(instance.getClass());
                    return CDI.current().getBeanManager().createInjectionTarget(type);
                });

        injectionTarget.inject(instance, CDI.current().getBeanManager().createCreationalContext(null));
        injectionTarget.postConstruct(instance);
        return instance;
    }

    /**
     * This method will fetch a managed CDI bean from the CDI container.
     * Using this method should help us bridge all places where we are in unmanaged instances
     * and we want an already managed instance. e.g all our Singletons getInstance methods are candidates for this usage,
     * meaning we Make the Singleton a managed bean but let the existing code still
     * get a reference by invoking getInstance which delegate to this method.
     * @param clazz the Runtime class representing the desired instance
     * @return the instance of type <code><T></T></code> which is managed by the CDI container
     */
    public static <T extends Object> T get(Class<T> clazz) {
        return CDI.current().select(clazz, DefaultLiteral.INSTANCE).get();
    }

    public static class DefaultLiteral extends AnnotationLiteral<Default> implements Default {
        public static final DefaultLiteral INSTANCE = new DefaultLiteral();
    }

    /**
     * Returns the {@link BeanManager} the {@code Injector} uses. This methods exists so it can be mocked away in the
     * tests that need to ignore the manager.
     */
    public BeanManager getManager() {
        return CDI.current().getBeanManager();
    }

}

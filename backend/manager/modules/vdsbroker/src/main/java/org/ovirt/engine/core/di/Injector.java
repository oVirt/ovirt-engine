package org.ovirt.engine.core.di;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * an application wide interaction point with the CDI container mostly to gap all the existing unmanaged code
 * or for unmanaged code which wants interaction with managed beans.
 * Typically this injector could be used anywhere to get a manage instance from instances which
 * aren't managed like some utility singletons etc
 */
@Singleton
public class Injector {

    private static Injector injector;

    @Inject
    private BeanManager manager;

    @PostConstruct
    private void init() {
        injector = this;
    }

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
        AnnotatedType type = injector.manager.createAnnotatedType(instance.getClass());
        InjectionTarget injectionTarget = injector.manager.createInjectionTarget(type);
        injectionTarget.inject(instance, injector.manager.createCreationalContext(null));
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
        return injector.instanceOf(clazz);
    }

    /**
     * convenience method, good for mocking and whoever holds a direct instance of Injector in hand.<br>
     * after all its a jdk "bug" to call a static method on an instance.<br>
     *{@link Injector#get(Class)} should supply the same behavior exactly
     * @return instance of T
     * @see #get(Class)
     */
    @SuppressWarnings("unchecked")
    public <T extends Object> T instanceOf(Class<T> clazz) {
        Bean<?> bean = injector.manager.getBeans(clazz).iterator().next();
        return (T) injector.manager.getReference(bean, clazz, injector.manager.createCreationalContext(bean));
    }
}

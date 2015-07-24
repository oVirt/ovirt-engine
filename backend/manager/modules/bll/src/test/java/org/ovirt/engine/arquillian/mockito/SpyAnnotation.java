package org.ovirt.engine.arquillian.mockito;

import static org.mockito.Mockito.spy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.ProcessBean;
import javax.enterprise.inject.spi.ProcessInjectionTarget;
import javax.enterprise.inject.spi.ProcessProducer;
import javax.enterprise.inject.spi.Producer;

import org.mockito.Spy;

/**
 * Allow the usage of Mockito's @Spy annotation within Arquillian managed tests.
 * <p>
 * This class hooks itself into the bean creation process and wraps a spy around injected resources.
 * To make use of this, the injection point in the test class must be annotated with @Inject and @Spy.
 * <p>
 * The class is heavily inspired by https://github.com/topikachu/arquillian-extension-mockito
 */
public class SpyAnnotation implements Extension {
    private Set<Class> spiedClasses = new HashSet<>();

    public <X> void processBean(@Observes ProcessBean<X> event) {
        for (InjectionPoint injectionPoint : event.getBean().getInjectionPoints()) {
            Spy spy = injectionPoint.getAnnotated().getAnnotation(Spy.class);
            if (spy != null) {
                final Type spiedType = injectionPoint.getAnnotated().getBaseType();
                if (spiedType instanceof Class) {
                    spiedClasses.add((Class) spiedType);
                } else if (spiedType instanceof ParameterizedType) {
                    spiedClasses.add((Class) ((ParameterizedType) spiedType).getActualTypeArguments()[0]);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <T> void processInjectTarget(@Observes final ProcessInjectionTarget<T> event) {
        event.setInjectionTarget((InjectionTarget<T>) Proxy.newProxyInstance(
                SpyAnnotation.class.getClassLoader(),
                new Class[] { InjectionTarget.class },
                new ProducerInvocationHandler(event.getInjectionTarget())));
    }

    @SuppressWarnings("unchecked")
    public <T, X> void processProducer(@Observes final ProcessProducer<T, X> event) {
        event.setProducer((Producer<X>) Proxy.newProxyInstance(
                SpyAnnotation.class.getClassLoader(),
                new Class[] { Producer.class },
                new ProducerInvocationHandler(event.getProducer())));
    }

    class ProducerInvocationHandler implements InvocationHandler {

        private Producer<?> producer;

        public ProducerInvocationHandler(Producer<?> producer) {
            this.producer = producer;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Object object = method.invoke(producer, args);
            if (object != null && method.getName().equals("produce") && spiedClasses.contains(object.getClass())) {
                object = spy(object);
            }
            return object;
        }
    }
}

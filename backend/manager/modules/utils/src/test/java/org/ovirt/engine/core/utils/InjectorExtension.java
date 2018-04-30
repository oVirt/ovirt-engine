package org.ovirt.engine.core.utils;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.enterprise.util.TypeLiteral;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.support.AnnotationSupport;

/**
 * An extension to inject mocks to the Injector.
 * In order to use it, just add it as an extension (e.g., {@code @ExtendWith(InjectorExtension.class)}), and add a
 * {@link InjectedMock} annotation to any <strong>public</strong> field you want injected in that way.
 */
public class InjectorExtension implements BeforeEachCallback, AfterEachCallback {

    private static final Map<Class<?>, Object> beansCache = new ConcurrentHashMap<>();

    static {
        CDI.setCDIProvider(TestCDIProvider::new);
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) throws Exception {
        List<Field> fields =
                AnnotationSupport.findPublicAnnotatedFields(extensionContext.getTestClass().get(), Object.class, InjectedMock.class);

        for (Field field : fields) {
            beansCache.put(field.getType(), field.get(extensionContext.getTestInstance().get()));
        }
    }

    @Override
    public void afterEach(ExtensionContext extensionContext) throws Exception {
        beansCache.clear();
    }


    /* Inner utils classes */

    private static class TestCDIProvider<T> extends CDI<T> {
        @Override
        public BeanManager getBeanManager() {
            return mock(BeanManager.class, RETURNS_DEEP_STUBS);
        }

        @Override
        public Instance<T> select(Annotation... var1) {
            return null;
        }

        @Override
        public <U extends T> Instance<U> select(TypeLiteral<U> typeLiteral, Annotation... annotations) {
            return null;
        }

        @Override
        public boolean isUnsatisfied() {
            return false;
        }

        @Override
        public boolean isAmbiguous() {
            return false;
        }

        @Override
        public void destroy(T t) {
        }

        @Override
        @SuppressWarnings("unchecked")
        public <U extends T> Instance<U> select(Class<U> aClass, Annotation... annotations) {
            return new SimpleInstanceIdGenerator<>((U) beansCache.get(aClass));
        }

        @Override
        public Iterator<T> iterator() {
            return null;
        }

        @Override
        public T get() {
            return null;
        }
    }

    private static class SimpleInstanceIdGenerator<T> implements Instance<T> {
        private final T value;

        public SimpleInstanceIdGenerator(T value) {
            this.value = value;
        }

        @Override
        public T get() {
            return value;
        }

        @Override
        public Instance<T> select(Annotation... annotations) {
            return null;
        }

        @Override
        public <U extends T> Instance<U> select(Class<U> aClass, Annotation... annotations) {
            return null;
        }

        @Override
        public <U extends T> Instance<U> select(TypeLiteral<U> typeLiteral, Annotation... annotations) {
            return null;
        }

        @Override
        public boolean isUnsatisfied() {
            return false;
        }

        @Override
        public boolean isAmbiguous() {
            return false;
        }

        @Override
        public void destroy(T u) {
        }

        @Override
        public Iterator<T> iterator() {
            return null;
        }
    }
}

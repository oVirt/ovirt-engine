package org.ovirt.engine.core.di;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.enterprise.util.TypeLiteral;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

public class InjectorRule extends TestWatcher {

    private static final Map<Class<?>, Object> beansCache = new ConcurrentHashMap<>();

    static {
        CDI.setCDIProvider(TestCDIProvider::new);
    }

    @Override
    protected void finished(Description description) {
        super.finished(description);
        beansCache.clear();
    }

    public <T> void bind(Class<T> pureClsType, T instance) {
        beansCache.put(pureClsType, instance);
    }

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

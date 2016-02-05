package org.ovirt.engine.core.utils.lock;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class LockedObjectFactory {
    /**
     * @param instance class to be decorated.
     * @param interfaceClass class type of interface implemented by the decorated class.
     * @param lock lock used for locking.
     * @param <T> <b>type of !INTERFACE!</b>
     */
    public <T> T createLockingInstance(T instance,
            Class<T> interfaceClass,
            ReentrantReadWriteLock lock) {
        Objects.requireNonNull(instance);

        //noinspection unchecked
        return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(),
                new Class<?>[] { interfaceClass },
                new LockingInvocationHandler(instance, lock));
    }

    static class LockingInvocationHandler<T> implements InvocationHandler {
        private final ReentrantReadWriteLock lockObj;
        private final T instance;

        public LockingInvocationHandler(T instance, ReentrantReadWriteLock lockObj) {
            this.instance = instance;
            this.lockObj = lockObj;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

            if (method.isAnnotationPresent(AcquireReadLock.class)) {
                try (AutoCloseableLock l = new AutoCloseableLock(lockObj.readLock())) {
                    return method.invoke(instance, args);
                }
            }

            if (method.isAnnotationPresent(AcquireWriteLock.class)) {
                try (AutoCloseableLock l = new AutoCloseableLock(lockObj.writeLock())) {
                    try {
                        return method.invoke(instance, args);
                    } catch (InvocationTargetException e) {
                        throw e.getTargetException();
                    }
                }
            }

            return method.invoke(instance, args);
        }

    }
}

package org.ovirt.engine.core.utils.lock;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.enterprise.context.ApplicationScoped;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class LockedObjectFactory {
    private static final Logger log = LoggerFactory.getLogger(LockedObjectFactory.class);

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

        log.debug("Creating locking proxy for {} using lock: {}", instance, lock);
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

            try {
                return method.invoke(instance, args);
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            }
        }

    }
}

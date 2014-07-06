package org.ovirt.engine.core.utils.lock;

import java.util.concurrent.locks.Lock;

/**
 * Wrapper for a {@link Lock} to make it auto closeable.<br>
 * <br>
 * Example:
 *
 * <pre>
 * try (AutoCloseableLock l = new AutoCloseableLock(someLock)) {
 *     // Thread safe code here
 * }
 * </pre>
 */
public final class AutoCloseableLock implements AutoCloseable {

    private Lock lock;

    /**
     * Construct the {@link AutoCloseableLock} and lock the given lock.
     *
     * @param lock
     *            The lock to acquire (and eventually release).
     */
    public AutoCloseableLock(Lock lock) {
        this.lock = lock;
        lock.lock();
    }

    @Override
    public void close() {
        lock.unlock();
    }
}

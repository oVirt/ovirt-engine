package org.ovirt.engine.core.utils.lock;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.junit.jupiter.api.Test;

/**
 * In this test, we need to test, that MacPoolLockingProxy actually works. Assuming write lock, we must setup scenario,
 * where two thread may meet inside of 'method to be locked' and if lock is defunct, we must detect it.
 * <p>
 * To do so, we need to alter object wrapped by this proxy, which is MacPool, so its methods does not finish quickly.
 * This is achieved in org.ovirt.engine.core.bll.network.macpool.MacPoolLockingProxyTest#verifyingPoolProxy().
 * This method suspends current thread for
 * org.ovirt.engine.core.bll.network.macpool.MacPoolLockingProxyTest#METHOD_LOCKED_DURATION giving chance to another
 * thread to step into here meantime. This method, aside from stalling, also notes 'integer' value (obtained from
 * thread safe atomicInteger field) at its start and end. If there was any other thread communication, 'start' and
 * 'end'
 * will differ by bigger number than 1. However, we do not have place where to note these numbers, as then cannot be
 * stored in 'stalling proxy' (which is shared between threads). Because of that, those numbers are stored into
 * org.ovirt.engine.core.bll.network.macpool.MacPoolLockingProxyTest#threadMarks using thread ID as a key.
 */
public class LockedObjectFactoryTest {
    //be careful with making this delays smaller or removing them entirely, as surprising result may occur.
    /**
     * number of millis for which is each decorated method delayed.
     */
    private static final int METHOD_LOCKED_DURATION = 100;

    /**
     * number of millis between 2 tread execution
     */
    private static final int DELAY_BETWEEN_THREADS_MILLIS = 20;


    private final TestInstance testInstanceA = new TestInstance();
    private final TestInstance testInstanceB = new TestInstance();

    private final LockedObjectFactory lockedObjectFactory = new LockedObjectFactory();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private TestInterface lockedTestInstanceA = createLockedTestInstance(testInstanceA);
    private TestInterface lockedTestInstanceB = createLockedTestInstance(testInstanceB);

    private TestInterface createLockedTestInstance(TestInstance testInstanceA) {
        return lockedObjectFactory.createLockingInstance(testInstanceA, TestInterface.class, lock);
    }

    @Test
    public void testMethodWithReadLockWhenBlockedByWriteMethod() {
        Runnable action = () -> lockedTestInstanceA.methodWithReadLock();
        performOperation(action, () -> lockedTestInstanceB.methodWithWriteLock(), false);
    }

    @Test
    public void testMethodWithReadLockWhenAccessedTwice() {
        performOperation(() -> lockedTestInstanceA.methodWithReadLock(),
                () -> lockedTestInstanceB.methodWithReadLock(), true);
    }

    @Test
    public void testMethodWithWriteWhenAccessedTwiceLock() {
        performOperation(() -> lockedTestInstanceA.methodWithWriteLock(),
                () -> lockedTestInstanceB.methodWithWriteLock(), false);
    }

    @Test
    public void testAllocateNewMacWhenBlockedByReadMethod() {
        Runnable action = () -> lockedTestInstanceA.methodWithWriteLock();
        performOperation(() -> lockedTestInstanceB.methodWithReadLock(), action, false);
    }

    /**
     * @param action1 action for first thread
     * @param action2 action for the second thread
     * @param expectOverlap expected result; use false to expect threads to be executed in sequential manner.
     */
    private void performOperation(Runnable action1, Runnable action2, boolean expectOverlap) {
        Thread thread1 = new Thread(action1);
        thread1.start();

        sleep(DELAY_BETWEEN_THREADS_MILLIS); // give chance thread above to actually start.

        Thread thread2 = new Thread(action2);
        thread2.start();

        //we need to join, so that JUnit will not finish execution before threads are finished.
        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        assertThat(threadsOverlaps(), is(expectOverlap));
    }

    private boolean threadsOverlaps() {
        return threadsOverlaps(testInstanceA.getThreadMarks())
                || threadsOverlaps(testInstanceB.getThreadMarks());
    }

    /**
     * @param marks List of integers representing marks of one thread. If those marks
     * forms sequence of numbers increasing by 1, then there was no interruption during execution.
     *
     * @return true if threads intervenes, false otherwise (their execution was sequential).
     */
    private boolean threadsOverlaps(List<Integer> marks) {
        int marksCount = marks.size();
        if (marksCount == 0) {
            return false;
        }

        for (int i = 1; i < marksCount; i++) {
            Integer left = marks.get(i - 1);
            Integer right = marks.get(i);
            if (left + 1 != right) {
                return true;
            }
        }

        return false;
    }

    @Test
    public void testThreadsOverlaps() {
        assertThat(threadsOverlaps(Arrays.asList(0, 2)), is(true));
        assertThat(threadsOverlaps(Arrays.asList(1, 3)), is(true));

        assertThat(threadsOverlaps(Arrays.asList(0, 1)), is(false));
        assertThat(threadsOverlaps(Arrays.asList(2, 3)), is(false));
    }

    /**
     * Tests, that proxy does not throw controlled exception, if proxied instance throws a RuntimeException.
     * Otherwise it would cause caller to fail with UndeclaredThrowableException
     */
    @Test
    public void testThatNoControlledExceptionIsThrown() {
        NullPointerException runtimeException = new NullPointerException();
        assertThrows(runtimeException.getClass(), () -> lockedTestInstanceA.failingMethod(runtimeException));
    }

    private static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public interface TestInterface {
        void unlockedMethod();

        @AcquireReadLock
        void methodWithReadLock();

        @AcquireWriteLock
        void methodWithWriteLock();

        void failingMethod(RuntimeException runtimeException);
    }

    public static class TestInstance implements TestInterface {

        /**
         * this is a global counter, used by individual threads, to mark they progress through execution of tested method.
         */
        private static AtomicInteger ATOMIC_INTEGER = new AtomicInteger();

        private List<Integer> threadMarks = new ArrayList<>();

        @Override
        public void unlockedMethod() {
            methodImpl();
        }

        @Override
        public void methodWithReadLock() {
            methodImpl();
        }

        @Override
        public void methodWithWriteLock() {
            methodImpl();
        }

        private void methodImpl() {
            markExecutionProgress();
            sleep(METHOD_LOCKED_DURATION);
            markExecutionProgress();
        }

        /**
         * When called new atomic integer is queried and stored in list to have start and end
         * number marks for thread excercising this TestInstance.
         */
        private void markExecutionProgress() {
            int value = ATOMIC_INTEGER.getAndIncrement();
            threadMarks.add(value);
        }

        public List<Integer> getThreadMarks() {
            return threadMarks;
        }

        @Override
        public void failingMethod(RuntimeException runtimeException) {
            throw runtimeException;
        }
    }
}

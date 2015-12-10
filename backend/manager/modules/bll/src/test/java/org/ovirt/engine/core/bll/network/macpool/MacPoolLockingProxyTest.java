package org.ovirt.engine.core.bll.network.macpool;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;
import org.mockito.Mockito;
import org.ovirt.engine.core.utils.collections.MultiValueMapUtils;

/**
 * In this test, we need to test, that MacPoolLockingProxy actually works. Assuming write lock, we must setup scenario,
 * where two thread may meet inside of 'method to be locked' and if lock is defunct, we must detect it.
 *
 * To do so, we need to alter object wrapped by this proxy, which is MacPool, so its methods does not finish quickly.
 * This is achieved in org.ovirt.engine.core.bll.network.macpool.MacPoolLockingProxyTest#verifyingPoolProxy().
 * This method suspends current thread for
 * org.ovirt.engine.core.bll.network.macpool.MacPoolLockingProxyTest#METHOD_LOCKED_DURATION giving chance to another
 * thread to step into here meantime. This method, aside from stalling, also notes 'integer' value (obtained from
 * thread safe atomicInteger field) at its start and end. If there was any other thread communication, 'start' and 'end'
 * will differ by bigger number than 1. However, we do not have place where to note these numbers, as then cannot be
 * stored in 'stalling proxy' (which is shared between threads). Because of that, those numbers are stored into
 * org.ovirt.engine.core.bll.network.macpool.MacPoolLockingProxyTest#threadMarks using thread ID as a key.
 */
public class MacPoolLockingProxyTest {

    //be careful with making this delays smaller or removing them entirely, as surprising result may occur.
    /**
     * number of millis for which is each decorated method delayed.
     */
    private static final int METHOD_LOCKED_DURATION = 50;

    /**
     * number of millis between 2 tread execution
     */
    private static final int DELAY_BETWEEN_THREADS_MILLIS = 10;

    private MacPoolLockingProxy lockedPool = new MacPoolLockingProxy(verifyingPoolProxy());

    /**
     * this is a global counter, used by individual threads, to mark they progress through execution of tested method.
     */
    private AtomicInteger atomicInteger = new AtomicInteger();

    /**
     * stores all marks made by each thread. See {@link #markExecutionProgress}.
     */
    private Map<Long, List<Integer>> threadMarks = new HashMap<>();
    private final MultiValueMapUtils.ListCreator<Integer> listCreator = new MultiValueMapUtils.ListCreator<>();

    /**
     * Method known to obtain write lock to be used for obtaining write lock when testing other methods.
     */
    private Runnable sampleMethodWithWritableLock = ()-> lockedPool.allocateNewMac();

    /**
     * Method known to obtain read lock to be used for obtaining read lock when testing other methods.
     */
    private Runnable sampleMethodWithReadableLock = ()-> lockedPool.getAvailableMacsCount();


    //<editor-fold desc="methods requiring read lock">
    @Test
    public void testGetAvailableMacsCountWhenBlockedByWriteMethod() throws Exception {
        methodWithReadLockIsBlockedByAcquiredWriteLock(()-> lockedPool.getAvailableMacsCount());
    }

    @Test
    public void testGetAvailableMacsCountWhenAccessedTwice() throws Exception {
        testSelfLockoutWithReadableLock(()-> lockedPool.getAvailableMacsCount());
    }

    @Test
    public void testIsMacInUseWhenBlockedByWriteMethod() throws Exception {
        methodWithReadLockIsBlockedByAcquiredWriteLock(()-> lockedPool.isMacInUse(null));
    }

    @Test
    public void testIsMacInUseWhenAccessedTwice() throws Exception {
        testSelfLockoutWithReadableLock(()-> lockedPool.isMacInUse(null));
    }

    //</editor-fold>

    //<editor-fold desc="methods requiring write lock">

    @Test
    public void testAllocateNewMac() {
        testSelfLockoutWithWritableLock(() -> lockedPool.allocateNewMac());
    }

    //TODO MM: Dear code reviewer! Do we want to test this scenario as well? Please advise.
    @Test
    public void testAllocateNewMac2() {
        Runnable action = () -> lockedPool.allocateNewMac();
        performOperation(sampleMethodWithReadableLock, action, false);
    }

    //TODO MM: Dear code reviewer! Do we want to test this scenario as well? Please advise.
    @Test
    public void testAllocateNewMac3() {
        Runnable action = () -> lockedPool.allocateNewMac();
        performOperation(action, sampleMethodWithReadableLock, false);
    }

    @Test
    public void testFreeMac() {
        testSelfLockoutWithWritableLock(() -> lockedPool.freeMac(null));
    }

    @Test
    public void testAddMac() {
        testSelfLockoutWithWritableLock(() -> lockedPool.addMac(null));
    }

    @Test
    public void testForceAddMac() {
        testSelfLockoutWithWritableLock(() -> lockedPool.forceAddMac(null));
    }

    @Test
    public void testFreeMacs() {
        testSelfLockoutWithWritableLock(() -> lockedPool.freeMacs(null));
    }

    @Test
    public void testAllocateMacAddresses() {
        testSelfLockoutWithWritableLock(() -> {
            int unimportantNumberOfMacs = 5;
            lockedPool.allocateMacAddresses(unimportantNumberOfMacs);
        });
    }

//    //</editor-fold>

    //<editor-fold desc="Util methods">
    private void methodWithReadLockIsBlockedByAcquiredWriteLock(Runnable action) {
        performOperation(action, sampleMethodWithWritableLock, false);
    }

    private void testSelfLockoutWithReadableLock(Runnable action) {
        performOperation(action, true);
    }

    private void testSelfLockoutWithWritableLock(Runnable action) {
        performOperation(action, false);
    }

    private void performOperation(Runnable action, boolean expectOverlap) {
        performOperation(action, action, expectOverlap);
    }

    /**
     *
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
        return threadsOverlaps(threadMarks.values());
    }

    /**
     *
     * @param values Collection of lists of integers. Each list of integer holds marks of one thread. If those marks
     * forms sequence of numbers increasing by 1, then there was no interruption during execution.
     * @return true if threads intervenes, false otherwise (their execution was sequential).
     */
    private boolean threadsOverlaps(Collection<List<Integer>> values) {
        for (List<Integer> marks : values) {
            int marksCount = marks.size();
            if (marksCount == 0) {
                continue;
            }

            for(int i = 1; i < marksCount; i++) {
                Integer left = marks.get(i - 1);
                Integer right = marks.get(i);
                if (left + 1 != right) {
                    return true;
                }
            }
        }

        return false;
    }

    @Test
    public void testThreadsOverlaps() {
        assertThat(threadsOverlaps(Arrays.asList(Arrays.asList(0, 2), Arrays.asList(1, 3))), is(true));
        assertThat(threadsOverlaps(Arrays.asList(Arrays.asList(0, 1), Arrays.asList(2, 3))), is(false));
    }

    /**
     * @return MacPool instance, which all methods lasts at least {@link #METHOD_LOCKED_DURATION} millis, and uses
     * {link #markExecutionProgres} to denote start and end of method. Return values of all methods are default values:
     * null, 0, false.
     */
    private MacPool verifyingPoolProxy() {
        return (MacPool) Proxy.newProxyInstance(MacPoolLockingProxyTest.class.getClassLoader(),
                new Class<?>[]{MacPool.class},
                (proxy, method, args) -> {
                    markExecutionProgress();

                    sleep(METHOD_LOCKED_DURATION);

                    //note: mock is here used ONLY to avoid the need of correct handling of return value.
                    Object result = method.invoke(Mockito.mock(MacPool.class), args);

                    markExecutionProgress();

                    return result;
                });
    }

    /**
     * When called new atomic integer is queried and stored in map using thread id as a key to have start and end
     * numbers for each thread.
     */
    private void markExecutionProgress() {
        int value = atomicInteger.getAndIncrement();
        MultiValueMapUtils.addToMap(Thread.currentThread().getId(), value, threadMarks, listCreator);
    }

    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    //</editor-fold>
}

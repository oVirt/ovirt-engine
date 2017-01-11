package org.ovirt.engine.core.bll.network.macpool;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.lock.LockedObjectFactory;

@RunWith(MockitoJUnitRunner.class)
public class DecoratedMacPoolFactoryTest {

    @Mock
    private MacPool macPool;

    @Mock
    private MacPoolDecorator macPoolDecoratorA;

    @Mock
    private MacPoolDecorator macPoolDecoratorB;

    @Mock
    private LockedObjectFactory lockedObjectFactory;

    private Guid poolId = Guid.newGuid();

    @Test
    public void testCreateDecoratedPoolWhenNoDecoratorsAreRequested() {
        MacPool decoratedPool = decoratePoolWhileNotUsingLocking(Collections.emptyList());
        assertThat(decoratedPool, is(macPool));
    }

    @Test
    public void testEqualyDecoratedPoolsUseSameLock() {
        multipleDecoratorsUsesSameLock(macPoolDecoratorA, macPoolDecoratorA);
    }

    @Test
    public void testDifferenlyDecoratedPoolsUseSameLock() {
        multipleDecoratorsUsesSameLock(macPoolDecoratorA, macPoolDecoratorB);
    }

    private void multipleDecoratorsUsesSameLock(MacPoolDecorator... decorators) {
        DecoratedMacPoolFactory factory = new DecoratedMacPoolFactory(lockedObjectFactory);

        Arrays.stream(decorators)
                .forEach(decorator -> factory.createDecoratedPool(poolId, macPool, singletonList(decorator)));

        ArgumentCaptor<ReentrantReadWriteLock> captor = ArgumentCaptor.forClass(ReentrantReadWriteLock.class);
        verify(lockedObjectFactory, times(2))
                .createLockingInstance(eq(macPool), eq(MacPool.class), captor.capture());

        //there's is one distinct item —> all locks are same.
        assertThat(captor.getAllValues().stream().distinct().count(), is(1L));
    }

    @Test
    public void verifyDecoratorOrder() {
        DecoratedMacPoolFactory factory = createDecoratedMacPoolFactoryWithDisabledLocking();
        MacPool decoratedPool = factory.createDecoratedPool(poolId,
                macPool,
                Arrays.asList(macPoolDecoratorA, macPoolDecoratorB));

        assertThat(decoratedPool, is(macPoolDecoratorB));

        ArgumentCaptor<MacPool> firstDecoratorMacPoolArgumentCaptor = ArgumentCaptor.forClass(MacPool.class);
        verify(macPoolDecoratorB).setMacPool(firstDecoratorMacPoolArgumentCaptor.capture());
        verify(macPoolDecoratorB).setMacPoolId(any());
        assertThat(firstDecoratorMacPoolArgumentCaptor.getValue(), is(macPoolDecoratorA));

        ArgumentCaptor<MacPool> secondDecoratorMacPoolArgumentCaptor = ArgumentCaptor.forClass(MacPool.class);
        verify(macPoolDecoratorA).setMacPool(secondDecoratorMacPoolArgumentCaptor.capture());
        verify(macPoolDecoratorA).setMacPoolId(any());
        assertThat(secondDecoratorMacPoolArgumentCaptor.getValue(), is(macPool));

        Mockito.verifyNoMoreInteractions(macPoolDecoratorA, macPoolDecoratorB, macPool);
    }

    @Test
    public void testTwoDifferentPoolsShouldUsesDifferentLock() {
        List<MacPoolDecorator> noDecorators = Collections.emptyList();
        Guid anotherPooldId = Guid.newGuid();
        MacPool anotherMacPool = mock(MacPool.class);

        /*
         * here we just want to return some proxied 'locked' MacPool. 'Any' should not be problem, as this argument
         * is verified below. Here we just want to return different 'locked' MacPool for different calls, and check if
         * they were returned.
         */
        MacPool dummyLockedMacPool1 = mock(MacPool.class);
        MacPool dummyLockedMacPool2 = mock(MacPool.class);
        when(lockedObjectFactory.createLockingInstance(eq(macPool), eq(MacPool.class), any()))
                .thenReturn(dummyLockedMacPool1);
        when(lockedObjectFactory.createLockingInstance(eq(anotherMacPool), eq(MacPool.class), any()))
                .thenReturn(dummyLockedMacPool2);

        DecoratedMacPoolFactory factory = new DecoratedMacPoolFactory(lockedObjectFactory);

        assertThat(factory.createDecoratedPool(poolId, macPool, noDecorators), is(dummyLockedMacPool1));
        assertThat(factory.createDecoratedPool(anotherPooldId, anotherMacPool, noDecorators), is(dummyLockedMacPool2));

        ArgumentCaptor<ReentrantReadWriteLock> captor1 = ArgumentCaptor.forClass(ReentrantReadWriteLock.class);
        verify(lockedObjectFactory).createLockingInstance(eq(macPool), eq(MacPool.class), captor1.capture());

        ArgumentCaptor<ReentrantReadWriteLock> captor2 = ArgumentCaptor.forClass(ReentrantReadWriteLock.class);
        verify(lockedObjectFactory).createLockingInstance(eq(anotherMacPool), eq(MacPool.class), captor2.capture());

        assertNotEquals(captor1.getValue(), captor2.getValue());
    }

    private MacPool decoratePoolWhileNotUsingLocking(List<MacPoolDecorator> decorators) {
        return createDecoratedMacPoolFactoryWithDisabledLocking().createDecoratedPool(poolId, macPool, decorators);
    }

    private DecoratedMacPoolFactory createDecoratedMacPoolFactoryWithDisabledLocking() {
        doAnswer(invocation -> invocation.getArguments()[0])
                .when(lockedObjectFactory).createLockingInstance(any(), eq(MacPool.class), any());

        return new DecoratedMacPoolFactory(lockedObjectFactory);
    }
}

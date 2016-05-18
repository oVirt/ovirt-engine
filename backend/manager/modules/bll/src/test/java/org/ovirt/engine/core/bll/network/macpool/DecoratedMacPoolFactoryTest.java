package org.ovirt.engine.core.bll.network.macpool;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.lock.LockedObjectFactory;

@RunWith(MockitoJUnitRunner.class)
public class DecoratedMacPoolFactoryTest {

    @Mock
    private MacPool macPool;

    @Spy
    private MacPoolDecorator macPoolDecoratorA = new DelegatingMacPoolDecorator();

    @Spy
    private MacPoolDecorator macPoolDecoratorB = new DelegatingMacPoolDecorator();

    @Mock
    private LockedObjectFactory lockedObjectFactoryMock;

    @Spy
    private LockedObjectFactory lockedObjectFactorySpy = new LockedObjectFactory();

    private Guid poolId = Guid.newGuid();

    @Test
    public void testCreateDecoratedPoolWhenNoDecoratorsAreRequested() {
        doAnswer(invocation -> invocation.getArguments()[0])
                .when(lockedObjectFactoryMock).createLockingInstance(any(), eq(MacPool.class), any());
        MacPool decoratedPool = decoratePoolWhileNotUsingLocking(Collections.emptyList());
        assertThat(decoratedPool, is(macPool));
    }

    @Test
    public void testCreateDecoratedPoolByTwoDecorators() {
        doAnswer(invocation -> invocation.getArguments()[0])
                .when(lockedObjectFactoryMock).createLockingInstance(any(), eq(MacPool.class), any());

        //decorate macPool in order by decorators A and then B.
        MacPool decoratedPool = decoratePoolWhileNotUsingLocking(Arrays.asList(macPoolDecoratorA, macPoolDecoratorB));

        //assert that last decorator is B.
        assertThat(decoratedPool, is(macPoolDecoratorB));

        //verify, that value mocked at start of chain propagates to the top, somehow.
        String macToPropagate = "macToPropagate";
        when(macPool.allocateNewMac()).thenReturn(macToPropagate);
        String propagatedMac = decoratedPool.allocateNewMac();
        assertThat(propagatedMac, is(macToPropagate));

        /*
         * make sure, that allocateNewMac() was called on each instance on chain, and nothing else was.
         * This, propagation, and last decorator in chain (of length 3) should grant that object is
         * properly decorated.
         */
        verify(macPoolDecoratorA).allocateNewMac();
        verify(macPoolDecoratorB).allocateNewMac();
        verify(macPool).allocateNewMac();
        verifyNoMoreInteractions(macPoolDecoratorA);
        verifyNoMoreInteractions(macPoolDecoratorB);
        verifyNoMoreInteractions(macPool);
    }

    @Test
    public void testCreateDecoratedTwoRequestsToGetEquallyDecoratedSamePoolUseSameLock() {
        DecoratedMacPoolFactory factory = new DecoratedMacPoolFactory(lockedObjectFactorySpy);

        assertThat(factory.createDecoratedPool(poolId, macPool, singletonList(macPoolDecoratorA)),
                instanceOf(Proxy.class));

        assertThat(factory.createDecoratedPool(poolId, macPool, singletonList(macPoolDecoratorA)),
                instanceOf(Proxy.class));

        ArgumentCaptor<ReentrantReadWriteLock> captor = ArgumentCaptor.forClass(ReentrantReadWriteLock.class);
        verify(lockedObjectFactorySpy, times(2)).createLockingInstance(eq(macPoolDecoratorA), eq(MacPool.class), captor.capture());


        List<ReentrantReadWriteLock> capturesValues = captor.getAllValues();
        assertEquals(capturesValues.get(0), capturesValues.get(1));
    }

    @Test
    public void testCreateDecoratedTwoRequestsToGetDifferentlyDecoratedSamePoolUseSameLock() {
        DecoratedMacPoolFactory factory = new DecoratedMacPoolFactory(lockedObjectFactorySpy);

        assertThat(factory.createDecoratedPool(poolId, macPool, singletonList(macPoolDecoratorA)),
                instanceOf(Proxy.class));
        assertThat(factory.createDecoratedPool(poolId, macPool, singletonList(macPoolDecoratorB)),
                instanceOf(Proxy.class));

        ArgumentCaptor<ReentrantReadWriteLock> captor1 = ArgumentCaptor.forClass(ReentrantReadWriteLock.class);
        verify(lockedObjectFactorySpy).createLockingInstance(eq(macPoolDecoratorA), eq(MacPool.class), captor1.capture());

        ArgumentCaptor<ReentrantReadWriteLock> captor2 = ArgumentCaptor.forClass(ReentrantReadWriteLock.class);
        verify(lockedObjectFactorySpy).createLockingInstance(eq(macPoolDecoratorB), eq(MacPool.class), captor2.capture());

        assertEquals(captor1.getValue(), captor2.getValue());
    }

    @Test
    public void testCreateDecoratedTwoRequestsToGetDifferentPoolUseSameLock() {
        DecoratedMacPoolFactory factory = new DecoratedMacPoolFactory(lockedObjectFactorySpy);

        assertThat(factory.createDecoratedPool(poolId, macPool, Collections.emptyList()), instanceOf(Proxy.class));

        Guid anotherPooldId = Guid.newGuid();
        MacPool anotherMacPool = mock(MacPool.class);
        assertThat(factory.createDecoratedPool(anotherPooldId, anotherMacPool, Collections.emptyList()),
                instanceOf(Proxy.class));

        ArgumentCaptor<ReentrantReadWriteLock> captor1 = ArgumentCaptor.forClass(ReentrantReadWriteLock.class);
        verify(lockedObjectFactorySpy).createLockingInstance(eq(macPool), eq(MacPool.class), captor1.capture());

        ArgumentCaptor<ReentrantReadWriteLock> captor2 = ArgumentCaptor.forClass(ReentrantReadWriteLock.class);
        verify(lockedObjectFactorySpy).createLockingInstance(eq(anotherMacPool), eq(MacPool.class), captor2.capture());

        assertNotEquals(captor1.getValue(), captor2.getValue());
    }

    private MacPool decoratePoolWhileNotUsingLocking(List<MacPoolDecorator> decorators) {
        return createDecoratedMacPoolFactoryWithDisabledLocking().createDecoratedPool(poolId, macPool, decorators);
    }

    private DecoratedMacPoolFactory createDecoratedMacPoolFactoryWithDisabledLocking() {
        doAnswer(invocation -> invocation.getArguments()[0])
                .when(lockedObjectFactoryMock).createLockingInstance(any(), eq(MacPool.class), any());

        return new DecoratedMacPoolFactory(lockedObjectFactoryMock);
    }

}

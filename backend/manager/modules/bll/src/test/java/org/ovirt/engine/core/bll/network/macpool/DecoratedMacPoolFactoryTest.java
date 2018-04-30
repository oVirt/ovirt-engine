package org.ovirt.engine.core.bll.network.macpool;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.lang.math.LongRange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.utils.lock.LockedObjectFactory;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class DecoratedMacPoolFactoryTest {

    @Mock
    private MacPool macPool;

    @Mock
    private AuditLogDirector auditLogDirector;

    @Mock
    private MacPoolDecorator macPoolDecoratorA;

    @Mock
    private MacPoolDecorator macPoolDecoratorB;

    @Mock
    private LockedObjectFactory lockedObjectFactory;

    @BeforeEach
    public void setUp() {
        when(macPool.getId()).thenReturn(Guid.newGuid());
    }

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
                .forEach(decorator -> factory.createDecoratedPool(macPool, singletonList(decorator)));

        ArgumentCaptor<ReentrantReadWriteLock> captor = ArgumentCaptor.forClass(ReentrantReadWriteLock.class);
        verify(lockedObjectFactory, times(2))
                .createLockingInstance(eq(macPool), eq(MacPool.class), captor.capture());

        //there's is one distinct item —> all locks are same.
        assertThat(captor.getAllValues().stream().distinct().count(), is(1L));
    }

    @Test
    public void verifyDecoratorOrder() {
        DecoratedMacPoolFactory factory = createDecoratedMacPoolFactoryWithDisabledLocking();
        MacPool decoratedPool = factory.createDecoratedPool(macPool,
                Arrays.asList(macPoolDecoratorA, macPoolDecoratorB));

        assertThat(decoratedPool, is(macPoolDecoratorB));

        ArgumentCaptor<MacPool> firstDecoratorMacPoolArgumentCaptor = ArgumentCaptor.forClass(MacPool.class);
        verify(macPoolDecoratorB).setMacPool(firstDecoratorMacPoolArgumentCaptor.capture());
        assertThat(firstDecoratorMacPoolArgumentCaptor.getValue(), is(macPoolDecoratorA));

        ArgumentCaptor<MacPool> secondDecoratorMacPoolArgumentCaptor = ArgumentCaptor.forClass(MacPool.class);
        verify(macPoolDecoratorA).setMacPool(secondDecoratorMacPoolArgumentCaptor.capture());
        assertThat(secondDecoratorMacPoolArgumentCaptor.getValue(), is(macPool));

        verify(macPool).getId();
        verifyNoMoreInteractions(macPoolDecoratorA, macPoolDecoratorB, macPool);
    }

    @Test
    public void testTwoDifferentPoolsShouldUsesDifferentLock() {
        List<MacPoolDecorator> noDecorators = Collections.emptyList();
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

        assertThat(factory.createDecoratedPool(macPool, noDecorators), is(dummyLockedMacPool1));
        assertThat(factory.createDecoratedPool(anotherMacPool, noDecorators), is(dummyLockedMacPool2));

        ArgumentCaptor<ReentrantReadWriteLock> captor1 = ArgumentCaptor.forClass(ReentrantReadWriteLock.class);
        verify(lockedObjectFactory).createLockingInstance(eq(macPool), eq(MacPool.class), captor1.capture());

        ArgumentCaptor<ReentrantReadWriteLock> captor2 = ArgumentCaptor.forClass(ReentrantReadWriteLock.class);
        verify(lockedObjectFactory).createLockingInstance(eq(anotherMacPool), eq(MacPool.class), captor2.capture());

        assertNotEquals(captor1.getValue(), captor2.getValue());
    }

    private MacPool decoratePoolWhileNotUsingLocking(List<MacPoolDecorator> decorators) {
        return decoratePoolWhileNotUsingLocking(this.macPool, decorators);
    }

    private MacPool decoratePoolWhileNotUsingLocking(MacPool pool, List<MacPoolDecorator> decorators) {
        return createDecoratedMacPoolFactoryWithDisabledLocking().createDecoratedPool(pool, decorators);
    }

    private DecoratedMacPoolFactory createDecoratedMacPoolFactoryWithDisabledLocking() {
        doAnswer(invocation -> invocation.getArguments()[0])
                .when(lockedObjectFactory).createLockingInstance(any(), eq(MacPool.class), any());

        return new DecoratedMacPoolFactory(lockedObjectFactory);
    }

    @Test
    public void testToString() {
        Guid underlyingPoolId = Guid.newGuid();
        MacPoolUsingRanges underlyingPool = new MacPoolUsingRanges(underlyingPoolId,
                Collections.singletonList(new LongRange(1, 2)),
                false,
                auditLogDirector);

        DelegatingMacPoolDecorator decoratorA = new DelegatingMacPoolDecorator();
        DelegatingMacPoolDecorator decoratorB = new DelegatingMacPoolDecorator();

        MacPool decoratedPool = decoratePoolWhileNotUsingLocking(underlyingPool, Arrays.asList(decoratorA, decoratorB));

        String expectedToStringResult = String.format(
                "%1$s:{macPool='%2$s:{macPool='%3$s:{id='%4$s'}'}'}",
                decoratorA.getClass().getSimpleName(),
                decoratorB.getClass().getSimpleName(),
                underlyingPool.getClass().getSimpleName(),
                underlyingPoolId
                );

        assertThat(decoratedPool.toString(), is(expectedToStringResult));
    }
}

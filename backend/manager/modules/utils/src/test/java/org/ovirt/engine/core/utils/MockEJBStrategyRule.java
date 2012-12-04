package org.ovirt.engine.core.utils;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.junit.Assert;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.ovirt.engine.core.utils.ejb.BeanProxyType;
import org.ovirt.engine.core.utils.ejb.BeanType;
import org.ovirt.engine.core.utils.ejb.ContainerManagedResourceType;
import org.ovirt.engine.core.utils.ejb.EJBUtilsStrategy;
import org.ovirt.engine.core.utils.ejb.EjbUtils;

/**
 * This rule is used to mock EJB bean loading in oVirt Engine classes (accessed via {@link EjbUtils}, without having to resort to Power Mocking.
 * Since most classes that use {@link EjbUtils} rely on it for transaction management services, they are mocked by default.
 * Additional bean may be mocked either by passing the mocks during the rule's construction or by calling {@link #mockBean(BeanType, Object)}.
 *
 * To use it, simple add a {@link MockEJBStrategyRule} member to your test, with the {@link @Rule} annotation.
 */
public class MockEJBStrategyRule extends TestWatcher {

    private EJBUtilsStrategy origStrategy;
    private EJBUtilsStrategy mockStrategy;
    private Map<BeanType, Object> mockBeanMap;

    /** Create the rule with no mocking */
    public MockEJBStrategyRule() {
        this(new EnumMap<BeanType, Object>(BeanType.class));
    }

    /** Create the rule with with a mock of a single bean */
    public MockEJBStrategyRule(BeanType type, Object bean) {
        this(new EnumMap<BeanType, Object>(Collections.singletonMap(type, bean)));
    }

    /** Create the rule with with a mock of a several beans */
    public MockEJBStrategyRule(Map<BeanType, Object> beans) {
        mockBeanMap = beans;
        origStrategy = EjbUtils.getStrategy();
        mockStrategy = mock(EJBUtilsStrategy.class);
        EjbUtils.setStrategy(mockStrategy);
    }

    private void mockTransactionManagement() throws SystemException {
        Transaction trans = mock(Transaction.class);
        TransactionManager tm = mock(TransactionManager.class);
        doReturn(trans).when(tm).getTransaction();
        doReturn(tm).when(mockStrategy)
                .<TransactionManager> findResource(ContainerManagedResourceType.TRANSACTION_MANAGER);
    }

    public void mockBean(BeanType type, Object bean) {
        mockBeanMap.put(type, bean);
    }

    @Override
    public void starting(Description description) {
        try {
            mockTransactionManagement();
            for (Map.Entry<BeanType, Object> mockBeanEntry : mockBeanMap.entrySet()) {
                when(mockStrategy.findBean(eq(mockBeanEntry.getKey()), any(BeanProxyType.class))).thenReturn(mockBeanEntry.getValue());
            }
        } catch (SystemException e) {
            Assert.fail("Unable to mock tranaction management");
        }
    }

    @Override
    public void finished(Description description) {
        EjbUtils.setStrategy(origStrategy);
    }
}

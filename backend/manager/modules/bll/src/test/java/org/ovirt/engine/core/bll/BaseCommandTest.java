package org.ovirt.engine.core.bll;

import javax.transaction.TransactionManager;

import org.junit.Before;
import org.junit.ClassRule;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.ovirt.engine.core.bll.aaa.SessionDataContainer;
import org.ovirt.engine.core.bll.aaa.SsoSessionUtils;
import org.ovirt.engine.core.dao.EngineSessionDao;
import org.ovirt.engine.core.di.InjectorRule;

public abstract class BaseCommandTest {

    @ClassRule
    public static InjectorRule injectorRule = new InjectorRule();

    @Mock
    protected EngineSessionDao engineSessionDao;

    @Spy
    @InjectMocks
    protected SessionDataContainer sessionDataContainer;

    @Mock
    protected SsoSessionUtils ssoSessionUtils;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    protected TransactionManager transactionManager;

    @Before
    public void setUpBase() {
        MockitoAnnotations.initMocks(this);

        injectorRule.bind(TransactionManager.class, transactionManager);
    }
}

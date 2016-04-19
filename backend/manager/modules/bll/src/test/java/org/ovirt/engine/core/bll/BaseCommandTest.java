package org.ovirt.engine.core.bll;

import org.junit.Before;
import org.junit.ClassRule;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.ovirt.engine.core.bll.aaa.SessionDataContainer;
import org.ovirt.engine.core.bll.aaa.SsoSessionUtils;
import org.ovirt.engine.core.dao.EngineSessionDao;

public abstract class BaseCommandTest {

    @ClassRule
    public static InjectorRule injectorRule = new InjectorRule();

    @Mock
    protected EngineSessionDao engineSessionDao;

    @InjectMocks
    protected SessionDataContainer sessionDataContainer;

    @Mock
    protected SsoSessionUtils ssoSessionUtils;

    @Before
    public void setUpSessionDataContainer() {
        MockitoAnnotations.initMocks(this);
    }
}

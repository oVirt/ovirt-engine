package org.ovirt.engine.core.bll;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.EngineSession;
import org.ovirt.engine.core.common.queries.GetEntitiesWithPermittedActionParameters;
import org.ovirt.engine.core.utils.RandomUtils;

public abstract class AbstractGetEntitiesWithPermittedActionParametersQueryTest<P, Q> extends AbstractUserQueryTest<GetEntitiesWithPermittedActionParameters, QueriesCommandBase<? extends GetEntitiesWithPermittedActionParameters>> {

    /** The {@link ActionGroup} used in the test */
    private ActionGroup actionGroup;

    /** The user session to use */
    private String sessionID;

    protected ActionGroup getActionGroup() {
        return actionGroup;
    }

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        // Mock the parameters
        actionGroup = RandomUtils.instance().pickRandom(ActionGroup.values());
        when(getQueryParameters().getActionGroup()).thenReturn(actionGroup);
        sessionID = RandomUtils.instance().nextString(10);
        when(getQueryParameters().getSessionId()).thenReturn(sessionID);

        when(engineSessionDao.save(any(EngineSession.class))).thenReturn(RandomUtils.instance().nextLong());
        when(engineSessionDao.remove(any(Long.class))).thenReturn(1);

        when(ssoSessionUtils.isSessionInUse(anyLong())).thenReturn(false);

        sessionDataContainer.setUser(sessionID, getUser());
    }

    @After
    public void tearDown() {
        sessionDataContainer.removeSessionOnLogout(sessionID);
    }
}

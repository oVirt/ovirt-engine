package org.ovirt.engine.core.bll;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
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

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();

        // Mock the parameters
        actionGroup = RandomUtils.instance().pickRandom(ActionGroup.values());
        when(getQueryParameters().getActionGroup()).thenReturn(actionGroup);
        sessionID = RandomUtils.instance().nextString(10);
        when(getQueryParameters().getSessionId()).thenReturn(sessionID);

        when(engineSessionDao.save(any())).thenReturn(RandomUtils.instance().nextLong());
        when(engineSessionDao.remove(anyLong())).thenReturn(1);

        when(ssoSessionUtils.isSessionInUse(anyLong())).thenReturn(false);

        sessionDataContainer.setUser(sessionID, getUser());
    }

    @AfterEach
    public void tearDown() {
        sessionDataContainer.removeSessionOnLogout(sessionID);
    }
}

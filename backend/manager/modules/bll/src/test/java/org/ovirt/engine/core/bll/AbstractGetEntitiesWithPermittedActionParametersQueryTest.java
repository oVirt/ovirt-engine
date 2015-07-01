package org.ovirt.engine.core.bll;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.ovirt.engine.core.bll.aaa.SessionDataContainer;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.EngineSession;
import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.core.common.queries.GetEntitiesWithPermittedActionParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.EngineSessionDao;
import org.ovirt.engine.core.dao.PermissionDao;
import org.ovirt.engine.core.utils.RandomUtils;

import java.util.ArrayList;

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
        DbFacade dbFacadeMock = mock(DbFacade.class);

        EngineSessionDao engineSessionDaoMock = mock(EngineSessionDao.class);
        when(engineSessionDaoMock.save(any(EngineSession.class))).thenReturn(RandomUtils.instance().nextLong());
        when(engineSessionDaoMock.remove(any(Long.class))).thenReturn(1);
        when(dbFacadeMock.getEngineSessionDao()).thenReturn(engineSessionDaoMock);

        PermissionDao permissionsDaoMock = mock(PermissionDao.class);
        when(permissionsDaoMock.getAllForEntity(any(Guid.class), any(Long.class), any(Boolean.class))).thenReturn(new ArrayList<Permission>());
        when(dbFacadeMock.getPermissionDao()).thenReturn(permissionsDaoMock);

        SessionDataContainer.getInstance().setDbFacade(dbFacadeMock);

        SessionDataContainer.getInstance().setUser(sessionID, getUser());
    }

    @After
    public void tearDown() {
        SessionDataContainer.getInstance().removeSessionOnLogout(sessionID);
    }
}

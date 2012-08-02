package org.ovirt.engine.core.bll;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.StorageDomainDAO;
import org.ovirt.engine.core.dao.VdsDAO;
import org.ovirt.engine.core.utils.MockConfigRule;

@RunWith(MockitoJUnitRunner.class)
public class AutoRecoveryManagerTest {
    @Rule
    public static MockConfigRule mcr = new MockConfigRule();
    private AutoRecoveryManager manager;

    @Mock
    private BackendInternal backendMock;

    // Entities needing recovery
    private List<VDS> vdss = new ArrayList<VDS>();
    private List<storage_domains> storageDomains = new ArrayList<storage_domains>();

    @Before
    public void setup() {
        manager = spy(AutoRecoveryManager.getInstance());
        doReturn(backendMock).when(manager).getBackend();
        final DbFacade dbFacadeMock = mock(DbFacade.class);
        doReturn(dbFacadeMock).when(manager).getDbFacade();
        final VdsDAO vdsDaoMock = mock(VdsDAO.class);

        final VDS vds = new VDS();
        vdss.add(vds);
        when(vdsDaoMock.listFailedAutorecoverables()).thenReturn(vdss);
        when(dbFacadeMock.getVdsDAO()).thenReturn(vdsDaoMock);

        storage_domains domain = new storage_domains();
        domain.setstorage_pool_id(Guid.NewGuid());
        storageDomains.add(domain);
        final StorageDomainDAO storageDomainDaoMock = mock(StorageDomainDAO.class);
        when(storageDomainDaoMock.listFailedAutorecoverables()).thenReturn(storageDomains);
        when(dbFacadeMock.getStorageDomainDAO()).thenReturn(storageDomainDaoMock);
    }

    @Test
    public void onTimer() {
        manager.onTimer();
        verify(backendMock, times(vdss.size())).runInternalAction(eq(VdcActionType.ActivateVds),
                any(VdcActionParametersBase.class));
        verify(backendMock, times(storageDomains.size())).runInternalAction(eq(VdcActionType.ActivateStorageDomain),
                any(VdcActionParametersBase.class));
    }
}

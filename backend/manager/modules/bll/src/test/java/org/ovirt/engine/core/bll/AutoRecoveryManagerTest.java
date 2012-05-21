package org.ovirt.engine.core.bll;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.StorageDomainDAO;
import org.ovirt.engine.core.dao.VdsDAO;
import org.ovirt.engine.core.utils.MockConfigRule;

public class AutoRecoveryManagerTest {
    @Rule
    public static MockConfigRule mcr = new MockConfigRule();
    private AutoRecoveryManager manager;

    @Before
    public void setup() {
        manager = spy(AutoRecoveryManager.getInstance());
        final BackendInternal backendMock = mock(BackendInternal.class);
        doReturn(backendMock).when(manager).getBackend();

        final DbFacade dbFacadeMock = mock(DbFacade.class);
        doReturn(dbFacadeMock).when(manager).getDbFacade();
        final VdsDAO vdsDaoMock = mock(VdsDAO.class);
        final ArrayList<VDS> vdss = new ArrayList<VDS>();
        final VDS vds = new VDS();
        vdss.add(vds);
        when(vdsDaoMock.listFailedAutorecoverables()).thenReturn(vdss);
        when(dbFacadeMock.getVdsDAO()).thenReturn(vdsDaoMock);

        final StorageDomainDAO storageDomainDaoMock = mock(StorageDomainDAO.class);
        final ArrayList<storage_domains> storageDomains = new ArrayList<storage_domains>();
        when(storageDomainDaoMock.listFailedAutorecoverables()).thenReturn(storageDomains);
        when(dbFacadeMock.getStorageDomainDAO()).thenReturn(storageDomainDaoMock);
    }

    @Test
    public void onTimer() {
        manager.onTimer();
    }
}

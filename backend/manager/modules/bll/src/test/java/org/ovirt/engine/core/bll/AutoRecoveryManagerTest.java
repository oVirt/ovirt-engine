package org.ovirt.engine.core.bll;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.StorageDomainDAO;
import org.ovirt.engine.core.dao.VdsDAO;
import org.ovirt.engine.core.utils.MockConfigRule;

@RunWith(MockitoJUnitRunner.class)
public class AutoRecoveryManagerTest {
    private AutoRecoveryManager manager;

    @ClassRule
    public static MockConfigRule mcr =
    new MockConfigRule(mockConfig(ConfigValues.AutoRecoveryAllowedTypes, new HashMap<String, String>()));

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
        when(dbFacadeMock.getVdsDao()).thenReturn(vdsDaoMock);

        storage_domains domain = new storage_domains();
        domain.setstorage_pool_id(Guid.NewGuid());
        storageDomains.add(domain);
        final StorageDomainDAO storageDomainDaoMock = mock(StorageDomainDAO.class);
        when(storageDomainDaoMock.listFailedAutorecoverables()).thenReturn(storageDomains);
        when(dbFacadeMock.getStorageDomainDao()).thenReturn(storageDomainDaoMock);
    }

    @Test
    public void onTimerFullConfig() {
        Config.<Map<String, String>> GetValue(ConfigValues.AutoRecoveryAllowedTypes).put("storage domains",
                Boolean.TRUE.toString());
        Config.<Map<String, String>> GetValue(ConfigValues.AutoRecoveryAllowedTypes).put("hosts",
                Boolean.TRUE.toString());
        manager.onTimer();
        verify(backendMock, times(vdss.size())).runInternalAction(eq(VdcActionType.ActivateVds),
                any(VdcActionParametersBase.class));
        verify(backendMock, times(storageDomains.size())).runInternalAction(eq(VdcActionType.ConnectDomainToStorage),
                any(VdcActionParametersBase.class));
    }

    @Test
    public void onTimerFalseConfig() {
        Config.<Map<String, String>> GetValue(ConfigValues.AutoRecoveryAllowedTypes).put("storage domains",
                Boolean.FALSE.toString());
        Config.<Map<String, String>> GetValue(ConfigValues.AutoRecoveryAllowedTypes).put("hosts",
                Boolean.FALSE.toString());
        manager.onTimer();
        verify(backendMock, never()).runInternalAction(eq(VdcActionType.ActivateVds),
                any(VdcActionParametersBase.class));
        verify(backendMock, never()).runInternalAction(eq(VdcActionType.ConnectDomainToStorage),
                any(VdcActionParametersBase.class));
    }
}

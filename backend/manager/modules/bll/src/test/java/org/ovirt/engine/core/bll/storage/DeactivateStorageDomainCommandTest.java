package org.ovirt.engine.core.bll.storage;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.CommandMocks;
import org.ovirt.engine.core.bll.context.CompensationContext;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.common.action.StorageDomainPoolParametersBase;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMap;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMapId;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.eventqueue.EventQueue;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSParametersBase;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.AsyncTaskDAO;
import org.ovirt.engine.core.dao.StorageDomainDAO;
import org.ovirt.engine.core.dao.StoragePoolDAO;
import org.ovirt.engine.core.dao.StoragePoolIsoMapDAO;
import org.ovirt.engine.core.dao.VdsDAO;
import org.ovirt.engine.core.dao.VmDynamicDAO;
import org.ovirt.engine.core.dao.VmStaticDAO;
import org.ovirt.engine.core.utils.MockEJBStrategyRule;
import org.ovirt.engine.core.utils.ejb.BeanType;

@RunWith(MockitoJUnitRunner.class)
public class DeactivateStorageDomainCommandTest {

    @Rule
    public MockEJBStrategyRule ejbRule = new MockEJBStrategyRule(BeanType.EVENTQUEUE_MANAGER, mock(EventQueue.class));

    @Mock
    private DbFacade dbFacade;
    @Mock
    private StoragePoolIsoMapDAO isoMapDAO;
    @Mock
    private StoragePoolDAO storagePoolDAO;
    @Mock
    private StorageDomainDAO storageDomainDAO;
    @Mock
    private VdsDAO vdsDAO;
    @Mock
    private BackendInternal backendInternal;
    @Mock
    private VDSBrokerFrontend vdsBrokerFrontend;
    @Mock
    private AsyncTaskDAO asyncTaskDAO;
    @Mock
    private VDS vds;
    @Mock
    private VmStaticDAO vmStaticDAO;
    @Mock
    private VmDynamicDAO vmDynamicDAO;

    StoragePoolIsoMap map = new StoragePoolIsoMap();

    private StorageDomain domain = null;

    @ClassRule
    public static MockEJBStrategyRule mockEjbRule = new MockEJBStrategyRule();

    StorageDomainPoolParametersBase params = new StorageDomainPoolParametersBase(Guid.newGuid(), Guid.newGuid());
    DeactivateStorageDomainCommand<StorageDomainPoolParametersBase> cmd =
            spy(new DeactivateStorageDomainCommand<StorageDomainPoolParametersBase>(params));

    @Before
    public void setup() {
        CommandMocks.mockDbFacade(cmd, dbFacade);
        when(dbFacade.getStoragePoolDao()).thenReturn(storagePoolDAO);
        when(dbFacade.getStorageDomainDao()).thenReturn(storageDomainDAO);
    }

    @Test
    public void statusSetInMap() {
        doReturn(mock(IStorageHelper.class)).when(cmd).getStorageHelper(any(StorageDomain.class));
        when(dbFacade.getStoragePoolIsoMapDao()).thenReturn(isoMapDAO);
        when(dbFacade.getVdsDao()).thenReturn(vdsDAO);
        when(dbFacade.getAsyncTaskDao()).thenReturn(asyncTaskDAO);
        when(storagePoolDAO.get(any(Guid.class))).thenReturn(new StoragePool());
        when(isoMapDAO.get(any(StoragePoolIsoMapId.class))).thenReturn(map);
        when(storageDomainDAO.getForStoragePool(any(Guid.class), any(Guid.class))).thenReturn(new StorageDomain());

        doReturn(backendInternal).when(cmd).getBackend();
        when(vdsDAO.getAllForStoragePoolAndStatus(any(Guid.class), any(VDSStatus.class))).thenReturn(new ArrayList<VDS>());
        when(backendInternal.getResourceManager()).thenReturn(vdsBrokerFrontend);
        VDSReturnValue returnValue = new VDSReturnValue();
        returnValue.setSucceeded(true);
        when(vdsBrokerFrontend.RunVdsCommand(any(VDSCommandType.class), any(VDSParametersBase.class)))
                .thenReturn(returnValue);
        when(vdsDAO.get(any(Guid.class))).thenReturn(vds);
        map.setStatus(StorageDomainStatus.Active);

        cmd.setCompensationContext(mock(CompensationContext.class));
        cmd.executeCommand();
        assertTrue(map.getStatus() == StorageDomainStatus.Maintenance);
    }

    @Test
    public void testVmsWithNoIsoAttached() {
        mockDomain();
        doReturn(domain).when(cmd).getStorageDomain();
        when(dbFacade.getVmStaticDao()).thenReturn(vmStaticDAO);
        List<VmStatic> listVMs = new ArrayList<>();
        when(vmStaticDAO.getAllByStoragePoolId(any(Guid.class))).thenReturn(listVMs);
        assertTrue(cmd.isRunningVmsWithIsoAttached());
        assertTrue(cmd.getReturnValue().getCanDoActionMessages().isEmpty());
    }

    @Test
    public void testVmsWithIsoAttached() {
        setup();
        mockDomain();
        doReturn(domain).when(cmd).getStorageDomain();
        when(dbFacade.getVmStaticDao()).thenReturn(vmStaticDAO);
        when(dbFacade.getVmDynamicDao()).thenReturn(vmDynamicDAO);

        List<VmStatic> listVMs = new ArrayList<>();
        VmStatic vmStatic = new VmStatic();
        vmStatic.setName("TestVM");
        vmStatic.setId(Guid.newGuid());
        listVMs.add(vmStatic);
        doReturn(listVMs).when(cmd).getVmsWithAttachedISO();
        assertFalse(cmd.isRunningVmsWithIsoAttached());
        assertTrue(cmd.getReturnValue()
                .getCanDoActionMessages()
                .contains(VdcBllMessages.ERROR_CANNOT_DEACTIVATE_STORAGE_DOMAIN_WITH_ISO_ATTACHED.toString()));
    }

    private void mockDomain() {
        domain = new StorageDomain();
        domain.setStorageDomainType(StorageDomainType.ISO);
    }
}

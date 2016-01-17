package org.ovirt.engine.core.bll.storage.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.context.CompensationContext;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.storage.connection.IStorageHelper;
import org.ovirt.engine.core.common.action.StorageDomainPoolParametersBase;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMap;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMapId;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.eventqueue.EventQueue;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSParametersBase;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.AsyncTaskDao;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.StoragePoolIsoMapDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.VmDynamicDao;
import org.ovirt.engine.core.dao.VmStaticDao;
import org.ovirt.engine.core.utils.MockEJBStrategyRule;
import org.ovirt.engine.core.utils.ejb.BeanType;

public class DeactivateStorageDomainCommandTest extends BaseCommandTest {

    @Rule
    public MockEJBStrategyRule ejbRule = new MockEJBStrategyRule(BeanType.EVENTQUEUE_MANAGER, mock(EventQueue.class));
    @Mock
    private StoragePoolIsoMapDao isoMapDao;
    @Mock
    private StoragePoolDao storagePoolDao;
    @Mock
    private StorageDomainDao storageDomainDao;
    @Mock
    private VdsDao vdsDao;
    @Mock
    private BackendInternal backendInternal;
    @Mock
    private VDSBrokerFrontend vdsBrokerFrontend;
    @Mock
    private AsyncTaskDao asyncTaskDao;
    @Mock
    private VDS vds;
    @Mock
    private VmStaticDao vmStaticDao;
    @Mock
    private VmDynamicDao vmDynamicDao;
    @Mock
    private EventQueue eventQueue;

    private StoragePoolIsoMap map;
    private StorageDomain domain;

    @ClassRule
    public static MockEJBStrategyRule mockEjbRule = new MockEJBStrategyRule();

    StorageDomainPoolParametersBase params = new StorageDomainPoolParametersBase(Guid.newGuid(), Guid.newGuid());
    DeactivateStorageDomainCommand<StorageDomainPoolParametersBase> cmd;

    @Before
    public void setup() {
        map = new StoragePoolIsoMap();

        cmd = spy(new DeactivateStorageDomainCommand<>(params, CommandContext.createContext(params.getSessionId())));
        doReturn(storagePoolDao).when(cmd).getStoragePoolDao();
        doReturn(storageDomainDao).when(cmd).getStorageDomainDao();
        doReturn(eventQueue).when(cmd).getEventQueue();
    }

    @Test
    public void statusSetInMap() {
        doReturn(mock(IStorageHelper.class)).when(cmd).getStorageHelper(any(StorageDomain.class));
        doReturn(isoMapDao).when(cmd).getStoragePoolIsoMapDao();
        doReturn(vdsDao).when(cmd).getVdsDao();
        doReturn(asyncTaskDao).when(cmd).getAsyncTaskDao();
        doReturn(vdsBrokerFrontend).when(cmd).getVdsBroker();
        when(storagePoolDao.get(any(Guid.class))).thenReturn(new StoragePool());
        when(isoMapDao.get(any(StoragePoolIsoMapId.class))).thenReturn(map);
        when(storageDomainDao.getForStoragePool(any(Guid.class), any(Guid.class))).thenReturn(new StorageDomain());

        doReturn(backendInternal).when(cmd).getBackend();
        doReturn(Collections.<VDS>emptyList()).when(cmd).getAllRunningVdssInPool();
        VDSReturnValue returnValue = new VDSReturnValue();
        returnValue.setSucceeded(true);
        when(vdsBrokerFrontend.runVdsCommand(any(VDSCommandType.class), any(VDSParametersBase.class)))
                .thenReturn(returnValue);
        when(vdsDao.get(any(Guid.class))).thenReturn(vds);
        map.setStatus(StorageDomainStatus.Active);

        cmd.setCompensationContext(mock(CompensationContext.class));
        cmd.executeCommand();
        assertEquals(StorageDomainStatus.Maintenance, map.getStatus());
    }

    @Test
    public void testVmsWithNoIsoAttached() {
        mockDomain();
        doReturn(domain).when(cmd).getStorageDomain();
        doReturn(vmStaticDao).when(cmd).getVmStaticDao();
        when(vmStaticDao.getAllByStoragePoolId(any(Guid.class))).thenReturn(Collections.<VmStatic>emptyList());
        assertTrue(cmd.isRunningVmsWithIsoAttached());
        assertTrue(cmd.getReturnValue().getValidationMessages().isEmpty());
    }

    @Test
    public void testVmsWithIsoAttached() {
        setup();
        mockDomain();
        doReturn(domain).when(cmd).getStorageDomain();
        doReturn(vmStaticDao).when(cmd).getVmStaticDao();
        doReturn(vmDynamicDao).when(cmd).getVmDynamicDao();

        VmStatic vmStatic = new VmStatic();
        vmStatic.setName("TestVM");
        vmStatic.setId(Guid.newGuid());
        doReturn(Collections.singletonList(vmStatic)).when(cmd).getVmsWithAttachedISO();
        assertFalse(cmd.isRunningVmsWithIsoAttached());
        assertTrue(cmd.getReturnValue()
                .getValidationMessages()
                .contains(EngineMessage.ERROR_CANNOT_DEACTIVATE_STORAGE_DOMAIN_WITH_ISO_ATTACHED.toString()));
    }

    private void mockDomain() {
        domain = new StorageDomain();
        domain.setStorageDomainType(StorageDomainType.ISO);
    }
}

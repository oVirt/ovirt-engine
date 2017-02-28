package org.ovirt.engine.core.bll.storage.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.EnumSet;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.context.CompensationContext;
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
import org.ovirt.engine.core.dao.VmStaticDao;

public class DeactivateStorageDomainCommandTest extends BaseCommandTest {

    @Mock
    private StoragePoolIsoMapDao isoMapDao;
    @Mock
    private StoragePoolDao storagePoolDao;
    @Mock
    private StorageDomainDao storageDomainDao;
    @Mock
    private VdsDao vdsDao;
    @Mock
    private VDSBrokerFrontend vdsBrokerFrontend;
    @Mock
    private AsyncTaskDao asyncTaskDao;
    @Mock
    private VDS vds;
    @Mock
    private VmStaticDao vmStaticDao;
    @Mock
    private EventQueue eventQueue;

    private StoragePoolIsoMap map;
    private StorageDomain domain;

    StorageDomainPoolParametersBase params = new StorageDomainPoolParametersBase(Guid.newGuid(), Guid.newGuid());

    @Spy
    @InjectMocks
    DeactivateStorageDomainCommand<StorageDomainPoolParametersBase> cmd =
            new DeactivateStorageDomainCommand<>(params, CommandContext.createContext(params.getSessionId()));

    @Before
    public void setup() {
        map = new StoragePoolIsoMap();
        cmd.init();
    }

    @Test
    public void statusSetInMap() {
        doReturn(mock(IStorageHelper.class)).when(cmd).getStorageHelper(any(StorageDomain.class));
        when(storagePoolDao.get(any(Guid.class))).thenReturn(new StoragePool());
        when(isoMapDao.get(any(StoragePoolIsoMapId.class))).thenReturn(map);
        when(storageDomainDao.getForStoragePool(any(Guid.class), any(Guid.class))).thenReturn(new StorageDomain());

        doReturn(Collections.emptyList()).when(cmd).getAllRunningVdssInPool();
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
        when(vmStaticDao.getAllByStoragePoolId(any(Guid.class))).thenReturn(Collections.emptyList());
        assertTrue(cmd.isRunningVmsWithIsoAttached());
        assertTrue(cmd.getReturnValue().getValidationMessages().isEmpty());
    }

    @Test
    public void testVmsWithIsoAttached() {
        mockDomain();
        domain.setStorageDomainType(StorageDomainType.ISO);
        doReturn(domain).when(cmd).getStorageDomain();

        VmStatic vmStatic = new VmStatic();
        vmStatic.setName("TestVM");
        vmStatic.setId(Guid.newGuid());
        doReturn(Collections.singletonList(vmStatic)).when(cmd).getVmsWithAttachedISO();
        assertFalse(cmd.isRunningVmsWithIsoAttached());
        assertTrue(cmd.getReturnValue()
                .getValidationMessages()
                .contains(EngineMessage.ERROR_CANNOT_DEACTIVATE_STORAGE_DOMAIN_WITH_ISO_ATTACHED.toString()));
    }

    @Test
    public void testDeactivateNoExistingDomainFails() {
        doReturn(null).when(cmd).getStorageDomain();
        ValidateTestUtils.runAndAssertValidateFailure(cmd, EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_NOT_EXIST);
    }

    @Test
    public void testDeactivateNoActiveDomainFails() {
        mockDomain();
        EnumSet<StorageDomainStatus> invalidStatuses = EnumSet.allOf(StorageDomainStatus.class);
        invalidStatuses.remove(StorageDomainStatus.Active);
        invalidStatuses.forEach(s -> {
            domain.setStatus(s);
            ValidateTestUtils.runAndAssertValidateFailure(cmd, EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_STATUS_ILLEGAL2);
        });
    }

    @Test
    public void testDeactivateHostedDomainStorageFails() {
        mockDomain();
        domain.setHostedEngineStorage(true);
        ValidateTestUtils.runAndAssertValidateFailure(cmd, EngineMessage.ACTION_TYPE_FAILED_HOSTED_ENGINE_STORAGE);
    }

    @Test
    public void testDeactivateStorageDomainWithRunningVmWithLeaseFails() {
        VmStatic vm = new VmStatic();
        vm.setName("myRunningVmWithLease");

        mockDomain();
        when(vmStaticDao.getAllRunningWithLeaseOnStorageDomain(domain.getId())).thenReturn(Collections.singletonList(vm));
        ValidateTestUtils.runAndAssertValidateFailure(cmd, EngineMessage.ERROR_CANNOT_DEACTIVATE_DOMAIN_WITH_RUNNING_VMS_WITH_LEASES);
        assertTrue(cmd.getReturnValue().getValidationMessages().contains(String.format("$vmNames %s", vm.getName())));
    }

    private void mockDomain() {
        domain = new StorageDomain();
        domain.setId(Guid.newGuid());
        domain.setStatus(StorageDomainStatus.Active);
        doReturn(domain).when(cmd).getStorageDomain();
    }
}

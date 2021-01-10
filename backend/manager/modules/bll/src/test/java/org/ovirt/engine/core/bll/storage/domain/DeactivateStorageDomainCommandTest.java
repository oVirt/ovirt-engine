package org.ovirt.engine.core.bll.storage.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.EnumSet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.context.CompensationContext;
import org.ovirt.engine.core.bll.storage.connection.IStorageHelper;
import org.ovirt.engine.core.bll.storage.pool.StoragePoolStatusHandler;
import org.ovirt.engine.core.common.action.StorageDomainPoolParametersBase;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMap;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.storage.ImageTransfer;
import org.ovirt.engine.core.common.businessentities.storage.ImageTransferPhase;
import org.ovirt.engine.core.common.constants.StorageConstants;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.eventqueue.EventQueue;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.AsyncTaskDao;
import org.ovirt.engine.core.dao.ImageTransferDao;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.StoragePoolIsoMapDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.VmStaticDao;

@MockitoSettings(strictness = Strictness.LENIENT)
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
    @Mock
    private StoragePoolStatusHandler storagePoolStatusHandler;
    @Mock
    private ImageTransferDao imageTransferDao;

    private StoragePoolIsoMap map;
    private StorageDomain domain;

    StorageDomainPoolParametersBase params = new StorageDomainPoolParametersBase(Guid.newGuid(), Guid.newGuid());

    @Spy
    @InjectMocks
    DeactivateStorageDomainCommand<StorageDomainPoolParametersBase> cmd =
            new DeactivateStorageDomainCommand<>(params, CommandContext.createContext(params.getSessionId()));

    @BeforeEach
    public void setup() {
        map = new StoragePoolIsoMap();
        cmd.init();
    }

    @Test
    public void statusSetInMap() {
        doReturn(mock(IStorageHelper.class)).when(cmd).getStorageHelper(any());
        when(storagePoolDao.get(any())).thenReturn(new StoragePool());
        when(isoMapDao.get(any())).thenReturn(map);
        when(storageDomainDao.getForStoragePool(any(), any())).thenReturn(new StorageDomain());

        doReturn(Collections.emptyList()).when(cmd).getAllRunningVdssInPool();
        VDSReturnValue returnValue = new VDSReturnValue();
        returnValue.setSucceeded(true);
        when(vdsBrokerFrontend.runVdsCommand(any(), any())).thenReturn(returnValue);
        when(vdsDao.get(any())).thenReturn(vds);
        map.setStatus(StorageDomainStatus.Active);

        cmd.setCompensationContext(mock(CompensationContext.class));
        cmd.executeCommand();
        assertEquals(StorageDomainStatus.Maintenance, map.getStatus());
    }

    @Test
    public void vmsWithNoIsoAttached() {
        mockDomain();
        doReturn(domain).when(cmd).getStorageDomain();
        assertTrue(cmd.isNoRunningVmsWithIsoAttached());
        assertTrue(cmd.getReturnValue().getValidationMessages().isEmpty());
    }

    @Test
    public void vmsWithIsoAttached() {
        mockDomain();
        domain.setStorageDomainType(StorageDomainType.ISO);
        doReturn(domain).when(cmd).getStorageDomain();

        doReturn(Collections.singletonList("TestVM")).when(cmd).getVmsWithAttachedISO();
        assertFalse(cmd.isNoRunningVmsWithIsoAttached());
        assertTrue(cmd.getReturnValue()
                .getValidationMessages()
                .contains(EngineMessage.ERROR_CANNOT_DEACTIVATE_STORAGE_DOMAIN_WITH_ISO_ATTACHED.toString()));
    }

    @Test
    public void vmsWithIsoOnDataDomainAttached() {
        mockDomain();
        domain.setStorageDomainType(StorageDomainType.Data);
        doReturn(domain).when(cmd).getStorageDomain();
        doReturn(domain.getId()).when(cmd).getStorageDomainId();

        doReturn(Collections.singletonList("TestVM")).when(vmStaticDao).getAllRunningNamesWithIsoOnStorageDomain(domain.getId());
        assertFalse(cmd.isNoRunningVmsWithIsoAttached());
        assertTrue(cmd.getReturnValue()
                .getValidationMessages()
                .contains(EngineMessage.ERROR_CANNOT_DEACTIVATE_STORAGE_DOMAIN_WITH_ISO_ATTACHED.toString()));
    }


    @Test
    public void deactivateNoExistingDomainFails() {
        doReturn(null).when(cmd).getStorageDomain();
        ValidateTestUtils.runAndAssertValidateFailure(cmd, EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_NOT_EXIST);
    }

    @Test
    public void deactivateImageTransferActiveUploadFails() {
        mockDomain();
        ImageTransfer transfer = new ImageTransfer();
        transfer.setPhase(ImageTransferPhase.TRANSFERRING);
        when(imageTransferDao.getByStorageId(any())).thenReturn(Collections.singletonList(transfer));
        ValidateTestUtils.runAndAssertValidateFailure(cmd, EngineMessage.ERROR_CANNOT_DEACTIVATE_STORAGE_DOMAIN_DURING_UPLOAD_OR_DOWNLOAD);
    }

    @Test
    public void deactivateNonMonitoredDomainFails() {
        mockDomain();
        EnumSet<StorageDomainStatus> invalidStatuses = EnumSet.allOf(StorageDomainStatus.class);
        invalidStatuses.removeAll(StorageConstants.monitoredDomainStatuses);
        invalidStatuses.forEach(s -> {
            domain.setStatus(s);
            ValidateTestUtils.runAndAssertValidateFailure(cmd, EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_STATUS_ILLEGAL2);
        });
    }

    @Test
    public void deactivateMonitoredDomainSucceeds() {
        mockDomain();
        cmd.getParameters().setIsInternal(false);
        StorageConstants.monitoredDomainStatuses.forEach(s -> {
            domain.setStatus(s);
            assertTrue(cmd.validateDomainStatus());
        });
    }

    @Test
    public void deactivateHostedDomainStorageFails() {
        mockDomain();
        domain.setHostedEngineStorage(true);
        ValidateTestUtils.runAndAssertValidateFailure(cmd, EngineMessage.ACTION_TYPE_FAILED_HOSTED_ENGINE_STORAGE);
    }

    @Test
    public void deactivateStorageDomainWithRunningVmWithLeaseFails() {
        VmStatic vm = new VmStatic();
        vm.setName("myRunningVmWithLease");

        mockDomain();
        when(vmStaticDao.getAllRunningNamesWithLeaseOnStorageDomain(domain.getId())).thenReturn(Collections.singletonList(vm.getName()));
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

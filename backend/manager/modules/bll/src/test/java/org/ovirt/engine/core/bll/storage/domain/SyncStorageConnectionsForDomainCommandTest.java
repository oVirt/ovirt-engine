package org.ovirt.engine.core.bll.storage.domain;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.ValidateTestUtils.runAndAssertValidateFailure;
import static org.ovirt.engine.core.bll.ValidateTestUtils.runAndAssertValidateSuccess;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.storage.connection.ISCSIStorageHelper;
import org.ovirt.engine.core.bll.validator.HostValidator;
import org.ovirt.engine.core.common.action.SyncLunsInfoForBlockStorageDomainParameters;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;

@MockitoSettings(strictness = Strictness.LENIENT)
public class SyncStorageConnectionsForDomainCommandTest extends BaseCommandTest {

    private Guid storageDomainId = Guid.newGuid();
    private Guid vdsId = Guid.newGuid();

    private SyncLunsInfoForBlockStorageDomainParameters parameters =
            new SyncLunsInfoForBlockStorageDomainParameters(storageDomainId, vdsId);

    @Spy
    @InjectMocks
    private SyncStorageConnectionsForDomainCommand<SyncLunsInfoForBlockStorageDomainParameters> command =
            new SyncStorageConnectionsForDomainCommand<>(parameters, null);

    @Mock
    private HostValidator hostValidator;

    @Mock
    private ISCSIStorageHelper iscsiStorageHelper;

    private StorageDomain domain;

    @BeforeEach
    public void setUp() {
        doReturn(hostValidator).when(command).getHostValidator();
        domain = new StorageDomain();
        domain.setId(storageDomainId);
        domain.setStorageType(StorageType.ISCSI);
        doReturn(domain).when(command).getStorageDomain();
        when(hostValidator.hostExists()).thenReturn(ValidationResult.VALID);
        when(hostValidator.isUp()).thenReturn(ValidationResult.VALID);
    }

    @Test
    public void validateFailsWhenHostDoesNotExist() {
        when(hostValidator.hostExists()).thenReturn(new ValidationResult(EngineMessage.VDS_INVALID_SERVER_ID));
        runAndAssertValidateFailure(command, EngineMessage.VDS_INVALID_SERVER_ID);
    }

    @Test
    public void validateFailsWhenHostIsNotUp() {
        when(hostValidator.isUp())
                .thenReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_VDS_STATUS_ILLEGAL));
        runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_VDS_STATUS_ILLEGAL);
    }

    @Test
    public void validateFailsWhenStorageDomainNotExist() {
        doReturn(null).when(command).getStorageDomain();
        runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_NOT_EXIST);
    }

    @Test
    public void validateFailsWhenStorageDomainIsNotBlockDomain() {
        doReturn(true).when(command).checkStorageDomain();
        domain.setStorageType(StorageType.NFS);
        runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_TYPE_ILLEGAL);
    }

    @Test
    public void validateSucceedsForBlockDomainAndUpHost() {
        doReturn(true).when(command).checkStorageDomain();
        runAndAssertValidateSuccess(command);
    }

    @Test
    public void executeCommandSetsSucceededTrueWhenSyncSucceeds() {
        when(iscsiStorageHelper.syncDomainInfo(domain, vdsId, true)).thenReturn(true);

        command.executeCommand();

        // The persistNewConnections flag is passed as true so ad-hoc logins are persisted.
        verify(iscsiStorageHelper, times(1)).syncDomainInfo(domain, vdsId, true);
        org.junit.jupiter.api.Assertions.assertTrue(command.getReturnValue().getSucceeded());
    }

    @Test
    public void executeCommandSetsSucceededFalseWhenSyncReturnsFalse() {
        when(iscsiStorageHelper.syncDomainInfo(domain, vdsId, true)).thenReturn(false);

        command.executeCommand();

        verify(iscsiStorageHelper, times(1)).syncDomainInfo(domain, vdsId, true);
        org.junit.jupiter.api.Assertions.assertFalse(command.getReturnValue().getSucceeded());
    }

    @Test
    public void executeCommandSetsSucceededFalseWhenSyncThrows() {
        doThrow(new RuntimeException("boom")).when(iscsiStorageHelper).syncDomainInfo(any(), any(), eq(true));

        command.executeCommand();

        verify(iscsiStorageHelper, times(1)).syncDomainInfo(domain, vdsId, true);
        org.junit.jupiter.api.Assertions.assertFalse(command.getReturnValue().getSucceeded());
    }
}

package org.ovirt.engine.core.bll.storage.domain;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.doReturn;
import static org.ovirt.engine.core.bll.ValidateTestUtils.runAndAssertValidateFailure;
import static org.ovirt.engine.core.bll.ValidateTestUtils.runAndAssertValidateSuccess;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.storage.connection.ConnectAllHostsToLunCommand.ConnectAllHostsToLunCommandReturnValue;
import org.ovirt.engine.core.bll.storage.utils.BlockStorageDiscardFunctionalityHelper;
import org.ovirt.engine.core.common.action.ExtendSANStorageDomainParameters;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;

@RunWith(MockitoJUnitRunner.class)
public class ExtendSANStorageDomainCommandTest {

    private StorageDomain storageDomain = createStorageDomain();

    private ExtendSANStorageDomainParameters parameters =
            new ExtendSANStorageDomainParameters(storageDomain.getId(), new ArrayList<>());

    @Spy
    @InjectMocks
    private ExtendSANStorageDomainCommand<ExtendSANStorageDomainParameters> command =
            new ExtendSANStorageDomainCommand<>(parameters, null);

    @Mock
    protected BlockStorageDiscardFunctionalityHelper discardHelper;

    @Before
    public void setUp() {
        command.setStorageDomain(storageDomain);
    }

    @Test
    public void validateSucceeds() {
        passAllValidations();
        runAndAssertValidateSuccess(command);
    }

    @Test
    public void validateFailsDiscardFunctionalityBreaks() {
        passAllValidations();
        EngineMessage lunsBreakStorageDomainDiscardSupportMessage =
                EngineMessage.ACTION_TYPE_FAILED_LUN_BREAKS_STORAGE_DOMAIN_PASS_DISCARD_SUPPORT;
        doReturn(new ValidationResult(lunsBreakStorageDomainDiscardSupportMessage)).when(discardHelper)
                .isExistingDiscardFunctionalityPreserved(anyListOf(LUNs.class), any(StorageDomain.class));
        runAndAssertValidateFailure(command, lunsBreakStorageDomainDiscardSupportMessage);
    }

    private StorageDomain createStorageDomain() {
        StorageDomain storageDomain = new StorageDomain();
        storageDomain.setId(Guid.newGuid());
        return storageDomain;
    }

    private void passAllValidations() {
        doReturn(false).when(command).isLunsAlreadyInUse(anyListOf(String.class));
        doReturn(true).when(command).checkStorageDomain();
        doReturn(true).when(command).checkStorageDomainStatus(StorageDomainStatus.Active);
        storageDomain.setStorageType(StorageType.ISCSI);

        ConnectAllHostsToLunCommandReturnValue connectResult = new ConnectAllHostsToLunCommandReturnValue();
        connectResult.setActionReturnValue(new ArrayList<>());
        connectResult.setSucceeded(true);
        doReturn(connectResult).when(command).connectAllHostsToLun();

        doReturn(ValidationResult.VALID).when(discardHelper).isExistingDiscardFunctionalityPreserved(
                anyListOf(LUNs.class), any(StorageDomain.class));
    }
}

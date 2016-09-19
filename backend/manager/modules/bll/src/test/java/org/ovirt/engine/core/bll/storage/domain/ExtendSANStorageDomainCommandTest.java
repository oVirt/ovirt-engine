package org.ovirt.engine.core.bll.storage.domain;

import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.bll.storage.connection.ConnectAllHostsToLunCommand.ConnectAllHostsToLunCommandReturnValue;
import org.ovirt.engine.core.common.action.ExtendSANStorageDomainParameters;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.compat.Guid;

public class ExtendSANStorageDomainCommandTest {

    private ExtendSANStorageDomainCommand<ExtendSANStorageDomainParameters> command;
    private ExtendSANStorageDomainParameters parameters;
    private StorageDomain storageDomain;

    @Before
    public void setUp() {
        createStorageDomain();
        parameters = new ExtendSANStorageDomainParameters(storageDomain.getId(), new ArrayList<>());
        command = spy(new ExtendSANStorageDomainCommand<>(parameters, null));
        command.setStorageDomain(storageDomain);
    }

    @Test
    public void validateSucceeds() {
        passAllValidations();
        ValidateTestUtils.runAndAssertValidateSuccess(command);
    }

    private void createStorageDomain() {
        storageDomain = new StorageDomain();
        storageDomain.setId(Guid.newGuid());
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
    }
}

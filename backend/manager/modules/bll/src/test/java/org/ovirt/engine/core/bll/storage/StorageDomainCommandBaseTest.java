package org.ovirt.engine.core.bll.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.common.action.StorageDomainParametersBase;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.dal.VdcBllMessages;

public class StorageDomainCommandBaseTest {

    public StorageDomainCommandBase<StorageDomainParametersBase> cmd;

    @Before
    public void setUp() {
        createTestCommand();
    }

    @Test
    public void statusMatches() {
        setStorageDomainStatus(StorageDomainStatus.InActive);
        assertTrue(cmd.checkStorageDomainStatus(StorageDomainStatus.InActive));
        assertFalse(commandHasInvalidStatusMessage());
    }

    @Test
    public void statusNotMatch() {
        setStorageDomainStatus(StorageDomainStatus.InActive);
        assertFalse(cmd.checkStorageDomainStatus(StorageDomainStatus.Active));
        assertTrue(commandHasInvalidStatusMessage());
    }

    @Test
    public void statusInList() {
        setStorageDomainStatus(StorageDomainStatus.InActive);
        assertTrue(cmd.checkStorageDomainStatus(StorageDomainStatus.Locked, StorageDomainStatus.InActive,
                StorageDomainStatus.Unknown));
        assertFalse(commandHasInvalidStatusMessage());
    }

    @Test
    public void statusNotInList() {
        setStorageDomainStatus(StorageDomainStatus.InActive);
        assertFalse(cmd.checkStorageDomainStatus(StorageDomainStatus.Locked, StorageDomainStatus.Active,
                StorageDomainStatus.Unknown));
        assertTrue(commandHasInvalidStatusMessage());
    }

    @Test
    public void canDetachInactiveDomain() {
        setStorageDomainStatus(StorageDomainStatus.InActive);
        storagePoolExists();
        masterDomainIsUp();
        isNotLocalData();
        canDetachDomain();
        assertTrue(cmd.canDetachDomain(false, false, false));
    }

    @Test
    public void canDetachMaintenanceDomain() {
        setStorageDomainStatus(StorageDomainStatus.InActive);
        storagePoolExists();
        masterDomainIsUp();
        isNotLocalData();
        canDetachDomain();
        assertTrue(cmd.canDetachDomain(false, false, false));
    }

    @Test
    public void checkStorageDomainNotEqualWithStatusActive() {
        setStorageDomainStatus(StorageDomainStatus.Active);
        assertFalse(cmd.checkStorageDomainStatusNotEqual(StorageDomainStatus.Active));
        List<String> messages = cmd.getReturnValue().getCanDoActionMessages();
        assertEquals(messages.size(), 2);
        assertEquals(messages.get(0), VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_STATUS_ILLEGAL2.toString());
        assertEquals(messages.get(1), String.format("$status %1$s", StorageDomainStatus.Active));
    }

    @Test
    public void checkStorageDomainNotEqualWithNonActiveStatus() {
        setStorageDomainStatus(StorageDomainStatus.Maintenance);
        assertTrue(cmd.checkStorageDomainStatusNotEqual(StorageDomainStatus.Active));
    }

    private void storagePoolExists() {
        when(cmd.checkStoragePool()).thenReturn(true);
    }

    private void masterDomainIsUp() {
        doReturn(true).when(cmd).checkMasterDomainIsUp();
    }

    private void isNotLocalData() {
        doReturn(true).when(cmd).isNotLocalData(anyBoolean());
    }

    private void canDetachDomain() {
        doReturn(true).when(cmd).isDetachAllowed(anyBoolean());
    }

    private boolean commandHasInvalidStatusMessage() {
        return cmd.getReturnValue().getCanDoActionMessages().contains(
                VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_STATUS_ILLEGAL.toString());
    }

    private void setStorageDomainStatus(StorageDomainStatus status) {
        StorageDomain domain = new StorageDomain();
        domain.setstatus(status);
        when(cmd.getStorageDomain()).thenReturn(domain);
    }

    private void createTestCommand() {
        StorageDomainParametersBase parameters = new StorageDomainParametersBase();
        cmd = spy(new TestStorageCommandBase(parameters));
    }

    class TestStorageCommandBase extends StorageDomainCommandBase<StorageDomainParametersBase> {

        public TestStorageCommandBase(StorageDomainParametersBase parameters) {
            super(parameters);
        }

        @Override
        protected void executeCommand() {

        }
    }
}

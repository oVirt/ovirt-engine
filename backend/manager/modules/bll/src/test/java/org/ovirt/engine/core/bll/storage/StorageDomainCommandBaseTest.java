package org.ovirt.engine.core.bll.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.action.StorageDomainParametersBase;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.LunDAO;

@RunWith(MockitoJUnitRunner.class)
public class StorageDomainCommandBaseTest {
    private static final Guid[] GUIDS = new Guid[] {
            new Guid("11111111-1111-1111-1111-111111111111"),
            new Guid("22222222-2222-2222-2222-222222222222")
    };

    @Mock
    private LunDAO lunDAO;

    public StorageDomainCommandBase<StorageDomainParametersBase> cmd;

    @Before
    public void setUp() {
        createTestCommand();
    }

    @Test
    public void statusMatches() {
        setStorageDomainStatus(StorageDomainStatus.Inactive);
        assertTrue(cmd.checkStorageDomainStatus(StorageDomainStatus.Inactive));
        assertFalse(commandHasInvalidStatusMessage());
    }

    @Test
    public void statusNotMatch() {
        setStorageDomainStatus(StorageDomainStatus.Inactive);
        assertFalse(cmd.checkStorageDomainStatus(StorageDomainStatus.Active));
        assertTrue(commandHasInvalidStatusMessage());
    }

    @Test
    public void statusInList() {
        setStorageDomainStatus(StorageDomainStatus.Inactive);
        assertTrue(cmd.checkStorageDomainStatus(StorageDomainStatus.Locked, StorageDomainStatus.Inactive,
                StorageDomainStatus.Unknown));
        assertFalse(commandHasInvalidStatusMessage());
    }

    @Test
    public void statusNotInList() {
        setStorageDomainStatus(StorageDomainStatus.Inactive);
        assertFalse(cmd.checkStorageDomainStatus(StorageDomainStatus.Locked, StorageDomainStatus.Active,
                StorageDomainStatus.Unknown));
        assertTrue(commandHasInvalidStatusMessage());
    }

    @Test
    public void canDetachInactiveDomain() {
        setStorageDomainStatus(StorageDomainStatus.Inactive);
        storagePoolExists();
        masterDomainIsUp();
        isNotLocalData();
        canDetachDomain();
        assertTrue(cmd.canDetachDomain(false, false, false));
    }

    @Test
    public void canDetachMaintenanceDomain() {
        setStorageDomainStatus(StorageDomainStatus.Inactive);
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

    @Test
    public void lunAlreadyPartOfStorageDomains() {
        LUNs lun1 = new LUNs();
        lun1.setLUN_id(GUIDS[0].toString());
        lun1.setStorageDomainId(Guid.newGuid());
        LUNs lun2 = new LUNs();
        lun2.setLUN_id(GUIDS[1].toString());
        lun2.setStorageDomainId(Guid.newGuid());

        doReturn(lunDAO).when(cmd).getLunDao();
        when(lunDAO.getAll()).thenReturn(Arrays.asList(lun1, lun2));
        List<String> specifiedLunIds = Collections.singletonList(GUIDS[0].toString());

        assertTrue(cmd.isLunsAlreadyInUse(specifiedLunIds));
        List<String> messages = cmd.getReturnValue().getCanDoActionMessages();
        assertEquals(messages.size(), 2);
        assertEquals(messages.get(0), VdcBllMessages.ACTION_TYPE_FAILED_LUNS_ALREADY_PART_OF_STORAGE_DOMAINS.toString());
        assertEquals(messages.get(1), String.format("$lunIds %1$s", cmd.getFormattedLunId(lun1, lun1.getStorageDomainName())));
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
                VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_STATUS_ILLEGAL2.toString());
    }

    private void setStorageDomainStatus(StorageDomainStatus status) {
        StorageDomain domain = new StorageDomain();
        domain.setStatus(status);
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

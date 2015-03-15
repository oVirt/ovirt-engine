package org.ovirt.engine.core.bll.storage;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.CanDoActionTestUtils;
import org.ovirt.engine.core.bll.CommandAssertUtils;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.validator.storage.StorageConnectionValidator;
import org.ovirt.engine.core.common.action.AttachDetachStorageConnectionParameters;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.LunDAO;
import org.ovirt.engine.core.utils.MockEJBStrategyRule;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DetachStorageConnectionFromStorageDomainCommandTest {
    @ClassRule
    public static MockEJBStrategyRule ejbRule = new MockEJBStrategyRule();

    private DetachStorageConnectionFromStorageDomainCommand<AttachDetachStorageConnectionParameters> command = null;

    private StorageConnectionValidator validator = null;

    private StorageDomain domain = null;

    Guid connectionId = Guid.newGuid();
    Guid domainId = Guid.newGuid();

    @Mock
    LunDAO lunDao;

    @Before
    public void init() {

        AttachDetachStorageConnectionParameters parameters = new AttachDetachStorageConnectionParameters();
        parameters.setStorageConnectionId(connectionId.toString());
        parameters.setStorageDomainId(domainId);
        validator = mock(StorageConnectionValidator.class);
        command = spy(new DetachStorageConnectionFromStorageDomainCommand<AttachDetachStorageConnectionParameters>(parameters));
        doReturn(validator).when(command).createStorageConnectionValidator();
        domain = new StorageDomain();
        domain.setId(domainId);
        domain.setStorageDomainType(StorageDomainType.Data);
        domain.setStatus(StorageDomainStatus.Maintenance);
        domain.setStorageType(StorageType.ISCSI);
        domain.setStorage(Guid.newGuid().toString());
        doReturn(domain).when(command).getStorageDomain();
        doReturn(lunDao).when(command).getLunDao();
    }

    @Test
    public void canDoActionSuccess() {
        when(validator.isConnectionExists()).thenReturn(ValidationResult.VALID);
        when(validator.isConnectionForISCSIDomainAttached(domain)).thenReturn(Boolean.TRUE);
        when(validator.isISCSIConnectionAndDomain(domain)).thenReturn(ValidationResult.VALID);
        when(validator.isDomainOfConnectionExistsAndInactive(domain)).thenReturn(ValidationResult.VALID);
        CanDoActionTestUtils.runAndAssertCanDoActionSuccess(command);
    }

    @Test
    public void canDoActionFailure() {
        when(validator.isConnectionExists()).thenReturn(ValidationResult.VALID);
        when(validator.isConnectionForISCSIDomainAttached(domain)).thenReturn(Boolean.FALSE);
        when(validator.isISCSIConnectionAndDomain(domain)).thenReturn(ValidationResult.VALID);
        when(validator.isDomainOfConnectionExistsAndInactive(domain)).thenReturn(ValidationResult.VALID);
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command, VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_CONNECTION_FOR_DOMAIN_NOT_EXIST);
    }

    @Test
    public void canDoActionFailureNotExists() {
        ValidationResult result = new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_CONNECTION_NOT_EXIST);
        when(validator.isConnectionExists()).thenReturn(result);
        when(validator.isISCSIConnectionAndDomain(domain)).thenReturn(ValidationResult.VALID);
        when(validator.isDomainOfConnectionExistsAndInactive(domain)).thenReturn(ValidationResult.VALID);
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command, VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_CONNECTION_NOT_EXIST);
    }

    @Test
    public void executeCommand() {
        LUNs lun1 = new LUNs();
        lun1.setLUN_id(Guid.newGuid().toString());
        lun1.setStorageDomainId(domain.getId());

        LUNs lun2 = new LUNs();
        lun2.setLUN_id(Guid.newGuid().toString());
        lun2.setStorageDomainId(domain.getId());

        LUNs lun3 = new LUNs();
        lun3.setLUN_id(Guid.newGuid().toString());
        lun3.setStorageDomainId(domain.getId());

        List<LUNs> lunsForConnection = new ArrayList<>();
        lunsForConnection.add(lun1);
        lunsForConnection.add(lun2);
        lunsForConnection.add(lun3);

        List<LUNs> lunsForVG = new ArrayList<>();
        lunsForVG.add(lun1);
        lunsForVG.add(lun2);

        when(lunDao.getAllForStorageServerConnection(connectionId.toString())).thenReturn(lunsForConnection);
        when(lunDao.getAllForVolumeGroup(domain.getStorage())).thenReturn(lunsForVG);
        when(lunDao.get(lun1.getLUN_id())).thenReturn(lun1);
        when(lunDao.get(lun2.getLUN_id())).thenReturn(lun2);
        doNothing().when(lunDao).remove(lun1.getLUN_id());
        doNothing().when(lunDao).remove(lun2.getLUN_id());
        command.executeCommand();
        verify(lunDao, times(1)).remove(lun1.getLUN_id());
        verify(lunDao, times(1)).remove(lun2.getLUN_id());
        CommandAssertUtils.checkSucceeded(command, true);
    }
}

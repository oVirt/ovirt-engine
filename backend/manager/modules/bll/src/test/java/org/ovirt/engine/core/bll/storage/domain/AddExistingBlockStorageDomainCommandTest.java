package org.ovirt.engine.core.bll.storage.domain;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.common.action.StorageDomainManagementParameter;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StorageDomainStaticDao;
import org.ovirt.engine.core.utils.MockConfigRule;

public class AddExistingBlockStorageDomainCommandTest extends BaseCommandTest {

    private StorageDomainManagementParameter parameters = new StorageDomainManagementParameter(getStorageDomain());

    @Spy
    @InjectMocks
    private AddExistingBlockStorageDomainCommand<StorageDomainManagementParameter> command =
            new AddExistingBlockStorageDomainCommand<>(parameters, null);

    @ClassRule
    public static MockConfigRule mcr = new MockConfigRule();

    @Mock
    private StorageDomainStaticDao storageDomainStaticDao;

    @Before
    public void setUp() {
        doNothing().when(command).addStorageDomainInDb();
        doNothing().when(command).updateStorageDomainDynamicFromIrs();
        doNothing().when(command).saveLUNsInDB(anyList());
        doNothing().when(command).updateMetadataDevices();
    }

    @Test
    public void testAddExistingBlockDomainSuccessfully() {
        doReturn(getLUNs()).when(command).getLUNsFromVgInfo(parameters.getStorageDomain().getStorage());
        command.executeCommand();
        assertTrue(command.getReturnValue().getSucceeded());
    }

    @Test
    public void testAddExistingBlockDomainWhenVgInfoReturnsEmptyLunList() {
        doReturn(Collections.emptyList()).when(command).getLUNsFromVgInfo(parameters.getStorageDomain().getStorage());
        assertFalse("Could not connect to Storage Domain", command.canAddDomain());
        assertTrue("Import block Storage Domain should have failed due to empty Lun list returned from VGInfo ",
                command.getReturnValue()
                        .getValidationMessages()
                        .contains(EngineMessage.ACTION_TYPE_FAILED_PROBLEM_WITH_CANDIDATE_INFO.toString()));
    }

    @Test
    public void testAlreadyExistStorageDomain() {
        when(storageDomainStaticDao.get(any(Guid.class))).thenReturn(getStorageDomain());
        assertFalse("Storage Domain already exists", command.canAddDomain());
        assertTrue("Import block Storage Domain should have failed due to already existing Storage Domain",
                command.getReturnValue()
                        .getValidationMessages()
                        .contains(EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_ALREADY_EXIST.toString()));
    }

    private static StorageDomainStatic getStorageDomain() {
        StorageDomainStatic storageDomain = new StorageDomainStatic();
        storageDomain.setStorage(Guid.newGuid().toString());
        return storageDomain;
    }

    private static List<LUNs> getLUNs() {
        LUNs lun = new LUNs();
        lun.setId(Guid.newGuid().toString());
        return Collections.singletonList(lun);
    }
}

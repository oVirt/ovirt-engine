package org.ovirt.engine.core.bll.storage;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.action.StorageDomainManagementParameter;
import org.ovirt.engine.core.common.businessentities.LUNs;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.compat.Guid;

import java.util.ArrayList;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class AddExistingBlockStorageDomainCommandTest {

    private AddExistingBlockStorageDomainCommand<StorageDomainManagementParameter> command;
    private StorageDomainManagementParameter parameters;

    @Before
    public void setUp() {
        parameters = new StorageDomainManagementParameter(getStorageDomain());
        parameters.setVdsId(Guid.newGuid());
        command = spy(new AddExistingBlockStorageDomainCommand<>(parameters));

        doNothing().when(command).addStorageDomainInDb();
        doNothing().when(command).updateStorageDomainDynamicFromIrs();
        doNothing().when(command).saveLUNsInDB(any(List.class));
    }

    @Test
    public void testAddExistingBlockDomainSuccessfully() {
        when(command.getLUNsFromVgInfo(parameters.getStorageDomain().getStorage())).thenReturn(getLUNs());
        command.executeCommand();
        assertTrue(command.getReturnValue().getSucceeded());
    }

    private StorageDomainStatic getStorageDomain() {
        StorageDomainStatic storageDomain = new StorageDomainStatic();
        storageDomain.setStorage(Guid.newGuid().toString());
        return storageDomain;
    }

    private List<LUNs> getLUNs() {
        List<LUNs> luns = new ArrayList<>();
        LUNs lun = new LUNs();
        lun.setId(Guid.newGuid().toString());
        luns.add(lun);
        return luns;
    }
}

package org.ovirt.engine.core.bll.storage;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.action.ReconstructMasterParameters;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.errors.VdcBllMessages;

public class ReconstructMasterDomainCommandTest {

    public ReconstructMasterDomainCommand<ReconstructMasterParameters> cmd =
            new ReconstructMasterDomainCommand(new ReconstructMasterParameters());

    private void testAddInvalidSDStatusMessage(StorageDomainStatus status) {
        cmd.addInvalidSDStatusMessage(status);
        List<String> messages = cmd.getReturnValue().getCanDoActionMessages();
        assertEquals(messages.get(0), VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_STATUS_ILLEGAL2.toString());
        assertEquals(messages.get(1), String.format("$status %1$s", status));
    }

    @Test
    public void testAddInvalidSDStatusMessageLocked() {
        testAddInvalidSDStatusMessage(StorageDomainStatus.Locked);
    }

    @Test
    public void testAddInvalidSDStatusMessagePreparingForMaintenance() {
        testAddInvalidSDStatusMessage(StorageDomainStatus.PreparingForMaintenance);
    }
}

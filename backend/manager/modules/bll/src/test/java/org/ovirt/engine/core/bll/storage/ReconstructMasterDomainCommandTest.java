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

    @Test
    public void testAddInvalidSDStatusMessage() {
        StorageDomainStatus status = StorageDomainStatus.Locked;
        cmd.addInvalidSDStatusMessage(status);
        List<String> messages = cmd.getReturnValue().getCanDoActionMessages();
        assertEquals(messages.get(0), VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_STATUS_ILLEGAL2.toString());
        assertEquals(messages.get(1), String.format("$status %1$s", status));
    }
}

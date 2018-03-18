package org.ovirt.engine.core.bll.network.macpool;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Collections;

import org.apache.commons.lang.math.LongRange;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableImpl;

@RunWith(MockitoJUnitRunner.class)
public class MacPoolUsingRangesTest {
    private static final String MAC_ADDRESS = "00:1a:4a:16:01:00";
    @Mock
    private AuditLogDirector auditLogDirector;


    @Test
    public void testReactionToDuplicatesWhenDuplicatesDuringStartup() throws Exception {
        MacPoolUsingRanges macPoolUsingRanges = createMacPoolDisallowingDuplicates();
        macPoolUsingRanges.initialize(true, Arrays.asList(MAC_ADDRESS, MAC_ADDRESS));
        verify(auditLogDirector).log(any(AuditLogableImpl.class), eq(AuditLogType.MAC_ADDRESS_VIOLATES_NO_DUPLICATES_SETTING), anyString());
    }

    @Test(expected = EngineException.class)
    public void testReactionToDuplicatesWhenDuplicatesNotDuringStartup() throws Exception {
        MacPoolUsingRanges macPoolUsingRanges = createMacPoolDisallowingDuplicates();
        macPoolUsingRanges.initialize(false, Arrays.asList(MAC_ADDRESS, MAC_ADDRESS));
    }

    private MacPoolUsingRanges createMacPoolDisallowingDuplicates() {
        return new MacPoolUsingRanges(Guid.newGuid(),
                Collections.singletonList(new LongRange(1, 2)),
                false,
                auditLogDirector);
    }
}

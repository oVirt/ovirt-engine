package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.network.macpool.ReadMacPool;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.utils.ReplacementUtils;

@RunWith(MockitoJUnitRunner.class)
public class ChangeVmClusterValidatorTest {

    @Mock
    private ReadMacPool targetMacPool;

    @InjectMocks
    ChangeVmClusterValidator underTest = new ChangeVmClusterValidator(null, null, null);

    private static final String MAC_TO_MIGRATE = "mac";
    private static final List<String> MACS_TO_MIGRATE = Collections.singletonList(MAC_TO_MIGRATE);

    @Test
    public void testMacPoolChangedAndMacsCannotBeMoved() throws Exception {

        mockAllowedDuplicatesOnTargetPool(false);
        mockMacIsUsedInTargetPool(true);

        EngineMessage engineMessage =
                EngineMessage.ACTION_TYPE_FAILED_CANNOT_UPDATE_VM_TARGET_CLUSTER_HAS_DUPLICATED_MACS;
        Collection<String> replacements =
                ReplacementUtils.getListVariableAssignmentString(engineMessage, MACS_TO_MIGRATE);

        assertThat(underTest.validateCanMoveMacs(targetMacPool, MACS_TO_MIGRATE),
                failsWith(engineMessage, replacements));
    }

    @Test
    public void testMacPoolChangedAndMacsCanBeMovedBecauseOfNoDuplicates() throws Exception {
        mockAllowedDuplicatesOnTargetPool(false);
        mockMacIsUsedInTargetPool(false);
        assertThat(underTest.validateCanMoveMacs(targetMacPool, MACS_TO_MIGRATE), isValid());
    }

    @Test
    public void testMacPoolChangedAndMacsCanBeMovedBecauseOfAllowedDuplicates() throws Exception {
        mockAllowedDuplicatesOnTargetPool(true);
        mockMacIsUsedInTargetPool(true);
        assertThat(underTest.validateCanMoveMacs(targetMacPool, MACS_TO_MIGRATE), isValid());
    }

    private void mockMacIsUsedInTargetPool(boolean value) {
        when(targetMacPool.isMacInUse(MAC_TO_MIGRATE)).thenReturn(value);
    }

    private void mockAllowedDuplicatesOnTargetPool(boolean allowed) {
        when(targetMacPool.isDuplicateMacAddressesAllowed()).thenReturn(allowed);
    }
}

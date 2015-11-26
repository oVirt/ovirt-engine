package org.ovirt.engine.core.bll.validator.network;

import static org.junit.Assert.assertThat;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.utils.ReplacementUtils.replaceWith;
import static org.ovirt.engine.core.utils.linq.LinqUtils.concat;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.errors.EngineMessage;

public class DetachNetworkUsedByVmValidatorTest {
    private static final String NETWORK_A = "networkA";
    private static final String NETWORK_B = "networkB";
    private static final String VM_A = "vmA";
    private static final String VM_B = "vmB";
    private DetachNetworkUsedByVmValidator underTest;

    @Test
    public void testValidateNotRemovingUsedNetworkByVmsSingleNetworkMultipleVms() {
        final List<String> vmsNames = Arrays.asList(VM_A, VM_B);
        final List<String> removedNetworks = Arrays.asList(NETWORK_A);
        underTest = new DetachNetworkUsedByVmValidator(vmsNames, removedNetworks);
        assertThat(underTest.validate(),
                failsWith(EngineMessage.SINGLE_NETWORK_CANNOT_DETACH_NETWORK_USED_BY_VMS,
                        concat(replaceWith(DetachNetworkUsedByVmValidator.VAR_VM_NAMES, vmsNames),
                                replaceWith(DetachNetworkUsedByVmValidator.VAR_NETWORK_NAME, removedNetworks))));
    }

    @Test
    public void testValidateNotRemovingUsedNetworkByVmsSingleNetworkSingleVm() {
        final List<String> vmsNames = Arrays.asList(VM_A);
        final List<String> removedNetworks = Arrays.asList(NETWORK_A);
        underTest = new DetachNetworkUsedByVmValidator(vmsNames, removedNetworks);
        assertThat(underTest.validate(),
                failsWith(EngineMessage.SINGLE_NETWORK_CANNOT_DETACH_NETWORK_USED_BY_SINGLE_VM,
                        concat(replaceWith(DetachNetworkUsedByVmValidator.VAR_VM_NAME, vmsNames),
                                replaceWith(DetachNetworkUsedByVmValidator.VAR_NETWORK_NAME, removedNetworks))));
    }

    @Test
    public void testValidateNotRemovingUsedNetworkByVmsMultipleNetworkSingleVm() {
        final List<String> vmsNames = Arrays.asList(VM_A);
        final List<String> removedNetworks = Arrays.asList(NETWORK_A, NETWORK_B);
        underTest = new DetachNetworkUsedByVmValidator(vmsNames, removedNetworks);
        assertThat(underTest.validate(),
                failsWith(EngineMessage.MULTIPLE_NETWORKS_CANNOT_DETACH_NETWORKS_USED_BY_SINGLE_VM,
                        concat(replaceWith(DetachNetworkUsedByVmValidator.VAR_NETWORK_NAMES, removedNetworks),
                                replaceWith(DetachNetworkUsedByVmValidator.VAR_VM_NAME, vmsNames))));
    }

    @Test
    public void testValidateNotRemovingUsedNetworkByVmsMultipleNetworkMultipleVm() {
        final List<String> vmsNames = Arrays.asList(VM_A, VM_B);
        final List<String> removedNetworks = Arrays.asList(NETWORK_A, NETWORK_B);
        underTest = new DetachNetworkUsedByVmValidator(vmsNames, removedNetworks);
        assertThat(underTest.validate(),
                failsWith(EngineMessage.MULTIPLE_NETWORKS_CANNOT_DETACH_NETWORKS_USED_BY_VMS,
                        concat(replaceWith(DetachNetworkUsedByVmValidator.VAR_NETWORK_NAMES, removedNetworks),
                                replaceWith(DetachNetworkUsedByVmValidator.VAR_VM_NAMES, vmsNames))));
    }
}

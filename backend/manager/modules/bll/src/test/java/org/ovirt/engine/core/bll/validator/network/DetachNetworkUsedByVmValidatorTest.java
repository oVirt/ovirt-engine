package org.ovirt.engine.core.bll.validator.network;

import static org.junit.Assert.assertThat;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.utils.ReplacementUtils.replaceWith;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
                        Stream.concat(replaceWith(DetachNetworkUsedByVmValidator.VAR_VM_NAMES, vmsNames).stream(),
                                replaceWith(DetachNetworkUsedByVmValidator.VAR_NETWORK_NAME, removedNetworks).stream())
                                .collect(Collectors.toList())));
    }

    @Test
    public void testValidateNotRemovingUsedNetworkByVmsSingleNetworkSingleVm() {
        final List<String> vmsNames = Arrays.asList(VM_A);
        final List<String> removedNetworks = Arrays.asList(NETWORK_A);
        underTest = new DetachNetworkUsedByVmValidator(vmsNames, removedNetworks);
        assertThat(underTest.validate(),
                failsWith(EngineMessage.SINGLE_NETWORK_CANNOT_DETACH_NETWORK_USED_BY_SINGLE_VM,
                        Stream.concat(replaceWith(DetachNetworkUsedByVmValidator.VAR_VM_NAME, vmsNames).stream(),
                                replaceWith(DetachNetworkUsedByVmValidator.VAR_NETWORK_NAME, removedNetworks).stream())
                                .collect(Collectors.toList())));
    }

    @Test
    public void testValidateNotRemovingUsedNetworkByVmsMultipleNetworkSingleVm() {
        final List<String> vmsNames = Arrays.asList(VM_A);
        final List<String> removedNetworks = Arrays.asList(NETWORK_A, NETWORK_B);
        underTest = new DetachNetworkUsedByVmValidator(vmsNames, removedNetworks);
        assertThat(underTest.validate(),
                failsWith(EngineMessage.MULTIPLE_NETWORKS_CANNOT_DETACH_NETWORKS_USED_BY_SINGLE_VM,
                        Stream.concat(replaceWith(DetachNetworkUsedByVmValidator.VAR_NETWORK_NAMES, removedNetworks).stream(),
                                replaceWith(DetachNetworkUsedByVmValidator.VAR_VM_NAME, vmsNames).stream())
                                .collect(Collectors.toList())));
    }

    @Test
    public void testValidateNotRemovingUsedNetworkByVmsMultipleNetworkMultipleVm() {
        final List<String> vmsNames = Arrays.asList(VM_A, VM_B);
        final List<String> removedNetworks = Arrays.asList(NETWORK_A, NETWORK_B);
        underTest = new DetachNetworkUsedByVmValidator(vmsNames, removedNetworks);
        assertThat(underTest.validate(),
                failsWith(EngineMessage.MULTIPLE_NETWORKS_CANNOT_DETACH_NETWORKS_USED_BY_VMS,
                        Stream.concat(replaceWith(DetachNetworkUsedByVmValidator.VAR_NETWORK_NAMES, removedNetworks).stream(),
                                replaceWith(DetachNetworkUsedByVmValidator.VAR_VM_NAMES, vmsNames).stream())
                                .collect(Collectors.toList())));
    }
}

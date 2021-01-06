package org.ovirt.engine.core.bll.validator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.utils.NumaTestUtils.createVdsNumaNode;
import static org.ovirt.engine.core.bll.utils.NumaTestUtils.createVmNumaNode;
import static org.ovirt.engine.core.common.businessentities.MigrationSupport.PINNED_TO_HOST;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.hostdev.HostDeviceManager;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;

@ExtendWith(MockitoExtension.class)
public class InClusterUpgradeValidatorTest {

    @Mock
    HostDeviceManager hostDeviceManager;

    VM invalidVM;

    VM validVM;

    VDS oldHost;

    VDS newHost1;

    VDS newHost2;

    @InjectMocks
    InClusterUpgradeValidator validator = new InClusterUpgradeValidator();

    @BeforeEach
    public void setUp() {
        invalidVM = newVM();
        validVM = newVM();
        oldHost = newHost("RHEV Hypervisor - 6.1 - 1.el6");
        newHost1 = newHost("RHEL - 7.2 - 1.el7");
        newHost2 = newHost("RHEL - 7.2 - 1.el7"); }

    @Test
    public void shouldDetectUpgradeInProgress() {
        assertThat(validator.isUpgradeDone(Arrays.asList(oldHost, newHost1))).isNotEqualTo(ValidationResult.VALID);
    }

    @Test
    public void shouldDetectInvalidHostOsOnNewlyAddedHost() {
        newHost1.setHostOs("which OS am I?");
        assertThat(validator.isUpgradeDone(Arrays.asList(newHost1, newHost2))).isNotEqualTo(ValidationResult.VALID);
    }

    @Test
    public void shouldDetectUpgradeDone() {
        assertThat(validator.isUpgradeDone(Arrays.asList(newHost1, newHost2))).isEqualTo(ValidationResult.VALID);
    }

    @Test
    public void shouldDetectUpgradePossible() {
        assertThat(validator.isUpgradePossible(Arrays.asList(newHost1, newHost2), Collections.singletonList(validVM)))
                .isEqualTo(ValidationResult.VALID);
    }

    @Test
    public void shouldDetectInvalidHostOsIdentifier() {
        newHost1.setHostOs("which OS am I?");
        assertThat(validator.isUpgradePossible(Arrays.asList(newHost1, newHost2), Collections.singletonList(validVM)))
                .isNotEqualTo(ValidationResult.VALID);
    }

    @Test
    public void shouldDetectInvalidVMOnClusterUpgradeCheck() {
        invalidVM.setCpuPinning("i am pinned");
        assertThat(validator.isUpgradePossible(Arrays.asList(newHost1, newHost2), Arrays.asList(validVM, invalidVM)))
                .isNotEqualTo(ValidationResult.VALID);
    }

    @Test
    public void shouldDetectNonMigratableVMs() {
        invalidVM.setMigrationSupport(PINNED_TO_HOST);
        assertThat(validator.checkVmReadyForUpgrade(invalidVM)).contains(
                EngineMessage.CLUSTER_UPGRADE_DETAIL_VM_NOT_MIGRATABLE.name());
    }

    @Test
    public void shouldDetectCpuPinning() {
        invalidVM.setCpuPinning("i am pinned");
        assertThat(validator.checkVmReadyForUpgrade(invalidVM)).contains(
                EngineMessage.CLUSTER_UPGRADE_DETAIL_VM_CPUS_PINNED.name());
    }

    @Test
    public void shouldDetectNumaPinning() {
        invalidVM.setvNumaNodeList(Collections.singletonList(createVmNumaNode(1, Collections.singletonList(createVdsNumaNode(1)))));
        assertThat(validator.checkVmReadyForUpgrade(invalidVM)).contains(
                EngineMessage.CLUSTER_UPGRADE_DETAIL_VM_NUMA_PINNED.name());
    }

    @Test
    public void shouldAllowUnpinnedNumaNodes() {
        validVM.setvNumaNodeList(Collections.singletonList(createVmNumaNode(1)));
        assertThat(validator.checkVmReadyForUpgrade(validVM)).isEmpty();
    }

    @Test
    public void shouldDetectSuspendedVM() {
        invalidVM.setStatus(VMStatus.Suspended);
        assertThat(validator.checkVmReadyForUpgrade(invalidVM)).contains(
                EngineMessage.CLUSTER_UPGRADE_DETAIL_VM_SUSPENDED.name());
    }

    @Test
    public void shouldDetectPassThroughDeviceOnVM() {
        when(hostDeviceManager.checkVmNeedsDirectPassthrough(any())).thenReturn(true);
        assertThat(validator.checkVmReadyForUpgrade(invalidVM)).contains(
                EngineMessage.CLUSTER_UPGRADE_DETAIL_VM_NEEDS_PASSTHROUGH.name());
    }

    @Test
    public void shouldCreateNiceValidationResult() {
        invalidVM.setCpuPinning("i am pinned");
        invalidVM.setDedicatedVmForVdsList(Collections.singletonList(Guid.newGuid()));
        invalidVM.setMigrationSupport(PINNED_TO_HOST);
        invalidVM.setId(Guid.Empty);
        newHost1.setHostOs("invalid os");
        ValidationResult validationResult = validator.isUpgradePossible(Collections.singletonList(newHost1),
                Collections.singletonList(invalidVM));
        assertThat(validationResult.getVariableReplacements()).contains(
                "CLUSTER_UPGRADE_DETAIL_HOST_INVALID_OS",
                "CLUSTER_UPGRADE_DETAIL_VM_CPUS_PINNED",
                "CLUSTER_UPGRADE_DETAIL_VM_NOT_MIGRATABLE");
    }

    @Test
    public void shouldAllowUpgradeForVM() {
        assertThat(validator.checkVmReadyForUpgrade(validVM)).isEmpty();
    }

    private VM newVM() {
        final VM vm = new VM();
        vm.setId(Guid.newGuid());
        return vm;
    }

    private VDS newHost(String os) {
        final VDS host = new VDS();
        host.setId(Guid.newGuid());
        host.setHostOs(os);
        return host;
    }
}

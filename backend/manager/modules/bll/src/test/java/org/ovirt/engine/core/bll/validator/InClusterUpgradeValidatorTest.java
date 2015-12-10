package org.ovirt.engine.core.bll.validator;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.utils.NumaTestUtils.createVdsNumaNode;
import static org.ovirt.engine.core.bll.utils.NumaTestUtils.createVmNumaNode;
import static org.ovirt.engine.core.bll.validator.InClusterUpgradeValidator.UPGRADE_ERROR.VM_CPUS_PINNED;
import static org.ovirt.engine.core.bll.validator.InClusterUpgradeValidator.UPGRADE_ERROR.VM_NEEDS_PASSTHROUGH;
import static org.ovirt.engine.core.bll.validator.InClusterUpgradeValidator.UPGRADE_ERROR.VM_NUMA_PINNED;
import static org.ovirt.engine.core.bll.validator.InClusterUpgradeValidator.UPGRADE_ERROR.VM_SUSPENDED;
import static org.ovirt.engine.core.common.businessentities.MigrationSupport.PINNED_TO_HOST;

import java.io.IOException;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.hostdev.HostDeviceManager;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.compat.Guid;

@RunWith(MockitoJUnitRunner.class)
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

    @Before
    public void setUp() throws Exception {
        invalidVM = newVM();
        validVM = newVM();
        oldHost = newHost("RHEL - 6.1 - 1.el6");
        newHost1 = newHost("RHEL - 7.2 - 1.el7");
        newHost2 = newHost("RHEL - 7.2 - 1.el7");
    }

    @Test
    public void shouldDetectUpgradeInProgress() {
        assertThat(validator.isUpgradeDone(Arrays.asList(oldHost, newHost1)), not(equalTo(ValidationResult.VALID)));
    }

    @Test
    public void shouldDetectInvalidHostOsOnNewlyAddedHost() {
        newHost1.setHostOs("which OS am I?");
        assertThat(validator.isUpgradeDone(Arrays.asList(newHost1, newHost2)), not(equalTo(ValidationResult.VALID)));
    }

    @Test
    public void shouldDetectUpgradeDone() {
        assertThat(validator.isUpgradeDone(Arrays.asList(newHost1, newHost2)), equalTo(ValidationResult.VALID));
    }

    @Test
    public void shouldDetectUpgradePossible() {
        assertThat(validator.isUpgradePossible(Arrays.asList(newHost1, newHost2), Arrays.asList(validVM)),
                equalTo(ValidationResult.VALID));
    }

    @Test
    public void shouldDetectInvalidHostOsIdentifier() {
        newHost1.setHostOs("which OS am I?");
        assertThat(validator.isUpgradePossible(Arrays.asList(newHost1, newHost2), Arrays.asList(validVM)),
                not(equalTo(ValidationResult.VALID)));
    }

    @Test
    public void shouldDetectInvalidVMOnClusterUpgradeCheck() {
        invalidVM.setCpuPinning("i am pinned");
        assertThat(validator.isUpgradePossible(Arrays.asList(newHost1, newHost2), Arrays.asList(validVM, invalidVM)),
                not(equalTo(ValidationResult.VALID)));
    }

    @Test
    public void shouldDetectCpuPinning() {
        invalidVM.setCpuPinning("i am pinned");
        assertThat(validator.isVmReadyForUpgrade(invalidVM), hasItem(VM_CPUS_PINNED));
    }

    @Test
    public void shouldDetectNumaPinning() {
        invalidVM.setvNumaNodeList(Arrays.asList(createVmNumaNode(1, Arrays.asList(createVdsNumaNode(1)))));
        assertThat(validator.isVmReadyForUpgrade(invalidVM), hasItem(VM_NUMA_PINNED));
    }

    @Test
    public void shouldAllowUnpinnedNumaNodes() {
        validVM.setvNumaNodeList(Arrays.asList(createVmNumaNode(1)));
        assertThat(validator.isVmReadyForUpgrade(validVM), is(empty()));
    }

    @Test
    public void shouldDetectSuspendedVM() {
        invalidVM.setStatus(VMStatus.Suspended);
        assertThat(validator.isVmReadyForUpgrade(invalidVM), hasItem(VM_SUSPENDED));
    }

    @Test
    public void shouldDetectPassThroughDeviceOnVM() {
        when(hostDeviceManager.checkVmNeedsDirectPassthrough(any(VM.class))).thenReturn(true);
        assertThat(validator.isVmReadyForUpgrade(invalidVM), hasItem(VM_NEEDS_PASSTHROUGH));
    }

    @Test
    public void shouldCreateNiceJsonValidationResult() throws IOException {
        invalidVM.setCpuPinning("i am pinned");
        invalidVM.setDedicatedVmForVdsList(Guid.newGuid());
        invalidVM.setMigrationSupport(PINNED_TO_HOST);
        invalidVM.setId(Guid.Empty);
        newHost1.setHostOs("invalid os");
        ValidationResult validationResult = validator.isUpgradePossible(Arrays.asList(newHost1),
                Arrays.asList(invalidVM));

        assertThat(validationResult.getVariableReplacements().get(0),
                containsString("VM_CPUS_PINNED"));
        assertThat(validationResult.getVariableReplacements().get(0),
                containsString("VM_NOT_MIGRATABLE"));
        assertThat(validationResult.getVariableReplacements().get(0),
                containsString("HOST_INVALID_OS"));
    }

    @Test
    public void shouldAllowUpgradeForVM() {
        assertThat(validator.isVmReadyForUpgrade(validVM), is(empty()));
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

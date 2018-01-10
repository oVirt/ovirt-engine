package org.ovirt.engine.core.utils.ovf;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.storage.FullEntityOvfData;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.utils.MockConfigRule;

@RunWith(MockitoJUnitRunner.class)
public class HostedEngineOvfWriterTest {

    private VM vm;
    private FullEntityOvfData fullEntityOvfData;
    private String emulatedMachine;
    private Version version = Version.getLast();
    private String cpuId;
    private String engineXml;

    private HostedEngineOvfWriter underTest;

    @Mock
    private OsRepository osRepository;

    @Rule
    public MockConfigRule mockConfigRule = new MockConfigRule(
            mockConfig(ConfigValues.VdcVersion, version.getValue()),
            mockConfig(ConfigValues.MaxNumOfVmCpus, version, 160),
            mockConfig(ConfigValues.MaxNumOfVmSockets, version, 4));

    @Before
    public void setup() {
        initVm();
        fullEntityOvfData = new FullEntityOvfData();
        emulatedMachine = "pc";
        cpuId = "SandyBridge";
        engineXml = "<Envelope></Envelope>";
        underTest = new HostedEngineOvfWriter(
                vm,
                fullEntityOvfData,
                version,
                emulatedMachine,
                cpuId,
                osRepository,
                engineXml);
    }

    @Test
    public void clusterEmulatedMachineIsBuilt() {
        assertThat(underTest.build().getStringRepresentation().contains("<CustomEmulatedMachine>pc</CustomEmulatedMachine>"))
                .isTrue();
    }

    @Test
    public void clusterEmulatedMachineIsNull() {
        emulatedMachine = null;
        assertThatThrownBy(
                () -> new HostedEngineOvfWriter(vm, fullEntityOvfData, version, emulatedMachine, cpuId, osRepository, engineXml))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("emulated machine");
    }

    @Test
    public void cpuNameIsBuild() {
        assertThat(underTest.build().getStringRepresentation().contains("<CustomCpuName>SandyBridge</CustomCpuName>"))
                .isTrue();
    }

    @Test
    public void cpuIdIsNull() {
        cpuId = null;
        assertThatThrownBy(
                () -> new HostedEngineOvfWriter(vm, fullEntityOvfData, version, emulatedMachine, cpuId, osRepository, engineXml))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("cpuId");
    }

    @Test
    public void notHostedEngineVM() {
        vm.setOrigin(OriginType.OVIRT);
        assertThatThrownBy(
                () -> new HostedEngineOvfWriter(vm, fullEntityOvfData, version, emulatedMachine, cpuId, osRepository, engineXml))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessageContaining("Hosted Engine");

    }

    private void initVm() {
        vm = new VM();
        vm.setName("Hosted Engine");
        vm.setOrigin(OriginType.MANAGED_HOSTED_ENGINE);
        vm.setVmOs(OsRepository.DEFAULT_X86_OS);
        vm.setCpuPerSocket(4);
        vm.setThreadsPerCpu(2);
    }
}

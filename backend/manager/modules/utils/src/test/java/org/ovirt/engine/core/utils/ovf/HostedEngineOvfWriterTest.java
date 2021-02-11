package org.ovirt.engine.core.utils.ovf;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.BiosType;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.storage.FullEntityOvfData;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.utils.MockConfigDescriptor;
import org.ovirt.engine.core.utils.MockConfigExtension;

@ExtendWith({MockitoExtension.class, MockConfigExtension.class})
public class HostedEngineOvfWriterTest {

    private VM vm;
    private FullEntityOvfData fullEntityOvfData;
    private String emulatedMachine;
    private Version version = Version.getLast();
    private String cpuId;
    private String engineXml;

    private HostedEngineOvfWriter underTest;

    private static Map<String, Integer> createMaxNumberOfVmCpusMap() {
        Map<String, Integer> maxVmCpusMap = new HashMap<>();
        maxVmCpusMap.put("s390x", 384);
        maxVmCpusMap.put("x86", 160);
        maxVmCpusMap.put("ppc", 384);
        return maxVmCpusMap;
    }

    @Mock
    private OsRepository osRepository;

    public static Stream<MockConfigDescriptor<?>> mockConfiguration() {
        return Stream.of(
                MockConfigDescriptor.of(ConfigValues.VdcVersion, Version.getLast().getValue()),
                MockConfigDescriptor.of(ConfigValues.MaxNumOfVmCpus, Version.getLast(), createMaxNumberOfVmCpusMap()),
                MockConfigDescriptor.of(ConfigValues.MaxNumOfVmSockets, Version.getLast(), 4)
        );
    }

    @BeforeEach
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
        vm.setClusterBiosType(BiosType.Q35_SEA_BIOS);
        vm.setClusterArch(ArchitectureType.x86_64);
        vm.setBiosType(BiosType.Q35_SEA_BIOS);
    }
}

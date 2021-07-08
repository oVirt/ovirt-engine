package org.ovirt.engine.core.bll.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.BiosType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.migration.NoMigrationPolicy;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.SimpleDependencyInjector;
import org.ovirt.engine.core.common.utils.customprop.VmPropertiesUtils;
import org.ovirt.engine.core.common.utils.exceptions.InitializationException;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.utils.MockConfigDescriptor;
import org.ovirt.engine.core.utils.MockConfigExtension;

@ExtendWith({MockitoExtension.class, MockConfigExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
public class CompatibilityVersionUpdaterTest {
    private static final int OS_ID = 30;
    private static final int MAX_MEM = 4194304;

    private static Map<String, Integer> createMaxNumberOfVmCpusMap() {
        Map<String, Integer> maxVmCpusMap = new HashMap<>();
        maxVmCpusMap.put("s390x", 384);
        maxVmCpusMap.put("x86", 710);
        maxVmCpusMap.put("ppc", 384);
        return maxVmCpusMap;
    }

    public static Stream<MockConfigDescriptor<?>> mockConfiguration() {
        return Stream.of(
                MockConfigDescriptor.of(ConfigValues.SupportedClusterLevels, new HashSet<>(Version.ALL)),

                MockConfigDescriptor.of(ConfigValues.PredefinedVMProperties, Version.getLast(), "prop_1=^(true|false)$"),
                MockConfigDescriptor.of(ConfigValues.UserDefinedVMProperties, Version.getLast(), "prop_2=^(true|false)$"),

                MockConfigDescriptor.of(ConfigValues.VM64BitMaxMemorySizeInMB, Version.getLast(), MAX_MEM),
                MockConfigDescriptor.of(ConfigValues.MaxNumOfVmCpus, Version.getLast(), createMaxNumberOfVmCpusMap()),
                MockConfigDescriptor.of(ConfigValues.MaxNumOfVmSockets, Version.getLast(), 16),
                MockConfigDescriptor.of(ConfigValues.MaxNumOfCpuPerSocket, Version.getLast(), 254),
                MockConfigDescriptor.of(ConfigValues.MaxNumOfThreadsPerCpu, Version.getLast(), 8),
                MockConfigDescriptor.of(ConfigValues.BiosTypeSupported, Version.getLast(), true),

                MockConfigDescriptor.of(ConfigValues.IsMigrationSupported, Version.getLast(), Map.of(
                        "undefined", "true",
                        "x86", "true",
                        "ppc", "true"
                ))
        );
    }

    @Mock
    private OsRepository osRepository;

    private Cluster cluster;
    private VM vm;

    private CompatibilityVersionUpdater versionUpdater;

    @BeforeEach
    public void setUp() throws InitializationException {
        Map<Integer, Map<Version, List<Pair<GraphicsType, DisplayType>>>> displayTypeMap = new HashMap<>();
        displayTypeMap.put(OS_ID, new HashMap<>());
        displayTypeMap.get(OS_ID).put(null, Collections.singletonList(new Pair<>(GraphicsType.SPICE, DisplayType.qxl)));
        when(osRepository.getGraphicsAndDisplays()).thenReturn(displayTypeMap);
        when(osRepository.get64bitOss()).thenReturn(List.of(OS_ID));
        when(osRepository.getOsArchitectures()).thenReturn(Map.of(OS_ID, ArchitectureType.x86_64));
        SimpleDependencyInjector.getInstance().bind(OsRepository.class, osRepository);

        cluster = new Cluster();
        cluster.setId(Guid.newGuid());
        cluster.setArchitecture(ArchitectureType.x86_64);
        cluster.setCompatibilityVersion(Version.getLast());
        cluster.setBiosType(BiosType.Q35_SEA_BIOS);

        vm = createVm();

        VmPropertiesUtils propertiesUtils = spy(new VmPropertiesUtils());
        propertiesUtils.init();

        versionUpdater = spy(new CompatibilityVersionUpdater());
        doReturn(propertiesUtils).when(versionUpdater).getVmPropertiesUtils();
    }

    @Test
    public void testUpdateMemory() {
        int hugeMem = 10000000;
        int hugeMaxMem = 20000000;
        int minMem = 1024;

        vm.setVmMemSizeMb(hugeMem);
        vm.setMaxMemorySizeMb(hugeMaxMem);
        vm.setMinAllocatedMem(minMem);

        var updates = performUpdate();

        assertThat(updates).containsOnly(VmUpdateType.MEMORY);
        assertThat(vm.getVmMemSizeMb()).isEqualTo(MAX_MEM);
        assertThat(vm.getMaxMemorySizeMb()).isEqualTo(MAX_MEM);
        assertThat(vm.getMinAllocatedMem()).isEqualTo(minMem);
    }

    @Test
    public void testUpdateCpuTopology() {
        vm.setNumOfSockets(16);
        vm.setCpuPerSocket(16);
        vm.setThreadsPerCpu(4);

        var updates = performUpdate();

        assertThat(updates).containsOnly(VmUpdateType.CPU_TOPOLOGY);
        assertThat(vm.getNumOfSockets()).isEqualTo(11);
        assertThat(vm.getCpuPerSocket()).isEqualTo(16);
        assertThat(vm.getThreadsPerCpu()).isEqualTo(4);
    }

    @Test
    public void testUpdateCpuTopologyManyCpus() {
        vm.setNumOfSockets(512);
        vm.setCpuPerSocket(512);
        vm.setThreadsPerCpu(512);

        var updates = performUpdate();

        assertThat(updates).containsOnly(VmUpdateType.CPU_TOPOLOGY);
        assertThat(vm.getNumOfSockets()).isEqualTo(1);
        assertThat(vm.getCpuPerSocket()).isEqualTo(88);
        assertThat(vm.getThreadsPerCpu()).isEqualTo(8);
    }

    @Test
    public void testUpdateProperties() {
        vm.setCustomProperties("prop_1=false;prop_2=incorrect;nonexistent=123");
        var updates = performUpdate();

        assertThat(updates).containsOnly(VmUpdateType.PROPERTIES);
        assertThat(vm.getCustomProperties()).isEqualTo("prop_1=false");
    }

    @Test
    public void testUpdateMigration() {
        cluster.setMigrationPolicyId(Guid.newGuid());
        vm.setMigrationPolicyId(NoMigrationPolicy.ID);
        var updates = performUpdate();

        assertThat(updates).containsOnly(VmUpdateType.MIGRATION_POLICY);
        assertThat(vm.getMigrationPolicyId()).isEqualTo(cluster.getMigrationPolicyId());
    }

    @Test
    public void testUpdateDisplaytype() {
        vm.setDefaultDisplayType(DisplayType.cirrus);
        var updates = performUpdate();
        assertThat(updates).containsOnly(VmUpdateType.DEFAULT_DISPLAY_TYPE);
        assertThat(vm.getDefaultDisplayType()).isEqualTo(DisplayType.vga);
    }

    private Set<VmUpdateType> performUpdate() {
        return versionUpdater.updateVmCompatibilityVersion(vm, Version.getLast(), cluster);
    }

    private VM createVm() {
        VM vm = new VM();
        vm.setId(Guid.newGuid());
        vm.setClusterArch(cluster.getArchitecture());
        vm.setMigrationPolicyId(Guid.newGuid());
        vm.setBiosType(BiosType.Q35_SEA_BIOS);
        vm.setDefaultDisplayType(DisplayType.vga);
        vm.setVmOs(OS_ID);
        vm.setClusterArch(ArchitectureType.x86_64);

        vm.setVmMemSizeMb(2048);
        vm.setMaxMemorySizeMb(4096);
        vm.setMinAllocatedMem(1024);

        vm.setNumOfSockets(2);
        vm.setCpuPerSocket(4);
        vm.setThreadsPerCpu(2);

        vm.setClusterCompatibilityVersionOrigin(Version.getLowest());
        return vm;
    }
}

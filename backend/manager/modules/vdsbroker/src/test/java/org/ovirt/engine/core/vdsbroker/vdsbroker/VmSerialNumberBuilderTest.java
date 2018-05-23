package org.ovirt.engine.core.vdsbroker.vdsbroker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.SerialNumberPolicy;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.utils.MockConfigDescriptor;
import org.ovirt.engine.core.utils.MockConfigExtension;
import org.ovirt.engine.core.utils.MockedConfig;

@ExtendWith({MockitoExtension.class, MockConfigExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
public class VmSerialNumberBuilderTest {

    private static final Guid VM_ID = Guid.newGuid();
    private static final String CUSTOM_VM_SERIAL = "custom VM serial";
    private static final String CUSTOM_CUSTER_SERIAL = "custom CLUSTER serial";
    private static final String CUSTOM_CONFIG_SERIAL = "custom CONFIG serial";

    @Mock
    private ClusterDao clusterDao;

    @InjectMocks
    private VmSerialNumberBuilder vmSerialNumberBuilder;

    VM vm;
    Cluster cluster;

    @BeforeEach
    public void setUp() {
        vm = new VM();
        vm.setId(VM_ID);

        cluster = new Cluster();
        when(clusterDao.get(any())).thenReturn(cluster);
    }

    @Test
    public void testVmHostPolicy() {
        setupVmWithSerialNumber(SerialNumberPolicy.HOST_ID, null);
        assertSerialNumber(null);
    }

    @Test
    public void testVmIdPolicy() {
        setupVmWithSerialNumber(SerialNumberPolicy.VM_ID, null);
        assertSerialNumber(VM_ID.toString());
    }

    @Test
    public void testVmCustomPolicy() {
        setupVmWithSerialNumber(SerialNumberPolicy.CUSTOM, CUSTOM_VM_SERIAL);
        assertSerialNumber(CUSTOM_VM_SERIAL);
    }

    @Test
    public void testClusterHostPolicy() {
        setupClusterWithSerialNumber(SerialNumberPolicy.HOST_ID, null);
        assertSerialNumber(null);
    }

    @Test
    public void testClusterVmIdPolicy() {
        setupClusterWithSerialNumber(SerialNumberPolicy.VM_ID, null);
        assertSerialNumber(VM_ID.toString());
    }

    @Test
    public void testClusterCustomPolicy() {
        setupClusterWithSerialNumber(SerialNumberPolicy.CUSTOM, CUSTOM_CUSTER_SERIAL);
        assertSerialNumber(CUSTOM_CUSTER_SERIAL);
    }

    @Test
    @MockedConfig("mockConfigForHost")
    public void testConfigHostPolicy() {
        assertSerialNumber(null);
    }

    public static Stream<MockConfigDescriptor<?>> mockConfigForHost() {
        return mockConfigWithSerialNumber(SerialNumberPolicy.HOST_ID, null);
    }

    @Test
    @MockedConfig("mockConfigForVM")
    public void testConfigVmIdPolicy() {
        assertSerialNumber(VM_ID.toString());
    }

    public static Stream<MockConfigDescriptor<?>> mockConfigForVM() {
        return mockConfigWithSerialNumber(SerialNumberPolicy.VM_ID, null);
    }

    @Test
    @MockedConfig("mockConfigForSerial")
    public void testConfigCustomPolicy() {
        assertSerialNumber(CUSTOM_CONFIG_SERIAL);
    }

    @Test
    public void testVmAppliedBeforeCluster() {
        setupVmWithSerialNumber(SerialNumberPolicy.HOST_ID, null);
        setupClusterWithSerialNumber(SerialNumberPolicy.VM_ID, null);
        assertSerialNumber(null);
    }

    @Test
    @MockedConfig("mockConfigForSerial")
    public void testVmAppliedBeforeConfig() {
        setupVmWithSerialNumber(SerialNumberPolicy.CUSTOM, CUSTOM_VM_SERIAL);
        assertSerialNumber(CUSTOM_VM_SERIAL);
    }

    @Test
    @MockedConfig("mockConfigForSerial")
    public void testClusterAppliedBeforeConfig() {
        setupClusterWithSerialNumber(SerialNumberPolicy.VM_ID, null);
        assertSerialNumber(VM_ID.toString());
    }

    public static Stream<MockConfigDescriptor<?>> mockConfigForSerial() {
        return mockConfigWithSerialNumber(SerialNumberPolicy.CUSTOM, CUSTOM_CONFIG_SERIAL);
    }

    private void assertSerialNumber(String serialNumber) {
        assertEquals(serialNumber, vmSerialNumberBuilder.buildVmSerialNumber(vm));
    }

    private void setupVmWithSerialNumber(SerialNumberPolicy serialNumberPolicy, String customSerialNumber) {
        vm.setSerialNumberPolicy(serialNumberPolicy);
        vm.setCustomSerialNumber(customSerialNumber);
    }

    private void setupClusterWithSerialNumber(SerialNumberPolicy serialNumberPolicy, String customSerialNumber) {
        cluster.setSerialNumberPolicy(serialNumberPolicy);
        cluster.setCustomSerialNumber(customSerialNumber);
    }

    private static Stream<MockConfigDescriptor<?>> mockConfigWithSerialNumber
            (SerialNumberPolicy serialNumberPolicy, String customSerialNumber) {
        return Stream.of(
                MockConfigDescriptor.of(ConfigValues.DefaultSerialNumberPolicy, serialNumberPolicy),
                MockConfigDescriptor.of(ConfigValues.DefaultCustomSerialNumber, customSerialNumber)
        );
    }
}

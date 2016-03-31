package org.ovirt.engine.core.vdsbroker.vdsbroker;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.SerialNumberPolicy;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.MockConfigRule;
import org.ovirt.engine.core.compat.Guid;

@RunWith(MockitoJUnitRunner.class)
public class VmSerialNumberBuilderTest {

    private static final Guid VM_ID = Guid.newGuid();
    private static final String CUSTOM_VM_SERIAL = "custom VM serial";
    private static final String CUSTOM_CUSTER_SERIAL = "custom CLUSTER serial";
    private static final String CUSTOM_CONFIG_SERIAL = "custom CONFIG serial";

    VM vm;
    Cluster cluster;
    Map<String, Object> creationInfo;

    @Rule
    public MockConfigRule mockConfigRule = new MockConfigRule();

    @Before
    public void setUp() {
        vm = new VM();
        vm.setId(VM_ID);

        cluster = new Cluster();
        creationInfo = new HashMap<>();
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
    public void testConfigHostPolicy() {
        setupConfigWithSerialNumber(SerialNumberPolicy.HOST_ID, null);
        assertSerialNumber(null);
    }

    @Test
    public void testConfigVmIdPolicy() {
        setupConfigWithSerialNumber(SerialNumberPolicy.VM_ID, null);
        assertSerialNumber(VM_ID.toString());
    }

    @Test
    public void testConfigCustomPolicy() {
        setupConfigWithSerialNumber(SerialNumberPolicy.CUSTOM, CUSTOM_CONFIG_SERIAL);
        assertSerialNumber(CUSTOM_CONFIG_SERIAL);
    }

    @Test
    public void testVmAppliedBeforeCluster() {
        setupVmWithSerialNumber(SerialNumberPolicy.HOST_ID, null);
        setupClusterWithSerialNumber(SerialNumberPolicy.VM_ID, null);
        assertSerialNumber(null);
    }

    @Test
    public void testVmAppliedBeforeConfig() {
        setupVmWithSerialNumber(SerialNumberPolicy.CUSTOM, CUSTOM_VM_SERIAL);
        setupConfigWithSerialNumber(SerialNumberPolicy.CUSTOM, CUSTOM_CONFIG_SERIAL);
        assertSerialNumber(CUSTOM_VM_SERIAL);
    }

    @Test
    public void testClusterAppliedBeforeConfig() {
        setupClusterWithSerialNumber(SerialNumberPolicy.VM_ID, null);
        setupConfigWithSerialNumber(SerialNumberPolicy.CUSTOM, CUSTOM_CONFIG_SERIAL);
        assertSerialNumber(VM_ID.toString());
    }

    private void assertSerialNumber(String serialNumber) {
        new VmSerialNumberBuilder(vm, cluster, creationInfo).buildVmSerialNumber();

        assertEquals(serialNumber, creationInfo.get(VdsProperties.SERIAL_NUMBER));
    }

    private void setupVmWithSerialNumber(SerialNumberPolicy serialNumberPolicy, String customSerialNumber) {
        vm.setSerialNumberPolicy(serialNumberPolicy);
        vm.setCustomSerialNumber(customSerialNumber);
    }

    private void setupClusterWithSerialNumber(SerialNumberPolicy serialNumberPolicy, String customSerialNumber) {
        cluster.setSerialNumberPolicy(serialNumberPolicy);
        cluster.setCustomSerialNumber(customSerialNumber);
    }

    private void setupConfigWithSerialNumber(SerialNumberPolicy serialNumberPolicy, String customSerialNumber) {
        mockConfigRule.mockConfigValue(ConfigValues.DefaultSerialNumberPolicy, serialNumberPolicy);
        mockConfigRule.mockConfigValue(ConfigValues.DefaultCustomSerialNumber, customSerialNumber);
    }
}

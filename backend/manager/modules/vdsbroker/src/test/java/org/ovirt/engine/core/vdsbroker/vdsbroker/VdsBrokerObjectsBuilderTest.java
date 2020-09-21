package org.ovirt.engine.core.vdsbroker.vdsbroker;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.LeaseStatus;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VmStatistics;
import org.ovirt.engine.core.common.businessentities.network.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.storage.DiskImageDynamic;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.serialization.json.JsonObjectDeserializer;

public class VdsBrokerObjectsBuilderTest {
    private VdsBrokerObjectsBuilder vdsBrokerObjectsBuilder = new VdsBrokerObjectsBuilder();

    private static final long SIZE_FOR_DISK_STATS = 100L;
    private static final Guid IMAGE_ID = Guid.createGuidFromString("ed185868-3f9e-4040-a340-e1a64726ebc0");
    private static final Guid VM_ID = Guid.createGuidFromString("71ca53fb-c223-4b31-926d-de1c2ab0b0a9");
    private static final String DEFAULT_VALUE = "0.00";

    @Test
    public void testDisksUsages() {
        Object[] disksUsages = initDisksUsageData();
        Map<String, Object> map = setDisksUsage(disksUsages);
        validateDisksUsagesList(getVmStatistics(), disksUsages, map);
    }

    @Test
    public void testEmptyDisksUsages() {
        Object[] disksUsages = new Object[0];
        Map<String, Object> xml = setDisksUsage(disksUsages);
        validateDisksUsagesList(getVmStatistics(), disksUsages, xml);
    }

    @Test
    public void testDisksUsagesWithEmptyEntry() {
        Object[] disksUsages = initDisksUsageData();
        disksUsages[1] = new HashMap<>();
        Map<String, Object> xml = setDisksUsage(disksUsages);
        validateDisksUsagesList(getVmStatistics(), disksUsages, xml);
    }

    @Test
    public void testDisksUsagesWithNullEntry() {
        Object[] disksUsages = initDisksUsageData();
        disksUsages[1] = null;
        Map<String, Object> xml = setDisksUsage(disksUsages);
        validateDisksUsagesList(getVmStatistics(), disksUsages, xml);
    }

    @Test
    public void testNullDisksUsages() {
        VmStatistics vmStatistics = getVmStatistics();
        Map<String, Object> xml = setDisksUsage(null);
        vdsBrokerObjectsBuilder.updateVMStatisticsData(vmStatistics, xml);
        assertNull(vmStatistics.getDisksUsage());
    }

    @Test
    public void testFlushLatency() {
        String doubleValue = "1";
        Map<String, Object> diskData = setDiskData();
        diskData.put(VdsProperties.vm_disk_flush_latency, doubleValue);
        Map<String, Object> xml = setMockForTesting(diskData);
        List<DiskImageDynamic> disks = vdsBrokerObjectsBuilder.buildVmDiskStatistics(xml);
        assertEquals(new Double("0.000000001"), disks.get(0).getFlushLatency());
    }

    @Test
    public void testReadLatency() {
        String doubleValue = "2";
        Map<String, Object> diskData = setDiskData();
        diskData.put(VdsProperties.vm_disk_read_latency, doubleValue);
        Map<String, Object> xml = setMockForTesting(diskData);
        List<DiskImageDynamic> disks = vdsBrokerObjectsBuilder.buildVmDiskStatistics(xml);
        assertEquals(new Double("0.000000002"), disks.get(0).getReadLatency());
    }

    @Test
    public void testWriteLatency() {
        String doubleValue = "3";
        Map<String, Object> diskData = setDiskData();
        diskData.put(VdsProperties.vm_disk_write_latency, doubleValue);
        Map<String, Object> xml = setMockForTesting(diskData);
        List<DiskImageDynamic> disks = vdsBrokerObjectsBuilder.buildVmDiskStatistics(xml);
        assertEquals(new Double("0.000000003"), disks.get(0).getWriteLatency());
    }

    @Test
    public void testOneSecondLatency() {
        String doubleValue = "1000000000";
        Map<String, Object> diskData = setDiskData();
        diskData.put(VdsProperties.vm_disk_write_latency, doubleValue);
        Map<String, Object> xml = setMockForTesting(diskData);
        List<DiskImageDynamic> disks = vdsBrokerObjectsBuilder.buildVmDiskStatistics(xml);
        assertEquals(new Double("1"), disks.get(0).getWriteLatency());
    }

    @Test
    public void testZeroLatency() {
        String doubleValue = "0";
        Map<String, Object> diskData = setDiskData();
        diskData.put(VdsProperties.vm_disk_write_latency, doubleValue);
        Map<String, Object> xml = setMockForTesting(diskData);
        List<DiskImageDynamic> disks = vdsBrokerObjectsBuilder.buildVmDiskStatistics(xml);
        assertEquals(new Double("0"), disks.get(0).getWriteLatency());
    }

    @Test
    public void testMaximumLatency() {
        String doubleValue = "999999999000000000";
        Map<String, Object> diskData = setDiskData();
        diskData.put(VdsProperties.vm_disk_write_latency, doubleValue);
        Map<String, Object> xml = setMockForTesting(diskData);
        List<DiskImageDynamic> disks = vdsBrokerObjectsBuilder.buildVmDiskStatistics(xml);
        assertEquals(new Double("999999999"), disks.get(0).getWriteLatency());
    }

    @Test
    public void testNullValuesLatency() {
        Map<String, Object> diskData = setDiskData();
        diskData.put(VdsProperties.vm_disk_write_latency, null);
        diskData.put(VdsProperties.vm_disk_read_latency, null);
        diskData.put(VdsProperties.vm_disk_flush_latency, null);
        Map<String, Object> xml = setMockForTesting(diskData);
        List<DiskImageDynamic> disks = vdsBrokerObjectsBuilder.buildVmDiskStatistics(xml);
        assertEquals(disks.get(0).getWriteLatency(), new Double(DEFAULT_VALUE));
        assertEquals(disks.get(0).getReadLatency(), new Double(DEFAULT_VALUE));
        assertEquals(disks.get(0).getFlushLatency(), new Double(DEFAULT_VALUE));
    }

    @Test
    public void testWhenVDSMNotSendingFields() {
        Map<String, Object> diskData = new HashMap<>();
        diskData.put(VdsProperties.vm_disk_read_rate, DEFAULT_VALUE);
        diskData.put(VdsProperties.vm_disk_read_ops, DEFAULT_VALUE);
        diskData.put(VdsProperties.ImageId, IMAGE_ID.toString());
        diskData.put(VdsProperties.vm_disk_write_rate, DEFAULT_VALUE);
        diskData.put(VdsProperties.vm_disk_write_ops, DEFAULT_VALUE);

        // Set the default values to the fields.
        diskData.put(VdsProperties.vm_disk_flush_latency, DEFAULT_VALUE);

        Map<String, Object> xml = setMockForTesting(diskData);
        List<DiskImageDynamic> disks = vdsBrokerObjectsBuilder.buildVmDiskStatistics(xml);
        assertNull(disks.get(0).getWriteLatency());
        assertNull(disks.get(0).getReadLatency());
        assertEquals(disks.get(0).getFlushLatency(), new Double(DEFAULT_VALUE));
    }

    @Test
    public void testDiskStats() {
        Map<String, Object> disksStats = new HashMap<>();
        Map<String, Object> disk = new HashMap<>();
        disk.put(VdsProperties.DISK_STATS_FREE, SIZE_FOR_DISK_STATS);

        disksStats.put("a", disk);
        disksStats.put("b", disk);
        Map<String, Object> xml = setDisksStats(disksStats);

        validateDisksStatsList(getVds(), xml, false);
    }

    @Test
    public void testEmptyDiskStats() {
        Map<String, Object> disksStats = new HashMap<>();
        Map<String, Object> xml = setDisksStats(disksStats);

        validateDisksStatsList(getVds(), xml, false);
    }

    @Test
    public void testNoDiskStats() {
        VDS vds = getVds();
        vdsBrokerObjectsBuilder.updateLocalDisksUsage(vds, new HashMap<>());

        assertNull(vds.getLocalDisksUsage());
    }

    @Test
    public void testNoDiskStatsDataForDisks() {
        Map<String, Object> disksStats = new HashMap<>();
        Map<String, Object> disk = new HashMap<>();

        disksStats.put("a", disk);
        disksStats.put("b", disk);
        Map<String, Object> xml = setDisksStats(disksStats);

        validateDisksStatsList(getVds(), xml, true);
    }

    @Test
    public void testAddNicWithId(){
        String nicId = Guid.newGuid().toString();
        Map<String, Object> vmStruct = createNicDeviceStruct(nicId);
        validateVmNetworkInterfaceId(nicId, vmStruct);
    }

    @Test
    public void testAddNicWithNullId(){
        String nicId = null;
        Map<String, Object> vmStruct = createNicDeviceStruct(nicId);
        validateVmNetworkInterfaceId(nicId, vmStruct);
    }

    private Map<String, Object> createNicDeviceStruct(String nicId) {
        Map<String, Object> device = new HashMap<>();
        device.put(VdsProperties.DeviceId, nicId);
        device.put(VdsProperties.Type, VdsProperties.VM_INTERFACE_DEVICE_TYPE);
        device.put(VdsProperties.NIC_TYPE, VmInterfaceType.e1000.getInternalName());

        Object[] devices = new Object[1];
        devices[0] = device;

        Map<String, Object> vmStruct = new HashMap<>();
        vmStruct.put(VdsProperties.Devices, devices);
        return vmStruct;
    }

    private void validateVmNetworkInterfaceId(String nicId, Map<String, Object> vmStruct) {
        List<VmNetworkInterface> vmNetworkInterfaceList = vdsBrokerObjectsBuilder.buildVmNetworkInterfacesFromDevices(vmStruct);
        assertNotNull(vmNetworkInterfaceList);
        assertEquals(1, vmNetworkInterfaceList.size());

        VmNetworkInterface vmNetworkInterface = vmNetworkInterfaceList.get(0);
        assertEquals(Guid.createGuidFromString(nicId), vmNetworkInterface.getId());
    }

    private void validateDisksUsagesList(VmStatistics vmStatistics, Object[] disksUsages, Map<String, Object> xml) {
        vdsBrokerObjectsBuilder.updateVMStatisticsData(vmStatistics, xml);
        assertEquals(Arrays.asList(disksUsages),
                new JsonObjectDeserializer().deserializeUnformattedJson(vmStatistics.getDisksUsage(), ArrayList.class));
    }

    private void validateDisksStatsList(VDS vds, Map<String, Object> xml, boolean assertNullValues) {
        vdsBrokerObjectsBuilder.updateLocalDisksUsage(vds, xml);
        assertNotNull(vds.getLocalDisksUsage());

        for (Long usage : vds.getLocalDisksUsage().values()) {
            if (assertNullValues) {
                assertNull(usage);
            } else {
                assertEquals(SIZE_FOR_DISK_STATS, usage.longValue());
            }
        }
    }

    private static Map<String, Object> setMockForTesting(Map<String, Object> diskData) {
        Map<String, Map<String, Object>> disksData = new HashMap<>();
        disksData.put("vda", diskData);
        return setDisks(disksData);
    }

    private static VDS getVds() {
        return new VDS();
    }

    private static VmStatistics getVmStatistics() {
        VmStatistics vmStatistics = new VmStatistics();
        vmStatistics.setId(VM_ID);
        return vmStatistics;
    }

    private static Map<String, Object> setDisks(Map<String, Map<String, Object>> disksData) {
        Map<String, Object> map = new HashMap<>();
        map.put(VdsProperties.vm_disks, disksData);
        return map;
    }

    private static Map<String, Object>  setDisksUsage(Object[] disksUsageData) {
        Map<String, Object> map = new HashMap<>();
        map.put(VdsProperties.VM_DISKS_USAGE, disksUsageData);
        return map;
    }

    private static Map<String, Object> setDisksStats(Object disksStatsData) {
        Map<String, Object> map = new HashMap<>();
        map.put(VdsProperties.DISK_STATS, disksStatsData);
        return map;
    }

    private static Map<String, Object> setDiskData() {
        Map<String, Object> diskData = new HashMap<>();
        diskData.put(VdsProperties.vm_disk_read_rate, DEFAULT_VALUE);
        diskData.put(VdsProperties.vm_disk_read_ops, DEFAULT_VALUE);
        diskData.put(VdsProperties.ImageId, IMAGE_ID.toString());
        diskData.put(VdsProperties.vm_disk_write_rate, DEFAULT_VALUE);
        diskData.put(VdsProperties.vm_disk_write_ops, DEFAULT_VALUE);

        // Set the default values to the fields.
        diskData.put(VdsProperties.vm_disk_flush_latency, DEFAULT_VALUE);
        diskData.put(VdsProperties.vm_disk_write_latency, DEFAULT_VALUE);
        diskData.put(VdsProperties.vm_disk_read_latency, DEFAULT_VALUE);
        return diskData;
    }

    private static Object[] initDisksUsageData() {
        Object[] disksUsage = new Object[2];
        disksUsage[0] = getDiskUsageAsMap("11704201216", "FAT32", "c:\\", "9516027904");
        disksUsage[1] = getDiskUsageAsMap("133543936", "CDFS", "d:\\", "133543936");
        return disksUsage;
    }

    private static Map<String, String> getDiskUsageAsMap(String total, String fs, String path, String used) {
        Map<String, String> diskUsage = new HashMap<>();
        diskUsage.put("total", total);
        diskUsage.put("fs", fs);
        diskUsage.put("path", path);
        diskUsage.put("used", used);
        return diskUsage;
    }

    @Test
    public void leaseFree() {
        assertTrue(getLeaseStatus(new Object[0]).isFree());
    }

    @Test
    public void leaseNotFreeMultipleOwners() {
        Object[] owners = { 1, 2, 3 }; // Currently not expected to happen
        assertFalse(getLeaseStatus(owners).isFree());
    }

    @Test
    public void leaseNotFreeSingleOwner() {
        Object[] owners = { 1 };
        assertFalse(getLeaseStatus(owners).isFree());
    }

    private LeaseStatus getLeaseStatus(Object[] owners) {
        return vdsBrokerObjectsBuilder.buildLeaseStatus(Collections.singletonMap("owners", owners));
    }

    @Test
    public void testExtractIpv6Prefix() {
        assertThat(vdsBrokerObjectsBuilder.extractIpv6Prefix("::/128"), is(128));
    }

    @Test
    public void testExtractIpv6PrefixNull() {
        assertThat(vdsBrokerObjectsBuilder.extractIpv6Prefix(null), nullValue());
    }

    @Test
    public void testExtractIpv6PrefixNoPrefix() {
        assertThat(vdsBrokerObjectsBuilder.extractIpv6Prefix("::"), nullValue());
    }

    @Test
    public void testExtractIpv6PrefixInvalidPrefix() {
        assertThat(vdsBrokerObjectsBuilder.extractIpv6Prefix("::/zzz"), nullValue());
    }

    @Test
    public void testExtractProperIpv6AddressWithPrefix() {
        assertThat(vdsBrokerObjectsBuilder.extractIpv6Address("::/123"), is("::"));
    }

    @Test
    public void testExtractProperIpv6AddressWithTooLongPrefix() {
        assertThat(vdsBrokerObjectsBuilder.extractIpv6Address("::/1234"), is("::/1234"));
    }

    @Test
    public void testExtractProperIpv6AddressWithInvalidPrefix() {
        assertThat(vdsBrokerObjectsBuilder.extractIpv6Address("::/a"), is("::/a"));
    }

    @Test
    public void testExtractProperIpv6AddressWithNoPrefix() {
        assertThat(vdsBrokerObjectsBuilder.extractIpv6Address("::/"), is("::/"));
    }

    @Test
    public void testExtractProperIpv6AddressWithoutSlash() {
        assertThat(vdsBrokerObjectsBuilder.extractIpv6Address("::"), is("::"));
    }

    @Test
    public void testExtractProperIpv6AddressMultipleSlashes() {
        assertThat(vdsBrokerObjectsBuilder.extractIpv6Address(":/:/123"), is(":/:/123"));
    }

    @Test
    public void testExtractIpv6AddressEmptyString() {
        assertThat(vdsBrokerObjectsBuilder.extractIpv6Address(""), nullValue());
    }


    @Test
    public void testExtractIpv6GatewayEmptyString() {
        assertThat(vdsBrokerObjectsBuilder.extractIpv6Gateway(Collections.singletonMap(VdsProperties.IPV6_GLOBAL_GATEWAY, "")), nullValue());
    }

    @Test
    public void testExtractIpv4AddressEmptyString() {
        assertThat(vdsBrokerObjectsBuilder.extractAddress(Collections.singletonMap(VdsProperties.ADDR, "")), nullValue());
    }

    @Test
    public void testExtractIpv4SubnetEmptyString() {
        assertThat(vdsBrokerObjectsBuilder.extractSubnet(Collections.singletonMap(VdsProperties.NETMASK, "")), nullValue());
    }

    @Test
    public void testExtractIpv4GatewayEmptyString() {
        assertThat(vdsBrokerObjectsBuilder.extractGateway(Collections.singletonMap(VdsProperties.GLOBAL_GATEWAY, "")), nullValue());
    }
}

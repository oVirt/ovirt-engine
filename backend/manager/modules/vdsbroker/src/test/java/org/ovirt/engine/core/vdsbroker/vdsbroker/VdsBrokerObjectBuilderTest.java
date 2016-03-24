package org.ovirt.engine.core.vdsbroker.vdsbroker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.VmStatistics;
import org.ovirt.engine.core.common.businessentities.network.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.serialization.json.JsonObjectDeserializer;

public class VdsBrokerObjectBuilderTest {

    private static final long SIZE_FOR_DISK_STATS = 100L;
    private static final Guid IMAGE_ID = Guid.createGuidFromString("ed185868-3f9e-4040-a340-e1a64726ebc0");
    private static final Guid VM_ID = Guid.createGuidFromString("71ca53fb-c223-4b31-926d-de1c2ab0b0a9");
    private static final String DEFAULT_VALUE = "0.00";

    @Test
    public void testDisksUsages() {
        Object[] disksUsages = initDisksUsageData();
        Map<String, Object> map = setDisksUsageInXmlRpc(disksUsages);
        validateDisksUsagesList(getVmStatistics(), disksUsages, map);
    }

    @Test
    public void testEmptyDisksUsages() {
        Object[] disksUsages = new Object[0];
        Map<String, Object> xml = setDisksUsageInXmlRpc(disksUsages);
        validateDisksUsagesList(getVmStatistics(), disksUsages, xml);
    }

    @Test
    public void testDisksUsagesWithEmptyEntry() {
        Object[] disksUsages = initDisksUsageData();
        disksUsages[1] = new HashMap<>();
        Map<String, Object> xml = setDisksUsageInXmlRpc(disksUsages);
        validateDisksUsagesList(getVmStatistics(), disksUsages, xml);
    }

    @Test
    public void testDisksUsagesWithNullEntry() {
        Object[] disksUsages = initDisksUsageData();
        disksUsages[1] = null;
        Map<String, Object> xml = setDisksUsageInXmlRpc(disksUsages);
        validateDisksUsagesList(getVmStatistics(), disksUsages, xml);
    }

    @Test
    public void testNullDisksUsages() {
        VmStatistics vmStatistics = getVmStatistics();
        Map<String, Object> xml = setDisksUsageInXmlRpc(null);
        VdsBrokerObjectsBuilder.updateVMStatisticsData(vmStatistics, xml);
        assertNull(vmStatistics.getDisksUsage());
    }

    @Test
    public void testFlushLatency() {
        String doubleValue = "1";
        VmDynamic vmDynamic = getVmDynamic();
        Map<String, Object> diskData = setDiskData();
        diskData.put(VdsProperties.vm_disk_flush_latency, doubleValue);
        Map<String, Object> xml = setMockForTesting(diskData);
        VdsBrokerObjectsBuilder.updateVMDynamicData(vmDynamic, xml, getVds());
        assertEquals(vmDynamic.getDisks().get(0).getFlushLatency(), new Double("0.000000001"));
    }

    @Test
    public void testReadLatency() {
        String doubleValue = "2";
        VmDynamic vmDynamic = getVmDynamic();
        Map<String, Object> diskData = setDiskData();
        diskData.put(VdsProperties.vm_disk_read_latency, doubleValue);
        Map<String, Object> xml = setMockForTesting(diskData);
        VdsBrokerObjectsBuilder.updateVMDynamicData(vmDynamic, xml, getVds());
        assertEquals(vmDynamic.getDisks().get(0).getReadLatency(), new Double("0.000000002"));
    }

    @Test
    public void testWriteLatency() {
        String doubleValue = "3";
        VmDynamic vmDynamic = getVmDynamic();
        Map<String, Object> diskData = setDiskData();
        diskData.put(VdsProperties.vm_disk_write_latency, doubleValue);
        Map<String, Object> xml = setMockForTesting(diskData);
        VdsBrokerObjectsBuilder.updateVMDynamicData(vmDynamic, xml, getVds());
        assertEquals(vmDynamic.getDisks().get(0).getWriteLatency(), new Double("0.000000003"));
    }

    @Test
    public void testOneSecondLatency() {
        String doubleValue = "1000000000";
        VmDynamic vmDynamic = getVmDynamic();
        Map<String, Object> diskData = setDiskData();
        diskData.put(VdsProperties.vm_disk_write_latency, doubleValue);
        Map<String, Object> xml = setMockForTesting(diskData);
        VdsBrokerObjectsBuilder.updateVMDynamicData(vmDynamic, xml, getVds());
        assertEquals(vmDynamic.getDisks().get(0).getWriteLatency(), new Double("1"));
    }

    @Test
    public void testZeroLatency() {
        String doubleValue = "0";
        VmDynamic vmDynamic = getVmDynamic();
        Map<String, Object> diskData = setDiskData();
        diskData.put(VdsProperties.vm_disk_write_latency, doubleValue);
        Map<String, Object> xml = setMockForTesting(diskData);
        VdsBrokerObjectsBuilder.updateVMDynamicData(vmDynamic, xml, getVds());
        assertEquals(vmDynamic.getDisks().get(0).getWriteLatency(), new Double("0"));
    }

    @Test
    public void testMaximumLatency() {
        String doubleValue = "999999999000000000";
        VmDynamic vmDynamic = getVmDynamic();
        Map<String, Object> diskData = setDiskData();
        diskData.put(VdsProperties.vm_disk_write_latency, doubleValue);
        Map<String, Object> xml = setMockForTesting(diskData);
        VdsBrokerObjectsBuilder.updateVMDynamicData(vmDynamic, xml, getVds());
        assertEquals(vmDynamic.getDisks().get(0).getWriteLatency(), new Double("999999999"));
    }

    @Test
    public void testNullValuesLatency() {
        VmDynamic vmDynamic = getVmDynamic();
        Map<String, Object> diskData = setDiskData();
        diskData.put(VdsProperties.vm_disk_write_latency, null);
        diskData.put(VdsProperties.vm_disk_read_latency, null);
        diskData.put(VdsProperties.vm_disk_flush_latency, null);
        Map<String, Object> xml = setMockForTesting(diskData);
        VdsBrokerObjectsBuilder.updateVMDynamicData(vmDynamic, xml, getVds());
        assertEquals(vmDynamic.getDisks().get(0).getWriteLatency(), new Double(DEFAULT_VALUE));
        assertEquals(vmDynamic.getDisks().get(0).getReadLatency(), new Double(DEFAULT_VALUE));
        assertEquals(vmDynamic.getDisks().get(0).getFlushLatency(), new Double(DEFAULT_VALUE));
    }

    @Test
    public void testWhenVDSMNotSendingFields() {
        VmDynamic vmDynamic = getVmDynamic();
        Map<String, Object> diskData = new HashMap<>();
        diskData.put(VdsProperties.vm_disk_read_rate, DEFAULT_VALUE);
        diskData.put(VdsProperties.ImageId, IMAGE_ID.toString());
        diskData.put(VdsProperties.vm_disk_write_rate, DEFAULT_VALUE);

        // Set the default values to the fields.
        diskData.put(VdsProperties.vm_disk_flush_latency, DEFAULT_VALUE);

        Map<String, Object> xml = setMockForTesting(diskData);
        VdsBrokerObjectsBuilder.updateVMDynamicData(vmDynamic, xml, getVds());
        assertNull(vmDynamic.getDisks().get(0).getWriteLatency());
        assertNull(vmDynamic.getDisks().get(0).getReadLatency());
        assertEquals(vmDynamic.getDisks().get(0).getFlushLatency(), new Double(DEFAULT_VALUE));
    }

    @Test
    public void testDiskStats() {
        Map<String, Object> disksStats = new HashMap<>();
        Map<String, Object> disk = new HashMap<>();
        disk.put(VdsProperties.DISK_STATS_FREE, SIZE_FOR_DISK_STATS);

        disksStats.put("a", disk);
        disksStats.put("b", disk);
        Map<String, Object> xml = setDisksStatsInXmlRpc(disksStats);

        validateDisksStatsList(getVds(), xml, false);
    }

    @Test
    public void testEmptyDiskStats() {
        Map<String, Object> disksStats = new HashMap<>();
        Map<String, Object> xml = setDisksStatsInXmlRpc(disksStats);

        validateDisksStatsList(getVds(), xml, false);
    }

    @Test
    public void testNoDiskStats() {
        VDS vds = getVds();
        VdsBrokerObjectsBuilder.updateLocalDisksUsage(vds, new HashMap<>());

        assertNull(vds.getLocalDisksUsage());
    }

    @Test
    public void testNoDiskStatsDataForDisks() {
        Map<String, Object> disksStats = new HashMap<>();
        Map<String, Object> disk = new HashMap<>();

        disksStats.put("a", disk);
        disksStats.put("b", disk);
        Map<String, Object> xml = setDisksStatsInXmlRpc(disksStats);

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
        List<VmNetworkInterface> vmNetworkInterfaceList = VdsBrokerObjectsBuilder.buildVmNetworkInterfacesFromDevices(vmStruct);
        assertNotNull(vmNetworkInterfaceList);
        assertEquals(1, vmNetworkInterfaceList.size());

        VmNetworkInterface vmNetworkInterface = vmNetworkInterfaceList.get(0);
        assertEquals(Guid.createGuidFromString(nicId), vmNetworkInterface.getId());
    }

    private static void validateDisksUsagesList(VmStatistics vmStatistics, Object[] disksUsages, Map<String, Object> xml) {
        VdsBrokerObjectsBuilder.updateVMStatisticsData(vmStatistics, xml);
        assertEquals(Arrays.asList(disksUsages),
                new JsonObjectDeserializer().deserializeUnformattedJson(vmStatistics.getDisksUsage(), ArrayList.class));
    }

    private static void validateDisksStatsList(VDS vds, Map<String, Object> xml, boolean assertNullValues) {
        VdsBrokerObjectsBuilder.updateLocalDisksUsage(vds, xml);
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
        return setDisksInXmlRpc(disksData);
    }

    private static VDS getVds() {
        return new VDS();
    }

    private static VmStatistics getVmStatistics() {
        VmStatistics vmStatistics = new VmStatistics();
        vmStatistics.setId(VM_ID);
        return vmStatistics;
    }

    private static VmDynamic getVmDynamic() {
        VmDynamic vmDynamic = new VmDynamic();
        vmDynamic.setId(VM_ID);
        return vmDynamic;
    }

    private static Map<String, Object> setDisksInXmlRpc(Map<String, Map<String, Object>> disksData) {
        Map<String, Object> map = new HashMap<>();
        map.put(VdsProperties.vm_disks, disksData);
        return map;
    }

    private static Map<String, Object>  setDisksUsageInXmlRpc(Object[] disksUsageData) {
        Map<String, Object> map = new HashMap<>();
        map.put(VdsProperties.VM_DISKS_USAGE, disksUsageData);
        return map;
    }

    private static Map<String, Object> setDisksStatsInXmlRpc(Object disksStatsData) {
        Map<String, Object> map = new HashMap<>();
        map.put(VdsProperties.DISK_STATS, disksStatsData);
        return map;
    }

    private static Map<String, Object> setDiskData() {
        Map<String, Object> diskData = new HashMap<>();
        diskData.put(VdsProperties.vm_disk_read_rate, DEFAULT_VALUE);
        diskData.put(VdsProperties.ImageId, IMAGE_ID.toString());
        diskData.put(VdsProperties.vm_disk_write_rate, DEFAULT_VALUE);

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
 }

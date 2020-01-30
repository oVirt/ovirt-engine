package org.ovirt.engine.core.vdsbroker.jsonrpc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Future;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.vdsbroker.irsbroker.FileStatsReturn;
import org.ovirt.engine.core.vdsbroker.irsbroker.OneUuidReturn;
import org.ovirt.engine.core.vdsbroker.irsbroker.StoragePoolInfo;
import org.ovirt.engine.core.vdsbroker.irsbroker.StorageStatusReturn;
import org.ovirt.engine.core.vdsbroker.vdsbroker.IQNListReturn;
import org.ovirt.engine.core.vdsbroker.vdsbroker.ServerConnectionStatusReturn;
import org.ovirt.engine.core.vdsbroker.vdsbroker.Status;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VDSInfoReturn;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VMListReturn;
import org.ovirt.vdsm.jsonrpc.client.JsonRpcClient;
import org.ovirt.vdsm.jsonrpc.client.JsonRpcRequest;
import org.ovirt.vdsm.jsonrpc.client.JsonRpcResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

public class MarshallingTestCase {

    @SuppressWarnings("unchecked")
    @Test
    public void testGetCapabilities() throws Exception {
        // Given
        String capabilitiesJson =
                "{\"jsonrpc\": \"2.0\", \"id\": \"aed1feb4-42cf-4bf4-8ddf-852251152b68\", \"result\": {\"HBAInventory\": {\"iSCSI\": "
                        + "[{\"InitiatorName\": \"iqn.1994-05.com.redhat:d990cf85cdeb\"}], \"FC\": []}, \"packages2\": {\"kernel\": {\"release\": \"200.fc19.x86_64\","
                        + " \"buildtime\": 1384356599.0, \"version\": \"3.11.8\"}, \"spice-server\": {\"release\": \"3.fc19\", \"buildtime\": 1383130020, "
                        + "\"version\": \"0.12.4\"}, \"vdsm\": {\"release\": \"163.git9adad51.fc19\", \"buildtime\": 1385064768, \"version\": \"4.13.0\"}, "
                        + "\"qemu-kvm\": {\"release\": \"13.fc19\", \"buildtime\": 1383700301, \"version\": \"1.4.2\"}, \"libvirt\": {\"release\": "
                        + "\"1.fc19\", \"buildtime\": 1383765188, \"version\": \"1.0.5.7\"}, \"qemu-img\": {\"release\": \"13.fc19\", \"buildtime\": "
                        + "1383700301, \"version\": \"1.4.2\"}, \"mom\": {\"release\": \"3.13.giteb3985f.fc19\", \"buildtime\": 1384283536, \"version\""
                        + ": \"0.3.2\"}}, \"cpuModel\": \"Intel(R) Core(TM) i7-3770 CPU @ 3.40GHz\", \"hooks\": {}, \"cpuSockets\": \"1\", \"vmTypes\": "
                        + "[\"kvm\"], \"networks\": {\"ovirtmgmt\": {\"iface\": \"ovirtmgmt\", \"addr\":"
                        + " \"192.168.1.10\", \"cfg\": {\"DEFROUTE\": \"yes\", \"IPADDR\": \"192.168.1.10\", \"GATEWAY\": \"192.168.1.1\", \"DELAY\":"
                        + " \"0\", \"NM_CONTROLLED\": \"no\", \"NETMASK\": \"255.255.255.0\", \"BOOTPROTO\": \"none\", \"STP\": \"no\", \"DEVICE\": "
                        + "\"ovirtmgmt\", \"TYPE\": \"Bridge\", \"ONBOOT\": \"yes\"}, \"ipv6addrs\": [\"fe80::baca:3aff:fea9:77e2/64\"], \"gateway\":"
                        + " \"192.168.1.1\", \"netmask\": \"255.255.255.0\", \"stp\": \"off\", \"bridged\": true, \"qosInbound\": \"\", \"qosOutbound\":"
                        + " \"\", \"mtu\": \"1500\", \"ipv6gateway\": \"::\", \"ports\": [\"em1\"]}}, \"bridges\": {\"ovirtmgmt\": {\"addr\": \"192.168.1.10\","
                        + " \"cfg\": {\"DEFROUTE\": \"yes\", \"IPADDR\": \"192.168.1.10\", \"GATEWAY\": \"192.168.1.1\", \"DELAY\": \"0\", \"NM_CONTROLLED\":"
                        + " \"no\", \"NETMASK\": \"255.255.255.0\", \"BOOTPROTO\": \"none\", \"STP\": \"no\", \"DEVICE\": \"ovirtmgmt\", \"TYPE\": \"Bridge\","
                        + " \"ONBOOT\": \"yes\"}, \"ipv6addrs\": [\"fe80::baca:3aff:fea9:77e2/64\"], \"mtu\": \"1500\", \"netmask\": \"255.255.255.0\", "
                        + "\"stp\": \"off\", \"ipv6gateway\": \"::\", \"gateway\": \"192.168.1.1\", \"ports\": [\"em1\"]}}, \"uuid\": \"4C4C4544-0046-4E10-8032-B2C04F385A31\","
                        + " \"nics\": {\"em1\": {\"netmask\": \"\", \"addr\": \"\", \"hwaddr\": \"b8:ca:3a:a9:77:e2\", \"cfg\": {\"BRIDGE\": \"ovirtmgmt\", \"NM_CONTROLLED\":"
                        + " \"no\", \"HWADDR\": \"b8:ca:3a:a9:77:e2\", \"STP\": \"no\", \"DEVICE\": \"em1\", \"ONBOOT\": \"yes\"}, \"ipv6addrs\": [\"fe80::baca:3aff:fea9:77e2/64\"],"
                        + " \"speed\": 100, \"mtu\": \"1500\"}}, \"software_revision\": \"163\", \"clusterLevels\": [\"3.0\", \"3.1\", \"3.2\", \"3.3\"], \"cpuFlags\": "
                        + "\"fpu,vme,de,pse,tsc,msr,pae,mce,cx8,apic,sep,mtrr,pge,mca,cmov,pat,pse36,clflush,dts,acpi,mmx,fxsr,sse,sse2,ss,ht,tm,pbe,syscall,nx,rdtscp,lm,constant_"
                        + "tsc,arch_perfmon,pebs,bts,rep_good,nopl,xtopology,nonstop_tsc,aperfmperf,eagerfpu,pni,pclmulqdq,dtes64,monitor,ds_cpl,vmx,smx,est,tm2,ssse3,cx16,xtpr,pdcm,"
                        + "pcid,sse4_1,sse4_2,x2apic,popcnt,tsc_deadline_timer,aes,xsave,avx,f16c,rdrand,lahf_lm,ida,arat,epb,xsaveopt,pln,pts,dtherm,tpr_shadow,vnmi,flexpriority,ept,"
                        + "vpid,fsgsbase,smep,erms,model_Nehalem,model_Conroe,model_coreduo,model_core2duo,model_Penryn,model_Westmere,model_n270,model_SandyBridge\", \"ISCSIInitiatorName\""
                        + ": \"iqn.1994-05.com.redhat:d990cf85cdeb\", \"netConfigDirty\": \"False\", \"supportedENGINEs\": [\"3.0\", \"3.1\", \"3.2\", \"3.3\"], \"reservedMem\": \"321\","
                        + " \"bondings\": {\"bond0\": {\"netmask\": \"\", \"addr\": \"\", \"slaves\": [], \"hwaddr\": \"6e:31:40:a3:e3:d7\", \"cfg\": {}, \"ipv6addrs\": [], \"mtu\": \"1500\"}},"
                        + " \"software_version\": \"4.13\", \"memSize\": \"15937\", \"cpuSpeed\": \"3400.000\", \"version_name\": \"Snow Man\", \"vlans\": {}, \"cpuCores\": \"4\", \"kvmEnabled\":"
                        + " \"true\", \"guestOverhead\": \"65\", \"cpuThreads\": \"8\", \"emulatedMachines\": [\"pc\", \"q35\", \"isapc\", \"pc-0.10\", \"pc-0.11\", \"pc-0.12\", \"pc-0.13\", "
                        + "\"pc-0.14\", \"pc-0.15\", \"pc-1.0\", \"pc-1.1\", \"pc-1.2\", \"pc-1.3\", \"none\"], \"operatingSystem\": {\"release\": \"4\", \"version\": \"19\", \"name\": \"Fedora\"}}}";
        ObjectMapper mapper = new ObjectMapper();
        JsonRpcResponse response = JsonRpcResponse.fromJsonNode(mapper.readTree(capabilitiesJson));
        Future<JsonRpcResponse> future = mock(Future.class);
        when(future.get()).thenReturn(response);
        JsonRpcClient client = mock(JsonRpcClient.class);
        JsonRpcRequest request = mock(JsonRpcRequest.class);
        when(client.call(request)).thenReturn(future);

        // When
        Map<String, Object> map = new FutureMap(client, request);

        // Then
        VDSInfoReturn vdsInfo = new VDSInfoReturn(map);
        Status status = vdsInfo.status;
        assertEquals("Done", status.message);
        assertEquals(0, status.code);

        Map<String, Object> info = vdsInfo.info;
        assertTrue(!info.isEmpty());
        Map<String, Object> bonds = (Map<String, Object>) info.get("bondings");
        for (Entry<String, Object> entry : bonds.entrySet()) {
            Map<String, Object> bond = (Map<String, Object>) entry.getValue();
            assertEquals(0, ((Object[]) bond.get("slaves")).length);
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testEmptyListVDS() throws Exception {
        // Given
        String json = "{\"jsonrpc\": \"2.0\", \"id\": \"3a0a4c64-1b67-4b48-bc31-e4e0cb7538b1\", \"result\": []}";
        ObjectMapper mapper = new ObjectMapper();
        JsonRpcResponse response = JsonRpcResponse.fromJsonNode(mapper.readTree(json));
        Future<JsonRpcResponse> future = mock(Future.class);
        when(future.get()).thenReturn(response);
        JsonRpcClient client = mock(JsonRpcClient.class);
        JsonRpcRequest request = mock(JsonRpcRequest.class);
        when(client.call(request)).thenReturn(future);

        // When
        Map<String, Object> map =
                new FutureMap(client, request).withResponseKey("vmList").withResponseType(Object[].class);

        // Then
        VMListReturn vmList = new VMListReturn(map);
        Status status = vmList.status;
        assertEquals("Done", status.message);
        assertEquals(0, status.code);
        assertEquals(0, vmList.vmList.length);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testShortList() throws Exception {
        // Given
        String json =
                "{\"jsonrpc\": \"2.0\", \"id\": \"ae80f5c4-0f63-4c2e-aed6-5372f07a14c1\", \"result\": [\"e4a0fc02-c5ad-4b35-b2d0-5a4b6557c06b\"]}";
        ObjectMapper mapper = new ObjectMapper();
        JsonRpcResponse response = JsonRpcResponse.fromJsonNode(mapper.readTree(json));
        Future<JsonRpcResponse> future = mock(Future.class);
        when(future.get()).thenReturn(response);
        JsonRpcClient client = mock(JsonRpcClient.class);
        JsonRpcRequest request = mock(JsonRpcRequest.class);
        when(client.call(request)).thenReturn(future);

        // When
        Map<String, Object> map =
                new FutureMap(client, request).withResponseKey("vmList")
                        .withResponseType(Object[].class)
                        .withSubTypeClazz(HashMap.class)
                        .withSubtypeKey("vmId");

        // Then
        VMListReturn vmList = new VMListReturn(map);
        Status status = vmList.status;
        assertEquals("Done", status.message);
        assertEquals(0, status.code);
        assertEquals(1, vmList.vmList.length);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testVdsStatsError() throws Exception {
        // Given
        String json =
                "{\"jsonrpc\": \"2.0\", \"id\": \"4d2d6215-3159-4c03-8066-5148cfa09587\", \"error\": {\"message\":"
                        + " \"'NoneType' object has no attribute 'statistics'\", \"code\": -32603}}";
        ObjectMapper mapper = new ObjectMapper();
        JsonRpcResponse response = JsonRpcResponse.fromJsonNode(mapper.readTree(json));
        Future<JsonRpcResponse> future = mock(Future.class);
        when(future.get()).thenReturn(response);
        JsonRpcClient client = mock(JsonRpcClient.class);
        JsonRpcRequest request = mock(JsonRpcRequest.class);
        when(client.call(request)).thenReturn(future);

        // When
        Map<String, Object> map = new FutureMap(client, request);

        // Then
        Map<String, Object> status = (Map<String, Object>) map.get("status");
        assertTrue(!status.isEmpty());
        assertEquals("'NoneType' object has no attribute 'statistics'", status.get("message"));
        assertEquals(-32603, status.get("code"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testAddDomain() throws Exception {
        // Given
        String json =
                "{\"jsonrpc\": \"2.0\", \"id\": \"4b0838b3-f940-4780-b2f0-fd56c1fbc573\", \"result\": [{\"status\": 0, \"id\": \"00000000-0000-0000-0000-000000000000\"}]}";
        ObjectMapper mapper = new ObjectMapper();
        JsonRpcResponse response = JsonRpcResponse.fromJsonNode(mapper.readTree(json));
        Future<JsonRpcResponse> future = mock(Future.class);
        when(future.get()).thenReturn(response);
        JsonRpcClient client = mock(JsonRpcClient.class);
        JsonRpcRequest request = mock(JsonRpcRequest.class);
        when(client.call(request)).thenReturn(future);

        // When
        Map<String, Object> map =
                new FutureMap(client, request).withResponseKey("statuslist").withResponseType(Object[].class);

        // Then
        ServerConnectionStatusReturn status = new ServerConnectionStatusReturn(map);
        assertEquals("Done", status.getStatus().message);
        assertEquals(0, status.getStatus().code);
        assertEquals(1, status.statusList.length);
        Map<String, Object> result = status.statusList[0];
        assertEquals(0, result.get("status"));
        assertEquals("00000000-0000-0000-0000-000000000000", result.get("id"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetIsoListWithoutIsos() throws Exception {
        // Given
        String json = "{\"jsonrpc\": \"2.0\", \"id\": \"c1796b67-8932-4e90-a6f9-aa68266493f8\", \"result\": []}";
        ObjectMapper mapper = new ObjectMapper();
        JsonRpcResponse response = JsonRpcResponse.fromJsonNode(mapper.readTree(json));
        Future<JsonRpcResponse> future = mock(Future.class);
        when(future.get()).thenReturn(response);
        JsonRpcClient client = mock(JsonRpcClient.class);
        JsonRpcRequest request = mock(JsonRpcRequest.class);
        when(client.call(request)).thenReturn(future);

        // When
        Map<String, Object> map =
                new FutureMap(client, request).withResponseKey("iso_list").withResponseType(Object[].class);

        // Then
        FileStatsReturn isoList = new FileStatsReturn(map);
        assertEquals("Done", isoList.getStatus().message);
        assertEquals(0, isoList.getStatus().code);
        assertEquals(0, isoList.getFileStats().size());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetIsoListWithImage() throws Exception {
        // Given
        String json =
                "{\"jsonrpc\": \"2.0\", \"id\": \"8d38c4c9-3fdb-4663-993a-dc65488875bb\", \"result\": [\"Fedora-Live-Desktop-x86_64-19-1.iso\"]}";
        ObjectMapper mapper = new ObjectMapper();
        JsonRpcResponse response = JsonRpcResponse.fromJsonNode(mapper.readTree(json));
        Future<JsonRpcResponse> future = mock(Future.class);
        when(future.get()).thenReturn(response);
        JsonRpcClient client = mock(JsonRpcClient.class);
        JsonRpcRequest request = mock(JsonRpcRequest.class);
        when(client.call(request)).thenReturn(future);

        // When
        Map<String, Object> map =
                new FutureMap(client, request).withResponseKey("isolist").withResponseType(Object[].class);

        // Then
        FileStatsReturn isoList = new FileStatsReturn(map);
        assertEquals("Done", isoList.getStatus().message);
        assertEquals(0, isoList.getStatus().code);
        assertEquals(1, isoList.getFileStats().size());
        assertEquals("Fedora-Live-Desktop-x86_64-19-1.iso", isoList.getFileStats().keySet().iterator().next());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetVMList() throws Exception {
        // Given
        String json =
                "{\"jsonrpc\": \"2.0\", \"id\": \"e32621e5-753f-45b8-b071-2eb929408efd\", \"result\": [{\"status\": \"Up\", \"vmId\": \"dd4d61c3-5128-4c26-ae71-0dbe5081ea93\"}]}";
        ObjectMapper mapper = new ObjectMapper();
        JsonRpcResponse response = JsonRpcResponse.fromJsonNode(mapper.readTree(json));
        Future<JsonRpcResponse> future = mock(Future.class);
        when(future.get()).thenReturn(response);
        JsonRpcClient client = mock(JsonRpcClient.class);
        JsonRpcRequest request = mock(JsonRpcRequest.class);
        when(client.call(request)).thenReturn(future);

        // When
        Map<String, Object> map = new FutureMap(client, request).withResponseKey("vmList")
                .withResponseType(Object[].class);

        // Then
        VMListReturn vmList = new VMListReturn(map);
        assertEquals("Done", vmList.status.message);
        assertEquals(0, vmList.status.code);
        assertEquals(1, vmList.vmList.length);
        assertEquals("dd4d61c3-5128-4c26-ae71-0dbe5081ea93", vmList.vmList[0].get("vmId"));
        assertEquals("Up", vmList.vmList[0].get("status"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetVMListIdsOnly() throws Exception {
        // Given
        String json =
                "{\"jsonrpc\": \"2.0\", \"id\": \"e9968b53-7450-4059-83e6-d3569f7024ec\", \"result\": [\"1397d80b-1c48-4d4a-acf9-ebd669bf3b25\"]}";
        ObjectMapper mapper = new ObjectMapper();
        JsonRpcResponse response = JsonRpcResponse.fromJsonNode(mapper.readTree(json));
        Future<JsonRpcResponse> future = mock(Future.class);
        when(future.get()).thenReturn(response);
        JsonRpcClient client = mock(JsonRpcClient.class);
        JsonRpcRequest request = mock(JsonRpcRequest.class);
        when(client.call(request)).thenReturn(future);

        // When
        Map<String, Object> map = new FutureMap(client, request).withResponseKey("vmList")
                .withResponseType(Object[].class);

        // Then
        VMListReturn vmList = new VMListReturn(map);
        assertEquals("Done", vmList.status.message);
        assertEquals(0, vmList.status.code);
        assertEquals(1, vmList.vmList.length);
        assertEquals("1397d80b-1c48-4d4a-acf9-ebd669bf3b25", vmList.vmList[0].get("vmId"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testStoragePoolInfo() throws Exception {
        // Given
        String json =
                "{\"jsonrpc\": \"2.0\", \"id\": \"609b5787-ab3a-485b-8ed8-0bc9a27eda27\", \"result\": {\"info\": {\"spm_id\": 1, \"master_uuid\": \"05a0ad59-1259-4353-b40b-34eb80d8590a\","
                        + " \"name\": \"Default\", \"version\": \"0\", \"domains\": \"05a0ad59-1259-4353-b40b-34eb80d8590a:Active,6ca00a0d-3f1d-4762-b5ff-c58a6a0a0324:Active,4192b643-fae9-4c1c-8b8b-9c9c6cc10523:Active\", "
                        + "\"pool_status\": \"connected\", \"isoprefix\": \"/rhev/data-center/mnt/192.168.1.10:_export_iso/6ca00a0d-3f1d-4762-b5ff-c58a6a0a0324/images/11111111-1111-1111-1111-111111111111\", "
                        + "\"type\": \"NFS\", \"master_ver\": 1, \"lver\": 0}, \"dominfo\": {\"05a0ad59-1259-4353-b40b-34eb80d8590a\": {\"status\": \"Active\", \"diskfree\": \"43887099904\", \"isoprefix\": \"\","
                        + " \"alerts\": [], \"disktotal\": \"52710866944\", \"version\": 0}, \"6ca00a0d-3f1d-4762-b5ff-c58a6a0a0324\": {\"status\": \"Active\", \"diskfree\": \"43887099904\", \"isoprefix\": "
                        + "\"/rhev/data-center/mnt/192.168.1.10:_export_iso/6ca00a0d-3f1d-4762-b5ff-c58a6a0a0324/images/11111111-1111-1111-1111-111111111111\", \"alerts\": [], \"disktotal\": \"52710866944\", "
                        + "\"version\": 0}, \"4192b643-fae9-4c1c-8b8b-9c9c6cc10523\": {\"status\": \"Active\", \"isoprefix\": \"\", \"alerts\": [], \"version\": -1}}}}";
        ObjectMapper mapper = new ObjectMapper();
        JsonRpcResponse response = JsonRpcResponse.fromJsonNode(mapper.readTree(json));
        Future<JsonRpcResponse> future = mock(Future.class);
        when(future.get()).thenReturn(response);
        JsonRpcClient client = mock(JsonRpcClient.class);
        JsonRpcRequest request = mock(JsonRpcRequest.class);
        when(client.call(request)).thenReturn(future);

        // When
        Map<String, Object> map = new FutureMap(client, request).withIgnoreResponseKey();

        // Then
        StoragePoolInfo storagePoolInfo = new StoragePoolInfo(map);
        assertEquals("Done", storagePoolInfo.getStatus().message);
        assertEquals(0, storagePoolInfo.getStatus().code);
        Set<String> keys = storagePoolInfo.domainsList.keySet();
        assertEquals(3, keys.size());
        assertTrue(keys.contains("05a0ad59-1259-4353-b40b-34eb80d8590a"));
        assertTrue(keys.contains("6ca00a0d-3f1d-4762-b5ff-c58a6a0a0324"));
        assertTrue(keys.contains("4192b643-fae9-4c1c-8b8b-9c9c6cc10523"));
        Map<String, Object> info = storagePoolInfo.storagePoolInfo;
        assertEquals("/rhev/data-center/mnt/192.168.1.10:_export_iso/6ca00a0d-3f1d-4762-b5ff-c58a6a0a0324/images/11111111-1111-1111-1111-111111111111",
                info.get("isoprefix"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testActivateDomain() throws Exception {
        // Given
        String json = "{\"jsonrpc\": \"2.0\", \"id\": \"5ba8294b-afd7-4810-968d-607703a7bd93\", \"result\": true}";
        ObjectMapper mapper = new ObjectMapper();
        JsonRpcResponse response = JsonRpcResponse.fromJsonNode(mapper.readTree(json));
        Future<JsonRpcResponse> future = mock(Future.class);
        when(future.get()).thenReturn(response);
        JsonRpcClient client = mock(JsonRpcClient.class);
        JsonRpcRequest request = mock(JsonRpcRequest.class);
        when(client.call(request)).thenReturn(future);

        // When
        Map<String, Object> map = new FutureMap(client, request).withResponseKey("storageStatus")
                .withResponseType(String.class);

        // Then
        StorageStatusReturn storageStatus = new StorageStatusReturn(map);
        assertEquals("Done", storageStatus.getStatus().message);
        assertEquals(0, storageStatus.getStatus().code);
        assertEquals("true", storageStatus.storageStatus);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testVolumeCreate() throws Exception {
        // Given
        String json =
                "{\"jsonrpc\": \"2.0\", \"id\": \"e8ea1fa9-d819-4c41-ae9c-b103a236fb29\", \"result\": {\"uuid\": \"4f84eef5-8f8b-4732-babd-0a860cf0d1b9\"}}";
        ObjectMapper mapper = new ObjectMapper();
        JsonRpcResponse response = JsonRpcResponse.fromJsonNode(mapper.readTree(json));
        Future<JsonRpcResponse> future = mock(Future.class);
        when(future.get()).thenReturn(response);
        JsonRpcClient client = mock(JsonRpcClient.class);
        JsonRpcRequest request = mock(JsonRpcRequest.class);
        when(client.call(request)).thenReturn(future);

        // When
        Map<String, Object> map = new FutureMap(client, request).withIgnoreResponseKey();

        // Then
        OneUuidReturn oneuuid = new OneUuidReturn(map);
        assertEquals("Done", oneuuid.getStatus().message);
        assertEquals(0, oneuuid.getStatus().code);
        assertEquals("4f84eef5-8f8b-4732-babd-0a860cf0d1b9", UUID.fromString(oneuuid.uuid).toString());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testDiscoverSendTargets() throws Exception {
        // Given
        String json =
                "{\"jsonrpc\": \"2.0\", \"id\": \"9afac443-3473-454d-b6c7-80f0b7876ff7\", \"result\": [\"10.35.16.25:3260,1 iqn.1994-05.com.redhat.com:ahadas-iscsi\","
                        + " \"10.35.16.25:3260,1 iqn.1994-05.com.redhat.com:sgotliv-iscsi\", \"10.35.16.25:3260,1 iqn.1994-05.com.redhat.com:sgotliv-iscsi2\", "
                        + "\"10.35.16.25:3260,1 iqn.1994-05.com.redhat:nsoffer-target1\", \"10.35.16.25:3260,1 iqn.1994-05.com.redhat:nsoffer-target2\","
                        + " \"10.35.16.25:3260,1 iqn.1994-05.com.redhat:pkliczew\", \"10.35.16.25:3260,1 iqn.1994-05.com.redhat:ybronhei-target\", \"10.35.16.25:3260,1 iqn.2011-05.com.redhat:achub-iscsi\","
                        + " \"10.35.16.25:3260,1 iqn.2011-05.com.redhat:ashakarc-iscsi\", \"10.35.16.25:3260,1 iqn.2011-05.com.redhat:derez-iscsi\","
                        + " \"10.35.16.25:3260,1 iqn.2011-05.com.redhat:drankevi-iscsi\", \"10.35.16.25:3260,1 iqn.2011-05.com.redhat:ecohen-iscsi\","
                        + " \"10.35.16.25:3260,1 iqn.2011-05.com.redhat:gchaplik-iscsi\", \"10.35.16.25:3260,1 iqn.2011-05.com.redhat:mkolesnik-iscsi\","
                        + " \"10.35.16.25:3260,1 iqn.2011-05.com.redhat:mpastern-iscsi\", \"10.35.16.25:3260,1 iqn.2011-05.com.redhat:oliel-iscsi\","
                        + " \"10.35.16.25:3260,1 iqn.2011-05.com.redhat:oliel-iscsi2\", \"10.35.16.25:3260,1 iqn.2011-05.com.redhat:tnisan-iscsi\","
                        + " \"10.35.16.25:3260,1 iqn.2011-06.com.redhat:jchoate-iscsi\", \"10.35.16.25:3260,1 iqn.2011-06.com.redhat:msalem-iscsi\","
                        + " \"10.35.16.25:3260,1 iqn.2011-06.com.redhat:ofrenkel-iscsi\", \"10.35.16.25:3260,1 iqn.2011-08.com.redhat:mlipchuk-iscsi\","
                        + " \"10.35.16.25:3260,1 iqn.2011-08.com.redhat:oourfali1\", \"10.35.16.25:3260,1 iqn.2011-08.com.redhat:shavivi1\", \"10.35.16.25:3260,1 iqn.2011-08.com.redhat:shavivi2\","
                        + " \"10.35.16.25:3260,1 iqn.2011-09.com.redhat:masayag-iscsi\", \"10.35.16.25:3260,1 iqn.2012-01.com.redhat:storage-negative-test1\", \"10.35.16.25:3260,1 iqn.2012-03.com.redhat:rami-api\","
                        + " \"10.35.16.25:3260,1 iqn.2012-04.com.redhat:rhevm-em1\", \"10.35.16.25:3260,1 iqn.2012-06.com.redhat:rgolan1\", \"10.35.16.25:3260,1 iqn.2012-06.com.redhat:tnisan1\","
                        + " \"10.35.16.25:3260,1 iqn.2012-09.com.redhat.com:omasad1\", \"10.35.16.25:3260,1 iqn.2012-12.com.redhat.com:rnori\", \"10.35.16.25:3260,1 iqn.2013-01.redhat.com:vered1\","
                        + " \"10.35.16.25:3260,1 iqn.2013-04.redhat.com:abonas-target1\", \"10.35.16.25:3260,1 iqn.2013-04.redhat.com:abonas-target2\", \"10.35.16.25:3260,1 iqn.2013-04.redhat.com:mtayer1\"]}";

        ObjectMapper mapper = new ObjectMapper();
        JsonRpcResponse response = JsonRpcResponse.fromJsonNode(mapper.readTree(json));
        Future<JsonRpcResponse> future = mock(Future.class);
        when(future.get()).thenReturn(response);
        JsonRpcClient client = mock(JsonRpcClient.class);
        JsonRpcRequest request = mock(JsonRpcRequest.class);
        when(client.call(request)).thenReturn(future);

        // When
        Map<String, Object> map = new FutureMap(client, request).withResponseKey("fullTargets");
        IQNListReturn list = new IQNListReturn(map);
        assertEquals("Done", list.getStatus().message);
        assertEquals(0, list.getStatus().code);
        assertEquals(37, parseFullTargets(list.getIqnList()).size());
    }

    // copied from DiscoverSendTargetsVDSCommand
    private List<StorageServerConnections> parseFullTargets(List<String> iqnList) {
        ArrayList<StorageServerConnections> connections = new ArrayList<>(iqnList.size());
        for (String fullTarget : iqnList) {
            StorageServerConnections con = new StorageServerConnections();
            String[] tokens = fullTarget.split(",");
            String[] address = tokens[0].split(":");
            String[] literals = tokens[1].split(" ");

            con.setConnection(address[0]);
            con.setPort(address[1]);
            con.setPortal(literals[0]);
            con.setIqn(literals[1]);
            connections.add(con);
        }

        return connections;
    }
}

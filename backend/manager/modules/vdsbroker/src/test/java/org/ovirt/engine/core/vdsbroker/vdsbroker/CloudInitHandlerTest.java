package org.ovirt.engine.core.vdsbroker.vdsbroker;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.stringContainsInOrder;

import java.io.IOException;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.VmInit;
import org.ovirt.engine.core.common.businessentities.VmInitNetwork;
import org.ovirt.engine.core.common.businessentities.network.CloudInitNetworkProtocol;
import org.ovirt.engine.core.common.businessentities.network.Ipv4BootProtocol;
import org.ovirt.engine.core.utils.JsonHelper;

public class CloudInitHandlerTest {

    private static final String IFACE_NAME = "iface name";
    private static final String IPV4_ADDRESS = "ipv4 address";
    private static final String IPV4_NETMASK = "ipv4 netmask";
    private static final String IPV4_GATEWAY = "ipv4 gateway";
    //    private static final String IPV6_ADDRESS = "ipv6 address";
    //    private static final int IPV6_PREFIX = 666;
    //    private static final String IPV6_GATEWAY = "ipv6 gateway";
    private static final String DNS_SERVERS = "dns servers";
    private static final String DNS_SEARCH = "dns search";

    protected VmInit vmInit;
    private CloudInitHandler underTest;

    @BeforeEach
    public void setUp() throws IOException {
        vmInit = new VmInit();
        vmInit.setCloudInitNetworkProtocol(CloudInitNetworkProtocol.ENI);
        underTest = new CloudInitHandler(vmInit);
    }

    @Test
    public void testGetFileDataStoreNoNetwork() throws IOException {
        final Map<String, byte[]> actual = underTest.getFileData();

        final Map<String, Object> actualMetaData = parseResult(actual);
        assertThat(actualMetaData, not(hasKey("network_config")));
        assertThat(actualMetaData, not(hasKey("network-interfaces")));
        assertOpenstackNetworkDataFileNotCreated(actual);
    }

    @Test
    public void testGetFileDataStoreNetwork() throws IOException {
        vmInit.setNetworks(singletonList(createVmInitNetwork()));

        final Map<String, byte[]> actual = underTest.getFileData();

        final Map<String, Object> actualMetaData = parseResult(actual);
        assertNetworkConfig(actualMetaData);
        assertNetworkInterfaces(actualMetaData);
        assertOpenstackNetworkDataFileNotCreated(actual);
    }

    @Test
    public void testGetFileDataStoreDns() throws IOException {
        vmInit.setNetworks(singletonList(createVmInitNetwork()));
        vmInit.setDnsServers(DNS_SERVERS);
        vmInit.setDnsSearch(DNS_SEARCH);

        final Map<String, byte[]> actual = underTest.getFileData();

        final Map<String, Object> actualMetaData = parseResult(actual);
        assertDnsProperties(actualMetaData);
        assertOpenstackNetworkDataFileNotCreated(actual);
    }

    private void assertDnsProperties(Map<String, Object> actualMetaData) {
        assertThat(actualMetaData, hasKey("network-interfaces"));
        final String networkInterfaces = (String) actualMetaData.get("network-interfaces");
        assertThat(networkInterfaces, stringContainsInOrder(asList(
                "iface " + IFACE_NAME + " inet static\n",
                "  dns-nameservers " + DNS_SERVERS + "\n",
                "  dns-search " + DNS_SEARCH + "\n"
                // "iface " + IFACE_NAME + " inet6 static\n"
        )));
    }

    private void assertNetworkConfig(Map<String, Object> metaData) {
        @SuppressWarnings("unchecked")
        final Map<String, String> networkConfig = (Map<String, String>) metaData.get("network_config");
        assertThat(networkConfig, hasEntry("path", "/etc/network/interfaces"));
        assertThat(networkConfig, hasEntry("content_path", "/content/0000"));
    }

    private void assertNetworkInterfaces(Map<String, Object> actualMetaData) {
        assertThat(actualMetaData, hasKey("network-interfaces"));
        final String networkInterfaces = (String) actualMetaData.get("network-interfaces");
        assertThat(networkInterfaces, stringContainsInOrder(asList(
                "iface " + IFACE_NAME + " inet static\n",
                "  address " + IPV4_ADDRESS + "\n",
                "  netmask " + IPV4_NETMASK + "\n",
                "  gateway " + IPV4_GATEWAY + "\n"
                // "iface " + IFACE_NAME + " inet6 static\n",
                // "  address " + IPV6_ADDRESS + "\n",
                // "  netmask " + IPV6_PREFIX + "\n",
                // "  gateway " + IPV6_GATEWAY + "\n"
        )));
    }

    private Map<String, Object> parseResult(Map<String, byte[]> actual) throws IOException {
        final byte[] actualJson = actual.get("openstack/latest/meta_data.json");
        final String jsonString = new String(actualJson, "US-ASCII");
        return JsonHelper.jsonToMap(jsonString);
    }

    private VmInitNetwork createVmInitNetwork() {
        final VmInitNetwork vmInitNetwork = new VmInitNetwork();
        vmInitNetwork.setName(IFACE_NAME);
        vmInitNetwork.setBootProtocol(Ipv4BootProtocol.STATIC_IP);
        vmInitNetwork.setIp(IPV4_ADDRESS);
        vmInitNetwork.setNetmask(IPV4_NETMASK);
        vmInitNetwork.setGateway(IPV4_GATEWAY);
        // vmInitNetwork.setIpv6BootProtocol(Ipv6BootProtocol.STATIC_IP);
        // vmInitNetwork.setIpv6Address(IPV6_ADDRESS);
        // vmInitNetwork.setIpv6Prefix(IPV6_PREFIX);
        // vmInitNetwork.setIpv6Gateway(IPV6_GATEWAY);
        return vmInitNetwork;
    }

    private void assertOpenstackNetworkDataFileNotCreated(Map<String, byte[]> actual) {
        assertThat(actual, not(hasKey("openstack/latest/network_data.json")));
    }
}

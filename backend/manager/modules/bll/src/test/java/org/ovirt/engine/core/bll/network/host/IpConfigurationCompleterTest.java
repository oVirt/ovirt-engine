package org.ovirt.engine.core.bll.network.host;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;

import java.util.Collections;
import java.util.Set;

import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.network.IPv4Address;
import org.ovirt.engine.core.common.businessentities.network.IpConfiguration;
import org.ovirt.engine.core.common.businessentities.network.IpV6Address;
import org.ovirt.engine.core.common.businessentities.network.Ipv4BootProtocol;
import org.ovirt.engine.core.common.businessentities.network.Ipv6BootProtocol;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;

public class IpConfigurationCompleterTest {

    private static final String IPV4_ADDRESS = "ipv4 address";
    private static final String IPV4_MASK = "ipv4 mask";
    private static final String IPV4_GATEWAY = "ipv4 gateway";
    private static final String IPV6_ADDRESS = "ipv6 address";
    private static final Integer IPV6_PREFIX = 666;
    private static final String IPV6_GATEWAY = "ipv6 gateway";

    private NetworkAttachment networkAttachment;
    private IpConfiguration ipConfiguration;
    private IpConfigurationCompleter underTest;
    private Set<NetworkAttachment> networkAttachments;

    @BeforeEach
    public void setUp() {
        underTest = new IpConfigurationCompleter();
        ipConfiguration = new IpConfiguration();
        networkAttachment = new NetworkAttachment();
        networkAttachment.setIpConfiguration(ipConfiguration);
        networkAttachments = Collections.singleton(networkAttachment);
    }

    @Test
    public void testFillInUnsetIpConfigsBoth() {
        underTest.fillInUnsetIpConfigs(networkAttachments);
        assertIpv4DefaultValues(networkAttachment);
        assertIpv6DefaultValues(networkAttachment);
    }

    @Test
    public void testFillInUnsetIpConfigsIpv4() {
        final IpV6Address ipv6Address = createIpv6Address();
        ipConfiguration.setIpV6Addresses(Collections.singletonList(ipv6Address));
        underTest.fillInUnsetIpConfigs(networkAttachments);
        assertIpv4DefaultValues(networkAttachment);
        assertIpv6Values(networkAttachment,
                is(Ipv6BootProtocol.AUTOCONF),
                is(IPV6_ADDRESS),
                is(IPV6_PREFIX),
                is(IPV6_GATEWAY));
    }

    @Test
    public void testFillInUnsetIpConfigsIpv6() {
        final IPv4Address ipv4Address = createIpv4Address();
        ipConfiguration.setIPv4Addresses(Collections.singletonList(ipv4Address));
        underTest.fillInUnsetIpConfigs(networkAttachments);
        assertIpv6DefaultValues(networkAttachment);
        assertIpv4Values(networkAttachment,
                is(Ipv4BootProtocol.DHCP),
                is(IPV4_ADDRESS),
                is(IPV4_MASK),
                is(IPV4_GATEWAY));
    }

    @Test
    public void testFillInUnsetIpConfigsMissingIpConfig() {
        networkAttachment.setIpConfiguration(null);
        underTest.fillInUnsetIpConfigs(networkAttachments);
        assertIpv4DefaultValues(networkAttachment);
        assertIpv6DefaultValues(networkAttachment);
    }

    private IPv4Address createIpv4Address() {
        final IPv4Address iPv4Address = new IPv4Address();
        iPv4Address.setBootProtocol(Ipv4BootProtocol.DHCP);
        iPv4Address.setAddress(IPV4_ADDRESS);
        iPv4Address.setNetmask(IPV4_MASK);
        iPv4Address.setGateway(IPV4_GATEWAY);
        return iPv4Address;
    }

    private IpV6Address createIpv6Address() {
        final IpV6Address ipv6Address = new IpV6Address();
        ipv6Address.setBootProtocol(Ipv6BootProtocol.AUTOCONF);
        ipv6Address.setAddress(IPV6_ADDRESS);
        ipv6Address.setPrefix(IPV6_PREFIX);
        ipv6Address.setGateway(IPV6_GATEWAY);
        return ipv6Address;
    }

    private void assertIpv4DefaultValues(NetworkAttachment networkAttachment) {
        assertIpv4Values(networkAttachment,
                is(Ipv4BootProtocol.NONE),
                nullValue(String.class),
                nullValue(String.class),
                nullValue(String.class));
    }

    private void assertIpv6DefaultValues(NetworkAttachment networkAttachment) {
        assertIpv6Values(networkAttachment,
                is(Ipv6BootProtocol.NONE),
                nullValue(String.class),
                nullValue(Integer.class),
                nullValue(String.class));
    }

    private void assertIpv6Values(
            NetworkAttachment networkAttachment,
            Matcher<Ipv6BootProtocol> bootProtocolMatcher,
            Matcher<String> addressMatcher,
            Matcher<Integer> prefixMatcher,
            Matcher<String> gatewayMatcher) {
        final IpConfiguration ipConfiguration = networkAttachment.getIpConfiguration();
        assertThat(ipConfiguration, notNullValue());
        assertThat(ipConfiguration.getIpV6Addresses(), hasSize(1));
        assertThat(ipConfiguration.getIpv6PrimaryAddress().getBootProtocol(), bootProtocolMatcher);
        assertThat(ipConfiguration.getIpv6PrimaryAddress().getAddress(), addressMatcher);
        assertThat(ipConfiguration.getIpv6PrimaryAddress().getPrefix(), prefixMatcher);
        assertThat(ipConfiguration.getIpv6PrimaryAddress().getGateway(), gatewayMatcher);
    }

    private void assertIpv4Values(NetworkAttachment networkAttachment,
            Matcher<Ipv4BootProtocol> bootProtocolMatcher,
            Matcher<String> addressMatcher,
            Matcher<String> prefixMatcher,
            Matcher<String> gatewayMatcher) {
        final IpConfiguration ipConfiguration = networkAttachment.getIpConfiguration();
        assertThat(ipConfiguration, notNullValue());
        assertThat(ipConfiguration.getIPv4Addresses(), hasSize(1));
        assertThat(ipConfiguration.getIpv4PrimaryAddress().getBootProtocol(), bootProtocolMatcher);
        assertThat(ipConfiguration.getIpv4PrimaryAddress().getAddress(), addressMatcher);
        assertThat(ipConfiguration.getIpv4PrimaryAddress().getNetmask(), prefixMatcher);
        assertThat(ipConfiguration.getIpv4PrimaryAddress().getGateway(), gatewayMatcher);
    }

}

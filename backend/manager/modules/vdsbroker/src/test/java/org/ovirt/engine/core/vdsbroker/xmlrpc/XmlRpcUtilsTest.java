package org.ovirt.engine.core.vdsbroker.xmlrpc;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.ovirt.engine.core.common.utils.MockConfigRule.mockConfig;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.ClassRule;
import org.junit.Test;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.MockConfigRule;
import org.ovirt.engine.core.common.utils.Pair;

public class XmlRpcUtilsTest {

    private static final String HOSTNAME = "hostname";
    private static final int PORT = 1234;
    private static final String PATH = "path";
    private static final String IPV6_ADDRESS = "1:2::3:4";
    private static final String IPV4_ADDRESS = "1.2.3.4";

    @ClassRule
    public static MockConfigRule mcr = new MockConfigRule(mockConfig(ConfigValues.EncryptHostCommunication, false));

    @Test
    public void testGetConnectionUrl1() throws MalformedURLException {
        final Pair<String, URL> actual = XmlRpcUtils.getConnectionUrl(HOSTNAME, PORT, PATH, true);

        final String expectedUrl = String.format("https://%s:%d/%s", HOSTNAME, PORT, PATH);

        assertThat(actual.getFirst(), is(expectedUrl));
        assertThat(actual.getSecond(), is(new URL(expectedUrl)));
    }

    @Test
    public void testGetConnectionUrl2() throws MalformedURLException {
        final Pair<String, URL> actual = XmlRpcUtils.getConnectionUrl(HOSTNAME, PORT, null, false);

        final String expectedUrl = String.format("http://%s:%d", HOSTNAME, PORT);

        assertThat(actual.getFirst(), is(expectedUrl));
        assertThat(actual.getSecond(), is(new URL(expectedUrl)));
    }

    @Test
    public void testGetConnectionUrlWithIpv4Address() throws MalformedURLException {
        final Pair<String, URL> actual = XmlRpcUtils.getConnectionUrl(IPV4_ADDRESS, PORT, null, false);

        final String expectedUrl = String.format("http://%s:%d", IPV4_ADDRESS, PORT);

        assertThat(actual.getFirst(), is(expectedUrl));
        assertThat(actual.getSecond(), is(new URL(expectedUrl)));
    }

    @Test
    public void testGetConnectionUrlWithIpv6Address() throws MalformedURLException {
        final Pair<String, URL> actual = XmlRpcUtils.getConnectionUrl(IPV6_ADDRESS, PORT, null, false);

        final String expectedUrl = String.format("http://[%s]:%d", IPV6_ADDRESS, PORT);

        assertThat(actual.getFirst(), is(expectedUrl));
        assertThat(actual.getSecond(), is(new URL(expectedUrl)));
    }

    @Test
    public void testGetConnectionUrlWithWrappedIpv6Address() throws MalformedURLException {
        final String wrappedIpv6Address = String.format("[%s]", IPV6_ADDRESS);
        final Pair<String, URL> actual = XmlRpcUtils.getConnectionUrl(wrappedIpv6Address, PORT, null, true);

        final String expectedUrl = String.format("https://[%s]:%d", IPV6_ADDRESS, PORT);

        assertThat(actual.getFirst(), is(expectedUrl));
        assertThat(actual.getSecond(), is(new URL(expectedUrl)));
    }
}

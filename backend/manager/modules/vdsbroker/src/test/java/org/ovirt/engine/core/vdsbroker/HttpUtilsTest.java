package org.ovirt.engine.core.vdsbroker;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.utils.MockConfigDescriptor;
import org.ovirt.engine.core.utils.MockConfigExtension;

@ExtendWith(MockConfigExtension.class)
public class HttpUtilsTest {

    private static final String HOSTNAME = "hostname";
    private static final int PORT = 1234;
    private static final String PATH = "path";
    private static final String IPV6_ADDRESS = "1:2::3:4";
    private static final String IPV4_ADDRESS = "1.2.3.4";

    public static Stream<MockConfigDescriptor<?>> mockConfiguration() {
        return Stream.of(MockConfigDescriptor.of(ConfigValues.EncryptHostCommunication, false));
    }

    @Test
    public void testGetConnectionUrl1() throws MalformedURLException {
        final Pair<String, URL> actual = HttpUtils.getConnectionUrl(HOSTNAME, PORT, PATH, true);

        final String expectedUrl = String.format("https://%s:%d/%s", HOSTNAME, PORT, PATH);

        assertThat(actual.getFirst(), is(expectedUrl));
        assertThat(actual.getSecond(), is(new URL(expectedUrl)));
    }

    @Test
    public void testGetConnectionUrl2() throws MalformedURLException {
        final Pair<String, URL> actual = HttpUtils.getConnectionUrl(HOSTNAME, PORT, null, false);

        final String expectedUrl = String.format("http://%s:%d", HOSTNAME, PORT);

        assertThat(actual.getFirst(), is(expectedUrl));
        assertThat(actual.getSecond(), is(new URL(expectedUrl)));
    }

    @Test
    public void testGetConnectionUrlWithIpv4Address() throws MalformedURLException {
        final Pair<String, URL> actual = HttpUtils.getConnectionUrl(IPV4_ADDRESS, PORT, null, false);

        final String expectedUrl = String.format("http://%s:%d", IPV4_ADDRESS, PORT);

        assertThat(actual.getFirst(), is(expectedUrl));
        assertThat(actual.getSecond(), is(new URL(expectedUrl)));
    }

    @Test
    public void testGetConnectionUrlWithIpv6Address() throws MalformedURLException {
        final Pair<String, URL> actual = HttpUtils.getConnectionUrl(IPV6_ADDRESS, PORT, null, false);

        final String expectedUrl = String.format("http://[%s]:%d", IPV6_ADDRESS, PORT);

        assertThat(actual.getFirst(), is(expectedUrl));
        assertThat(actual.getSecond(), is(new URL(expectedUrl)));
    }

    @Test
    public void testGetConnectionUrlWithWrappedIpv6Address() throws MalformedURLException {
        final String wrappedIpv6Address = String.format("[%s]", IPV6_ADDRESS);
        final Pair<String, URL> actual = HttpUtils.getConnectionUrl(wrappedIpv6Address, PORT, null, true);

        final String expectedUrl = String.format("https://[%s]:%d", IPV6_ADDRESS, PORT);

        assertThat(actual.getFirst(), is(expectedUrl));
        assertThat(actual.getSecond(), is(new URL(expectedUrl)));
    }
}

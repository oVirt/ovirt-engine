package org.ovirt.engine.core.vdsbroker.vdsbroker;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class VdsBrokerObjectsBuilderTest {
    @Test
    public void testExtractIpv6Prefix() {
        assertThat(VdsBrokerObjectsBuilder.extractIpv6Prefix("::/128"), is(128));
    }

    @Test
    public void testExtractIpv6PrefixNull() {
        assertThat(VdsBrokerObjectsBuilder.extractIpv6Prefix(null), nullValue());
    }

    @Test
    public void testExtractIpv6PrefixNoPrefix() {
        assertThat(VdsBrokerObjectsBuilder.extractIpv6Prefix("::"), nullValue());
    }

    @Test
    public void testExtractIpv6PrefixInvalidPrefix() {
        assertThat(VdsBrokerObjectsBuilder.extractIpv6Prefix("::/zzz"), nullValue());
    }

    @Test
    public void testExtractProperIpv6AddressWithPrefix() {
        assertThat(VdsBrokerObjectsBuilder.extractIpv6Address("::/123"), is("::"));
    }

    @Test
    public void testExtractProperIpv6AddressWithTooLongPrefix() {
        assertThat(VdsBrokerObjectsBuilder.extractIpv6Address("::/1234"), is("::/1234"));
    }

    @Test
    public void testExtractProperIpv6AddressWithInvalidPrefix() {
        assertThat(VdsBrokerObjectsBuilder.extractIpv6Address("::/a"), is("::/a"));
    }

    @Test
    public void testExtractProperIpv6AddressWithNoPrefix() {
        assertThat(VdsBrokerObjectsBuilder.extractIpv6Address("::/"), is("::/"));
    }

    @Test
    public void testExtractProperIpv6AddressWithoutSlash() {
        assertThat(VdsBrokerObjectsBuilder.extractIpv6Address("::"), is("::"));
    }

    @Test
    public void testExtractProperIpv6AddressMultipleSlashes() {
        assertThat(VdsBrokerObjectsBuilder.extractIpv6Address(":/:/123"), is(":/:/123"));
    }
}

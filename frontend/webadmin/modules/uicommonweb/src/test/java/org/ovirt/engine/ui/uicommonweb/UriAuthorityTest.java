package org.ovirt.engine.ui.uicommonweb;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

public class UriAuthorityTest {
    private static final String IPV6_ADDRESS = "[an:IPv6:address]"; //$NON-NLS-1$
    private static final String NON_IPV6_ADDRESS = "a non-IPv6 address or FQDN"; //$NON-NLS-1$
    private static final String PORT = "666";  //$NON-NLS-1$
    private static final String EMPTY_PORT = "";  //$NON-NLS-1$
    private static final String TOO_BIG_PORT = "123456";  //$NON-NLS-1$
    private static final String COLON = ":";  //$NON-NLS-1$

    @Test
    public void testIsValidNonIpv6Host() {
        final UriAuthority underTest = new UriAuthority(NON_IPV6_ADDRESS + COLON + PORT);
        assertThat(underTest.isValid(), is(true));
        assertThat(underTest.getHost(), is(NON_IPV6_ADDRESS));
        assertThat(underTest.getPort(), is(PORT));
    }

    @Test
    public void testIsValidIpv6Host() {
        final UriAuthority underTest = new UriAuthority(IPV6_ADDRESS + COLON + PORT);
        assertThat(underTest.isValid(), is(true));
        assertThat(underTest.getHost(), is(IPV6_ADDRESS));
        assertThat(underTest.getPort(), is(PORT));
    }

    @Test
    public void testEmptyPort() {
        final UriAuthority underTest = new UriAuthority(IPV6_ADDRESS + COLON + EMPTY_PORT);
        assertThat(underTest.isValid(), is(false));
    }

    @Test
    public void testTooBigPort() {
        final UriAuthority underTest = new UriAuthority(IPV6_ADDRESS + COLON + TOO_BIG_PORT);
        assertThat(underTest.isValid(), is(false));
    }

}

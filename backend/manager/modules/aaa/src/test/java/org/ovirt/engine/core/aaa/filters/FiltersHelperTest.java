package org.ovirt.engine.core.aaa.filters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.Test;

public class FiltersHelperTest {

    /**
     * Check that the persistent authentication preference is recognized when there are more than one {@code Prefer}
     * headers.
     */
    @Test
    public void testPersistentAuthWithSeveralHeaders() {
        assertEquals(FiltersHelper.PREFER_PERSISTENCE_AUTH, getPrefer("persistent-auth", "x", "y"));
        assertEquals(FiltersHelper.PREFER_PERSISTENCE_AUTH, getPrefer("x", "persistent-auth", "y"));
        assertEquals(FiltersHelper.PREFER_PERSISTENCE_AUTH, getPrefer("x", "y", "persistent-auth"));
    }

    /**
     * Check that the persistent authentication preference is recognized regardless of case.
     */
    @Test
    public void testPreferIgnoresCase() {
        assertEquals(FiltersHelper.PREFER_PERSISTENCE_AUTH, getPrefer("Persistent-Auth"));
        assertEquals(FiltersHelper.PREFER_PERSISTENCE_AUTH, getPrefer("PERSISTENT-AUTH"));
        assertEquals(FiltersHelper.PREFER_NEW_AUTH, getPrefer("new-auth"));
        assertEquals(FiltersHelper.PREFER_NEW_AUTH, getPrefer("NEW-AUTH"));
    }

    /**
     * Check that the persistent authentication preference is recognized when there are other preferences in the same
     * header.
     */
    @Test
    public void testPersistentAuthOtherPreferencesInSameHeader() {
        assertEquals(FiltersHelper.PREFER_PERSISTENCE_AUTH, getPrefer("persistent-auth, x, y"));
        assertEquals(FiltersHelper.PREFER_PERSISTENCE_AUTH, getPrefer("x, persistent-auth, y"));
        assertEquals(FiltersHelper.PREFER_PERSISTENCE_AUTH, getPrefer("x, y, persistent-auth"));
    }

    /**
     * Check that the persistent authentication preference is recognized even it is has a value (the value should be
     * ignored).
     */
    @Test
    public void testPreferWithValue() {
        assertEquals(FiltersHelper.PREFER_PERSISTENCE_AUTH, getPrefer("persistent-auth=false"));
        assertEquals(FiltersHelper.PREFER_PERSISTENCE_AUTH, getPrefer("persistent-auth=true"));
        assertEquals(FiltersHelper.PREFER_PERSISTENCE_AUTH, getPrefer("persistent-auth=junk"));
        assertEquals(FiltersHelper.PREFER_NEW_AUTH, getPrefer("new-auth=false"));
        assertEquals(FiltersHelper.PREFER_PERSISTENCE_AUTH | FiltersHelper.PREFER_NEW_AUTH, getPrefer("persistent-auth=false, new-auth=false"));
    }

    /**
     * Check that the persistent authentication preference is recognized even it is has parameters (the parameters
     * should be ignored).
     */
    @Test
    public void testPreferParameters() {
        assertEquals(FiltersHelper.PREFER_PERSISTENCE_AUTH, getPrefer("persistent-auth; x=0; y=0"));
        assertEquals(FiltersHelper.PREFER_PERSISTENCE_AUTH | FiltersHelper.PREFER_NEW_AUTH, getPrefer("persistent-auth, new-auth; x=0; y=0"));
    }

    /**
     * Check that the persistent authentication isn't enabled if the preference isn't present.
     */
    @Test
    public void testPersistentAuthDisabled() {
        assertEquals(0, getPrefer());
        assertEquals(0, getPrefer("x", "y"));
        assertEquals(0, getPrefer("x", "y"));
    }

    /**
     * Check address gets resolved to ipv6 address properly
     */
    @Test
    public void testGetRedirectUriServerName() {
        String address = "fe80::56ee:75ff:fe5c:6cc7";
        assertEquals(String.format("[%s]", address), FiltersHelper.getRedirectUriServerName(address));
    }

    /**
     * Check localhost resolves to correct Redirect Uri Server Name
     */
    @Test
    public void testLocalHostGetRedirectUriServerName() {
        String address = "localhost";
        assertEquals(address, FiltersHelper.getRedirectUriServerName(address));
    }

    /**
     * Check ip address resolves to correct Redirect Uri Server Name
     */
    @Test
    public void testIpGetRedirectUriServerName() {
        String address = "192.168.1.134";
        assertEquals(address, FiltersHelper.getRedirectUriServerName(address));
    }

    /**
     * This method constructs a mocked HTTP request, populates it with values for the {@code Prefer} header, and then
     * calls the method that checks if persistent authentication is enabled. It is intended to simplify other tests.
     *
     * @param values the values of the {@code Prefer} header
     */
    private int getPrefer(String... values) {
        // Create a vector containing the values of the header:
        Vector<String> vector = new Vector<>();
        Collections.addAll(vector, values);

        // Create the mocked request:
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeaders(FiltersHelper.Constants.HEADER_PREFER)).thenReturn(vector.elements());

        // Call the method that checks for persistent authentication:
        return FiltersHelper.getPrefer(request);
    }

}

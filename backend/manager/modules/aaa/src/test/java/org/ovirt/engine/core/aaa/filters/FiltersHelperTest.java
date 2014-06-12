package org.ovirt.engine.core.aaa.filters;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Vector;
import javax.servlet.http.HttpServletRequest;

import org.junit.Test;

public class FiltersHelperTest {

    /**
     * Check that the persistent authentication preference is recognized when there are more than one {@code Prefer}
     * headers.
     */
    @Test
    public void testPersistentAuthWithSeveralHeaders() {
        assertTrue(isPersistentAuth("persistent-auth", "x", "y"));
        assertTrue(isPersistentAuth("x", "persistent-auth", "y"));
        assertTrue(isPersistentAuth("x", "y", "persistent-auth"));
    }

    /**
     * Check that the persistent authentication preference is recognized regardless of case.
     */
    @Test
    public void testPersistentAuthIgnoresCase() {
        assertTrue(isPersistentAuth("Persistent-Auth"));
        assertTrue(isPersistentAuth("PERSISTENT-AUTH"));
    }

    /**
     * Check that the persistent authentication preference is recognized when there are other preferences in the same
     * header.
     */
    @Test
    public void testPersistentAuthOtherPreferencesInSameHeader() {
        assertTrue(isPersistentAuth("persistent-auth, x, y"));
        assertTrue(isPersistentAuth("x, persistent-auth, y"));
        assertTrue(isPersistentAuth("x, y, persistent-auth"));
    }

    /**
     * Check that the persistent authentication preference is recognized even it is has a value (the value should be
     * ignored).
     */
    @Test
    public void testPersistentAuthWithValue() {
        assertTrue(isPersistentAuth("persistent-auth=false"));
        assertTrue(isPersistentAuth("persistent-auth=true"));
        assertTrue(isPersistentAuth("persistent-auth=junk"));
    }

    /**
     * Check that the persistent authentication preference is recognized even it is has parameters (the parameters
     * should be ignored).
     */
    @Test
    public void testPersistentAuthWithParameters() {
        assertTrue(isPersistentAuth("persistent-auth; x=0; y=0"));
    }

    /**
     * Check that the persistent authentication isn't enabled if the preference isn't present.
     */
    @Test
    public void testPersistentAuthDisabled() {
        assertFalse(isPersistentAuth());
        assertFalse(isPersistentAuth("x", "y"));
        assertFalse(isPersistentAuth("x", "y"));
    }

    /**
     * This method constructs a mocked HTTP request, populates it with values for the {@cod Prefer} header, and then
     * calls the method that checks if persistent authentication is enabled. It is intended to simplify other tests.
     *
     * @param values the values of the {@code Prefer} header
     */
    private boolean isPersistentAuth(String... values) {
        // Create a vector containing the values of the header:
        Vector<String> vector = new Vector<>();
        Collections.addAll(vector, values);

        // Create the mocked request:
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeaders(FiltersHelper.Constants.HEADER_PREFER)).thenReturn(vector.elements());

        // Call the method that checks for persistent authentication:
        return FiltersHelper.isPersistentAuth(request);
    }

}

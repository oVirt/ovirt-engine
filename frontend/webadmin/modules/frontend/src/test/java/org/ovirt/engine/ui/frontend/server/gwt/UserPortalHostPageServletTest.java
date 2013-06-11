package org.ovirt.engine.ui.frontend.server.gwt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.utils.branding.BrandingTheme;

@RunWith(MockitoJUnitRunner.class)
public class UserPortalHostPageServletTest extends AbstractGwtDynamicHostPageServletTest<UserPortalHostPageServlet> {

    @Override
    protected UserPortalHostPageServlet getTestServletSpy() {
        return spy(new UserPortalHostPageServlet());
    }

    @Test
    public void testGetSelectorScriptName() {
        assertEquals(testServlet.getSelectorScriptName(),
                "org.ovirt.engine.ui.userportal.UserPortal.nocache.js"); //$NON-NLS-1$
    }

    @Test
    public void testFilterQueries() {
        assertTrue("Filter queries should be 'true'", testServlet.filterQueries()); //$NON-NLS-1$
    }

    @Test
    public void testGetApplicationType() {
        assertEquals("Application type should be 'USER_PORTAL'", //$NON-NLS-1$
                testServlet.getApplicationType(), BrandingTheme.ApplicationType.USER_PORTAL);
    }
}

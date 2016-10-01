package org.ovirt.engine.ui.frontend.server.gwt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.utils.MockConfigRule;

@RunWith(MockitoJUnitRunner.class)
public class UserPortalHostPageServletTest extends AbstractGwtDynamicHostPageServletTest<UserPortalHostPageServlet> {
    @ClassRule
    public static MockConfigRule mcr = new MockConfigRule();

    @Override
    protected UserPortalHostPageServlet getTestServletSpy() {
        return spy(new UserPortalHostPageServlet());
    }

    @Test
    public void testGetSelectorScriptName() {
        assertEquals("userportal.nocache.js", testServlet.getSelectorScriptName()); //$NON-NLS-1$
    }

    @Test
    public void testFilterQueries() {
        assertTrue("Filter queries should be 'true'", testServlet.filterQueries()); //$NON-NLS-1$
    }
}

package org.ovirt.engine.ui.frontend.server.gwt.branding;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Locale;
import java.util.ResourceBundle;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for the {@code BrandingTheme} class.
 */
public class BrandingThemeTest {
    /**
     * The testTheme object.
     */
    BrandingTheme testTheme;

    @Before
    public void setUp() throws Exception {
        File testThemeRootPath = new File(this.getClass().getClassLoader().
            getResource("./org/ovirt/engine/ui/frontend/server/gwt/branding") //$NON-NLS-1$
            .getFile());
        File testThemePath = new File(testThemeRootPath.getAbsoluteFile(), "01-test.brand"); //$NON-NLS-1$
        testTheme = new BrandingTheme(testThemePath.getAbsolutePath(),
                testThemeRootPath, 1);
        assertTrue("The theme should load", testTheme.load()); //$NON-NLS-1$
    }

    @Test
    public void testGetPath() {
        assertEquals("Path should be '/01-test.brand'", "/01-test.brand", //$NON-NLS-1$ //$NON-NLS-2$
                testTheme.getPath());
    }

    @Test
    public void testGetThemeStyleSheet() {
        assertEquals("User portal style sheet: 'user_portal.css'", "user_portal.css", //$NON-NLS-1$ //$NON-NLS-2$
                testTheme.getThemeStyleSheet(BrandingTheme.ApplicationType.USER_PORTAL));
        assertEquals("Wedadmin style sheet: 'web_admin.css'", "web_admin.css", //$NON-NLS-1$ //$NON-NLS-2$
                testTheme.getThemeStyleSheet(BrandingTheme.ApplicationType.WEBADMIN));
    }

    @Test
    public void testGetMessagesBundle() {
        ResourceBundle bundle = testTheme.getMessagesBundle();
        assertNotNull("There should be a bundle", bundle); //$NON-NLS-1$
        assertEquals("Login header", bundle.getString("obrand.common.login_header_label")); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Test
    public void testGetMessagesBundleLocale() {
        ResourceBundle bundle = testTheme.getMessagesBundle(Locale.FRENCH);
        assertNotNull("There should be a bundle", bundle); //$NON-NLS-1$
        assertEquals("Login header(fr)", bundle.getString("common.login_header_label")); //$NON-NLS-1$ //$NON-NLS-2$
    }
}

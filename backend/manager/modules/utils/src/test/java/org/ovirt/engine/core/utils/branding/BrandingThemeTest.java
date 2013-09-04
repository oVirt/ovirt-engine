package org.ovirt.engine.core.utils.branding;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;
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
            getResource("./org/ovirt/engine/core/utils/branding") //$NON-NLS-1$
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
        List<ResourceBundle> bundle = testTheme.getMessagesBundle();
        assertNotNull("There should be a bundle", bundle); //$NON-NLS-1$
        assertEquals("Login header", bundle.get(0).getString("obrand.common.login_header_label")); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Test
    public void testGetMessagesBundleLocale() {
        List<ResourceBundle> bundle = testTheme.getMessagesBundle(Locale.FRENCH);
        assertNotNull("There should be a bundle", bundle); //$NON-NLS-1$
        assertEquals("Login header(fr)", bundle.get(0).getString("obrand.common.login_header_label")); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Test
    public void testGetCascadingResource() {
        assertTrue("getCascadingResource not reading file from resources.properties", //$NON-NLS-1$
                testTheme.getCascadingResource("favicon").getFile().getAbsolutePath().contains("/01-test.brand/images/favicon.ico")); //$NON-NLS-1$ //$NON-NLS-2$
        assertTrue("getCascadingResource not reading contentType from resources.properties", //$NON-NLS-1$
                testTheme.getCascadingResource("favicon").getContentType().equals("someMadeUp/contentType")); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Test
    public void testGetCascadingResource_missingKey() {
        assertNull("getCascadingResource not using resources.properties properly", //$NON-NLS-1$
                testTheme.getCascadingResource("this_is_not_a_valid_key")); //$NON-NLS-1$
    }

    @Test
    public void testGetCascadingResource_missingResourcesFile() {
        File testThemeRootPath = new File(this.getClass().getClassLoader().
                getResource("./org/ovirt/engine/core/utils/branding") //$NON-NLS-1$
                .getFile());
        // theme 4 is purposely missing a resources.properties file
        File testThemePath = new File(testThemeRootPath.getAbsoluteFile(), "04-test4.brand"); //$NON-NLS-1$
        BrandingTheme theme4 = new BrandingTheme(testThemePath.getAbsolutePath(),
                testThemeRootPath, 1);
        assertTrue("Theme 4 should load", theme4.load()); //$NON-NLS-1$

        assertNull("getCascadingResource not handling missing resources.properties gracefully", //$NON-NLS-1$
                theme4.getCascadingResource("this_file_is_missing_anyway")); //$NON-NLS-1$
    }

    @Test
    public void testGetCascadingResource_missingResourcesProperty() {
        File testThemeRootPath = new File(this.getClass().getClassLoader().
                getResource("./org/ovirt/engine/core/utils/branding") //$NON-NLS-1$
                .getFile());
        // theme 5 is purposely missing a resources key in branding.properties
        File testThemePath = new File(testThemeRootPath.getAbsoluteFile(), "05-test5.brand"); //$NON-NLS-1$
        BrandingTheme theme5 = new BrandingTheme(testThemePath.getAbsolutePath(),
                testThemeRootPath, 1);
        assertTrue("Theme 5 should load", theme5.load()); //$NON-NLS-1$

        assertNull("getCascadingResource not handling missing resources key gracefully", //$NON-NLS-1$
                theme5.getCascadingResource("this_file_is_missing_anyway")); //$NON-NLS-1$
    }

}

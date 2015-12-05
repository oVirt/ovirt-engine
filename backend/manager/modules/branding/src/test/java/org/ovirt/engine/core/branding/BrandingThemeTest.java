package org.ovirt.engine.core.branding;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URISyntaxException;
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
            getResource("./org/ovirt/engine/core/branding").toURI().getPath()); //$NON-NLS-1$
        File testThemePath = new File(testThemeRootPath.getAbsoluteFile(), "01-test.brand"); //$NON-NLS-1$
        testTheme = new BrandingTheme(testThemePath.getAbsolutePath(),
                testThemeRootPath, 2);
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
                testTheme.getThemeStylesheets("userportal").get(0));
        assertEquals("User portal style sheet: 'abc.css'", "abc.css", //$NON-NLS-1$ //$NON-NLS-2$
                testTheme.getThemeStylesheets("userportal").get(1));

        assertEquals("Wedadmin style sheet: 'web_admin.css'", "web_admin.css", //$NON-NLS-1$ //$NON-NLS-2$
                testTheme.getThemeStylesheets("webadmin").get(0));
        assertEquals("Wedadmin style sheet: '123.css'", "123.css", //$NON-NLS-1$ //$NON-NLS-2$
                testTheme.getThemeStylesheets("webadmin").get(1));
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
    public void testGetCascadingResourceMissingKey() {
        assertNull("getCascadingResource not using resources.properties properly", //$NON-NLS-1$
                testTheme.getCascadingResource("this_is_not_a_valid_key")); //$NON-NLS-1$
    }

    @Test
    public void testGetCascadingResourceMissingResourcesFile() throws URISyntaxException {
        File testThemeRootPath = new File(this.getClass().getClassLoader().
                getResource("./org/ovirt/engine/core/branding") //$NON-NLS-1$
                .toURI().getPath());
        // theme 4 is purposely missing a resources.properties file
        File testThemePath = new File(testThemeRootPath.getAbsoluteFile(), "04-test4.brand"); //$NON-NLS-1$
        BrandingTheme theme4 = new BrandingTheme(testThemePath.getAbsolutePath(),
                testThemeRootPath, 2); //$NON-NLS-1$
        assertTrue("Theme 4 should load", theme4.load()); //$NON-NLS-1$

        assertNull("getCascadingResource not handling missing resources.properties gracefully", //$NON-NLS-1$
                theme4.getCascadingResource("this_file_is_missing_anyway")); //$NON-NLS-1$
    }

    @Test
    public void testGetCascadingResourceMissingResourcesProperty() throws URISyntaxException {
        File testThemeRootPath = new File(this.getClass().getClassLoader().
                getResource("./org/ovirt/engine/core/branding") //$NON-NLS-1$
                .toURI().getPath());
        // theme 5 is purposely missing a resources key in branding.properties
        File testThemePath = new File(testThemeRootPath.getAbsoluteFile(), "05-test5.brand"); //$NON-NLS-1$
        BrandingTheme theme5 = new BrandingTheme(testThemePath.getAbsolutePath(),
                testThemeRootPath, 2); //$NON-NLS-1$
        assertTrue("Theme 5 should load", theme5.load()); //$NON-NLS-1$

        assertNull("getCascadingResource not handling missing resources key gracefully", //$NON-NLS-1$
                theme5.getCascadingResource("this_file_is_missing_anyway")); //$NON-NLS-1$
    }

    @Test
    public void testInvalidTemplateReplaceProperty() throws URISyntaxException {
        File testThemeRootPath = new File(this.getClass().getClassLoader().
                getResource("./org/ovirt/engine/core/branding") //$NON-NLS-1$
                .toURI().getPath());
        // theme 6 purposely has an invalid welcome_replace value.
        File testThemePath = new File(testThemeRootPath.getAbsoluteFile(), "06-test6.brand"); //$NON-NLS-1$
        BrandingTheme theme6 = new BrandingTheme(testThemePath.getAbsolutePath(),
                testThemeRootPath, 1); //$NON-NLS-1$
        assertFalse("Theme 6 should not load", theme6.load()); //$NON-NLS-1$

    }

    @Test
    public void testTemplateReplaceProperty() throws URISyntaxException {
        File testThemeRootPath = new File(this.getClass().getClassLoader().
                getResource("./org/ovirt/engine/core/branding") //$NON-NLS-1$
                .toURI().getPath());
        File testThemePath = new File(testThemeRootPath.getAbsoluteFile(), "01-test.brand"); //$NON-NLS-1$
        BrandingTheme theme1 = new BrandingTheme(testThemePath.getAbsolutePath(),
                testThemeRootPath, 2); //$NON-NLS-1$
        assertTrue("Theme 1 should load", theme1.load()); //$NON-NLS-1$
        assertFalse("should replace template should be false", //$NON-NLS-1$
                theme1.shouldReplaceWelcomePageSectionTemplate());

    }
}

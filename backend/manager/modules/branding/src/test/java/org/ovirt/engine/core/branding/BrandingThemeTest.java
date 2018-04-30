package org.ovirt.engine.core.branding;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the {@code BrandingTheme} class.
 */
public class BrandingThemeTest {
    /**
     * The testTheme object.
     */
    BrandingTheme testTheme;

    @BeforeEach
    public void setUp() throws Exception {
        File testThemeRootPath = new File(this.getClass().getClassLoader().
            getResource("./org/ovirt/engine/core/branding").toURI().getPath()); //$NON-NLS-1$
        File testThemePath = new File(testThemeRootPath.getAbsoluteFile(), "01-test.brand"); //$NON-NLS-1$
        testTheme = new BrandingTheme(testThemePath.getAbsolutePath(),
                testThemeRootPath, 2);
        assertTrue(testTheme.load(), "The theme should load"); //$NON-NLS-1$
    }

    @Test
    public void testGetPath() {
        assertEquals("/01-test.brand", //$NON-NLS-1$ //$NON-NLS-2$
                testTheme.getPath(), "Path should be '/01-test.brand'");
    }

    @Test
    public void testGetThemeStyleSheet() {
        assertEquals("web_admin.css", //$NON-NLS-1$ //$NON-NLS-2$
                testTheme.getThemeStylesheets("webadmin").get(0), "Wedadmin style sheet: 'web_admin.css'");
        assertEquals("123.css", //$NON-NLS-1$ //$NON-NLS-2$
                testTheme.getThemeStylesheets("webadmin").get(1), "Wedadmin style sheet: '123.css'");
    }

    @Test
    public void testGetMessagesBundle() {
        List<ResourceBundle> bundle = testTheme.getMessagesBundle();
        assertNotNull(bundle, "There should be a bundle"); //$NON-NLS-1$
        assertEquals("Login header", bundle.get(0).getString("obrand.common.login_header_label")); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Test
    public void testGetMessagesBundleLocale() {
        List<ResourceBundle> bundle = testTheme.getMessagesBundle(Locale.FRENCH);
        assertNotNull(bundle, "There should be a bundle"); //$NON-NLS-1$
        assertEquals("Login header(fr)", bundle.get(0).getString("obrand.common.login_header_label")); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Test
    public void testGetCascadingResource() {
        assertTrue( //$NON-NLS-1$
                testTheme.getCascadingResource("favicon").getFile().getAbsolutePath().contains("/01-test.brand/images/favicon.ico"),
                "getCascadingResource not reading file from resources.properties"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("someMadeUp/contentType",
                testTheme.getCascadingResource("favicon").getContentType(),
                "getCascadingResource not reading contentType from resources.properties"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Test
    public void testGetCascadingResourceMissingKey() {
        assertNull( //$NON-NLS-1$
                testTheme.getCascadingResource("this_is_not_a_valid_key"),
                "getCascadingResource not using resources.properties properly"); //$NON-NLS-1$
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
        assertTrue(theme4.load(), "Theme 4 should load"); //$NON-NLS-1$

        assertNull( //$NON-NLS-1$
                theme4.getCascadingResource("this_file_is_missing_anyway"),
                "getCascadingResource not handling missing resources.properties gracefully"); //$NON-NLS-1$
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
        assertTrue(theme5.load(), "Theme 5 should load"); //$NON-NLS-1$

        assertNull( //$NON-NLS-1$
                theme5.getCascadingResource("this_file_is_missing_anyway"),
                "getCascadingResource not handling missing resources key gracefully"); //$NON-NLS-1$
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
        assertFalse(theme6.load(), "Theme 6 should not load"); //$NON-NLS-1$

    }

    @Test
    public void testTemplateReplaceProperty() throws URISyntaxException {
        File testThemeRootPath = new File(this.getClass().getClassLoader().
                getResource("./org/ovirt/engine/core/branding") //$NON-NLS-1$
                .toURI().getPath());
        File testThemePath = new File(testThemeRootPath.getAbsoluteFile(), "01-test.brand"); //$NON-NLS-1$
        BrandingTheme theme1 = new BrandingTheme(testThemePath.getAbsolutePath(),
                testThemeRootPath, 2); //$NON-NLS-1$
        assertTrue(theme1.load(), "Theme 1 should load"); //$NON-NLS-1$
        assertFalse( //$NON-NLS-1$
                theme1.shouldReplaceWelcomePageSectionTemplate(), "should replace template should be false");

    }
}

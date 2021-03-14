package org.ovirt.engine.core.branding;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.utils.servlet.LocaleFilter;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class BrandingManagerTest {
    BrandingManager testManager;

    @BeforeAll
    public static void setLocale() {
        Locale.setDefault(LocaleFilter.DEFAULT_LOCALE);
    }

    @BeforeEach
    public void setUp() throws Exception {
        File etcDir = new File(this.getClass().getClassLoader().
                getResource("./org/ovirt/engine/core").toURI().getPath()); //$NON-NLS-1$
        testManager = new BrandingManager(etcDir);
    }

    @Test
    public void testGetBrandingThemes() {
        List<BrandingTheme> result = testManager.getBrandingThemes();
        assertNotNull(result, "There should be a result"); //$NON-NLS-1$
        assertEquals(5, result.size(), "There should be five active themes"); //$NON-NLS-1$
        List<BrandingTheme> result2 = testManager.getBrandingThemes();
        assertNotNull(result2, "There should be a result"); //$NON-NLS-1$
        assertEquals(5, result2.size(), "There should be five active themes"); //$NON-NLS-1$
        // The second result should be the exact same object as the first one.
        assertSame(result, result2, "The result are not the same object"); //$NON-NLS-1$
    }

    @Test
    public void testGetMessages() throws JsonParseException, IOException {
        String result = testManager.getMessages("webadmin", Locale.US);
        assertNotNull(result, "There should be a result"); //$NON-NLS-1$
        ObjectMapper mapper = new ObjectMapper();
        JsonFactory factory = mapper.getJsonFactory();
        JsonParser parser = factory.createJsonParser(result);
        JsonNode resultNode = mapper.readTree(parser);
        // There should be 6 key value pairs (2 from webadmin, 4 common)
        assertEquals(6, resultNode.size(), "Size should be 6"); //$NON-NLS-1$
        assertEquals("Web Admin", resultNode.get("application_title").asText()); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Test
    public void testGetMessagesFromMap() {
        Map<String, String> input = new HashMap<>();
        String result = testManager.getMessagesFromMap(input);
        assertNull(result, "There should be no result"); //$NON-NLS-1$
        input.put("key1", "value1"); //$NON-NLS-1$ //$NON-NLS-2$
        result = testManager.getMessagesFromMap(input);
        assertNotNull(result, "There should be a result"); //$NON-NLS-1$
        assertEquals("{\"key1\":\"value1\"}", result, "String doesn't match"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Test
    public void testGetBrandingRootPath() throws URISyntaxException {
        String rootPath = this.getClass().getClassLoader().
            getResource("./org/ovirt/engine/core/").toURI().getPath() + "/branding"; //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(new File(rootPath), testManager.getBrandingRootPath(), "Root paths don't match"); //$NON-NLS-1$
    }

    @Test
    public void testGetMessageDefaultLocale() {
        String testKey = "obrand.common.main_header_label";
        String result = testManager.getMessage(testKey);
        assertEquals("Main header", result, "The result should be 'Main header'");
    }

    @Test
    public void testGetMessageBadKey() {
        String testKey = "obrandcommonmain_header_label";
        String result = testManager.getMessage(testKey);
        assertEquals("", result, "The result should be a blank string");
    }

    @Test
    public void testGetMessageNullKey() {
        String testKey = null;
        String result = testManager.getMessage(testKey);
        assertEquals("", result, "The result should be a blank string");
    }

    @Test
    public void testGetMessageFrenchLocale() {
        String testKey = "obrand.common.main_header_label";
        String result = testManager.getMessage(testKey, Locale.FRENCH);
        assertEquals("Main header(fr)", result, "The result should be 'Main header(fr)'");
    }

    /**
     * Test that resource serving works so that the resource in the highest number theme is served,
     * unless that theme has no resource -- in which case search the next highest, and so on.
     * e.g. if there are themes 01, 02, and 03, and 01 and 02 have favicon.ico, and 03 does not --
     * the favicon.ico in 02 is served.
     */
    @Test
    public void testGetCascadedResource() {
        // resources for this are hardcoded in test/resources.
        // brands  5, 4, 3 have no icon, brands 1 and 2 do. Should retrieve highest brand's (existing)
        // favicon (so, 2)
        assertNotNull(
                testManager.getCascadingResource("favicon"), "Should have found test brand 2's resource");
        assertTrue(
                testManager.getCascadingResource("favicon").getFile().getAbsolutePath()
                .contains("02-test2.brand"), "Should have found test brand 2's resource");
    }

    /**
     * Test that looking for a not-defined (in resources.properties) resource returns null.
     */
    @Test
    public void testGetCascadedResourceNotDefinedNotFound() {
        // resources for this are hardcoded in test/resources.
        assertNull(
                testManager.getCascadingResource("i_am_not_in_branding_properties"),
                "getCascadedResource should have returned null"); // not in any theme
    }

    /**
     * Test that looking for a not-defined (in resources.properties) resource returns null.
     */
    @Test
    public void testGetCascadedResourceDefinedButNotFound() {
        // resources for this are hardcoded in test/resources.
        assertNull(
                testManager.getCascadingResource("doesnt_exist"), "getCascadedResource should have returned null"); // exists is themes 1 and 2, but file is missing
    }

}

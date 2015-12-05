package org.ovirt.engine.core.branding;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ovirt.engine.core.utils.servlet.LocaleFilter;

public class BrandingManagerTest {
    BrandingManager testManager;

    @BeforeClass
    public static void setLocale() {
        Locale.setDefault(LocaleFilter.DEFAULT_LOCALE);
    }

    @Before
    public void setUp() throws Exception {
        File etcDir = new File(this.getClass().getClassLoader().
                getResource("./org/ovirt/engine/core").toURI().getPath()); //$NON-NLS-1$
        testManager = new BrandingManager(etcDir);
    }

    @Test
    public void testGetBrandingThemes() {
        List<BrandingTheme> result = testManager.getBrandingThemes();
        assertNotNull("There should be a result", result); //$NON-NLS-1$
        assertEquals("There should be five active themes", 5, result.size()); //$NON-NLS-1$
        List<BrandingTheme> result2 = testManager.getBrandingThemes();
        assertNotNull("There should be a result", result2); //$NON-NLS-1$
        assertEquals("There should be five active themes", 5, result2.size()); //$NON-NLS-1$
        // The second result should be the exact same object as the first one.
        assertTrue("The result are not the same object", result == result2); //$NON-NLS-1$
    }

    @Test
    public void testGetMessages() throws JsonParseException, IOException {
        String result = testManager.getMessages("user_portal", Locale.US);
        assertNotNull("There should be a result", result); //$NON-NLS-1$
        ObjectMapper mapper = new ObjectMapper();
        JsonFactory factory = mapper.getJsonFactory();
        JsonParser parser = factory.createJsonParser(result);
        JsonNode resultNode = mapper.readTree(parser);
        // There should be 5 key value pairs (1 from user portal, 4 common)
        assertEquals("Size should be 5", 5, resultNode.size()); //$NON-NLS-1$
        assertEquals(resultNode.get("application_title").getTextValue(), "User Portal"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Test
    public void testGetMessagesFromMap() {
        Map<String, String> input = new HashMap<>();
        String result = testManager.getMessagesFromMap(input);
        assertNull("There should be no result", result); //$NON-NLS-1$
        input.put("key1", "value1"); //$NON-NLS-1$ //$NON-NLS-2$
        result = testManager.getMessagesFromMap(input);
        assertNotNull("There should be a result", result); //$NON-NLS-1$
        assertEquals("String doesn't match", "{\"key1\":\"value1\"}", result); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Test
    public void testGetBrandingRootPath() throws URISyntaxException {
        String rootPath = this.getClass().getClassLoader().
            getResource("./org/ovirt/engine/core/").toURI().getPath() + "/branding"; //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("Root paths don't match", new File(rootPath), testManager.getBrandingRootPath()); //$NON-NLS-1$
    }

    @Test
    public void testGetMessageDefaultLocale() {
        String testKey = "obrand.common.main_header_label";
        String result = testManager.getMessage(testKey);
        assertEquals("The result should be 'Main header'", "Main header", result);
    }

    @Test
    public void testGetMessageBadKey() {
        String testKey = "obrandcommonmain_header_label";
        String result = testManager.getMessage(testKey);
        assertEquals("The result should be a blank string", "", result);
    }

    @Test
    public void testGetMessageNullKey() {
        String testKey = null;
        String result = testManager.getMessage(testKey);
        assertEquals("The result should be a blank string", "", result);
    }

    @Test
    public void testGetMessageFrenchLocale() {
        String testKey = "obrand.common.main_header_label";
        String result = testManager.getMessage(testKey, Locale.FRENCH);
        assertEquals("The result should be 'Main header(fr)'", "Main header(fr)", result);
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
        assertNotNull("Should have found test brand 2's resource",
                testManager.getCascadingResource("favicon"));
        assertTrue("Should have found test brand 2's resource",
                testManager.getCascadingResource("favicon").getFile().getAbsolutePath()
                .contains("02-test2.brand"));
    }

    /**
     * Test that looking for a not-defined (in resources.properties) resource returns null.
     */
    @Test
    public void testGetCascadedResourceNotDefinedNotFound() {
        // resources for this are hardcoded in test/resources.
        assertNull("getCascadedResource should have returned null",
                testManager.getCascadingResource("i_am_not_in_branding_properties")); // not in any theme
    }

    /**
     * Test that looking for a not-defined (in resources.properties) resource returns null.
     */
    @Test
    public void testGetCascadedResourceDefinedButNotFound() {
        // resources for this are hardcoded in test/resources.
        assertNull("getCascadedResource should have returned null",
                testManager.getCascadingResource("doesnt_exist")); // exists is themes 1 and 2, but file is missing
    }

}

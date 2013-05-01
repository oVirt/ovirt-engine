package org.ovirt.engine.core.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.config.validation.ConfigActionType;

public class EngineConfigCLIParserTest {
    private EngineConfigCLIParser parser;

    @Before
    public void setUp() {
        parser = new EngineConfigCLIParser();
    }

    @Test
    public void testParseAllAction() throws Exception {
        parser.parse(new String[] { "-a" });
        assertEquals(ConfigActionType.ACTION_ALL, parser.getConfigAction());
    }

    @Test
    public void testParseListActionWithExtraArguments() throws Exception {
        parser.parse(new String[] { "-l", "b", "c" });
        assertEquals(ConfigActionType.ACTION_LIST, parser.getConfigAction());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseNoAction() {
        parser.parse(new String[] { "-b", "-t", "filename" });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseActionNotFirst() throws Exception {
        parser.parse(new String[] { "-b", "-a", "filename" });
    }

    @Test
    public void testGetOptionalConfigDir() throws Exception {
        parser.parse(new String[] { "-a", "-c", "dirname" });
        assertEquals("dirname", parser.getAlternateConfigFile());
    }

    @Test
    public void testGetAlternativePropertiesFile() throws Exception {
        parser.parse(new String[] { "-a", "-p", "filename" });
        assertEquals("filename", parser.getAlternatePropertiesFile());
    }

    @Test
    public void testParseGetActionWithKeyInFirstArgument() throws Exception {
        parser.parse(new String[] { "--get=keyToGet" });
        assertEquals(ConfigActionType.ACTION_GET, parser.getConfigAction());
        assertEquals("keyToGet", parser.getKey());
    }

    @Test
    public void testParseGetActionWithKeyInSecondArgument() throws Exception {
        parser.parse(new String[] { "-g", "keyToGet" });
        assertEquals(ConfigActionType.ACTION_GET, parser.getConfigAction());
        assertEquals("keyToGet", parser.getKey());
    }

    @Test
    public void testParseSetActionWithValidArguments() throws Exception {
        parser.parse(new String[] { "-s", "keyToSet=valueToSet" });
        assertEquals(ConfigActionType.ACTION_SET, parser.getConfigAction());
        assertEquals("keyToSet", parser.getKey());
        assertEquals("valueToSet", parser.getValue());
    }

    @Test
    public void testParseReloadActionWithUser() throws Exception {
        parser.parse(new String[] { "-r", "--user=username" });
        assertEquals(ConfigActionType.ACTION_RELOAD, parser.getConfigAction());
        assertEquals("username", parser.getUser());
    }

    @Test
    public void testParseReloadActionWithUserPassFile() throws Exception {
        parser.parse(new String[] { "--reload", "--user=username", "--admin-pass-file=filename" });
        assertEquals(ConfigActionType.ACTION_RELOAD, parser.getConfigAction());
        assertEquals("username", parser.getUser());
        assertEquals("filename", parser.getAdminPassFile());
    }

    @Test
    public void testParseOnlyReloadableFlag() throws Exception {
        parser.parse(new String[] { "--list", "--only-reloadable" });
        assertTrue(parser.isOnlyReloadable());
    }

    @Test
    public void testParseNoOnlyReloadableFlag() throws Exception {
        parser.parse(new String[] { "--list" });
        assertFalse(parser.isOnlyReloadable());
    }
}

package org.ovirt.engine.core.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.config.validation.ConfigActionType;

public class EngineConfigCLIParserTest {
    private EngineConfigCLIParser parser;

    @BeforeEach
    public void setUp() {
        parser = new EngineConfigCLIParser();
    }

    @Test
    public void testParseAllAction() {
        parser.parse(new String[] { "-a" });
        assertEquals(ConfigActionType.ACTION_ALL, parser.getConfigAction());
    }

    @Test
    public void testParseListActionWithExtraArguments() {
        parser.parse(new String[] { "-l", "b", "c" });
        assertEquals(ConfigActionType.ACTION_LIST, parser.getConfigAction());
    }

    @Test
    public void testParseNoAction() {
        assertThrows(IllegalArgumentException.class, () -> parser.parse(new String[] { "-b", "-t", "filename" }));
    }

    @Test
    public void testParseActionNotFirst() {
        assertThrows(IllegalArgumentException.class, () -> parser.parse(new String[] { "-b", "-a", "filename" }));
    }

    @Test
    public void testGetOptionalConfigDir() {
        parser.parse(new String[] { "-a", "-c", "dirname" });
        assertEquals("dirname", parser.getAlternateConfigFile());
    }

    @Test
    public void testGetAlternativePropertiesFile() {
        parser.parse(new String[] { "-a", "-p", "filename" });
        assertEquals("filename", parser.getAlternatePropertiesFile());
    }

    @Test
    public void testParseGetActionWithKeyInFirstArgument() {
        parser.parse(new String[] { "--get=keyToGet" });
        assertEquals(ConfigActionType.ACTION_GET, parser.getConfigAction());
        assertEquals("keyToGet", parser.getKey());
    }

    @Test
    public void testParseGetActionWithKeyInSecondArgument() {
        parser.parse(new String[] { "-g", "keyToGet" });
        assertEquals(ConfigActionType.ACTION_GET, parser.getConfigAction());
        assertEquals("keyToGet", parser.getKey());
    }

    @Test
    public void testParseSetActionWithValidArguments() {
        parser.parse(new String[] { "-s", "keyToSet=valueToSet" });
        assertEquals(ConfigActionType.ACTION_SET, parser.getConfigAction());
        assertEquals("keyToSet", parser.getKey());
        assertEquals("valueToSet", parser.getValue());
    }

    @Test
    public void testParseReloadActionWithUser() {
        parser.parse(new String[] { "-r", "--user=username" });
        assertEquals(ConfigActionType.ACTION_RELOAD, parser.getConfigAction());
        assertEquals("username", parser.getUser());
    }

    @Test
    public void testParseReloadActionWithUserPassFile() {
        parser.parse(new String[] { "--reload", "--user=username", "--admin-pass-file=filename" });
        assertEquals(ConfigActionType.ACTION_RELOAD, parser.getConfigAction());
        assertEquals("username", parser.getUser());
        assertEquals("filename", parser.getAdminPassFile());
    }

    @Test
    public void testParseOnlyReloadableFlag() {
        parser.parse(new String[] { "--list", "--only-reloadable" });
        assertTrue(parser.isOnlyReloadable());
    }

    @Test
    public void testParseNoOnlyReloadableFlag() {
        parser.parse(new String[] { "--list" });
        assertFalse(parser.isOnlyReloadable());
    }
}

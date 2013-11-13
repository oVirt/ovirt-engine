package org.ovirt.engine.core.searchbackend;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class OsValueAutoCompleterTest {

    private OsValueAutoCompleter completer;

    @Before
    public void setup() {

        Map<Integer, String> completionMap = new HashMap<Integer, String>();
        completionMap.put(0, "other");
        completionMap.put(1, "rhel_x");
        completionMap.put(2, "rhel_x_y");
        completer = new OsValueAutoCompleter(completionMap);
    }

    @Test
    public void testCompleteSingleValue() {
        Assert.assertEquals(1, completer.getCompletion("ot").length);
    }

    @Test
    public void testCompletionMutliReturnValue() {
        Assert.assertTrue(completer.getCompletion("r").length > 1);
    }

    @Test
    public void testConvertStringToId() {
        Assert.assertEquals("0", completer.convertFieldEnumValueToActualValue("other"));
        Assert.assertEquals("1", completer.convertFieldEnumValueToActualValue("rhel_x"));
        Assert.assertEquals("2", completer.convertFieldEnumValueToActualValue("rhel_x_y"));
    }


}

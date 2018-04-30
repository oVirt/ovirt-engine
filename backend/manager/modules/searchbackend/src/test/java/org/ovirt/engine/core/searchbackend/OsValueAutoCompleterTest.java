package org.ovirt.engine.core.searchbackend;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class OsValueAutoCompleterTest {

    private OsValueAutoCompleter completer;
    public static Map<Integer, String> completionMap = new HashMap<>();
    static {
        completionMap.put(0, "other");
        completionMap.put(1, "rhel_x");
        completionMap.put(2, "rhel_x_y");
        completionMap.put(3, "windows_2008");
        completionMap.put(4, "windows_2008_R2");
        completionMap.put(5, "windows_2008_R2x64");
    }

    public static Stream<Map.Entry<Integer, String>> data() {
        return completionMap.entrySet().stream();
    }

    @BeforeEach
    public void setup() {
        completer = new OsValueAutoCompleter(completionMap);
    }

    @Test
    public void testCompleteSingleValue() {
        assertEquals(1, completer.getCompletion("ot").length);
    }

    @Test
    public void testCompletionMutliReturnValue() {
        assertTrue(completer.getCompletion("r").length > 1);
    }

    /**
     * every auto completed input is always valid when using the auto-completer validate() method
     */
    @ParameterizedTest
    @MethodSource("data")
    public void autoCompletedInputIsAlwaysValid(Map.Entry<Integer, String> osCompletionEntry) {
        String reason = "input " + osCompletionEntry.getValue() + " is invalid";
        assertThat(reason,
                true, is(completer.validate(osCompletionEntry.getValue())));
    }

    /**
     * every auto-completed value matches its numeric key value.
     * e.g
     * when auto-completing "rhel" and "rhel_x" was picked and is used as a search term, it shall
     * match its key in the completion list. i.e convertFieldEnumValueToActualValue("rhel_x") always yields -> 1
     */
    @ParameterizedTest
    @MethodSource("data")
    public void autoCompletedInputMatchesItsNumericKeyValue(Map.Entry<Integer, String> osCompletionEntry) {
        assertThat(osCompletionEntry.getKey().toString(),
                is(completer.convertFieldEnumValueToActualValue(osCompletionEntry.getValue())));
    }

}

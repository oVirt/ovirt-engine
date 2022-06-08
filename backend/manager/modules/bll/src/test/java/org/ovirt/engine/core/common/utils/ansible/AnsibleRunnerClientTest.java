package org.ovirt.engine.core.common.utils.ansible;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.http.client.HttpClient;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AnsibleRunnerClientTest {
    private static Instant dateTime =
            LocalDateTime.of(1989, 11, 17, 16, 0, 0).atOffset(ZoneOffset.UTC).toInstant();

    @Mock
    HttpClient httpClient;

    @InjectMocks
    AnsibleRunnerClient client;


    @ParameterizedTest
    @MethodSource("provideParamsForCommandVariablesFormatting")
    void testCommandVariablesFormatting(Map<String, Object> variables, String playAction, String result) {
        assertEquals(result, client.formatCommandVariables(variables, playAction));
    }

    private static Map<String, Object> provideCommandVariables() {
        Map<String, Object> variables = new HashMap<>();
        variables.put("dateValue", dateTime);
        variables.put("stringValue", "default");
        variables.put("booleanValue", false);
        variables.put("integerValue", 4);
        variables.put("stringWithQuotes", "Contains \"double quotes\"");
        variables.put("xmlVersion", "\\\\\\\"1.0\\\\\\\"");
        return variables;
    }

    private static Stream<Arguments> provideParamsForCommandVariablesFormatting() {
        return Stream.of(
                Arguments.of(
                        provideCommandVariables(),
                        "",
                        "{\"dateValue\":\"1989-11-17T16:00:00Z\","
                                + "\"stringValue\":\"default\","
                                // Boolean values are stored as boolean in JSON
                                + "\"booleanValue\":false,"
                                // Numbers are stored as numbers in JSON
                                + "\"integerValue\":4,"
                                // Double quotes need to be properly escaped in JSON output
                                + "\"stringWithQuotes\":\"Contains \\\"double quotes\\\"\","
                                + "\"xmlVersion\":\"\\\\\\\\\\\\\\\"1.0\\\\\\\\\\\\\\\"\"}"));
    }
}

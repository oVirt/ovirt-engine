package org.ovirt.engine.core.vdsbroker.vdsbroker;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.JsonObject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.utils.JsonHelper;
import org.ovirt.engine.core.utils.MockConfigExtension;

@ExtendWith({MockitoExtension.class, MockConfigExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
public class IgnitionPayloadTest extends CloudInitHandlerTest {

    private String validIgnitionString;

    @BeforeEach
    public void setUp() throws IOException {
        super.setUp();
        validIgnitionString = Files.lines(Paths.get("src/test/resources/ignition.ign")).collect(Collectors.joining());
    }

    @Test
    public void testIgnitionCustomScriptHandling() {
        vmInit.setCustomScript(validIgnitionString);

        Map<String, byte[]> payload =
                assertDoesNotThrow(() -> new IgnitionHandler(vmInit).getFileData());

        assertTrue(payload.containsKey("openstack/latest/user_data"));

        String userData =
                assertDoesNotThrow(() -> new String(payload.get("openstack/latest/user_data")));

        Map<String, Object> ignitionJson =
                assertDoesNotThrow(() -> JsonHelper.jsonToMap(userData));

        assertTrue(ignitionJson.containsKey("ignition"));

        assertEquals(validIgnitionString, userData);
    }


    @Test
    public void testIgnitionHostnameHandling() {
        vmInit.setCustomScript(validIgnitionString);
        vmInit.setHostname("master-0.our.org");

        Map<String, byte[]> payload = assertDoesNotThrow(() -> new IgnitionHandler(vmInit).getFileData());

        String userData =
                assertDoesNotThrow(() -> new String(payload.get("openstack/latest/user_data")));

        JsonObject jsonObject = Json.createReader(new StringReader(userData)).readObject();

        assertTrue(jsonObject.containsKey("ignition"));
        assertTrue(jsonObject.containsKey("storage"));
        assertTrue(jsonObject.getJsonObject("storage").containsKey("files"));
    }
}

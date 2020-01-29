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
import org.ovirt.engine.core.compat.Version;
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
                assertDoesNotThrow(() -> new IgnitionHandler(vmInit, null).getFileData());

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

        Map<String, byte[]> payload = assertDoesNotThrow(() -> new IgnitionHandler(vmInit, null).getFileData());

        String userData =
                assertDoesNotThrow(() -> new String(payload.get("openstack/latest/user_data")));

        JsonObject jsonObject = Json.createReader(new StringReader(userData)).readObject();

        assertTrue(jsonObject.containsKey("ignition"));
        assertTrue(jsonObject.containsKey("storage"));
        assertTrue(jsonObject.getJsonObject("storage").containsKey("files"));
    }

    @Test
    public void testIgnitionUserHandling() {
        vmInit.setCustomScript(validIgnitionString);
        vmInit.setUserName("test");
        vmInit.setRootPassword("test");
        vmInit.setAuthorizedKeys(
                "ssh-rsa A your_user\n" +
                "ssh-rsa B username@hostname");

        Map<String, byte[]> payload = assertDoesNotThrow(() -> new IgnitionHandler(vmInit, null).getFileData());

        String userData =
                assertDoesNotThrow(() -> new String(payload.get("openstack/latest/user_data")));

        JsonObject jsonObject = Json.createReader(new StringReader(userData)).readObject();

        assertTrue(jsonObject.containsKey("ignition"));
        assertTrue(jsonObject.containsKey("passwd"));
        assertTrue(jsonObject.getJsonObject("passwd").containsKey("users"));
        JsonObject users = jsonObject.getJsonObject("passwd").getJsonArray("users").getJsonObject(0);

        assertEquals("core", users.getJsonString("name").getString());
        assertTrue(users.containsKey("passwordHash"));
        assertEquals("$6$43y3tkl...", users.getJsonString("passwordHash").getString());
        assertTrue(users.containsKey("sshAuthorizedKeys"));
        assertEquals(1, users.getJsonArray("sshAuthorizedKeys").size());
        assertEquals("key1", users.getJsonArray("sshAuthorizedKeys").getString(0));

        users = jsonObject.getJsonObject("passwd").getJsonArray("users").getJsonObject(1);

        assertTrue(users.containsKey("name"));
        assertEquals("test", users.getJsonString("name").getString());
        assertTrue(users.containsKey("passwordHash"));
        assertEquals("test", users.getJsonString("passwordHash").getString());
        assertTrue(users.containsKey("sshAuthorizedKeys"));
        assertEquals(2, users.getJsonArray("sshAuthorizedKeys").size());
        assertEquals("ssh-rsa A your_user", users.getJsonArray("sshAuthorizedKeys").getString(0));
        assertEquals("ssh-rsa B username@hostname", users.getJsonArray("sshAuthorizedKeys").getString(1));
    }

    @Test
    public void testIgnitionStorageHandling() {
        vmInit.setCustomScript(validIgnitionString);
        vmInit.setHostname("test");

        Map<String, byte[]> payload = assertDoesNotThrow(() -> new IgnitionHandler(vmInit, null).getFileData());

        String userData =
                assertDoesNotThrow(() -> new String(payload.get("openstack/latest/user_data")));

        JsonObject jsonObject = Json.createReader(new StringReader(userData)).readObject();

        assertTrue(jsonObject.containsKey("ignition"));
        assertTrue(jsonObject.containsKey("storage"));
        assertTrue(jsonObject.getJsonObject("storage").containsKey("filesystems"));
        assertTrue(jsonObject.getJsonObject("storage").getJsonArray("filesystems").getJsonObject(0).containsKey("mount"));
        JsonObject filesystems = jsonObject.getJsonObject("storage").getJsonArray("filesystems").getJsonObject(0).getJsonObject("mount");

        assertEquals("/dev/disk/by-label/ROOT", filesystems.getJsonString("device").getString());

        assertEquals(2, jsonObject.getJsonObject("storage").getJsonArray("files").size());
        JsonObject files = jsonObject.getJsonObject("storage").getJsonArray("files").getJsonObject(0);

        assertTrue(files.containsKey("path"));
        assertEquals("/etc/test", files.getJsonString("path").getString());

        files = jsonObject.getJsonObject("storage").getJsonArray("files").getJsonObject(1);
        assertEquals("/etc/hostname", files.getJsonString("path").getString());
    }

    @Test
    public void testHostNameUIHandlingNullCustomScript() {
        vmInit.setHostname("test");
        vmInit.setCustomScript(null);

        Map<String, byte[]> payload = assertDoesNotThrow(() -> new IgnitionHandler(vmInit, new Version("2.2.0")).getFileData());

        String userData =
                assertDoesNotThrow(() -> new String(payload.get("openstack/latest/user_data")));

        JsonObject jsonObject = Json.createReader(new StringReader(userData)).readObject();

        assertTrue(jsonObject.containsKey("ignition"));
        assertEquals("2.2.0", jsonObject.getJsonObject("ignition").getJsonString("version").getString());
        assertTrue(jsonObject.containsKey("storage"));

        JsonObject files = jsonObject.getJsonObject("storage").getJsonArray("files").getJsonObject(0);
        assertEquals("/etc/hostname", files.getJsonString("path").getString());
    }

    @Test
    public void testUserUIHandlingNullCustomScript() {
        vmInit.setUserName("test");
        vmInit.setRootPassword("some_pass");
        vmInit.setCustomScript(null);

        Map<String, byte[]> payload = assertDoesNotThrow(() -> new IgnitionHandler(vmInit, new Version("2.2.0")).getFileData());

        String userData =
                assertDoesNotThrow(() -> new String(payload.get("openstack/latest/user_data")));

        JsonObject jsonObject = Json.createReader(new StringReader(userData)).readObject();

        assertTrue(jsonObject.containsKey("ignition"));
        assertEquals("2.2.0", jsonObject.getJsonObject("ignition").getJsonString("version").getString());
        assertTrue(jsonObject.containsKey("passwd"));
        assertTrue(jsonObject.getJsonObject("passwd").containsKey("users"));
        JsonObject users = jsonObject.getJsonObject("passwd").getJsonArray("users").getJsonObject(0);

        assertEquals("test", users.getJsonString("name").getString());
        assertTrue(users.containsKey("passwordHash"));
        assertEquals("some_pass", users.getJsonString("passwordHash").getString());
    }
}

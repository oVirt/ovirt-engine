package org.ovirt.engine.core.common.utils.ansible;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.utils.MockEngineLocalConfigExtension;

@ExtendWith(MockEngineLocalConfigExtension.class)
public class AnsibleCommandFactoryTest {

    private static final String OVIRT_HOME = "/var/lib/ovirt-engine/";
    private static final String PRIVATE_KEY = "--private-key=/etc/pki/ovirt-engine/keys/engine_id_rsa";
    private static final String ANSIBLE_PLAYBOOK = "myplaybook.yml";
    private static final String ANSIBLE_PLAYBOOK_FULL_PATH = "/usr/share/ovirt-engine/playbooks/myplaybook.yml";
    private static final String IGNORE_SSH_CONFIG = "--ssh-common-args=-F " + OVIRT_HOME + ".ssh/config";
    private static final String ANSIBLE_LOG_LEVEL = "-v";
    private AnsibleCommandFactory commandFactory;

    @SuppressWarnings("unused") // used via reflection by MockEngineLocalConfigExtension
    public static Stream<Pair<String, String>> mockEngineLocalConfiguration() {
        return Stream.of(
                new Pair<>("ENGINE_PKI", "/etc/pki/ovirt-engine/"),
                new Pair<>("ENGINE_USR", "/usr/share/ovirt-engine/"),
                new Pair<>("ENGINE_VAR", OVIRT_HOME),
                new Pair<>("ENGINE_LOG", "/var/log/ovirt-engine/"));
    }

    @BeforeEach
    public void setup() {
        commandFactory = new AnsibleCommandFactory();
    }

    @Test
    public void testAllEmpty() {
        String command = createCommand(new AnsibleCommandConfig().playbook(ANSIBLE_PLAYBOOK));
        assertEquals(
                join(
                        AnsibleCommandConfig.ANSIBLE_COMMAND,
                        IGNORE_SSH_CONFIG,
                        ANSIBLE_LOG_LEVEL,
                        PRIVATE_KEY,
                        ANSIBLE_PLAYBOOK_FULL_PATH),
                command);
    }

    @Test
    public void testDisableVerboseMode() {
        String command = createCommand(
                new AnsibleCommandConfig()
                        .verboseLevel(AnsibleVerbosity.LEVEL0)
                        .playbook(ANSIBLE_PLAYBOOK));

        assertEquals(
                join(AnsibleCommandConfig.ANSIBLE_COMMAND, IGNORE_SSH_CONFIG, PRIVATE_KEY, ANSIBLE_PLAYBOOK_FULL_PATH),
                command);
    }

    @Test
    public void testInventoryFile() {
        Path inventoryFile = Paths.get("myfile");
        String command = createCommand(
                new AnsibleCommandConfig()
                        .playbook(ANSIBLE_PLAYBOOK),
                inventoryFile);
        assertEquals(
                join(
                        AnsibleCommandConfig.ANSIBLE_COMMAND,
                        IGNORE_SSH_CONFIG,
                        ANSIBLE_LOG_LEVEL,
                        PRIVATE_KEY,
                        "--inventory=" + inventoryFile,
                        ANSIBLE_PLAYBOOK_FULL_PATH),
                command);
    }

    @Test
    public void testDifferentVerbosity() {
        String command = createCommand(
                new AnsibleCommandConfig()
                        .verboseLevel(AnsibleVerbosity.LEVEL2)
                        .playbook(ANSIBLE_PLAYBOOK));
        assertEquals(
                join(
                        AnsibleCommandConfig.ANSIBLE_COMMAND,
                        IGNORE_SSH_CONFIG,
                        "-vv",
                        PRIVATE_KEY,
                        ANSIBLE_PLAYBOOK_FULL_PATH),
                command);
    }

    @Test
    public void testVerbosityLevelZero() {
        String command = createCommand(
                new AnsibleCommandConfig()
                        .verboseLevel(AnsibleVerbosity.LEVEL0)
                        .playbook(ANSIBLE_PLAYBOOK));
        assertEquals(
                join(AnsibleCommandConfig.ANSIBLE_COMMAND, IGNORE_SSH_CONFIG, PRIVATE_KEY, ANSIBLE_PLAYBOOK_FULL_PATH),
                command);
    }

    @Test
    public void testExtraVariables() {
        String command = createCommand(
                new AnsibleCommandConfig()
                        .variable("a", "1")
                        .variable("b", "2")
                        .variable("c", "3")
                        .playbook(ANSIBLE_PLAYBOOK));
        assertEquals(
                join(
                        AnsibleCommandConfig.ANSIBLE_COMMAND,
                        IGNORE_SSH_CONFIG,
                        ANSIBLE_LOG_LEVEL,
                        PRIVATE_KEY,
                        "--extra-vars=a=\"1\"",
                        "--extra-vars=b=\"2\"",
                        "--extra-vars=c=\"3\"",
                        ANSIBLE_PLAYBOOK_FULL_PATH),
                command);
    }

    @Test
    public void testComplexCommand() {
        String command = createCommand(
                new AnsibleCommandConfig()
                        .privateKey(Paths.get("/mykey"))
                        .inventoryFile(Paths.get("/myinventory"))
                        .limit("mylimit")
                        .verboseLevel(AnsibleVerbosity.LEVEL3)
                        .variable("a", "1")
                        .variable("b", "2")
                        .playbook(ANSIBLE_PLAYBOOK),
                Paths.get("/myinventory"));
        assertEquals(
                join(
                        AnsibleCommandConfig.ANSIBLE_COMMAND,
                        IGNORE_SSH_CONFIG,
                        "-vvv",
                        "--private-key=/mykey",
                        "--inventory=/myinventory",
                        "--limit=mylimit",
                        "--extra-vars=a=\"1\"",
                        "--extra-vars=b=\"2\"",
                        ANSIBLE_PLAYBOOK_FULL_PATH),
                command);
    }

    private String createCommand(AnsibleCommandConfig commandConfig) {
        return createCommand(commandConfig, null);
    }

    private String createCommand(AnsibleCommandConfig commandConfig, Path inventoryFile) {
        return StringUtils.join(commandFactory.create(commandConfig, inventoryFile), " ").trim();
    }

    private String join(String... params) {
        return StringUtils.join(params, " ");
    }
}

package org.ovirt.engine.core.common.utils.ansible;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.time.Clock;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.utils.MockEngineLocalConfigExtension;

@ExtendWith(MockEngineLocalConfigExtension.class)
public class AnsibleCommandLogFileFactoryTest {
    private static final String ANSIBLE_PLAYBOOK = "myplaybook.yml";
    private static final ZoneId DEFAULT_ZONE_ID = Clock.systemDefaultZone().getZone();
    private static final ZonedDateTime MOCKED_NOW = ZonedDateTime.of(2019, 8, 30, 11, 32, 0, 0, DEFAULT_ZONE_ID);
    private static final String MOCKED_DATE_TIME_STRING =
            MOCKED_NOW.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    private static final Clock MOCKED_CLOCK = Clock.fixed(MOCKED_NOW.toInstant(), DEFAULT_ZONE_ID);

    private AnsibleCommandLogFileFactory factory;

    @SuppressWarnings("unused") // used via reflection by MockEngineLocalConfigExtension
    public static Stream<Pair<String, String>> mockEngineLocalConfiguration() {
        return Stream.of(
                new Pair<>("ENGINE_PKI", "/etc/pki/ovirt-engine/"),
                new Pair<>("ENGINE_USR", "/usr/share/ovirt-engine/"),
                new Pair<>("ENGINE_VAR", "/var/lib/ovirt-engine/"),
                new Pair<>("ENGINE_LOG", "/var/log/ovirt-engine/"));
    }

    @BeforeEach
    public void setup() {
        factory = new AnsibleCommandLogFileFactory();
        factory.setClock(MOCKED_CLOCK);
    }

    @Test
    public void shouldCreateLogFileWithDefaultConfig() {
        AnsibleCommandConfig commandConfig = new AnsibleCommandConfig()
                .playbook(ANSIBLE_PLAYBOOK)
                .enableLogging(true);

        File logFile = factory.create(commandConfig);

        String expectedPath =
                String.format("/var/log/ovirt-engine/ansible/ansible-%s-%s.log",
                        MOCKED_DATE_TIME_STRING,
                        ANSIBLE_PLAYBOOK.replace('.', '_'));
        assertThat(logFile.getAbsolutePath()).isEqualTo(expectedPath);
    }

    @Test
    public void shouldCreateLogFileWithUserConfig() {
        AnsibleCommandConfig commandConfig = new AnsibleCommandConfig()
                .playbook(ANSIBLE_PLAYBOOK)
                .logFileDirectory("myDir")
                .logFileName("myFileName")
                .logFilePrefix("myPrefix")
                .logFileSuffix("mySuffix")
                .enableLogging(true);

        File logFile = factory.create(commandConfig);

        String expectedPath =
                String.format("/var/log/ovirt-engine/myDir/myPrefix-%s-myFileName-mySuffix.log",
                        MOCKED_DATE_TIME_STRING);
        assertThat(logFile.getAbsolutePath()).isEqualTo(expectedPath);
    }

    @Test
    public void shouldNotCreateLogFileIfLogginDisabled() {
        AnsibleCommandConfig commandConfig = new AnsibleCommandConfig()
                .playbook(ANSIBLE_PLAYBOOK)
                .enableLogging(false)
                .logFileDirectory("myDir")
                .logFileName("myFileName")
                .logFilePrefix("myPrefix")
                .logFileSuffix("mySuffix");

        File logFile = factory.create(commandConfig);

        assertThat(logFile).isNull();
    }

}

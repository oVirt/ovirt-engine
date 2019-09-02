package org.ovirt.engine.core.common.utils.ansible;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.utils.MockEngineLocalConfigExtension;

@ExtendWith(MockEngineLocalConfigExtension.class)
public class AnsibleCommandInventoryFileFactoryTest {
    private static final String ANSIBLE_PLAYBOOK = "myplaybook.yml";

    private AnsibleCommandInventoryFileFactory factory;

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
        factory = new AnsibleCommandInventoryFileFactory();
    }

    @Test
    public void shouldUseUserProvidedInventoryFile() throws IOException {
        Path expectedInventoryFile = Paths.get("myfile");
        AnsibleCommandConfig config = AnsibleCommandConfig.builder()
                .playbook(ANSIBLE_PLAYBOOK)
                .inventoryFile(expectedInventoryFile)
                .build();

        AutoRemovableTempFile createdInventoryFile = factory.create(config);

        assertThat(createdInventoryFile.getFilePath()).isSameAs(expectedInventoryFile);
    }
}

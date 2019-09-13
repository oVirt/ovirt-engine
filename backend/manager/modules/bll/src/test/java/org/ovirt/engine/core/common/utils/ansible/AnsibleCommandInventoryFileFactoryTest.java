package org.ovirt.engine.core.common.utils.ansible;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.utils.InjectorExtension;
import org.ovirt.engine.core.utils.MockEngineLocalConfigExtension;

@ExtendWith({MockitoExtension.class, InjectorExtension.class, MockEngineLocalConfigExtension.class})
public class AnsibleCommandInventoryFileFactoryTest {
    private static final String ANSIBLE_PLAYBOOK = "myplaybook.yml";

    @SuppressWarnings("unused") // used via reflection by MockEngineLocalConfigExtension
    public static Stream<Pair<String, String>> mockEngineLocalConfiguration() {
        return Stream.of(
                new Pair<>("ENGINE_PKI", "/etc/pki/ovirt-engine/"),
                new Pair<>("ENGINE_USR", "/usr/share/ovirt-engine/"),
                new Pair<>("ENGINE_VAR", "/var/lib/ovirt-engine/"),
                new Pair<>("ENGINE_LOG", "/var/log/ovirt-engine/"));
    }

    @Mock
    private FileRemover fileRemover;

    @InjectMocks
    private AnsibleCommandInventoryFileFactory factory;

    @Test
    public void shouldUseUserProvidedInventoryFile() throws IOException {
        Path expectedInventoryFile = Paths.get("myfile");
        AnsibleCommandConfig config = AnsibleCommandConfig.builder()
                .playbook(ANSIBLE_PLAYBOOK)
                .inventoryFile(expectedInventoryFile)
                .build();

        try (AutoRemovableTempFile createdInventoryFile = factory.create(config)) {
            assertThat(createdInventoryFile)
                    .isNotNull();
            assertThat(createdInventoryFile.getFilePath())
                    .isNotNull()
                    .isSameAs(expectedInventoryFile);
        }
    }
}

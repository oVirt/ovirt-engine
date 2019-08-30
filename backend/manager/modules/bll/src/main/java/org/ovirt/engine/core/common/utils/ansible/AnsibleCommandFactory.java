package org.ovirt.engine.core.common.utils.ansible;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.inject.Singleton;

import org.ovirt.engine.core.utils.EngineLocalConfig;

@Singleton
public class AnsibleCommandFactory {

    private EngineLocalConfig config;

    public AnsibleCommandFactory() {
        config = EngineLocalConfig.getInstance();
    }

    /**
     * The generated command will look like:
     *
     * /usr/bin/ansible-playbook -${verboseLevel} --private-key=${privateKey} --limit=${limit} \
     * --extra-vars=${variables} ${playbook}
     */
    public List<String> create(AnsibleCommandConfig ansibleCommandConfig, Path inventoryFile) {
        List<String> ansibleCommand = new ArrayList<>();
        ansibleCommand.add(AnsibleCommandConfig.ANSIBLE_COMMAND);

        // Always ignore system wide SSH configuration:
        ansibleCommand.add(String.format("--ssh-common-args=-F %1$s/.ssh/config", config.getVarDir()));

        if (ansibleCommandConfig.verboseLevel().ordinal() > 0) {
            ansibleCommand.add(
                    "-" + IntStream.range(0, ansibleCommandConfig.verboseLevel().ordinal())
                            .mapToObj(i -> "v")
                            .collect(Collectors.joining()));
        }

        if (ansibleCommandConfig.isCheckMode()) {
            ansibleCommand.add("--check");
        }

        if (ansibleCommandConfig.privateKey() != null) {
            ansibleCommand.add(String.format("--private-key=%1$s", ansibleCommandConfig.privateKey()));
        }

        if (inventoryFile != null) {
            ansibleCommand.add(String.format("--inventory=%1$s", inventoryFile));
        }

        if (ansibleCommandConfig.limit() != null) {
            ansibleCommand.add(String.format("--limit=%1$s", ansibleCommandConfig.limit()));
        }

        ansibleCommandConfig.variables()
                .entrySet()
                .stream()
                .map(e -> String.format("--extra-vars=%1$s=\"%2$s\"", e.getKey(), e.getValue()))
                .forEach(ansibleCommand::add);

        if (ansibleCommandConfig.variableFilePath() != null) {
            ansibleCommand.add(String.format("--extra-vars=@%s", ansibleCommandConfig.variableFilePath()));
        }

        ansibleCommand.add(ansibleCommandConfig.playbook());

        return ansibleCommand;
    }
}

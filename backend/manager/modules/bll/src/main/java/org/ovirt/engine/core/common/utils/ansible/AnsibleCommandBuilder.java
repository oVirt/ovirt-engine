/*
Copyright (c) 2017 Red Hat, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.ovirt.engine.core.common.utils.ansible;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.utils.EngineLocalConfig;

/**
 * AnsibleCommandBuilder creates a ansible-playbook command.
 *
 * By default:
 * 1) We don't use any cluster.
 * 2) We don't use verbose mode.
 * 3) Playbook directory is $PREFIX/usr/share/ovirt-ansible-roles/playbooks
 * 4) Private key used is $PREFIX/etc/pki/ovirt-engine/keys/engine_id_rsa
 * 5) Log file is $PREFIX/var/log/ovirt-engine/ansible/{prefix}-{playbook-name}-{timestamp}.log
 * 6) Default inventory file is used.
 */
public class AnsibleCommandBuilder {

    public static final String ANSIBLE_COMMAND = "/usr/bin/ansible-playbook";

    private AnsibleVerbosity verboseLevel;
    private Path privateKey;
    private String cluster;
    private List<String> hostnames;
    private List<String> variables;
    private String limit;
    private Path inventoryFile;
    private String playbook;
    private boolean checkMode;
    private boolean enableLogging;

    // Logging:
    private File logFile;
    private String logFileDirectory;
    private String logFilePrefix;
    private String logFileSuffix;
    private String logFileName;

    private EngineLocalConfig config;
    private Path playbookDir;

    public AnsibleCommandBuilder() {
        cluster = "unspecified";
        enableLogging = true;
        verboseLevel = AnsibleVerbosity.LEVEL0;
        config = EngineLocalConfig.getInstance();
        playbookDir = Paths.get(config.getUsrDir().getPath(), "..", "ovirt-ansible-roles", "playbooks");
        privateKey = Paths.get(config.getPKIDir().getPath(), "keys", "engine_id_rsa");
    }

    public AnsibleCommandBuilder checkMode(boolean checkMode) {
        this.checkMode = checkMode;
        return this;
    }

    public AnsibleCommandBuilder verboseLevel(AnsibleVerbosity verboseLevel) {
        this.verboseLevel = verboseLevel;
        return this;
    }

    public AnsibleCommandBuilder privateKey(Path privateKey) {
        this.privateKey = privateKey;
        return this;
    }

    public AnsibleCommandBuilder inventoryFile(Path inventoryFile) {
        this.inventoryFile = inventoryFile;
        return this;
    }

    public AnsibleCommandBuilder cluster(String cluster) {
        this.cluster = cluster;
        return this;
    }

    public AnsibleCommandBuilder hostnames(String ... hostnames) {
        this.hostnames = Arrays.asList(hostnames);
        return this;
    }

    public AnsibleCommandBuilder variables(Pair<String, Object>... variables) {
        this.variables = Arrays.stream(variables)
            .map(p -> String.format("%1$s=%2$s", p.getFirst(), p.getSecond()))
            .collect(Collectors.toList());
        return this;
    }

    public AnsibleCommandBuilder limit(String limit) {
        this.limit = limit;
        return this;
    }

    public AnsibleCommandBuilder logFileDirectory(String logFileDirectory) {
        this.logFileDirectory = logFileDirectory;
        return this;
    }

    public AnsibleCommandBuilder logFileName(String logFileName) {
        this.logFileName = logFileName;
        return this;
    }

    public AnsibleCommandBuilder logFilePrefix(String logFilePrefix) {
        this.logFilePrefix = logFilePrefix;
        return this;
    }

    public AnsibleCommandBuilder logFileSuffix(String logFileSuffix) {
        this.logFileSuffix = logFileSuffix;
        return this;
    }

    public AnsibleCommandBuilder playbook(String playbook) {
        this.playbook = Paths.get(playbookDir.toString(), playbook).toString();
        return this;
    }

    public String playbook() {
        return playbook;
    }

    public File logFile() {
        return logFile;
    }

    public AnsibleCommandBuilder enableLogging(boolean enableLogging) {
        this.enableLogging = enableLogging;
        return this;
    }

    public Path inventoryFile() {
        return inventoryFile;
    }

    public Path playbookDir() {
        return playbookDir;
    }

    public List<String> hostnames() {
        return hostnames;
    }

    public String cluster() {
        return cluster;
    }

    public Path privateKey() {
        return privateKey;
    }

    public String logFileDirectory() {
        return logFileDirectory;
    }

    public String logFileName() {
        return logFileName;
    }

    public String logFilePrefix() {
        return logFilePrefix;
    }

    public String logFileSuffix() {
        return logFileSuffix;
    }

    public boolean enableLogging() {
        return enableLogging;
    }

    /**
     * The generated command will look like:
     *
     * /usr/bin/ansible-playbook -${verboseLevel} --private-key=${privateKey} --limit=${limit} \
     *    --extra-vars=${variables} ${playbook}
     *
     * The logFile is set up to:
     *
     *  /var/log/${logDirectory:ansible}/${logFilePrefix:""}-${logFileName:playbook}-${timestamp}-${logFileSuffix:""}
     */
    public List<String> build() {
        List<String> ansibleCommand = new ArrayList<>();
        ansibleCommand.add(ANSIBLE_COMMAND);

        if (verboseLevel.ordinal() > 0) {
            ansibleCommand.add(
                "-" + IntStream.range(0, verboseLevel.ordinal()).mapToObj(i -> "v").collect(Collectors.joining())
            );
        }

        if (checkMode) {
            ansibleCommand.add("--check");
        }

        if (privateKey != null) {
            ansibleCommand.add(String.format("--private-key=%1$s", privateKey));
        }

        if (inventoryFile != null) {
            ansibleCommand.add(String.format("--inventory=%1$s", inventoryFile));
        }

        if (limit != null) {
            ansibleCommand.add(String.format("--limit=%1$s", limit));
        }

        if (CollectionUtils.isNotEmpty(variables)) {
            variables.stream()
                .map(v -> String.format("--extra-vars=%1$s", v))
                .forEach(ansibleCommand::add);
        }

        if (logFile == null && enableLogging) {
            logFile = Paths.get(
                config.getLogDir().toString(),
                logFileDirectory != null ? logFileDirectory : AnsibleExecutor.DEFAULT_LOG_DIRECTORY,
                String.format(
                    "%1$s-%2$s-%3$s-%4$s.log",
                    logFilePrefix != null ? logFilePrefix : "ansible",
                    new SimpleDateFormat("yyyyMMddHHmmss").format(
                        Calendar.getInstance().getTime()
                    ),
                    logFileName != null ?
                        logFileName : playbook.substring(playbook.lastIndexOf('/') + 1).replace('.', '_'),
                    logFileSuffix != null ? logFileSuffix : "suffix"
                )
            ).toFile();
        }

        ansibleCommand.add(playbook);

        return ansibleCommand;
    }

    @Override
    public String toString() {
        return StringUtils.join(build(), " ");
    }
}

/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.core.common.utils.ansible;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.utils.CorrelationIdTracker;
import org.ovirt.engine.core.utils.EngineLocalConfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;

public class AnsibleCommandConfig implements LogFileConfig, PlaybookConfig {

    public static final String ANSIBLE_COMMAND = "ansible-runner";
    public static final String ANSIBLE_EXECUTION_METHOD = "start";

    private String cluster;
    private VDS host;
    private Map<String, Object> variables;
    private String variableFilePath;
    private String limit;
    private String playbook;
    private boolean checkMode;
    private String correlationId;
    private String playAction;
    private Path inventoryFile;
    private UUID uuid;
    // Logging
    private String logFileDirectory;
    private String logFileName;
    private String logFilePrefix;
    private String logFileSuffix;
    private ObjectMapper mapper;

    public AnsibleCommandConfig() {
        this.playAction = "Ansible Runner.";
        this.variables = new HashMap<>();
        // By default we want to pass correlationId to Ansible playbook to allow tracking of the whole process
        this.correlationId = CorrelationIdTracker.getCorrelationId();
        this.logFileSuffix = this.correlationId;
        this.mapper = JsonMapper
                .builder()
                .findAndAddModules()
                .build()
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        this.uuid = UUID.randomUUID();
    }

    public void setUUID(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public AnsibleCommandConfig inventoryFile(Path inventoryFile) {
        this.inventoryFile = inventoryFile;
        return this;
    }

    public Path getInventoryFile() {
        return this.inventoryFile;
    }

    @Override
    public String logFileDirectory() {
        return logFileDirectory;
    }

    @Override
    public String logFileName() {
        return logFileName;
    }

    @Override
    public String logFilePrefix() {
        return logFilePrefix;
    }

    @Override
    public String logFileSuffix() {
        return logFileSuffix;
    }

    public String playbook() {
        return playbook;
    }

    public String cluster() {
        return cluster;
    }

    public boolean isCheckMode() {
        return checkMode;
    }

    public String limit() {
        return limit;
    }

    public Map<String, Object> variables() {
        return shallowCopy(variables);
    }

    public String variableFilePath() {
        return variableFilePath;
    }

    public String correlationId() {
        return correlationId;
    }

    public String playAction() {
        return playAction;
    }

    public VDS host() {
        return this.host;
    }

    /**
     * @param originalList
     *            nullable allowed
     * @param <T>
     *            list item type
     * @return shallow copy of List
     */
    private <T> List<T> shallowCopy(List<T> originalList) {
        if (originalList == null) {
            return null;
        }
        final List<T> copy = new ArrayList<>(originalList.size());
        copy.addAll(originalList);
        return copy;

    }

    /**
     * @param originalMap
     *            nullable allowed
     * @param <K>
     *            map's key type
     * @param <V>
     *            map's value type
     * @return shallow copy of List
     */
    private <K, V> Map<K, V> shallowCopy(Map<K, V> originalMap) {
        if (originalMap == null) {
            return null;
        }
        return new HashMap<K, V>(originalMap);
    }

    public AnsibleCommandConfig checkMode(boolean checkMode) {
        this.checkMode = checkMode;
        return this;
    }

    public AnsibleCommandConfig cluster(String cluster) {
        this.cluster = cluster;
        return this;
    }

    public AnsibleCommandConfig host(VDS host) {
        this.host = host;
        return this;
    }

    public AnsibleCommandConfig variable(String name, Object value) {
        this.variables.put(name, value);
        return this;
    }

    public AnsibleCommandConfig limit(String limit) {
        this.limit = limit;
        return this;
    }

    public AnsibleCommandConfig playbook(String playbook) {
        this.playbook = playbook;
        return this;
    }

    public AnsibleCommandConfig variableFilePath(String variableFilePath) {
        this.variableFilePath = variableFilePath;
        return this;
    }

    public AnsibleCommandConfig correlationId(String correlationId) {
        this.correlationId = correlationId;
        return this;
    }

    public AnsibleCommandConfig withoutCorrelationId() {
        if (correlationId != null) {
            // By default we set correlationId to logFileSuffix, so we should clear it when we clear correlationId
            if (correlationId.equals(logFileSuffix)) {
                this.logFileSuffix = null;
            }
            correlationId = null;
        }
        return this;
    }

    public AnsibleCommandConfig playAction(String playAction) {
        this.playAction = playAction;
        return this;
    }

    public AnsibleCommandConfig logFileDirectory(String logFileDirectory) {
        this.logFileDirectory = logFileDirectory;
        return this;
    }

    public AnsibleCommandConfig logFileName(String logFilename) {
        this.logFileName = logFilename;
        return this;
    }

    public AnsibleCommandConfig logFilePrefix(String logFilePrefix) {
        this.logFilePrefix = logFilePrefix;
        return this;
    }

    public AnsibleCommandConfig logFileSuffix(String logFileSuffix) {
        this.logFileSuffix = logFileSuffix;
        return this;
    }

    public List<String> build() {
        List<String> ansibleCommand = new ArrayList<>();
        String artifactsDir = String.format("%1$s/%2$s/artifacts/", AnsibleConstants.ANSIBLE_RUNNER_PATH, this.uuid);
        String projectDir = String.format("%1$s/project", AnsibleConstants.RUNNER_SERVICE_PROJECT_PATH);
        try {
            File ansibleRunnerExecutionDir = setupAnsibleRunnerExecutionDir();
            ansibleCommand.add(ANSIBLE_COMMAND);
            ansibleCommand.add(ANSIBLE_EXECUTION_METHOD);
            ansibleCommand.add(ansibleRunnerExecutionDir.toString());
            ansibleCommand.add("-p");
            ansibleCommand.add(playbook);
            ansibleCommand.add("--project-dir");
            ansibleCommand.add(projectDir);
            ansibleCommand.add("--artifact-dir");
            ansibleCommand.add(artifactsDir);
            ansibleCommand.add("-i");
            ansibleCommand.add(String.valueOf(this.uuid));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return ansibleCommand;
    }

    private File setupAnsibleRunnerExecutionDir() throws IOException {
        File privateDataDir = new File(String.format("%1$s/%2$s/", AnsibleConstants.ANSIBLE_RUNNER_PATH, this.uuid));
        privateDataDir.mkdirs();

        File env = new File(String.format("%1$s/%2$s/env", AnsibleConstants.ANSIBLE_RUNNER_PATH, this.uuid));
        env.mkdir();

        // Create a link to engine private ssh key
        Path engineSshKey = Paths.get(
            EngineLocalConfig.getInstance().getPKIDir().toString(),
            "/keys/engine_id_rsa");
        Path linkToEngineSshKey = Paths.get(
            privateDataDir.toString(),
            "/env/ssh_key");
        Files.createSymbolicLink(linkToEngineSshKey, engineSshKey);

        // Create a variable file in project env/extravars, which will be passed to the playbook by ansible-runner.
        createExtraVarsFile(env);

        File inventory = new File(String.format("%1$s/%2$s/inventory", AnsibleConstants.ANSIBLE_RUNNER_PATH, this.uuid));
        inventory.mkdir();

        // Create a host inventory file in project inventory/host, which will be passed to the playbook by ansible-runner.
        createHostFile(inventory);

        return privateDataDir;
    }

    private void createHostFile(File inventory) {
        File hostFile = new File(String.format("%1$s/hosts", inventory));
        try {
            hostFile.createNewFile();
            if (host != null) {
                // if VDS is null then playbook is running on engine and ansible uses localhost inventory automatically
                Files.write(hostFile.toPath(),
                        String.format(this.host.getHostName() + " ansible_port=%1$s", this.host.getSshPort()).getBytes());
            }
        } catch (IOException ex) {
            throw new AnsibleRunnerCallException(
                    String.format("Failed to create inventory file '%s':", hostFile.toString()),
                    ex);
        }
    }

    private void createExtraVarsFile(File env) {
        String vars = getFormattedVariables();
        File extraVars = new File(String.format("%1$s/extravars", env));
        try {
            extraVars.createNewFile();
            Files.writeString(extraVars.toPath(), vars);
        } catch (IOException ex) {
            throw new AnsibleRunnerCallException(
                String.format("Failed to create host variables file '%s':", extraVars.toString()), ex);
        }
    }

    private String getFormattedVariables() {
        String result;
        try {
            result = this.mapper.writeValueAsString(this.variables);
        } catch (IOException ex) {
            throw new AnsibleRunnerCallException("Failed to create host deploy variables mapper", ex);
        }
        return result;
    }
}

/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.core.common.utils.ansible;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.utils.CorrelationIdTracker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;

public class AnsibleCommandConfig implements LogFileConfig, PlaybookConfig {

    public static final String ANSIBLE_COMMAND = "ansible-runner";
    public static final String ANSIBLE_EXECUTION_METHOD = "start";

    private String cluster;
    private List<String> hostnames;
    private List<VDS> hosts;
    private Map<String, Object> variables;
    private String variableFilePath;
    private String limit;
    private String playbook;
    private boolean checkMode;
    private String correlationId;
    private String playAction;
    private Path inventoryFile;
    private UUID uuid;
    private File extraVars;
    private Path specificPlaybook;
    private File hostDir;
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

    public List<String> hostnames() {
        return shallowCopy(hostnames);
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

    public List<VDS> hosts() {
        return hosts;
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

    public AnsibleCommandConfig hosts(VDS... hosts) {
        this.hostnames = Arrays.stream(hosts)
                .map(h -> h.getHostName())
                .collect(Collectors.toList());
        this.hosts = Arrays.asList(hosts);
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
        createTempVarsFile();
        createSpecificPlayBook();
        addExtraVarsToPlaybook();
        List<String> ansibleCommand = new ArrayList<>();
        String playbook = specificPlaybook.toString().substring(specificPlaybook.toString().lastIndexOf("/")+1);
        try {
            createInventoryFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
        ansibleCommand.add(ANSIBLE_COMMAND);
        ansibleCommand.add(ANSIBLE_EXECUTION_METHOD);
        ansibleCommand.add(AnsibleConstants.HOST_DEPLOY_PROJECT_DIR);
        ansibleCommand.add("-p");
        ansibleCommand.add(playbook);
        ansibleCommand.add("--artifact-dir");
        ansibleCommand.add(AnsibleConstants.ARTIFACTS_DIR);
        ansibleCommand.add("--inventory");
        ansibleCommand.add(String.valueOf(inventoryFile));
        ansibleCommand.add("-i");
        ansibleCommand.add(String.valueOf(uuid));
        return ansibleCommand;
    }

    private void createInventoryFile() {
        Path inventoryFile;
        if (getInventoryFile() == null) {
//             If hostnames are empty we just don't pass any inventory file:
            if (CollectionUtils.isNotEmpty(hostnames())) {
                try {
                    inventoryFile = Files.createTempFile("ansible-inventory", "");
                    Files.write(inventoryFile, StringUtils.join(hostnames(), System.lineSeparator()).getBytes());
                    inventoryFile(inventoryFile);
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //create temp extraVars file under play uuid path
    private void createTempVarsFile() {
        String vars = formatCommandVariables(variables);
        try {
            hostDir = new File(AnsibleConstants.EXTRA_VARS_DIR + uuid);
            hostDir.mkdir();
            extraVars = new File(hostDir + "/vars.yml");
            extraVars.createNewFile();
            Files.writeString(extraVars.toPath(), vars);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void createSpecificPlayBook() {
        Path orgPlaybook = Paths.get(AnsibleConstants.PROJECT_PATH + playbook);
        specificPlaybook = Paths.get(AnsibleConstants.HOST_DEPLOY_PROJECT_DIR + "/project/" + uuid + ".yml");
        try {
            Files.copy(orgPlaybook, specificPlaybook, StandardCopyOption.REPLACE_EXISTING);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void addExtraVarsToPlaybook() {
        try {
            List<String> lines = Files.readAllLines(specificPlaybook, StandardCharsets.UTF_8);
            int position = lines.indexOf("      include_vars:");
            if (position != -1) {
                lines.add(position + 1, "        file: " + "\"" + extraVars.getPath() + "\"");
                Files.write(specificPlaybook, lines, StandardCharsets.UTF_8);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }


    protected String formatCommandVariables(Map<String, Object> variables) {
        String result;
        try {
            result = mapper.writeValueAsString(variables);
        } catch (IOException ex) {
            throw new AnsibleRunnerCallException("Failed to create host deploy variables mapper", ex);
        }
        return result;
    }

    public void cleanup() {
        try {
            extraVars.delete();
            hostDir.delete();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}

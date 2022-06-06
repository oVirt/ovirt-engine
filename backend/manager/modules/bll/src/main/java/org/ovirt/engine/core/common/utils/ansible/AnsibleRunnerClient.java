/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.ovirt.engine.core.common.utils.ansible;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;

@Singleton
public class AnsibleRunnerClient {
    private static Logger log = LoggerFactory.getLogger(AnsibleRunnerClient.class);
    private ObjectMapper mapper;
    private AnsibleRunnerLogger runnerLogger;
    private JsonNode lastEvent;
    private static final int POLL_INTERVAL = 3000;
    private AnsibleReturnValue returnValue;

    public AnsibleRunnerClient() {
        this.mapper = JsonMapper
                .builder()
                .findAndAddModules()
                .build()
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        this.returnValue = new AnsibleReturnValue(AnsibleReturnCode.ERROR);
    }

    public Boolean playHasEnded() {
        if(this.lastEvent == null){
            return false;
        }
        return RunnerJsonNode.playbookStats(this.lastEvent);
    }

    public AnsibleReturnValue artifactHandler(UUID uuid, int timeout, BiConsumer<String, String> fn)
            throws Exception {
        int iteration = 0;
        int processRet = 0;
        this.setReturnValue(uuid);
        // retrieve timeout from engine constants.
        while (!this.playHasEnded()) {
            // Host is unreachable
            if (processRet == -1) {
                return returnValue;
            }
            if (iteration > timeout * 60) {
                // Cancel playbook, and raise exception in case timeout occur:
                this.cancelPlaybook(uuid, timeout);
                throw new TimeoutException(
                        "Play execution has reached timeout");
            }
            processRet = this.processEvents(uuid.toString(), fn);
            iteration += POLL_INTERVAL / 1000;
            Thread.sleep(POLL_INTERVAL);
        }
        this.returnValue.setAnsibleReturnCode(AnsibleReturnCode.OK);
        return returnValue;
    }

    public void setReturnValue(UUID uuid) {
        this.returnValue.setPlayUuid(uuid.toString());
        this.returnValue.setLogFile(this.runnerLogger.getLogFile());
    }

    public String getNextEvent(String jobEventsDir) {
        Optional<String> nextEvent = Optional.empty();
        // ignoring incompleted json files, add to list only events that haven't been handles yet.
        if (Files.exists(Paths.get(jobEventsDir))) {
            nextEvent = Stream.of(new File(jobEventsDir).listFiles())
                    .map(File::getName)
                    .filter(item -> !item.contains("partial"))
                    .filter(item -> !item.endsWith(".tmp"))
                    .filter(item -> item.startsWith((getLastEventId() + 1) + "-"))
                    .findFirst();
        }
        return nextEvent.isPresent() ? nextEvent.get() : null;
    }

    public int getLastEventId() {
        if(this.lastEvent == null){
            return 0;
        }
        return Integer.valueOf(RunnerJsonNode.playCounter(this.lastEvent));
    }

    public String getJobEventsDir(String playUuid) {
        return String.format("%1$s/%2$s/artifacts/%2$s/job_events/", AnsibleConstants.ANSIBLE_RUNNER_PATH, playUuid);
    }

    public int processEvents(
            String playUuid,
            BiConsumer<String, String> fn
        ) {
        String jobEventsDir = this.getJobEventsDir(playUuid);
        String nodeDir = this.getNextEvent(jobEventsDir);
        while(nodeDir != null){
            JsonNode currentNode = this.getEvent(jobEventsDir + nodeDir);
            String stdout = RunnerJsonNode.getStdout(currentNode);

            if (RunnerJsonNode.isEventUnreachable(currentNode)) {
                this.runnerLogger.log(currentNode);
                this.returnValue.setAnsibleReturnCode(AnsibleReturnCode.UNREACHABLE);
                return -1;
            }

            // might need special attention
            if (RunnerJsonNode.isEventVerbose(currentNode)) {
                if (!stdout.contains("Identity added")) {
                    this.runnerLogger.log(stdout);
                }
            }

            // want to log only these kind of events:
            if (RunnerJsonNode.isEventStart(currentNode) || RunnerJsonNode.isEventOk(currentNode)
                    || RunnerJsonNode.playbookStats(currentNode) || RunnerJsonNode.isEventFailed(currentNode)) {

                String taskName = "";
                JsonNode eventNode = currentNode.get("event_data");

                JsonNode taskNode = eventNode.get("task");
                if (taskNode != null) {
                    taskName = taskNode.textValue();
                }

                if (RunnerJsonNode.isEventStart(currentNode) || RunnerJsonNode.playbookStats(currentNode)) {
                    this.runnerLogger.log(stdout);
                }

                String action = "";
                JsonNode eventAction = eventNode.get("task_action");
                if (eventAction != null) {
                    action = eventAction.asText();
                }

                if (RunnerJsonNode.isEventOk(currentNode)) {
                    this.runnerLogger.log(currentNode);

                    String taskText = action.equals("debug")
                            ? RunnerJsonNode.formatDebugMessage(taskName, stdout)
                            : taskName;
                    fn.accept(taskText, String.format(jobEventsDir + nodeDir));

                } else if (RunnerJsonNode.isEventFailed(currentNode)) {
                    this.runnerLogger.log(currentNode);
                    if (!RunnerJsonNode.ignore(currentNode)) {
                        this.returnValue.setAnsibleReturnCode(AnsibleReturnCode.FAIL);
                        throw new AnsibleRunnerCallException(
                                String.format(
                                        "Task %1$s failed to execute. Please check logs for more details: %2$s",
                                        taskName,
                                        this.runnerLogger.getLogFile()));
                    }
                }
            }
            this.lastEvent = currentNode;
            this.returnValue.setLastEventId(this.getLastEventId());
            nodeDir = this.getNextEvent(jobEventsDir);
        }
        return this.getLastEventId();
    }

    private Boolean jsonIsValid(String content) {
        try {
            this.mapper.readTree(content);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public void cancelPlaybook(UUID uuid, int timeout) throws Exception {
        File privateDataDir = new File(String.format("%1$s/%2$s/", AnsibleConstants.ANSIBLE_RUNNER_PATH, uuid));
        File output = new File(String.format("%1$s/engine-cancel-output.log", privateDataDir));
        String command = String.format("ansible-runner stop %1$s", privateDataDir);
        ProcessBuilder ansibleProcessBuilder = new ProcessBuilder(command).redirectErrorStream(true).redirectOutput(output);
        Process ansibleProcess;
        try {
            ansibleProcess = ansibleProcessBuilder.start();
            // if play execution was already completed, the command fails.
        } catch (IOException e) {
            log.error(String.format("Failed to execute call to cancel playbook. %1$s, %2$s ",
                    output.toString(), Paths.get(this.getJobEventsDir(uuid.toString()) + "../stdout")));
            return;
        }
        if (!ansibleProcess.waitFor(timeout, TimeUnit.MINUTES)) {
            throw new Exception("Timeout occurred while canceling Ansible playbook.");
        }
        if (ansibleProcess.exitValue() != 0) {
            throw new AnsibleRunnerCallException(
                    "Failed to execute call to cancel playbook. %1$s",
                    output.toString());
        }
    }

    public void runPlaybook(List<String> command, int timeout, String uuid) throws Exception {
        File output = new File(String.format("%1$s/%2$s/engine-start-output.log", AnsibleConstants.ANSIBLE_RUNNER_PATH, uuid));
        ProcessBuilder ansibleProcessBuilder =
                new ProcessBuilder(command).redirectErrorStream(true).redirectOutput(output);
        Process ansibleProcess = ansibleProcessBuilder.start();
        String playCommand = String.join(" ", command);
        log.debug(String.format("%1$s started executing command %2$s", Thread.currentThread().getName(), playCommand));
        if (!ansibleProcess.waitFor(timeout, TimeUnit.MINUTES)) {
            throw new AnsibleRunnerCallException("Timeout occurred while executing Ansible playbook.");
        }
        if (ansibleProcess.exitValue() != 0) {
            String errorOutput = null;
            try {
                errorOutput = Files.readString(output.toPath());
            } catch (IOException ex) {
                log.error("Error reading output from ansible-runner execution: {}", ex.getMessage());
                log.debug("Exception", ex);
            }
            throw new AnsibleRunnerCallException(
                    "Failed to execute call to start playbook. %1$s",
                    errorOutput);
        }
    }

    public String formatCommandVariables(Map<String, Object> variables, String playAction) {
        String result;
        try {
            result = this.mapper.writeValueAsString(variables);
        } catch (IOException ex) {
            throw new AnsibleRunnerCallException("Failed to create host deploy variables mapper", ex);
        }
        return result;
    }

    public PlaybookStatus getPlaybookStatus(String playUuid) {
        String status = "";
        String rc = "";
        String playData = String.format("%1$s/%2$s/artifacts/%2$s/", AnsibleConstants.ANSIBLE_RUNNER_PATH, playUuid);
        try {
            if (!Files.exists(Paths.get(String.format("%1$s/status", playData)))) {
                // artifacts are not yet present, try to fetch them in the next polling round
                return new PlaybookStatus("unknown", "");
            }
            status = Files.readString(Paths.get(String.format("%1$s/status", playData)));
            rc = Files.readString(Paths.get(String.format("%1$s/rc", playData)));
        } catch (Exception e) {
            throw new AnsibleRunnerCallException(
                String.format("Failed to read playbook result at: %1$s", playData), e);
        }
        return new PlaybookStatus(rc, status);
    }

    private List<String> getEvents(String playUuid) {
        List<String> sortedEvents = new ArrayList<>();
        File jobEvents = new File(this.getJobEventsDir(playUuid));
        if (jobEvents.exists()) {
            sortedEvents = Stream.of(jobEvents.listFiles())
                .map(File::getName)
                .filter(item -> !item.contains("partial"))
                .filter(item -> !item.endsWith(".tmp"))
                .sorted()
                .collect(Collectors.toList());
        }
        return sortedEvents;
    }

    public int getTotalEvents(String playUuid) {
        List<String> events = this.getEvents(playUuid);
        // if playbook artifacts directory is not yet populated, return 0
        return events.size();
    }

    private JsonNode getEvent(String eventPath) {
        // Fetch the event info:
        JsonNode currentNode = null;
        try {
            String jsonOutput = Files.readString(Paths.get(eventPath), StandardCharsets.UTF_8);
            if (!this.jsonIsValid(jsonOutput)) {
                throw new AnsibleRunnerCallException(
                        "Failed to fetch info about event: %1$s",
                        eventPath
                );
            }
            currentNode = this.mapper.readTree(jsonOutput);
        } catch(IOException ex) {
            throw new AnsibleRunnerCallException("Failed to read event: %1$s", eventPath);
        }
        return currentNode;
    }

    public String getVdsmId(String eventUrl) {
        // Fetch the event info:
        JsonNode event = this.getEvent(eventUrl);

        // Parse the output of the events info:
        JsonNode taskNode = RunnerJsonNode.taskNode(event);
        return RunnerJsonNode.content(taskNode);
    }

    public Set<String> getYumPackages(String eventUrl) {
        // Fetch the event info:
        JsonNode event = this.getEvent(eventUrl);

        // Parse the output of the events info:
        JsonNode taskNode = RunnerJsonNode.taskNode(event);
        Set<String> packages = new HashSet<>();
        if (RunnerJsonNode.hasUpdates(taskNode)) {
            packages = RunnerJsonNode.getPackages(taskNode);
        }

        if (!packages.isEmpty()) {
            log.info("Found updates of packages: {}", StringUtils.join(packages, ","));
        }

        return packages;

    }

    public String getCommandStdout(String eventUrl) {
        JsonNode event = this.getEvent(eventUrl);
        JsonNode taskNode = RunnerJsonNode.taskNode(event);
        return RunnerJsonNode.getStdout(taskNode);
    }

    public void setLogger(AnsibleRunnerLogger runnerLogger) {
        this.runnerLogger = runnerLogger;
    }

    public AnsibleRunnerLogger getLogger() {
        return this.runnerLogger;
    }

    public static class PlaybookStatus {
        private String status;
        private String msg;

        public PlaybookStatus(String status, String msg) {
            this.status = status;
            this.msg = msg;
        }

        public String getMsg() {
            return msg;
        }

        public String getStatus() {
            return status;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            PlaybookStatus that = (PlaybookStatus) o;
            return Objects.equals(status, that.status) &&
                    Objects.equals(msg, that.msg);
        }

        @Override
        public int hashCode() {
            return Objects.hash(status, msg);
        }

        @Override
        public String toString() {
            return String.format("Playbook status: '%1$s' message: '%2$s'", this.status, this.msg);
        }
    }
}

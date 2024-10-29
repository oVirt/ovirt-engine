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
import java.util.Set;
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
    private static final int POLL_INTERVAL = 3000;

    public AnsibleRunnerClient() {
        this.mapper = JsonMapper
                .builder()
                .findAndAddModules()
                .build()
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    public Boolean playHasEnded(String uuid, int lastEventId) {
        // get current status, but new events might have appeared since last processing
        PlaybookStatus currentPlaybookStatus = getPlaybookStatus(uuid);
        String msg = currentPlaybookStatus.getMsg();
        return !msg.equalsIgnoreCase("running") && !(lastEventId < getTotalEvents(uuid));
    }

    public void artifactHandler(AnsibleReturnValue returnValue,
            int timeout,
            BiConsumer<String, String> fn,
            AnsibleRunnerLogger runnerLogger) throws Exception {
        int executionTime = 0;
        while (!playHasEnded(returnValue.getPlayUuid(), returnValue.getLastEventId())) {
            processEvents(returnValue, fn, runnerLogger);
            if (returnValue.getLastEventId() == -1) {
                return;
            }
            executionTime += POLL_INTERVAL / 1000;
            if (executionTime > timeout * 60) {
                // Cancel playbook, and raise exception in case timeout occur:
                cancelPlaybook(returnValue.getPlayUuid(), timeout);
                throw new TimeoutException(
                        "Play execution has reached timeout");
            }
            Thread.sleep(POLL_INTERVAL);
        }
        returnValue.setAnsibleReturnCode(AnsibleReturnCode.OK);
    }

    public String getEventFileName(String playUuid, int eventId) {
        String jobEvents = getJobEventsDir(playUuid);
        if (!Files.exists(Paths.get(jobEvents))) {
            return null;
        }
        // ignoring incompleted json files, add to list only events that haven't been handles yet.
        return Stream.of(new File(jobEvents).listFiles())
                .map(File::getName)
                .filter(item -> !item.contains("partial"))
                .filter(item -> !item.endsWith(".tmp"))
                .filter(item -> item.startsWith(eventId + "-"))
                .findFirst()
                .orElse(null);
    }

    public String getJobEventsDir(String playUuid) {
        return String.format("%1$s/%2$s/artifacts/%2$s/job_events/", AnsibleConstants.ANSIBLE_RUNNER_PATH, playUuid);
    }

    public void processEvents(AnsibleReturnValue returnValue,
            BiConsumer<String, String> fn,
            AnsibleRunnerLogger runnerLogger) {
        String jobEvents = getJobEventsDir(returnValue.getPlayUuid());
        while (true) {
            // get next event
            String event = getEventFileName(returnValue.getPlayUuid(), returnValue.getLastEventId() + 1);
            if (event == null) {
                break;
            }

            JsonNode currentNode = getEvent(jobEvents + event);
            String stdout = RunnerJsonNode.getStdout(currentNode);

            if (RunnerJsonNode.isEventUnreachable(currentNode)) {
                runnerLogger.log(currentNode);
                returnValue.setAnsibleReturnCode(AnsibleReturnCode.UNREACHABLE);
                returnValue.setLastEventId(-1);
                break;
            }

            // might need special attention
            if (RunnerJsonNode.isEventVerbose(currentNode)) {
                if (!stdout.contains("Identity added")) {
                    runnerLogger.log(stdout);
                }
            }

            log.debug("Current node event: {} lastEventId: {} ", currentNode.get("event").textValue(), returnValue.getLastEventId());
            // want to log only these kind of events:
            if (RunnerJsonNode.isEventStart(currentNode) || RunnerJsonNode.isEventOk(currentNode)
                    || RunnerJsonNode.playbookStats(currentNode) || RunnerJsonNode.isEventFailed(currentNode)) {

                String taskName = "";
                JsonNode eventNode = currentNode.get("event_data");
                if (eventNode != null) {
                    JsonNode taskNode = eventNode.get("task");
                    if (taskNode != null) {
                        taskName = taskNode.textValue();
                    }
                }

                if (RunnerJsonNode.isEventStart(currentNode) || RunnerJsonNode.playbookStats(currentNode)) {
                    runnerLogger.log(stdout);
                }

                String action = "";
                JsonNode eventAction = eventNode.get("task_action");
                if (eventAction != null) {
                    action = eventAction.asText();
                }

                if (RunnerJsonNode.isEventOk(currentNode)) {
                    runnerLogger.log(currentNode);

                    String taskText = action.equals("debug")
                            ? RunnerJsonNode.formatDebugMessage(taskName, stdout)
                            : taskName;
                    fn.accept(taskText, String.format(jobEvents + event));

                } else if (RunnerJsonNode.isEventFailed(currentNode)) {
                    runnerLogger.log(currentNode);
                    if (!RunnerJsonNode.ignore(currentNode)) {
                        returnValue.setAnsibleReturnCode(AnsibleReturnCode.FAIL);
                        throw new AnsibleRunnerCallException(
                                String.format(
                                        "Task %1$s failed to execute. Please check logs for more details: %2$s",
                                        taskName,
                                        runnerLogger.getLogFile()));
                    }
                }
            }
            returnValue.setLastEventId(Integer.valueOf(event.split("-")[0]));
        }
    }

    private Boolean jsonIsValid(String content) {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            mapper.readTree(content);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public void cancelPlaybook(String uuid, int timeout) throws Exception {
        File privateDataDir = new File(String.format("%1$s/%2$s/", AnsibleConstants.ANSIBLE_RUNNER_PATH, uuid));
        File output = new File(String.format("%1$s/output.log", privateDataDir));
        String command = String.format("ansible-runner stop %1$s", privateDataDir);
        Process ansibleProcess;
        ProcessBuilder ansibleProcessBuilder = new ProcessBuilder(command).redirectErrorStream(true).redirectOutput(output);
        try {
            ansibleProcess = ansibleProcessBuilder.start();
            // if play execution was already completed, the command fails.
        } catch (IOException e) {
            log.error(String.format("Failed to execute call to cancel playbook. %1$s, %2$s../stdout ",
                    output.toString(),
                    Paths.get(this.getJobEventsDir(uuid))));
            log.debug("Exception: ", e);
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
        File output = new File(String.format("%1$s/%2$s/output.log", AnsibleConstants.ANSIBLE_RUNNER_PATH, uuid));
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

    protected String formatCommandVariables(Map<String, Object> variables, String playAction) {
        String result;
        try {
            result = mapper.writeValueAsString(variables);
        } catch (IOException ex) {
            throw new AnsibleRunnerCallException("Failed to create host deploy variables mapper", ex);
        }
        return result;
    }

    public PlaybookStatus getPlaybookStatus(String playUuid) {
        String status = "";
        String rc = "";
        String privateRunDir = String.format("%1$s/%2$s/", AnsibleConstants.ANSIBLE_RUNNER_PATH, playUuid);
        String playData = String.format("%1$s/%2$s/artifacts/%2$s/", AnsibleConstants.ANSIBLE_RUNNER_PATH, playUuid);
        try {
            // regardless if we run sync or async playbook we launch ansible-runner the same way,
            // "status" and "rc" are created only once the playbook finishes
            if (Files.exists(Paths.get(String.format("%1$s/status", playData)))) {
                status = Files.readString(Paths.get(String.format("%1$s/status", playData)));
                rc = Files.readString(Paths.get(String.format("%1$s/rc", playData)));
            // we call ansible-runner synchronously and "damon.log" is crated by the main process before
            // going to background. So we can rely on its existence if ansible managed to start and still runs
            } else if (Files.exists(Paths.get(String.format("%1$s/daemon.log", privateRunDir)))) {
                // artifacts are not yet present, try to fetch them in the next polling round
                return new PlaybookStatus("", "running");
            // ansible-runner didn't manage to start the background process so we know it failed right away
            } else {
                log.warn(String.format("The playbook failed with unknow error at: %1$s", playData));
                return new PlaybookStatus("", "failed");
            }
        } catch (Exception e) {
            throw new AnsibleRunnerCallException(
                String.format("Failed to read playbook result at: %1$s", playData), e);
        }
        return new PlaybookStatus(rc, status);
    }

    private List<String> getEvents(String playUuid) {
        List<String> sortedEvents = new ArrayList<>();
        File jobEvents = new File(getJobEventsDir(playUuid));
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
        List<String> events = getEvents(playUuid);
        // if playbook artifacts directory is not yet populated, return 0
        return events.size();
    }

    private JsonNode getEvent(String eventPath) {
        // Fetch the event info:
        JsonNode currentNode = null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            String jsonOutput = Files.readString(Paths.get(eventPath), StandardCharsets.UTF_8);
            if (!jsonIsValid(jsonOutput)) {
                throw new AnsibleRunnerCallException(
                        "Failed to fetch info about event: %1$s",
                        eventPath
                );
            }
            currentNode = mapper.readTree(jsonOutput);
        } catch (Exception ex) {
            throw new AnsibleRunnerCallException("Failed to read event: %1$s", eventPath);
        }
        return currentNode;
    }

    public String getVdsmId(String eventUrl) {
        // Fetch the event info:
        JsonNode event = getEvent(eventUrl);

        // Parse the output of the events info:
        JsonNode taskNode = RunnerJsonNode.taskNode(event);
        return RunnerJsonNode.content(taskNode);
    }

    public Set<String> getYumPackages(String eventUrl) {
        // Fetch the event info:
        JsonNode event = getEvent(eventUrl);

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
        JsonNode event = getEvent(eventUrl);
        JsonNode taskNode = RunnerJsonNode.taskNode(event);
        return RunnerJsonNode.getStdout(taskNode);
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

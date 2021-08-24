/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.ovirt.engine.core.common.utils.ansible;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;

@Singleton
public class AnsibleRunnerHttpClient {

    private static final Object inventoryLock = new Object();
    private static final Object executeLock = new Object();
    private static final String HOST_GROUP = "ovirt";
    private static final String API_VERSION = "/api/v1";

    private static Logger log = LoggerFactory.getLogger(AnsibleRunnerHttpClient.class);

    private ObjectMapper mapper;
    private HttpClient httpClient;
    private AnsibleRunnerLogger runnerLogger;

    public AnsibleRunnerHttpClient() {
        this.httpClient = HttpClientBuilder.create().build();
        this.mapper = JsonMapper
                .builder()
                .findAndAddModules()
                .build()
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    public void addHost(String hostname, int port) {
        if (existsHost(hostname)) {
            return;
        }

        URI uri = buildRunnerURI(
            String.format("hosts/%1$s/groups/%2$s", hostname, HOST_GROUP),
            new BasicNameValuePair("port", String.valueOf(port))
        );

        StringEntity entity = new StringEntity("{}", ContentType.APPLICATION_JSON);
        HttpPost request = new HttpPost(uri);
        request.setEntity(entity);

        // There is no synchronization in ansible-runner-service for accessing the inventory file,
        // so we need to synchronize it here:
        synchronized (inventoryLock) {
            HttpResponse response = execute(request);
            JsonNode node = readResponse(response);
            if (!RunnerJsonNode.isStatusOk(node)) {
                throw new InventoryException("Failed to add host to inventory: %1$s", RunnerJsonNode.msg(node));
            }
        }
    }

    public void cancelPlaybook(String playUuid) {
        URI uri = buildRunnerURI(String.format("playbooks/", playUuid));

        HttpDelete request = new HttpDelete(uri);
        HttpResponse response = execute(request);
        JsonNode node = readResponse(response);

        if (!RunnerJsonNode.isStatusOk(node)) {
            throw new PlaybookExecutionException("Failed to cancel playbook: %1$s", RunnerJsonNode.msg(node));
        }
    }

    public String runPlaybook(AnsibleCommandConfig command) {
        if (command.hosts() != null) {
            for (VDS host : command.hosts()) {
                addHost(host.getHostName(), host.getSshPort());
            }
        }

        URI uri = buildRunnerURI(
            String.format("playbooks/%1$s", command.playbook()),
            command.hostnames() == null ? null :
                new BasicNameValuePair("limit", StringUtils.join(command.hostnames(), ",")),
            new BasicNameValuePair("check", String.valueOf(command.isCheckMode()))
        );

        StringEntity entity = new StringEntity(formatCommandVariables(command.variables(), command.playAction()),
                ContentType.APPLICATION_JSON);

        HttpPost request = new HttpPost(uri);
        request.setEntity(entity);

        // We need to lock the call to execute the playbook, because all extravars/cmdline/settings of the playbook
        // are written to the local disk and in multiple parallel execution of playbooks are executed it may happen
        // that it execute the first playbook, store its data, and the second playbook is executed with cached data.
        // The removal of cached data happens at the begging of the playbook execution in runner-service, so we must
        // be sure to not execute that code in parallel.
        synchronized (executeLock) {
            HttpResponse response = execute(request);
            JsonNode node = readResponse(response);
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_ACCEPTED) {
                throw new AnsibleRunnerCallException(
                        "Failed to execute call to start playbook. %1$s", node.asText()
                );
            }
            return RunnerJsonNode.playUuid(node);
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
        URI statusUri = buildRunnerURI(String.format("playbooks/%1$s", playUuid));

        HttpGet statusReguest = new HttpGet(statusUri);
        HttpResponse statusResponse = execute(statusReguest);
        JsonNode statusNode = readResponse(statusResponse);

        String status = RunnerJsonNode.status(statusNode).toLowerCase();
        String msg = RunnerJsonNode.msg(statusNode).toLowerCase();
        return new PlaybookStatus(status, msg);
    }

    private JsonNode getEvents(String playUuid) {
        URI eventsUri = buildRunnerURI(String.format("jobs/%1$s/events", playUuid));

        HttpGet eventsRequest = new HttpGet(eventsUri);
        HttpResponse eventsResponse = execute(eventsRequest);
        JsonNode events = readResponse(eventsResponse);
        if (eventsResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            throw new AnsibleRunnerCallException(
                "Failed to fetch info playbook events. %1$s: %2$s",
                RunnerJsonNode.status(events),
                RunnerJsonNode.msg(events)
            );
        }
        return events;
    }

    public int getTotalEvents(String playUuid) {
        JsonNode responseNode = getEvents(playUuid);
        return RunnerJsonNode.totalEvents(responseNode);
    }

    public boolean isHostUnreachable(String playUuid){
        JsonNode events = getEvents(playUuid);
        Iterator<JsonNode> it = events.get("data").get("events").iterator();
        while (it.hasNext()) {
            if (RunnerJsonNode.isEventUnreachable(it.next())) {
                return true;
            }
        }
        return false;
    }

    public int processEvents(String playUuid, int lastEventId, BiConsumer<String, String> fn, String msg, Path logFile) {
        JsonNode responseNode = getEvents(playUuid);
        JsonNode eventNodes = RunnerJsonNode.eventNodes(responseNode);
        Set<String> events = sortedEvents(eventNodes.fieldNames(), lastEventId);
        String eventInfo = "";
        for (String event : events) {
            String task = null;
            JsonNode currentNode = eventNodes.get(event);
            JsonNode taskNode = currentNode.get("task");
            if (taskNode != null) {
                task = taskNode.textValue();
            }
            if (RunnerJsonNode.isEventStart(currentNode) || RunnerJsonNode.isEventOk(currentNode)
                    || RunnerJsonNode.playbookStats(currentNode) || RunnerJsonNode.isEventFailed(currentNode)
                    || RunnerJsonNode.isEventError(currentNode) || ("failed".equals(msg))
            ) {
                JsonNode okNode = readUrl(String.format("jobs/%1$s/events/%2$s", playUuid, event));
                JsonNode data = okNode.get("data");
                String action = "";
                JsonNode eventAction = data.get("event_data").get("task_action");
                if (eventAction != null) {
                    action = eventAction.asText();
                }

                // Log stdout:
                String stdout = RunnerJsonNode.getStdout(data);
                runnerLogger.log(stdout.trim());

                // Log stderr:
                String stderr = RunnerJsonNode.getStderr(data);
                runnerLogger.log(stderr);

                if (RunnerJsonNode.isEventOk(currentNode)) {
                    if (taskNode != null) {
                        String taskText = action.equals("debug")
                                ? RunnerJsonNode.formatDebugMessage(taskNode.textValue(), stdout)
                                : taskNode.textValue();
                        runnerLogger.log(readUrl(String.format("jobs/%1$s/events/%2$s", playUuid, event)));
                        fn.accept(taskText, String.format("jobs/%1$s/events/%2$s", playUuid, event));
                    }
                } else if (RunnerJsonNode.isEventFailed(currentNode) || RunnerJsonNode.isEventError(currentNode)) {
                    JsonNode eventNode = getEvent(String.format("jobs/%1$s/events/%2$s", playUuid, event));
                    if (!RunnerJsonNode.ignore(eventNode)) {
                        runnerLogger.log(readUrl(String.format("jobs/%1$s/events/%2$s", playUuid, event)));
                        throw new AnsibleRunnerCallException(
                                String.format("Task %1$s failed to execute. Please check logs for more details: %2$s", task, logFile)
                        );
                    }
                }
            }
            eventInfo = event;
        }
        return eventInfo.isEmpty() ? lastEventId : Integer.valueOf(eventInfo.split("-")[0]);
    }

    private SortedSet<String> sortedEvents(Iterator<String> it, int lastEventId) {
        // Ansible-runner return the events randomly so we need to sort them:
        SortedSet<String> set = new TreeSet<>(
            Comparator.comparing(s -> Integer.parseInt(s.substring(0, s.indexOf("-"))))
        );
        while (it.hasNext()) {
            String n = it.next();
            int index = Integer.parseInt(n.substring(0, n.indexOf("-")));
            if (index > lastEventId) {
                set.add(n);
            }
        }

        return set;
    }

    private JsonNode getEvent(String eventUrl) {
        // Fetch the event info:
        URI eventsUri = buildRunnerURI(eventUrl);
        HttpGet events = new HttpGet(eventsUri);
        HttpResponse statusResponse = execute(events);
        JsonNode event = readResponse(statusResponse);
        if (statusResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            throw new AnsibleRunnerCallException(
                    "Failed to fetch info about event. %1$s: %2$s",
                    RunnerJsonNode.status(event),
                    RunnerJsonNode.msg(event)
            );
        }

        return event;
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
        URI eventsUri = buildRunnerURI(eventUrl);
        HttpGet events = new HttpGet(eventsUri);
        HttpResponse statusResponse = execute(events);
        JsonNode event = readResponse(statusResponse);
        if (statusResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            throw new AnsibleRunnerCallException(
                "Failed to fetch info command task output'. %1$s: %2$s",
                RunnerJsonNode.status(event),
                RunnerJsonNode.msg(event)
            );
        }

        JsonNode taskNode = RunnerJsonNode.taskNode(event);
        return RunnerJsonNode.getStdout(taskNode);
    }

    public void setLogger(AnsibleRunnerLogger runnerLogger) {
        this.runnerLogger = runnerLogger;
    }

    public AnsibleRunnerLogger getLogger() {
        return this.runnerLogger;
    }

    private URIBuilder baseURI() {
        return new URIBuilder()
                .setScheme("http")
                .setHost("localhost")
                .setPort(50001);
    }

    private URI buildRunnerURI(String path) {
        return buildRunnerURI(path, null);
    }

    private URI buildRunnerURI(String path, NameValuePair... query) {
        StringBuilder v1path = new StringBuilder(String.format("%1$s/%2$s", API_VERSION, path));
        try {
            URIBuilder uri = baseURI()
                .setPath(v1path.toString());
            if (query != null && query.length > 0) {
                uri.addParameters(Arrays.asList(query).stream().filter(Objects::nonNull).collect(Collectors.toList()));
            }
            return uri.build();
        } catch (URISyntaxException ex) {
            throw new AnsibleRunnerCallException(String.format("Failed to build Ansible runner URI %1$s", v1path), ex);
        }
    }

    private JsonNode readResponse(HttpResponse response) {
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
            throw new AnsibleRunnerCallException("Internal server error");
        }

        try {
            HttpEntity httpEntity = response.getEntity();
            InputStream inputStream = httpEntity.getContent();
            return mapper.readTree(inputStream);
        } catch (IOException ex) {
            throw new AnsibleRunnerCallException("Failed to read the runner-service response.", ex);
        }
    }

    private HttpResponse execute(HttpUriRequest request) {
        try {
            return httpClient.execute(request);
        } catch (IOException ex) {
            throw new AnsibleRunnerCallException(
                String.format("Failed to execute call to ansible runner service: %1$s", request.getURI()), ex
            );
        }
    }

    private boolean existsHost(String hostname) {
        URI uri = buildRunnerURI(String.format("hosts/%1$s", hostname));
        HttpGet request = new HttpGet(uri);
        HttpResponse response = execute(request);
        JsonNode node = readResponse(response);

        return !RunnerJsonNode.isStatusNotFound(node);
    }

    private JsonNode readUrl(String url) {
        URI eventsUri = buildRunnerURI(url);
        HttpGet events = new HttpGet(eventsUri);
        HttpResponse statusResponse = execute(events);
        return readResponse(statusResponse);
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

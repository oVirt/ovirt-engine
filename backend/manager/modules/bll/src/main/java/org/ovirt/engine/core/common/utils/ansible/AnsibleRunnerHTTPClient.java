/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.ovirt.engine.core.common.utils.ansible;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class AnsibleRunnerHTTPClient {

    private static final String HOST_GROUP = "ovirt";
    private static Logger log = LoggerFactory.getLogger(AnsibleRunnerHTTPClient.class);

    private ObjectMapper mapper;
    private HttpClient httpClient;

    public AnsibleRunnerHTTPClient() {
        this.httpClient = HttpClientBuilder.create().build();
        this.mapper = new ObjectMapper();
    }

    private URIBuilder baseURI() {
        return new URIBuilder()
            .setScheme("http")
            .setHost("localhost")
            .setPort(5001);
    }

    private boolean existsHost(String hostname) {
        try {
            URI uri = baseURI()
                .setPath(String.format("/api/v1/hosts/%1$s", hostname))
                .build();
            HttpGet request = new HttpGet(uri);
            HttpResponse response = httpClient.execute(request);

            HttpEntity httpEntity = response.getEntity();
            InputStream inputStream = httpEntity.getContent();
            JsonNode node = mapper.readTree(inputStream);
            return !RunnerJsonNode.isStatusNotFound(node);
        } catch (URISyntaxException | IOException ex) {
            throw new AnsibleRunnerCallException("Failed to execute call to get host from inventory.", ex);
        }
    }

    public void addHost(String hostname, int port) {
        if (existsHost(hostname)) {
            return;
        }

        try {
            URI uri = baseURI()
                .setPath(String.format("/api/v1/hosts/%1$s/groups/%2$s?port=%3$s", hostname, HOST_GROUP, port))
                .build();

            StringEntity entity = new StringEntity("{}");
            HttpPost request = new HttpPost(uri);
            request.setEntity(entity);

            HttpResponse response = httpClient.execute(request);
            JsonNode node = mapper.readTree(response.getEntity().getContent());

            if (!RunnerJsonNode.isStatusOk(node)) {
                throw new InventoryException("Failed to add host to inventory: %1$s", node.get("msg").getTextValue());
            }

        } catch (URISyntaxException | IOException ex) {
            throw new AnsibleRunnerCallException("Failed to execute call to add host to inventory.", ex);
        }
    }

    public void cancelPlaybook(String playUuid) {
        try {
            URI uri = baseURI()
                .setPath(String.format("/api/v1/playbooks/", playUuid))
                .build();

            HttpDelete request = new HttpDelete(uri);

            HttpResponse response = httpClient.execute(request);
            JsonNode node = mapper.readTree(response.getEntity().getContent());

            if (!RunnerJsonNode.isStatusOk(node)) {
                throw new PlaybookExecutionException("Failed to cancel playbook: %1$s", node.get("msg").getTextValue());
            }

        } catch (URISyntaxException | IOException ex) {
            throw new AnsibleRunnerCallException("Failed to execute call to cancel playbook.", ex);
        }
    }

    public String runPlaybook(AnsibleCommandConfig command) {
        for (VDS host : command.hosts()) {
            addHost(host.getHostName(), host.getSshPort());
        }

        try {
            URI uri = baseURI()
                .setPath(String.format("/api/v1/playbooks/%1$s", command.playbook()))
                .addParameter("limit", StringUtils.join(command.hostnames(), ","))
                .addParameter("check", String.valueOf(command.isCheckMode()))
                .build();

            StringEntity entity = new StringEntity("{" +
                    command.variables().entrySet()
                            .stream()
                            .map(e -> String.format(
                                    "\"%1$s\": \"%2$s\"",
                                    e.getKey(),
                                    // Replace to have proper formatting of JSON newlines
                                    String.valueOf(e.getValue()).replaceAll("\n", "\\n")
                            ))
                            .collect(Collectors.joining(",")) +
                    "}", ContentType.APPLICATION_JSON);

            HttpPost request = new HttpPost(uri);
            request.setEntity(entity);

            HttpResponse response = httpClient.execute(request);
            JsonNode node = mapper.readTree(response.getEntity().getContent());
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_ACCEPTED) {
                throw new AnsibleRunnerCallException(
                    "Failed to execute call to start playbook. %1$s: %2$s",
                    RunnerJsonNode.status(node),
                    RunnerJsonNode.msg(node)
                );
            }
            return RunnerJsonNode.playUuid(node);

        } catch (URISyntaxException | IOException ex) {
            throw new AnsibleRunnerCallException("Failed to execute call to start playbook.", ex);
        }
    }

    public PlaybookStatus getPlaybookStatus(String playUuid) {
        try {
            URI statusUri = baseURI()
                .setPath(String.format("/api/v1/playbooks/%1$s", playUuid))
                .build();

            HttpGet statusReguest = new HttpGet(statusUri);
            HttpResponse statusResponse = httpClient.execute(statusReguest);
            JsonNode statusNode = mapper.readTree(statusResponse.getEntity().getContent());

            String status = RunnerJsonNode.status(statusNode).toLowerCase();
            String msg = RunnerJsonNode.msg(statusNode).toLowerCase();
            return new PlaybookStatus(status, msg);
        } catch (URISyntaxException | IOException ex) {
            throw new AnsibleRunnerCallException("Failed to execute call to playbook status.", ex);
        }
    }

    private JsonNode getEvents(String playUuid) {
        try {
            URI eventsUri = baseURI()
                .setPath(String.format("/api/v1/jobs/%1$s/events", playUuid))
                .build();

            HttpGet eventsRequest = new HttpGet(eventsUri);
            HttpResponse eventsResponse = httpClient.execute(eventsRequest);
            JsonNode events = mapper.readTree(eventsResponse.getEntity().getContent());
            if (eventsResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new AnsibleRunnerCallException(
                    "Failed to fetch info playbook events. %1$s: %2$s",
                    RunnerJsonNode.status(events),
                    RunnerJsonNode.msg(events)
                );
            }
            return events;
        } catch (URISyntaxException | IOException ex) {
            throw new AnsibleRunnerCallException("Failed to execute call to get events of playbook.", ex);
        }
    }

    public int getTotalEvents(String playUuid) {
        JsonNode responseNode = getEvents(playUuid);
        return RunnerJsonNode.totalEvents(responseNode);
    }

    public int processEvents(String playUuid, int lastEventId, BiConsumer<String, String> fn) {
        JsonNode responseNode = getEvents(playUuid);
        JsonNode eventNodes = RunnerJsonNode.eventNodes(responseNode);
        Iterator<String> iterator = eventNodes.getFieldNames();
        while (iterator.hasNext()) {
            String n = iterator.next();
            int index = Integer.parseInt(n.substring(0, n.indexOf("-")));
            if (index > lastEventId) {
                JsonNode currentNode = eventNodes.get(n);
                if (RunnerJsonNode.isEventOk(currentNode)) {
                    JsonNode taskNode = currentNode.get("task");
                    if (taskNode != null) {
                        fn.accept(taskNode.getTextValue(), String.format("/api/v1/jobs/%1$s/events/%2$s", playUuid, n));
                    }
                }
            }
        }

        return RunnerJsonNode.totalEvents(responseNode);
    }

    public List<String> getYumPackages(String eventUrl) {
        try {
            URI eventsUri = baseURI()
                .setPath(eventUrl)
                .build();

            HttpGet events = new HttpGet(eventsUri);
            HttpResponse statusResponse = httpClient.execute(events);
            JsonNode event = mapper.readTree(statusResponse.getEntity().getContent());
            if (statusResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new AnsibleRunnerCallException(
                    "Failed to fetch info about system packages. %1$s: %2$s",
                    RunnerJsonNode.status(event),
                    RunnerJsonNode.msg(event)
                );
            }

            JsonNode taskNode = RunnerJsonNode.taskNode(event);

            List<String> packages = new ArrayList<>();
            if (RunnerJsonNode.changed(taskNode)) {
                Iterator<JsonNode> nodes = RunnerJsonNode.installed(taskNode).getElements();
                while (nodes.hasNext()) {
                    packages.add(nodes.next().getElements().next().asText());
                }

                nodes = RunnerJsonNode.updated(taskNode).getElements();
                while (nodes.hasNext()) {
                    packages.add(nodes.next().getElements().next().asText());
                }
            }

            log.info("Found updates of packages: {}", StringUtils.join(packages, ","));

            return packages;

        } catch (URISyntaxException | IOException ex) {
            throw new AnsibleRunnerCallException("Failed to execute call to get event info.", ex);
        }
    }

    public String getCommandStdout(String eventUrl) {
        try {
            URI eventsUri = baseURI()
                    .setPath(eventUrl)
                    .build();

            HttpGet events = new HttpGet(eventsUri);
            HttpResponse statusResponse = httpClient.execute(events);
            JsonNode event = mapper.readTree(statusResponse.getEntity().getContent());
            if (statusResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new AnsibleRunnerCallException(
                        "Failed to fetch info about 'qemu-img measure'. %1$s: %2$s",
                        RunnerJsonNode.status(event),
                        RunnerJsonNode.msg(event)
                );
            }

            JsonNode taskNode = RunnerJsonNode.taskNode(event);
            return RunnerJsonNode.getStdout(taskNode);

        } catch (URISyntaxException | IOException ex) {
            throw new AnsibleRunnerCallException("Failed to execute call to get event info.", ex);
        }
    }

    static class PlaybookStatus {
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

package org.ovirt.engine.core.vdsbroker.jsonrpc;

import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;

import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.vdsm.jsonrpc.client.ClientConnectionException;
import org.ovirt.vdsm.jsonrpc.client.JsonRpcClient;
import org.ovirt.vdsm.jsonrpc.client.internal.ClientPolicy;
import org.ovirt.vdsm.jsonrpc.client.internal.ResponseWorker;
import org.ovirt.vdsm.jsonrpc.client.reactors.ManagerProvider;
import org.ovirt.vdsm.jsonrpc.client.reactors.Reactor;
import org.ovirt.vdsm.jsonrpc.client.reactors.ReactorClient;
import org.ovirt.vdsm.jsonrpc.client.reactors.ReactorFactory;
import org.ovirt.vdsm.jsonrpc.client.reactors.ReactorType;
import org.ovirt.vdsm.jsonrpc.client.reactors.stomp.StompClientPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonRpcUtils {
    private static Logger log = LoggerFactory.getLogger(JsonRpcUtils.class);
    private static final String identifierLogMessage = "Setting up connection policy identifier for host {}";

    public static JsonRpcClient createStompClient(String hostname,
            int port,
            int connectionTimeout,
            int clientTimeout,
            int connectionRetry,
            int heartbeat,
            boolean isSecure,
            String protocol,
            int parallelism,
            int eventTimeoutInHours,
            String requestQueue,
            String responseQueue,
            String eventQueue,
            ScheduledExecutorService executorService) {
        StompClientPolicy connectionPolicy =
                new StompClientPolicy(connectionTimeout,
                        connectionRetry,
                        heartbeat,
                        IOException.class,
                        requestQueue,
                        responseQueue);
        connectionPolicy.setEventQueue(eventQueue);

        ClientPolicy clientPolicy = new ClientPolicy(clientTimeout, connectionRetry, heartbeat, IOException.class);
        if (Config.getValue(ConfigValues.UseHostNameIdentifier)){
            log.debug(identifierLogMessage, hostname);
            connectionPolicy.setIdentifier(hostname);
        }
        return createClient(hostname, port, connectionPolicy, clientPolicy, isSecure, ReactorType.STOMP, protocol, parallelism, eventTimeoutInHours, executorService);
    }

    private static JsonRpcClient createClient(String hostname,
            int port,
            ClientPolicy connectionPolicy,
            ClientPolicy clientPolicy,
            boolean isSecure,
            ReactorType type,
            String protocol,
            int parallelism,
            int eventTimeoutInHours,
            ScheduledExecutorService executorService) {
        ManagerProvider provider = null;
        if (isSecure) {
            provider = new EngineManagerProvider(protocol);
        }
        try {
            final Reactor reactor = ReactorFactory.getReactor(provider, type);
            return getJsonClient(reactor, hostname, port, connectionPolicy, clientPolicy, parallelism, eventTimeoutInHours, executorService);
        } catch (ClientConnectionException e) {
            log.error("Exception occurred during building ssl context or obtaining selector for '{}': {}",
                    hostname,
                    e.getMessage());
            log.debug("Exception", e);
            throw new IllegalStateException(e);
        }
    }

    private static JsonRpcClient getJsonClient(Reactor reactor,
            String hostName,
            int port,
            ClientPolicy connectionPolicy,
            ClientPolicy clientPolicy,
            int parallelism,
            int eventTimeoutInHours,
            ScheduledExecutorService executorService) throws ClientConnectionException {
        final ReactorClient client = reactor.createClient(hostName, port);
        client.setClientPolicy(connectionPolicy);
        ResponseWorker worker = ReactorFactory.getWorker(parallelism, eventTimeoutInHours);
        JsonRpcClient jsonClient = worker.register(client);
        jsonClient.setRetryPolicy(clientPolicy);
        jsonClient.setExecutorService(executorService);
        return jsonClient;
    }
}

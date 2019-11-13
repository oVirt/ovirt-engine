package org.ovirt.engine.core.vdsbroker;

import java.util.concurrent.ScheduledExecutorService;

import org.apache.http.impl.client.CloseableHttpClient;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.vdsbroker.irsbroker.IIrsServer;
import org.ovirt.engine.core.vdsbroker.jsonrpc.JsonRpcIIrsServer;
import org.ovirt.engine.core.vdsbroker.jsonrpc.JsonRpcUtils;
import org.ovirt.engine.core.vdsbroker.jsonrpc.JsonRpcVdsServer;
import org.ovirt.engine.core.vdsbroker.vdsbroker.IVdsServer;

public class TransportFactory {
    public static IIrsServer createIrsServer(
            String hostname, int port, int clientTimeOut, int connectionTimeOut, int clientRetries, int heartbeat, ScheduledExecutorService executorService) {
        return new JsonRpcIIrsServer(
                JsonRpcUtils.createStompClient(
                        hostname,
                        port,
                        connectionTimeOut,
                        clientTimeOut,
                        clientRetries,
                        heartbeat,
                        Config.getValue(ConfigValues.EncryptHostCommunication),
                        Config.getValue(ConfigValues.VdsmSSLProtocol),
                        Config.getValue(ConfigValues.EventProcessingPoolSize),
                        Config.getValue(ConfigValues.EventPurgeTimeoutInHours),
                        Config.getValue(ConfigValues.IrsRequestQueueName),
                        Config.getValue(ConfigValues.IrsResponseQueueName),
                        null,
                        executorService));
    }

    public static IVdsServer createVdsServer(
            String hostname,
            int port,
            int clientTimeOut,
            int connectionTimeOut,
            int clientRetries,
            int heartbeat,
            ScheduledExecutorService executorService) {

        CloseableHttpClient client = HttpUtils.getConnection(
                connectionTimeOut,
                clientRetries,
                Config.getValue(ConfigValues.VdsMaxConnectionsPerHost),
                Config.getValue(ConfigValues.MaxTotalConnections));

        String eventQueue = Config.getValue(ConfigValues.EventQueueName);
        return new JsonRpcVdsServer(
                JsonRpcUtils.createStompClient(
                        hostname,
                        port,
                        connectionTimeOut,
                        clientTimeOut,
                        clientRetries,
                        heartbeat,
                        Config.getValue(ConfigValues.EncryptHostCommunication),
                        Config.getValue(ConfigValues.VdsmSSLProtocol),
                        Config.getValue(ConfigValues.EventProcessingPoolSize),
                        Config.getValue(ConfigValues.EventPurgeTimeoutInHours),
                        Config.getValue(ConfigValues.VdsRequestQueueName),
                        Config.getValue(ConfigValues.VdsResponseQueueName),
                        eventQueue,
                        executorService)
                , client);
    }
}

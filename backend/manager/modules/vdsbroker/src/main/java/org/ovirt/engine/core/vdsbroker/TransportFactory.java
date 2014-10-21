package org.ovirt.engine.core.vdsbroker;

import org.apache.commons.httpclient.HttpClient;
import org.ovirt.engine.core.common.businessentities.VdsProtocol;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.vdsbroker.irsbroker.IIrsServer;
import org.ovirt.engine.core.vdsbroker.irsbroker.IrsServerConnector;
import org.ovirt.engine.core.vdsbroker.irsbroker.IrsServerWrapper;
import org.ovirt.engine.core.vdsbroker.jsonrpc.JsonRpcIIrsServer;
import org.ovirt.engine.core.vdsbroker.jsonrpc.JsonRpcUtils;
import org.ovirt.engine.core.vdsbroker.jsonrpc.JsonRpcVdsServer;
import org.ovirt.engine.core.vdsbroker.vdsbroker.IVdsServer;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsServerConnector;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsServerWrapper;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcUtils;

public class TransportFactory {
    public static IIrsServer createIrsServer(VdsProtocol vdsProtocol, String hostname, int port, int clientTimeOut,
            int connectionTimeOut, int clientRetries, int heartbeat) {
        IIrsServer irsServer = null;
        if (VdsProtocol.STOMP == vdsProtocol) {
            irsServer = new JsonRpcIIrsServer(JsonRpcUtils.createStompClient(hostname,
                    port, connectionTimeOut, clientTimeOut, clientRetries, heartbeat,
                    Config.<Boolean> getValue(ConfigValues.EncryptHostCommunication)));
        } else if (VdsProtocol.XML == vdsProtocol){
            Pair<IrsServerConnector, HttpClient> returnValue =
                    XmlRpcUtils.getConnection(hostname, port, clientTimeOut, connectionTimeOut,
                            clientRetries, IrsServerConnector.class,
                            Config.<Boolean> getValue(ConfigValues.EncryptHostCommunication));
            irsServer = new IrsServerWrapper(returnValue.getFirst(), returnValue.getSecond());
        }
        return irsServer;
    }

    public static IVdsServer createVdsServer(VdsProtocol vdsProtocol, String hostname, int port, int clientTimeOut,
            int connectionTimeOut, int clientRetries, int heartbeat) {
        IVdsServer vdsServer = null;
        Pair<VdsServerConnector, HttpClient> returnValue =
                XmlRpcUtils.getConnection(hostname, port, clientTimeOut, connectionTimeOut,
                        clientRetries, VdsServerConnector.class,
                        Config.<Boolean> getValue(ConfigValues.EncryptHostCommunication));

        if (VdsProtocol.STOMP == vdsProtocol) {
            vdsServer = new JsonRpcVdsServer(JsonRpcUtils.createStompClient(hostname,
                    port, connectionTimeOut, clientTimeOut, clientRetries, heartbeat,
                    Config.<Boolean> getValue(ConfigValues.EncryptHostCommunication)), returnValue.getSecond());
        } else if (VdsProtocol.XML == vdsProtocol) {
            vdsServer = new VdsServerWrapper(returnValue.getFirst(), returnValue.getSecond());
        }
        return vdsServer;
    }
}

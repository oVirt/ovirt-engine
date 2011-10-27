package org.ovirt.engine.core.common.vdscommands;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.common.businessentities.storage_server_connections;
import org.ovirt.engine.core.compat.Guid;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "DiscoverSendTargetsVDSCommandParameters")
public class DiscoverSendTargetsVDSCommandParameters extends VdsIdVDSCommandParametersBase {
    public DiscoverSendTargetsVDSCommandParameters(Guid vdsId, storage_server_connections connection) {
        super(vdsId);
        setConnection(connection);
    }

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement(name = "Connection")
    private storage_server_connections privateConnection;

    public storage_server_connections getConnection() {
        return privateConnection;
    }

    private void setConnection(storage_server_connections value) {
        privateConnection = value;
    }

    public DiscoverSendTargetsVDSCommandParameters() {
    }

    @Override
    public String toString() {
        return String.format("%s, connection=%s", super.toString(), getConnection());
    }

}

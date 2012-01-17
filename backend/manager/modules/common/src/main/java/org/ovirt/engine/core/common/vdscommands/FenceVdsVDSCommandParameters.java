package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.businessentities.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "FenceVdsVDSCommandParameters")
public class FenceVdsVDSCommandParameters extends VdsIdVDSCommandParametersBase {
    public FenceVdsVDSCommandParameters(Guid vdsId, Guid targetVdsId, String ip, String port, String type, String user,
            String password, String options, FenceActionType action) {
        super(vdsId);
        _targetVdsId = targetVdsId;
        _ip = ip;
        _port = port;
        _type = type;
        _user = user;
        _password = password;
        _action = action;
        _options = options;
    }

    @XmlElement
    private Guid _targetVdsId;
    @XmlElement
    private String _ip;
    @XmlElement
    private String _port;
    @XmlElement
    private String _type;
    @XmlElement
    private String _user;
    @XmlElement
    private String _password;
    @XmlElement
    private String _options = "";
    @XmlElement
    private FenceActionType _action = FenceActionType.forValue(0);

    public Guid getTargetVdsID() {
        return _targetVdsId;
    }

    public String getIp() {
        return _ip;
    }

    public String getPort() {
        return _port;
    }

    public String getType() {
        return _type;
    }

    public String getUser() {
        return _user;
    }

    public String getPassword() {
        return _password;
    }

    public String getOptions() {
        return _options;
    }

    public FenceActionType getAction() {
        return _action;
    }

    public FenceVdsVDSCommandParameters() {
    }

    @Override
    public String toString() {
        return String.format("%s, targetVdsId = %s, action = %s, ip = %s, port = %s, type = %s, user = %s, " +
                "password = %s, options = '%s'", super.toString(), getTargetVdsID(), getAction(), getIp(), getPort(),
                getType(), getUser(), (getPassword() == null ? null : "******"), getOptions());
    }
}

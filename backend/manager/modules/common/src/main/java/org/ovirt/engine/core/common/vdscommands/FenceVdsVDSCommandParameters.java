package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.businessentities.FenceActionType;
import org.ovirt.engine.core.common.businessentities.FencingPolicy;
import org.ovirt.engine.core.compat.Guid;

public class FenceVdsVDSCommandParameters extends VdsIdVDSCommandParametersBase {
    public FenceVdsVDSCommandParameters(Guid vdsId, Guid targetVdsId, String ip, String port, String type, String user,
            String password, String options, FenceActionType action, FencingPolicy fencingPolicy) {
        super(vdsId);
        _targetVdsId = targetVdsId;
        _ip = ip;
        _port = port;
        _type = type;
        _user = user;
        _password = password;
        _action = action;
        _options = options;
        this.fencingPolicy = fencingPolicy;
    }

    private Guid _targetVdsId;
    private String _ip;
    private String _port;
    private String _type;
    private String _user;
    private String _password;
    private String _options;
    private FenceActionType _action;

    private FencingPolicy fencingPolicy;

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
        _options = "";
        _action = FenceActionType.Restart;
    }

    public FencingPolicy getFencingPolicy() {
        return fencingPolicy;
    }

    @Override
    public String toString() {
        return String.format("%s, targetVdsId = %s, action = %s, ip = %s, port = %s, type = %s, user = %s, " +
                "password = %s, options = '%s', policy = '%s'", super.toString(), getTargetVdsID(), getAction(),
                getIp(), getPort(), getType(), getUser(), (getPassword() == null ? null : "******"), getOptions(),
                getFencingPolicy());
    }
}

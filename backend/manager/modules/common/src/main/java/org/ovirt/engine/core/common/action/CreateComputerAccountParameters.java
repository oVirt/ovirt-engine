package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;

public class CreateComputerAccountParameters extends VdcActionParametersBase implements java.io.Serializable {

    private static final long serialVersionUID = 5715983315771665602L;

    private String _path;

    private Guid _vmId = new Guid();

    private String _userName;

    private String _userPassword;

    private String _domain;

    public CreateComputerAccountParameters(String path, Guid vmId, String userName, String userPassword, String domain) {
        _path = path;
        _vmId = vmId;
        _userName = userName;
        _userPassword = userPassword;
        _domain = domain;
    }

    public String getPath() {
        return _path;
    }

    public Guid getVmId() {
        return _vmId;
    }

    public String getUserName() {
        return _userName;
    }

    public String getUserPassword() {
        return _userPassword;
    }

    public String getDomain() {
        return _domain;
    }

    public CreateComputerAccountParameters() {
    }
}

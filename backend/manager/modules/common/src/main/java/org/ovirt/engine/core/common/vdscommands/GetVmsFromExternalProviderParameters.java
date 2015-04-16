package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class GetVmsFromExternalProviderParameters extends VdsIdVDSCommandParametersBase {

    private String url;
    private String username;
    private String password;

    public GetVmsFromExternalProviderParameters() {
    }

    public GetVmsFromExternalProviderParameters(Guid vdsId, String url, String username, String password) {
        super(vdsId);

        this.url = url;
        this.username = username;
        this.password = password;
    }

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("url", url)
                .append("username", username);
    }
}

package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class GetVmsFromExternalProviderParameters extends VdsIdVDSCommandParametersBase {

    private String url;
    private String username;
    private String password;
    private OriginType originType;

    public GetVmsFromExternalProviderParameters() {
    }

    public GetVmsFromExternalProviderParameters(Guid vdsId, String url, String username, String password, OriginType originType) {
        super(vdsId);

        this.url = url;
        this.username = username;
        this.password = password;
        this.originType = originType;
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

    public OriginType getOriginType() {
        return originType;
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("url", url)
                .append("username", username)
                .append("originType", originType);
    }
}

package org.ovirt.engine.core.common.vdscommands;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class GetVmsFromExternalProviderParameters extends VdsIdVDSCommandParametersBase {

    private String url;
    private String username;
    private String password;
    private OriginType originType;
    private List<String> namesOfVms;

    public GetVmsFromExternalProviderParameters() {
    }

    public GetVmsFromExternalProviderParameters(Guid vdsId, String url, String username, String password, OriginType originType) {
        super(vdsId);

        this.url = url;
        this.username = username;
        this.password = password;
        this.originType = originType;
    }

    public GetVmsFromExternalProviderParameters(Guid vdsId, String url, String username, String password, OriginType originType, List<String> namesOfVms) {
        this(vdsId, url, username, password, originType);
        this.namesOfVms = namesOfVms;
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

    public List<String> getNamesOfVms() {
        return namesOfVms;
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("url", url)
                .append("username", username)
                .append("originType", originType)
                .append("namesOfVms", namesOfVms);
    }
}

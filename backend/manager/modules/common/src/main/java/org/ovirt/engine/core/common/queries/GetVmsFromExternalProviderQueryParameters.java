package org.ovirt.engine.core.common.queries;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.compat.Guid;


public class GetVmsFromExternalProviderQueryParameters extends QueryParametersBase {
    private static final long serialVersionUID = 5436719744430725756L;

    private String url;
    private String username;
    private String password;
    private OriginType originType;
    private Guid proxyHostId;
    private Guid dataCenterId;
    private List<String> namesOfVms;

    public GetVmsFromExternalProviderQueryParameters() {
    }

    public GetVmsFromExternalProviderQueryParameters(String url, String username,
            String password, OriginType originType, Guid proxyHost, Guid dataCenterId) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.originType = originType;
        this.proxyHostId = proxyHost;
        this.dataCenterId = dataCenterId;
    }

    public GetVmsFromExternalProviderQueryParameters(String url, String username,
            String password, OriginType originType, Guid proxyHost, Guid dataCenterId, List<String> namesOfVms) {
        this(url, username, password, originType, proxyHost, dataCenterId);
        this.namesOfVms = namesOfVms;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public OriginType getOriginType() {
        return originType;
    }

    public void setOriginType(OriginType originType) {
        this.originType = originType;
    }

    public Guid getProxyHostId() {
        return proxyHostId;
    }

    public void setProxyHostId(Guid proxyHost) {
        this.proxyHostId = proxyHost;
    }

    public Guid getDataCenterId() {
        return dataCenterId;
    }

    public void setDataCenterId(Guid dataCenterId) {
        this.dataCenterId = dataCenterId;
    }

    public List<String> getNamesOfVms() {
        return namesOfVms;
    }

    public void setNamesOfVms(List<String> namesOfVms) {
        this.namesOfVms = namesOfVms;
    }
}

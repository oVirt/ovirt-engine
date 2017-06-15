package org.ovirt.engine.core.common.action;

import java.io.Serializable;

import org.ovirt.engine.core.compat.Guid;

public class LoginOnBehalfParameters extends ActionParametersBase implements Serializable {
    private static final long serialVersionUID = -1660445011620552804L;

    public enum QueryType {ByInternalId, ByExternalId, ByPrincipalName, Unknown}

    private QueryType queryType = QueryType.Unknown;
    private Guid userId;
    private String authzName;
    private String externalId;
    private String namespace;
    private String principalName;

    public LoginOnBehalfParameters() {
    }

    public LoginOnBehalfParameters(Guid userId) {
        this.userId = userId;
        queryType = QueryType.ByInternalId;
    }

    public LoginOnBehalfParameters(String externalId, String authzName, String namespace) {
        this.externalId = externalId;
        this.authzName = authzName;
        this.namespace = namespace;
        queryType = QueryType.ByInternalId;
    }

    public LoginOnBehalfParameters(String userName, String authzName) {
        this.principalName = userName;
        this.authzName = authzName;
        queryType = QueryType.ByPrincipalName;
    }

    public Guid getUserId() {
        return userId;
    }

    public void setUserId(Guid userId) {
        this.userId = userId;
    }

    public String getAuthzName() {
        return authzName;
    }

    public void setAuthzName(String authzName) {
        this.authzName = authzName;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getPrincipalName() {
        return principalName;
    }

    public void setPrincipalName(String principalName) {
        this.principalName = principalName;
    }

    public QueryType getQueryType() {
        return queryType;
    }

    public void setQueryType(QueryType queryType) {
        this.queryType = queryType;
    }
}

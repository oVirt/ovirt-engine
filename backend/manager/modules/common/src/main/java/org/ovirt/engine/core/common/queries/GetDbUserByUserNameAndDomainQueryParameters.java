package org.ovirt.engine.core.common.queries;

public class GetDbUserByUserNameAndDomainQueryParameters extends QueryParametersBase {

    public GetDbUserByUserNameAndDomainQueryParameters() {
        super();
    }

    public GetDbUserByUserNameAndDomainQueryParameters(String sessionId) {
        super(sessionId);
    }

    public GetDbUserByUserNameAndDomainQueryParameters(String userName, String domainName) {
        super();
        this.userName = userName;
        this.domainName = domainName;
    }

    private static final long serialVersionUID = 5204792601949266874L;


    private String userName;
    private String domainName;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }
}

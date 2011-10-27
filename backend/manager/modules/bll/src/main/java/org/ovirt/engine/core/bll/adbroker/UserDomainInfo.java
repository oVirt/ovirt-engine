/**
 *
 */
package org.ovirt.engine.core.bll.adbroker;

import javax.security.auth.login.LoginContext;


/**
 * An association between domain and user to provide information for the user in
 * context of a given domain
 *
 */
public class UserDomainInfo {

    private String domainName;
    private String userName;
    private LoginContext loginContext;

    public UserDomainInfo(String userName, String domainName) {
        this.userName = userName;
        this.domainName = domainName;
    }

    public String getDomainName() {
        return domainName;
    }

    public String getUserName() {
        return userName;
    }

    public LoginContext getLoginContext() {
        return loginContext;
    }

    public void setLoginContext(LoginContext loginContext) {
        this.loginContext = loginContext;
    }

}

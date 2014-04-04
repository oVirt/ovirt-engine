package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

import java.util.Properties;

import org.ovirt.engine.api.extensions.Base;
import org.ovirt.engine.api.extensions.ExtMap;
import org.ovirt.engine.api.extensions.aaa.Authn;

public class LdapUserPasswordBaseParameters extends LdapBrokerBaseParameters {
    public LdapUserPasswordBaseParameters(String domain, String loginName, String password) {
        super(domain);
        setLoginName(loginName);
        setPassword(password);
    }

    public LdapUserPasswordBaseParameters(ExtMap input, ExtMap output) {
        super(input.<ExtMap> get(Base.InvokeKeys.CONTEXT)
                .<Properties> get(Base.ContextKeys.CONFIGURATION)
                .getProperty("ovirt.engine.aaa.authn.authz.plugin"));
        setLoginName(input.<String> get(Authn.InvokeKeys.USER));
        setPassword(input.<String> get(Authn.InvokeKeys.CREDENTIALS));
        setInputMap(input);
        setOutputMap(output);
    }
}

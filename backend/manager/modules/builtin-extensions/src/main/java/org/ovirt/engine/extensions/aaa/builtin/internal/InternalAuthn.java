package org.ovirt.engine.extensions.aaa.builtin.internal;

import java.util.Collection;
import java.util.Properties;
import org.ovirt.engine.api.extensions.Base;
import org.ovirt.engine.api.extensions.ExtMap;
import org.ovirt.engine.api.extensions.Extension;
import org.ovirt.engine.api.extensions.aaa.Authn;

/**
 * This authenticator authenticates the internal user as specified in the {@code AdminUser} and {@code AdminPassword}
 * configuration parameters stored in the database. Currently it is in an interim status of development as
 */
public class InternalAuthn implements Extension {

    private ExtMap context;
    private String adminUser;
    private String adminPassword;

    @Override
    public void invoke(ExtMap input, ExtMap output) {
        try {
            if (input.get(Base.InvokeKeys.COMMAND).equals(Base.InvokeCommands.LOAD)) {
                doLoad(input, output);
            } else if (input.get(Base.InvokeKeys.COMMAND).equals(Base.InvokeCommands.INITIALIZE)) {
            } else if (input.get(Base.InvokeKeys.COMMAND).equals(Authn.InvokeCommands.AUTHENTICATE_CREDENTIALS)) {
                doAuthenticate(input, output);
            } else {
                output.put(Base.InvokeKeys.RESULT, Base.InvokeResult.UNSUPPORTED);
            }
            output.putIfAbsent(Base.InvokeKeys.RESULT, Base.InvokeResult.SUCCESS);
        } catch (Exception ex) {
            output.mput(Base.InvokeKeys.RESULT, Base.InvokeResult.FAILED).
                   mput(Base.InvokeKeys.MESSAGE, ex.getMessage());
        }

    }

    private void doAuthenticate(ExtMap input, ExtMap output) {
        output.put(Authn.InvokeKeys.PRINCIPAL, input.get(Authn.InvokeKeys.USER));
        if (!(input.get(Authn.InvokeKeys.USER).equals(adminUser) && input.get(Authn.InvokeKeys.CREDENTIALS)
                .equals(adminPassword))) {
            output.put(Authn.InvokeKeys.RESULT, Authn.AuthResult.CREDENTIALS_INVALID);
        } else {
            output.mput(
                    Authn.InvokeKeys.RESULT,
                    Authn.AuthResult.SUCCESS
                    ).mput(
                            Authn.InvokeKeys.AUTH_RECORD,
                            new ExtMap().mput(
                                    Authn.AuthRecord.PRINCIPAL,
                                    adminUser
                                    )
                    );
        }
    }

    private void doLoad(ExtMap input, ExtMap output) {
        context = input.<ExtMap> get(Base.InvokeKeys.CONTEXT);
        context.<Collection<String>> get(
                Base.ContextKeys.CONFIGURATION_SENSITIVE_KEYS
                ).add("config.authn.user.password");
        context.mput(
                Base.ContextKeys.AUTHOR,
                "The oVirt Project"
                ).mput(
                        Base.ContextKeys.EXTENSION_NAME,
                        "Internal Authn (Built-in)"
                ).mput(
                        Base.ContextKeys.LICENSE,
                        "ASL 2.0"
                ).mput(
                        Base.ContextKeys.HOME_URL,
                        "http://www.ovirt.org"
                ).mput(
                        Base.ContextKeys.VERSION,
                        "N/A"
                ).mput(
                        Authn.ContextKeys.CAPABILITIES,
                        (
                            Authn.Capabilities.AUTHENTICATE_CREDENTIALS |
                            Authn.Capabilities.AUTHENTICATE_PASSWORD
                        )
                ).mput(
                        Base.ContextKeys.BUILD_INTERFACE_VERSION,
                        Base.INTERFACE_VERSION_CURRENT);
        Properties config = context.<Properties> get(Base.ContextKeys.CONFIGURATION);
        adminUser = config.getProperty("config.authn.user.name");
        adminPassword = config.getProperty("config.authn.user.password");
    }

}

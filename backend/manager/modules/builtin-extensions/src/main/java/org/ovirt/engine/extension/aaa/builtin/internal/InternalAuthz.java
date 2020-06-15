package org.ovirt.engine.extension.aaa.builtin.internal;

import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

import org.ovirt.engine.api.extensions.Base;
import org.ovirt.engine.api.extensions.ExtKey;
import org.ovirt.engine.api.extensions.ExtMap;
import org.ovirt.engine.api.extensions.ExtUUID;
import org.ovirt.engine.api.extensions.Extension;
import org.ovirt.engine.api.extensions.aaa.Authn;
import org.ovirt.engine.api.extensions.aaa.Authz;

public class InternalAuthz implements Extension {

    private static final String DEFAULT_PRINCIPAL_NAME = "admin";
    private static final String DEFAULT_PRINCIPAL_ID = "fdfc627c-d875-11e0-90f0-83df133b58cc";
    private static final String NAMESPACE = "*";

    private ExtMap adminUser;

    private static class Opaque {

        private boolean firstCall;
        private boolean found;

        public Opaque(boolean found) {
            firstCall = true;
            this.found = found;
        }
    }

    @Override
    public void invoke(ExtMap input, ExtMap output) {
        try {
            ExtUUID command = input.get(Base.InvokeKeys.COMMAND);
            if (input.get(Base.InvokeKeys.COMMAND).equals(Base.InvokeCommands.LOAD)) {
                doLoad(input);
            } else if (input.get(Base.InvokeKeys.COMMAND).equals(Base.InvokeCommands.INITIALIZE)) {
                // Do nothing
            } else if (command.equals(Authz.InvokeCommands.FETCH_PRINCIPAL_RECORD)) {
                doFetchPrincipalRecord(input, output);
            } else if (command.equals(Authz.InvokeCommands.QUERY_CLOSE)) {
                // Do nothing
            } else if (command.equals(Authz.InvokeCommands.QUERY_OPEN)) {
                doQueryOpen(input, output);
            } else if (command.equals(Authz.InvokeCommands.QUERY_EXECUTE)) {
                doQueryExecute(input, output);
            } else {
                output.put(
                        Base.InvokeKeys.RESULT,
                        Base.InvokeResult.UNSUPPORTED
                        );
            }
            output.putIfAbsent(Authz.InvokeKeys.STATUS, Authz.Status.SUCCESS);
            output.putIfAbsent(Base.InvokeKeys.RESULT, Base.InvokeResult.SUCCESS);
        } catch (Exception ex) {
            output.mput(
                    Base.InvokeKeys.RESULT,
                    Base.InvokeResult.FAILED
                    ).mput(
                            Base.InvokeKeys.MESSAGE,
                            ex.getMessage()
                    );
        }
    }

    private void doQueryOpen(ExtMap input, ExtMap output) {
        if (input.get(Authz.InvokeKeys.QUERY_ENTITY).equals(Authz.QueryEntity.PRINCIPAL)) {
            output.put(Authz.InvokeKeys.QUERY_OPAQUE, new Opaque(doQueryOpenImpl(input.get(Authz.InvokeKeys.QUERY_FILTER))));
        } else {
            output.put(Authz.InvokeKeys.QUERY_OPAQUE, new Opaque(false));
        }
    }

    private boolean doQueryOpenImpl(ExtMap filter) {
        boolean found = false;
        if (filter.<Integer> get(Authz.QueryFilterRecord.OPERATOR) == Authz.QueryFilterOperator.EQ) {
            ExtKey extKey = filter.get(Authz.QueryFilterRecord.KEY);
            if (extKey.equals(Authz.PrincipalRecord.NAME)) {
                String name = filter.get(Authz.PrincipalRecord.NAME);
                found = adminUser.<String>get(Authz.PrincipalRecord.NAME).matches(name.replace("*", ".*"));
            } else if (extKey.equals(Authz.PrincipalRecord.ID)) {
                found = filter.<String>get(Authz.PrincipalRecord.ID).equals(adminUser.<String>get(Authz.PrincipalRecord.ID));
            } else {
                found = false;
            }
        } else {
            for (ExtMap currentFilter : filter.<Collection<ExtMap>> get(Authz.QueryFilterRecord.FILTER)) {
                found = found || doQueryOpenImpl(currentFilter);
            }
        }
        return found;
    }

    private void doQueryExecute(ExtMap input, ExtMap output) {
        Opaque opaque = input.get(Authz.InvokeKeys.QUERY_OPAQUE);
        output.put(Authz.InvokeKeys.QUERY_RESULT,
                opaque.firstCall && opaque.found ? Collections.singletonList(adminUser)
                        : null);
        opaque.firstCall = false;
    }

    private void doFetchPrincipalRecord(ExtMap input, ExtMap output) {
        ExtMap authRecord = input.get(Authn.InvokeKeys.AUTH_RECORD);
        String principal = authRecord != null ? authRecord.get(Authn.AuthRecord.PRINCIPAL) : input.get(Authz.InvokeKeys.PRINCIPAL);
        if (principal.equals(adminUser.<String> get(Authz.PrincipalRecord.NAME))) {
            output.put(Authz.InvokeKeys.PRINCIPAL_RECORD, adminUser);
        }
    }

    private void doLoad(ExtMap input) {
        ExtMap context = input.get(Base.InvokeKeys.CONTEXT);
        Properties configuration = context.get(Base.ContextKeys.CONFIGURATION);
        context.mput(
                Base.ContextKeys.AUTHOR,
                "The oVirt Project"
                ).mput(
                        Base.ContextKeys.EXTENSION_NAME,
                        "Internal Authz (Built-in)"
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
                        Authz.ContextKeys.QUERY_MAX_FILTER_SIZE,
                        Integer.parseInt(
                                configuration.getProperty("config.query.filter.size", "10")
                        )
                ).mput(
                        Base.ContextKeys.BUILD_INTERFACE_VERSION,
                        Base.INTERFACE_VERSION_CURRENT
                ).mput(
                        Authz.ContextKeys.AVAILABLE_NAMESPACES,
                        Collections.singletonList(NAMESPACE)
                        );
        adminUser = new ExtMap().mput(
                Authz.PrincipalRecord.NAMESPACE,
                NAMESPACE
                ).mput(
                        Authz.PrincipalRecord.NAME,
                        configuration.getProperty("config.authz.user.name", DEFAULT_PRINCIPAL_NAME)
                ).mput(
                        Authz.PrincipalRecord.FIRST_NAME,
                        configuration.getProperty("config.authz.user.name", DEFAULT_PRINCIPAL_NAME)
                ).mput(
                        Authz.PrincipalRecord.ID,
                        configuration.getProperty("config.authz.user.id", DEFAULT_PRINCIPAL_ID)
                ).mput(Authz.PrincipalRecord.PRINCIPAL,
                        configuration.getProperty("config.authz.user.name", DEFAULT_PRINCIPAL_NAME)
                );

    }

}

package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.ovirt.engine.api.extensions.Base;
import org.ovirt.engine.api.extensions.ExtKey;
import org.ovirt.engine.api.extensions.ExtMap;
import org.ovirt.engine.api.extensions.ExtUUID;
import org.ovirt.engine.api.extensions.Extension;
import org.ovirt.engine.api.extensions.aaa.Authn;
import org.ovirt.engine.api.extensions.aaa.Authz;
import org.ovirt.engine.api.extensions.aaa.Authz.QueryFilterRecord;
import org.ovirt.engine.core.common.businessentities.aaa.LdapGroup;
import org.ovirt.engine.core.common.businessentities.aaa.LdapUser;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

/**
 * This directory implementation is a bridge between the new directory interfaces and the existing LDAP infrastructure.
 * It will exist only while the engine is migrated to use the new directory interfaces, then it will be removed.
 */
public class KerberosLdapAuthz implements Extension {

    private static final Log log = LogFactory.getLog(KerberosLdapAuthz.class);

    private static final String NAMESPACE = "*";

    private static final String USERS_QUERY_PREFIX = "(&($USER_ACCOUNT_TYPE)";

    private static final String GROUPS_QUERY_PREFIX = "(&($LDAP_GROUP_CATEGORY)";

    private static Map<ExtKey, String> keysToAttributes = new HashMap<>();
    private static Map<Integer, Character> operatorsToChars = new HashMap<>();

    private static class Opaque {
        ExtMap queryInfo = null;

        public Opaque(ExtMap queryInfo) {
            this.queryInfo = queryInfo;
        }

        public void resetQueryInfo() {
            queryInfo = null;
        }

        public ExtMap getQueryInfo() {
            return queryInfo;
        }
    }

    static {
        keysToAttributes.put(Authz.PrincipalRecord.FIRST_NAME, "$GIVENNAME");
        keysToAttributes.put(Authz.PrincipalRecord.NAME, "$SAMACCOUNTNAME");
        keysToAttributes.put(Authz.PrincipalRecord.DISPLAY_NAME, "$USER_ACCOUNT_NAME");
        keysToAttributes.put(Authz.PrincipalRecord.LAST_NAME, "sn");
        keysToAttributes.put(Authz.PrincipalRecord.DEPARTMENT, "$DEPARTMENT");
        keysToAttributes.put(Authz.PrincipalRecord.TITLE, "$TITLE");
        keysToAttributes.put(Authz.PrincipalRecord.ID, "$USER_ID");
        keysToAttributes.put(Authz.GroupRecord.ID, "$GROUP_ID");
        keysToAttributes.put(Authz.GroupRecord.NAME, "$CN");
        operatorsToChars.put(Authz.QueryFilterOperator.AND, '&');
        operatorsToChars.put(Authz.QueryFilterOperator.OR, '|');
        operatorsToChars.put(Authz.QueryFilterOperator.NOT, '!');
        operatorsToChars.put(Authz.QueryFilterOperator.EQ, '=');
    }

    /**
     * The reference to the LDAP broker that implements the authentication.
     */
    private LdapBroker broker;

    private ExtMap context;

    private Properties configuration;

    public KerberosLdapAuthz() {
    }

    public ExtMap queryGroups(ExtMap input, ExtMap output) {
        LdapQueryData queryData = new LdapQueryDataImpl();
        queryData.setLdapQueryType(LdapQueryType.searchGroups);
        queryData.setDomain(getDirectoryName());
        queryData.setFilterParameters(new Object[] { generateQueryString(input) });
        // Find the users using the old mechanism:
        LdapReturnValueBase ldapResult = broker.runAdAction(
                AdActionType.SearchGroupsByQuery,
                new LdapSearchByQueryParameters(configuration,
                        null,
                        getDirectoryName(),
                        queryData,
                        false)
                );
        List<LdapGroup> ldapGroups = (List<LdapGroup>) ldapResult.getReturnValue();
        List<ExtMap> results = new ArrayList<>();
        for (LdapGroup ldapGroup : ldapGroups) {
            results.add(mapLdapGroup(ldapGroup));
        }
        return output.mput(Authz.InvokeKeys.QUERY_RESULT, results);
    }

    public ExtMap queryUsers(ExtMap input, ExtMap output) {
        LdapQueryData queryData = new LdapQueryDataImpl();
        queryData.setLdapQueryType(LdapQueryType.searchUsers);
        queryData.setDomain(getDirectoryName());
        queryData.setFilterParameters(new Object[] { generateQueryString(input) });
        // Find the users using the old mechanism:
        LdapReturnValueBase ldapResult = broker.runAdAction(
                AdActionType.SearchUserByQuery,
                new LdapSearchByQueryParameters(configuration,
                        null,
                        getDirectoryName(),
                        queryData,
                                (input.<Integer> get(Authz.InvokeKeys.QUERY_FLAGS, 0) & Authz.QueryFlags.RESOLVE_GROUPS_RECURSIVE) != 0
                )
        );
        List<LdapUser> ldapUsers = (List<LdapUser>) ldapResult.getReturnValue();
        List<ExtMap> results = new ArrayList<>();
        for (LdapUser ldapUser : ldapUsers) {
            results.add(mapLdapUser(ldapUser));
        }
        return output.mput(Authz.InvokeKeys.QUERY_RESULT, results);
    }

    @Override
    public void invoke(ExtMap input, ExtMap output) {
        try {
            Object command = input.get(Base.InvokeKeys.COMMAND);
            if (command.equals(Base.InvokeCommands.LOAD)) {
                doLoad(input, output);
            } else if (command.equals(Base.InvokeCommands.INITIALIZE)) {
                doInit(input, output);
            } else if (command.equals(Authz.InvokeCommands.QUERY_OPEN)) {
                doQueryOpen(input, output);
            } else if (command.equals(Authz.InvokeCommands.FETCH_PRINCIPAL_RECORD)) {
                doFetchPrincipalRecord(input, output);
            } else if (command.equals(Authz.InvokeCommands.QUERY_EXECUTE)) {
                doQueryExecute(input, output);
            } else if (command.equals(Authz.InvokeCommands.QUERY_CLOSE)) {
            } else {
                output.put(Base.InvokeKeys.RESULT, Base.InvokeResult.UNSUPPORTED);
            }
            output.putIfAbsent(Base.InvokeKeys.RESULT, Base.InvokeResult.SUCCESS);
            output.putIfAbsent(Authz.InvokeKeys.STATUS, Authz.Status.SUCCESS);

        } catch (Exception ex) {
            output.mput(
                    Base.InvokeKeys.RESULT,
                    Base.InvokeResult.FAILED
                    ).mput(
                            Base.InvokeKeys.MESSAGE,
                            ex.getMessage()
                    ).mput(
                            Authz.InvokeKeys.STATUS,
                            Authz.Status.GENERAL_ERROR
                    );
        }
    }

    private void doInit(ExtMap input, ExtMap output) {
        try {
            Utils.handleApplicationInit(context.<ExtMap> get(Base.ContextKeys.GLOBAL_CONTEXT)
                    .<String> get(Base.GlobalContextKeys.APPLICATION_NAME));
        } catch (Exception e) {
            output.mput(
                    Base.InvokeKeys.MESSAGE,
                    e.getMessage()
                    ).mput(
                            Base.InvokeKeys.RESULT,
                            Base.InvokeResult.FAILED
                    );
        }
    }

    private void doFetchPrincipalRecord(ExtMap input, ExtMap output) {
        ExtMap authRecord = input.<ExtMap> get(Authn.InvokeKeys.AUTH_RECORD);
        LdapReturnValueBase ldapResult =
                broker.runAdAction(AdActionType.GetAdUserByUserName, new LdapSearchByUserNameParameters(configuration,
                        null,
                        getDirectoryName(), authRecord != null ? authRecord.<String> get(Authn.AuthRecord.PRINCIPAL)
                                : input.<String> get(Authz.InvokeKeys.PRINCIPAL)));
        output.mput(
                Authz.InvokeKeys.PRINCIPAL_RECORD,
                mapLdapUser(((LdapUser) ldapResult.getReturnValue()))
                ).mput(
                        Authz.InvokeKeys.STATUS,
                        Authz.Status.SUCCESS);

    }

    private void doQueryExecute(ExtMap input, ExtMap output) {
        Opaque opaque = input.<Opaque> get(Authz.InvokeKeys.QUERY_OPAQUE);
        if (opaque.getQueryInfo() == null) {
            output.mput(Authz.InvokeKeys.QUERY_RESULT, null);
        } else {
            if (opaque.getQueryInfo().<ExtUUID> get(Authz.InvokeKeys.QUERY_ENTITY).equals(Authz.QueryEntity.GROUP)) {
                queryGroups(opaque.getQueryInfo(), output);
            } else if (opaque.getQueryInfo()
                    .<ExtUUID> get(Authz.InvokeKeys.QUERY_ENTITY)
                    .equals(Authz.QueryEntity.PRINCIPAL)) {
                queryUsers(opaque.getQueryInfo(), output);
            }
            opaque.resetQueryInfo();
        }
        output.mput(Authz.InvokeKeys.QUERY_OPAQUE, opaque);
    }

    private void doQueryOpen(ExtMap input, ExtMap output) {
        output.mput(Authz.InvokeKeys.QUERY_OPAQUE, new Opaque(input));
    }

    private void doLoad(ExtMap inputMap, ExtMap outputMap) {
        context = inputMap.<ExtMap> get(Base.InvokeKeys.CONTEXT);
        configuration = context.<Properties> get(Base.ContextKeys.CONFIGURATION);
        broker = LdapFactory.getInstance(getDirectoryName());
        Utils.setDefaults(configuration, getDirectoryName());
        context.mput(
                Base.ContextKeys.AUTHOR,
                "The oVirt Project").mput(
                Base.ContextKeys.EXTENSION_NAME,
                "Kerberos/Ldap Authz (Built-in)"
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
                        Integer.parseInt(configuration.getProperty("config.query.filter.size"))
                ).mput(
                        Base.ContextKeys.BUILD_INTERFACE_VERSION,
                        Base.INTERFACE_VERSION_CURRENT
                ).mput(
                        Authz.ContextKeys.AVAILABLE_NAMESPACES,
                        Arrays.asList(NAMESPACE)
                ).mput(
                        Authz.ContextKeys.CAPABILITIES,
                        Authz.Capabilities.RECURSIVE_GROUP_RESOLUTION
                );
    }

    private ExtMap mapLdapUser(LdapUser user) {
        ExtMap result = new ExtMap();
        result.mput(
                Authz.PrincipalRecord.NAMESPACE,
                NAMESPACE
                ).mput(
                        Authz.PrincipalRecord.ID,
                        user.getUserId()
                ).mput(
                        Authz.PrincipalRecord.DEPARTMENT,
                        user.getDepartment()
                ).mput(
                        Authz.PrincipalRecord.DISPLAY_NAME,
                        user.getUserName()
                ).mput(
                        Authz.PrincipalRecord.EMAIL,
                        user.getEmail()
                ).mput(
                        Authz.PrincipalRecord.FIRST_NAME,
                        user.getName()
                ).mput(
                        Authz.PrincipalRecord.LAST_NAME,
                        user.getSurName()
                ).mput(
                        Authz.PrincipalRecord.NAME,
                        formatValue(user.getUserName(), Authz.PrincipalRecord.NAME)
                ).mput(
                        Authz.PrincipalRecord.TITLE,
                        user.getTitle()
                ).mput(
                        Authz.PrincipalRecord.PRINCIPAL,
                        formatValue(user.getUserName(), Authz.PrincipalRecord.PRINCIPAL)
                );
        if (user.getGroups() != null) {
            List<ExtMap> groups = new ArrayList<>();
            for (LdapGroup group : user.getGroups().values()) {
                groups.add(mapLdapGroup(group));
            }
            result.put(Authz.PrincipalRecord.GROUPS, groups);
        }
        return result;
    }

    private ExtMap mapLdapGroup(LdapGroup group) {
        ExtMap result = new ExtMap();
        result.mput(
                Authz.GroupRecord.ID,
                group.getid()
                ).mput(
                        Authz.GroupRecord.NAMESPACE,
                        NAMESPACE
                ).mput(
                        Authz.GroupRecord.NAME,
                        group.getname()
                );
        return result;
    }

    private String getDirectoryName() {
        return configuration.getProperty(Base.ConfigKeys.NAME);
    }

    private String generateQueryString(ExtMap query) {
        ExtMap queryFilterRecord = query.<ExtMap> get(Authz.InvokeKeys.QUERY_FILTER);
        return String.format(
                "%1$s%2$s)",
                query.get(Authz.InvokeKeys.QUERY_ENTITY).equals(Authz.QueryEntity.PRINCIPAL) ? USERS_QUERY_PREFIX
                        : GROUPS_QUERY_PREFIX,
                generateFromFilter(queryFilterRecord, new StringBuilder()).toString()
                );
    }

    private StringBuilder generateFromFilter(ExtMap queryFilterRecord, StringBuilder query) {
        List<ExtMap> filter = queryFilterRecord.<List<ExtMap>> get(Authz.QueryFilterRecord.FILTER);
        if (filter == null) {
            ExtKey key = queryFilterRecord.<ExtKey> get(Authz.QueryFilterRecord.KEY);
            query.append(
                    String.format(
                            "(%1$s%2$s%3$s)", keysToAttributes.get(key),
                            operatorsToChars.get(queryFilterRecord.get(QueryFilterRecord.OPERATOR)),
                            formatValue(queryFilterRecord.get(key).toString(), key)
                            )
                    );
        } else {
            query.append(
                    String.format(
                            "(%1$s",
                            operatorsToChars.get(queryFilterRecord.<Integer> get(Authz.QueryFilterRecord.OPERATOR)
                            )
                     )
            );
            for (ExtMap filterRecord : filter) {
                generateFromFilter(filterRecord, query);
            }
            query.append(")");
        }
        return query;
    }

    private String formatValue(String value, ExtKey key) {
        String result = value;
        if (key.equals(Authz.PrincipalRecord.NAME) || key.equals(Authz.PrincipalRecord.PRINCIPAL)) {
            result = value.contains("@") ? value.substring(0, value.indexOf("@")) : value;
        } else if (key.equals(Authz.GroupRecord.NAME)) {
            result = value.contains("/") ? value.substring(value.lastIndexOf('/') + 1) : value;
        }
        return result;
    }

}

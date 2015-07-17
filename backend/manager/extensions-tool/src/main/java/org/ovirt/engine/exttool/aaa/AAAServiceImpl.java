
package org.ovirt.engine.exttool.aaa;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.ovirt.engine.api.extensions.Base;
import org.ovirt.engine.api.extensions.ExtKey;
import org.ovirt.engine.api.extensions.ExtMap;
import org.ovirt.engine.api.extensions.ExtUUID;
import org.ovirt.engine.api.extensions.aaa.Acct;
import org.ovirt.engine.api.extensions.aaa.Authn;
import org.ovirt.engine.api.extensions.aaa.Authz;
import org.ovirt.engine.api.extensions.aaa.Mapping;
import org.ovirt.engine.core.extensions.mgr.ExtensionProxy;
import org.ovirt.engine.core.extensions.mgr.ExtensionsManager;
import org.ovirt.engine.core.uutils.cli.parser.ArgumentsParser;
import org.ovirt.engine.exttool.core.ExitException;
import org.ovirt.engine.exttool.core.ModuleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AAAServiceImpl implements ModuleService {
    private static final Logger log = LoggerFactory.getLogger(AAAServiceImpl.class);

    private interface Logic {
        void execute(ExtMap context, Map<String, Object> argMap);
    }

    private enum Action {
        AUTHZ_FETCH_PRINCIPAL_RECORD(
            new Logic() {
                @Override
                public void execute(ExtMap context, Map<String, Object> argMap) {
                    ExtensionProxy authzExtension = getExtensionsManager(context).getExtensionByName((String) argMap.get("extension-name"));

                    log.info("API: -->Authz.InvokeCommands.FETCH_PRINCIPAL_RECORD principal='{}'", argMap.get("principal-name"));
                    ExtMap outMap = authzExtension.invoke(
                        new ExtMap().mput(
                            Authz.InvokeKeys.PRINCIPAL,
                            argMap.get("principal-name")
                        ).mput(
                            Base.InvokeKeys.COMMAND,
                            Authz.InvokeCommands.FETCH_PRINCIPAL_RECORD
                        ).mput(
                            Authz.InvokeKeys.QUERY_FLAGS,
                            getAuthzFlags((List<String>) argMap.get("authz-flag"))
                        )
                    );
                    log.info(
                        "API: <--Authz.InvokeCommands.FETCH_PRINCIPAL_RECORD status={}",
                        getFieldNameByValue(Authz.Status.class, outMap.<Integer> get(Authz.InvokeKeys.STATUS))
                    );

                    if (outMap.<Integer> get(Authz.InvokeKeys.STATUS) != Authz.Status.SUCCESS) {
                        throw new RuntimeException(
                            String.format(
                                "Authz.Status code is: %1$s",
                                getFieldNameByValue(Authz.Status.class, outMap.<Integer> get(Authz.InvokeKeys.STATUS))
                            )
                        );
                    }

                    Dump.PRINCIPAL_RECORD.dump(outMap.<ExtMap>get(Authz.InvokeKeys.PRINCIPAL_RECORD));
                }
            }
        ),
        AUTHN_AUTHENTICATE_CREDENTIALS(
            new Logic() {
                @Override
                public void execute(ExtMap context, Map<String, Object> argMap) {
                    ExtensionProxy authnExtension = getExtensionsManager(context).getExtensionByName((String) argMap.get("extension-name"));

                    log.info("API: -->Authn.InvokeCommands.AUTHENTICATE_CREDENTIALS user='{}'", argMap.get("user-name"));
                    ExtMap outMap = authnExtension.invoke(
                        new ExtMap().mput(
                            Base.InvokeKeys.COMMAND,
                            Authn.InvokeCommands.AUTHENTICATE_CREDENTIALS
                        ).mput(
                            Authn.InvokeKeys.USER,
                            argMap.get("user-name")
                        ).mput(
                            Authn.InvokeKeys.CREDENTIALS,
                            getPassword((String)argMap.get("password"))
                        )
                    );
                    log.info(
                        "API: <--Authn.InvokeCommands.AUTHENTICATE_CREDENTIALS result={}",
                        getFieldNameByValue(Authn.AuthResult.class, outMap.<Integer> get(Authn.InvokeKeys.RESULT))
                    );

                    Dump.AUTH_RECORD.dump(outMap.<ExtMap>get(Authn.InvokeKeys.AUTH_RECORD));

                    if (outMap.<Integer> get(Authn.InvokeKeys.RESULT) != Authn.AuthResult.SUCCESS) {
                        throw new RuntimeException(
                            String.format(
                                "Authn.Result code is: %1$s",
                                getFieldNameByValue(Authn.AuthResult.class, outMap.<Integer> get(Authn.InvokeKeys.RESULT))
                            )
                        );
                    }
                }
            }
        ),
        LOGIN_USER(
            new Logic() {
                @Override
                public void execute(ExtMap context, Map<String, Object> argMap) {
                    ExtensionProxy mappingExtension = null;
                    ExtensionProxy authnExtension = getExtensionByProfile(context, (String) argMap.get("profile-name"));
                    String authzName = authnExtension.getContext().<Properties>get(Base.ContextKeys.CONFIGURATION).getProperty(Authn.ConfigKeys.AUTHZ_PLUGIN);
                    String mappingName = authnExtension.getContext().<Properties>get(Base.ContextKeys.CONFIGURATION).getProperty(Authn.ConfigKeys.MAPPING_PLUGIN);

                    log.info(
                        "Profile='{}' authn='{}' authz='{}' mapping='{}'",
                        argMap.get("profile-name"),
                        authnExtension.getContext().get(Base.ContextKeys.INSTANCE_NAME),
                        authzName,
                        mappingName
                    );

                    ExtensionProxy authzExtension = getExtensionsManager(context).getExtensionByName(authzName);
                    if(mappingName != null) {
                        mappingExtension = getExtensionsManager(context).getExtensionByName(mappingName);
                    }

                    String user = (String)argMap.get("user-name");

                    if(mappingExtension != null) {
                        log.info("API: -->Mapping.InvokeCommands.MAP_USER user='{}'", user);
                        user = mappingExtension.invoke(
                            new ExtMap().mput(
                                Base.InvokeKeys.COMMAND,
                                Mapping.InvokeCommands.MAP_USER
                            ).mput(
                                Mapping.InvokeKeys.USER,
                                user
                            ),
                            true
                        ).<String>get(Mapping.InvokeKeys.USER, user);
                        log.info("API: <--Mapping.InvokeCommands.MAP_USER user='{}'", user);
                    }

                    log.info("API: -->Authn.InvokeCommands.AUTHENTICATE_CREDENTIALS user='{}'", user);
                    ExtMap outMap = authnExtension.invoke(
                        new ExtMap().mput(
                            Base.InvokeKeys.COMMAND,
                            Authn.InvokeCommands.AUTHENTICATE_CREDENTIALS
                        ).mput(
                            Authn.InvokeKeys.USER,
                            user
                        ).mput(
                            Authn.InvokeKeys.CREDENTIALS,
                            getPassword((String)argMap.get("password"))
                        )
                    );
                    log.info(
                        "API: <--Authn.InvokeCommands.AUTHENTICATE_CREDENTIALS result={}",
                        getFieldNameByValue(Authn.AuthResult.class, outMap.<Integer> get(Authn.InvokeKeys.RESULT))
                    );

                    ExtMap authRecord = outMap.<ExtMap>get(Authn.InvokeKeys.AUTH_RECORD);
                    Dump.AUTH_RECORD.dump(authRecord);

                    if (outMap.<Integer> get(Authn.InvokeKeys.RESULT) != Authn.AuthResult.SUCCESS) {
                        acctReport(
                            context,
                            Acct.ReportReason.PRINCIPAL_LOGIN_FAILED,
                            authzName,
                            authRecord,
                            null,
                            user,
                            "User '%1$s' could not login"
                        );
                        throw new RuntimeException(
                            String.format(
                                "Authn.Result code is: %1$s",
                                getFieldNameByValue(Authn.AuthResult.class, outMap.<Integer> get(Authn.InvokeKeys.RESULT))
                            )
                        );
                    }

                    if(mappingExtension != null) {
                        log.info("API: -->Mapping.InvokeCommands.MAP_AUTH_RECORD");
                        Dump.AUTH_RECORD.dump(authRecord);
                        authRecord = mappingExtension.invoke(
                            new ExtMap().mput(
                                Base.InvokeKeys.COMMAND,
                                Mapping.InvokeCommands.MAP_AUTH_RECORD
                            ).mput(
                                Authn.InvokeKeys.AUTH_RECORD,
                                authRecord
                            ),
                            true
                        ).<ExtMap>get(
                            Authn.InvokeKeys.AUTH_RECORD,
                            authRecord
                        );
                        log.info("API: <--Mapping.InvokeCommands.MAP_AUTH_RECORD");
                        Dump.AUTH_RECORD.dump(authRecord);
                    }

                    log.info("API: -->Authz.InvokeCommands.FETCH_PRINCIPAL_RECORD principal='{}'", authRecord.get(Authn.AuthRecord.PRINCIPAL));
                    outMap = authzExtension.invoke(
                        new ExtMap().
                            mput(
                                Base.InvokeKeys.COMMAND,
                                Authz.InvokeCommands.FETCH_PRINCIPAL_RECORD
                            ).mput(
                                Authn.InvokeKeys.AUTH_RECORD,
                                authRecord
                            ).mput(
                                Authz.InvokeKeys.QUERY_FLAGS,
                                (
                                    Authz.QueryFlags.RESOLVE_GROUPS |
                                    Authz.QueryFlags.RESOLVE_GROUPS_RECURSIVE
                                )
                            )
                    );
                    log.info(
                        "API: <--Authz.InvokeCommands.FETCH_PRINCIPAL_RECORD status={}",
                        getFieldNameByValue(Authz.Status.class, outMap.<Integer> get(Authz.InvokeKeys.STATUS))
                    );

                    ExtMap principalRecord = outMap.<ExtMap> get(Authz.InvokeKeys.PRINCIPAL_RECORD);
                    Dump.PRINCIPAL_RECORD.dump(principalRecord);

                    if (outMap.<Integer> get(Authz.InvokeKeys.STATUS) != Authz.Status.SUCCESS) {
                        if (principalRecord == null) {
                            acctReport(
                                context,
                                Acct.ReportReason.PRINCIPAL_NOT_FOUND,
                                authzName,
                                authRecord,
                                null,
                                user,
                                "User '%1$s' could not be found"
                            );
                        } else {
                            acctReport(
                                context,
                                Acct.ReportReason.PRINCIPAL_LOGIN_FAILED,
                                authzName,
                                authRecord,
                                principalRecord,
                                user,
                                "User '%1$s' could not be found"
                            );
                        }

                        throw new RuntimeException(
                            String.format(
                                "Authz.Status code is: %1$s",
                                getFieldNameByValue(Authz.Status.class, outMap.<Integer> get(Authz.InvokeKeys.STATUS))
                            )
                        );
                    }

                    acctReport(
                        context,
                        Acct.ReportReason.PRINCIPAL_LOGIN_CREDENTIALS,
                        authzName,
                        authRecord,
                        principalRecord,
                        user,
                        "Principal '%1$s' logged in"
                    );

                    if ((authnExtension.getContext().<Long> get(Authn.ContextKeys.CAPABILITIES) & Authn.Capabilities.LOGOUT) != 0) {
                        log.info("API: -->Authn.InvokeCommands.LOGOUT principal='{}'", authRecord.<String> get(Authn.AuthRecord.PRINCIPAL));
                        authnExtension.invoke(
                            new ExtMap().mput(
                                Base.InvokeKeys.COMMAND,
                                Authn.InvokeCommands.LOGOUT
                            ).mput(
                                Authn.InvokeKeys.AUTH_RECORD,
                                authRecord
                            )
                        );
                        log.info("API: <--Authn.InvokeCommands.LOGOUT");
                    }

                    acctReport(
                        context,
                        Acct.ReportReason.PRINCIPAL_LOGOUT,
                        authzName,
                        authRecord,
                        principalRecord,
                        user,
                        "Principal '%1$s' logged out"
                    );
                }
            }
        ),
        SEARCH(
            new Logic() {
                @Override
                public void execute(ExtMap context, Map<String, Object> argMap) {
                    ExtensionProxy authzExtension = getExtensionsManager(context).getExtensionByName((String) argMap.get("extension-name"));
                    ExtUUID entity = getQueryEntity((String)argMap.get("entity"));
                    ExtMap filter = createQueryFilter(entity, argMap);
                    Dump.QUERY_FILTER_RECORD.dump(filter, "");

                    log.info("API: -->Authz.InvokeCommands.QUERY_OPEN");
                    ExtMap outMap = authzExtension.invoke(
                        new ExtMap().mput(
                            Base.InvokeKeys.COMMAND,
                            Authz.InvokeCommands.QUERY_OPEN
                        ).mput(
                            Authz.InvokeKeys.QUERY_ENTITY,
                            entity
                        ).mput(
                            Authz.InvokeKeys.QUERY_FLAGS,
                            getAuthzFlags((List<String>)argMap.get("authz-flag"))
                        ).mput(
                            Authz.InvokeKeys.QUERY_FILTER,
                            filter
                        ).mput(
                            Authz.InvokeKeys.NAMESPACE,
                            getNamespace(authzExtension, (String)argMap.get("namespace"))
                        )
                    );
                    log.info("API: <--Authz.InvokeCommands.QUERY_OPEN");

                    Object opaque = outMap.get(Authz.InvokeKeys.QUERY_OPAQUE);
                    boolean done = false;
                    while (!done) {
                        log.info("API: -->Authz.InvokeCommands.QUERY_EXECUTE");
                        outMap = authzExtension.invoke(
                            new ExtMap().mput(
                                Base.InvokeKeys.COMMAND,
                                Authz.InvokeCommands.QUERY_EXECUTE
                            ).mput(
                                Authz.InvokeKeys.QUERY_OPAQUE,
                                opaque
                            ).mput(
                                Authz.InvokeKeys.PAGE_SIZE,
                                argMap.get("page-size")
                            )
                        );
                        List<ExtMap> results = outMap.get(Authz.InvokeKeys.QUERY_RESULT);
                        log.info("API: <--Authz.InvokeCommands.QUERY_EXECUTE count={}", results == null ? "END" : results.size());

                        if (results == null) {
                            done = true;
                        } else {
                            for (ExtMap result : results) {
                                if (Authz.QueryEntity.PRINCIPAL.equals(entity)) {
                                    Dump.PRINCIPAL_RECORD.dump(result);
                                } else if (Authz.QueryEntity.GROUP.equals(entity)) {
                                    Dump.GROUP_RECORD.dump(result);
                                }
                            }
                        }
                    }

                    log.info("API: -->Authz.InvokeCommands.QUERY_CLOSE");
                    authzExtension.invoke(
                        new ExtMap().mput(
                            Base.InvokeKeys.COMMAND,
                            Authz.InvokeCommands.QUERY_CLOSE
                        ).mput(
                            Authz.InvokeKeys.QUERY_OPAQUE,
                            opaque
                        )
                    );
                    log.info("API: <--Authz.InvokeCommands.QUERY_CLOSE");
                }
            }
        );

        private Logic logic;

        private Action(Logic logic) {
            this.logic = logic;
        }

        Map<String, Object> parse(Map<String, String> substitutions, Properties props, List<String> moduleArgs) {
            ArgumentsParser parser = new ArgumentsParser(props, moduleArgs.remove(0));
            parser.getSubstitutions().putAll(substitutions);
            parser.parse(moduleArgs);
            Map<String, Object> argMap = parser.getParsedArgs();

            if((Boolean)argMap.get("help")) {
                System.out.format("Usage: %s", parser.getUsage());
                throw new ExitException("Help", 0);
            }
            if(!parser.getErrors().isEmpty()) {
                for(Throwable t : parser.getErrors()) {
                    log.error(t.getMessage());
                }
                throw new ExitException("Parsing error", 1);
            }
            if (moduleArgs.size() != 0) {
                log.error("Extra parameters in command-line");
                throw new ExitException("Parsing error", 1);
            }

            return argMap;
        }

        void execute(ExtMap context, Map<String, Object> argMap) {
            logic.execute(context, argMap);
        }
    }

    private interface DumpFormat {
        void dump(ExtMap map, String indent);
    }

    private enum Dump {
        AUTH_RECORD(
            new DumpFormat() {
                @Override
                public void dump(ExtMap map, String indent) {
                    if (map != null) {
                        log.info("--- Begin AuthRecord ---");
                        dumpRecord(map, Collections.<ExtKey>emptyList(), "AuthRecord", "");
                        log.info("--- End   AuthRecord ---");
                    }
                }
            }
        ),
        PRINCIPAL_RECORD(
            new DumpFormat() {
                @Override
                public void dump(ExtMap map, String indent) {
                    if (map != null) {
                        log.info("{}--- Begin PrincipalRecord ---", indent);
                        dumpRecord(map, Arrays.asList(Authz.PrincipalRecord.GROUPS), "PrincipalRecord", indent);
                        for (ExtMap group : map.get(Authz.PrincipalRecord.GROUPS, Collections.<ExtMap> emptyList())) {
                            GROUP_RECORD.dump(group, indent + "  ");
                        }
                        log.info("{}--- End   PrincipalRecord ---", indent);
                    }
                }
            }
        ),
        GROUP_RECORD(
            new DumpFormat() {
                @Override
                public void dump(ExtMap map, String indent) {
                    if (map != null) {
                        log.info("{}--- Begin GroupRecord ---", indent);
                        dumpRecord(map, Arrays.asList(Authz.GroupRecord.GROUPS), "GroupRecord", indent);
                        for (ExtMap group : map.get(Authz.GroupRecord.GROUPS, Collections.<ExtMap> emptyList())) {
                            dump(group, indent + "  ");
                        }
                        log.info("{}--- End   GroupRecord ---", indent);
                    }
                }
            }
        ),
        QUERY_FILTER_RECORD(
            new DumpFormat() {
                @Override
                public void dump(ExtMap map, String indent) {
                    if (map != null) {
                        log.info("{}--- Begin QueryFilterRecord ---", indent);
                        dumpRecord(map, Arrays.asList(Authz.QueryFilterRecord.FILTER), "QueryFilterRecord", indent);
                        for (ExtMap filter : map.get(Authz.QueryFilterRecord.FILTER, Collections.<ExtMap> emptyList())) {
                            dump(filter, indent + "  ");
                        }
                        log.info("{}--- End QueryFilterRecord ---", indent);
                    }
                }
            }
        );

        private DumpFormat dumpFormat;

        private Dump(DumpFormat dumpFormat) {
            this.dumpFormat = dumpFormat;
        }

        private static void dumpRecord(ExtMap extMap, List<ExtKey> ignore, String title, String indent) {
            if (extMap != null) {
                log.debug("{}{}: {}", indent, title, extMap);
                for (Map.Entry<ExtKey, Object> entry : extMap.entrySet()) {
                    if (ignore.contains(entry.getKey())) {
                        continue;
                    }
                    log.info("{}    {}: {}", indent, entry.getKey().getUuid().getName(), entry.getValue());
                }
            }
        }

        public void dump(ExtMap map, String indent) {
            dumpFormat.dump(map, indent);
        }

        public void dump(ExtMap map) {
            dump(map, "");
        }
    }

    private ExtMap context;
    private Action action;
    private Map<String, Object> argMap;
    private String password;

    private static <T> List<T> safeList(List<T> list) {
        return list == null ? Collections.<T>emptyList() : list;
    }

    private static ExtensionsManager getExtensionsManager(ExtMap context) {
        return (ExtensionsManager)context.get(ContextKeys.EXTENSION_MANAGER);
    }

    private static ExtensionProxy getExtensionByProfile(ExtMap context, String name) {
        ExtensionProxy ret = getExtensionByConfigKey(context, Authn.ConfigKeys.PROFILE_NAME, name);
        if (ret == null) {
            throw new IllegalArgumentException(String.format("Profile '%1$s' not found", name));
        }
        return ret;
    }

    private static ExtensionProxy getExtensionByConfigKey(ExtMap context, String key, String value) {
        ExtensionProxy ret = null;

        for(ExtensionProxy proxy : getExtensionsManager(context).getExtensionsByService(Authn.class.getName())) {
            if (
                value.equals(
                    proxy.getContext().<Properties>get(
                        Base.ContextKeys.CONFIGURATION
                    ).getProperty(key)
                )
            ) {
                ret = proxy;
                break;
            }
        }

        return ret;
    }

    private static String getPassword(String what) {
        String keyValue[] = what.split(":", 2);
        String type = keyValue[0];
        String value = keyValue[1];

        String password = null;
        if ("pass".equals(type)) {
            password = value;
        } else if ("file".equals(type)) {
            try(
                InputStream is = new FileInputStream(value);
                Reader reader = new InputStreamReader(is, Charset.forName("UTF-8"));
                BufferedReader breader = new BufferedReader(reader);
            ) {
                password = breader.readLine();
            } catch (IOException ex) {
                throw new IllegalArgumentException(String.format("Unable to read file '%s'.", value));
            }
        } else if ("env".equals(type)) {
            password = System.getenv(value);
        } else if ("interactive".equals(type)) {
            if (System.console() == null) {
                throw new RuntimeException("Console is not available, interactive password prompt is impossible");
            }
            System.out.print("Password: ");
            char passwordChars[] = System.console().readPassword();
            if (passwordChars == null) {
                throw new RuntimeException("Cannot read password");
            }
            password = new String(passwordChars);
        } else {
            throw new IllegalArgumentException(String.format("Invalid type: '%s'", type));
        }

        return password;
    }

    public String getName() {
        return "aaa";
    }

    public String getDescription() {
        return "AAA interfaces.";
    }

    public void setContext(ExtMap context) {
        this.context = context;
    }

    public ExtMap getContext() {
        return context;
    }

    @Override
    public void parseArguments(List<String> args) throws Exception {
        args.remove(0);

        Properties props = new Properties();
        try (
            InputStream in = AAAServiceImpl.class.getResourceAsStream("arguments.properties");
            Reader reader = new InputStreamReader(in, Charset.forName("UTF-8"));
        ) {
            props.load(reader);
        }
        Map<String, String> substitutions = (Map)context.get(ContextKeys.CLI_PARSER_SUBSTITUTIONS);
        ArgumentsParser parser = new ArgumentsParser(props, "module");
        parser.getSubstitutions().putAll(substitutions);
        parser.parse(args);
        Map<String, Object> moduleArgs = parser.getParsedArgs();

        if((Boolean)moduleArgs.get("help")) {
            System.out.format("Usage: %s", parser.getUsage());
            throw new ExitException("Help", 0);
        }
        if(!parser.getErrors().isEmpty()) {
            for(Throwable t : parser.getErrors()) {
                log.error(t.getMessage());
            }
            throw new ExitException("Parsing error", 1);
        }

        if (args.size() < 1) {
            log.error("Action not provided");
            throw new ExitException("Action not provided", 1);
        }

        try {
            action = Action.valueOf(args.get(0).toUpperCase().replace("-", "_"));
        } catch(IllegalArgumentException e) {
            log.error("Invalid action '{}'", args.get(0));
            throw new ExitException("Invalid action", 1);
        }

        argMap = action.parse(substitutions, props, args);
    }

    @Override
    public void run() throws Exception {
        action.execute(context, argMap);
    }

    private static String getNamespace(ExtensionProxy authzExtension, String namespace) {
        if(namespace == null) {
            for (String nm : authzExtension.getContext().get(Authz.ContextKeys.AVAILABLE_NAMESPACES, Collections.<String>emptyList())) {
                namespace = nm;
                break;
            }
        }
        return namespace;
    }

    private static ExtMap createQueryFilter(ExtUUID entity, Map<String, Object> argMap) {
        List<ExtMap> filter = new ArrayList<>();

        for (String name : safeList((List<String>) argMap.get("entity-name"))) {
            filter.add(
                createQueryFilterElement(
                    Authz.QueryEntity.GROUP.equals(entity) ? Authz.GroupRecord.NAME : Authz.PrincipalRecord.NAME,
                    name
                )
            );
        }

        for (String id : safeList((List<String>)argMap.get("entity-id"))) {
            filter.add(
                createQueryFilterElement(
                    Authz.QueryEntity.GROUP.equals(entity) ? Authz.GroupRecord.ID : Authz.PrincipalRecord.ID,
                    id
                )
            );
        }

        return new ExtMap().mput(
            Authz.InvokeKeys.QUERY_ENTITY,
            entity
        ).mput(
            Authz.QueryFilterRecord.OPERATOR,
            Authz.QueryFilterOperator.OR
        ).mput(
            Authz.QueryFilterRecord.FILTER,
            filter
        );
    }

    private static ExtMap createQueryFilterElement(ExtKey key, String value) {
        return new ExtMap().mput(
            Authz.QueryFilterRecord.OPERATOR,
            Authz.QueryFilterOperator.EQ
        ).mput(
            Authz.QueryFilterRecord.KEY,
            key
        ).mput(
            key,
            value
        );
    }

    private static ExtUUID getQueryEntity(String entity)  {
        try {
            return (ExtUUID) Authz.QueryEntity.class.getDeclaredField(
                entity.toUpperCase()
            ).get(Authz.QueryEntity.class);
        } catch (IllegalAccessException | NoSuchFieldException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static int getAuthzFlags(List<String> flags) {
        int ret = 0;
        for (String f : safeList(flags)) {
            try {
                ret |= Authz.QueryFlags.class.getDeclaredField(f.toUpperCase().replace("-", "_")).getInt(new Authz.QueryFlags());
            } catch(NoSuchFieldException | IllegalAccessException ex) {
                log.error("Unknown Authz flag '{}' (ignored)", f);
            }
        }
        return ret;
    }

    public static <T> String getFieldNameByValue(Class clz, T v) {
        for (Field f : clz.getFields()) {
            try {
                if (v.equals(f.get(null))) {
                    return f.getName();
                }
            } catch (IllegalAccessException e) {
                // ignore
            }
        }
        return null;
    }

    private static void acctReport(ExtMap context, int reason, String authzName, ExtMap authRecord, ExtMap principalRecord, String user, String msg) {

        String displayUser = null;
        if (displayUser == null && principalRecord != null) {
            displayUser = principalRecord.<String>get(Authz.PrincipalRecord.NAME);
        }
        if (displayUser == null && authRecord != null) {
            displayUser = authRecord.<String>get(Authn.AuthRecord.PRINCIPAL);
        }
        if (displayUser == null) {
            displayUser = user;
        }
        String displayMessage = String.format(msg, displayUser);

        List<ExtensionProxy> acctExtensions = getExtensionsManager(context).getExtensionsByService(Acct.class.getName());
        if (acctExtensions != null) {
            ExtMap input = new ExtMap().mput(
                Acct.InvokeKeys.REASON,
                reason
            ).mput(
                Base.InvokeKeys.COMMAND,
                Acct.InvokeCommands.REPORT
            ).mput(
                Acct.InvokeKeys.PRINCIPAL_RECORD,
                new ExtMap().mput(
                    Acct.PrincipalRecord.AUTHZ_NAME,
                    authzName
                ).mput(
                    Acct.PrincipalRecord.AUTH_RECORD,
                    authRecord
                ).mput(
                    Acct.PrincipalRecord.PRINCIPAL_RECORD,
                    principalRecord
                ).mput(
                    Acct.PrincipalRecord.USER,
                    displayUser
                ).mput(
                    Acct.InvokeKeys.MESSAGE,
                    String.format(displayMessage, displayUser)
                )
            );
            for (ExtensionProxy proxy : acctExtensions) {
                log.info(
                    "API: -->Acct.InvokeCommands.REPORT extension={}, reason={}, user='{}', message='{}'",
                    proxy.getContext().get(Base.ContextKeys.INSTANCE_NAME),
                    getFieldNameByValue(Acct.ReportReason.class, reason),
                    displayUser,
                    displayMessage
                );
                proxy.invoke(input);
                log.info("API: <--Acct.InvokeCommands.REPORT");
            }
        }
    }
}

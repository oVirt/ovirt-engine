package org.ovirt.engine.core.sso.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.ovirt.engine.api.extensions.Base;
import org.ovirt.engine.api.extensions.ExtMap;
import org.ovirt.engine.api.extensions.aaa.Authn;
import org.ovirt.engine.api.extensions.aaa.Authz;
import org.ovirt.engine.core.extensions.mgr.ExtensionProxy;

public class AuthzUtils {

    @FunctionalInterface
    private interface QueryResultHandler {
        boolean handle(Collection<ExtMap> queryResults);
    }

    private static final int QUERIES_RESULTS_LIMIT = 1000;
    private static final int PAGE_SIZE = 500;

    public static String getName(ExtensionProxy proxy) {
        return proxy.getContext().get(Base.ContextKeys.INSTANCE_NAME);
    }

    public static boolean supportsPasswordAuthentication(ExtensionProxy proxy) {
        return (proxy.getContext().<Long> get(Authn.ContextKeys.CAPABILITIES, 0L)
                & Authn.Capabilities.AUTHENTICATE_PASSWORD) != 0;
    }

    public static ExtMap fetchPrincipalRecord(
            final ExtensionProxy extension,
            String principal,
            boolean resolveGroups,
            boolean resolveGroupsRecursive) {
        return fetchPrincipalRecordImpl(
                extension,
                new ExtMap().mput(
                        Authz.InvokeKeys.PRINCIPAL,
                        principal),
                resolveGroups,
                resolveGroupsRecursive);
    }

    private static ExtMap fetchPrincipalRecordImpl(
            final ExtensionProxy extension,
            ExtMap m,
            boolean resolveGroups,
            boolean resolveGroupsRecursive) {
        ExtMap ret = null;
        ExtMap output = extension.invoke(
                m.mput(
                        Base.InvokeKeys.COMMAND,
                        Authz.InvokeCommands.FETCH_PRINCIPAL_RECORD)
                        .mput(
                                Authz.InvokeKeys.QUERY_FLAGS,
                                (resolveGroups ? Authz.QueryFlags.RESOLVE_GROUPS : 0) |
                                        (resolveGroupsRecursive ? Authz.QueryFlags.RESOLVE_GROUPS_RECURSIVE : 0)));
        if (output.<Integer> get(Authz.InvokeKeys.STATUS) == Authz.Status.SUCCESS) {
            ret = output.get(Authz.InvokeKeys.PRINCIPAL_RECORD);
        }
        return ret;
    }

    public static Collection<ExtMap> queryPrincipalRecords(
            final ExtensionProxy extension,
            final String namespace,
            final ExtMap filter,
            boolean groupsResolving,
            boolean groupsResolvingRecursive) {
        ExtMap inputMap = new ExtMap().mput(
                Authz.InvokeKeys.QUERY_ENTITY,
                Authz.QueryEntity.PRINCIPAL)
                .mput(
                        Authz.InvokeKeys.QUERY_FLAGS,
                        queryFlagValue(groupsResolving, groupsResolvingRecursive))
                .mput(
                        Authz.InvokeKeys.QUERY_FILTER,
                        filter)
                .mput(
                        Authz.InvokeKeys.NAMESPACE,
                        namespace);
        return populateRecords(
                extension,
                namespace,
                inputMap);

    }

    public static Collection<ExtMap> queryGroupRecords(
            final ExtensionProxy extension,
            final String namespace,
            final ExtMap filter,
            boolean groupsResolving,
            boolean groupsResolvingRecursive) {
        ExtMap inputMap = new ExtMap().mput(
                Authz.InvokeKeys.QUERY_ENTITY,
                Authz.QueryEntity.GROUP)
                .mput(
                        Authz.InvokeKeys.QUERY_FLAGS,
                        queryFlagValue(groupsResolving, groupsResolvingRecursive))
                .mput(
                        Authz.InvokeKeys.QUERY_FILTER,
                        filter)
                .mput(
                        Authz.InvokeKeys.NAMESPACE,
                        namespace);
        return populateRecords(
                extension,
                namespace,
                inputMap);

    }

    public static Collection<ExtMap> populateRecords(
            final ExtensionProxy extension,
            final String namespace,
            final ExtMap input) {
        final Collection<ExtMap> records = new ArrayList<>();
        queryImpl(extension, namespace, input, queryResults -> {
            boolean result = true;
            for (ExtMap queryResult : queryResults) {
                if (records.size() < QUERIES_RESULTS_LIMIT) {
                    records.add(queryResult);
                } else {
                    result = false;
                    break;
                }
            }
            return result;
        });
        return records;
    }

    private static void queryImpl(
            final ExtensionProxy extension,
            final String namespace,
            final ExtMap input,
            final QueryResultHandler handler) {
        Object opaque = extension.invoke(
                new ExtMap().mput(
                        Base.InvokeKeys.COMMAND,
                        Authz.InvokeCommands.QUERY_OPEN)
                        .mput(
                                Authz.InvokeKeys.NAMESPACE,
                                namespace)
                        .mput(
                                input))
                .get(Authz.InvokeKeys.QUERY_OPAQUE);
        Collection<ExtMap> result = null;
        try {
            do {
                result = extension.invoke(
                        new ExtMap().mput(
                                Base.InvokeKeys.COMMAND,
                                Authz.InvokeCommands.QUERY_EXECUTE)
                                .mput(
                                        Authz.InvokeKeys.QUERY_OPAQUE,
                                        opaque)
                                .mput(
                                        Authz.InvokeKeys.PAGE_SIZE,
                                        PAGE_SIZE))
                        .get(Authz.InvokeKeys.QUERY_RESULT);
            } while (result != null && handler.handle(result));
        } finally {
            extension.invoke(
                    new ExtMap().mput(
                            Base.InvokeKeys.COMMAND,
                            Authz.InvokeCommands.QUERY_CLOSE)
                            .mput(
                                    Authz.InvokeKeys.QUERY_OPAQUE,
                                    opaque));
        }
    }

    public static List<ExtMap> findPrincipalsByIds(
            final ExtensionProxy extension,
            final String namespace,
            final Collection<String> ids,
            final boolean groupsResolving,
            final boolean groupsResolvingRecursive) {
        List<ExtMap> results = new ArrayList<>();
        SearchParsingUtils.getIdsBatches(extension.getContext(), ids)
                .forEach(
                        batch -> results.addAll(
                                queryPrincipalRecords(
                                        extension,
                                        namespace,
                                        SearchParsingUtils.generateQueryMap(
                                                batch,
                                                Authz.QueryEntity.PRINCIPAL),
                                        groupsResolving,
                                        groupsResolvingRecursive)));
        return results;
    }

    public static Collection<ExtMap> findGroupRecordsByIds(
            final ExtensionProxy extension,
            final String namespace,
            final Collection<String> ids,
            final boolean groupsResolving,
            final boolean groupsResolvingRecursive) {
        Collection<ExtMap> results = new ArrayList<>();
        SearchParsingUtils.getIdsBatches(extension.getContext(), ids)
                .forEach(
                        batch -> results.addAll(
                                queryGroupRecords(
                                        extension,
                                        namespace,
                                        SearchParsingUtils.generateQueryMap(
                                                batch,
                                                Authz.QueryEntity.GROUP),
                                        groupsResolving,
                                        groupsResolvingRecursive)));
        return results;
    }

    private static int queryFlagValue(boolean resolveGroups, boolean resolveGroupsRecursive) {
        int result = 0;
        if (resolveGroups) {
            result |= Authz.QueryFlags.RESOLVE_GROUPS;
        }
        if (resolveGroupsRecursive) {
            result |= Authz.QueryFlags.RESOLVE_GROUPS_RECURSIVE | Authz.QueryFlags.RESOLVE_GROUPS;
        }
        return result;
    }

}

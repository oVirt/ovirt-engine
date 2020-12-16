package org.ovirt.engine.core.sso.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.ovirt.engine.api.extensions.Base;
import org.ovirt.engine.api.extensions.ExtMap;
import org.ovirt.engine.api.extensions.aaa.Authz;
import org.ovirt.engine.core.extensions.mgr.ExtensionProxy;

public class DirectoryUtils {

    @FunctionalInterface
    private interface QueryResultHandler {
        boolean handle(Collection<ExtMap> queryResults);
    }

    private static final int QUERIES_RESULTS_LIMIT = 1000;
    private static final int PAGE_SIZE = 500;

    public static Collection<ExtMap> findDirectoryUsersByQuery(
            final ExtensionProxy extension,
            final String namespace,
            final String query) {
        return queryDirectoryUsers(
                extension,
                namespace,
                SearchParsingUtils.generateQueryMap(
                        query,
                        Authz.QueryEntity.PRINCIPAL),
                false,
                false);
    }

    public static Collection<ExtMap> findDirectoryGroupsByQuery(
            final ExtensionProxy extension,
            final String namespace,
            final String query) {
        return queryDirectoryGroups(
                extension,
                namespace,
                SearchParsingUtils.generateQueryMap(query, Authz.QueryEntity.GROUP),
                false,
                false);
    }

    private static Collection<ExtMap> queryDirectoryUsers(
            final ExtensionProxy extension,
            final String namespace,
            final ExtMap filter,
            boolean groupsResolving,
            boolean groupsResolvingRecursive) {
        return queryPrincipalRecords(extension,
                namespace,
                filter,
                groupsResolving,
                groupsResolvingRecursive);
    }

    private static List<ExtMap> queryDirectoryGroups(
            final ExtensionProxy extension,
            final String namespace,
            final ExtMap filter,
            boolean groupsResolving,
            boolean groupsResolvingRecursive) {
        return queryGroupRecords(extension, namespace, filter, groupsResolving, groupsResolvingRecursive)
                .stream()
                .collect(Collectors.toList());
    }

    private static Collection<ExtMap> queryPrincipalRecords(
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

    private static Collection<ExtMap> queryGroupRecords(
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

    private static Collection<ExtMap> populateRecords(
            final ExtensionProxy extension,
            final String namespace,
            final ExtMap input) {
        final List<ExtMap> records = new ArrayList<>();
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

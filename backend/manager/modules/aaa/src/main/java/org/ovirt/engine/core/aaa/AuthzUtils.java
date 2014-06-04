package org.ovirt.engine.core.aaa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.ovirt.engine.api.extensions.Base;
import org.ovirt.engine.api.extensions.ExtMap;
import org.ovirt.engine.api.extensions.aaa.Authn;
import org.ovirt.engine.api.extensions.aaa.Authz;
import org.ovirt.engine.api.extensions.aaa.Authz.QueryEntity;
import org.ovirt.engine.core.extensions.mgr.ExtensionProxy;

public class AuthzUtils {


    private static interface QueryResultHandler {
        public boolean handle(List<ExtMap> queryResults);
    }

    private static final int QUERIES_RESULTS_LIMIT = 1000;
    private static final int PAGE_SIZE = 500;


    public static String getName(ExtensionProxy proxy) {
        return proxy.getContext().<String> get(Base.ContextKeys.INSTANCE_NAME);
    }

    public static DirectoryUser fetchPrincipalRecord(final ExtensionProxy extension, ExtMap authRecord) {
        return mapPrincipalRecord(extension, extension.invoke(new ExtMap().mput(
                Base.InvokeKeys.COMMAND,
                Authz.InvokeCommands.FETCH_PRINCIPAL_RECORD
                ).mput(
                        Authn.InvokeKeys.AUTH_RECORD,
                        authRecord
                ).mput(
                        Authz.InvokeKeys.QUERY_FLAGS,
                        Authz.QueryFlags.RESOLVE_GROUPS | Authz.QueryFlags.RESOLVE_GROUPS_RECURSIVE
                )).<ExtMap> get(Authz.InvokeKeys.PRINCIPAL_RECORD));

    }

    public static List<DirectoryUser> findPrincipalsByQuery(
            final ExtensionProxy extension,
            final String namespace,
            final String query) {
        return queryPrincipals(
                extension,
                namespace,
                SearchQueryParsingUtils.generateQueryMap(
                        query,
                        Authz.QueryEntity.PRINCIPAL
                        ),
                false,
                false);
    }

    public static List<DirectoryUser> findPrincipalsByIds(
            final ExtensionProxy extension,
            final String namespace,
            final List<String> ids,
            final boolean groupsResolving,
            final boolean groupsResolvingRecursive
            ) {
        List<DirectoryUser> results = new ArrayList<>();
        for (List<String> batch : SearchQueryParsingUtils.getIdsBatches(extension.getContext(), ids)) {
            results.addAll(
                    queryPrincipals(
                            extension,
                            namespace,
                            SearchQueryParsingUtils.generateQueryMap(
                                    batch,
                                    Authz.QueryEntity.PRINCIPAL
                                    ),
                            groupsResolving,
                            groupsResolvingRecursive
                    )
                    );
        }
        return results;
    }

    public static DirectoryUser findPrincipalById(
            final ExtensionProxy extension,
            final String namespace,
            final String id,
            final boolean groupsResolving,
            final boolean groupsResolvingRecursive
            ) {
        List<DirectoryUser> users =
                findPrincipalsByIds(extension, namespace, Arrays.asList(id), groupsResolving, groupsResolvingRecursive);
        if (users.isEmpty()) {
            return null;
        }
        return users.get(0);
    }

    public static List<DirectoryGroup> findGroupsByQuery(
            final ExtensionProxy extension,
            final String namespace,
            final String query) {
        return queryGroups(
                extension,
                namespace,
                SearchQueryParsingUtils.generateQueryMap(query, Authz.QueryEntity.GROUP),
                false,
                false
                );
    }

    public static List<DirectoryGroup> findGroupsByIds(
            final ExtensionProxy extension,
            final String namespace,
            final List<String> ids,
            final boolean resolveGroups,
            final boolean resolveGroupsRecursive) {
        List<DirectoryGroup> results = new ArrayList<>();
        for (List<String> batch : SearchQueryParsingUtils.getIdsBatches(extension.getContext(), ids)) {
            results.addAll(
                    queryGroups(
                            extension,
                            namespace,
                            SearchQueryParsingUtils.generateQueryMap(batch, QueryEntity.GROUP),
                            resolveGroups,
                            resolveGroupsRecursive
                            )
                    );
        }
        return results;
    }

    public static DirectoryGroup findGroupById(final ExtensionProxy extension,
            String namespace,
            final String id,
            final boolean resolveGroups,
            final boolean resolveGroupsRecursive
            ) {
        List<DirectoryGroup> groups = findGroupsByIds(extension, namespace, Arrays.asList(id), resolveGroups, resolveGroupsRecursive);
        if (groups.isEmpty()) {
            return null;
        }
        return groups.get(0);
    }

    private static List<DirectoryUser> queryPrincipals(
            final ExtensionProxy extension,
            final String namespace,
            final ExtMap filter,
            boolean groupsResolving,
            boolean groupsResolvingRecursive
            ) {
        ExtMap inputMap = new ExtMap().mput(
                Authz.InvokeKeys.QUERY_ENTITY,
                Authz.QueryEntity.PRINCIPAL
                ).mput(
                        Authz.InvokeKeys.QUERY_FLAGS,
                        queryFlagValue(groupsResolving, groupsResolvingRecursive)
                ).mput(
                        Authz.InvokeKeys.QUERY_FILTER,
                        filter
                ).mput(
                        Authz.InvokeKeys.NAMESPACE,
                        namespace
                );
        return populatePrincipals(
                extension,
                namespace,
                inputMap);

    }


    private static List<DirectoryGroup> queryGroups(
            final ExtensionProxy extension,
            final String namespace,
            final ExtMap filter,
            boolean resolveGroups,
            boolean recursiveGroupResolving
            ) {
        return populateGroups(
                extension,
                namespace,
                new ExtMap().mput(
                        Authz.InvokeKeys.QUERY_ENTITY,
                        Authz.QueryEntity.GROUP
                        ).mput(
                                Authz.InvokeKeys.QUERY_FILTER,
                                filter
                        ).mput(
                                Authz.InvokeKeys.QUERY_FLAGS,
                                queryFlagValue(resolveGroups, recursiveGroupResolving)
                        )
              );
    }

    private static List<DirectoryUser> populatePrincipals(
            final ExtensionProxy extension,
            final String namespace,
            final ExtMap input) {
        final List<DirectoryUser> directoryUsers = new ArrayList<>();
        queryImpl(extension, namespace, input, new QueryResultHandler() {

            @Override
            public boolean handle(List<ExtMap> queryResults) {
                boolean result = true;
                for (ExtMap queryResult : queryResults) {
                    if (directoryUsers.size() < QUERIES_RESULTS_LIMIT) {
                        directoryUsers.add(mapPrincipalRecord(extension, queryResult));
                    } else {
                        result = false;
                        break;
                    }
                }
                return result;
            }
        });
        return directoryUsers;
    }

    private static List<DirectoryGroup> populateGroups(final ExtensionProxy extension, final String namespace,
            final ExtMap input) {
        final List<DirectoryGroup> directoryGroups = new ArrayList<>();
        queryImpl(extension, namespace, input, new QueryResultHandler() {
            @Override
            public boolean handle(List<ExtMap> queryResults) {

                boolean result = true;
                for (ExtMap queryResult : queryResults) {
                    if (directoryGroups.size() < QUERIES_RESULTS_LIMIT) {
                        directoryGroups.add(mapGroupRecord(extension, queryResult));
                    } else {
                        result = false;
                    }
                }
                return result;
            }

        });
        return directoryGroups;
    }

    private static void queryImpl(
            final ExtensionProxy extension,
            final String namespace,
            final ExtMap input,
            final QueryResultHandler handler
            ) {
        Object opaque = extension.invoke(
                new ExtMap().mput(
                        Base.InvokeKeys.COMMAND,
                        Authz.InvokeCommands.QUERY_OPEN
                        ).mput(
                                Authz.InvokeKeys.NAMESPACE,
                                namespace
                        ).mput(
                                input
                        )
                ).get(Authz.InvokeKeys.QUERY_OPAQUE);
        List<ExtMap> result = null;
        try {
            do {
                result = extension.invoke(new ExtMap().mput(
                        Base.InvokeKeys.COMMAND,
                        Authz.InvokeCommands.QUERY_EXECUTE
                        ).mput(
                                Authz.InvokeKeys.QUERY_OPAQUE,
                                opaque
                        ).mput(
                                Authz.InvokeKeys.PAGE_SIZE,
                                PAGE_SIZE
                        )
                        ).get(Authz.InvokeKeys.QUERY_RESULT);
            } while (result != null && handler.handle(result));
        } finally {
            extension.invoke(new ExtMap().mput(
                    Base.InvokeKeys.COMMAND,
                    Authz.InvokeCommands.QUERY_CLOSE
                    ).mput(
                            Authz.InvokeKeys.QUERY_OPAQUE,
                            opaque
                    )
                    );
        }
    }

    private static DirectoryUser mapPrincipalRecord(final ExtensionProxy extension, final ExtMap principalRecord) {
        DirectoryUser directoryUser = null;
        if (principalRecord != null) {
            directoryUser = new DirectoryUser(
                    extension.getContext().<String> get(Base.ContextKeys.INSTANCE_NAME),
                    principalRecord.<String> get(Authz.PrincipalRecord.NAMESPACE),
                    principalRecord.<String> get(Authz.PrincipalRecord.ID),
                    principalRecord.<String> get(Authz.PrincipalRecord.NAME)
                    );
            directoryUser.setDepartment(principalRecord.<String> get(Authz.PrincipalRecord.DEPARTMENT));
            directoryUser.setFirstName(principalRecord.<String> get(Authz.PrincipalRecord.FIRST_NAME));
            directoryUser.setLastName(principalRecord.<String> get(Authz.PrincipalRecord.LAST_NAME));
            directoryUser.setEmail(principalRecord.<String> get(Authz.PrincipalRecord.EMAIL));
            directoryUser.setTitle(principalRecord.<String> get(Authz.PrincipalRecord.TITLE));
            List<DirectoryGroup> directoryGroups = new ArrayList<DirectoryGroup>();
            List<ExtMap> groups = principalRecord.<List<ExtMap>> get(Authz.PrincipalRecord.GROUPS);
            if (groups != null) {
                for (ExtMap group : groups) {
                    directoryGroups.add(mapGroupRecord(extension, group));
                }
            }
            directoryUser.setGroups(directoryGroups);
        }
        return directoryUser;
    }

    private static DirectoryGroup mapGroupRecord(final ExtensionProxy extension, final ExtMap group) {
        DirectoryGroup directoryGroup = null;
        if (group != null) {
            directoryGroup = new DirectoryGroup(
                    extension.getContext().<String> get(Base.ContextKeys.INSTANCE_NAME),
                    group.<String> get(Authz.GroupRecord.NAMESPACE),
                    group.<String> get(Authz.GroupRecord.ID),
                    group.<String> get(Authz.GroupRecord.NAME)
                    );
            for (ExtMap memberOf : group.<List<ExtMap>> get(Authz.GroupRecord.GROUPS, Collections.<ExtMap> emptyList())) {
                directoryGroup.getGroups().add(mapGroupRecord(extension, memberOf));
            }
        }
        return directoryGroup;
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

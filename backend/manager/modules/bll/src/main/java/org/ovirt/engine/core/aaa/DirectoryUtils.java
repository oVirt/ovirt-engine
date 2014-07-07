package org.ovirt.engine.core.aaa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ovirt.engine.api.extensions.ExtMap;
import org.ovirt.engine.api.extensions.aaa.Authz;
import org.ovirt.engine.core.common.businessentities.DbGroup;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.DbGroupDAO;
import org.ovirt.engine.core.extensions.mgr.ExtensionProxy;

public class DirectoryUtils {

    public static HashSet<Guid> getGroupIdsFromUser(DirectoryUser directoryUser) {
        HashSet<Guid> results = new HashSet<Guid>();
        DbGroupDAO dao = DbFacade.getInstance().getDbGroupDao();
        if (directoryUser.getGroups() != null) {
            for (DirectoryGroup group : directoryUser.getGroups()) {
                DbGroup dbGroup = dao.getByExternalId(group.getDirectoryName(), group.getId());
                if (dbGroup != null) {
                    results.add(dbGroup.getId());
                }
            }
        }
        return results;
    }

    public static List<DirectoryUser> findDirectoryUsersByQuery(
            final ExtensionProxy extension,
            final String namespace,
            final String query) {
        return queryDirectoryUsers(
                extension,
                namespace,
                SearchQueryParsingUtils.generateQueryMap(
                        query,
                        Authz.QueryEntity.PRINCIPAL
                        ),
                false,
                false);
    }

    public static DirectoryGroup findDirectoryGroupById(final ExtensionProxy extension,
            String namespace,
            final String id,
            final boolean resolveGroups,
            final boolean resolveGroupsRecursive
            ) {
        List<DirectoryGroup> groups =
                findDirectoryGroupsByIds(extension, namespace, Arrays.asList(id), resolveGroups, resolveGroupsRecursive);
        if (groups.isEmpty()) {
            return null;
        }
        return groups.get(0);
    }

    public static List<DirectoryUser> findDirectoryUserByIds(
            final ExtensionProxy extension,
            final String namespace,
            final List<String> ids,
            final boolean groupsResolving,
            final boolean groupsResolvingRecursive
            ) {
        return mapPrincipalRecords(
                AuthzUtils.getName(extension),
                AuthzUtils.findPrincipalsByIds(
                        extension,
                        namespace,
                        ids,
                        groupsResolving,
                        groupsResolvingRecursive
                        )
                );
    }

    public static DirectoryUser findDirectoryUserById(
            final ExtensionProxy extension,
            final String namespace,
            final String id,
            final boolean groupsResolving,
            final boolean groupsResolvingRecursive
            ) {
        List<DirectoryUser> users =
                findDirectoryUserByIds(
                        extension,
                        namespace,
                        Arrays.asList(id),
                        groupsResolving,
                        groupsResolvingRecursive);
        if (users.isEmpty()) {
            return null;
        }
        return users.get(0);
    }

    public static List<DirectoryGroup> findDirectoryGroupsByQuery(
            final ExtensionProxy extension,
            final String namespace,
            final String query) {
        return queryDirectoryGroups(
                extension,
                namespace,
                SearchQueryParsingUtils.generateQueryMap(query, Authz.QueryEntity.GROUP),
                false,
                false);
    }

    public static List<DirectoryGroup> findDirectoryGroupsByIds(
            final ExtensionProxy extension,
            final String namespace,
            final List<String> ids,
            final boolean resolveGroups,
            final boolean resolveGroupsRecursive) {
        return mapGroupRecords(AuthzUtils.getName(extension),
                AuthzUtils.findGroupRecordsByIds(
                        extension,
                        namespace,
                        ids,
                        resolveGroups,
                        resolveGroupsRecursive)
                        );
    }

    public static DirectoryUser mapPrincipalRecord(final String authzName, final ExtMap principalRecord) {
        DirectoryUser directoryUser = null;
        if (principalRecord != null) {
            directoryUser = new DirectoryUser(
                    authzName,
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
                    directoryGroups.add(mapGroupRecord(authzName, group));
                }
            }
            directoryUser.setGroups(directoryGroups);
        }
        return directoryUser;
    }

    public static DirectoryGroup mapGroupRecord(final String authzName, final ExtMap group) {
        DirectoryGroup directoryGroup = null;
        if (group != null) {
            directoryGroup = new DirectoryGroup(
                    authzName,
                    group.<String> get(Authz.GroupRecord.NAMESPACE),
                    group.<String> get(Authz.GroupRecord.ID),
                    group.<String> get(Authz.GroupRecord.NAME)
                    );
            for (ExtMap memberOf : group.<List<ExtMap>> get(Authz.GroupRecord.GROUPS, Collections.<ExtMap> emptyList())) {
                directoryGroup.getGroups().add(mapGroupRecord(authzName, memberOf));
            }
        }
        return directoryGroup;
    }

    public static List<DirectoryGroup> mapGroupRecords(final String authzName, final List<ExtMap> groups) {
        List<DirectoryGroup> results = new ArrayList<>();
        for (ExtMap group : groups) {
            results.add(mapGroupRecord(authzName, group));
        }
        return results;
    }

    public static List<DirectoryUser> mapPrincipalRecords(final String authzName, final List<ExtMap> users) {
        List<DirectoryUser> results = new ArrayList<>();
        for (ExtMap user : users) {
            results.add(mapPrincipalRecord(authzName, user));
        }
        return results;
    }

    private static List<DirectoryUser> queryDirectoryUsers(
            final ExtensionProxy extension,
            final String namespace,
            final ExtMap filter,
            boolean groupsResolving,
            boolean groupsResolvingRecursive
            ) {
        return mapPrincipalRecords(AuthzUtils.getName(extension), AuthzUtils.queryPrincipalRecords(extension,
                namespace,
                filter,
                groupsResolving,
                groupsResolvingRecursive));
    }

    private static List<DirectoryGroup> queryDirectoryGroups(
            final ExtensionProxy extension,
            final String namespace,
            final ExtMap filter,
            boolean groupsResolving,
            boolean groupsResolvingRecursive
            ) {
                List<DirectoryGroup> directoryGroups = new ArrayList<>();
                for (ExtMap group : AuthzUtils.queryGroupRecords(extension, namespace, filter, groupsResolving, groupsResolvingRecursive)) {
                    directoryGroups.add(mapGroupRecord(AuthzUtils.getName(extension), group));
                }
                return directoryGroups;
    }

    private static void flatGroups(Set<DirectoryGroup> accumulator, List<DirectoryGroup> groupsFrom) {
        for (DirectoryGroup group : groupsFrom) {
            flatGroups(accumulator, group.getGroups());
            accumulator.add(group);
        }

    }
}

package org.ovirt.engine.core.aaa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.ovirt.engine.api.extensions.ExtMap;
import org.ovirt.engine.api.extensions.aaa.Authz;
import org.ovirt.engine.api.extensions.aaa.Authz.GroupRecord;
import org.ovirt.engine.api.extensions.aaa.Authz.PrincipalRecord;
import org.ovirt.engine.core.common.businessentities.DbGroup;
import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.DbGroupDAO;
import org.ovirt.engine.core.extensions.mgr.ExtensionProxy;

public class DirectoryUtils {

    public static HashSet<Guid> getGroupIdsFromPrincipal(String authz, ExtMap principal) {
        HashSet<Guid> results = new HashSet<Guid>();
        DbGroupDAO dao = DbFacade.getInstance().getDbGroupDao();
        for (ExtMap group : principal.get(PrincipalRecord.GROUPS, Collections.<ExtMap> emptyList())) {
            DbGroup dbGroup = dao.getByExternalId(authz, group.<String> get(GroupRecord.ID));
            if (dbGroup != null) {
                results.add(dbGroup.getId());
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
        return mapPrincipalRecordsToDirectoryUsers(
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
        return mapGroupRecordsToDirectoryGroups(AuthzUtils.getName(extension),
                AuthzUtils.findGroupRecordsByIds(
                        extension,
                        namespace,
                        ids,
                        resolveGroups,
                        resolveGroupsRecursive)
                        );
    }

    public static DbUser mapPrincipalRecordToDbUser(final String authzName, final ExtMap principalRecord) {
        return new DbUser(mapPrincipalRecordToDirectoryUser(authzName, principalRecord));
    }

    public static DirectoryUser mapPrincipalRecordToDirectoryUser(final String authzName, final ExtMap principalRecord) {
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
                    directoryGroups.add(mapGroupRecordToDirectoryGroup(authzName, group));
                }
            }
            directoryUser.setGroups(directoryGroups);
        }
        return directoryUser;
    }

    public static DirectoryGroup mapGroupRecordToDirectoryGroup(final String authzName, final ExtMap group) {
        DirectoryGroup directoryGroup = null;
        if (group != null) {
            directoryGroup = new DirectoryGroup(
                    authzName,
                    group.<String> get(Authz.GroupRecord.NAMESPACE),
                    group.<String> get(Authz.GroupRecord.ID),
                    group.<String> get(Authz.GroupRecord.NAME)
                    );
            for (ExtMap memberOf : group.<List<ExtMap>> get(Authz.GroupRecord.GROUPS, Collections.<ExtMap> emptyList())) {
                directoryGroup.getGroups().add(mapGroupRecordToDirectoryGroup(authzName, memberOf));
            }
        }
        return directoryGroup;
    }

    public static DbGroup mapGroupRecordToDbGroup(String directory, ExtMap groupRecord) {
        return new DbGroup(mapGroupRecordToDirectoryGroup(directory, groupRecord));
    }


    public static void flatGroups(ExtMap principal) {
        List<ExtMap> accumulator = new ArrayList<>();
        flatGroups(accumulator, principal.get(GroupRecord.GROUPS, Collections.<ExtMap> emptyList()));
        principal.put(GroupRecord.GROUPS, accumulator);
    }

    public static List<DirectoryGroup> mapGroupRecordsToDirectoryGroups(final String authzName, final List<ExtMap> groups) {
        List<DirectoryGroup> results = new ArrayList<>();
        for (ExtMap group : groups) {
            results.add(mapGroupRecordToDirectoryGroup(authzName, group));
        }
        return results;
    }

    public static List<DirectoryUser> mapPrincipalRecordsToDirectoryUsers(final String authzName, final List<ExtMap> users) {
        List<DirectoryUser> results = new ArrayList<>();
        for (ExtMap user : users) {
            results.add(mapPrincipalRecordToDirectoryUser(authzName, user));
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
        return mapPrincipalRecordsToDirectoryUsers(AuthzUtils.getName(extension), AuthzUtils.queryPrincipalRecords(extension,
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
                for (ExtMap group : AuthzUtils.queryPrincipalRecords(extension, namespace, filter, groupsResolving, groupsResolvingRecursive)) {
                    directoryGroups.add(mapGroupRecordToDirectoryGroup(AuthzUtils.getName(extension), group));
                }
                return directoryGroups;
    }

    private static void flatGroups(List<ExtMap> accumulator, List<ExtMap> groupsFrom) {
        for (ExtMap group : groupsFrom) {
            flatGroups(accumulator, group.get(GroupRecord.GROUPS, Collections.<ExtMap> emptyList()));
            accumulator.add(group);
        }

    }

}

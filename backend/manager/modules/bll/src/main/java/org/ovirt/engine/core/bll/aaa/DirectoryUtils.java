package org.ovirt.engine.core.bll.aaa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.ovirt.engine.api.extensions.ExtKey;
import org.ovirt.engine.api.extensions.ExtMap;
import org.ovirt.engine.api.extensions.aaa.Authz;
import org.ovirt.engine.api.extensions.aaa.Authz.GroupRecord;
import org.ovirt.engine.api.extensions.aaa.Authz.PrincipalRecord;
import org.ovirt.engine.core.aaa.AuthzUtils;
import org.ovirt.engine.core.aaa.DirectoryGroup;
import org.ovirt.engine.core.aaa.DirectoryUser;
import org.ovirt.engine.core.aaa.SearchQueryParsingUtils;
import org.ovirt.engine.core.common.businessentities.aaa.DbGroup;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.DbGroupDAO;
import org.ovirt.engine.core.extensions.mgr.ExtensionProxy;

public class DirectoryUtils {

    public static DbUser mapPrincipalRecordToDbUser(String authz, ExtMap principal) {
        principal = principal.clone();
        flatGroups(principal);
        DbUser dbUser = DbFacade.getInstance().getDbUserDao().getByExternalId(authz,  principal.<String>get(PrincipalRecord.ID));
        Guid userId = dbUser != null ? dbUser.getId() : Guid.newGuid();
        dbUser = new DbUser(mapPrincipalRecordToDirectoryUser(authz, principal));
        dbUser.setId(userId);
        DbGroupDAO dao = DbFacade.getInstance().getDbGroupDao();
        LinkedList<Guid> groupIds = new LinkedList<Guid>();
        LinkedList<String> groupsNames = new LinkedList<String>();
        for (ExtMap group : principal.get(PrincipalRecord.GROUPS, Collections.<ExtMap> emptyList())) {
            DbGroup dbGroup = dao.getByExternalId(authz, group.<String> get(GroupRecord.ID));
            if (dbGroup != null) {
                groupIds.add(dbGroup.getId());
                groupsNames.add(dbGroup.getName());
            }
        }
        dbUser.setGroupIds(groupIds);
        dbUser.setGroupNames(groupsNames);
        return dbUser;
    }

    public static Collection<DirectoryUser> findDirectoryUsersByQuery(
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
        Collection<DirectoryGroup> groups =
                findDirectoryGroupsByIds(extension, namespace, Arrays.asList(id), resolveGroups, resolveGroupsRecursive);
        if (groups.size() == 0) {
            return null;
        }
        return new ArrayList<DirectoryGroup>(groups).get(0);
    }

    public static Collection<DirectoryUser> findDirectoryUserByIds(
            final ExtensionProxy extension,
            final String namespace,
            final Collection<String> ids,
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
        Collection<DirectoryUser> users =
                findDirectoryUserByIds(
                        extension,
                        namespace,
                        Arrays.asList(id),
                        groupsResolving,
                        groupsResolvingRecursive);
        if (users.size() == 0) {
            return null;
        }
        return new ArrayList<DirectoryUser>(users).get(0);
    }

    public static Collection<DirectoryGroup> findDirectoryGroupsByQuery(
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

    public static Collection<DirectoryGroup> findDirectoryGroupsByIds(
            final ExtensionProxy extension,
            final String namespace,
            final Collection<String> ids,
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
            directoryUser.setPrincipal(principalRecord.<String> get(Authz.PrincipalRecord.PRINCIPAL));
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
        principal.put(PrincipalRecord.GROUPS, flatGroups(principal, PrincipalRecord.GROUPS, new ArrayList<ExtMap>()));
    }

    private static List<ExtMap> flatGroups(ExtMap entity, ExtKey key, List<ExtMap> accumulator) {
        for (ExtMap group : entity.get(key, Collections.<ExtMap> emptyList())) {
            accumulator.add(group);
            flatGroups(group, GroupRecord.GROUPS, accumulator);
        }
        return accumulator;
    }

    public static Collection<DirectoryGroup> mapGroupRecordsToDirectoryGroups(final String authzName,
            final Collection<ExtMap> groups) {
        List<DirectoryGroup> results = new ArrayList<>();
        for (ExtMap group : groups) {
            results.add(mapGroupRecordToDirectoryGroup(authzName, group));
        }
        return results;
    }

    public static Collection<DirectoryUser> mapPrincipalRecordsToDirectoryUsers(final String authzName,
            final Collection<ExtMap> users) {
        List<DirectoryUser> results = new ArrayList<>();
        for (ExtMap user : users) {
            results.add(mapPrincipalRecordToDirectoryUser(authzName, user));
        }
        return results;
    }

    private static Collection<DirectoryUser> queryDirectoryUsers(
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
                for (ExtMap group : AuthzUtils.queryGroupRecords(extension, namespace, filter, groupsResolving, groupsResolvingRecursive)) {
                    directoryGroups.add(mapGroupRecordToDirectoryGroup(AuthzUtils.getName(extension), group));
                }
                return directoryGroups;
    }

}

package org.ovirt.engine.core.bll.aaa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ovirt.engine.api.extensions.ExtKey;
import org.ovirt.engine.api.extensions.ExtMap;
import org.ovirt.engine.api.extensions.aaa.Authz;
import org.ovirt.engine.api.extensions.aaa.Authz.GroupRecord;
import org.ovirt.engine.api.extensions.aaa.Authz.PrincipalRecord;
import org.ovirt.engine.core.aaa.DirectoryGroup;
import org.ovirt.engine.core.aaa.DirectoryUser;
import org.ovirt.engine.core.common.businessentities.aaa.DbGroup;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.DbGroupDao;

public class DirectoryUtils {

    public static DbUser mapPrincipalRecordToDbUser(String authz, ExtMap principal) {
        principal = principal.clone();
        flatGroups(principal);
        DbUser dbUser = DbFacade.getInstance().getDbUserDao().getByExternalId(authz,  principal.<String>get(PrincipalRecord.ID));
        Guid userId = dbUser != null ? dbUser.getId() : Guid.newGuid();
        dbUser = new DbUser(mapPrincipalRecordToDirectoryUser(authz, principal));
        dbUser.setId(userId);
        DbGroupDao dao = DbFacade.getInstance().getDbGroupDao();
        Set<Guid> groupIds = new HashSet<>();
        Set<String> groupsNames = new HashSet<>();
        for (ExtMap group : principal.<Collection<ExtMap>>get(PrincipalRecord.GROUPS, Collections.<ExtMap> emptyList())) {
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

    public static DirectoryUser mapPrincipalRecordToDirectoryUser(final String authzName, final ExtMap principalRecord) {
        DirectoryUser directoryUser = null;
        if (principalRecord != null) {
            directoryUser = new DirectoryUser(
                    authzName,
                    principalRecord.<String> get(Authz.PrincipalRecord.NAMESPACE),
                    principalRecord.<String> get(Authz.PrincipalRecord.ID),
                    principalRecord.<String> get(Authz.PrincipalRecord.NAME),
                    principalRecord.<String> get(Authz.PrincipalRecord.PRINCIPAL),
                    principalRecord.<String> get(Authz.PrincipalRecord.DISPLAY_NAME)
                    );
            directoryUser.setDepartment(principalRecord.<String> get(Authz.PrincipalRecord.DEPARTMENT));
            directoryUser.setFirstName(principalRecord.<String> get(Authz.PrincipalRecord.FIRST_NAME));
            directoryUser.setLastName(principalRecord.<String> get(Authz.PrincipalRecord.LAST_NAME));
            directoryUser.setEmail(principalRecord.<String> get(Authz.PrincipalRecord.EMAIL));
            directoryUser.setTitle(principalRecord.<String> get(Authz.PrincipalRecord.TITLE));
            directoryUser.setPrincipal(principalRecord.<String> get(Authz.PrincipalRecord.PRINCIPAL));
            List<DirectoryGroup> directoryGroups = new ArrayList<>();
            Collection<ExtMap> groups = principalRecord.<Collection<ExtMap>> get(Authz.PrincipalRecord.GROUPS);
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
        return mapGroupRecordToDirectoryGroup(authzName, group, new HashSet<>());
    }

    private static DirectoryGroup mapGroupRecordToDirectoryGroup(
            final String authzName,
            final ExtMap group,
            final Set<String> loopPrevention
    ) {
        DirectoryGroup directoryGroup = null;
        if (group != null) {
            directoryGroup = new DirectoryGroup(
                    authzName,
                    group.<String> get(Authz.GroupRecord.NAMESPACE),
                    group.<String> get(Authz.GroupRecord.ID),
                    group.<String> get(Authz.GroupRecord.NAME),
                    group.<String> get(Authz.GroupRecord.DISPLAY_NAME)
                    );
            loopPrevention.add(directoryGroup.getId());
            for (ExtMap memberOf : group.<Collection<ExtMap>> get(
                    Authz.GroupRecord.GROUPS,
                    Collections.<ExtMap>emptyList())) {
                if(!loopPrevention.contains(memberOf.<String>get(GroupRecord.ID))) {
                    directoryGroup.getGroups().add(mapGroupRecordToDirectoryGroup(authzName, memberOf, loopPrevention));
                }
            }
        }
        return directoryGroup;
    }

    public static DbGroup mapGroupRecordToDbGroup(String directory, ExtMap groupRecord) {
        return new DbGroup(mapGroupRecordToDirectoryGroup(directory, groupRecord));
    }

    public static void flatGroups(ExtMap principal) {
        Map<String, ExtMap> accumulator = new HashMap<>();
        flatGroups(principal, PrincipalRecord.GROUPS, accumulator);
        principal.put(PrincipalRecord.GROUPS, new ArrayList<>(accumulator.values()));
    }

    private static void flatGroups(ExtMap entity, ExtKey key, Map<String, ExtMap> accumulator) {
        for (ExtMap group : entity.<Collection<ExtMap>>get(key, Collections.<ExtMap> emptyList())) {
            if(!accumulator.containsKey(group.<String>get(GroupRecord.ID))) {
                accumulator.put(group.<String>get(GroupRecord.ID), group);
                flatGroups(group, GroupRecord.GROUPS, accumulator);
            }
        }
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

}

package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.ovirt.engine.api.extensions.ExtMap;
import org.ovirt.engine.core.aaa.AuthzUtils;
import org.ovirt.engine.core.aaa.DirectoryUtils;
import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.extensions.mgr.ExtensionProxy;
import org.ovirt.engine.core.utils.collections.MultiValueMapUtils;
import org.ovirt.engine.core.utils.extensionsmgr.EngineExtensionsManager;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

public class SyncUsers {

    private static final Log log = LogFactory.getLog(SyncUsers.class);

    public static DbUser sync(DbUser dbUser) {
        List<DbUser> synchedUsers = sync(Arrays.asList(dbUser));
        return synchedUsers.isEmpty() ? null : synchedUsers.get(0);
    }

    public static List<DbUser> sync(List<DbUser> dbUsers) {
        List<DbUser> usersToUpdate = new ArrayList<>();
        Map<String, Map<String, Set<String>>> authzToNamespaceToUserIds = new HashMap<>();
        Map<DirectoryEntryKey, DbUser> originalDbUsersMap = new HashMap<>();
        Map<String, List<DbUser>> dbUsersPerAuthz = new HashMap<>();

        //Initialize the entries based on authz in the map
        for (DbUser dbUser : dbUsers) {
            MultiValueMapUtils.addToMap(dbUser.getDomain(), dbUser, dbUsersPerAuthz);
            if (!authzToNamespaceToUserIds.containsKey(dbUser.getDomain())) {
                authzToNamespaceToUserIds.put(dbUser.getDomain(), new HashMap<String, Set<String>>());
            }
            MultiValueMapUtils.addToMapOfSets(dbUser.getNamespace(), dbUser.getExternalId(), authzToNamespaceToUserIds.get(dbUser.getDomain()));
            originalDbUsersMap.put(new DirectoryEntryKey(dbUser), dbUser);
        }

        for (Entry<String, Map<String, Set<String>>> entry : authzToNamespaceToUserIds.entrySet()) {
            Map<String, DbUser> activeUsers = new HashMap<>();
            String authz = entry.getKey();
            try {
                ExtensionProxy authzExtension = EngineExtensionsManager.getInstance().getExtensionByName(authz);
                for (Entry<String, Set<String>> userIdsPerNamespace : entry.getValue().entrySet()) {
                    for (
                        ExtMap principal :
                        AuthzUtils.fetchPrincipalsByIdsRecursively(
                                authzExtension, userIdsPerNamespace.getKey(),
                                userIdsPerNamespace.getValue())
                        ) {
                            DirectoryUtils.flatGroups(principal);
                            DbUser dbUser = DirectoryUtils.mapPrincipalRecordToDbUser(authz, principal);
                            dbUser.setGroupIds(DirectoryUtils.getGroupIdsFromPrincipal(authz, principal));
                            activeUsers.put(dbUser.getExternalId(), dbUser);
                    }
                }

                for (DbUser dbUser : dbUsersPerAuthz.get(authz)) {
                    DbUser activeUser = activeUsers.get(dbUser.getExternalId());
                    if (activeUser != null) {
                        if (!activeUser.equals(dbUser)) {
                            activeUser.setId(dbUser.getId());
                            log.infoFormat("Principal {0}::{1} synchronized",
                                    activeUser.getLoginName(),
                                    activeUser.getDomain());
                            usersToUpdate.add(activeUser);
                        }
                    } else {
                        log.infoFormat("Deactivating non existing principal {0}::{1}",
                                dbUser.getLoginName(),
                                dbUser.getDomain());
                        dbUser.setActive(false);
                        usersToUpdate.add(dbUser);
                    }
                }
            } catch (Exception ex) {
                log.errorFormat("Error during user synchronization of extension {0}. Exception message is {1}",
                        authz, ex.getMessage());
                log.debug("", ex);
            }
        }
        return usersToUpdate;
    }
}

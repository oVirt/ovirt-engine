package org.ovirt.engine.core.bll.aaa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.ovirt.engine.api.extensions.ExtMap;
import org.ovirt.engine.core.aaa.AuthzUtils;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.extensions.mgr.ExtensionProxy;
import org.ovirt.engine.core.utils.collections.MultiValueMapUtils;
import org.ovirt.engine.core.utils.extensionsmgr.EngineExtensionsManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SyncUsers {

    private static final Logger log = LoggerFactory.getLogger(SyncUsers.class);

    public static DbUser sync(DbUser dbUser) {
        List<DbUser> synchedUsers = sync(Arrays.asList(dbUser));
        return synchedUsers.isEmpty() ? null : synchedUsers.get(0);
    }

    public static List<DbUser> sync(List<DbUser> dbUsers) {
        List<DbUser> usersToUpdate = new ArrayList<>();
        Map<String, Map<String, Set<String>>> authzToNamespaceToUserIds = new HashMap<>();
        Map<String, List<DbUser>> dbUsersPerAuthz = new HashMap<>();

        //Initialize the entries based on authz in the map
        for (DbUser dbUser : dbUsers) {
            MultiValueMapUtils.addToMap(dbUser.getDomain(), dbUser, dbUsersPerAuthz);
            if (!authzToNamespaceToUserIds.containsKey(dbUser.getDomain())) {
                authzToNamespaceToUserIds.put(dbUser.getDomain(), new HashMap<String, Set<String>>());
            }
            MultiValueMapUtils.addToMapOfSets(dbUser.getNamespace(), dbUser.getExternalId(), authzToNamespaceToUserIds.get(dbUser.getDomain()));
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
                            DbUser dbUser = DirectoryUtils.mapPrincipalRecordToDbUser(authz, principal);
                            activeUsers.put(dbUser.getExternalId(), dbUser);
                    }
                }

                for (DbUser dbUser : dbUsersPerAuthz.get(authz)) {
                    DbUser activeUser = activeUsers.get(dbUser.getExternalId());
                    if (activeUser != null) {
                        if (!activeUser.equals(dbUser)) {
                            activeUser.setId(dbUser.getId());
                            activeUser.setAdmin(dbUser.isAdmin());
                            log.info("Principal {}::{} synchronized",
                                    activeUser.getLoginName(),
                                    activeUser.getDomain());
                            usersToUpdate.add(activeUser);
                        }
                    } else {
                        log.info("Deactivating non existing principal {}::{}",
                                dbUser.getLoginName(),
                                dbUser.getDomain());
                        dbUser.setActive(false);
                        usersToUpdate.add(dbUser);
                    }
                }
            } catch (Exception ex) {
                log.error("Error during user synchronization of extension '{}': {}",
                        authz, ex.getMessage());
                log.debug("Exception", ex);
            }
        }
        return usersToUpdate;
    }
}

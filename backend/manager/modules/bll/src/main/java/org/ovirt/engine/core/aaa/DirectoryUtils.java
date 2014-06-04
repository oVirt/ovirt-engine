package org.ovirt.engine.core.aaa;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.DbGroup;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.DbGroupDAO;

public class DirectoryUtils {

    public static HashSet<Guid> getGroupIdsFromUser(DirectoryUser directoryUser) {
        HashSet<Guid> results = new HashSet<Guid>();
        Set<DirectoryGroup> groupsSet = new HashSet<DirectoryGroup>();
        flatGroups(groupsSet, directoryUser.getGroups());
        DbGroupDAO dao = DbFacade.getInstance().getDbGroupDao();
        if (groupsSet != null) {
            for (DirectoryGroup group : groupsSet) {
                DbGroup dbGroup = dao.getByExternalId(group.getDirectoryName(), group.getId());
                if (dbGroup != null) {
                    results.add(dbGroup.getId());
                }
            }
        }
        return results;
    }

    private static void flatGroups(Set<DirectoryGroup> accumulator, List<DirectoryGroup> groupsFrom) {
        for (DirectoryGroup group : groupsFrom) {
            flatGroups(accumulator, group.getGroups());
            accumulator.add(group);
        }

    }
}

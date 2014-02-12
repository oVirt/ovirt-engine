package org.ovirt.engine.core.aaa;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.DbGroup;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.DbGroupDAO;

public class DirectoryUtils {

    public static String getGroupIdsFromUser(DirectoryUser directoryUser) {

        StringBuilder sb = new StringBuilder();
        List<DirectoryGroup> groups = directoryUser.getGroups();
        DbGroupDAO dao = DbFacade.getInstance().getDbGroupDao();
        if (groups != null) {
            boolean first = true;
            for (DirectoryGroup group : groups) {
                DbGroup dbGroup = dao.getByExternalId(group.getDirectory().getName(), group.getId());
                if (!first) {
                    sb.append(",");
                } else {
                    first = false;
                }
                sb.append(dbGroup != null ? dbGroup.getId() : Guid.Empty);
            }

        }
        return sb.toString();
    }

}

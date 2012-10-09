package org.ovirt.engine.core.bll.utils;

import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.gluster.GlusterBrickDao;

public class GlusterUtils {
    private static GlusterUtils instance = new GlusterUtils();

    public static GlusterUtils getInstance() {
        return instance;
    }

    public GlusterBrickDao getGlusterBrickDao() {
        return DbFacade.getInstance().getGlusterBrickDao();
    }

    public boolean hasBricks(Guid serverId) {
        return (getGlusterBrickDao().getGlusterVolumeBricksByServerId(serverId).size() > 0);
    }
}

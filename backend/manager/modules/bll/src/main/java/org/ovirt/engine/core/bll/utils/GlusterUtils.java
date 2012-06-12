package org.ovirt.engine.core.bll.utils;

import javax.ejb.Singleton;

import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.gluster.GlusterBrickDao;

@Singleton
public class GlusterUtils {
    private static GlusterUtils instance = null;

    public static GlusterUtils getInstance() {
        if (instance == null) {
            instance = new GlusterUtils();
        }
        return instance;
    }

    public GlusterBrickDao getGlusterBrickDao() {
        return DbFacade.getInstance().getGlusterBrickDao();
    }

    public boolean hasBricks(Guid serverId) {
        return (getGlusterBrickDao().getGlusterVolumeBricksByServerId(serverId).size() > 0);
    }
}

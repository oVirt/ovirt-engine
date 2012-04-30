package org.ovirt.engine.core.bll.utils;

import java.util.List;

import javax.ejb.Singleton;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.VdsDAO;
import org.ovirt.engine.core.utils.RandomUtils;

@Singleton
public class ClusterUtils {

    private static ClusterUtils instance = null;

    public static ClusterUtils getInstance() {
        if(instance == null) {
            instance = new ClusterUtils();
        }
        return instance;
    }

    /**
     * Returns a server that is in {@link VDSStatus#Up} status.<br>
     * This server is chosen randomly from all the Up servers.
     *
     * @param clusterId
     * @return One of the servers in up status
     */
    public VDS getUpServer(Guid clusterId) {
        List<VDS> servers = getVdsDao()
                .getAllForVdsGroupWithStatus(clusterId, VDSStatus.Up);

        if (servers == null || servers.isEmpty()) {
            throw new VdcBLLException(VdcBllErrors.NO_UP_SERVER_FOUND);
        }
        return RandomUtils.instance().pickRandom(servers);
    }

    public VdsDAO getVdsDao() {
        return DbFacade.getInstance()
                .getVdsDAO();
    }
}

package org.ovirt.engine.core.bll.utils;

import java.util.List;
import java.util.Random;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.VdsDAO;
import org.ovirt.engine.core.dao.VdsGroupDAO;

public class ClusterUtils {

    private static ClusterUtils instance = new ClusterUtils();

    public static ClusterUtils getInstance() {
        return instance;
    }

    /**
     * Returns a server that is in {@link VDSStatus#Up} status.<br>
     * This server is chosen randomly from all the Up servers.
     *
     * @param clusterId
     * @return One of the servers in up status
     */
    public VDS getRandomUpServer(Guid clusterId) {
        List<VDS> servers = getAllUpServers(clusterId);
        if (servers == null || servers.isEmpty()) {
            return null;
        }
        return servers.get(new Random().nextInt(servers.size()));
    }

    /**
     * Returns a server that is in {@link VDSStatus#Up} status.<br>
     * This server is returned as first from list of the Up servers.
     *
     * @param clusterId
     * @return One of the servers in up status
     */
    public VDS getUpServer(Guid clusterId) {
        List<VDS> servers = getAllUpServers(clusterId);
        if (servers == null || servers.isEmpty()) {
            return null;
        }
        return servers.get(0);
    }

    public List<VDS> getAllUpServers(Guid clusterId) {
        return getVdsDao().getAllForVdsGroupWithStatus(clusterId, VDSStatus.Up);
    }

    public List<VDS> getAllServers(Guid clusterId) {
        return getVdsDao().getAllForVdsGroup(clusterId);
    }

    public boolean hasMultipleServers(Guid clusterId) {
        return getServerCount(clusterId) > 1;
    }

    public boolean hasServers(Guid clusterId) {
        return getServerCount(clusterId) > 0;
    }

    public int getServerCount(Guid clusterId) {
        return getVdsDao().getAllForVdsGroup(clusterId).size();
    }

    public VdsDAO getVdsDao() {
        return DbFacade.getInstance()
                .getVdsDao();
    }

    public static Version getCompatibilityVersion(VM vm) {
        return getCompatibilityVersion(vm.getStaticData());
    }

    public static Version getCompatibilityVersion(VmBase vmBase) {
        return vmBase.getVdsGroupId() != null ?
                getInstance().getVdsGroupDao().get(vmBase.getVdsGroupId()).getcompatibility_version()
                : Version.v3_0;
    }

    public VdsGroupDAO getVdsGroupDao() {
        return DbFacade.getInstance().getVdsGroupDao();
    }
}

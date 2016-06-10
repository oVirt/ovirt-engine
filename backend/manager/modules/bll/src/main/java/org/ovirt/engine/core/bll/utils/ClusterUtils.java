package org.ovirt.engine.core.bll.utils;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.VdsDao;

public class ClusterUtils {

    private static ClusterUtils instance = new ClusterUtils();

    public static ClusterUtils getInstance() {
        return instance;
    }

     public List<VDS> getAllServers(Guid clusterId) {
        return getVdsDao().getAllForCluster(clusterId);
    }

    public boolean hasMultipleServers(Guid clusterId) {
        return getServerCount(clusterId) > 1;
    }

    public boolean hasServers(Guid clusterId) {
        return getServerCount(clusterId) > 0;
    }

    public int getServerCount(Guid clusterId) {
        return getVdsDao().getAllForCluster(clusterId).size();
    }

    public VdsDao getVdsDao() {
        return DbFacade.getInstance()
                .getVdsDao();
    }

    public static Version getCompatibilityVersion(VM vm) {
        return getCompatibilityVersion(vm.getStaticData());
    }

    public static Version getCompatibilityVersion(VmBase vmBase) {
        return vmBase.getClusterId() != null ?
                getInstance().getClusterDao().get(vmBase.getClusterId()).getCompatibilityVersion()
                : Version.ALL.get(0);
    }

    public ClusterDao getClusterDao() {
        return DbFacade.getInstance().getClusterDao();
    }
}

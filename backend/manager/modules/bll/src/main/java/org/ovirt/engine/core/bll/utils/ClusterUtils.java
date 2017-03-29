package org.ovirt.engine.core.bll.utils;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.VdsDao;

@Singleton
public class ClusterUtils {

    @Inject
    private VdsDao vdsDao;

    @Inject
    private ClusterDao clusterDao;

    public boolean hasMultipleServers(Guid clusterId) {
        return getServerCount(clusterId) > 1;
    }

    public boolean hasServers(Guid clusterId) {
        return getServerCount(clusterId) > 0;
    }

    public int getServerCount(Guid clusterId) {
        return vdsDao.getAllForCluster(clusterId).size();
    }

    public Version getCompatibilityVersion(VM vm) {
        return getCompatibilityVersion(vm.getStaticData());
    }

    public Version getCompatibilityVersion(VmBase vmBase) {
        return vmBase.getClusterId() != null
                ? clusterDao.get(vmBase.getClusterId()).getCompatibilityVersion()
                : Version.ALL.get(0);
    }
}

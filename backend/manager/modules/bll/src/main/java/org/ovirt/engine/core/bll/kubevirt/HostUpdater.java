package org.ovirt.engine.core.bll.kubevirt;

import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.RemoveVdsParameters;
import org.ovirt.engine.core.common.action.hostdeploy.AddVdsActionParameters;
import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSType;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsDao;

import io.kubernetes.client.models.V1Node;

@ApplicationScoped
public class HostUpdater {

    @Inject
    private Instance<BackendInternal> backend;

    @Inject
    private VdsDao vdsDao;

    public boolean removeHost(Guid hostId) {
        ActionReturnValue returnValue =
                backend.get().runInternalAction(ActionType.RemoveVds, new RemoveVdsParameters(hostId));
        return returnValue.getSucceeded();
    }

    public boolean removeHost(V1Node node, Guid clusterId) {
        VDS host = vdsDao.getByName(node.getMetadata().getName(), clusterId);
        if (host == null) {
            return false;
        }

        return removeHost(host.getId());
    }

    public boolean addHost(V1Node node, Guid clusterId) {
        String vdsName = node.getMetadata().getName();
        if (vdsDao.getByName(vdsName, clusterId) != null) {
            return false;
        }

        VdsStatic vdsStatic = createVdsStatic(node, clusterId);
        AddVdsActionParameters parameters = new AddVdsActionParameters(vdsStatic, null);
        ActionReturnValue returnValue = backend.get().runInternalAction(ActionType.AddVds, parameters);
        return returnValue.getSucceeded();
    }

    private VdsStatic createVdsStatic(V1Node node, Guid clusterId) {
        VdsStatic vds = new VdsStatic();
        vds.setId(Guid.newGuid());
        vds.setUniqueID(node.getMetadata().getUid());
        vds.setClusterId(clusterId);
        vds.setName(node.getMetadata().getName());
        vds.setVdsType(VDSType.KubevirtNode);
        vds.setPort(88);
        vds.setHostName(getHostName(node));
        vds.setVdsSpmPriority(BusinessEntitiesDefinitions.HOST_MIN_SPM_PRIORITY);
        return vds;
    }

    public String getHostName(V1Node node) {
        Map<String, String> labels = node.getMetadata().getLabels();
        return labels.get("kubernetes.io/hostname");
    }
}

package org.ovirt.engine.core.vdsbroker.gluster;

import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterGlobalVolumeOptionEntity;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeOptionVDSParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.gluster.GlusterOptionDao;

/**
 * VDS command to set a gluster volume option
 */
public class SetGlusterVolumeOptionVDSCommand<P extends GlusterVolumeOptionVDSParameters> extends AbstractGlusterBrokerCommand<P> {
    public SetGlusterVolumeOptionVDSCommand(P parameters) {
        super(parameters);
    }

    @Inject
    private VdsDao vdsDao;
    @Inject
    private GlusterOptionDao glusterOptionDao;
    @Override
    protected void executeVdsBrokerCommand() {
        String volumeName = getParameters().getVolumeName();
        VDS vds = vdsDao.get(getParameters().getVdsId());
        List<GlusterGlobalVolumeOptionEntity> globalVolumeOptionsList =
                glusterOptionDao.getGlobalVolumeOptions(vds.getClusterId());
        if (!globalVolumeOptionsList.isEmpty()) {
            for (GlusterGlobalVolumeOptionEntity option : globalVolumeOptionsList) {
                if (option.getKey().equalsIgnoreCase(getParameters().getVolumeOption().getKey())) {
                    volumeName = "all";
                    break;
                }
            }
        } else {
            GlusterVolumeGlobalOptionsInfoReturn entity =
                    getBroker().glusterVolumeGlobalOptionsGet();
            List<GlusterGlobalVolumeOptionEntity> globalVolumeOptionsListFromVds = entity.globalOptionList();
            if (!globalVolumeOptionsListFromVds.isEmpty()) {
                for (GlusterGlobalVolumeOptionEntity option : globalVolumeOptionsListFromVds) {
                    option.setClusterId(vds.getClusterId());
                    option.setId(Guid.newGuid());
                    glusterOptionDao.saveGlobalVolumeOption(option);
                    if (option.getKey().equalsIgnoreCase(getParameters().getVolumeOption().getKey())) {
                        volumeName = "all";
                    }
                }

            }
        }
        status =
                getBroker().glusterVolumeSet(volumeName,
                        getParameters().getVolumeOption().getKey(),
                        getParameters().getVolumeOption().getValue());
        proceedProxyReturnValue();
    }
}

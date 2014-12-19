package org.ovirt.engine.core.vdsbroker.gluster;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSnapshotConfig;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeSnapshotSetConfigVDSParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.vdsbroker.vdsbroker.StatusForXmlRpc;

public class SetGlusterVolumeSnapshotConfigVDSCommand<P extends GlusterVolumeSnapshotSetConfigVDSParameters>
        extends AbstractGlusterBrokerCommand<P> {
    public SetGlusterVolumeSnapshotConfigVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected StatusForXmlRpc getReturnStatus() {
        return status.mStatus;
    }

    @Override
    protected void executeVdsBrokerCommand() {
        GlusterVolumeSnapshotConfig cfgParam = getParameters()
                .getConfgParam();

        if (cfgParam.getVolumeId() != null) {
            GlusterVolumeEntity volume = DbFacade.getInstance().getGlusterVolumeDao().getById(cfgParam.getVolumeId());
            status =
                    getBroker().glusterVolumeSnapshotConfigSet(volume.getName(),
                            cfgParam.getParamName(),
                            cfgParam.getParamValue());
        } else {
            status = getBroker().glusterSnapshotConfigSet(cfgParam.getParamName(), cfgParam.getParamValue());
        }

        proceedProxyReturnValue();
    }
}

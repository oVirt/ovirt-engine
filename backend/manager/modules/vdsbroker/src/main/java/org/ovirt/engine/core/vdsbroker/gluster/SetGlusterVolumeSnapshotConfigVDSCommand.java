package org.ovirt.engine.core.vdsbroker.gluster;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSnapshotConfig;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeSnapshotSetConfigVDSParameters;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeDao;
import org.ovirt.engine.core.vdsbroker.vdsbroker.Status;

public class SetGlusterVolumeSnapshotConfigVDSCommand<P extends GlusterVolumeSnapshotSetConfigVDSParameters>
        extends AbstractGlusterBrokerCommand<P> {

    @Inject
    private GlusterVolumeDao glusterVolumeDao;

    public SetGlusterVolumeSnapshotConfigVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected Status getReturnStatus() {
        return status.status;
    }

    @Override
    protected void executeVdsBrokerCommand() {
        GlusterVolumeSnapshotConfig cfgParam = getParameters()
                .getConfgParam();

        String paramValue = StringUtils.removeEnd(cfgParam.getParamValue(), "%");
        if (cfgParam.getVolumeId() != null) {
            GlusterVolumeEntity volume = glusterVolumeDao.getById(cfgParam.getVolumeId());
            status =
                    getBroker().glusterVolumeSnapshotConfigSet(volume.getName(),
                            cfgParam.getParamName(),
                            paramValue);
        } else {
            status = getBroker().glusterSnapshotConfigSet(cfgParam.getParamName(), paramValue);
        }

        proceedProxyReturnValue();
    }
}

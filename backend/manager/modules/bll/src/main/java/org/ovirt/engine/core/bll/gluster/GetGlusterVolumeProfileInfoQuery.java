package org.ovirt.engine.core.bll.gluster;

import java.util.List;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.gluster.BrickProfileDetails;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeProfileInfo;
import org.ovirt.engine.core.common.queries.gluster.GlusterVolumeProfileParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeProfileInfoVDSParameters;


/**
 * Query to fetch gluster volume profile info for the given the volume
 */
public class GetGlusterVolumeProfileInfoQuery<P extends GlusterVolumeProfileParameters> extends GlusterQueriesCommandBase<P> {

    public GetGlusterVolumeProfileInfoQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        GlusterVolumeEntity volumeCheck = glusterVolumeDao.getById(getParameters().getVolumeId());
        boolean nfs = volumeCheck.isNfsEnabled() && getParameters().isNfs();
        VDSReturnValue returnValue = runVdsCommand(VDSCommandType.GetGlusterVolumeProfileInfo,
                new GlusterVolumeProfileInfoVDSParameters(getParameters().getClusterId(),
                        getUpServerId(getParameters().getClusterId()),
                        getGlusterVolumeName(getParameters().getVolumeId()),
                        nfs));

        GlusterVolumeProfileInfo profileInfo = (GlusterVolumeProfileInfo) returnValue.getReturnValue();
        if (!nfs) {
           populateBrickNames(profileInfo);
        }
        getQueryReturnValue().setReturnValue(profileInfo);
    }

    protected GlusterVolumeProfileInfo populateBrickNames(GlusterVolumeProfileInfo profileInfo) {
        List<BrickProfileDetails> brickProfiles= profileInfo.getBrickProfileDetails();
        for(BrickProfileDetails brickProfileDetails : brickProfiles) {
            brickProfileDetails.setIdentity(glusterBrickDao.getById(brickProfileDetails.getBrickId()).getQualifiedName());
        }
        return profileInfo;
    }
}

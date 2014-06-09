package org.ovirt.engine.core.bll.gluster;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.gluster.BrickProfileDetails;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeProfileInfo;
import org.ovirt.engine.core.common.queries.gluster.GlusterVolumeProfileParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeProfileInfoVDSParameters;


/**
 * Query to fetch gluster volume profile info for the given the volume
 */
public class GetGlusterVolumeProfileInfoQuery<P extends GlusterVolumeProfileParameters> extends GlusterQueriesCommandBase<P> {

    public GetGlusterVolumeProfileInfoQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        VDSReturnValue returnValue = runVdsCommand(VDSCommandType.GetGlusterVolumeProfileInfo,
                new GlusterVolumeProfileInfoVDSParameters(getParameters().getClusterId(),
                        getUpServerId(getParameters().getClusterId()),
                        getGlusterVolumeName(getParameters().getVolumeId()),
                        getParameters().isNfs()));

        GlusterVolumeProfileInfo profileInfo = (GlusterVolumeProfileInfo) returnValue.getReturnValue();
        if (!getParameters().isNfs()) {
           populateBrickNames(profileInfo);
        }
        getQueryReturnValue().setReturnValue(profileInfo);
    }

    protected GlusterVolumeProfileInfo populateBrickNames(GlusterVolumeProfileInfo profileInfo) {
        List<BrickProfileDetails> brickProfiles= profileInfo.getBrickProfileDetails();
        for(BrickProfileDetails brickProfileDetails : brickProfiles) {
            brickProfileDetails.setIdentity(getGlusterBrickDao().getById(brickProfileDetails.getBrickId()).getQualifiedName());
        }
        return profileInfo;
    }
}

package org.ovirt.engine.core.bll.gluster;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeParameters;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.constants.gluster.GlusterConstants;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.dao.gluster.GlusterDBUtils;
import org.ovirt.engine.core.dao.gluster.GlusterGeoRepDao;
import org.ovirt.engine.core.dao.gluster.GlusterOptionDao;

/**
 * Base class for all Gluster Volume related commands
 */
public abstract class GlusterVolumeCommandBase<T extends GlusterVolumeParameters> extends GlusterCommandBase<T> {

    @Inject
    protected GlusterGeoRepDao glusterGeoRepDao;

    @Inject
    protected GlusterOptionDao glusterOptionDao;

    @Inject
    protected GlusterSyncJob glusterSyncJob;

    @Inject
    protected GlusterDBUtils glusterDBUtils;

    public GlusterVolumeCommandBase(T params, CommandContext commandContext) {
        super(params, commandContext);
        setGlusterVolumeId(getParameters().getVolumeId());

    }

    @Override
    public Cluster getCluster() {
        if (getGlusterVolume() != null) {
            setClusterId(getGlusterVolume().getClusterId());
        }
        return super.getCluster();
    }

    @Override
    protected boolean validate() {
        if (getGlusterVolume() == null) {
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_GLUSTER_VOLUME_INVALID);
            return false;
        }
        // super class validate expects cluster id (ClusterId).
        if (!super.validate()) {
            return false;
        }

        return true;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        // Any command that inherits from this class will check
        // permissions at volume level by default
        return Collections.singletonList(
                new PermissionSubject(
                        getParameters().getVolumeId(),
                        VdcObjectType.GlusterVolume,
                        getActionType().getActionGroup()));
    }

    @Override
    public Map<String, String> getJobMessageProperties() {
        if (jobProperties == null) {
            jobProperties = super.getJobMessageProperties();
            jobProperties.put(GlusterConstants.VOLUME, getGlusterVolumeName());
        }

        return jobProperties;
    }
}

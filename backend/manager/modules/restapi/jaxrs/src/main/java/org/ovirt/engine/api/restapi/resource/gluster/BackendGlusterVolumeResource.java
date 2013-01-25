package org.ovirt.engine.api.restapi.resource.gluster;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.GlusterVolume;
import org.ovirt.engine.api.model.Option;
import org.ovirt.engine.api.resource.gluster.GlusterBricksResource;
import org.ovirt.engine.api.resource.gluster.GlusterVolumeResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendActionableResource;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeActionParameters;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeOptionParameters;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeRebalanceParameters;
import org.ovirt.engine.core.common.action.gluster.ResetGlusterVolumeOptionsParameters;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeOptionEntity;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

/**
 * Implementation of the "glustervolumes/{id}" resource
 */
public class BackendGlusterVolumeResource
        extends AbstractBackendActionableResource<GlusterVolume, GlusterVolumeEntity>
        implements GlusterVolumeResource {
    private BackendGlusterVolumesResource parent;

    public BackendGlusterVolumeResource(String volumeId, BackendGlusterVolumesResource parent) {
        this(volumeId);
        setParent(parent);
    }

    public BackendGlusterVolumeResource(String volumeId) {
        super(volumeId, GlusterVolume.class, GlusterVolumeEntity.class, BackendGlusterVolumesResource.SUB_COLLECTIONS);
    }

    @Override
    protected GlusterVolume addParents(GlusterVolume model) {
        model.setId(id);
        parent.addParents(model);
        return model;
    }

    @Override
    public GlusterVolume get() {
        return performGet(VdcQueryType.GetGlusterVolumeById, new IdQueryParameters(guid));
    }

    @Override
    public Response start(Action action) {
        return doAction(VdcActionType.StartGlusterVolume, new GlusterVolumeActionParameters(guid,
                action.isSetForce() ? action.isForce() : false), action);
    }

    @Override
    public Response stop(Action action) {
        return doAction(VdcActionType.StopGlusterVolume, new GlusterVolumeActionParameters(guid,
                action.isSetForce() ? action.isForce() : false), action);
    }

    @Override
    public Response rebalance(Action action) {
        boolean fixLayoutOnly = (action.isSetFixLayout() ? action.isFixLayout() : false);
        boolean force = (action.isSetForce() ? action.isForce() : false);
        return doAction(VdcActionType.StartRebalanceGlusterVolume,
                new GlusterVolumeRebalanceParameters(guid,
                        fixLayoutOnly,
                        force), action);
    }

    @Override
    public Response setOption(Action action) {
        Option option = action.getOption();
        validateParameters(option, "name", "value");

        return doAction(VdcActionType.SetGlusterVolumeOption,
                new GlusterVolumeOptionParameters(new GlusterVolumeOptionEntity(guid,
                        option.getName(),
                        option.getValue())),
                action);
    }

    @Override
    public Response resetOption(Action action) {
        Option option = action.getOption();
        validateParameters(option, "name");
        return resetOption(action, option.getName(), action.isSetForce() ? action.isForce() : false);
    }

    @Override
    public Response resetAllOptions(Action action) {
        return resetOption(action, null, action.isSetForce() ? action.isForce() : false);
    }

    private Response resetOption(Action action, String optionName, boolean force) {
        return doAction(VdcActionType.ResetGlusterVolumeOptions,
                new ResetGlusterVolumeOptionsParameters(guid,
                        optionName,
                        force), action);
    }

    @Override
    public GlusterBricksResource getGlusterBrickSubResource() {
        return inject(new BackendGlusterBricksResource(this));
    }

    public BackendGlusterVolumesResource getParent() {
        return parent;
    }

    public void setParent(BackendGlusterVolumesResource parent) {
        this.parent = parent;
    }

    @Override
    protected GlusterVolume doPopulate(GlusterVolume model, GlusterVolumeEntity entity) {
        return model;
    }

    public String getId() {
       return this.id;
    }
}

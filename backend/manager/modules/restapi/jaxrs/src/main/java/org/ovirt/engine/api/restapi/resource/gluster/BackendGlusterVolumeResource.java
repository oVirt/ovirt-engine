package org.ovirt.engine.api.restapi.resource.gluster;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.GlusterVolume;
import org.ovirt.engine.api.model.GlusterVolumeProfileDetails;
import org.ovirt.engine.api.model.Option;
import org.ovirt.engine.api.resource.StatisticsResource;
import org.ovirt.engine.api.resource.gluster.GlusterBricksResource;
import org.ovirt.engine.api.resource.gluster.GlusterVolumeResource;
import org.ovirt.engine.api.restapi.logging.Messages;
import org.ovirt.engine.api.restapi.resource.AbstractBackendActionableResource;
import org.ovirt.engine.api.restapi.resource.BackendStatisticsResource;
import org.ovirt.engine.api.restapi.resource.VolumeStatisticalQuery;
import org.ovirt.engine.api.restapi.types.GlusterVolumeProfileInfoMapper;
import org.ovirt.engine.api.restapi.util.LinkHelper;
import org.ovirt.engine.api.restapi.util.ParametersHelper;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeActionParameters;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeOptionParameters;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeParameters;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeRebalanceParameters;
import org.ovirt.engine.core.common.action.gluster.ResetGlusterVolumeOptionsParameters;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeOptionEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeProfileInfo;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.queries.gluster.GlusterVolumeProfileParameters;
import org.ovirt.engine.core.compat.Guid;

/**
 * Implementation of the "glustervolumes/{id}" resource
 */
public class BackendGlusterVolumeResource
        extends AbstractBackendActionableResource<GlusterVolume, GlusterVolumeEntity>
        implements GlusterVolumeResource {
    private static final String NFS_CONSTRAINT_PARAMETER = "nfsStatistics";
    private BackendGlusterVolumesResource parent;

    public BackendGlusterVolumeResource(String volumeId, BackendGlusterVolumesResource parent) {
        this(volumeId);
        setParent(parent);
    }

    public BackendGlusterVolumeResource(String volumeId) {
        super(volumeId, GlusterVolume.class, GlusterVolumeEntity.class);
    }

    @Override
    protected GlusterVolume addParents(GlusterVolume model) {
        model.setId(id);
        parent.addParents(model);
        return model;
    }

    @Override
    public GlusterVolume get() {
        return performGet(QueryType.GetGlusterVolumeById, new IdQueryParameters(guid));
    }

    @Override
    public Response start(Action action) {
        return doAction(ActionType.StartGlusterVolume, new GlusterVolumeActionParameters(guid,
                action.isSetForce() ? action.isForce() : false), action);
    }

    @Override
    public Response stop(Action action) {
        return doAction(ActionType.StopGlusterVolume, new GlusterVolumeActionParameters(guid,
                action.isSetForce() ? action.isForce() : false), action);
    }

    @Override
    public Response rebalance(Action action) {
        boolean fixLayoutOnly = action.isSetFixLayout() ? action.isFixLayout() : false;
        boolean force = action.isSetForce() ? action.isForce() : false;
        return doAction(ActionType.StartRebalanceGlusterVolume,
                new GlusterVolumeRebalanceParameters(guid,
                        fixLayoutOnly,
                        force), action);
    }

    @Override
    public Response stopRebalance(Action action) {
        return doAction(ActionType.StopRebalanceGlusterVolume,
                new GlusterVolumeRebalanceParameters(guid), action);
    }

    @Override
    public Response setOption(Action action) {
        Option option = action.getOption();
        validateParameters(option, "name", "value");

        return doAction(ActionType.SetGlusterVolumeOption,
                new GlusterVolumeOptionParameters(new GlusterVolumeOptionEntity(guid,
                        option.getName(),
                        option.getValue())),
                action);
    }

    @Override
    public Response resetOption(Action action) {
        Option option = action.getOption();
        validateParameters(option, "name");
        return resetOption(action, option.getName(), option.getValue(), action.isSetForce() ? action.isForce() : false);
    }

    @Override
    public Response resetAllOptions(Action action) {
        return resetOption(action, null, null, action.isSetForce() ? action.isForce() : false);
    }

    private Response resetOption(Action action, String optionName, String optionValue, boolean force) {
        return doAction(ActionType.ResetGlusterVolumeOptions,
                new ResetGlusterVolumeOptionsParameters(guid,
                        optionName == null ? null : new GlusterVolumeOptionEntity(guid, optionName, optionValue),
                        force), action);
    }

    @Override
    public Response startProfile(Action action) {
        return doAction(ActionType.StartGlusterVolumeProfile, new GlusterVolumeParameters(guid), action);
    }


    @Override
    public Response stopProfile(Action action) {
        return doAction(ActionType.StopGlusterVolumeProfile, new GlusterVolumeParameters(guid), action);
    }

    @Override
    public GlusterBricksResource getGlusterBricksResource() {
        return inject(new BackendGlusterBricksResource(this));
    }

    public BackendGlusterVolumesResource getParent() {
        return parent;
    }

    public void setParent(BackendGlusterVolumesResource parent) {
        this.parent = parent;
    }

    public String getId() {
        return this.id;
    }

    @Override
    public StatisticsResource getStatisticsResource() {

        EntityIdResolver<Guid> resolver =
                new QueryIdResolver<>(QueryType.GetGlusterVolumeById, IdQueryParameters.class);
        VolumeStatisticalQuery query = new VolumeStatisticalQuery(resolver, newModel(id));
        return inject(new BackendStatisticsResource<>(entityType,
                guid,
                query));
    }

    @Override
    public Response getProfileStatistics(Action action) {
        boolean nfsStats = isNfsStatistics();
        QueryReturnValue result = runQuery(QueryType.GetGlusterVolumeProfileInfo,
                new GlusterVolumeProfileParameters(Guid.createGuidFromString(parent.getParent().get().getId()), guid, nfsStats));
        if (result != null
                && result.getSucceeded()
                && result.getReturnValue() != null) {
            GlusterVolumeProfileInfo info = result.getReturnValue();
            GlusterVolumeProfileDetails statistics = GlusterVolumeProfileInfoMapper.map(info, null);
            statistics = LinkHelper.addLinks(statistics);
            return Response.ok(statistics).build();
        } else {
            //throw exception
            throw new WebFaultException(null, localize(Messages.BACKEND_FAILED), Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    private Boolean isNfsStatistics() {
        return ParametersHelper.getBooleanParameter(httpHeaders, uriInfo, NFS_CONSTRAINT_PARAMETER, true, false);
    }

    @Override
    public Response remove() {
        get();
        return performAction(ActionType.DeleteGlusterVolume, new GlusterVolumeParameters(guid));
    }
}

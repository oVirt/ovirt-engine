package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.common.util.StatusUtils;
import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.Cluster;
import org.ovirt.engine.api.model.Fault;
import org.ovirt.engine.api.model.FenceType;
import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.IscsiDetails;
import org.ovirt.engine.api.model.LogicalUnit;
import org.ovirt.engine.api.model.PowerManagement;
import org.ovirt.engine.api.model.PowerManagementStatus;
import org.ovirt.engine.api.model.CreationStatus;
import org.ovirt.engine.api.resource.ActionResource;
import org.ovirt.engine.api.resource.AssignedPermissionsResource;
import org.ovirt.engine.api.resource.AssignedTagsResource;
import org.ovirt.engine.api.resource.HostResource;
import org.ovirt.engine.api.resource.HostNicsResource;
import org.ovirt.engine.api.resource.HostStorageResource;
import org.ovirt.engine.api.resource.StatisticsResource;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ApproveVdsParameters;
import org.ovirt.engine.core.common.action.ChangeVDSClusterParameters;
import org.ovirt.engine.core.common.action.FenceVdsActionParameters;
import org.ovirt.engine.core.common.action.FenceVdsManualyParameters;
import org.ovirt.engine.core.common.action.MaintananceNumberOfVdssParameters;
import org.ovirt.engine.core.common.action.StorageServerConnectionParametersBase;
import org.ovirt.engine.core.common.action.UpdateVdsActionParameters;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.FenceActionType;
import org.ovirt.engine.core.common.businessentities.FenceStatusReturnValue;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VDSType;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.storage_server_connections;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.DiscoverSendTargetsQueryParameters;
import org.ovirt.engine.core.common.queries.GetPermissionsForObjectParameters;
import org.ovirt.engine.core.common.queries.GetVdsByVdsIdParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.queries.VdsIdParametersBase;

import static org.ovirt.engine.api.restapi.resource.BackendHostsResource.SUB_COLLECTIONS;


public class BackendHostResource extends AbstractBackendActionableResource<Host, VDS> implements
        HostResource {

    private static final String DEFAULT_ISCSI_PORT = "3260";
    private BackendHostsResource parent;

    public BackendHostResource(String id, BackendHostsResource parent) {
        super(id, Host.class, VDS.class, SUB_COLLECTIONS);
        this.parent = parent;
    }

    @Override
    public Host get() {
        // Filtered users are not allowed to view hosts
        if (isFiltered()) {
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }
        return performGet(VdcQueryType.GetVdsByVdsId, new GetVdsByVdsIdParameters(guid));
    }

    @Override
    public Host update(Host incoming) {
        QueryIdResolver hostResolver = new QueryIdResolver(VdcQueryType.GetVdsByVdsId, GetVdsByVdsIdParameters.class);
        VDS entity = getEntity(hostResolver, true);
        if (incoming.isSetCluster() && incoming.getCluster().isSetId() && !asGuid(incoming.getCluster().getId()).equals(entity.getvds_group_id())) {
            performAction(VdcActionType.ChangeVDSCluster,
                          new ChangeVDSClusterParameters(asGuid(incoming.getCluster().getId()), guid));
        }
        return performUpdate(incoming,
                             entity,
                             map(entity),
                             hostResolver,
                             VdcActionType.UpdateVds,
                             new UpdateParametersProvider());
    }

    @Override
    public Response install(Action action) {
        // REVISIT fencing options
        VDS vds = getEntity();
        UpdateVdsActionParameters params = new UpdateVdsActionParameters(vds.getStaticData(), action.getRootPassword(), true);
        if (vds.getvds_type()==VDSType.oVirtNode) {
            params.setIsReinstallOrUpgrade(true);
            if (action.isSetImage()) {
                params.setoVirtIsoFile(action.getImage());
            }
        } else {
            validateParameters(action, "rootPassword");
        }
        return doAction(VdcActionType.UpdateVds,
                        params,
                        action);
    }

    @Override
    public Response activate(Action action) {
        return doAction(VdcActionType.ActivateVds,
                        new VdsActionParameters(guid),
                        action);
    }

    @Override
    public Response approve(Action action) {

        if (action.isSetCluster() && (action.getCluster().isSetId() || action.getCluster().isSetName())) {
            update(setCluster(get(), action.getCluster()));
        }

        return doAction(VdcActionType.ApproveVds,
                        new ApproveVdsParameters(guid),
                        action);
    }

    private Host setCluster(Host host, Cluster cluster) {
        if (cluster.isSetId()) {
            host.setCluster(cluster);
        } else {
            host.setCluster(new Cluster());
            host.getCluster().setId(lookupClusterByName(cluster.getName()).getId().toString());
        }
        return host;
    }

    protected VDSGroup lookupClusterByName(String name) {
        return getEntity(VDSGroup.class, SearchType.Cluster, "Cluster: name=" + name);
    }

    @Override
    public Response deactivate(Action action) {
        return doAction(VdcActionType.MaintananceNumberOfVdss,
                        new MaintananceNumberOfVdssParameters(asList(guid), false),
                        action);
    }

    @Override
    public Response iscsiLogin(Action action) {
        validateParameters(action, "iscsi.address", "iscsi.target");
        storage_server_connections cnx = new storage_server_connections();
        IscsiDetails iscsiDetails = action.getIscsi();
        cnx.setconnection(iscsiDetails.getAddress());
        cnx.setiqn(iscsiDetails.getTarget());
        cnx.setstorage_type(org.ovirt.engine.core.common.businessentities.StorageType.ISCSI);
        if (iscsiDetails.isSetPort()) {
            cnx.setport(iscsiDetails.getPort().toString());
        } else {
            cnx.setport(DEFAULT_ISCSI_PORT);
        }
        if (iscsiDetails.isSetUsername()) {
            cnx.setuser_name(iscsiDetails.getUsername());
        }
        if (iscsiDetails.isSetPassword()) {
            cnx.setpassword(iscsiDetails.getPassword());
        }
        cnx.setportal("0");//TODO: when VSDM and Backend will support this, we will need to externalize this parameter to the user
        StorageServerConnectionParametersBase connectionParms = new StorageServerConnectionParametersBase(cnx, guid);
        return doAction(VdcActionType.ConnectStorageToVds, connectionParms, action);
    }

    public Response iscsiDiscover(Action action) {
        validateParameters(action, "iscsi.address");

        List<storage_server_connections> result = getBackendCollection(storage_server_connections.class,
                                                                       VdcQueryType.DiscoverSendTargets,
                                                                       createDiscoveryQueryParams(action));

        return actionSuccess(mapTargets(action, result));
    }

    private Action mapTargets(Action action, List<storage_server_connections> targets) {
        if (targets != null) {
            for (storage_server_connections cnx : targets) {
                action.getIscsiTargets().add(map(cnx).getTarget());
            }
        }
        return action;
    }

    protected LogicalUnit map(storage_server_connections cnx) {
        return getMapper(storage_server_connections.class, LogicalUnit.class).map(cnx, null);
    }

    private DiscoverSendTargetsQueryParameters createDiscoveryQueryParams(Action action) {
        storage_server_connections connectionDetails = new storage_server_connections();
        IscsiDetails iscsiDetails = action.getIscsi();
        connectionDetails.setconnection(iscsiDetails.getAddress());
        connectionDetails.setstorage_type(org.ovirt.engine.core.common.businessentities.StorageType.ISCSI);
        if (iscsiDetails.isSetPort()) {
            connectionDetails.setport(iscsiDetails.getPort().toString());
        } else {
            connectionDetails.setport(DEFAULT_ISCSI_PORT);
        }
        if (iscsiDetails.isSetUsername()) {
            connectionDetails.setuser_name(iscsiDetails.getUsername());
        }
        if (iscsiDetails.isSetPassword()) {
            connectionDetails.setpassword(iscsiDetails.getPassword());
        }
        return new DiscoverSendTargetsQueryParameters(guid, connectionDetails);
    }

    @Override
    public Response commitNetConfig(Action action) {
        return doAction(VdcActionType.CommitNetworkChanges,
                        new VdsActionParameters(guid),
                        action);
    }

    @Override
    public Response fence(Action action) {
        validateParameters(action, "fenceType");

        FenceType fenceType = validateEnum(FenceType.class, action.getFenceType().toUpperCase());

        switch (fenceType) {
        case MANUAL:
            return fenceManually(action);
        case RESTART:
            return fence(action, VdcActionType.RestartVds, FenceActionType.Restart);
        case START:
            return fence(action, VdcActionType.StartVds, FenceActionType.Start);
        case STOP:
            return fence(action, VdcActionType.StopVds, FenceActionType.Stop);
        case STATUS:
            return getFencingStatus(action);
        default:
            return null;
        }
    }

    private Response getFencingStatus(Action action) {
        FenceStatusReturnValue result = getEntity(FenceStatusReturnValue.class, VdcQueryType.GetVdsFenceStatus, new VdsIdParametersBase(guid), guid.toString());
        if (result.getIsSucceeded()) {
            PowerManagement pm = new PowerManagement();
            pm.setStatus(result.getStatus().toLowerCase().equals("on") ? StatusUtils.create(PowerManagementStatus.ON)
                    : result.getStatus().toLowerCase().equals("off") ? StatusUtils.create(PowerManagementStatus.OFF)
                            : result.getStatus().toLowerCase().equals("unknown") ? StatusUtils.create(PowerManagementStatus.UNKNOWN)
                                    : null);
            action.setPowerManagement(pm);
            return actionSuccess(action);
        } else {
            return handleFailure(action, result.getMessage());
        }
    }

    private Response handleFailure(Action action, String message) {
        action.setStatus(StatusUtils.create(CreationStatus.FAILED));
        action.setFault(new Fault());
        action.getFault().setReason(message);
        return Response.ok().entity(action).build();
    }

    private Response fence(Action action, VdcActionType vdcAction, FenceActionType fenceType) {
        return doAction(vdcAction, new FenceVdsActionParameters(guid, fenceType), action);
    }

    private Response fenceManually(Action action) {
        FenceVdsManualyParameters params = new FenceVdsManualyParameters(true);
        params.setVdsId(guid);
        params.setStoragePoolId(getEntity().getStoragePoolId());
        return doAction(VdcActionType.FenceVdsManualy, params, action);
    }

    @Override
    public HostNicsResource getHostNicsResource() {
        return inject(new BackendHostNicsResource(id));
    }

    @Override
    public HostStorageResource getHostStorageResource() {
        return inject(new BackendHostStorageResource(id));
    }

    @Override
    public AssignedTagsResource getTagsResource() {
        return inject(new BackendHostTagsResource(id));
    }

    @Override
    public AssignedPermissionsResource getPermissionsResource() {
        return inject(new BackendAssignedPermissionsResource(guid,
                                                             VdcQueryType.GetPermissionsForObject,
                                                             new GetPermissionsForObjectParameters(guid),
                                                             Host.class,
                                                             VdcObjectType.VDS));
    }

    @Override
    public StatisticsResource getStatisticsResource() {
        EntityIdResolver resolver = new QueryIdResolver(VdcQueryType.GetVdsByVdsId, GetVdsByVdsIdParameters.class);
        HostStatisticalQuery query = new HostStatisticalQuery(resolver, newModel(id));
        return inject(new BackendStatisticsResource<Host, VDS>(entityType, guid, query));
    }

    @Override
    public ActionResource getActionSubresource(String action, String ids) {
        return inject(new BackendActionResource(action, ids));
    }

    protected VDS getEntity() {
        return getEntity(VDS.class, VdcQueryType.GetVdsByVdsId, new GetVdsByVdsIdParameters(guid), id);
    }

    protected class UpdateParametersProvider implements ParametersProvider<Host, VDS> {
        @Override
        public VdcActionParametersBase getParameters(Host incoming, VDS entity) {
            VdsStatic updated = getMapper(modelType, VdsStatic.class).map(incoming,
                    entity.getStaticData());
            UpdateVdsActionParameters updateParams = new UpdateVdsActionParameters(updated, "", false);
            if (incoming.isSetOverrideIptables()) {
                updateParams.setOverrideFirewall(incoming.isOverrideIptables());
            }
            return updateParams;
        }
    }

    @Override
    protected Host populate(Host model, VDS entity) {
        Host host = parent.addStatistics(model, entity, uriInfo, httpHeaders);
        parent.addCertificateInfo(host);
        return host;
    }

    public BackendHostsResource getParent() {
        return this.parent;
    }
}

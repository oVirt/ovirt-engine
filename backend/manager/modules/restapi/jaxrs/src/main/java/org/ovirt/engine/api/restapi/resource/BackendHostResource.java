package org.ovirt.engine.api.restapi.resource;

import static org.ovirt.engine.api.restapi.resource.BackendHostsResource.SUB_COLLECTIONS;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.common.util.StatusUtils;
import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.Cluster;
import org.ovirt.engine.api.model.CreationStatus;
import org.ovirt.engine.api.model.Fault;
import org.ovirt.engine.api.model.FenceType;
import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.IscsiDetails;
import org.ovirt.engine.api.model.LogicalUnit;
import org.ovirt.engine.api.model.PowerManagement;
import org.ovirt.engine.api.model.PowerManagementStatus;
import org.ovirt.engine.api.model.StorageDomains;
import org.ovirt.engine.api.resource.ActionResource;
import org.ovirt.engine.api.resource.AssignedPermissionsResource;
import org.ovirt.engine.api.resource.AssignedTagsResource;
import org.ovirt.engine.api.resource.HostNicsResource;
import org.ovirt.engine.api.resource.HostNumaNodesResource;
import org.ovirt.engine.api.resource.HostResource;
import org.ovirt.engine.api.resource.HostStorageResource;
import org.ovirt.engine.api.resource.StatisticsResource;
import org.ovirt.engine.api.restapi.model.AuthenticationMethod;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ApproveVdsParameters;
import org.ovirt.engine.core.common.action.ChangeVDSClusterParameters;
import org.ovirt.engine.core.common.action.FenceVdsActionParameters;
import org.ovirt.engine.core.common.action.FenceVdsManualyParameters;
import org.ovirt.engine.core.common.action.ForceSelectSPMParameters;
import org.ovirt.engine.core.common.action.MaintenanceNumberOfVdssParameters;
import org.ovirt.engine.core.common.action.StorageServerConnectionParametersBase;
import org.ovirt.engine.core.common.action.UpdateVdsActionParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.action.VdsOperationActionParameters;
import org.ovirt.engine.core.common.businessentities.FenceActionType;
import org.ovirt.engine.core.common.businessentities.FenceStatusReturnValue;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VDSType;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.queries.DiscoverSendTargetsQueryParameters;
import org.ovirt.engine.core.common.queries.GetPermissionsForObjectParameters;
import org.ovirt.engine.core.common.queries.GetUnregisteredBlockStorageDomainsParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.NameQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.queries.VdsIdParametersBase;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;


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
        if (isForce()) {
            performAction(VdcActionType.RefreshHostCapabilities,
                    new VdsActionParameters(guid));
        }

        return performGet(VdcQueryType.GetVdsByVdsId, new IdQueryParameters(guid));
    }

    @Override
    public Host update(Host incoming) {
        validateEnums(Host.class, incoming);
        QueryIdResolver<Guid> hostResolver = new QueryIdResolver<Guid>(VdcQueryType.GetVdsByVdsId, IdQueryParameters.class);
        VDS entity = getEntity(hostResolver, true);
        if (incoming.isSetCluster() && (incoming.getCluster().isSetId() || incoming.getCluster().isSetName())) {
            Guid clusterId = lookupClusterId(incoming);
            if (!clusterId.equals(entity.getVdsGroupId())) {
                performAction(VdcActionType.ChangeVDSCluster,
                        new ChangeVDSClusterParameters(clusterId, guid));

                // After changing the cluster with the specialized command we need to reload the entity, so that it
                // contains the new cluster id. If we don't do this the next command will think that we are trying
                // to change the cluster, and it will explicitly refuse to perform the update.
                entity = getEntity(hostResolver, true);
            }
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
        validateEnums(Action.class, action);
        UpdateVdsActionParameters params = new UpdateVdsActionParameters(vds.getStaticData(), action.getRootPassword(), true);
        params = (UpdateVdsActionParameters) getMapper
                (Action.class, VdsOperationActionParameters.class).map(action, (VdsOperationActionParameters) params);
        if (vds.getVdsType()==VDSType.oVirtNode) {
            params.setIsReinstallOrUpgrade(true);
            if (action.isSetImage()) {
                params.setoVirtIsoFile(action.getImage());
            }
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
        validateEnums(Action.class, action);
        ApproveVdsParameters params = new ApproveVdsParameters(guid);
        params = (ApproveVdsParameters) getMapper
                (Action.class, VdsOperationActionParameters.class).map(action, (VdsOperationActionParameters) params);

        // Set pk authentication as default
        params.setAuthMethod(VdsOperationActionParameters.AuthenticationMethod.PublicKey);

        if (action.isSetRootPassword()) {
            params.setAuthMethod(VdsOperationActionParameters.AuthenticationMethod.Password);
            params.setRootPassword(action.getRootPassword());
        }
        else if (action.isSetSsh() && action.getSsh().isSetAuthenticationMethod()) {
            if (action.getSsh().getAuthenticationMethod().equals(
                    AuthenticationMethod.PASSWORD.value())) {
                params.setAuthMethod(VdsOperationActionParameters.AuthenticationMethod.Password);
                if (action.getSsh().isSetUser() && action.getSsh().getUser().isSetPassword()) {
                    params.setPassword(action.getSsh().getUser().getPassword());
                }
            }
        }

        return doAction(VdcActionType.ApproveVds,
                        params,
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

    protected Guid lookupClusterId(Host host) {
        return host.getCluster().isSetId() ? asGuid(host.getCluster().getId())
                                           :
                                           lookupClusterByName(host.getCluster().getName()).getId();
    }

    protected VDSGroup lookupClusterByName(String name) {
        return getEntity(VDSGroup.class,
                VdcQueryType.GetVdsGroupByName,
                new NameQueryParameters(name),
                "Cluster: name=" + name);
    }

    @Override
    public Response deactivate(Action action) {
        return doAction(VdcActionType.MaintenanceNumberOfVdss,
                        new MaintenanceNumberOfVdssParameters(asList(guid), false),
                        action);
    }

    @Override
    public Response forceSelectSPM(Action action) {
        return doAction(VdcActionType.ForceSelectSPM,
                new ForceSelectSPMParameters(guid), action);
    }

    @Override
    public Response iscsiLogin(Action action) {
        validateParameters(action, "iscsi.address", "iscsi.target");
        StorageServerConnections cnx = new StorageServerConnections();
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

        StorageServerConnectionParametersBase connectionParms = new StorageServerConnectionParametersBase(cnx, guid);
        return doAction(VdcActionType.ConnectStorageToVds, connectionParms, action);
    }

    @Override
    public Response unregisteredStorageDomainsDiscover(Action action) {
        StorageType storageType =
                ((action.getIscsi() != null) && (action.getIscsi().getAddress() != null)) ? StorageType.ISCSI
                        : StorageType.FCP;


        List<StorageServerConnections> storageServerConnections = new ArrayList<>();
        if (storageType == StorageType.ISCSI) {
            for (String iscsiTarget : action.getIscsiTargets()) {
                StorageServerConnections connectionDetails = getInitializedConnectionIscsiDetails(action);
                connectionDetails.setiqn(iscsiTarget);
                storageServerConnections.add(connectionDetails);
            }
        } else {
            // For FC we don't need to do anything.
        }
        GetUnregisteredBlockStorageDomainsParameters unregisteredBlockStorageDomainsParameters =
                new GetUnregisteredBlockStorageDomainsParameters(guid, storageType, storageServerConnections);
        try {
            Pair<List<StorageDomain>, List<StorageServerConnections>> pair =
                    getEntity(Pair.class,
                        VdcQueryType.GetUnregisteredBlockStorageDomains,
                        unregisteredBlockStorageDomainsParameters,
                        "GetUnregisteredBlockStorageDomains", true);

            List<StorageDomain> storageDomains = pair.getFirst();
            return actionSuccess(mapToStorageDomains(action, storageDomains));
        } catch (Exception e) {
            return handleError(e, false);
        }
    }

    @Override
    public Response iscsiDiscover(Action action) {
        validateParameters(action, "iscsi.address");

        List<StorageServerConnections> result = getBackendCollection(StorageServerConnections.class,
                                                                       VdcQueryType.DiscoverSendTargets,
                                                                       createDiscoveryQueryParams(action));

        return actionSuccess(mapTargets(action, result));
    }

    private Action mapTargets(Action action, List<StorageServerConnections> targets) {
        if (targets != null) {
            for (StorageServerConnections cnx : targets) {
                action.getIscsiTargets().add(map(cnx).getTarget());
            }
        }
        return action;
    }

    private Action mapToStorageDomains(Action action, List<StorageDomain> storageDomains) {
        if (storageDomains != null) {
            action.setStorageDomains(new StorageDomains());
            for (StorageDomain storageDomain : storageDomains) {
                action.getStorageDomains().getStorageDomains().add(map(storageDomain));
            }
        }
        return action;
    }

    protected LogicalUnit map(StorageServerConnections cnx) {
        return getMapper(StorageServerConnections.class, LogicalUnit.class).map(cnx, null);
    }

    protected org.ovirt.engine.api.model.StorageDomain map(StorageDomain storageDomain) {
        return getMapper(StorageDomain.class, org.ovirt.engine.api.model.StorageDomain.class).map(storageDomain, null);
    }

    private DiscoverSendTargetsQueryParameters createDiscoveryQueryParams(Action action) {
        StorageServerConnections connectionDetails = getInitializedConnectionIscsiDetails(action);
        return new DiscoverSendTargetsQueryParameters(guid, connectionDetails);
    }

    private StorageServerConnections getInitializedConnectionIscsiDetails(Action action) {
        StorageServerConnections connectionDetails = new StorageServerConnections();
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
        return connectionDetails;
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
    public HostNumaNodesResource getHostNumaNodesResource() {
        return inject(new BackendHostNumaNodesResource(id));
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
        EntityIdResolver<Guid> resolver = new QueryIdResolver<Guid>(VdcQueryType.GetVdsByVdsId, IdQueryParameters.class);
        HostStatisticalQuery query = new HostStatisticalQuery(resolver, newModel(id));
        return inject(new BackendStatisticsResource<Host, VDS>(entityType, guid, query));
    }

    @Override
    public ActionResource getActionSubresource(String action, String ids) {
        return inject(new BackendActionResource(action, ids));
    }

    @Override
    protected VDS getEntity() {
        return getEntity(VDS.class, VdcQueryType.GetVdsByVdsId, new IdQueryParameters(guid), id);
    }

    protected class UpdateParametersProvider implements ParametersProvider<Host, VDS> {
        @Override
        public VdcActionParametersBase getParameters(Host incoming, VDS entity) {
            VdsStatic updated = getMapper(modelType, VdsStatic.class).map(incoming,
                    entity.getStaticData());
            UpdateVdsActionParameters updateParams = new UpdateVdsActionParameters(updated, incoming.getRootPassword(), false);
            if (incoming.isSetOverrideIptables()) {
                updateParams.setOverrideFirewall(incoming.isOverrideIptables());
            }

            updateParams = (UpdateVdsActionParameters) getMapper
                    (Host.class, VdsOperationActionParameters.class).map(incoming, (VdsOperationActionParameters) updateParams);
            return updateParams;
        }
    }

    @Override
    protected Host doPopulate(Host model, VDS entity) {
        Host host = parent.addHostedEngineIfConfigured(model, entity);
        return host;
    }

    @Override
    protected Host deprecatedPopulate(Host model, VDS entity) {
        Host host = parent.addStatistics(model, entity, uriInfo, httpHeaders);
        parent.addCertificateInfo(host);
        return host;
    }

    public BackendHostsResource getParent() {
        return this.parent;
    }

    @Override
    @Path("hooks")
    public BackendHostHooksResource getHooksResource() {
        return inject(new BackendHostHooksResource(id));
    }
}

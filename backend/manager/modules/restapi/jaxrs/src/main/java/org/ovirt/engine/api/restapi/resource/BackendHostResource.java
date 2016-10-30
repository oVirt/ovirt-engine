package org.ovirt.engine.api.restapi.resource;

import static org.ovirt.engine.api.restapi.resource.BackendHostsResource.SUB_COLLECTIONS;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.common.util.QueryHelper;
import org.ovirt.engine.api.common.util.StatusUtils;
import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.Agent;
import org.ovirt.engine.api.model.Cluster;
import org.ovirt.engine.api.model.CreationStatus;
import org.ovirt.engine.api.model.Fault;
import org.ovirt.engine.api.model.FenceType;
import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.HostNIC;
import org.ovirt.engine.api.model.IscsiDetails;
import org.ovirt.engine.api.model.Label;
import org.ovirt.engine.api.model.LogicalUnit;
import org.ovirt.engine.api.model.PowerManagement;
import org.ovirt.engine.api.model.PowerManagementStatus;
import org.ovirt.engine.api.model.StorageDomains;
import org.ovirt.engine.api.resource.ActionResource;
import org.ovirt.engine.api.resource.AssignedPermissionsResource;
import org.ovirt.engine.api.resource.AssignedTagsResource;
import org.ovirt.engine.api.resource.FenceAgentsResource;
import org.ovirt.engine.api.resource.HostDevicesResource;
import org.ovirt.engine.api.resource.HostNicsResource;
import org.ovirt.engine.api.resource.HostNumaNodesResource;
import org.ovirt.engine.api.resource.HostResource;
import org.ovirt.engine.api.resource.HostStorageResource;
import org.ovirt.engine.api.resource.NetworkAttachmentsResource;
import org.ovirt.engine.api.resource.StatisticsResource;
import org.ovirt.engine.api.resource.StorageServerConnectionExtensionsResource;
import org.ovirt.engine.api.resource.UnmanagedNetworksResource;
import org.ovirt.engine.api.resource.externalhostproviders.KatelloErrataResource;
import org.ovirt.engine.api.restapi.model.AuthenticationMethod;
import org.ovirt.engine.api.restapi.resource.externalhostproviders.BackendHostKatelloErrataResource;
import org.ovirt.engine.api.restapi.types.Mapper;
import org.ovirt.engine.api.utils.LinkHelper;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ChangeVDSClusterParameters;
import org.ovirt.engine.core.common.action.FenceVdsActionParameters;
import org.ovirt.engine.core.common.action.FenceVdsManualyParameters;
import org.ovirt.engine.core.common.action.ForceSelectSPMParameters;
import org.ovirt.engine.core.common.action.HostSetupNetworksParameters;
import org.ovirt.engine.core.common.action.MaintenanceNumberOfVdssParameters;
import org.ovirt.engine.core.common.action.RemoveVdsParameters;
import org.ovirt.engine.core.common.action.StorageServerConnectionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.action.VdsOperationActionParameters;
import org.ovirt.engine.core.common.action.hostdeploy.ApproveVdsParameters;
import org.ovirt.engine.core.common.action.hostdeploy.UpdateVdsActionParameters;
import org.ovirt.engine.core.common.action.hostdeploy.UpgradeHostParameters;
import org.ovirt.engine.core.common.businessentities.BusinessEntityMap;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.network.Bond;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.businessentities.network.NicLabel;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.pm.FenceOperationResult;
import org.ovirt.engine.core.common.businessentities.pm.FenceOperationResult.Status;
import org.ovirt.engine.core.common.businessentities.pm.PowerStatus;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.queries.DiscoverSendTargetsQueryParameters;
import org.ovirt.engine.core.common.queries.GetPermissionsForObjectParameters;
import org.ovirt.engine.core.common.queries.GetUnregisteredBlockStorageDomainsParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.NameQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;

public class BackendHostResource extends AbstractBackendActionableResource<Host, VDS> implements
        HostResource {

    public static final String STOP_GLUSTER_SERVICE = "stop_gluster_service";
    private static final String DEFAULT_ISCSI_PORT = "3260";

    private BackendHostsResource parent;

    public BackendHostResource(String id, BackendHostsResource parent) {
        super(id, Host.class, VDS.class, SUB_COLLECTIONS);
        this.parent = parent;
    }

    @Override
    public Host get() {
        // This logic shouldn't be part of the "get" method as it is an action. It will be replaced by
        // the "refresh" action and removed in the future.
        if (isForce()) {
            performAction(VdcActionType.RefreshHost, new VdsActionParameters(guid));
        }

        Host host = getVdsByVdsId();
        deprecatedAddLinksToAgents(host);
        return host;
    }

    private Host getVdsByVdsId() {
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

        Host host = performUpdate(incoming,
                entity,
                map(entity),
                hostResolver,
                VdcActionType.UpdateVds,
                new UpdateParametersProvider());
        deprecatedAddLinksToAgents(host);
        return host;
    }

    @Deprecated
    private void deprecatedAddLinksToAgents(Host host) {
        if (host.isSetPowerManagement() && host.getPowerManagement().isSetAgents()
                && host.getPowerManagement().getAgents().isSetAgents()) {
            for (Agent agent : host.getPowerManagement().getAgents().getAgents()) {
                Host host2 = new Host();
                host2.setId(host.getId());
                agent.setHost(host2);
                LinkHelper.addLinks(uriInfo, agent);
            }
        }
    }

    @Override
    public Response install(Action action) {
        // REVISIT fencing options
        VDS vds = getEntity();
        validateEnums(Action.class, action);
        UpdateVdsActionParameters params = new UpdateVdsActionParameters(vds.getStaticData(), action.getRootPassword(), true);
        params = (UpdateVdsActionParameters) getMapper
                (Action.class, VdsOperationActionParameters.class).map(action, (VdsOperationActionParameters) params);
        if (vds.isOvirtNode()) {
            params.setReinstallOrUpgrade(true);
            if (action.isSetImage()) {
                params.setoVirtIsoFile(action.getImage());
                return doAction(VdcActionType.UpgradeOvirtNode, params, action);
            }
        }
        return doAction(VdcActionType.UpdateVds,
                        params,
                        action);
    }

    @Override
    public Response upgrade(Action action) {
        UpgradeHostParameters params = new UpgradeHostParameters(guid);
        if (action.isSetImage()) {
            params.setoVirtIsoFile(action.getImage());
        }

        return doAction(VdcActionType.UpgradeHost, params, action);
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

    @Override
    public Response setupNetworks(Action action) {
        //verify if host exists to handle 404 status code.
        getVdsByVdsId();

        HostSetupNetworksParameters parameters = toParameters(action);
        return performAction(VdcActionType.HostSetupNetworks, parameters, action);
    }

    private HostSetupNetworksParameters toParameters(Action action) {
        HostSetupNetworksParameters parameters = new HostSetupNetworksParameters(guid);
        Map<Guid, NetworkAttachment> attachmentsById = getBackendNetworkAttachments();

        if (action.isSetModifiedNetworkAttachments()) {
            for (org.ovirt.engine.api.model.NetworkAttachment model : action.getModifiedNetworkAttachments()
                    .getNetworkAttachments()) {
                NetworkAttachment attachment = mapNetworkAttachment(attachmentsById, model);
                parameters.getNetworkAttachments().add(attachment);
            }
        }

        if (action.isSetSynchronizedNetworkAttachments()) {
            Map<Guid, NetworkAttachment> networkAttachmentFromParams =
                    Entities.businessEntitiesById(parameters.getNetworkAttachments());
            for (org.ovirt.engine.api.model.NetworkAttachment model : action.getSynchronizedNetworkAttachments()
                    .getNetworkAttachments()) {
                if (model.isSetId()) {
                    Guid networkAttachmentId = asGuid(model.getId());
                    if (networkAttachmentFromParams.containsKey(networkAttachmentId)) {
                        networkAttachmentFromParams.get(networkAttachmentId).setOverrideConfiguration(true);
                    } else if (attachmentsById.containsKey(networkAttachmentId)) {
                        NetworkAttachment networkAttachment = attachmentsById.get(networkAttachmentId);
                        networkAttachment.setOverrideConfiguration(true);
                        parameters.getNetworkAttachments().add(networkAttachment);
                    } else {
                        return handleError(new EntityNotFoundException("NetworkAttachment.id: " + model.getId()), true);
                    }
                }
            }
        }

        if (action.isSetModifiedLabels()) {
            for (Label label : action.getModifiedLabels().getLabels()) {
                NicLabel nicLabel = new NicLabel();
                nicLabel.setLabel(label.getId());
                if (label.isSetHostNic()) {
                    nicLabel.setNicId(label.getHostNic().isSetId() ? asGuid(label.getHostNic().getId()) : null);
                    nicLabel.setNicName(label.getHostNic().getName());
                }
                parameters.getLabels().add(nicLabel);
            }
        }

        if (action.isSetRemovedLabels()) {
            for (Label label : action.getRemovedLabels().getLabels()) {
                parameters.getRemovedLabels().add(label.getId());
            }
        }

        if (action.isSetRemovedNetworkAttachments()) {
            for (org.ovirt.engine.api.model.NetworkAttachment model : action.getRemovedNetworkAttachments()
                    .getNetworkAttachments()) {
                NetworkAttachment attachment = mapNetworkAttachment(attachmentsById, model);
                parameters.getRemovedNetworkAttachments().add(attachment.getId());
            }
        }

        BusinessEntityMap<Bond> bonds = getBackendHostBonds();
        if (action.isSetModifiedBonds()) {
            BusinessEntityMap<VdsNetworkInterface> nicsFromBackend = getBackendNics();
            for (HostNIC bond : action.getModifiedBonds().getHostNics()) {
                completeSlaveNames(nicsFromBackend, bond);
                parameters.getBonds().add(mapBonds(bonds, bond));
            }
        }

        if (action.isSetRemovedBonds()) {
            for (HostNIC bond : action.getRemovedBonds().getHostNics()) {
                parameters.getRemovedBonds().add(mapBonds(bonds, bond).getId());
            }
        }

        if (action.isSetCheckConnectivity()) {
            parameters.setRollbackOnFailure(action.isCheckConnectivity());
        }

        if (action.isSetConnectivityTimeout()) {
            parameters.setConectivityTimeout(action.getConnectivityTimeout());
        }

        return parameters;
    }

    private void completeSlaveNames(BusinessEntityMap<VdsNetworkInterface> nicsFromBackend, HostNIC bond) {
        if (bond.isSetBonding() && bond.getBonding().isSetSlaves()) {
            for (HostNIC slave : bond.getBonding().getSlaves().getSlaves()) {
                if (!slave.isSetName() && slave.isSetId()){
                    Guid slaveId = new Guid(slave.getId());
                    String slaveNameFromBackend = nicsFromBackend.get(slaveId).getName();
                    slave.setName(slaveNameFromBackend);
                }
            }
        }
    }

    public Map<Guid, NetworkAttachment> getBackendNetworkAttachments() {
        List<NetworkAttachment> backendAttachments =
                getBackendCollection(NetworkAttachment.class,
                        VdcQueryType.GetNetworkAttachmentsByHostId,
                        new IdQueryParameters(guid));
        return Entities.businessEntitiesById(backendAttachments);
    }

    public BusinessEntityMap<Bond> getBackendHostBonds() {
        List<Bond> backendBonds =
                getBackendCollection(Bond.class, VdcQueryType.GetHostBondsByHostId, new IdQueryParameters(guid));
        return new BusinessEntityMap<Bond>(backendBonds);
    }

    public BusinessEntityMap<VdsNetworkInterface> getBackendNics() {
        List<VdsNetworkInterface> backendNics =
                getBackendCollection(VdsNetworkInterface.class, VdcQueryType.GetVdsInterfacesByVdsId, new IdQueryParameters(guid));
        return new BusinessEntityMap<VdsNetworkInterface>(backendNics);
    }

    public NetworkAttachment mapNetworkAttachment(Map<Guid, NetworkAttachment> attachmentsById,
            org.ovirt.engine.api.model.NetworkAttachment model) {
        Mapper<org.ovirt.engine.api.model.NetworkAttachment, NetworkAttachment> networkAttachmentMapper =
                getMapper(org.ovirt.engine.api.model.NetworkAttachment.class, NetworkAttachment.class);
        NetworkAttachment attachment;
        if (model.isSetId()) {
            Guid attachmentId = asGuid(model.getId());
            attachment = networkAttachmentMapper.map(model, attachmentsById.get(attachmentId));
        } else {
            attachment = networkAttachmentMapper.map(model, null);
        }

        return attachment;
    }

    public Bond mapBonds(BusinessEntityMap<Bond> bonds, HostNIC model) {
        Mapper<HostNIC, Bond> hostNicMapper = getMapper(HostNIC.class, Bond.class);
        Bond bond;
        if (model.isSetId()) {
            Guid nicId = asGuid(model.getId());
            bond = hostNicMapper.map(model, bonds.get(nicId));
        } else {
            Bond template = model.isSetName() ? bonds.get(model.getName()) : null;
            bond = hostNicMapper.map(model, template);
        }

        return bond;
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
                new MaintenanceNumberOfVdssParameters(asList(guid), false, action.isSetReason() ? action.getReason()
                        : null, getBooleanMatrixParam(STOP_GLUSTER_SERVICE)),
                action);
    }

    @Override
    public Response forceSelectSPM(Action action) {
        return doAction(VdcActionType.ForceSelectSPM,
                new ForceSelectSPMParameters(guid), action);
    }

    @Override
    public Response enrollCertificate(Action action) {
        return doAction(VdcActionType.HostEnrollCertificate, new VdsActionParameters(guid), action);
    }

    @Override
    public Response iscsiLogin(Action action) {
        validateParameters(action, "iscsi.address", "iscsi.target");
        StorageServerConnections cnx = new StorageServerConnections();
        IscsiDetails iscsiDetails = action.getIscsi();
        cnx.setconnection(iscsiDetails.getAddress());
        cnx.setiqn(iscsiDetails.getTarget());
        cnx.setstorage_type(StorageType.ISCSI);
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

        StorageServerConnectionParametersBase connectionParms = new StorageServerConnectionParametersBase(cnx, guid, false);
        return doAction(VdcActionType.ConnectStorageToVds, connectionParms, action);
    }

    @Override
    public Response unregisteredStorageDomainsDiscover(Action action) {
        StorageType storageType =
                ((action.getIscsi() != null) && (action.getIscsi().getAddress() != null)) ? StorageType.ISCSI
                        : StorageType.FCP;


        // Validate if the Host exists.
        getEntity();
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
        connectionDetails.setstorage_type(StorageType.ISCSI);
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
            return fence(action, VdcActionType.RestartVds);
        case START:
            return fence(action, VdcActionType.StartVds);
        case STOP:
            return fence(action, VdcActionType.StopVds);
        case STATUS:
            return getFenceStatus(action);
        default:
            return null;
        }
    }

    private Response getFenceStatus(Action action) {
        VDSReturnValue result = getEntity(
                VDSReturnValue.class,
                VdcQueryType.GetVdsFenceStatus,
                new IdQueryParameters(guid),
                guid.toString());
        FenceOperationResult fenceResult = (FenceOperationResult) result.getReturnValue();
        if (fenceResult.getStatus() == Status.SUCCESS) {
            PowerManagement pm = new PowerManagement();
            pm.setStatus(StatusUtils.create(convertPowerStatus(fenceResult.getPowerStatus())));
            action.setPowerManagement(pm);
            return actionSuccess(action);
        } else {
            return handleFailure(action, fenceResult.getMessage());
        }
    }

    private PowerManagementStatus convertPowerStatus(PowerStatus status) {
        switch (status) {
            case ON:
                return PowerManagementStatus.ON;

            case OFF:
                return PowerManagementStatus.OFF;

            default:
                return PowerManagementStatus.UNKNOWN;
        }
    }

    private Response handleFailure(Action action, String message) {
        action.setStatus(StatusUtils.create(CreationStatus.FAILED));
        action.setFault(new Fault());
        action.getFault().setReason(message);
        return Response.ok().entity(action).build();
    }

    private Response fence(Action action, VdcActionType vdcAction) {
        return doAction(vdcAction, new FenceVdsActionParameters(guid), action);
    }

    private Response fenceManually(Action action) {
        FenceVdsManualyParameters params = new FenceVdsManualyParameters(true);
        params.setVdsId(guid);
        params.setStoragePoolId(getEntity().getStoragePoolId());
        return doAction(VdcActionType.FenceVdsManualy, params, action);
    }

    @Override
    public Response refresh(Action action) {
        return doAction(VdcActionType.RefreshHost, new VdsActionParameters(guid), action);
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
    public UnmanagedNetworksResource getUnmanagedNetworksResource() {
        return inject(new BackendUnmanagedNetworksResource(guid));
    }

    @Override
    public NetworkAttachmentsResource getNetworkAttachmentsResource() {
        return inject(new BackendHostNetworkAttachmentsResource(guid));
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
    public HostDevicesResource getHostDevicesResource() {
        return inject(new BackendHostDevicesResource(guid));
    }

    public StorageServerConnectionExtensionsResource getStorageConnectionExtensionsResource() {
        return inject(new BackendStorageServerConnectionExtensionsResource(guid));
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
            // Updating Fence-agents is deprecated from this context, so the original, unchanged, list of agents is
            // passed to the engine.
            updateParams.setFenceAgents(entity.getFenceAgents());
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
        parent.addStatistics(model, entity);
        parent.addCertificateInfo(model);
        return model;
    }


    @Override
    public Response remove() {
        get();
        return performAction(VdcActionType.RemoveVds, new RemoveVdsParameters(guid));
    }

    @Override
    public Response remove(Action action) {
        get();
        boolean force = false;
        if (action != null && action.isSetForce()) {
            force = action.isForce();
        }
        return performAction(VdcActionType.RemoveVds, new RemoveVdsParameters(guid, force));
    }

    public BackendHostsResource getParent() {
        return this.parent;
    }

    @Override
    public BackendHostHooksResource getHooksResource() {
        return inject(new BackendHostHooksResource(id));
    }

    @Override
    public FenceAgentsResource getFenceAgentsResource() {
        return inject(new BackendFenceAgentsResource(id));
    }

    @Override
    public KatelloErrataResource getKatelloErrataResource() {
        return inject(new BackendHostKatelloErrataResource(id));
    }

    private boolean getBooleanMatrixParam(String parameter) {
        if (getUriInfo() != null && QueryHelper.hasMatrixParam(getUriInfo(), parameter)) {
            String matrixString = QueryHelper.getMatrixConstraint(getUriInfo(), parameter);
            return Boolean.valueOf(matrixString);
        } else {
            return false;
        }
    }

}

package org.ovirt.engine.api.restapi.resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.CreationStatus;
import org.ovirt.engine.api.model.Fault;
import org.ovirt.engine.api.model.FenceType;
import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.HostNic;
import org.ovirt.engine.api.model.IscsiDetails;
import org.ovirt.engine.api.model.IscsiDetailss;
import org.ovirt.engine.api.model.LogicalUnit;
import org.ovirt.engine.api.model.NetworkLabel;
import org.ovirt.engine.api.model.PowerManagement;
import org.ovirt.engine.api.model.PowerManagementStatus;
import org.ovirt.engine.api.model.SshAuthenticationMethod;
import org.ovirt.engine.api.model.StorageDomains;
import org.ovirt.engine.api.resource.ActionResource;
import org.ovirt.engine.api.resource.AssignedAffinityLabelsResource;
import org.ovirt.engine.api.resource.AssignedPermissionsResource;
import org.ovirt.engine.api.resource.AssignedTagsResource;
import org.ovirt.engine.api.resource.ExternalNetworkProviderConfigurationsResource;
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
import org.ovirt.engine.api.restapi.resource.externalhostproviders.BackendHostKatelloErrataResource;
import org.ovirt.engine.api.restapi.types.Mapper;
import org.ovirt.engine.api.restapi.util.ParametersHelper;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ChangeVDSClusterParameters;
import org.ovirt.engine.core.common.action.CopyHostNetworksParameters;
import org.ovirt.engine.core.common.action.CreateOrUpdateBond;
import org.ovirt.engine.core.common.action.FenceVdsActionParameters;
import org.ovirt.engine.core.common.action.FenceVdsManualyParameters;
import org.ovirt.engine.core.common.action.ForceSelectSPMParameters;
import org.ovirt.engine.core.common.action.HostSetupNetworksParameters;
import org.ovirt.engine.core.common.action.MaintenanceNumberOfVdssParameters;
import org.ovirt.engine.core.common.action.PersistentHostSetupNetworksParameters;
import org.ovirt.engine.core.common.action.RemoveVdsParameters;
import org.ovirt.engine.core.common.action.StorageServerConnectionParametersBase;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.action.VdsOperationActionParameters;
import org.ovirt.engine.core.common.action.hostdeploy.ApproveVdsParameters;
import org.ovirt.engine.core.common.action.hostdeploy.UpdateVdsActionParameters;
import org.ovirt.engine.core.common.action.hostdeploy.UpgradeHostParameters;
import org.ovirt.engine.core.common.businessentities.BusinessEntityMap;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.HostedEngineDeployConfiguration;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.VDS;
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
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;

public class BackendHostResource extends AbstractBackendActionableResource<Host, VDS> implements HostResource {

    public static final String FORCE = "force";
    public static final String STOP_GLUSTER_SERVICE = "stop_gluster_service";

    private static final String DEFAULT_ISCSI_PORT = "3260";

    private BackendHostsResource parent;

    public BackendHostResource(String id, BackendHostsResource parent) {
        super(id, Host.class, VDS.class);
        this.parent = parent;
    }

    @Override
    public Host get() {
        return getVdsByVdsId();
    }

    private Host getVdsByVdsId() {
        return performGet(QueryType.GetVdsByVdsId, new IdQueryParameters(guid));
    }

    @Override
    public Host update(Host incoming) {
        QueryIdResolver<Guid> hostResolver = new QueryIdResolver<>(QueryType.GetVdsByVdsId, IdQueryParameters.class);
        VDS entity = getEntity(hostResolver, true);
        BackendExternalProviderHelper.completeExternalNetworkProviderConfigurations(this,
                incoming.getExternalNetworkProviderConfigurations());
        // if fence agents list is null set it to null in entity
        if(incoming.getAgents() == null) {
            entity.setFenceAgents(null);
        }
        if (incoming.isSetCluster() && (incoming.getCluster().isSetId() || incoming.getCluster().isSetName())) {
            Guid clusterId = lookupClusterId(incoming);
            if (!clusterId.equals(entity.getClusterId())) {
                performAction(ActionType.ChangeVDSCluster,
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
                ActionType.UpdateVds,
                new UpdateParametersProvider());
        return host;
    }

    @Override
    public Response install(Action action) {
        // REVISIT fencing options
        VDS vds = getEntity();
        UpdateVdsActionParameters params = new UpdateVdsActionParameters(vds.getStaticData(), action.getRootPassword(), true);
        params.setFenceAgents(null);  // Explicitly set null, to be clear we don't want to update fence agents.
        if (action.isSetDeployHostedEngine() && action.isDeployHostedEngine()) {
            params.setHostedEngineDeployConfiguration(
                    new HostedEngineDeployConfiguration(HostedEngineDeployConfiguration.Action.DEPLOY));
        } else if (action.isSetUndeployHostedEngine() && action.isUndeployHostedEngine()) {
            params.setHostedEngineDeployConfiguration(
                    new HostedEngineDeployConfiguration(HostedEngineDeployConfiguration.Action.UNDEPLOY));
        }
        params = (UpdateVdsActionParameters) getMapper
                (Action.class, VdsOperationActionParameters.class).map(action, params);
        // Installation is only done in maintenance mode, and should by default leave the host in maintenance mode.
        // this is why the default value for 'activate' here is false (vs in adding or approving a host, where it is 'true')
        boolean activate = action.isSetActivate() ? action.isActivate() :
            ParametersHelper.getBooleanParameter(httpHeaders, uriInfo, BackendHostsResource.ACTIVATE, true, false);
        params.setActivateHost(activate);
        // Default value for 'reboot' is true
        boolean reboot = action.isSetReboot() ? action.isReboot() :
                ParametersHelper.getBooleanParameter(httpHeaders, uriInfo, BackendHostsResource.REBOOT, true, true);
        params.setRebootHost(reboot);
        return doAction(ActionType.UpdateVds,
                        params,
                        action);
    }

    @Override
    public Response upgrade(Action action) {
        UpgradeHostParameters params = new UpgradeHostParameters(guid);
        if (action.isSetImage()) {
            params.setoVirtIsoFile(action.getImage());
        }
        if (action.isSetReboot()) {
            params.setReboot(action.isReboot());
        }
        if (action.isSetTimeout()) {
            params.setTimeout(action.getTimeout());
        }

        return doAction(ActionType.UpgradeHost, params, action);
    }

    @Override
    public Response upgradeCheck(Action action) {
        VdsActionParameters params = new VdsActionParameters(guid);

        return doAction(ActionType.HostUpgradeCheck, params, action);
    }

    @Override
    public Response activate(Action action) {
        return doAction(ActionType.ActivateVds,
                        new VdsActionParameters(guid),
                        action);
    }

    @Override
    public Response approve(Action action) {

        if (action.isSetCluster() && (action.getCluster().isSetId() || action.getCluster().isSetName())) {
            update(setCluster(get(), action.getCluster()));
        }
        ApproveVdsParameters params = new ApproveVdsParameters(guid);
        params = (ApproveVdsParameters) getMapper
                (Action.class, VdsOperationActionParameters.class).map(action, params);

        // Set pk authentication as default
        params.setAuthMethod(VdsOperationActionParameters.AuthenticationMethod.PublicKey);

        if (action.isSetRootPassword()) {
            params.setAuthMethod(VdsOperationActionParameters.AuthenticationMethod.Password);
            params.setRootPassword(action.getRootPassword());
        } else if (action.isSetSsh() && action.getSsh().isSetAuthenticationMethod()) {
            if (action.getSsh().getAuthenticationMethod() == SshAuthenticationMethod.PASSWORD) {
                params.setAuthMethod(VdsOperationActionParameters.AuthenticationMethod.Password);
                if (action.getSsh().isSetUser() && action.getSsh().getUser().isSetPassword()) {
                    params.setPassword(action.getSsh().getUser().getPassword());
                }
            }
        }
        // By default activate and reboot the host after approval
        boolean activate = action.isSetActivate() ? action.isActivate() :
            ParametersHelper.getBooleanParameter(httpHeaders, uriInfo, BackendHostsResource.ACTIVATE, true, true);
        params.setActivateHost(activate);
        boolean reboot = action.isSetReboot() ? action.isReboot() :
                ParametersHelper.getBooleanParameter(httpHeaders, uriInfo, BackendHostsResource.REBOOT, true, true);
        params.setRebootHost(reboot);

        return doAction(ActionType.ApproveVds,
                        params,
                        action);
    }

    @Override
    public Response syncAllNetworks(Action action) {
        return doAction(ActionType.SyncAllHostNetworks,
                new PersistentHostSetupNetworksParameters(guid),
                action);
    }

    @Override
    public Response copyHostNetworks(Action action) {
        var parameters = new CopyHostNetworksParameters();
        Host sourceHost = action.getSourceHost();
        parameters.setSourceHostId(Guid.createGuidFromString(sourceHost.getId()));
        parameters.setVdsId(guid);
        return doAction(ActionType.CopyHostNetworks, parameters, action);
    }

    @Override
    public Response setupNetworks(Action action) {
        //verify if host exists to handle 404 status code.
        getVdsByVdsId();

        HostSetupNetworksParameters parameters = toParameters(action);
        return performAction(ActionType.HostSetupNetworks, parameters, action);
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
            for (NetworkLabel label : action.getModifiedLabels().getNetworkLabels()) {
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
            for (NetworkLabel label : action.getRemovedLabels().getNetworkLabels()) {
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
            for (HostNic bond : action.getModifiedBonds().getHostNics()) {
                completeSlaveNames(nicsFromBackend, bond);
                parameters.getCreateOrUpdateBonds().add(mapBonds(bonds, bond));
            }
        }

        if (action.isSetRemovedBonds()) {
            for (HostNic bond : action.getRemovedBonds().getHostNics()) {
                parameters.getRemovedBonds().add(mapBonds(bonds, bond).getId());
            }
        }

        if (action.isSetCheckConnectivity()) {
            parameters.setRollbackOnFailure(action.isCheckConnectivity());
        }

        if (action.isSetConnectivityTimeout()) {
            parameters.setConectivityTimeout(action.getConnectivityTimeout());
        }

        parameters.setCommitOnSuccess(action.isSetCommitOnSuccess() && action.isCommitOnSuccess());

        return parameters;
    }

    private void completeSlaveNames(BusinessEntityMap<VdsNetworkInterface> nicsFromBackend, HostNic bond) {
        if (bond.isSetBonding() && bond.getBonding().isSetSlaves()) {
            for (HostNic slave : bond.getBonding().getSlaves().getHostNics()) {
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
                        QueryType.GetNetworkAttachmentsByHostId,
                        new IdQueryParameters(guid));
        return Entities.businessEntitiesById(backendAttachments);
    }

    public BusinessEntityMap<Bond> getBackendHostBonds() {
        List<Bond> backendBonds =
                getBackendCollection(Bond.class, QueryType.GetHostBondsByHostId, new IdQueryParameters(guid));
        return new BusinessEntityMap<>(backendBonds);
    }

    public BusinessEntityMap<VdsNetworkInterface> getBackendNics() {
        List<VdsNetworkInterface> backendNics =
                getBackendCollection(VdsNetworkInterface.class, QueryType.GetVdsInterfacesByVdsId, new IdQueryParameters(guid));
        return new BusinessEntityMap<>(backendNics);
    }

    public NetworkAttachment mapNetworkAttachment(Map<Guid, NetworkAttachment> attachmentsById,
            org.ovirt.engine.api.model.NetworkAttachment model) {
        Mapper<org.ovirt.engine.api.model.NetworkAttachment, NetworkAttachment> networkAttachmentMapper =
                getMapper(org.ovirt.engine.api.model.NetworkAttachment.class, NetworkAttachment.class);
        NetworkAttachment attachment = null;
        if (model.isSetId()) {
            attachment = attachmentsById.get(asGuid(model.getId()));
        } else if (model.isSetNetwork() && (model.getNetwork().isSetName() || model.getNetwork().isSetId())) {
            for (Map.Entry<Guid, NetworkAttachment> backendNetworkAttachmentMapEntry : attachmentsById.entrySet()) {
                NetworkAttachment backendNetworkAttachment = backendNetworkAttachmentMapEntry.getValue();
                String backendNetworkName = backendNetworkAttachment.getNetworkName();
                String backendNetworkId = backendNetworkAttachment.getNetworkId().toString();
                if (backendNetworkName.equals(model.getNetwork().getName()) ||
                        backendNetworkId.equals(model.getNetwork().getId())) {
                    attachment = backendNetworkAttachment;
                    break;
                }
            }
        }

        return networkAttachmentMapper.map(model,  attachment);
    }


    public CreateOrUpdateBond mapBonds(BusinessEntityMap<Bond> bonds, HostNic model) {
        Mapper<HostNic, Bond> hostNicMapper = getMapper(HostNic.class, Bond.class);
        Bond bond;
        if (model.isSetId()) {
            Guid nicId = asGuid(model.getId());
            bond = hostNicMapper.map(model, bonds.get(nicId));
        } else {
            Bond template = model.isSetName() ? bonds.get(model.getName()) : null;
            bond = hostNicMapper.map(model, template);
        }

        return CreateOrUpdateBond.fromBond(bond);
    }

    private Host setCluster(Host host, org.ovirt.engine.api.model.Cluster cluster) {
        if (cluster.isSetId()) {
            host.setCluster(cluster);
        } else {
            host.setCluster(new org.ovirt.engine.api.model.Cluster());
            host.getCluster().setId(lookupClusterByName(cluster.getName()).getId().toString());
        }
        return host;
    }

    protected Guid lookupClusterId(Host host) {
        return host.getCluster().isSetId() ? asGuid(host.getCluster().getId())
                                           :
                                           lookupClusterByName(host.getCluster().getName()).getId();
    }

    @Override
    protected Cluster lookupClusterByName(String name) {
        return getEntity(Cluster.class,
                QueryType.GetClusterByName,
                new NameQueryParameters(name),
                "Cluster: name=" + name);
    }

    @Override
    public Response deactivate(Action action) {
        boolean stopGlusterService = ParametersHelper.getBooleanParameter(httpHeaders, uriInfo, STOP_GLUSTER_SERVICE, true, false);
        return doAction(ActionType.MaintenanceNumberOfVdss,
                new MaintenanceNumberOfVdssParameters(asList(guid), false, action.isSetReason() ? action.getReason()
                        : null, stopGlusterService),
                action);
    }

    @Override
    public Response forceSelectSpm(Action action) {
        return doAction(ActionType.ForceSelectSPM,
                new ForceSelectSPMParameters(guid), action);
    }

    @Override
    public Response enrollCertificate(Action action) {
        return doAction(ActionType.HostEnrollCertificate, new VdsActionParameters(guid), action);
    }

    @Override
    public Response iscsiLogin(Action action) {
        validateParameters(action, "iscsi.address", "iscsi.target");
        StorageServerConnections cnx = new StorageServerConnections();
        IscsiDetails iscsiDetails = action.getIscsi();
        cnx.setConnection(iscsiDetails.getAddress());
        cnx.setIqn(iscsiDetails.getTarget());
        cnx.setStorageType(StorageType.ISCSI);
        if (iscsiDetails.isSetPort()) {
            cnx.setPort(iscsiDetails.getPort().toString());
        } else {
            cnx.setPort(DEFAULT_ISCSI_PORT);
        }
        if (iscsiDetails.isSetPortal()) {
            cnx.setPortal(iscsiDetails.getPortal());
        }
        if (iscsiDetails.isSetUsername()) {
            cnx.setUserName(iscsiDetails.getUsername());
        }
        if (iscsiDetails.isSetPassword()) {
            cnx.setPassword(iscsiDetails.getPassword());
        }

        StorageServerConnectionParametersBase connectionParms = new StorageServerConnectionParametersBase(cnx, guid, false);
        return doAction(ActionType.ConnectStorageToVds, connectionParms, action);
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
            Action.IscsiTargetsList iscsiTargets = action.getIscsiTargets();
            if (iscsiTargets != null) {
                for (String iscsiTarget : iscsiTargets.getIscsiTargets()) {
                    StorageServerConnections connectionDetails = getInitializedConnectionIscsiDetails(action);
                    connectionDetails.setIqn(iscsiTarget);
                    storageServerConnections.add(connectionDetails);
                }
            }
       } else {
            // For FC we don't need to do anything.
        }
        GetUnregisteredBlockStorageDomainsParameters unregisteredBlockStorageDomainsParameters =
                new GetUnregisteredBlockStorageDomainsParameters(guid, storageType, storageServerConnections);
        try {
            Pair<List<StorageDomain>, List<StorageServerConnections>> pair =
                    getEntity(Pair.class,
                        QueryType.GetUnregisteredBlockStorageDomains,
                        unregisteredBlockStorageDomainsParameters,
                        "GetUnregisteredBlockStorageDomains", true);

            List<StorageDomain> storageDomains = pair.getFirst();
            return actionSuccess(mapToStorageDomains(action, storageDomains));
        } catch (Exception e) {
            return handleError(e, false);
        }
    }

    @Override
    public Response discoverIscsi(Action action) {
        //In terms of implementation, this method does the same as iscsiDiscover.
        //But since the two are annotated differently in ovirt-engine-api-model,
        //the SDKS will interpret the response differently.
        //For iscsiDiscover, the SDKs will consider action.iscsiTargets in the Response object.
        //For discoverIscsi, the SDKs will consider action.iscsiDetails in the Response object.
        //This fixes https://bugzilla.redhat.com/1926819
        return iscsiDiscover(action);
    }

    @Override
    public Response iscsiDiscover(Action action) {
        validateParameters(action, "iscsi.address");

        List<StorageServerConnections> result = getBackendCollection(StorageServerConnections.class,
                                                                       QueryType.DiscoverSendTargets,
                                                                       createDiscoveryQueryParams(action));

        return actionSuccess(mapTargets(action, result));
    }

    private Action mapTargets(Action action, List<StorageServerConnections> targets) {
        if (targets != null) {
            Action.IscsiTargetsList iscsiTargets = new Action.IscsiTargetsList();
            IscsiDetailss iscsiDetailss = new IscsiDetailss();
            for (StorageServerConnections cnx : targets) {
                LogicalUnit logicalUnit = map(cnx);
                // The iscsiTargets property is replaced by discoveredTargets. The property is preserved
                // for backward compatibility, and should be removed in version 5 of the API.
                iscsiTargets.getIscsiTargets().add(logicalUnit.getTarget());
                iscsiDetailss.getIscsiDetailss().add(mapLogicalUnitToIscsiDetails(logicalUnit));
            }
            action.setIscsiTargets(iscsiTargets);
            action.setDiscoveredTargets(iscsiDetailss);
        }
        return action;
    }

    private IscsiDetails mapLogicalUnitToIscsiDetails(LogicalUnit logicalUnit) {
        IscsiDetails iscsiDetails = new IscsiDetails();
        iscsiDetails.setAddress(logicalUnit.getAddress());
        iscsiDetails.setPort(logicalUnit.getPort());
        iscsiDetails.setTarget(logicalUnit.getTarget());
        iscsiDetails.setPortal(logicalUnit.getPortal());
        iscsiDetails.setPaths(logicalUnit.getPaths());
        iscsiDetails.setVendorId(logicalUnit.getVendorId());
        iscsiDetails.setProductId(logicalUnit.getProductId());
        iscsiDetails.setSerial(logicalUnit.getSerial());
        return iscsiDetails;
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
        connectionDetails.setConnection(iscsiDetails.getAddress());
        connectionDetails.setStorageType(StorageType.ISCSI);
        if (iscsiDetails.isSetPort()) {
            connectionDetails.setPort(iscsiDetails.getPort().toString());
        } else {
            connectionDetails.setPort(DEFAULT_ISCSI_PORT);
        }
        if (iscsiDetails.isSetUsername()) {
            connectionDetails.setUserName(iscsiDetails.getUsername());
        }
        if (iscsiDetails.isSetPassword()) {
            connectionDetails.setPassword(iscsiDetails.getPassword());
        }
        return connectionDetails;
    }

    @Override
    public Response commitNetConfig(Action action) {
        return doAction(ActionType.CommitNetworkChanges,
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
            return fenceRestart(action);
        case START:
            return fence(action, ActionType.StartVds);
        case STOP:
            return fence(action, ActionType.StopVds);
        case STATUS:
            return getFenceStatus(action);
        default:
            return null;
        }
    }

    private Response getFenceStatus(Action action) {
        FenceOperationResult result = getEntity(
                FenceOperationResult.class,
                QueryType.GetVdsFenceStatus,
                new IdQueryParameters(guid),
                guid.toString());
        if (result.getStatus() == Status.SUCCESS) {
            PowerManagement pm = new PowerManagement();
            pm.setStatus(convertPowerStatus(result.getPowerStatus()));
            action.setPowerManagement(pm);
            return actionSuccess(action);
        } else {
            return handleFailure(action, result.getMessage());
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
        action.setStatus(CreationStatus.FAILED.value());
        action.setFault(new Fault());
        action.getFault().setReason(message);
        return Response.ok().entity(action).build();
    }

    private Response fenceRestart(Action action) {
        FenceVdsActionParameters params = new FenceVdsActionParameters(guid);
        if (action.isSetMaintenanceAfterRestart()) {
            params.setChangeHostToMaintenanceOnStart(action.isMaintenanceAfterRestart());
        }
        return doAction(ActionType.RestartVds, params, action);
    }

    private Response fence(Action action, ActionType vdcAction) {
        return doAction(vdcAction, new FenceVdsActionParameters(guid), action);
    }

    private Response fenceManually(Action action) {
        FenceVdsManualyParameters params = new FenceVdsManualyParameters(true);
        params.setVdsId(guid);
        params.setStoragePoolId(getEntity().getStoragePoolId());
        return doAction(ActionType.FenceVdsManualy, params, action);
    }

    @Override
    public Response refresh(Action action) {
        return doAction(ActionType.RefreshHost, new VdsActionParameters(guid), action);
    }

    @Override
    public HostNumaNodesResource getNumaNodesResource() {
        return inject(new BackendHostNumaNodesResource(id));
    }

    @Override
    public HostNicsResource getNicsResource() {
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
    public HostStorageResource getStorageResource() {
        return inject(new BackendHostStorageResource(id));
    }

    @Override
    public AssignedTagsResource getTagsResource() {
        return inject(new BackendHostTagsResource(id));
    }

    @Override
    public AssignedPermissionsResource getPermissionsResource() {
        return inject(new BackendAssignedPermissionsResource(guid,
                                                             QueryType.GetPermissionsForObject,
                                                             new GetPermissionsForObjectParameters(guid),
                                                             Host.class,
                                                             VdcObjectType.VDS));
    }

    @Override
    public StatisticsResource getStatisticsResource() {
        EntityIdResolver<Guid> resolver = new QueryIdResolver<>(QueryType.GetVdsByVdsId, IdQueryParameters.class);
        HostStatisticalQuery query = new HostStatisticalQuery(resolver, newModel(id));
        return inject(new BackendStatisticsResource<>(entityType, guid, query));
    }

    @Override
    public ActionResource getActionResource(String action, String ids) {
        return inject(new BackendActionResource(action, ids));
    }

    @Override
    public HostDevicesResource getDevicesResource() {
        return inject(new BackendHostDevicesResource(guid));
    }

    @Override
    public StorageServerConnectionExtensionsResource getStorageConnectionExtensionsResource() {
        return inject(new BackendStorageServerConnectionExtensionsResource(guid));
    }

    @Override
    protected VDS getEntity() {
        return getEntity(VDS.class, QueryType.GetVdsByVdsId, new IdQueryParameters(guid), id);
    }

    protected class UpdateParametersProvider implements ParametersProvider<Host, VDS> {
        @Override
        public ActionParametersBase getParameters(Host incoming, VDS entity) {
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
                    (Host.class, VdsOperationActionParameters.class).map(incoming, updateParams);
            return updateParams;
        }
    }

    @Override
    protected Host doPopulate(Host model, VDS entity) {
        Host host = parent.addHostedEngineIfConfigured(model, entity);
        parent.reportNetworkOperationInProgress(host, entity);
        return host;
    }

    @Override
    public Response remove() {
        get();
        boolean force = ParametersHelper.getBooleanParameter(httpHeaders, uriInfo, FORCE, true, false);
        return performAction(ActionType.RemoveVds, new RemoveVdsParameters(guid, force));
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
        return inject(new BackendFenceAgentsResource(guid));
    }

    @Override
    public KatelloErrataResource getKatelloErrataResource() {
        return inject(new BackendHostKatelloErrataResource(id));
    }

    @Override
    public AssignedAffinityLabelsResource getAffinityLabelsResource() {
        return inject(new BackendAssignedAffinityLabelsResource(id, VDS::new));
    }

    @Override
    public ExternalNetworkProviderConfigurationsResource getExternalNetworkProviderConfigurationsResource() {
        return inject(new BackendHostExternalNetworkProviderConfigurationsResource(guid));
    }
}

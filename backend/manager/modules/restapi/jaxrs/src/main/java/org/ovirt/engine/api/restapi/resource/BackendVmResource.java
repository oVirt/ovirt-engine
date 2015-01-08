package org.ovirt.engine.api.restapi.resource;

import static org.ovirt.engine.api.restapi.resource.BackendVmsResource.SUB_COLLECTIONS;
import static org.ovirt.engine.core.utils.Ticketing.generateOTP;

import java.util.List;
import java.util.Set;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.ovirt.engine.api.common.util.DetailHelper;
import org.ovirt.engine.api.common.util.QueryHelper;
import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.AuthorizedKey;
import org.ovirt.engine.api.model.CdRom;
import org.ovirt.engine.api.model.CdRoms;
import org.ovirt.engine.api.model.Certificate;
import org.ovirt.engine.api.model.CloudInit;
import org.ovirt.engine.api.model.CreationStatus;
import org.ovirt.engine.api.model.Display;
import org.ovirt.engine.api.model.Statistic;
import org.ovirt.engine.api.model.Statistics;
import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.api.model.Ticket;
import org.ovirt.engine.api.model.VM;
import org.ovirt.engine.api.resource.ActionResource;
import org.ovirt.engine.api.resource.AssignedPermissionsResource;
import org.ovirt.engine.api.resource.AssignedTagsResource;
import org.ovirt.engine.api.resource.CreationResource;
import org.ovirt.engine.api.resource.DevicesResource;
import org.ovirt.engine.api.resource.SnapshotsResource;
import org.ovirt.engine.api.resource.StatisticsResource;
import org.ovirt.engine.api.resource.VmApplicationsResource;
import org.ovirt.engine.api.resource.VmDisksResource;
import org.ovirt.engine.api.resource.VmNicsResource;
import org.ovirt.engine.api.resource.VmNumaNodesResource;
import org.ovirt.engine.api.resource.VmReportedDevicesResource;
import org.ovirt.engine.api.resource.VmResource;
import org.ovirt.engine.api.resource.VmSessionsResource;
import org.ovirt.engine.api.resource.WatchdogsResource;
import org.ovirt.engine.api.restapi.logging.Messages;
import org.ovirt.engine.api.restapi.types.RngDeviceMapper;
import org.ovirt.engine.api.restapi.types.VmMapper;
import org.ovirt.engine.api.restapi.util.DisplayHelper;
import org.ovirt.engine.api.utils.LinkHelper;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ChangeVMClusterParameters;
import org.ovirt.engine.core.common.action.CloneVmParameters;
import org.ovirt.engine.core.common.action.MigrateVmParameters;
import org.ovirt.engine.core.common.action.MigrateVmToServerParameters;
import org.ovirt.engine.core.common.action.MoveVmParameters;
import org.ovirt.engine.core.common.action.RemoveVmFromPoolParameters;
import org.ovirt.engine.core.common.action.RestoreAllSnapshotsParameters;
import org.ovirt.engine.core.common.action.RunVmOnceParams;
import org.ovirt.engine.core.common.action.RunVmParams;
import org.ovirt.engine.core.common.action.SetHaMaintenanceParameters;
import org.ovirt.engine.core.common.action.SetVmTicketParameters;
import org.ovirt.engine.core.common.action.ShutdownVmParameters;
import org.ovirt.engine.core.common.action.StopVmParameters;
import org.ovirt.engine.core.common.action.StopVmTypeEnum;
import org.ovirt.engine.core.common.action.TryBackToAllSnapshotsOfVmParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VmManagementParametersBase;
import org.ovirt.engine.core.common.action.VmOperationParameterBase;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.businessentities.HaMaintenanceMode;
import org.ovirt.engine.core.common.businessentities.InitializationType;
import org.ovirt.engine.core.common.businessentities.SnapshotActionEnum;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.VmInit;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.queries.GetPermissionsForObjectParameters;
import org.ovirt.engine.core.common.queries.GetVmTemplateParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.NameQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendVmResource extends
        AbstractBackendActionableResource<VM, org.ovirt.engine.core.common.businessentities.VM> implements
        VmResource {

    private static final long DEFAULT_TICKET_EXPIRY = 120 * 60; // 2 hours
    private BackendVmsResource parent;

    public static final String NEXT_RUN = "next_run";

    public BackendVmResource(String id, BackendVmsResource parent) {
        super(id, VM.class, org.ovirt.engine.core.common.businessentities.VM.class, SUB_COLLECTIONS);
        this.parent = parent;
    }

    private boolean isNextRunRequested() {
        return QueryHelper.getMatrixConstraints(getUriInfo(), NEXT_RUN).containsKey(NEXT_RUN);
    }

    @Override
    public VM get() {
        VM vm;
        if (isNextRunRequested()) {
            org.ovirt.engine.core.common.businessentities.VM entity =
                    getEntity(org.ovirt.engine.core.common.businessentities.VM.class, VdcQueryType.GetVmNextRunConfiguration,
                            new IdQueryParameters(guid), id, true);
            vm = addLinks(populate(VmMapper.map(entity, null, false), entity));

        } else {
            vm = performGet(VdcQueryType.GetVmByVmId, new IdQueryParameters(guid));
        }
        DisplayHelper.adjustDisplayData(this, vm);
        return removeRestrictedInfo(vm);
    }

    private VM removeRestrictedInfo(VM vm) {
        // Filtered users are not allowed to view host related information
        if (vm != null && isFiltered()) {
            vm.setHost(null);
            vm.setPlacementPolicy(null);
        }
        return vm;
    }

    @Override
    public VM update(VM incoming) {
        validateEnums(VM.class, incoming);
        validateParameters(incoming);
        if (incoming.isSetCluster() && (incoming.getCluster().isSetId() || incoming.getCluster().isSetName())) {
            Guid clusterId = lookupClusterId(incoming);
            if(!clusterId.toString().equals(get().getCluster().getId())){
                performAction(VdcActionType.ChangeVMCluster,
                              new ChangeVMClusterParameters(clusterId, guid));
            }
        }
        if (!isFiltered()) {
            //if the user updated the host within placement-policy, but supplied host-name rather than the host-id (legal) -
            //resolve the host's ID, because it will be needed down the line
            if (incoming.isSetPlacementPolicy() && incoming.getPlacementPolicy().isSetHost()
                    && incoming.getPlacementPolicy().getHost().isSetName() && !incoming.getPlacementPolicy().getHost().isSetId()) {
                incoming.getPlacementPolicy().getHost().setId(getHostId(incoming.getPlacementPolicy().getHost().getName()));
            }
        } else {
            incoming.setPlacementPolicy(null);
        }

        return removeRestrictedInfo(
                performUpdate(incoming,
                             new QueryIdResolver<Guid>(VdcQueryType.GetVmByVmId, IdQueryParameters.class),
                             VdcActionType.UpdateVm,
                             new UpdateParametersProvider()));
    }

    private void validateParameters(VM incoming) {
        if (incoming.isSetDomain() && !incoming.getDomain().isSetName()) {
            throw new WebFaultException(null,
                    localize(Messages.INCOMPLETE_PARAMS_REASON),
                    localize(Messages.INCOMPLETE_PARAMS_CONDITIONAL, "Domain", "Domain name"),
                    Response.Status.BAD_REQUEST);
        }
    }

    private String getHostId(String hostName) {
        return getEntity(VdsStatic.class,
                VdcQueryType.GetVdsStaticByName,
                new NameQueryParameters(hostName),
                "Hosts: name=" + hostName).getId().toString();
    }

    protected Guid lookupClusterId(VM vm) {
        return vm.getCluster().isSetId() ? asGuid(vm.getCluster().getId())
                : getEntity(VDSGroup.class,
                        VdcQueryType.GetVdsGroupByName,
                                                       new NameQueryParameters(vm.getCluster().getName()),
                        "Cluster: name=" + vm.getCluster().getName()).getId();
    }

    @Override
    public DevicesResource<CdRom, CdRoms> getCdRomsResource() {
        return inject(new BackendCdRomsResource(guid,
                                                VdcQueryType.GetVmByVmId,
                                                new IdQueryParameters(guid)));
    }

    @Override
    @SingleEntityResource
    public WatchdogsResource getWatchdogsResource() {
        return inject(new BackendWatchdogsResource(guid,
                                                VdcQueryType.GetWatchdog,
                                                new IdQueryParameters(guid)));
    }

    @Override
    public VmDisksResource getDisksResource() {
        return inject(new BackendVmDisksResource(guid,
                                               VdcQueryType.GetAllDisksByVmId,
                                               new IdQueryParameters(guid)));
    }

    @Override
    public VmNicsResource getNicsResource() {
        return inject(new BackendVmNicsResource(guid));
    }

    @Override
    public SnapshotsResource getSnapshotsResource() {
        return inject(new BackendSnapshotsResource(guid));
    }

    @Override
    public AssignedTagsResource getTagsResource() {
        return inject(new BackendVmTagsResource(id));
    }

    @Override
    public VmApplicationsResource getApplicationsResource() {
        return inject(new BackendVmApplicationsResource(guid));
    }

    @Override
    public AssignedPermissionsResource getPermissionsResource() {
        return inject(new BackendAssignedPermissionsResource(guid,
                                                             VdcQueryType.GetPermissionsForObject,
                                                             new GetPermissionsForObjectParameters(guid),
                                                             VM.class,
                                                             VdcObjectType.VM));
    }

    @Override
    public CreationResource getCreationSubresource(String ids) {
        return inject(new BackendCreationResource(ids));
    }

    @Override
    public ActionResource getActionSubresource(String action, String ids) {
        return inject(new BackendActionResource(action, ids));
    }

    @Override
    public StatisticsResource getStatisticsResource() {
        EntityIdResolver<Guid> resolver = new QueryIdResolver<Guid>(VdcQueryType.GetVmByVmId, IdQueryParameters.class);
        VmStatisticalQuery query = new VmStatisticalQuery(resolver, newModel(id));
        return inject(new BackendStatisticsResource<VM, org.ovirt.engine.core.common.businessentities.VM>(entityType, guid, query));
    }

    @Override
    public Response migrate(Action action) {
        boolean forceMigration = action.isSetForce() ? action.isForce() : false;

        if (!action.isSetHost()) {
            return doAction(VdcActionType.MigrateVm,
                    new MigrateVmParameters(forceMigration, guid, getTargetClusterId(action)),
                    action);
        } else {
            return doAction(VdcActionType.MigrateVmToServer,
                        new MigrateVmToServerParameters(forceMigration, guid, getHostId(action), getTargetClusterId(action)),
                        action);
        }
    }

    private Guid getTargetClusterId(Action action) {
        if (action.isSetCluster() && action.getCluster().isSetId()) {
            return asGuid(action.getCluster().getId());
        }

        // means use the cluster of the provided host
        return null;
    }

    @Override
    public Response shutdown(Action action) {
        // REVISIT add waitBeforeShutdown Action paramater
        // to api schema before next sub-milestone
        return doAction(VdcActionType.ShutdownVm,
                        new ShutdownVmParameters(guid, true),
                        action);
    }

    @Override
    public Response reboot(Action action) {
        return doAction(VdcActionType.RebootVm,
                        new VmOperationParameterBase(guid),
                        action);
    }

    @Override
    public Response undoSnapshot(Action action) {
        RestoreAllSnapshotsParameters restoreParams = new RestoreAllSnapshotsParameters(guid, SnapshotActionEnum.UNDO);
        Response response = doAction(VdcActionType.RestoreAllSnapshots,
                restoreParams,
                action);
        return response;
    }

    @Override
    public Response cloneVm(Action action) {
        validateParameters(action, "vm.name");

        org.ovirt.engine.core.common.businessentities.VM vm = getEntity(
                org.ovirt.engine.core.common.businessentities.VM.class,
                VdcQueryType.GetVmByVmId,
                new IdQueryParameters(guid), "VM: id=" + guid);
                CloneVmParameters cloneVmParameters = new CloneVmParameters(vm, action.getVm().getName());
        cloneVmParameters.setMakeCreatorExplicitOwner(isFiltered());
        Response response = doAction(VdcActionType.CloneVm,
                cloneVmParameters,
                action);

        return response;
    }

    @Override
    public Response commitSnapshot(Action action) {
        RestoreAllSnapshotsParameters restoreParams = new RestoreAllSnapshotsParameters(guid, SnapshotActionEnum.COMMIT);
        Response response = doAction(VdcActionType.RestoreAllSnapshots,
                restoreParams,
                action);
        return response;
    }

    @Override
    public Response previewSnapshot(Action action) {
        validateParameters(action, "snapshot.id");
        TryBackToAllSnapshotsOfVmParameters tryBackParams =
                new TryBackToAllSnapshotsOfVmParameters(guid, asGuid(action.getSnapshot().getId()));
        if (action.isSetRestoreMemory()) {
            tryBackParams.setRestoreMemory(action.isRestoreMemory());
        }
        if (action.isSetDisks()) {
            tryBackParams.setDisks(getParent().mapDisks(action.getDisks()));
        }
        Response response = doAction(VdcActionType.TryBackToAllSnapshotsOfVm,
                tryBackParams,
                action);
        return response;
    }

    @Override
    public Response start(Action action) {
        RunVmParams params;
        VdcActionType actionType;
        if (action.isSetVm()) {
            VM vm = action.getVm();
            validateEnums(VM.class, vm);
            actionType = VdcActionType.RunVmOnce;
            params = createRunVmOnceParams(vm);
        } else {
            actionType = VdcActionType.RunVm;
            params = new RunVmParams(guid);
        }
        if (action.isSetPause() && action.isPause()) {
            params.setRunAndPause(true);
        }
        return doAction(actionType, params, action);
    }

    private RunVmOnceParams createRunVmOnceParams(VM vm) {
        RunVmOnceParams params = map(vm, map(map(getEntity(entityType, VdcQueryType.GetVmByVmId, new IdQueryParameters(guid), id, true), new VM()),
                new RunVmOnceParams(guid)));
        if (vm.isSetPlacementPolicy() && vm.getPlacementPolicy().isSetHost()) {
            validateParameters(vm.getPlacementPolicy(), "host.id|name");
            params.setDestinationVdsId(getHostId(vm.getPlacementPolicy().getHost()));
        }
        if (vm.isSetInitialization() && vm.getInitialization().isSetCloudInit()) {
            CloudInit cloudInit = vm.getInitialization().getCloudInit();
            // currently only 'root' user is supported, alert the user if other user sent
            if (cloudInit.isSetAuthorizedKeys()) {
                for (AuthorizedKey authKey : cloudInit.getAuthorizedKeys().getAuthorizedKeys()) {
                    if (!"root".equals(authKey.getUser().getUserName())) {
                        throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                                .entity("Currently only the user 'root' is supported for authorized keys")
                                .build());
                    }
                }
            }
            params.setInitializationType(InitializationType.CloudInit);
            ((RunVmOnceParams) params).setVmInit(
                    getMapper(CloudInit.class, VmInit.class)
                    .map(cloudInit, null));
        }

        return params;
    }

    @Override
    public Response stop(Action action) {
        return doAction(VdcActionType.StopVm,
                        new StopVmParameters(guid, StopVmTypeEnum.NORMAL),
                        action);
    }

    @Override
    public Response suspend(Action action) {
        return doAction(VdcActionType.HibernateVm,
                        new VmOperationParameterBase(guid),
                        action);
    }

    @Override
    public Response detach(Action action) {
        return doAction(VdcActionType.RemoveVmFromPool,
                        new RemoveVmFromPoolParameters(guid),
                        action);
    }

    @Override
    public Response export(Action action) {
        validateParameters(action, "storageDomain.id|name");

        MoveVmParameters params = new MoveVmParameters(guid, getStorageDomainId(action));

        if (action.isSetExclusive() && action.isExclusive()) {
            params.setForceOverride(true);
        }

        if (action.isSetDiscardSnapshots() && action.isDiscardSnapshots()) {
            params.setCopyCollapse(true);
        }

        return doAction(VdcActionType.ExportVm, params, action);
    }

    @Override
    public Response move(Action action) {
        validateParameters(action, "storageDomain.id|name");

        return doAction(VdcActionType.MoveVm,
                        new MoveVmParameters(guid, getStorageDomainId(action)),
                        action);
    }

    @Override
    public Response ticket(Action action) {
        final Response response = doAction(VdcActionType.SetVmTicket,
                new SetVmTicketParameters(guid,
                        getTicketValue(action),
                        getTicketExpiry(action),
                        deriveGraphicsType()),
                action);

        final Action actionResponse = (Action) response.getEntity();

        if (CreationStatus.FAILED.value().equals(actionResponse.getStatus().getState())) {
            actionResponse.getTicket().setValue(null);
            actionResponse.getTicket().setExpiry(null);
        }

        return response;
    }

    private GraphicsType deriveGraphicsType() {
        org.ovirt.engine.core.common.businessentities.VM vm = getEntity(org.ovirt.engine.core.common.businessentities.VM.class,
                VdcQueryType.GetVmByVmId, new IdQueryParameters(guid), "GetVmByVmId");

        return (vm == null)
                ? null
                : VmMapper.deriveGraphicsType(vm.getGraphicsInfos());
    }

    protected String getTicketValue(Action action) {
        if (!ensureTicket(action).isSetValue()) {
            action.getTicket().setValue(generateOTP());
        }
        return action.getTicket().getValue();
    }


    protected int getTicketExpiry(Action action) {
        if (!ensureTicket(action).isSetExpiry()) {
            action.getTicket().setExpiry(DEFAULT_TICKET_EXPIRY);
        }
        return action.getTicket().getExpiry().intValue();
    }

    protected Ticket ensureTicket(Action action) {
        if (!action.isSetTicket()) {
            action.setTicket(new Ticket());
        }
        return action.getTicket();
    }

    @Override
    public Response logon(Action action) {
        final Response response = doAction(VdcActionType.VmLogon,
                new VmOperationParameterBase(guid),
                action);
        return response;
    }

    protected RunVmOnceParams map(VM vm, RunVmOnceParams params) {
        return getMapper(VM.class, RunVmOnceParams.class).map(vm, params);
    }

    @Override
    protected VM doPopulate(VM model, org.ovirt.engine.core.common.businessentities.VM entity) {
        parent.setConsoleDevice(model);
        parent.setVirtioScsiController(model);
        parent.setSoundcard(model);
        parent.setVmOvfConfiguration(model, entity);
        parent.setRngDevice(model);
        return model;
    }

    @Override
    protected VM deprecatedPopulate(VM model, org.ovirt.engine.core.common.businessentities.VM entity) {
        Set<String> details = DetailHelper.getDetails(httpHeaders, uriInfo);
        parent.addInlineDetails(details, model);
        if (details.contains("statistics")) {
            addStatistics(model, entity, uriInfo);
        }
        parent.setPayload(model);
        parent.setBallooning(model);
        parent.setCertificateInfo(model);
        return model;
    }

    private void addStatistics(VM model, org.ovirt.engine.core.common.businessentities.VM entity, UriInfo ui) {
        model.setStatistics(new Statistics());
        VmStatisticalQuery query = new VmStatisticalQuery(newModel(model.getId()));
        List<Statistic> statistics = query.getStatistics(entity);
        for (Statistic statistic : statistics) {
            LinkHelper.addLinks(ui, statistic, query.getParentType());
        }
        model.getStatistics().getStatistics().addAll(statistics);
    }

    protected class UpdateParametersProvider implements
            ParametersProvider<VM, org.ovirt.engine.core.common.businessentities.VM> {
        @Override
        public VdcActionParametersBase getParameters(VM incoming,
                org.ovirt.engine.core.common.businessentities.VM entity) {
            VmStatic updated = getMapper(modelType, VmStatic.class).map(incoming,
                    entity.getStaticData());

            updated.setUsbPolicy(VmMapper.getUsbPolicyOnUpdate(incoming.getUsb(), entity.getUsbPolicy(),
                    lookupCluster(updated.getVdsGroupId()).getCompatibilityVersion()));

            VmManagementParametersBase params = new VmManagementParametersBase(updated);

            params.setApplyChangesLater(isNextRunRequested());

            if (incoming.isSetPayloads()) {
                if (incoming.isSetPayloads() && incoming.getPayloads().isSetPayload()) {
                    params.setVmPayload(parent.getPayload(incoming));
                } else {
                    params.setClearPayload(true);
                }
            }
            if (incoming.isSetMemoryPolicy() && incoming.getMemoryPolicy().isSetBallooning()) {
               params.setBalloonEnabled(incoming.getMemoryPolicy().isBallooning());
            }
            if (incoming.isSetConsole() && incoming.getConsole().isSetEnabled()) {
                params.setConsoleEnabled(incoming.getConsole().isEnabled());
            }
            if (incoming.isSetVirtioScsi()) {
                params.setVirtioScsiEnabled(incoming.getVirtioScsi().isEnabled());
            }
            if (incoming.isSetSoundcardEnabled()) {
                params.setSoundDeviceEnabled(incoming.isSoundcardEnabled());
            }
            if (incoming.isSetRngDevice()) {
                params.setUpdateRngDevice(true);
                params.setRngDevice(RngDeviceMapper.map(incoming.getRngDevice(), null));
            }

            DisplayHelper.setGraphicsToParams(incoming.getDisplay(), params);

            if (incoming.isSetInstanceType() && (incoming.getInstanceType().isSetId() || incoming.getInstanceType().isSetName())) {
                updated.setInstanceTypeId(lookupInstanceTypeId(incoming.getInstanceType()));
            } else if (incoming.isSetInstanceType()) {
                // this means that the instance type should be unset
                updated.setInstanceTypeId(null);
            }
            return params;
        }
    }

    private VDSGroup lookupCluster(Guid id) {
        return getEntity(VDSGroup.class, VdcQueryType.GetVdsGroupByVdsGroupId, new IdQueryParameters(id), "GetVdsGroupByVdsGroupId");
    }

    private Guid lookupInstanceTypeId(Template template) {
        return template.isSetId() ? asGuid(template.getId()) : lookupInstanceTypeByName(template).getId();
    }

    private VmTemplate lookupInstanceTypeByName(Template template) {
        return getEntity(VmTemplate.class, VdcQueryType.GetVmTemplate, new GetVmTemplateParameters(template.getName()), "GetVmTemplate");
    }

    @Override
    public Response cancelMigration(Action action) {
        return doAction(VdcActionType.CancelMigrateVm,
                new VmOperationParameterBase(guid), action);
    }

    public BackendVmsResource getParent() {
        return parent;
    }

    public void setCertificateInfo(VM model) {
        VdcQueryReturnValue result =
            runQuery(VdcQueryType.GetVdsCertificateSubjectByVmId,
                    new IdQueryParameters(asGuid(model.getId())));

        if (result != null && result.getSucceeded() && result.getReturnValue() != null) {
            if (!model.isSetDisplay()) {
                model.setDisplay(new Display());
            }
            model.getDisplay().setCertificate(new Certificate());
            model.getDisplay().getCertificate().setSubject(result.getReturnValue().toString());
        }
    }

    @Override
    public VmReportedDevicesResource getVmReportedDevicesResource() {
        return inject(new BackendVmReportedDevicesResource(guid));
    }

    @Override
    public VmSessionsResource getVmSessionsResource() {
        return inject(new BackendVmSessionsResource(guid));
    }

    @Override
    public Response maintenance(Action action) {
        validateParameters(action, "maintenanceEnabled");

        org.ovirt.engine.core.common.businessentities.VM entity =
                getEntity(org.ovirt.engine.core.common.businessentities.VM.class,
                          VdcQueryType.GetVmByVmId,
                          new IdQueryParameters(guid),
                          id);
        if (!entity.isHostedEngine()) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Moving to maintenance mode is currently only available for the VM containing the hosted engine.")
                    .build());
        }

        return doAction(VdcActionType.SetHaMaintenance,
                        new SetHaMaintenanceParameters(entity.getRunOnVds(),
                                HaMaintenanceMode.GLOBAL, action.isMaintenanceEnabled()),
                        action);
    }

    @Override
    public VmNumaNodesResource getVirtualNumaNodesResource() {
        return inject(new BackendVmNumaNodesResource(guid));
    }
}

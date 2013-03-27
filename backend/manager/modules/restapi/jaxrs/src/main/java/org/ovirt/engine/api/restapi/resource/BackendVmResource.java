package org.ovirt.engine.api.restapi.resource;

import static org.ovirt.engine.api.restapi.resource.BackendVmsResource.SUB_COLLECTIONS;
import static org.ovirt.engine.core.utils.Ticketing.GenerateOTP;

import java.util.List;
import java.util.Set;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.ovirt.engine.api.common.util.DetailHelper;
import org.ovirt.engine.api.common.util.DetailHelper.Detail;
import org.ovirt.engine.api.common.util.LinkHelper;
import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.CdRom;
import org.ovirt.engine.api.model.CdRoms;
import org.ovirt.engine.api.model.Certificate;
import org.ovirt.engine.api.model.CreationStatus;
import org.ovirt.engine.api.model.Display;
import org.ovirt.engine.api.model.Statistic;
import org.ovirt.engine.api.model.Statistics;
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
import org.ovirt.engine.api.resource.VmReportedDevicesResource;
import org.ovirt.engine.api.resource.VmResource;
import org.ovirt.engine.api.restapi.types.VmMapper;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ChangeVMClusterParameters;
import org.ovirt.engine.core.common.action.HibernateVmParameters;
import org.ovirt.engine.core.common.action.MigrateVmParameters;
import org.ovirt.engine.core.common.action.MigrateVmToServerParameters;
import org.ovirt.engine.core.common.action.MoveVmParameters;
import org.ovirt.engine.core.common.action.RemoveVmFromPoolParameters;
import org.ovirt.engine.core.common.action.RunVmOnceParams;
import org.ovirt.engine.core.common.action.SetVmTicketParameters;
import org.ovirt.engine.core.common.action.ShutdownVmParameters;
import org.ovirt.engine.core.common.action.StopVmParameters;
import org.ovirt.engine.core.common.action.StopVmTypeEnum;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VmManagementParametersBase;
import org.ovirt.engine.core.common.action.VmOperationParameterBase;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.GetAllDisksByVmIdParameters;
import org.ovirt.engine.core.common.queries.GetPermissionsForObjectParameters;
import org.ovirt.engine.core.common.queries.GetVdsGroupByVdsGroupIdParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendVmResource extends
        AbstractBackendActionableResource<VM, org.ovirt.engine.core.common.businessentities.VM> implements
        VmResource {

    private static final long DEFAULT_TICKET_EXPIRY = 120 * 60; // 2 hours
    private BackendVmsResource parent;

    public BackendVmResource(String id, BackendVmsResource parent) {
        super(id, VM.class, org.ovirt.engine.core.common.businessentities.VM.class, SUB_COLLECTIONS);
        this.parent = parent;
    }

    @Override
    public VM get() {
        VM vm = performGet(VdcQueryType.GetVmByVmId, new IdQueryParameters(guid));
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

    private String getHostId(String hostName) {
        return getEntity(VDS.class, SearchType.VDS, "Hosts: name=" + hostName).getId().toString();
    }

    protected Guid lookupClusterId(VM vm) {
        return vm.getCluster().isSetId() ? asGuid(vm.getCluster().getId())
                                           :
                                           getEntity(VDSGroup.class,
                                                     SearchType.Cluster,
                                                     "Cluster: name=" + vm.getCluster().getName()).getId();
    }

    @Override
    public DevicesResource<CdRom, CdRoms> getCdRomsResource() {
        return inject(new BackendCdRomsResource(guid,
                                                VdcQueryType.GetVmByVmId,
                                                new IdQueryParameters(guid)));
    }

    @Override
    public VmDisksResource getDisksResource() {
        return inject(new BackendVmDisksResource(guid,
                                               VdcQueryType.GetAllDisksByVmId,
                                               new GetAllDisksByVmIdParameters(guid)));
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
                    new MigrateVmParameters(forceMigration, guid),
                    action);
        } else {
            return doAction(VdcActionType.MigrateVmToServer,
                        new MigrateVmToServerParameters(forceMigration, guid, getHostId(action)),
                        action);
        }
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
    public Response start(Action action) {
        RunVmOnceParams params =
                map(map(getEntity(entityType, VdcQueryType.GetVmByVmId, new IdQueryParameters(guid), id, true),
                        new VM()),
                        new RunVmOnceParams(guid));
        if (action.isSetVm()) {
            validateEnums(VM.class, action.getVm());
            VM vm = action.getVm();
            params = map(vm, params);
            if (vm.isSetPlacementPolicy() && vm.getPlacementPolicy().isSetHost()) {
                validateParameters(vm.getPlacementPolicy(), "host.id|name");
                params.setDestinationVdsId(getHostId(vm.getPlacementPolicy().getHost()));
            }
        }
        if (action.isSetPause() && action.isPause()) {
            params.setRunAndPause(true);
        }
        return doAction(VdcActionType.RunVmOnce, setReinitializeSysPrep(params), action);
    }

    private VdcActionParametersBase setReinitializeSysPrep(RunVmOnceParams params) {
        //REVISE when BE supports default val. for RunVmOnceParams.privateReinitialize
        org.ovirt.engine.core.common.businessentities.VM vm = getEntity(org.ovirt.engine.core.common.businessentities.VM.class,
                                                                        VdcQueryType.GetVmByVmId,
                                                                        new IdQueryParameters(guid),
                                                                        "VM");
        if(vm.getVmOs().isWindows() && vm.isFirstRun()) {
            params.setReinitialize(true);
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
                        new HibernateVmParameters(guid),
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
                                          getTicketExpiry(action)),
                action);

        final Action actionResponse = (Action) response.getEntity();

        if (CreationStatus.FAILED.value().equals(actionResponse.getStatus().getState())) {
            actionResponse.getTicket().setValue(null);
            actionResponse.getTicket().setExpiry(null);
        }

        return response;
    }

    protected String getTicketValue(Action action) {
        if (!ensureTicket(action).isSetValue()) {
            action.getTicket().setValue(GenerateOTP());
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

    protected RunVmOnceParams map(VM vm, RunVmOnceParams params) {
        return getMapper(VM.class, RunVmOnceParams.class).map(vm, params);
    }

    @Override
    protected VM doPopulate(VM model, org.ovirt.engine.core.common.businessentities.VM entity) {
        return model;
    }

    @Override
    protected VM deprecatedPopulate(VM model, org.ovirt.engine.core.common.businessentities.VM entity) {
        Set<Detail> details = DetailHelper.getDetails(getHttpHeaders());
        parent.addInlineDetails(details, model);
        addStatistics(model, entity, uriInfo, httpHeaders);
        parent.setPayload(model);
        parent.setBallooning(model);
        parent.setCertificateInfo(model);
        return model;
    }

    VM addStatistics(VM model, org.ovirt.engine.core.common.businessentities.VM entity, UriInfo ui, HttpHeaders httpHeaders) {
        if (DetailHelper.include(httpHeaders, "statistics")) {
            model.setStatistics(new Statistics());
            VmStatisticalQuery query = new VmStatisticalQuery(newModel(model.getId()));
            List<Statistic> statistics = query.getStatistics(entity);
            for (Statistic statistic : statistics) {
                LinkHelper.addLinks(ui, statistic, query.getParentType());
            }
            model.getStatistics().getStatistics().addAll(statistics);
        }
        return model;
    }

    protected class UpdateParametersProvider implements
            ParametersProvider<VM, org.ovirt.engine.core.common.businessentities.VM> {
        @Override
        public VdcActionParametersBase getParameters(VM incoming,
                org.ovirt.engine.core.common.businessentities.VM entity) {
            VmStatic updated = getMapper(modelType, VmStatic.class).map(incoming,
                    entity.getStaticData());

            updated.setUsbPolicy(VmMapper.getUsbPolicyOnUpdate(incoming.getUsb(), entity.getUsbPolicy(),
                    lookupCluster(updated.getVdsGroupId())));

            VmManagementParametersBase params = new VmManagementParametersBase(updated);

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
            return params;
        }
    }

    private VDSGroup lookupCluster(Guid id) {
        return getEntity(VDSGroup.class, VdcQueryType.GetVdsGroupByVdsGroupId, new GetVdsGroupByVdsGroupIdParameters(id), "GetVdsGroupByVdsGroupId");
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
}

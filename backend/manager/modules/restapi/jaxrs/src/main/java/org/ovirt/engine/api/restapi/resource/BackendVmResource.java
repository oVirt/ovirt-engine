/*
Copyright (c) 2015 Red Hat, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.ovirt.engine.api.restapi.resource;

import static org.ovirt.engine.api.restapi.resource.BackendVmsResource.SUB_COLLECTIONS;
import static org.ovirt.engine.core.utils.Ticketing.generateOTP;

import java.util.List;
import java.util.Set;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.common.util.DetailHelper;
import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.Certificate;
import org.ovirt.engine.api.model.CreationStatus;
import org.ovirt.engine.api.model.Display;
import org.ovirt.engine.api.model.Fault;
import org.ovirt.engine.api.model.Statistic;
import org.ovirt.engine.api.model.Statistics;
import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.api.model.Ticket;
import org.ovirt.engine.api.model.Vm;
import org.ovirt.engine.api.resource.ActionResource;
import org.ovirt.engine.api.resource.AssignedAffinityLabelsResource;
import org.ovirt.engine.api.resource.AssignedPermissionsResource;
import org.ovirt.engine.api.resource.AssignedTagsResource;
import org.ovirt.engine.api.resource.CreationResource;
import org.ovirt.engine.api.resource.DiskAttachmentsResource;
import org.ovirt.engine.api.resource.GraphicsConsolesResource;
import org.ovirt.engine.api.resource.SnapshotsResource;
import org.ovirt.engine.api.resource.StatisticsResource;
import org.ovirt.engine.api.resource.VmApplicationsResource;
import org.ovirt.engine.api.resource.VmCdromsResource;
import org.ovirt.engine.api.resource.VmDisksResource;
import org.ovirt.engine.api.resource.VmHostDevicesResource;
import org.ovirt.engine.api.resource.VmNicsResource;
import org.ovirt.engine.api.resource.VmNumaNodesResource;
import org.ovirt.engine.api.resource.VmReportedDevicesResource;
import org.ovirt.engine.api.resource.VmResource;
import org.ovirt.engine.api.resource.VmSessionsResource;
import org.ovirt.engine.api.resource.VmWatchdogsResource;
import org.ovirt.engine.api.resource.externalhostproviders.KatelloErrataResource;
import org.ovirt.engine.api.restapi.logging.Messages;
import org.ovirt.engine.api.restapi.resource.externalhostproviders.BackendVmKatelloErrataResource;
import org.ovirt.engine.api.restapi.types.InitializationMapper;
import org.ovirt.engine.api.restapi.types.RngDeviceMapper;
import org.ovirt.engine.api.restapi.types.VmMapper;
import org.ovirt.engine.api.restapi.util.DisplayHelper;
import org.ovirt.engine.api.restapi.util.IconHelper;
import org.ovirt.engine.api.restapi.util.LinkHelper;
import org.ovirt.engine.api.restapi.util.ParametersHelper;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ChangeVMClusterParameters;
import org.ovirt.engine.core.common.action.CloneVmParameters;
import org.ovirt.engine.core.common.action.MigrateVmParameters;
import org.ovirt.engine.core.common.action.MigrateVmToServerParameters;
import org.ovirt.engine.core.common.action.MoveOrCopyParameters;
import org.ovirt.engine.core.common.action.RemoveVmFromPoolParameters;
import org.ovirt.engine.core.common.action.RemoveVmParameters;
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
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.businessentities.HaMaintenanceMode;
import org.ovirt.engine.core.common.businessentities.InitializationType;
import org.ovirt.engine.core.common.businessentities.SnapshotActionEnum;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.queries.GetPermissionsForObjectParameters;
import org.ovirt.engine.core.common.queries.GetVmTemplateParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.NameQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendVmResource
        extends AbstractBackendActionableResource<Vm, org.ovirt.engine.core.common.businessentities.VM>
        implements VmResource {

    private static final long DEFAULT_TICKET_EXPIRY = 120 * 60; // 2 hours
    private BackendVmsResource parent;

    public static final String DETACH_ONLY = "detach_only";
    public static final String FORCE = "force";
    public static final String NEXT_RUN = "next_run";

    public BackendVmResource(String id, BackendVmsResource parent) {
        super(id, Vm.class, org.ovirt.engine.core.common.businessentities.VM.class, SUB_COLLECTIONS);
        this.parent = parent;
    }

    private boolean isNextRunRequested() {
        return ParametersHelper.getBooleanParameter(httpHeaders, uriInfo, NEXT_RUN, true, false);
    }

    @Override
    public Vm get() {
        Vm vm;
        if (isNextRunRequested()) {
            org.ovirt.engine.core.common.businessentities.VM entity =
                    getEntity(org.ovirt.engine.core.common.businessentities.VM.class, VdcQueryType.GetVmNextRunConfiguration,
                            new IdQueryParameters(guid), id, true);
            vm = addLinks(populate(VmMapper.map(entity, null, false), entity));

        } else {
            vm = performGet(VdcQueryType.GetVmByVmId, new IdQueryParameters(guid));
        }

        if (vm != null) {
            DisplayHelper.adjustDisplayData(this, vm, isNextRunRequested());
            removeRestrictedInfo(vm);
        }

        return vm;
    }

    private void removeRestrictedInfo(Vm vm) {
        // Filtered users are not allowed to view host related information
        if (isFiltered()) {
            vm.setHost(null);
            vm.setPlacementPolicy(null);
        }
    }

    @Override
    public Vm update(Vm incoming) {
        validateParameters(incoming);
        if (incoming.isSetCluster() && (incoming.getCluster().isSetId() || incoming.getCluster().isSetName())) {
            Guid clusterId = lookupClusterId(incoming);
            if(!clusterId.toString().equals(get().getCluster().getId())){
                performAction(VdcActionType.ChangeVMCluster,
                              new ChangeVMClusterParameters(clusterId, guid, null)); // TODO: change 'null' to 'incoming.getVmCompa...' when REST support is added
            }
        }
        if (!isFiltered()) {
            if (incoming.isSetPlacementPolicy()) {
                parent.validateAndUpdateHostsInPlacementPolicy(incoming.getPlacementPolicy());
            }
        } else {
            incoming.setPlacementPolicy(null);
        }

        Vm vm = performUpdate(
            incoming,
            new QueryIdResolver<>(VdcQueryType.GetVmByVmId, IdQueryParameters.class),
            VdcActionType.UpdateVm,
            new UpdateParametersProvider()
        );

        if (vm != null) {
            DisplayHelper.adjustDisplayData(this, vm, false);
            removeRestrictedInfo(vm);
        }

        return vm;
    }

    @Override
    public Response remove() {
        get();
        boolean force = ParametersHelper.getBooleanParameter(httpHeaders, uriInfo, FORCE, true, false);
        RemoveVmParameters params = new RemoveVmParameters(guid, force);
        // If detach only is set we do not remove the VM disks
        boolean detachOnly = ParametersHelper.getBooleanParameter(httpHeaders, uriInfo, DETACH_ONLY, true, false);
        if (detachOnly) {
            params.setRemoveDisks(false);
        }
        return performAction(VdcActionType.RemoveVm, params);
    }

    private void validateParameters(Vm incoming) {
        if (incoming.isSetDomain() && !incoming.getDomain().isSetName()) {
            throw new WebFaultException(null,
                    localize(Messages.INCOMPLETE_PARAMS_REASON),
                    localize(Messages.INCOMPLETE_PARAMS_CONDITIONAL, "Domain", "Domain name"),
                    Response.Status.BAD_REQUEST);
        }
        if (!IconHelper.validateIconParameters(incoming)) {
            throw new BaseBackendResource.WebFaultException(null,
                    localize(Messages.INVALID_ICON_PARAMETERS),
                    Response.Status.BAD_REQUEST);
        }
    }

    protected Guid lookupClusterId(Vm vm) {
        return vm.getCluster().isSetId() ? asGuid(vm.getCluster().getId())
                : getEntity(Cluster.class,
                        VdcQueryType.GetClusterByName,
                                                       new NameQueryParameters(vm.getCluster().getName()),
                        "Cluster: name=" + vm.getCluster().getName()).getId();
    }

    @Override
    public VmCdromsResource getCdromsResource() {
        return inject(new BackendVmCdromsResource(guid));
    }

    @Override
    public VmWatchdogsResource getWatchdogsResource() {
        return inject(new BackendVmWatchdogsResource(guid));
    }

    public VmDisksResource getDisksResource() {
        return inject(new BackendVmDisksResource(guid));
    }

    @Override
    public DiskAttachmentsResource getDiskAttachmentsResource() {
        return inject(new BackendDiskAttachmentsResource(guid));
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
                                                             Vm.class,
                                                             VdcObjectType.VM));
    }

    @Override
    public CreationResource getCreationResource(String ids) {
        return inject(new BackendCreationResource(ids));
    }

    @Override
    public ActionResource getActionResource(String action, String ids) {
        return inject(new BackendActionResource(action, ids));
    }

    @Override
    public StatisticsResource getStatisticsResource() {
        EntityIdResolver<Guid> resolver = new QueryIdResolver<>(VdcQueryType.GetVmByVmId, IdQueryParameters.class);
        VmStatisticalQuery query = new VmStatisticalQuery(resolver, newModel(id));
        return inject(new BackendStatisticsResource<>(entityType, guid, query));
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
    public Response doClone(Action action) {
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
    public Response reorderMacAddresses(Action action) {
        getEntity(org.ovirt.engine.core.common.businessentities.VM.class,
                VdcQueryType.GetVmByVmId,
                new IdQueryParameters(guid),
                "VM: id=" + guid,
                true);

        final VmOperationParameterBase params = new VmOperationParameterBase(guid);
        final Response response = doAction(
                VdcActionType.ReorderVmNics,
                params,
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
            Vm vm = action.getVm();
            actionType = VdcActionType.RunVmOnce;
            params = createRunVmOnceParams(vm);
        } else {
            actionType = VdcActionType.RunVm;
            params = new RunVmParams(guid);
        }
        if (action.isSetPause() && action.isPause()) {
            params.setRunAndPause(true);
        }

        boolean useSysprep = action.isSetUseSysprep() && action.isUseSysprep();
        boolean useCloudInit = action.isSetUseCloudInit() && action.isUseCloudInit();
        if (useSysprep && useCloudInit) {
            Fault fault = new Fault();
            fault.setReason(localize(Messages.CANT_USE_SYSPREP_AND_CLOUD_INIT_SIMULTANEOUSLY));
            return Response.status(Response.Status.CONFLICT).entity(fault).build();
        }
        if (useSysprep) {
            params.setInitializationType(InitializationType.Sysprep);
        }
        else if (useCloudInit) {
            params.setInitializationType(InitializationType.CloudInit);
        }
        else {
            params.setInitializationType(InitializationType.None);
        }
        return doAction(actionType, params, action);
    }

    private RunVmOnceParams createRunVmOnceParams(Vm vm) {
        VM entity = getEntity(entityType, VdcQueryType.GetVmByVmId, new IdQueryParameters(guid), id, true);
        RunVmOnceParams params = map(vm, map(map(entity, new Vm()),
                new RunVmOnceParams(guid)));
        if (vm.isSetPlacementPolicy()) {
            Set<Guid> hostsGuidsSet = parent.validateAndUpdateHostsInPlacementPolicy(vm.getPlacementPolicy());
            if (hostsGuidsSet.size() > 0) {
                // take the arbitrary first host for run destination
                params.setDestinationVdsId(hostsGuidsSet.iterator().next());
            }
        }
        if (vm.isSetInitialization()) {
            if (vm.getInitialization().isSetCloudInit()) {
                params.setInitializationType(InitializationType.CloudInit);
            }
            params.setVmInit(InitializationMapper.map(vm.getInitialization(), entity.getVmInit()));
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
                        new RemoveVmFromPoolParameters(guid, false, true),
                        action);
    }

    @Override
    public Response export(Action action) {
        validateParameters(action, "storageDomain.id|name");

        MoveOrCopyParameters params = new MoveOrCopyParameters(guid, getStorageDomainId(action));

        if (action.isSetExclusive() && action.isExclusive()) {
            params.setForceOverride(true);
        }

        if (action.isSetDiscardSnapshots() && action.isDiscardSnapshots()) {
            params.setCopyCollapse(true);
        }

        return doAction(VdcActionType.ExportVm, params, action);
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

        if (CreationStatus.FAILED.value().equals(actionResponse.getStatus())) {
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

    @Override
    public Response freezeFilesystems(Action action) {
        final Response response = doAction(VdcActionType.FreezeVm,
                new VmOperationParameterBase(guid),
                action);
        return response;
    }

    @Override
    public Response thawFilesystems(Action action) {
        final Response response = doAction(VdcActionType.ThawVm,
                new VmOperationParameterBase(guid),
                action);
        return response;
    }

    protected RunVmOnceParams map(Vm vm, RunVmOnceParams params) {
        return getMapper(Vm.class, RunVmOnceParams.class).map(vm, params);
    }

    @Override
    protected Vm doPopulate(Vm model, org.ovirt.engine.core.common.businessentities.VM entity) {
        BackendVmDeviceHelper.setConsoleDevice(this, model);
        BackendVmDeviceHelper.setVirtioScsiController(this, model);
        BackendVmDeviceHelper.setSoundcard(this, model);
        BackendVmDeviceHelper.setRngDevice(this, model);
        parent.setVmOvfConfiguration(model, entity);
        return model;
    }

    @Override
    protected Vm deprecatedPopulate(Vm model, org.ovirt.engine.core.common.businessentities.VM entity) {
        Set<String> details = DetailHelper.getDetails(httpHeaders, uriInfo);
        if (details.contains("statistics")) {
            addStatistics(model, entity);
        }
        BackendVmDeviceHelper.setPayload(this, model);
        BackendVmDeviceHelper.setCertificateInfo(this, model);
        MemoryPolicyHelper.setupMemoryBalloon(model, this);
        return model;
    }

    private void addStatistics(Vm model, org.ovirt.engine.core.common.businessentities.VM entity) {
        model.setStatistics(new Statistics());
        VmStatisticalQuery query = new VmStatisticalQuery(newModel(model.getId()));
        List<Statistic> statistics = query.getStatistics(entity);
        for (Statistic statistic : statistics) {
            LinkHelper.addLinks(statistic, query.getParentType());
        }
        model.getStatistics().getStatistics().addAll(statistics);
    }

    protected class UpdateParametersProvider implements
            ParametersProvider<Vm, org.ovirt.engine.core.common.businessentities.VM> {
        @Override
        public VdcActionParametersBase getParameters(Vm incoming,
                org.ovirt.engine.core.common.businessentities.VM entity) {
            VmStatic updated = getMapper(modelType, VmStatic.class).map(incoming,
                    entity.getStaticData());

            updated.setUsbPolicy(VmMapper.getUsbPolicyOnUpdate(incoming.getUsb(), entity.getUsbPolicy()));

            VmManagementParametersBase params = new VmManagementParametersBase(updated);

            params.setApplyChangesLater(isNextRunRequested());

            if (incoming.isSetPayloads()) {
                if (incoming.isSetPayloads() && incoming.getPayloads().isSetPayloads()) {
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
                if (incoming.getVirtioScsi().isSetEnabled()) {
                    params.setVirtioScsiEnabled(incoming.getVirtioScsi().isEnabled());
                }
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
            IconHelper.setIconToParams(incoming, params);
            return params;
        }
    }

    private Cluster lookupCluster(Guid id) {
        return getEntity(Cluster.class, VdcQueryType.GetClusterByClusterId, new IdQueryParameters(id), "GetClusterByClusterId");
    }

    private Guid lookupInstanceTypeId(Template template) {
        return template.isSetId() ? asGuid(template.getId()) : lookupInstanceTypeByName(template).getId();
    }

    private VmTemplate lookupInstanceTypeByName(Template template) {
        return getEntity(VmTemplate.class,
                VdcQueryType.GetInstanceType,
                new GetVmTemplateParameters(template.getName()),
                "GetVmTemplate");
    }

    @Override
    public Response cancelMigration(Action action) {
        return doAction(VdcActionType.CancelMigrateVm,
                new VmOperationParameterBase(guid), action);
    }

    public BackendVmsResource getParent() {
        return parent;
    }

    public void setCertificateInfo(Vm model) {
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
    public VmReportedDevicesResource getReportedDevicesResource() {
        return inject(new BackendVmReportedDevicesResource(guid));
    }

    @Override
    public VmSessionsResource getSessionsResource() {
        return inject(new BackendVmSessionsResource(guid));
    }

    @Override
    public GraphicsConsolesResource getGraphicsConsolesResource() {
        return inject(new BackendVmGraphicsConsolesResource(guid));
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
    public VmNumaNodesResource getNumaNodesResource() {
        return inject(new BackendVmNumaNodesResource(guid));
    }

    @Override
    public KatelloErrataResource getKatelloErrataResource() {
        return inject(new BackendVmKatelloErrataResource(id));
    }

    @Override
    public VmHostDevicesResource getHostDevicesResource() {
        return inject(new BackendVmHostDevicesResource(guid));
    }

    @Override
    public AssignedAffinityLabelsResource getAffinityLabelsResource() {
        return inject(new BackendAssignedAffinityLabelsResource(id, VM::new));
    }
}

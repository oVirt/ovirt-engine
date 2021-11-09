/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.resource;

import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.common.util.DetailHelper;
import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.Fault;
import org.ovirt.engine.api.model.Statistic;
import org.ovirt.engine.api.model.Statistics;
import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.api.model.Vm;
import org.ovirt.engine.api.resource.ActionResource;
import org.ovirt.engine.api.resource.AssignedAffinityLabelsResource;
import org.ovirt.engine.api.resource.AssignedPermissionsResource;
import org.ovirt.engine.api.resource.AssignedTagsResource;
import org.ovirt.engine.api.resource.CreationResource;
import org.ovirt.engine.api.resource.DiskAttachmentsResource;
import org.ovirt.engine.api.resource.SnapshotsResource;
import org.ovirt.engine.api.resource.StatisticsResource;
import org.ovirt.engine.api.resource.VmApplicationsResource;
import org.ovirt.engine.api.resource.VmBackupsResource;
import org.ovirt.engine.api.resource.VmCdromsResource;
import org.ovirt.engine.api.resource.VmCheckpointsResource;
import org.ovirt.engine.api.resource.VmDisksResource;
import org.ovirt.engine.api.resource.VmGraphicsConsolesResource;
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
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ChangeVMClusterParameters;
import org.ovirt.engine.core.common.action.CloneVmParameters;
import org.ovirt.engine.core.common.action.ExportVmToOvaParameters;
import org.ovirt.engine.core.common.action.MigrateMultipleVmsParameters;
import org.ovirt.engine.core.common.action.MigrateVmParameters;
import org.ovirt.engine.core.common.action.MigrateVmToServerParameters;
import org.ovirt.engine.core.common.action.MoveOrCopyParameters;
import org.ovirt.engine.core.common.action.RebootVmParameters;
import org.ovirt.engine.core.common.action.RemoveVmFromPoolParameters;
import org.ovirt.engine.core.common.action.RemoveVmParameters;
import org.ovirt.engine.core.common.action.RestoreAllSnapshotsParameters;
import org.ovirt.engine.core.common.action.RunVmOnceParams;
import org.ovirt.engine.core.common.action.RunVmParams;
import org.ovirt.engine.core.common.action.SetHaMaintenanceParameters;
import org.ovirt.engine.core.common.action.ShutdownVmParameters;
import org.ovirt.engine.core.common.action.StopVmParameters;
import org.ovirt.engine.core.common.action.StopVmTypeEnum;
import org.ovirt.engine.core.common.action.TryBackToAllSnapshotsOfVmParameters;
import org.ovirt.engine.core.common.action.VmManagementParametersBase;
import org.ovirt.engine.core.common.action.VmOperationParameterBase;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.CpuPinningPolicy;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.businessentities.HaMaintenanceMode;
import org.ovirt.engine.core.common.businessentities.InitializationType;
import org.ovirt.engine.core.common.businessentities.SnapshotActionEnum;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.queries.GetDiskImageByDiskAndImageIdsParameters;
import org.ovirt.engine.core.common.queries.GetPermissionsForObjectParameters;
import org.ovirt.engine.core.common.queries.GetVmTemplateParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.NameQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendVmResource
        extends AbstractBackendActionableResource<Vm, org.ovirt.engine.core.common.businessentities.VM>
        implements VmResource {

    private BackendVmsResource parent;

    public static final String DETACH_ONLY = "detach_only";
    public static final String FORCE = "force";
    public static final String NEXT_RUN = "next_run";

    public BackendVmResource(String id, BackendVmsResource parent) {
        super(id, Vm.class, org.ovirt.engine.core.common.businessentities.VM.class);
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
                    getEntity(org.ovirt.engine.core.common.businessentities.VM.class, QueryType.GetVmNextRunConfiguration,
                            new IdQueryParameters(guid), id, true);
            vm = addLinks(populate(VmMapper.map(entity, null, false), entity));

        } else {
            vm = performGet(QueryType.GetVmByVmId, new IdQueryParameters(guid));
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
    public Response autoPinCpuAndNumaNodes(Action action) {
        VmManagementParametersBase params = new VmManagementParametersBase(getEntity(
                org.ovirt.engine.core.common.businessentities.VM.class,
                QueryType.GetVmByVmId,
                new IdQueryParameters(guid), "VM: id=" + guid));
        if (action.isOptimizeCpuSettings() != null && action.isOptimizeCpuSettings()) {
            params.getVm().setCpuPinningPolicy(CpuPinningPolicy.RESIZE_AND_PIN_NUMA);
        } else {
            throw new BaseBackendResource.WebFaultException(null,
                    localize(Messages.NOT_SUPPORTED_REASON, "`Pin` CPU Pinning policy"),
                    Response.Status.BAD_REQUEST);
        }


        return performAction(ActionType.UpdateVm, params);
    }

    @Override
    public Vm update(Vm incoming) {
        validateParameters(incoming);
        parent.validateVirtioScsiMultiQueues(incoming);
        if (incoming.isSetCluster() && (incoming.getCluster().isSetId() || incoming.getCluster().isSetName())) {
            Guid clusterId = lookupClusterId(incoming);
            if (!clusterId.toString().equals(get().getCluster().getId())) {
                performAction(ActionType.ChangeVMCluster,
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

        QueryType queryType = isNextRunRequested()
                ? QueryType.GetVmNextRunConfiguration
                : QueryType.GetVmByVmIdForUpdate;

        Vm vm = performUpdate(
            incoming,
            new QueryIdResolver<>(queryType, IdQueryParameters.class),
            ActionType.UpdateVm,
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
        return performAction(ActionType.RemoveVm, params);
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
        // validate that the provided cluster-compatibility-version is legal
        parent.validateClusterCompatibilityVersion(incoming);
    }

    protected Guid lookupClusterId(Vm vm) {
        return vm.getCluster().isSetId() ? asGuid(vm.getCluster().getId())
                : getEntity(Cluster.class,
                        QueryType.GetClusterByName,
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

    @Override
    public VmBackupsResource getBackupsResource() {
        return inject(new BackendVmBackupsResource(guid));
    }

    @Override
    public VmCheckpointsResource getCheckpointsResource() {
        return inject(new BackendVmCheckpointsResource(guid));
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
                                                             QueryType.GetPermissionsForObject,
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
        EntityIdResolver<Guid> resolver = new QueryIdResolver<>(QueryType.GetVmByVmId, IdQueryParameters.class);
        VmStatisticalQuery query = new VmStatisticalQuery(resolver, newModel(id));
        return inject(new BackendStatisticsResource<>(entityType, guid, query));
    }

    @Override
    public Response migrate(Action action) {
        boolean forceMigration = action.isSetForce() ? action.isForce() : false;

        if (action.isSetMigrateVmsInAffinityClosure() && action.isMigrateVmsInAffinityClosure()) {
            MigrateMultipleVmsParameters params = new MigrateMultipleVmsParameters(
                    Collections.singletonList(guid),
                    forceMigration);

            params.setAddVmsInPositiveHardAffinity(true);
            if (action.isSetHost()) {
                params.setDestinationHostId(getHostId(action));
            }

            return doAction(ActionType.MigrateMultipleVms, params, action);
        }

        if (!action.isSetHost()) {
            return doAction(ActionType.MigrateVm,
                    new MigrateVmParameters(forceMigration, guid, getTargetClusterId(action)),
                    action);
        } else {
            return doAction(ActionType.MigrateVmToServer,
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
        boolean forceStop = action.isSetForce() ? action.isForce() : false;
        String reason = action.getReason();
        return doAction(ActionType.ShutdownVm,
                        new ShutdownVmParameters(guid, true, reason, forceStop),
                        action);
    }

    @Override
    public Response reboot(Action action) {
        boolean forceStop = action.isSetForce() ? action.isForce() : false;
        return doAction(ActionType.RebootVm,
                        new RebootVmParameters(guid, forceStop),
                        action);
    }

    @Override
    public Response reset(Action action) {
        return doAction(ActionType.ResetVm,
                        new VmOperationParameterBase(guid),
                        action);
    }

    @Override
    public Response undoSnapshot(Action action) {
        RestoreAllSnapshotsParameters restoreParams = new RestoreAllSnapshotsParameters(guid, SnapshotActionEnum.UNDO);
        Response response = doAction(ActionType.RestoreAllSnapshots,
                restoreParams,
                action);
        return response;
    }

    @Override
    public Response doClone(Action action) {
        validateParameters(action, "vm.name");

        org.ovirt.engine.core.common.businessentities.VM vm = getEntity(
                org.ovirt.engine.core.common.businessentities.VM.class,
                QueryType.GetVmByVmId,
                new IdQueryParameters(guid), "VM: id=" + guid);
                CloneVmParameters cloneVmParameters = new CloneVmParameters(vm, action.getVm().getName());
        cloneVmParameters.setMakeCreatorExplicitOwner(isFiltered());
        if (action.isSetStorageDomain() && getStorageDomainId(action) != null) {
            cloneVmParameters.setDestStorageDomainId(getStorageDomainId(action));
        }

        ActionType actionType = !action.isSetDiscardSnapshots() || action.isDiscardSnapshots() ?
                ActionType.CloneVm:
                ActionType.CloneVmNoCollapse;

        Response response = doAction(actionType,
                cloneVmParameters,
                action);

        return response;
    }

    @Override
    public Response reorderMacAddresses(Action action) {
        getEntity(org.ovirt.engine.core.common.businessentities.VM.class,
                QueryType.GetVmByVmId,
                new IdQueryParameters(guid),
                "VM: id=" + guid,
                true);

        final VmOperationParameterBase params = new VmOperationParameterBase(guid);
        final Response response = doAction(
                ActionType.ReorderVmNics,
                params,
                action);

        return response;
    }

    @Override
    public Response commitSnapshot(Action action) {
        RestoreAllSnapshotsParameters restoreParams = new RestoreAllSnapshotsParameters(guid, SnapshotActionEnum.COMMIT);
        Response response = doAction(ActionType.RestoreAllSnapshots,
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
            // Each disk parameter is being mapped to a DiskImage.
            List<DiskImage> disks = getParent().mapDisks(action.getDisks());
            List<DiskImage> disksFromDB = null;

            if (disks != null) {
                // In case a disk hasn't specified its image_id, the imageId value is set to Guid.Empty().
                disksFromDB = disks.stream()
                        .map(disk -> getEntity(org.ovirt.engine.core.common.businessentities.storage.DiskImage.class,
                                QueryType.GetDiskImageByDiskAndImageIds,
                                new GetDiskImageByDiskAndImageIdsParameters(disk.getId(), disk.getImageId()),
                                String.format("GetDiskImageByDiskAndImageIds: disk id=%s, image_id=%s",
                                        disk.getId(), disk.getImageId())))
                        .collect(Collectors.toList());
            }
            tryBackParams.setDisks(disksFromDB);
        }
        if (action.isSetLease()) {
            tryBackParams.setRestoreLease(action.getLease().isSetStorageDomain());
            if (action.getLease().isSetStorageDomain()) {
                tryBackParams.setDstLeaseDomainId(asGuid(action.getLease().getStorageDomain().getId()));
            }
        }

        Response response = doAction(ActionType.TryBackToAllSnapshotsOfVm,
                tryBackParams,
                action);
        return response;
    }

    @Override
    public Response start(Action action) {
        RunVmParams params;
        ActionType actionType;
        if (action.isSetVm()) {
            Vm vm = action.getVm();
            actionType = ActionType.RunVmOnce;
            params = createRunVmOnceParams(vm, action.isSetVolatile() && action.isVolatile());
        } else {
            actionType = ActionType.RunVm;
            params = new RunVmParams(guid);
        }
        if (action.isSetPause() && action.isPause()) {
            params.setRunAndPause(true);
        }

        boolean sysPrepSet = action.isSetUseSysprep();
        boolean useSysPrep = sysPrepSet && action.isUseSysprep();
        boolean cloudInitSet = action.isSetUseCloudInit();
        boolean useCloudInit = cloudInitSet && action.isUseCloudInit();
        boolean ignitionSet = action.isSetUseIgnition();
        boolean useIgnition = ignitionSet && action.isUseIgnition();
        boolean useInitialization = action.isSetUseInitialization() && action.isUseInitialization();
        if (useSysPrep && useCloudInit || useSysPrep && useIgnition || useCloudInit && useIgnition) {
            Fault fault = new Fault();
            fault.setReason(localize(Messages.CANT_USE_MIXED_INIT_SIMULTANEOUSLY));
            return Response.status(Response.Status.CONFLICT).entity(fault).build();
        }
        if (useSysPrep) {
            params.setInitializationType(InitializationType.Sysprep);
        } else if (useCloudInit) {
            params.setInitializationType(InitializationType.CloudInit);
        } else if (useIgnition) {
            params.setInitializationType(InitializationType.Ignition);
        } else if ((sysPrepSet && !useSysPrep) || (cloudInitSet && !useCloudInit) || (ignitionSet && !useIgnition)) {
            //if sysprep or cloud-init were explicitly set to false, this indicates
            //that the user wants no initialization
            params.setInitializationType(InitializationType.None);
        } else {
            params.setInitializationType(null); //Engine will decide based on VM properties
        }
        params.setInitialize(useInitialization);

        return doAction(actionType, params, action);
    }

    private RunVmOnceParams createRunVmOnceParams(Vm vm, boolean volatileRun) {
        VM entity = getEntity(entityType, QueryType.GetVmByVmId, new IdQueryParameters(guid), id, true);
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
        params.setVolatileRun(volatileRun);

        return params;
    }

    @Override
    public Response stop(Action action) {
        String reason = action.getReason();
        boolean forceStop = action.isSetForce() ? action.isForce() : false;
        return doAction(ActionType.StopVm,
                        new StopVmParameters(guid, StopVmTypeEnum.NORMAL, reason, forceStop),
                        action);
    }

    @Override
    public Response suspend(Action action) {
        return doAction(ActionType.HibernateVm,
                        new VmOperationParameterBase(guid),
                        action);
    }

    @Override
    public Response detach(Action action) {
        return doAction(ActionType.RemoveVmFromPool,
                        new RemoveVmFromPoolParameters(guid, true, true),
                        action);
    }

    @Override
    public Response exportToExportDomain(Action action) {
        MoveOrCopyParameters params = new MoveOrCopyParameters(guid, getStorageDomainId(action));

        if (action.isSetExclusive() && action.isExclusive()) {
            params.setForceOverride(true);
        }

        if (action.isSetDiscardSnapshots() && action.isDiscardSnapshots()) {
            params.setCopyCollapse(true);
        }

        return doAction(ActionType.ExportVm, params, action);
    }

    @Override
    public Response exportToPathOnHost(Action action) {
        ExportVmToOvaParameters params = new ExportVmToOvaParameters();

        params.setEntityId(guid);
        params.setProxyHostId(getHostId(action));
        params.setDirectory(action.getDirectory());
        params.setName(action.getFilename());

        return doAction(ActionType.ExportVmToOva, params, action);
    }

    @Override
    public Response ticket(Action action) {
        return BackendGraphicsConsoleHelper.setTicket(this, action, guid, deriveGraphicsType());
    }

    private GraphicsType deriveGraphicsType() {
        org.ovirt.engine.core.common.businessentities.VM vm = getEntity(org.ovirt.engine.core.common.businessentities.VM.class,
                QueryType.GetVmByVmId, new IdQueryParameters(guid), "GetVmByVmId");

        return (vm == null)
                ? null
                : VmMapper.deriveGraphicsType(vm.getGraphicsInfos());
    }

    @Override
    public Response logon(Action action) {
        final Response response = doAction(ActionType.VmLogon,
                new VmOperationParameterBase(guid),
                action);
        return response;
    }

    @Override
    public Response freezeFilesystems(Action action) {
        final Response response = doAction(ActionType.FreezeVm,
                new VmOperationParameterBase(guid),
                action);
        return response;
    }

    @Override
    public Response thawFilesystems(Action action) {
        final Response response = doAction(ActionType.ThawVm,
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
        BackendVmDeviceHelper.setTpmDevice(this, model);
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
        public ActionParametersBase getParameters(Vm incoming,
                org.ovirt.engine.core.common.businessentities.VM entity) {
            VmStatic updated = getMapper(modelType, VmStatic.class).map(incoming,
                    entity.getStaticData());

            CpuPinningPolicy previousPolicy = entity.getCpuPinningPolicy();
            parent.updateCpuPinningFields(updated, previousPolicy);

            updated.setUsbPolicy(VmMapper.getUsbPolicyOnUpdate(incoming.getUsb(), entity.getUsbPolicy()));

            VmManagementParametersBase params = new VmManagementParametersBase(updated);

            if (incoming.isSetNumaTuneMode()) {
                params.setUpdateNuma(true);
            }

            params.setApplyChangesLater(isNextRunRequested());
            params.setMemoryHotUnplugEnabled(true);

            if (incoming.isSetPayloads()) {
                if (incoming.isSetPayloads() && incoming.getPayloads().isSetPayloads()) {
                    params.setVmPayload(parent.getPayload(incoming));
                } else {
                    params.setClearPayload(true);
                }
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
            if (incoming.isSetTpmEnabled()) {
                params.setTpmEnabled(incoming.isTpmEnabled());
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

    private Guid lookupInstanceTypeId(Template template) {
        return template.isSetId() ? asGuid(template.getId()) : lookupInstanceTypeByName(template).getId();
    }

    private VmTemplate lookupInstanceTypeByName(Template template) {
        return getEntity(VmTemplate.class,
                QueryType.GetInstanceType,
                new GetVmTemplateParameters(template.getName()),
                "GetVmTemplate");
    }

    @Override
    public Response cancelMigration(Action action) {
        return doAction(ActionType.CancelMigrateVm,
                new VmOperationParameterBase(guid), action);
    }

    public BackendVmsResource getParent() {
        return parent;
    }

    public void setCertificateInfo(Vm model) {
        DisplayHelper.addDisplayCertificate(this, model);
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
    public VmGraphicsConsolesResource getGraphicsConsolesResource() {
        return inject(new BackendVmGraphicsConsolesResource(guid));
    }

    @Override
    public Response maintenance(Action action) {
        validateParameters(action, "maintenanceEnabled");

        org.ovirt.engine.core.common.businessentities.VM entity =
                getEntity(org.ovirt.engine.core.common.businessentities.VM.class,
                          QueryType.GetVmByVmId,
                          new IdQueryParameters(guid),
                          id);
        if (!entity.isHostedEngine()) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Moving to maintenance mode is currently only available for the VM containing the hosted engine.")
                    .build());
        }

        return doAction(ActionType.SetHaMaintenance,
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

    @Override
    public Response screenshot(Action action) {
        String screenshot =
                performAction(ActionType.ScreenshotVm, new VmOperationParameterBase(guid), String.class); // gets base64 encoded image

        byte[] originalImage = Base64.getDecoder().decode(screenshot);
        return Response.ok(originalImage).build();
    }
}

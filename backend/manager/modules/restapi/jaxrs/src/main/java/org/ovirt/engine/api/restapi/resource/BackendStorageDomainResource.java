package org.ovirt.engine.api.restapi.resource;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.CreationStatus;
import org.ovirt.engine.api.model.Fault;
import org.ovirt.engine.api.model.HostStorage;
import org.ovirt.engine.api.model.LogicalUnit;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.api.model.StorageDomainStatus;
import org.ovirt.engine.api.model.StorageDomainType;
import org.ovirt.engine.api.resource.AssignedDiskProfilesResource;
import org.ovirt.engine.api.resource.AssignedPermissionsResource;
import org.ovirt.engine.api.resource.DiskSnapshotsResource;
import org.ovirt.engine.api.resource.FilesResource;
import org.ovirt.engine.api.resource.ImagesResource;
import org.ovirt.engine.api.resource.StorageDomainDisksResource;
import org.ovirt.engine.api.resource.StorageDomainResource;
import org.ovirt.engine.api.resource.StorageDomainServerConnectionsResource;
import org.ovirt.engine.api.resource.StorageDomainTemplatesResource;
import org.ovirt.engine.api.resource.StorageDomainVmsResource;
import org.ovirt.engine.api.restapi.util.ParametersHelper;
import org.ovirt.engine.api.restapi.util.StorageDomainHelper;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ExtendSANStorageDomainParameters;
import org.ovirt.engine.core.common.action.ReduceSANStorageDomainDevicesCommandParameters;
import org.ovirt.engine.core.common.action.RemoveStorageDomainParameters;
import org.ovirt.engine.core.common.action.StorageDomainManagementParameter;
import org.ovirt.engine.core.common.action.StorageDomainParametersBase;
import org.ovirt.engine.core.common.businessentities.StorageDomainSharedStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.queries.GetPermissionsForObjectParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.NameQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.queries.StorageDomainsAndStoragePoolIdQueryParameters;
import org.ovirt.engine.core.compat.Guid;

public class BackendStorageDomainResource
        extends AbstractBackendActionableResource<StorageDomain, org.ovirt.engine.core.common.businessentities.StorageDomain>
        implements StorageDomainResource {

    public static final String DESTROY = "destroy";
    public static final String FORMAT = "format";
    public static final String HOST = "host";

    private final BackendStorageDomainsResource parent;

    public BackendStorageDomainResource(String id, BackendStorageDomainsResource parent) {
        super(id,
                StorageDomain.class,
                org.ovirt.engine.core.common.businessentities.StorageDomain.class);
        this.parent = parent;
    }

    BackendStorageDomainsResource getParent() {
        return parent;
    }

    @Override
    public StorageDomain get() {
        StorageDomain storageDomain = performGet(QueryType.GetStorageDomainById, new IdQueryParameters(guid));
        return addLinks(storageDomain, getLinksToExclude(storageDomain));
    }

    @Override
    public StorageDomain update(StorageDomain incoming) {
        QueryIdResolver<Guid> storageDomainResolver =
                new QueryIdResolver<>(QueryType.GetStorageDomainById, IdQueryParameters.class);
        org.ovirt.engine.core.common.businessentities.StorageDomain entity = getEntity(storageDomainResolver, true);
        StorageDomain model = map(entity, new StorageDomain());
        StorageType storageType = entity.getStorageType();
        if (storageType != null) {
            switch (storageType) {
            case ISCSI:
            case FCP:
                extendStorageDomain(incoming, model);
                break;
            default:
                break;
            }
        }

        return addLinks(performUpdate(incoming,
                entity,
                model,
                storageDomainResolver,
                ActionType.UpdateStorageDomain,
                new UpdateParametersProvider()),
                new String[] { "templates", "vms" });
    }

    @Override
    public Response updateOvfStore(Action action) {
        StorageDomainParametersBase params = new StorageDomainParametersBase(guid);
        return performAction(ActionType.UpdateOvfStoreForStorageDomain, params);
    }

    @Override
    public Response reduceLuns(Action action) {
        List<LogicalUnit> reducedLuns = action.getLogicalUnits().getLogicalUnits();
        List<String> lunIds = reducedLuns.stream().map(LogicalUnit::getId).collect(toList());
        ReduceSANStorageDomainDevicesCommandParameters parameters =
                new ReduceSANStorageDomainDevicesCommandParameters(guid, lunIds);
        return performAction(ActionType.ReduceSANStorageDomainDevices, parameters);
    }


    @Override
    public Response remove() {
        boolean destroy = ParametersHelper.getBooleanParameter(httpHeaders, uriInfo, DESTROY, true, false);
        get();
        if (destroy) {
            StorageDomainParametersBase parameters = new StorageDomainParametersBase(guid);
            return performAction(ActionType.ForceRemoveStorageDomain, parameters);
        } else {
            String host = ParametersHelper.getParameter(httpHeaders, uriInfo, HOST);
            if (host == null) {
                Fault fault = new Fault();
                fault.setReason("host parameter is missing");
                throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(fault).build());
            }
            boolean format = ParametersHelper.getBooleanParameter(httpHeaders, uriInfo, FORMAT, true, false);
            Guid hostId = getHostId(host);
            RemoveStorageDomainParameters parameters = new RemoveStorageDomainParameters(guid);
            parameters.setVdsId(hostId);
            parameters.setDoFormat(format);
            return performAction(ActionType.RemoveStorageDomain, parameters);
        }
    }

    private Guid getHostId(String host) {
        try {
            return Guid.createGuidFromString(host);
        } catch (IllegalArgumentException exception) {
            VdsStatic entity = getEntity(
                VdsStatic.class,
                QueryType.GetVdsStaticByName,
                new NameQueryParameters(host),
                host
            );
            if (entity != null) {
                return entity.getId();
            }
            return Guid.Empty;
        }
    }

    @Override
    public Response isAttached(Action action) {
        validateParameters(action, "host.id|name");
        Guid hostId = getHostId(action);
        org.ovirt.engine.core.common.businessentities.StorageDomain storageDomainToAttach = getEntity(
            org.ovirt.engine.core.common.businessentities.StorageDomain.class,
            QueryType.GetStorageDomainById,
            new IdQueryParameters(guid),
            guid.toString()
        );
        StorageDomainsAndStoragePoolIdQueryParameters parameters =
                new StorageDomainsAndStoragePoolIdQueryParameters(storageDomainToAttach, null, hostId);
        parameters.setCheckStoragePoolStatus(false);
        List<StorageDomainStatic> attachedStorageDomains = getEntity(
            List.class,
            QueryType.GetStorageDomainsWithAttachedStoragePoolGuid,
            parameters,
            guid.toString(),
            true
        );

        // This is an atypical action, as it doesn't invoke a backend action, but a query. As a result we need to
        // create and populate the returned action object so that it looks like a real action result.
        Action result = new Action();
        result.setIsAttached(!attachedStorageDomains.isEmpty());
        result.setStatus(CreationStatus.COMPLETE.value());

        return Response.ok().entity(result).build();
    }

    @Override
    public Response refreshLuns(Action action) {
        List<LogicalUnit> incomingLuns;
        if (action.isSetLogicalUnits()) {
            incomingLuns = action.getLogicalUnits().getLogicalUnits();
        } else {
            incomingLuns = Collections.emptyList();
        }
        ExtendSANStorageDomainParameters params = createParameters(guid, incomingLuns, false);
        return performAction(ActionType.RefreshLunsSize, params);
    }

    @Override
    public FilesResource getFilesResource() {
        return inject(new BackendFilesResource(id));
    }

    public static synchronized boolean isIsoDomain(StorageDomain storageDomain) {
        StorageDomainType type = storageDomain.getType();
        return type != null && type == StorageDomainType.ISO ? true : false;
    }

    public static synchronized boolean isIsoDomain(org.ovirt.engine.core.common.businessentities.StorageDomain storageDomain) {
        org.ovirt.engine.core.common.businessentities.StorageDomainType type = storageDomain.getStorageDomainType();
        return type != null && type == org.ovirt.engine.core.common.businessentities.StorageDomainType.ISO ? true
                : false;
    }

    public static synchronized boolean isExportDomain(StorageDomain storageDomain) {
        StorageDomainType type = storageDomain.getType();
        return type != null && type == StorageDomainType.EXPORT ? true : false;
    }

    public static synchronized boolean isImageDomain(StorageDomain storageDomain) {
        StorageDomainType type = storageDomain.getType();
        return type != null && type == StorageDomainType.IMAGE;
    }

    public static synchronized String[] getLinksToExclude(StorageDomain storageDomain) {
        return isIsoDomain(storageDomain) ? new String[] { "templates", "vms", "disks", "images" }
                : isExportDomain(storageDomain) ? new String[] { "files", "images" }
                        : isImageDomain(storageDomain) ? new String[] { "templates", "vms", "files", "disks",
                                "storageconnections" }
                                : new String[] { "files", "images" };
    }

    /**
     * if user added new LUNs - extend the storage domain.
     */
    private void extendStorageDomain(StorageDomain incoming, StorageDomain storageDomain) {
        if (incoming.getStorage() == null) {
            // LUNs info was not supplied in the request so no need to check whether to extend
            return;
        }
        List<LogicalUnit> existingLuns;
        if (storageDomain.isSetStorage() && storageDomain.getStorage().isSetVolumeGroup() &&
                storageDomain.getStorage().getVolumeGroup().isSetLogicalUnits()) {
            existingLuns = storageDomain.getStorage().getVolumeGroup().getLogicalUnits().getLogicalUnits();
        } else {
            existingLuns = Collections.emptyList();
        }
        List<LogicalUnit> incomingLuns = getIncomingLuns(incoming.getStorage());
        List<LogicalUnit> newLuns = findNewLuns(existingLuns, incomingLuns);
        boolean overrideLuns = incoming.getStorage().isSetOverrideLuns() ?
                incoming.getStorage().isOverrideLuns() : false;
        if (!newLuns.isEmpty()) {
            // If there are new LUNs, this means the user wants to extend the storage domain.
            addLunsToStorageDomain(newLuns, overrideLuns);
            // Remove the new LUNs from the incoming LUns before update, since they have already been dealt with.
            incomingLuns.removeAll(newLuns);
        }
    }

    private void addLunsToStorageDomain(List<LogicalUnit> newLuns, boolean overrideLuns) {

        ExtendSANStorageDomainParameters params = createParameters(guid, newLuns, overrideLuns);
        performAction(ActionType.ExtendSANStorageDomain, params);
    }

    @Override
    public AssignedPermissionsResource getPermissionsResource() {
        return inject(new BackendAssignedPermissionsResource(guid,
                QueryType.GetPermissionsForObject,
                new GetPermissionsForObjectParameters(guid),
                StorageDomain.class,
                VdcObjectType.Storage));
    }

    @Override
    protected StorageDomain map(org.ovirt.engine.core.common.businessentities.StorageDomain entity,
            StorageDomain template) {
        return parent.map(entity, template);
    }

    @Override
    protected StorageDomain deprecatedPopulate(StorageDomain model,
            org.ovirt.engine.core.common.businessentities.StorageDomain entity) {
        if (StorageDomainSharedStatus.Unattached.equals(entity.getStorageDomainSharedStatus())) {
            model.setStatus(StorageDomainStatus.UNATTACHED);
        } else {
            model.setStatus(null);
        }
        return super.deprecatedPopulate(model, entity);
    }

    private List<LogicalUnit> getIncomingLuns(HostStorage storage) {
        // user may pass the LUNs under Storage, or Storage-->VolumeGroup; both are supported.
        if (!storage.isSetLogicalUnits() || !storage.getLogicalUnits().isSetLogicalUnits()) {
            if (storage.isSetVolumeGroup() && storage.getVolumeGroup().isSetLogicalUnits()
                    && storage.getVolumeGroup().getLogicalUnits().isSetLogicalUnits()) {
                return storage.getVolumeGroup().getLogicalUnits().getLogicalUnits();
            } else {
                return new ArrayList<>();
            }
        } else {
            return storage.getLogicalUnits().getLogicalUnits();
        }
    }

    private ExtendSANStorageDomainParameters createParameters(Guid storageDomainId,
            List<LogicalUnit> newLuns,
            boolean force) {
        ExtendSANStorageDomainParameters params = new ExtendSANStorageDomainParameters();
        params.setStorageDomainId(storageDomainId);
        Set<String> lunIds = newLuns.stream().map(LogicalUnit::getId).collect(Collectors.toSet());
        params.setLunIds(lunIds);
        params.setForce(force);
        return params;
    }

    private List<LogicalUnit> findNewLuns(List<LogicalUnit> existingLuns, List<LogicalUnit> incomingLuns) {
        List<LogicalUnit> newLuns = new LinkedList<>();
        for (LogicalUnit incomingLun : incomingLuns) {
            boolean found = false;
            for (LogicalUnit existingLun : existingLuns) {
                if (lunsEqual(incomingLun, existingLun)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                newLuns.add(incomingLun);
            }
        }
        return newLuns;
    }

    private boolean lunsEqual(LogicalUnit firstLun, LogicalUnit secondLun) {
        return Objects.equals(firstLun.getId(), secondLun.getId());
    }

    protected class UpdateParametersProvider implements
            ParametersProvider<StorageDomain, org.ovirt.engine.core.common.businessentities.StorageDomain> {
        @Override
        public ActionParametersBase getParameters(StorageDomain incoming,
                org.ovirt.engine.core.common.businessentities.StorageDomain entity) {
            // save SD type before mapping
            org.ovirt.engine.core.common.businessentities.StorageDomainType currentType =
                    entity.getStorageStaticData() == null ? null : entity.getStorageStaticData().getStorageDomainType();
            StorageDomainStatic updated = getMapper(modelType, StorageDomainStatic.class).map(
                    incoming, entity.getStorageStaticData());
            // if SD type was 'Master', and user gave 'Data', they are the same, this is not a real update, so exchange
            // data back to master.
            if (currentType == org.ovirt.engine.core.common.businessentities.StorageDomainType.Master
                    && updated.getStorageDomainType() == org.ovirt.engine.core.common.businessentities.StorageDomainType.Data) {
                updated.setStorageDomainType(org.ovirt.engine.core.common.businessentities.StorageDomainType.Master);
            }
            return new StorageDomainManagementParameter(updated);
        }
    }

    @Override
    public StorageDomainTemplatesResource getTemplatesResource() {
        return inject(new BackendStorageDomainTemplatesResource(guid));
    }

    @Override
    public StorageDomainVmsResource getVmsResource() {
        return inject(new BackendStorageDomainVmsResource(guid));
    }

    @Override
    public StorageDomainDisksResource getDisksResource() {
        return inject(new BackendStorageDomainDisksResource(guid));
    }

    @Override
    public StorageDomainServerConnectionsResource getStorageConnectionsResource() {
        return inject(new BackendStorageDomainServerConnectionsResource(guid));
    }

    @Override
    public ImagesResource getImagesResource() {
        return inject(new BackendStorageDomainImagesResource(guid));
    }

    @Override
    public DiskSnapshotsResource getDiskSnapshotsResource() {
        return inject(new BackendStorageDomainDiskSnapshotsResource(guid));
    }

    @Override
    public AssignedDiskProfilesResource getDiskProfilesResource() {
        return inject(new BackendAssignedDiskProfilesResource(id));
    }

    @Override
    protected StorageDomain addParents(StorageDomain model) {
        StorageDomainHelper.addAttachedDataCenterReferences(this, model);
        return model;
    }
}

package org.ovirt.engine.api.restapi.resource;

import static org.ovirt.engine.api.restapi.resource.BackendStorageDomainResource.getLinksToExclude;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.api.model.HostStorage;
import org.ovirt.engine.api.model.LogicalUnit;
import org.ovirt.engine.api.model.LogicalUnits;
import org.ovirt.engine.api.model.Properties;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.api.model.StorageDomainStatus;
import org.ovirt.engine.api.model.StorageDomains;
import org.ovirt.engine.api.model.VolumeGroup;
import org.ovirt.engine.api.resource.StorageDomainResource;
import org.ovirt.engine.api.resource.StorageDomainsResource;
import org.ovirt.engine.api.restapi.types.StorageDomainMapper;
import org.ovirt.engine.api.restapi.util.StorageDomainHelper;
import org.ovirt.engine.api.restapi.utils.CustomPropertiesParser;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddManagedBlockStorageDomainParameters;
import org.ovirt.engine.core.common.action.AddSANStorageDomainParameters;
import org.ovirt.engine.core.common.action.StorageDomainManagementParameter;
import org.ovirt.engine.core.common.action.StorageServerConnectionParametersBase;
import org.ovirt.engine.core.common.businessentities.StorageDomainSharedStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.GetDeviceListQueryParameters;
import org.ovirt.engine.core.common.queries.GetExistingStorageDomainListParameters;
import org.ovirt.engine.core.common.queries.GetLunsByVgIdParameters;
import org.ovirt.engine.core.common.queries.GetUnregisteredBlockStorageDomainsParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.NameQueryParameters;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.queries.StorageServerConnectionQueryParametersBase;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;

public class BackendStorageDomainsResource
        extends AbstractBackendCollectionResource<StorageDomain, org.ovirt.engine.core.common.businessentities.StorageDomain>
        implements StorageDomainsResource {

    private final EntityIdResolver<Guid> ID_RESOLVER =
            new QueryIdResolver<>(QueryType.GetStorageDomainById, IdQueryParameters.class);

    public BackendStorageDomainsResource() {
        super(StorageDomain.class, org.ovirt.engine.core.common.businessentities.StorageDomain.class);
    }

    @Override
    public StorageDomains list() {
        if (isFiltered()) {
            return mapCollection(getBackendCollection(QueryType.GetAllStorageDomains,
                    new QueryParametersBase(), SearchType.StorageDomain));
        } else {
            return mapCollection(getBackendCollection(SearchType.StorageDomain));
        }
    }

    @Override
    public StorageDomainResource getStorageDomainResource(String id) {
        return inject(new BackendStorageDomainResource(id, this));
    }

    private Response addDomain(ActionType action,
            StorageDomain model,
            StorageDomainStatic entity,
            Guid hostId,
            StorageServerConnections connection) {
        Response response = null;
        boolean isConnNew = false;
        if (connection.getStorageType().isFileDomain() && StringUtils.isEmpty(connection.getId())) {
            isConnNew = true;
            connection.setId(addStorageServerConnection(connection, hostId));
        }
        entity.setStorage(connection.getId());
        if (action == ActionType.AddNFSStorageDomain || action == ActionType.AddPosixFsStorageDomain ||
                action == ActionType.AddGlusterFsStorageDomain) {
            org.ovirt.engine.core.common.businessentities.StorageDomain existing =
                    getExistingStorageDomain(hostId,
                            entity.getStorageType(),
                            entity.getStorageDomainType(),
                            connection);
            if (existing != null) {
                StorageDomainMapper.map(model, existing.getStorageStaticData());
                entity = existing.getStorageStaticData();
                action = ActionType.AddExistingFileStorageDomain;
            }
        }

        try {
            if (action != ActionType.AddExistingFileStorageDomain) {
                validateParameters(model, 2, "name");
            }
            response = performCreate(action, getAddParams(entity, hostId), ID_RESOLVER);
        } catch (WebApplicationException e) {
            // cleanup of created connection
            if (isConnNew) {
                removeStorageServerConnection(connection, hostId);
            }
            throw e;
        }
        return response;
    }

    private Response addManagedBlockStorageDomain(ActionType action,
            StorageDomainStatic entity,
            Properties driverOptions,
            Properties driverSensitiveOptions) {
        return performCreate(action, getManagedBlockStorageAddParams(entity, driverOptions, driverSensitiveOptions), ID_RESOLVER);
    }

    private Response addSAN(StorageDomain model, StorageType storageType, StorageDomainStatic entity, Guid hostId) {
        boolean overrideLuns = model.getStorage().isSetOverrideLuns() ? model.getStorage().isOverrideLuns() : false;

        return performCreate(ActionType.AddSANStorageDomain,
                getSanAddParams(entity,
                        hostId,
                        getLunIds(model.getStorage(), storageType, hostId),
                        overrideLuns),
                ID_RESOLVER);
    }

    private Response addExistingSAN(StorageDomain model, StorageType storageType, Guid hostId) {
        getEntity(VDS.class,
                QueryType.GetVdsByVdsId,
                new IdQueryParameters(hostId),
                "Host: id=" + hostId);
        List<LUNs> existingLuns = getDeviceList(hostId, storageType);
        List<StorageServerConnections> existingStorageServerConnections =
                getLunsWithInitializedStorageType(existingLuns, storageType);

        List<org.ovirt.engine.core.common.businessentities.StorageDomain> existingStorageDomains =
                getExistingBlockStorageDomain(hostId,
                        storageType,
                        existingStorageServerConnections);

        StorageDomainStatic storageDomainToImport =
                getMatchingStorageDomain(asGuid(model.getId()), existingStorageDomains);
        if (storageDomainToImport == null) {
            throw new WebFaultException(new WebApplicationException(), "Storage Domain id " + model.getId()
                    + " Does not exists", Status.NOT_FOUND);
        }
        StorageDomainMapper.map(model, storageDomainToImport);
        StorageDomainManagementParameter parameters =
                new StorageDomainManagementParameter(storageDomainToImport);
        parameters.setVdsId(hostId);
        return performCreate(ActionType.AddExistingBlockStorageDomain, parameters, ID_RESOLVER);
    }

    private StorageDomainStatic getMatchingStorageDomain(Guid storageId,
            List<org.ovirt.engine.core.common.businessentities.StorageDomain> existingStorageDomains) {
        StorageDomainStatic storageDomainStatic = new StorageDomainStatic();
        for (org.ovirt.engine.core.common.businessentities.StorageDomain storageDomain : existingStorageDomains) {
            if (storageDomain.getStorageStaticData().getId().equals(storageId)) {
                storageDomainStatic = storageDomain.getStorageStaticData();
                break;
            }
        }
        return storageDomainStatic;
    }

    private List<StorageServerConnections> getLunsWithInitializedStorageType(List<LUNs> luns, StorageType storageType) {
        List<StorageServerConnections> existingStorageServerConnections = new ArrayList<>();
        for (LUNs lun : luns) {
            for (StorageServerConnections storageServerConnection : lun.getLunConnections()) {
                storageServerConnection.setStorageType(storageType);
                existingStorageServerConnections.add(storageServerConnection);
            }
        }
        return existingStorageServerConnections;
    }

    private List<org.ovirt.engine.core.common.businessentities.StorageDomain> getExistingBlockStorageDomain(Guid hostId,
            StorageType storageType,
            List<StorageServerConnections> cnxList) {
        Pair<List<org.ovirt.engine.core.common.businessentities.StorageDomain>, List<StorageServerConnections>> pair =
                getEntity(Pair.class,
                        QueryType.GetUnregisteredBlockStorageDomains,
                        new GetUnregisteredBlockStorageDomainsParameters(hostId, storageType, cnxList),
                        "GetUnregisteredBlockStorageDomains", true);

        List<org.ovirt.engine.core.common.businessentities.StorageDomain> existingStorageDomains = pair.getFirst();
        return existingStorageDomains;
    }

    private List<LUNs> getDeviceList(Guid hostId, StorageType storageType) {
        return getEntity(List.class,
                QueryType.GetDeviceList,
                new GetDeviceListQueryParameters(hostId, storageType, false, null, false),
                "GetDeviceList", true);
    }

    private Set<String> getLunIds(HostStorage storage, StorageType storageType, Guid hostId) {
        List<LogicalUnit> logicalUnits = new ArrayList<>();

        if (storage.isSetLogicalUnits() && storage.getLogicalUnits().isSetLogicalUnits()) {
            logicalUnits = storage.getLogicalUnits().getLogicalUnits();
        } else if (storage.isSetVolumeGroup() && storage.getVolumeGroup().isSetLogicalUnits() &&
                storage.getVolumeGroup().getLogicalUnits().isSetLogicalUnits()) {
            logicalUnits = storage.getVolumeGroup().getLogicalUnits().getLogicalUnits();
        }

        Set<String> lunIds = new HashSet<>();
        for (LogicalUnit unit : logicalUnits) {
            validateParameters(unit, 4, "id");
            // if the address and target were not supplied, we understand from this that
            // the user assumes that the host is already logged-in to the target of this lun.
            // so in this case we do not need (and do not have the required information) to login
            // to the target.
            if ((storageType == StorageType.ISCSI) && !isConnectionAssumed(unit)) {
                connectStorageToHost(hostId, storageType, unit);
            }
            lunIds.add(unit.getId());
        }
        refreshHostStorage(hostId);
        return !lunIds.isEmpty() ? lunIds : null;
    }

    private boolean isConnectionAssumed(LogicalUnit unit) {
        // either 'target' and 'address' should both be provided, or none. Validate this
        if (unit.getAddress() != null || unit.getTarget() != null) {
            validateParameters(unit, "address", "target");
        }
        boolean connectionAssumed = unit.getAddress() == null || unit.getTarget() == null;
        return connectionAssumed;
    }

    /**
     * This is a work-around for a VDSM bug. The call to GetDeviceList causes a necessary refresh in the VDSM, without
     * which the creation will fail.
     */
    private void refreshHostStorage(Guid hostId) {
        getBackendCollection(QueryType.GetDeviceList, new GetDeviceListQueryParameters(hostId,
                StorageType.ISCSI,
                false, null, false));
    }

    private void connectStorageToHost(Guid hostId, StorageType storageType, LogicalUnit unit) {
        StorageServerConnections cnx =
                StorageDomainHelper.getConnection(storageType,
                        unit.getAddress(),
                        unit.getTarget(),
                        unit.getUsername(),
                        unit.getPassword(),
                        unit.getPort());
        performAction(ActionType.ConnectStorageToVds,
                new StorageServerConnectionParametersBase(cnx, hostId, false));
    }

    @Override
    public Response add(StorageDomain storageDomain) {
        validateParameters(storageDomain, "host.id|name", "type", "storage");
        validateRange("nfs_timeio", storageDomain.getStorage().getNfsTimeo(), 0, 65535);
        validateRange("nfs_retrans", storageDomain.getStorage().getNfsRetrans(), 0, 65535);
        HostStorage storageConnectionFromUser = storageDomain.getStorage();
        Guid hostId = getHostId(storageDomain);
        StorageServerConnections cnx = null;
        if (storageConnectionFromUser.getType() != org.ovirt.engine.api.model.StorageType.MANAGED_BLOCK_STORAGE) {
            if (!storageConnectionFromUser.isSetId()) {
                validateParameters(storageDomain, "storage.type");
                cnx = mapToCnx(storageDomain);
                if (cnx.getStorageType().isFileDomain()) {
                    validateParameters(storageConnectionFromUser, "path");
                }
            } else {
                cnx = getStorageServerConnection(storageConnectionFromUser.getId());
                storageDomain.getStorage().setType(mapType(cnx.getStorageType()));
            }
        }
        StorageDomainStatic entity = mapToStatic(storageDomain);
        Response resp = null;
        switch (entity.getStorageType()) {
        case ISCSI:
        case FCP:
            if (storageDomain.isSetImport() && storageDomain.isImport()) {
                validateParameters(storageDomain, "id");
                resp = addExistingSAN(storageDomain, entity.getStorageType(), hostId);
            } else {
                resp = addSAN(storageDomain, entity.getStorageType(), entity, hostId);
            }
            break;
        case NFS:
            if (!storageConnectionFromUser.isSetId()) {
                validateParameters(storageDomain.getStorage(), "address");
            }
            resp = addDomain(ActionType.AddNFSStorageDomain, storageDomain, entity, hostId, cnx);
            break;
        case LOCALFS:
            resp = addDomain(ActionType.AddLocalStorageDomain, storageDomain, entity, hostId, cnx);
            break;
        case POSIXFS:
            if (!storageConnectionFromUser.isSetId()) {
                validateParameters(storageDomain.getStorage(), "vfsType");
            }
            resp = addDomain(ActionType.AddPosixFsStorageDomain, storageDomain, entity, hostId, cnx);
            break;
        case GLUSTERFS:
            if (!storageConnectionFromUser.isSetId()) {
                validateParameters(storageDomain.getStorage(), "vfsType");
            }
            resp = addDomain(ActionType.AddGlusterFsStorageDomain, storageDomain, entity, hostId, cnx);
            break;
        case MANAGED_BLOCK_STORAGE:
            resp = addManagedBlockStorageDomain(ActionType.AddManagedBlockStorageDomain,
                    entity,
                    storageDomain.getStorage().getDriverOptions(),
                    storageDomain.getStorage().getDriverSensitiveOptions());
            break;
        default:
            break;
        }

        if (resp != null) {
            addLinks((StorageDomain) resp.getEntity(), getLinksToExclude(storageDomain));
        }
        return resp;
    }

    protected StorageDomainStatic mapToStatic(StorageDomain model) {
        return getMapper(modelType, StorageDomainStatic.class).map(model, null);
    }

    protected org.ovirt.engine.api.model.StorageType mapType(StorageType type) {
        return getMapper(StorageType.class, org.ovirt.engine.api.model.StorageType.class).map(type, null);
    }

    @Override
    protected StorageDomain map(org.ovirt.engine.core.common.businessentities.StorageDomain entity,
            StorageDomain template) {
        StorageDomain model = super.map(entity, template);

        // Mapping the connection properties only in case it is a non-filtered session
        if (!isFiltered()) {
            switch (entity.getStorageType()) {
            case ISCSI:
                mapVolumeGroupIscsi(model, entity);
                break;
            case FCP:
                mapVolumeGroupFcp(model, entity);
                break;
            case NFS:
            case LOCALFS:
            case POSIXFS:
            case GLUSTERFS:
                mapFileDomain(model, entity);
                break;
            }
        }

        return model;
    }

    protected void mapFileDomain(StorageDomain model,
            org.ovirt.engine.core.common.businessentities.StorageDomain entity) {
        final HostStorage storage = model.getStorage();
        StorageServerConnections cnx = getStorageServerConnection(entity.getStorage());
        if (cnx.getConnection().startsWith("[")) {
            String[] parts = cnx.getConnection().split("]:");
            storage.setAddress(parts[0].concat("]"));
            storage.setPath(parts[1]);
        } else if (cnx.getConnection().contains(":")) {
            String[] parts = cnx.getConnection().split(":");
            storage.setAddress(parts[0]);
            storage.setPath(parts[1]);
        } else {
            storage.setPath(cnx.getConnection());
        }
        storage.setMountOptions(cnx.getMountOptions());
        storage.setVfsType(cnx.getVfsType());
        if (entity.getStorageType() == StorageType.NFS) {
            if (cnx.getNfsRetrans() != null) {
                storage.setNfsRetrans(cnx.getNfsRetrans().intValue());
            }
            if (cnx.getNfsTimeo() != null) {
                storage.setNfsTimeo(cnx.getNfsTimeo().intValue());
            }
            if (cnx.getNfsVersion() != null) {
                storage.setNfsVersion(StorageDomainMapper.map(cnx.getNfsVersion(), null));
            }
        }
    }

    protected void mapVolumeGroupIscsi(StorageDomain model,
            org.ovirt.engine.core.common.businessentities.StorageDomain entity) {
        VolumeGroup vg = model.getStorage().getVolumeGroup();
        List<LUNs> luns = getLunsByVgId(vg.getId());
        if (luns != null && !luns.isEmpty()) {
            vg.setLogicalUnits(new LogicalUnits());
            for (LUNs lun : luns) {
                List<StorageServerConnections> lunConnections = lun.getLunConnections();
                if (lunConnections != null) {
                    for (StorageServerConnections cnx : lunConnections) {
                        LogicalUnit unit = map(lun);
                        unit = map(cnx, unit);
                        vg.getLogicalUnits().getLogicalUnits().add(unit);
                    }
                }
            }
        }
    }

    protected void mapVolumeGroupFcp(StorageDomain model,
            org.ovirt.engine.core.common.businessentities.StorageDomain entity) {
        VolumeGroup vg = model.getStorage().getVolumeGroup();
        List<LUNs> luns = getLunsByVgId(vg.getId());
        if (luns != null && !luns.isEmpty()) {
            vg.setLogicalUnits(new LogicalUnits());
            for (LUNs lun : luns) {
                LogicalUnit unit = map(lun);
                vg.getLogicalUnits().getLogicalUnits().add(unit);
            }
        }
    }

    protected LogicalUnit map(LUNs lun) {
        return getMapper(LUNs.class, LogicalUnit.class).map(lun, null);
    }

    protected LogicalUnit map(StorageServerConnections cnx, LogicalUnit template) {
        return getMapper(StorageServerConnections.class, LogicalUnit.class).map(cnx, template);
    }

    protected StorageType map(org.ovirt.engine.api.model.StorageType type) {
        return getMapper(org.ovirt.engine.api.model.StorageType.class, StorageType.class).map(type, null);
    }

    protected org.ovirt.engine.api.model.StorageType map(StorageType type) {
        return getMapper(StorageType.class, org.ovirt.engine.api.model.StorageType.class).map(type, null);
    }

    private StorageDomains mapCollection(List<org.ovirt.engine.core.common.businessentities.StorageDomain> entities) {
        StorageDomains collection = new StorageDomains();
        for (org.ovirt.engine.core.common.businessentities.StorageDomain entity : entities) {
            StorageDomain storageDomain = map(entity);
            // status is only relevant in the context of a data-center, so it can either be 'Unattached' or null.
            if (StorageDomainSharedStatus.Unattached.equals(entity.getStorageDomainSharedStatus())) {
                storageDomain.setStatus(StorageDomainStatus.UNATTACHED);
            } else {
                storageDomain.setStatus(null);
            }
            collection.getStorageDomains().add(addLinks(storageDomain, getLinksToExclude(storageDomain)));
        }
        return collection;
    }

    protected StorageServerConnections mapToCnx(StorageDomain model) {
        return getMapper(StorageDomain.class,
                StorageServerConnections.class).map(model, null);
    }

    private Guid getHostId(StorageDomain storageDomain) {
        // presence of host ID or name already validated
        return storageDomain.getHost().isSetId()
                ? new Guid(storageDomain.getHost().getId())
                : storageDomain.getHost().isSetName()
                        ? getEntity(VdsStatic.class,
                                QueryType.GetVdsStaticByName,
                                new NameQueryParameters(storageDomain.getHost().getName()),
                                "Hosts: name=" + storageDomain.getHost().getName()).getId()
                        : null;
    }

    private String addStorageServerConnection(StorageServerConnections cnx, Guid hostId) {
        return performAction(ActionType.AddStorageServerConnection,
                new StorageServerConnectionParametersBase(cnx, hostId, false),
                String.class);
    }

    private String removeStorageServerConnection(StorageServerConnections cnx, Guid hostId) {
        return performAction(ActionType.RemoveStorageServerConnection,
                new StorageServerConnectionParametersBase(cnx, hostId, false),
                String.class);
    }

    private StorageServerConnections getStorageServerConnection(String id) {
        QueryReturnValue result = runQuery(
                QueryType.GetStorageServerConnectionById,
                new StorageServerConnectionQueryParametersBase(id)
        );
        if (result.getSucceeded() && result.getReturnValue() != null) {
            return (StorageServerConnections) result.getReturnValue();
        }
        throw new WebFaultException(
                null,
                "Can't find storage server connection for id '" + id + "'.",
                Status.INTERNAL_SERVER_ERROR
        );
    }

    private List<LUNs> getLunsByVgId(String vgId) {
        return asCollection(LUNs.class,
                getEntity(List.class,
                        QueryType.GetLunsByVgId,
                        new GetLunsByVgIdParameters(vgId),
                        "LUNs for volume group: id=" + vgId));
    }

    private org.ovirt.engine.core.common.businessentities.StorageDomain getExistingStorageDomain(Guid hostId,
            StorageType storageType,
            StorageDomainType domainType,
            StorageServerConnections cnx) {
        List<org.ovirt.engine.core.common.businessentities.StorageDomain> existing =
                asCollection(org.ovirt.engine.core.common.businessentities.StorageDomain.class,
                        getEntity(ArrayList.class,
                                QueryType.GetExistingStorageDomainList,
                                new GetExistingStorageDomainListParameters(hostId,
                                        storageType,
                                        domainType,
                                        cnx.getConnection()),
                                "Existing storage domains: path=" + cnx.getConnection()));
        return existing.size() != 0 ? existing.get(0) : null;
    }

    private StorageDomainManagementParameter getAddParams(StorageDomainStatic entity, Guid hostId) {
        StorageDomainManagementParameter params = new StorageDomainManagementParameter(entity);
        params.setVdsId(hostId);
        return params;
    }

    private AddSANStorageDomainParameters getSanAddParams(StorageDomainStatic entity,
            Guid hostId,
            Set<String> lunIds,
            boolean force) {
        AddSANStorageDomainParameters params = new AddSANStorageDomainParameters(entity);
        params.setVdsId(hostId);
        params.setLunIds(lunIds);
        params.setForce(force);
        return params;
    }

    private AddManagedBlockStorageDomainParameters getManagedBlockStorageAddParams(StorageDomainStatic entity,
            Properties driverOptions,
            Properties driverSensitiveOptions) {
        AddManagedBlockStorageDomainParameters params = new AddManagedBlockStorageDomainParameters();

        // This assumes that every string passed wrapped in "[]" is an array, while there's a chance
        // an evil driver will use "[]" to wrap a regular string, there's not much we can do as we do not
        // know the types.
        Function<String, Object> mapper = value -> {
            if (value.startsWith("[") && value.endsWith("]")) {
                String arrField = value.substring(1, value.length() - 1);
                return Arrays.stream(arrField.split(",")).map(String::trim).toArray();
            }

            return value;
        };

        Map<String, Object> driverOptionsMap = new HashMap<>(CustomPropertiesParser.toObjectsMap(driverOptions, mapper));
        driverOptionsMap.put(AddManagedBlockStorageDomainParameters.VOLUME_BACKEND_NAME, entity.getName());
        params.setDriverOptions(driverOptionsMap);
        params.setSriverSensitiveOptions(CustomPropertiesParser.toObjectsMap(driverSensitiveOptions));
        entity.setStorage(Guid.Empty.toString());
        params.setStorageDomain(entity);
        return params;
    }

    @Override
    protected StorageDomain addParents(StorageDomain model) {
        StorageDomainHelper.addAttachedDataCenterReferences(this, model);
        return model;
    }
}

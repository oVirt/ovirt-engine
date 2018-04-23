package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.HostStorage;
import org.ovirt.engine.api.model.HostStorages;
import org.ovirt.engine.api.model.LogicalUnit;
import org.ovirt.engine.api.resource.HostStorageResource;
import org.ovirt.engine.api.resource.StorageResource;
import org.ovirt.engine.api.restapi.util.ParametersHelper;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.queries.GetDeviceListQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;

public class BackendHostStorageResource
    extends AbstractBackendCollectionResource<HostStorage, LUNs>
    implements HostStorageResource {

    private static final String REPORT_STATUS = "report_status";

    private String hostId;

    public BackendHostStorageResource(String hostId) {
        super(HostStorage.class, LUNs.class);
        this.hostId = hostId;
    }

    public HostStorages list() {
        HostStorages ret = new HostStorages();

        for (LUNs lun : getLogicalUnits()) {
            HostStorage storage = map(lun);
            List<StorageServerConnections> lunConnections = lun.getLunConnections();
            if (lunConnections!=null && !lunConnections.isEmpty()) {
                getMapper(StorageServerConnections.class, LogicalUnit.class).map(lunConnections.get(0),
                        storage.getLogicalUnits().getLogicalUnits().get(0));
            }
            ret.getHostStorages().add(addLinks(storage));
        }
        return ret;
    }

    public StorageResource getStorageResource(String id) {
        return new BackendStorageResource(id, this);
    }

    protected HostStorage lookupStorage(String id) {
        for (LUNs lun : getLogicalUnits()) {
            if (lun.getLUNId().equals(id)) {
                return addLinks(map(lun));
            }
        }
        return notFound();
    }

    protected List<LUNs> getLogicalUnits() {
        boolean reportStatus = ParametersHelper.getBooleanParameter(httpHeaders, uriInfo, REPORT_STATUS, true, true);
        GetDeviceListQueryParameters params =
                new GetDeviceListQueryParameters(asGuid(hostId), StorageType.UNKNOWN, reportStatus, null, true);
        return getBackendCollection(QueryType.GetDeviceList, params);
    }

    protected HostStorage map(org.ovirt.engine.core.common.businessentities.StorageDomain entity) {
        return getMapper(org.ovirt.engine.core.common.businessentities.StorageDomain.class, HostStorage.class).map(entity, null);
    }

    @Override
    protected HostStorage addParents(HostStorage storage) {
        storage.setHost(new Host());
        storage.getHost().setId(hostId);
        return storage;
    }
}

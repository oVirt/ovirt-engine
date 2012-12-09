package org.ovirt.engine.api.restapi.resource;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.HostStorage;
import org.ovirt.engine.api.model.LogicalUnit;
import org.ovirt.engine.api.model.Storage;
import org.ovirt.engine.api.resource.HostStorageResource;
import org.ovirt.engine.api.resource.StorageResource;

import org.ovirt.engine.core.common.businessentities.LUNs;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.queries.GetDeviceListQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.queries.VdsIdParametersBase;

public class BackendHostStorageResource
    extends AbstractBackendCollectionResource<Storage, LUNs>
    implements HostStorageResource {

    private String hostId;

    public BackendHostStorageResource(String hostId) {
        super(Storage.class, LUNs.class);
        this.hostId = hostId;
    }

    public HostStorage list() {
        HostStorage ret = new HostStorage();

        for (LUNs lun : getLogicalUnits()) {
            Storage storage = map(lun);
            ArrayList<StorageServerConnections> lunConnections = lun.getLunConnections();
            if (lunConnections!=null && !lunConnections.isEmpty()) {
                getMapper(StorageServerConnections.class, LogicalUnit.class).map(lunConnections.get(0), storage.getLogicalUnits().get(0));
            }
            ret.getStorage().add(addLinks(storage));
        }

        for (storage_domains vg : getVolumeGroups()) {
            ret.getStorage().add(addLinks(map(vg)));
        }

        return ret;
    }

    public StorageResource getStorageSubResource(String id) {
        return new BackendStorageResource(id, this);
    }

    protected Storage lookupStorage(String id) {
        for (LUNs lun : getLogicalUnits()) {
            if (lun.getLUN_id().equals(id)) {
                return addLinks(map(lun));
            }
        }
        for (storage_domains vg : getVolumeGroups()) {
            if (vg.getstorage().equals(id)) {
                return addLinks(map(vg));
            }
        }
        return notFound();
    }

    protected List<LUNs> getLogicalUnits() {
        return getBackendCollection(VdcQueryType.GetDeviceList,
                                    new GetDeviceListQueryParameters(asGuid(hostId), StorageType.UNKNOWN));
    }

    protected List<storage_domains> getVolumeGroups() {
        return getBackendCollection(storage_domains.class,
                                    VdcQueryType.GetVgList,
                                    new VdsIdParametersBase(asGuid(hostId)));
    }

    protected Storage map(storage_domains entity) {
        return getMapper(storage_domains.class, Storage.class).map(entity, null);
    }

    @Override
    protected Storage addParents(Storage storage) {
        storage.setHost(new Host());
        storage.getHost().setId(hostId);
        return storage;
    }

    @Override
    protected Response performRemove(String id) {
       throw new UnsupportedOperationException();
    }

    @Override
    protected Storage doPopulate(Storage model, LUNs entity) {
        return model;
    }

}

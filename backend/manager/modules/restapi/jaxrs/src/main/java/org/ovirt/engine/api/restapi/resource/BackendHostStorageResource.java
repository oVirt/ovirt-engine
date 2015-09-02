package org.ovirt.engine.api.restapi.resource;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.HostStorage;
import org.ovirt.engine.api.model.LogicalUnit;
import org.ovirt.engine.api.model.Storage;
import org.ovirt.engine.api.resource.HostStorageResource;
import org.ovirt.engine.api.resource.StorageResource;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.queries.GetDeviceListQueryParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

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

        for (org.ovirt.engine.core.common.businessentities.StorageDomain vg : getVolumeGroups()) {
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
        for (org.ovirt.engine.core.common.businessentities.StorageDomain vg : getVolumeGroups()) {
            if (vg.getStorage().equals(id)) {
                return addLinks(map(vg));
            }
        }
        return notFound();
    }

    protected List<LUNs> getLogicalUnits() {
        // The checkStatus is true here for backward compatibility in order to have the LUN status
        // populated as before. We should deprecate in the future or add an option to pass false
        return getBackendCollection(VdcQueryType.GetDeviceList,
                new GetDeviceListQueryParameters(asGuid(hostId), StorageType.UNKNOWN, true, null));
    }

    protected List<org.ovirt.engine.core.common.businessentities.StorageDomain> getVolumeGroups() {
        return getBackendCollection(org.ovirt.engine.core.common.businessentities.StorageDomain.class,
                                    VdcQueryType.GetVgList,
                                    new IdQueryParameters(asGuid(hostId)));
    }

    protected Storage map(org.ovirt.engine.core.common.businessentities.StorageDomain entity) {
        return getMapper(org.ovirt.engine.core.common.businessentities.StorageDomain.class, Storage.class).map(entity, null);
    }

    @Override
    protected Storage addParents(Storage storage) {
        storage.setHost(new Host());
        storage.getHost().setId(hostId);
        return storage;
    }
}

package org.ovirt.engine.api.restapi.resource;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.HostStorage;
import org.ovirt.engine.api.model.HostStorages;
import org.ovirt.engine.api.model.LogicalUnit;
import org.ovirt.engine.api.resource.HostStorageResource;
import org.ovirt.engine.api.resource.StorageResource;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.queries.GetDeviceListQueryParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendHostStorageResource
    extends AbstractBackendCollectionResource<HostStorage, LUNs>
    implements HostStorageResource {

    private String hostId;

    public BackendHostStorageResource(String hostId) {
        super(HostStorage.class, LUNs.class);
        this.hostId = hostId;
    }

    public HostStorages list() {
        HostStorages ret = new HostStorages();

        for (LUNs lun : getLogicalUnits()) {
            HostStorage storage = map(lun);
            ArrayList<StorageServerConnections> lunConnections = lun.getLunConnections();
            if (lunConnections!=null && !lunConnections.isEmpty()) {
                getMapper(StorageServerConnections.class, LogicalUnit.class).map(lunConnections.get(0),
                        storage.getLogicalUnits().getLogicalUnits().get(0));
            }
            ret.getHostStorages().add(addLinks(storage));
        }

        for (org.ovirt.engine.core.common.businessentities.StorageDomain vg : getVolumeGroups()) {
            ret.getHostStorages().add(addLinks(map(vg)));
        }

        return ret;
    }

    public StorageResource getStorageSubResource(String id) {
        return new BackendStorageResource(id, this);
    }

    protected HostStorage lookupStorage(String id) {
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

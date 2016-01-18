package org.ovirt.engine.api.restapi.resource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.DataCenter;
import org.ovirt.engine.api.model.DiskProfile;
import org.ovirt.engine.api.model.DiskProfiles;
import org.ovirt.engine.api.model.Qos;
import org.ovirt.engine.core.common.action.DiskProfileParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.qos.QosType;
import org.ovirt.engine.core.common.businessentities.qos.StorageQos;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QosQueryParameterBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public abstract class AbstractBackendDiskProfilesResource
        extends AbstractBackendCollectionResource<DiskProfile, org.ovirt.engine.core.common.businessentities.profiles.DiskProfile> {

    public AbstractBackendDiskProfilesResource(String... subCollections) {
        super(DiskProfile.class,
                org.ovirt.engine.core.common.businessentities.profiles.DiskProfile.class,
                subCollections);
    }

    protected DiskProfiles mapCollection(List<org.ovirt.engine.core.common.businessentities.profiles.DiskProfile> entities) {
        DiskProfiles collection = new DiskProfiles();
        Map<Guid, Qos> qosMap = new HashMap<>();
        for (org.ovirt.engine.core.common.businessentities.profiles.DiskProfile entity : entities) {
            DiskProfile profile = populate(map(entity), entity);
            collection.getDiskProfiles().add(profile);
            if (entity.getQosId() != null) {
                qosMap.put(entity.getQosId(), profile.getQos());
            }
        }

        handleQosDataCenterLinks(qosMap);
        for (DiskProfile diskProfile : collection.getDiskProfiles()) {
            addLinks(diskProfile);
        }
        return collection;
    }

    /**
     * used to set qos's href (requires dc id).
     */
    private void handleQosDataCenterLinks(Map<Guid, Qos> qosMap) {
        if (!qosMap.isEmpty()) {
            List<StorageQos> list = getBackendCollection(
                    StorageQos.class,
                    VdcQueryType.GetAllQosByType,
                    new QosQueryParameterBase(null, QosType.STORAGE));
            for (StorageQos storageQos : list) {
                Qos qos = qosMap.get(storageQos.getId());
                if (qos != null) {
                    qos.setDataCenter(new DataCenter());
                    qos.getDataCenter().setId(storageQos.getStoragePoolId().toString());
                }
            }
        }
    }

    protected Response add(DiskProfile diskProfile) {
        validateParameters(diskProfile);
        org.ovirt.engine.core.common.businessentities.profiles.DiskProfile map = map(diskProfile);
        DiskProfileParameters parameters =
                new DiskProfileParameters(map);
        return performCreate(VdcActionType.AddDiskProfile,
                parameters,
                new QueryIdResolver<Guid>(VdcQueryType.GetDiskProfileById, IdQueryParameters.class));
    }

    protected abstract void validateParameters(DiskProfile diskProfile);

    protected abstract List<org.ovirt.engine.core.common.businessentities.profiles.DiskProfile> getDiskProfilesCollection();

    protected DiskProfiles performList() {
        return mapCollection(getDiskProfilesCollection());
    }
}

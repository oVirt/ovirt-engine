package org.ovirt.engine.api.restapi.resource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.DataCenter;
import org.ovirt.engine.api.model.DiskProfile;
import org.ovirt.engine.api.model.DiskProfiles;
import org.ovirt.engine.api.model.QoS;
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
        Map<Guid, QoS> qosMap = new HashMap<>();
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
    private void handleQosDataCenterLinks(Map<Guid, QoS> qosMap) {
        if (!qosMap.isEmpty()) {
            List<StorageQos> list = getBackendCollection(
                    StorageQos.class,
                    VdcQueryType.GetAllQosByType,
                    new QosQueryParameterBase(null, QosType.STORAGE));
            for (StorageQos storageQos : list) {
                QoS qos = qosMap.get(storageQos.getId());
                if (qos != null) {
                    qos.setDataCenter(new DataCenter());
                    qos.getDataCenter().setId(storageQos.getStoragePoolId().toString());
                }
            }
        }
    }

    protected Response add(DiskProfile diskProfile) {
        validateParameters(diskProfile);
        DiskProfileParameters parameters =
                new DiskProfileParameters();
        org.ovirt.engine.core.common.businessentities.profiles.DiskProfile map = map(diskProfile);
        parameters.setProfile(map);
        return performCreate(VdcActionType.AddDiskProfile,
                parameters,
                new QueryIdResolver<Guid>(VdcQueryType.GetDiskProfileById, IdQueryParameters.class));
    }

    protected abstract void validateParameters(DiskProfile diskProfile);

    @Override
    protected Response performRemove(String id) {
        org.ovirt.engine.core.common.businessentities.profiles.DiskProfile diskProfile = getDiskProfile(id);
        return performAction(VdcActionType.RemoveDiskProfile,
                new DiskProfileParameters(diskProfile,
                        diskProfile.getId()));
    }

    protected org.ovirt.engine.core.common.businessentities.profiles.DiskProfile getDiskProfile(String id) {
        return getEntity(org.ovirt.engine.core.common.businessentities.profiles.DiskProfile.class,
                VdcQueryType.GetDiskProfileById,
                new IdQueryParameters(asGuidOr404(id)),
                "DiskProfiles");
    }

    @Override
    protected DiskProfile doPopulate(DiskProfile model,
            org.ovirt.engine.core.common.businessentities.profiles.DiskProfile entity) {
        return model;
    }

    protected abstract List<org.ovirt.engine.core.common.businessentities.profiles.DiskProfile> getDiskProfilesCollection();

    protected DiskProfiles performList() {
        return mapCollection(getDiskProfilesCollection());
    }
}

package org.ovirt.engine.api.restapi.types;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.api.model.AccessProtocol;
import org.ovirt.engine.api.model.GlusterVolume;
import org.ovirt.engine.api.model.GlusterVolumeStatus;
import org.ovirt.engine.api.model.GlusterVolumeType;
import org.ovirt.engine.api.model.Option;
import org.ovirt.engine.api.model.Options;
import org.ovirt.engine.api.model.TransportType;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeOptionEntity;
import org.ovirt.engine.core.compat.Guid;

public class GlusterVolumeMapper {

    @Mapping(from = GlusterVolume.class, to = GlusterVolumeEntity.class)
    public static GlusterVolumeEntity map(GlusterVolume fromVolume, GlusterVolumeEntity toVolume) {
        GlusterVolumeEntity volume = toVolume != null ? toVolume : new GlusterVolumeEntity();

        if(fromVolume.isSetId()) {
            volume.setId(Guid.createGuidFromStringDefaultEmpty(fromVolume.getId()));
        }

        if(fromVolume.isSetName()) {
            volume.setName(fromVolume.getName());
        }

        if(fromVolume.isSetVolumeType()) {
            volume.setVolumeType(map(fromVolume.getVolumeType(), null));
        }

        if(fromVolume.isSetTransportTypes()) {
            for (TransportType transportType : fromVolume.getTransportTypes().getTransportTypes()) {
                if (transportType != null) {
                    volume.addTransportType(map(transportType, null));
                }
            }
        }

        if(fromVolume.isSetReplicaCount()) {
            volume.setReplicaCount(fromVolume.getReplicaCount());
        }

        if(fromVolume.isSetStripeCount()) {
            volume.setStripeCount(fromVolume.getStripeCount());
        }

        if(fromVolume.isSetDisperseCount()) {
            volume.setDisperseCount(fromVolume.getDisperseCount());
        }

        if(fromVolume.isSetRedundancyCount()) {
            volume.setRedundancyCount(fromVolume.getRedundancyCount());
        }

        if (fromVolume.isSetOptions()) {
            Options options = fromVolume.getOptions();
            if (options.isSetOptions()) {
                for (Option option : options.getOptions()) {
                    if (option.isSetName() && option.isSetValue()) {
                        volume.setOption(option.getName(), option.getValue());
                    }
                }
            }
        }

        return volume;
    }

    @Mapping(from = GlusterVolumeEntity.class, to = GlusterVolume.class)
    public static GlusterVolume map(GlusterVolumeEntity fromVolume, GlusterVolume toVolume) {
        GlusterVolume volume = toVolume != null ? toVolume : new GlusterVolume();

        if(fromVolume.getId() != null) {
            volume.setId(fromVolume.getId().toString());
        }

        if(fromVolume.getName() != null) {
            volume.setName(fromVolume.getName());
        }

        if(fromVolume.getVolumeType() != null) {
            volume.setVolumeType(map(fromVolume.getVolumeType(), null));
        }

        if (fromVolume.getTransportTypes() != null) {
            ArrayList<TransportType> transportTypeList = new ArrayList<>();
            for (org.ovirt.engine.core.common.businessentities.gluster.TransportType transportType : fromVolume.getTransportTypes()) {
                transportTypeList.add(map(transportType, null));
            }
            volume.setTransportTypes(new GlusterVolume.TransportTypesList());
            volume.getTransportTypes()
                    .getTransportTypes()
                    .addAll(transportTypeList);
        }

        volume.setReplicaCount(fromVolume.getReplicaCount());
        volume.setStripeCount(fromVolume.getStripeCount());
        volume.setDisperseCount(fromVolume.getDisperseCount());
        volume.setRedundancyCount(fromVolume.getRedundancyCount());

        if(fromVolume.getStatus() != null) {
            volume.setStatus(mapVolumeStatus(fromVolume.getStatus()));
        }

        if (fromVolume.getOptions() != null) {
            Options glusterOptions = new Options();
            List<Option> options = glusterOptions.getOptions();
            for (GlusterVolumeOptionEntity option : fromVolume.getOptions()) {
                options.add(mapOption(option));
            }
            volume.setOptions(glusterOptions);
        }

        return volume;
    }

    private static Option mapOption(GlusterVolumeOptionEntity fromOption) {
        Option option = new Option();
        option.setName(fromOption.getKey());
        option.setValue(fromOption.getValue());

        return option;
    }

    @Mapping(from = GlusterVolumeType.class, to = org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeType.class)
    public static org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeType map(
            GlusterVolumeType glusterVolumeType,
            org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeType template) {
        switch (glusterVolumeType) {
        case DISTRIBUTE:
            return org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeType.DISTRIBUTE;
        case REPLICATE:
            return org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeType.REPLICATE;
        case DISTRIBUTED_REPLICATE:
            return org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeType.DISTRIBUTED_REPLICATE;
        case STRIPE:
            return org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeType.STRIPE;
        case DISTRIBUTED_STRIPE:
            return org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeType.DISTRIBUTED_STRIPE;
        case STRIPED_REPLICATE:
            return org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeType.STRIPED_REPLICATE;
        case DISTRIBUTED_STRIPED_REPLICATE:
            return org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeType.DISTRIBUTED_STRIPED_REPLICATE;
        case DISPERSE:
            return org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeType.DISPERSE;
        case DISTRIBUTED_DISPERSE:
            return org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeType.DISTRIBUTED_DISPERSE;
        default:
            return null;
        }
    }

    @Mapping(from = org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeType.class, to = GlusterVolumeType.class)
    public static GlusterVolumeType map(org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeType glusterVolumeType,
            GlusterVolumeType template) {
        switch (glusterVolumeType) {
        case DISTRIBUTE:
            return GlusterVolumeType.DISTRIBUTE;
        case REPLICATE:
            return GlusterVolumeType.REPLICATE;
        case DISTRIBUTED_REPLICATE:
            return GlusterVolumeType.DISTRIBUTED_REPLICATE;
        case STRIPE:
            return GlusterVolumeType.STRIPE;
        case DISTRIBUTED_STRIPE:
            return GlusterVolumeType.DISTRIBUTED_STRIPE;
        case STRIPED_REPLICATE:
            return GlusterVolumeType.STRIPED_REPLICATE;
        case DISTRIBUTED_STRIPED_REPLICATE:
            return GlusterVolumeType.DISTRIBUTED_STRIPED_REPLICATE;
        case DISPERSE:
            return GlusterVolumeType.DISPERSE;
        case DISTRIBUTED_DISPERSE:
            return GlusterVolumeType.DISTRIBUTED_DISPERSE;
        default:
            return null;
        }
    }

    @Mapping(from = GlusterVolumeType.class, to = org.ovirt.engine.core.common.businessentities.gluster.TransportType.class)
    public static org.ovirt.engine.core.common.businessentities.gluster.TransportType map(
            TransportType transportType,
            org.ovirt.engine.core.common.businessentities.gluster.TransportType template) {
        switch (transportType) {
        case TCP:
            return org.ovirt.engine.core.common.businessentities.gluster.TransportType.TCP;
        case RDMA:
            return org.ovirt.engine.core.common.businessentities.gluster.TransportType.RDMA;
        default:
            return null;
        }
    }

    @Mapping(from = org.ovirt.engine.core.common.businessentities.gluster.TransportType.class, to = TransportType.class)
    public static TransportType map(org.ovirt.engine.core.common.businessentities.gluster.TransportType transportType,
            TransportType template) {
        switch (transportType) {
        case TCP:
            return TransportType.TCP;
        case RDMA:
            return TransportType.RDMA;
        default:
            return null;
        }
    }

    @Mapping(from = GlusterVolumeType.class, to = org.ovirt.engine.core.common.businessentities.gluster.AccessProtocol.class)
    public static org.ovirt.engine.core.common.businessentities.gluster.AccessProtocol map(
            AccessProtocol accessProtocol,
            org.ovirt.engine.core.common.businessentities.gluster.AccessProtocol template) {
        switch (accessProtocol) {
        case GLUSTER:
            return org.ovirt.engine.core.common.businessentities.gluster.AccessProtocol.GLUSTER;
        case NFS:
            return org.ovirt.engine.core.common.businessentities.gluster.AccessProtocol.NFS;
        case CIFS:
            return org.ovirt.engine.core.common.businessentities.gluster.AccessProtocol.CIFS;
        default:
            return null;
        }
    }

    @Mapping(from = org.ovirt.engine.core.common.businessentities.gluster.AccessProtocol.class, to = AccessProtocol.class)
    public static AccessProtocol map(org.ovirt.engine.core.common.businessentities.gluster.AccessProtocol accessProtocol,
            String template) {
        switch (accessProtocol) {
        case GLUSTER:
            return AccessProtocol.GLUSTER;
        case NFS:
            return AccessProtocol.NFS;
        case CIFS:
            return AccessProtocol.CIFS;
        default:
            return null;
        }
    }

    private static GlusterVolumeStatus mapVolumeStatus(org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus status) {
        switch (status) {
        case UP:
            return GlusterVolumeStatus.UP;
        case DOWN:
            return GlusterVolumeStatus.DOWN;
        default:
            return null;
        }
    }
}

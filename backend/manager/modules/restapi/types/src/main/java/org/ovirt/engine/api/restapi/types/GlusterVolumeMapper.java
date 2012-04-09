package org.ovirt.engine.api.restapi.types;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.api.model.AccessProtocols;
import org.ovirt.engine.api.model.GlusterVolume;
import org.ovirt.engine.api.model.Option;
import org.ovirt.engine.api.model.Options;
import org.ovirt.engine.api.model.TransportTypes;
import org.ovirt.engine.core.common.businessentities.gluster.AccessProtocol;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeOptionEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeType;
import org.ovirt.engine.core.common.businessentities.gluster.TransportType;
import org.ovirt.engine.core.common.utils.EnumUtils;
import org.ovirt.engine.core.compat.Guid;

public class GlusterVolumeMapper {

    @Mapping(from = GlusterVolume.class, to = GlusterVolumeEntity.class)
    public static GlusterVolumeEntity map(GlusterVolume fromVolume, GlusterVolumeEntity toVolume) {
        GlusterVolumeEntity volume = toVolume != null ? toVolume : new GlusterVolumeEntity();

        if(fromVolume.isSetId()) {
            volume.setId(Guid.createGuidFromString(fromVolume.getId()));
        }

        if(fromVolume.isSetName()) {
            volume.setName(fromVolume.getName());
        }

        if(fromVolume.isSetVolumeType()) {
            volume.setVolumeType(GlusterVolumeType.valueOf(fromVolume.getVolumeType()));
        }

        if(fromVolume.isSetTransportTypes()) {
            for(String transportType : fromVolume.getTransportTypes().getTransportTypes()) {
                volume.addTransportType(TransportType.valueOf(transportType));
            }
        }

        if(fromVolume.isSetAccessProtocols()) {
            for(String accessProtocol : fromVolume.getAccessProtocols().getAccessProtocols()) {
                volume.addAccessProtocol(AccessProtocol.valueOf(accessProtocol));
            }
        }

        if (fromVolume.isSetAccessControlList()) {
            volume.setAccessControlList(StringUtils.join(
                    fromVolume.getAccessControlList().getAccessControlList(), ","));
        }

        if(fromVolume.isSetReplicaCount()) {
            volume.setReplicaCount(fromVolume.getReplicaCount());
        }

        if(fromVolume.isSetStripeCount()) {
            volume.setStripeCount(fromVolume.getStripeCount());
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
            volume.setVolumeType(fromVolume.getVolumeType().name());
        }

        if (fromVolume.getTransportTypes() != null) {
            volume.setTransportTypes(new TransportTypes());
            volume.getTransportTypes()
                    .getTransportTypes()
                    .addAll(EnumUtils.enumCollectionToStringList(fromVolume.getTransportTypes()));
        }

        if (fromVolume.getAccessProtocols() != null) {
            volume.setAccessProtocols(new AccessProtocols());
            volume.getAccessProtocols()
                    .getAccessProtocols()
                    .addAll(EnumUtils.enumCollectionToStringList(fromVolume.getAccessProtocols()));
        }

        if(fromVolume.getAccessControlList() != null) {
            volume.getAccessControlList()
                    .getAccessControlList()
                    .addAll(Arrays.asList(fromVolume.getAccessControlList().split(",")));
        }

        volume.setReplicaCount(fromVolume.getReplicaCount());
        volume.setStripeCount(fromVolume.getStripeCount());

        if(fromVolume.getStatus() != null) {
            volume.setState(fromVolume.getStatus().name());
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
}
